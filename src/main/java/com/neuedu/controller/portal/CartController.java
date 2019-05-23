package com.neuedu.controller.portal;

import com.neuedu.common.Constant;
import com.neuedu.common.EnumClass;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.User;
import com.neuedu.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    ICartService cartService;

    @RequestMapping(value = "/add.do")
    public ServerResponse add(HttpSession session,Integer productId, Integer count){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.add(user.getId(),productId,count);
    }


    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.list(user.getId());
    }

    /**
     * 更新购物车中某件商品的数量
     * */
    @RequestMapping(value = "/update.do")
    public ServerResponse update(HttpSession session,Integer productId,Integer count){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.update(user.getId(),productId,count);
    }

    /**
     * 移除购物车某个产品
     * */
    @RequestMapping(value = "/delete_product.do")
    public ServerResponse delete_product(HttpSession session,String productIds){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.delete_product(user.getId(),productIds);
    }

    /**
     * 购物车中选中某个商品
     * */
    @RequestMapping(value = "/select.do")
    public ServerResponse select(HttpSession session,Integer productId){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.select(user.getId(),productId, EnumClass.CartCheckedEnum.PRODUCT_CHECKED.getStatus());
    }

    /**
     * 购物车中取消选中某个商品
     * */
    @RequestMapping(value = "/un_select.do")
    public ServerResponse un_select(HttpSession session,Integer productId){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.select(user.getId(),productId,EnumClass.CartCheckedEnum.PRODUCT_UNCHECKED.getStatus());
    }


    /**
     * 购物车中全选商品
     * */
    @RequestMapping(value = "/select_all.do")
    public ServerResponse select_all(HttpSession session){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.select(user.getId(),null,EnumClass.CartCheckedEnum.PRODUCT_CHECKED.getStatus());
    }

    /**
     * 购物车中取消全选商品
     * */
    @RequestMapping(value = "/un_select_all.do")
    public ServerResponse un_select_all(HttpSession session){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.select(user.getId(),null,EnumClass.CartCheckedEnum.PRODUCT_UNCHECKED.getStatus());
    }

    /**
     * 购物车中产品的数量
     * */
    @RequestMapping(value = "/get_cart_product_count.do")
    public ServerResponse get_cart_product_count(HttpSession session){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("请登录");
        }

        return cartService.get_cart_product_count(user.getId());
    }


}
