package com.neuedu.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车实体类CartVO
 * */
public class CartVO {

    //购物信息集合
    private List<CartProductVO> cartProductVOList;
    //是否全选
    private boolean isallChecked;
    //总价格
    private BigDecimal cattotalprice;

    public List<CartProductVO> getCartProductVOList() {
        return cartProductVOList;
    }

    public void setCartProductVOList(List<CartProductVO> cartProductVOList) {
        this.cartProductVOList = cartProductVOList;
    }

    public boolean isIsallChecked() {
        return isallChecked;
    }

    public void setIsallChecked(boolean isallChecked) {
        this.isallChecked = isallChecked;
    }

    public BigDecimal getCattotalprice() {
        return cattotalprice;
    }

    public void setCattotalprice(BigDecimal cattotalprice) {
        this.cattotalprice = cattotalprice;
    }
}
