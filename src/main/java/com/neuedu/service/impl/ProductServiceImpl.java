package com.neuedu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.neuedu.common.Constant;
import com.neuedu.common.GoodsStates;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.CategoryMapper;
import com.neuedu.dao.ProductMapper;
import com.neuedu.pojo.Category;
import com.neuedu.pojo.Product;
import com.neuedu.service.ICategoryService;
import com.neuedu.service.IProductService;
import com.neuedu.utils.DateUtils;
import com.neuedu.utils.PropertiesUtils;
import com.neuedu.vo.ProductDetalVO;
import com.neuedu.vo.ProductListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    ICategoryService categoryService;

    @Override
    public ServerResponse saveOrUpdate(Product product) {

        //step1：参数的非空校验
        if (product==null){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2:图片的上传
        String subImages = product.getSubImages();
        if (subImages!=null&&!subImages.equals("")){
            String[] subImageArr = subImages.split(",");
            if (subImageArr.length>0){
                product.setMainImage(subImageArr[0]);
            }
        }

        //step3:判断是添加还是修改
        if (product.getId()==null){
            //添加
            int result = productMapper.insert(product);
            if (result<=0){
                return ServerResponse.serverResponseByError("添加失败");
            }else {
                return ServerResponse.serverResponseBySuccess();
            }

        }else {
            //更新
            int result = productMapper.updateProductKeySelective(product);
            if (result<=0){
                return ServerResponse.serverResponseByError("更新失败");
            }else {
                return ServerResponse.serverResponseBySuccess();
            }
        }
    }

    @Override
    public ServerResponse set_sale_status(Integer productId, Integer status) {

        //step1:参数的非空判断
        if (productId==null||productId.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (status==null||status.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }

        //step2:更新商品的状态
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int result = productMapper.updateProductKeySelective(product);
       if (result>0){
           return ServerResponse.serverResponseBySuccess();
       }else {
           return ServerResponse.serverResponseByError("更新失败");
       }
    }

    @Override
    public ServerResponse detail(Integer productId) {

        //step1：参数校验
        if (productId==null){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2:查询product
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product==null){
            return ServerResponse.serverResponseByError("商品不存在");
        }
        //step3:将product-->productDetailVO
        ProductDetalVO productDetalVO = assembleProductDetailVO(product);
        //step4:返回结果
        return ServerResponse.serverResponseBySuccess(productDetalVO);
    }

    private ProductDetalVO assembleProductDetailVO(Product product){
        ProductDetalVO productDetalVO = new ProductDetalVO();
        productDetalVO.setCategoryId(product.getCategoryId());
        productDetalVO.setCreateTime(DateUtils.dateToStr(product.getCreateTime()));
        productDetalVO.setDetail(product.getDetail());
        productDetalVO.setImageHost(PropertiesUtils.readByKey("imageHost"));
        productDetalVO.setName(product.getName());
        productDetalVO.setMainImage(product.getMainImage());
        productDetalVO.setId(product.getId());
        productDetalVO.setPrice(product.getPrice());
        productDetalVO.setStatus(product.getStatus());
        productDetalVO.setStock(product.getStock());
        productDetalVO.setSubImages(product.getSubImages());
        productDetalVO.setSubtitle(product.getSubtitle());
        productDetalVO.setUpdateTime(DateUtils.dateToStr(product.getUpdateTime()));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category!=null){
            productDetalVO.setParentCategoryId(category.getParentId());
        }else {
            //默认根节点
            productDetalVO.setParentCategoryId(0);
        }
        return productDetalVO;
    }

    @Override
    public ServerResponse list(Integer pageNum, Integer pageSize) {


        PageHelper.startPage(pageNum,pageSize);

        List<Product> productList = productMapper.selectAll();

        List<ProductListVO> productListVOList = Lists.newArrayList();
        if (productList!=null&&productList.size()>0){
            for (Product product:productList){

                ProductListVO productListVO = assembleProductListVO(product);
                productListVOList.add(productListVO);
            }
        }
        PageInfo pageInfo = new PageInfo(productListVOList);

        return ServerResponse.serverResponseBySuccess(pageInfo);
    }



    private ProductListVO assembleProductListVO(Product product){
        ProductListVO productListVO = new ProductListVO();
        productListVO.setId(product.getId());
        productListVO.setCategoryId(product.getCategoryId());
        productListVO.setMainImage(product.getMainImage());
        productListVO.setName(product.getName());
        productListVO.setPrice(product.getPrice());
        productListVO.setStatus(product.getStatus());
        productListVO.setSubtitle(product.getSubtitle());

        return productListVO;
    }

    @Override
    public ServerResponse search(Integer productId, String productName,
                                 Integer pageNum, Integer pageSize) {



        if (productName!=null&&!productName.equals("")){
            productName= "%"+productName+"%";
        }
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.findProductByProductIdAndProductName(productId,productName);
        List<ProductListVO> productListVOList = Lists.newArrayList();
        if (productList!=null&&productList.size()>0){
            for (Product product:productList){
                ProductListVO productListVO = assembleProductListVO(product);
                productListVOList.add(productListVO);
            }
        }
        PageInfo pageInfo = new PageInfo( productListVOList);

        return ServerResponse.serverResponseBySuccess(pageInfo);
    }

    @Override
    public ServerResponse detail_portal(Integer productId) {

        //step1：参数校验
        if (productId==null){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2:查询product
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product==null){
            return ServerResponse.serverResponseByError("商品不存在");
        }
        //step3:检验商品状态

        if (product.getStatus()!= GoodsStates.PRODUCT_ONLINE.getRole()){
            return ServerResponse.serverResponseByError("商品下架或删除");
        }
        //step4：获取productDetalVO
        ProductDetalVO productDetalVO = assembleProductDetailVO(product);
        //step5：返回结果
        return ServerResponse.serverResponseBySuccess(productDetalVO);
    }

    @Override
    public ServerResponse list_portal(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy) {

        Set<Integer> integerSet = Sets.newHashSet();
        //step1:参数校验，categoryId和keyword不能同时为空
        if (categoryId==null&&(keyword==null||keyword.equals(""))){
            return ServerResponse.serverResponseByError("至少输入一个查询条件");
        }
        //step2：如果categoryId不为空keyword为空
        if (categoryId!=null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category==null&&(keyword==null||keyword.equals(""))){
                PageHelper.startPage(pageNum,pageSize);
                ArrayList<ProductListVO> productListVOList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVOList);
                return ServerResponse.serverResponseBySuccess(pageInfo);
            }


            ServerResponse serverResponse = categoryService.get_deep_category(categoryId);

            if (serverResponse.isSuccess()){
                integerSet =(Set<Integer>) serverResponse.getDate();
            }
        }
        if (keyword!=null&&!keyword.equals("")){
            keyword="%"+keyword+"%";
        }
        if (orderBy.equals("")) {
            PageHelper.startPage(pageNum, pageSize);
        }else {
            String[] orderByArr = orderBy.split("_");
            if (orderByArr.length>1){
                PageHelper.startPage(pageNum,pageSize,orderByArr[0]+""+orderByArr[1]);
            }else {
                PageHelper.startPage(pageNum, pageSize);
            }
        }


        List<Product> productList = productMapper.searchProduct(integerSet, keyword);

        List<ProductListVO> productListVOList = Lists.newArrayList();
        if (productList!=null&&productList.size()>0){
            for (Product product:productList){
                ProductListVO productListVO = assembleProductListVO(product);
                productListVOList.add(productListVO);
            }
        }


        //分页
        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(productListVOList);


        return ServerResponse.serverResponseBySuccess(pageInfo);
    }

}
