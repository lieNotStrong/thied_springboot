package com.neuedu.service;

import com.neuedu.common.ServerResponse;

public interface ICategoryService {

    //查看子类别(没有孙子)
    ServerResponse get_category(Integer categoryId);
    //创建子节点
    ServerResponse create_category(Integer parentId,String categoryName);
    //修改节点
    ServerResponse set_category_name(Integer categoryId,String categoryName);
    //获取当前分类id及递归子节点categoryId
    ServerResponse get_deep_category(Integer categoryId);
}
