package com.neuedu.service;

import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.User;

public interface IUserService {

    //登录接口
    ServerResponse login(String username,String password);
    //注册接口
    ServerResponse register(User user);
    //管理员注册
    ServerResponse admin_register(User user);
    //根据用户名获取密保问题
    ServerResponse forget_get_question(String username);
    //提交问题答案
    ServerResponse forget_check_answer(String username,String question,String answer);
    //重置密码
    ServerResponse forget_reset_password(String username,String passwordNew,String forgetToken);
    //检测用户名或邮箱是否有效
    ServerResponse check_valid(String str,String type);
    //登录状态下修改密码
    ServerResponse reset_password(String username,String passwordOld,String passwordNew);
    //登录状态下更新个人信息
    ServerResponse update_information(User userInfo);
    //根据userid查询用户信息
    User findUserByUserId(Integer userId);
}
