package com.neuedu.common;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 封装服务器返回前端高复用对象
 * */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> {

    //返回前端的状态码
    private int status;
    //返回给前端的数据
    private T date;
    //当status！=0时，封装的错误信息
    private String msg;

    private ServerResponse() {

    }
    private ServerResponse(int status) {
        this.status = status;
    }
    private ServerResponse(int status, T date) {
        this.status = status;
        this.date = date;
    }
    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
    private ServerResponse(int status, T date, String msg) {
        this.status = status;
        this.date = date;
        this.msg = msg;
    }

    /**
     * 成功的方法
     * */

    public static ServerResponse serverResponseBySuccess(){
        return new ServerResponse(Constant.SUCCESS);
    }
    public static <T> ServerResponse serverResponseBySuccess(T data){
        return new ServerResponse(Constant.SUCCESS,data);
    }
    public static <T> ServerResponse serverResponseBySuccess(T data,String msg){
        return new ServerResponse(Constant.SUCCESS,data,msg);
    }

    /**
     * 接口调用失败时回调
     * */
    public static ServerResponse serverResponseByError(){
        return new ServerResponse(Constant.ERROR);
    }
    public static <T> ServerResponse serverResponseByError(int status){
        return new ServerResponse(status);
    }
    public static <T> ServerResponse serverResponseByError(String msg){
        return new ServerResponse(Constant.ERROR,msg);
    }
    public static <T> ServerResponse serverResponseByError(int status,String msg){
        return new ServerResponse(status,msg);
    }

    /**
     * 判断接口返回是否成功
     * status=0
     * */

    @JsonIgnore
    public boolean isSuccess(){
        return this.status==Constant.SUCCESS;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getDate() {
        return date;
    }

    public void setDate(T date) {
        this.date = date;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
