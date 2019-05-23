package com.neuedu.service;

import com.neuedu.common.ServerResponse;
import com.neuedu.pojo.Product;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

public interface IProductService {

    //新增或修改商品
    ServerResponse saveOrUpdate(Product product);
    //商品的上下架
    ServerResponse set_sale_status(Integer productId,Integer status);
    //查看商品详情
    ServerResponse detail(Integer productId);
    //后台商品列表-分页
    ServerResponse list(Integer pageNum,Integer pageSize);
    //后台搜索商品
    ServerResponse search(Integer productId,String productName,Integer pageNum,Integer pageSize);
    //前台商品详情
    ServerResponse detail_portal(Integer productId);
    //前台-商品搜索排序  orderBy:排序字段
    ServerResponse list_portal(Integer categoryId, String keyword, Integer pageNum, Integer pageSize, String orderBy);
}
