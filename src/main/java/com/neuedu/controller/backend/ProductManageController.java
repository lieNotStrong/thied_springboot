package com.neuedu.controller.backend;


import com.neuedu.common.Constant;
import com.neuedu.common.RoleEnum;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Product;
import com.neuedu.pojo.User;
import com.neuedu.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/manage/product")
public class ProductManageController {

    @Autowired
    IProductService productService;

    /**
     * 添加或者更新商品
     * */
    @RequestMapping(value = "/save.do")
    public ServerResponse saveOrUpdate(HttpSession session, Product product){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        return productService.saveOrUpdate(product);
    }

    /**
     * 产品的上下架
     * */

    @RequestMapping(value = "/set_sale_status.do")
    public ServerResponse set_sale_status(HttpSession session, Integer productId,Integer status){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        return productService.set_sale_status(productId,status);
    }



    /**
     *  查看商品详情
     * */

    @RequestMapping(value = "/detail.do")
    public ServerResponse detail(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        return productService.detail(productId);
    }

    /**
     * 分页查看商品
     * */

    @RequestMapping(value = "/list.do")
    public ServerResponse list(HttpSession session,
                                 @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                                 @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        return productService.list(pageNum,pageSize);
    }


    /**
     * 后台-产品搜素
     * */
    @RequestMapping(value = "/search.do")
    public ServerResponse search(HttpSession session,
                                 @RequestParam(value = "productId",required = false) Integer productId,
                                 @RequestParam(value = "productName",required = false) String productName,
                                 @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                                 @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        return productService.search(productId,productName,pageNum,pageSize);
    }
}
