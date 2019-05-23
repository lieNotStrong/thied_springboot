package com.neuedu.controller.backend;


import com.neuedu.common.Constant;
import com.neuedu.common.RoleEnum;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.User;
import com.neuedu.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/manage/category")
public class CategoryManageController {


    @Autowired
    ICategoryService iCategoryService;
    /**
     * 获取品类子节点(平级 -->下一级)
     * */
    @RequestMapping(value = "/get_category.do")
    public ServerResponse  get_category(HttpSession session,Integer categoryId){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        //查看子类别

        return iCategoryService.get_category(categoryId);

    }



    /**
     * 添加一个节点
     * */
    @RequestMapping(value = "/add_category.do")
    public ServerResponse  add_category(HttpSession session,
                                        @RequestParam(required = false,defaultValue = "0") Integer parentId,
                                        String categoryName){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        return iCategoryService.create_category(parentId,categoryName);

    }


    /**
     * 修改节点
     * */
    @RequestMapping(value = "/set_category_name.do")
    public ServerResponse  set_category_name(HttpSession session, Integer categoryId,String categoryName){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        return iCategoryService.set_category_name(categoryId,categoryName);

    }

    /**
     * 获取当前分类id及递归子节点categoryId
     * */
    @RequestMapping(value = "/get_deep_category.do")
    public ServerResponse  get_deep_category(HttpSession session, Integer categoryId){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.NO_LOGIN,"用户未登录");
        }
        if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
            return ServerResponse.serverResponseByError(Constant.NO_POWER,"没有登录权限");
        }

        return iCategoryService.get_deep_category(categoryId);

    }

}
