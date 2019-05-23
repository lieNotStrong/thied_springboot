package com.neuedu.common;

public class Constant {

    /**
     * 成功时的status值
     * */
    public static final int SUCCESS=0;
    //统一的错误码
    public static final int ERROR=100;

    //当前用户
    public static final String CURRENTUSER="current_user";

    //注册参数为空
    public static final int PARAM_NOT_NULL=1;

    //用户名已存在
    public static final int USERNAME_EXISTS=2;

    //邮箱已存在
    public static final int EMAIL_EXISTS=3;

    //注册失败
    public static final int EXISTS_FAIL=4;

    //用户名不存在
    public static final int USERNAME_NOT_EXIST=5;

    //密保问题为空
    public static final int QUESTION_ISNULL=6;

    //修改密码成功
    public static final int CHANGE_PASSWORD_SUCCESS=7;

    //参数类型错误
    public static final int PARAM_TYPE_ERROR=8;

    //用户未登录
    public static final int NO_LOGIN=9;

    //用户没有访问权限
    public static final int NO_POWER=10;

    //没有此id对应的类型
    public static final int NO_TYPE_FOR_THISID=11;



}
