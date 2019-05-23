package com.neuedu.service.impl;

import com.google.common.collect.Lists;
import com.neuedu.common.Constant;
import com.neuedu.common.EnumClass;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.CartMapper;
import com.neuedu.dao.ProductMapper;
import com.neuedu.pojo.Cart;
import com.neuedu.pojo.Product;
import com.neuedu.service.ICartService;
import com.neuedu.utils.BigDecimalUtils;
import com.neuedu.vo.CartProductVO;
import com.neuedu.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductMapper productMapper;
    @Override
    public ServerResponse add(Integer userId,Integer productId, Integer count) {

        //step1:参数的的非空校验

        if (productId==null||count==null){
            return ServerResponse.serverResponseByError("参数不能为空");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product==null){
            return ServerResponse.serverResponseByError("要添加的商品不存在");
        }

        //step2：根据userId和productId查询购物信息

        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart==null){
            //添加购物车
            Cart cart1 = new Cart();
            cart1.setUserId(userId);
            cart1.setProductId(productId);
            cart1.setQuantity(count);
            cart1.setChecked(EnumClass.CartCheckedEnum.PRODUCT_CHECKED.getStatus());
            cartMapper.insert(cart1);
        }else {
            //更新
            Cart cart1 = new Cart();
            cart1.setId(cart.getId());
            cart1.setUserId(userId);
            cart1.setProductId(productId);
            cart1.setQuantity(count);
            cart1.setChecked(cart.getChecked());
            cartMapper.updateByPrimaryKey(cart1);


        }
        CartVO cartVOLimit = getCartVOLimit(userId);
        return ServerResponse.serverResponseBySuccess(cartVOLimit);
    }

    @Override
    public ServerResponse list(Integer userId) {

        CartVO cartVOLimit = getCartVOLimit(userId);
        return ServerResponse.serverResponseBySuccess(cartVOLimit);
    }

    @Override
    public ServerResponse update(Integer userId, Integer productId, Integer count) {

        //step1:参数的非空判断
        if (productId==null||count==null){
            return ServerResponse.serverResponseByError("参数不能为空");
        }
        //step2：查询购物车中的商品
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart!=null){
            //step3:更新数量
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }
        //step4:返回结果
        return ServerResponse.serverResponseBySuccess(getCartVOLimit(userId));
    }

    @Override
    public ServerResponse delete_product(Integer userId, String productIds) {

        //step1:参数的非空校验
        if (productIds==null||productIds.equals("")){
            return ServerResponse.serverResponseByError("参数不能为空");
        }
        //step2：将productIds-->List<Integer>
        List<Integer> productIdList = Lists.newArrayList();
        String[] productIdsArr = productIds.split(",");
        if (productIdsArr!=null&&productIdsArr.length>0){
            for (String productIdstr:productIdsArr){
                Integer productId = Integer.parseInt(productIdstr);
                productIdList.add(productId);

            }
        }

        //step3:调用dao删除方法

        cartMapper.deleteByUseridAndProductIds(userId,productIdList);
        //step4：返回结果
        return ServerResponse.serverResponseBySuccess(getCartVOLimit(userId));
    }

    @Override
    public ServerResponse select(Integer userId, Integer productId,Integer check) {

        //step1:参数非空校验
//        if (productId==null){
//            return ServerResponse.serverResponseByError("参数不能为空");
//        }
        //step2：dao接口
        cartMapper.selectOrUnselectProduct(userId,productId,check);
        //返回结果
        return ServerResponse.serverResponseBySuccess(getCartVOLimit(userId));
    }

    @Override
    public ServerResponse get_cart_product_count(Integer userId) {

        int quantity = cartMapper.get_cart_product_count(userId);

        return ServerResponse.serverResponseBySuccess(quantity);
    }

    private CartVO getCartVOLimit(Integer userId){
        CartVO cartVO = new CartVO();
        //step1:根据userid查询购物信息-->List<Cart>
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        //step2:List<Cart> -->List<CartProcuctVO>
        List<CartProductVO> cartProductVOList = Lists.newArrayList();

        //购物车总价格
        BigDecimal carttotalPrice = new BigDecimal("0");
        if (cartList!=null&&cartList.size()>0){
            for (Cart cart:cartList){
                CartProductVO cartProductVO = new CartProductVO();
                cartProductVO.setId(cart.getId());
                cartProductVO.setQuantity(cart.getQuantity());
                cartProductVO.setUserId(userId);
                cartProductVO.setProductChecked(cart.getChecked());
                //查询商品
                Product product = productMapper.selectByPrimaryKey(cart.getProductId());
                if (product!=null){
                    cartProductVO.setProductId(cart.getProductId());
                    cartProductVO.setProductMainImage(product.getMainImage());
                    cartProductVO.setProductName(product.getName());
                    cartProductVO.setProductPrice(product.getPrice());
                    cartProductVO.setProductStatus(product.getStatus());
                    cartProductVO.setProductStock(product.getStock());
                    cartProductVO.setProductSubtitle(product.getSubtitle());
                    int stock = product.getStock();
                    int limitProductCount=0;
                    if (stock>=cart.getQuantity()){
                        limitProductCount= cart.getQuantity();
                        cartProductVO.setLimitQuantity("LIMIT_NUM_SUCCESS");
                    }else {
                        limitProductCount=stock;
                        //更新购物车中商品的数量
                        Cart cart1 = new Cart();
                        cart1.setId(cart.getId());
                        cart1.setQuantity(stock);
                        cart1.setProductId(cart.getProductId());
                        cart1.setChecked(cart.getChecked());
                        cart1.setUserId(userId);
                        cartMapper.updateByPrimaryKey(cart1);
                        cartProductVO.setLimitQuantity("LIMIT_NUM_FAIL");
                    }

                    //获取商品总价格
                    cartProductVO.setQuantity(limitProductCount);
                    cartProductVO.setProductTotalPrice(BigDecimalUtils.mul(product.getPrice().doubleValue(),Double.valueOf(cartProductVO.getQuantity())));
                }

                //计算购物车总价格
                if (cartProductVO.getProductChecked()==EnumClass.CartCheckedEnum.PRODUCT_CHECKED.getStatus()){//被选中 计算总价
                    carttotalPrice= BigDecimalUtils.add(carttotalPrice.doubleValue(),cartProductVO.getProductTotalPrice().doubleValue());
                }

                cartProductVOList.add(cartProductVO);
            }
        }
        //把总价格添加进购物车
        cartVO.setCartProductVOList(cartProductVOList);
        cartVO.setCattotalprice(carttotalPrice);

        //判断购物车是否全选
        int checkedAll = cartMapper.isCheckedAll(userId);
        if (checkedAll>0){
            cartVO.setIsallChecked(false);
        }else {
            cartVO.setIsallChecked(true);
        }
        return cartVO;
    }

}
