package com.neuedu.controller.portal;

import com.neuedu.common.Constant;
import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.User;
import com.neuedu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 前台用户控制器类
 * */
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    IUserService iUserService;

    /**
     * 登录
     * */
    @RequestMapping(value = "/login.do")
    public ServerResponse login(HttpSession session,@RequestParam("username") String username,
                                @RequestParam("password") String password){

        ServerResponse serverResponse = iUserService.login(username,password);
        if (serverResponse.isSuccess()){
            User user = (User) serverResponse.getDate();
            session.setAttribute(Constant.CURRENTUSER,user);
        }

        return serverResponse;
    }


    /**
     * 注册
     * */
    @RequestMapping(value = "/register.do")
    public ServerResponse register(HttpSession session,User user){

        return iUserService.register(user);
    }


    /**
     * 根据用户名获取密保问题
     * */
    @RequestMapping(value = "/forget_get_question.do")
    public ServerResponse forget_get_question(String username){

        return iUserService.forget_get_question(username);
    }


    /**
     * 提交问题答案
     * */
    @RequestMapping(value = "/forget_check_answer.do")
    public ServerResponse forget_check_answer(String username,String question,String answer){

        return iUserService.forget_check_answer(username,question,answer);
    }

    /**
     * 重置密码
     * */
    @RequestMapping(value = "/forget_reset_password.do")
    public ServerResponse forget_reset_password(String username,String passwordNew,String forgetToken){

        return iUserService.forget_reset_password(username,passwordNew,forgetToken);
    }

    /**
     * 检查用户名或邮箱是否有效
     * */
    @RequestMapping(value = "/check_valid.do")
    public ServerResponse check_valid(String str,String type){

        return iUserService.check_valid(str,type);
    }

    /**
     * 获取登录用户信息
     * */
    @RequestMapping(value = "/get_user_info.do")
    public ServerResponse get_user_info(HttpSession session){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录或登录已过期");
        }

        user.setPassword("");
        user.setQuestion("");
        user.setAnswer("");
        user.setRole(null);
        return ServerResponse.serverResponseBySuccess(user);
    }


    /**
     * 登录状态下重置(修改)密码
     * */
    @RequestMapping(value = "/reset_password.do")
    public ServerResponse reset_password(HttpSession session,String passwordOld,String passwordNew){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录或登录已过期");
        }
        return iUserService.reset_password(user.getUsername(),passwordOld,passwordNew);
    }


    /**
     * 登录状态更新个人信息
     * */
    @RequestMapping(value = "/update_information.do")
    public ServerResponse update_information(HttpSession session,User userInfo){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录或登录已过期");
        }
        userInfo.setId(user.getId());
        ServerResponse serverResponse =  iUserService.update_information(userInfo);
        if (serverResponse.isSuccess()){

            User result = iUserService.findUserByUserId(user.getId());
            session.setAttribute(Constant.CURRENTUSER,result);
        }
        return serverResponse;
    }


    /**
     * 获取登录用户详细信息
     * */
    @RequestMapping(value = "/get_information.do")
    public ServerResponse get_information(HttpSession session){

        User user = (User) session.getAttribute(Constant.CURRENTUSER);
        if (user==null){
            return ServerResponse.serverResponseByError("用户未登录或登录已过期");
        }

        user.setPassword("");
        return ServerResponse.serverResponseBySuccess(user);
    }


    /**
     * 退出登录
     * */
    @RequestMapping(value = "/logout.do")
    public ServerResponse logout(HttpSession session){

       session.removeAttribute(Constant.CURRENTUSER);
        return ServerResponse.serverResponseBySuccess();
    }


}
