package com.neuedu.common;

public enum GoodsStates {



     PRODUCT_ONLINE(1,"商品在售"),

     PRODUCT_OFFLINE(2,"商品下架"),

     PRODUCT_DELETE(3,"商品删除")
    ;
    private int role;
    private String desc;

    GoodsStates(int role, String desc) {
        this.role = role;
        this.desc = desc;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }




}
