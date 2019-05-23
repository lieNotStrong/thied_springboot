package com.neuedu.controller.backend;

import com.neuedu.common.Constant;
import com.neuedu.common.RoleEnum;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.User;
import com.neuedu.service.IUserService;
import com.neuedu.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 后台用户控制器类
 * */
@RestController
@RequestMapping(value = "/manage/user")
public class UserManageController {


    @Autowired
    IUserService iUserService;

    /**
     * 后台用户登录
     * */
    @RequestMapping(value = "/login.do")
    public ServerResponse login(HttpSession session, @RequestParam("username") String username,
                                @RequestParam("password") String password){

        ServerResponse serverResponse = iUserService.login(username,password);
        if (serverResponse.isSuccess()){
            User user = (User) serverResponse.getDate();
            if (user.getRole()== RoleEnum.ROLE_USER.getRole()){
                return ServerResponse.serverResponseByError("无权限登录");
            }
            session.setAttribute(Constant.CURRENTUSER,user);
        }

        return serverResponse;
    }


    /**
     * 注册
     * */
    @RequestMapping(value = "/admin_register.do")
    public ServerResponse admin_register(HttpSession session,User user){

        return iUserService.admin_register(user);
    }

}
