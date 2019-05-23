package com.neuedu.service.impl;

import com.google.common.collect.Sets;
import com.neuedu.common.Constant;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.CategoryMapper;
import com.neuedu.pojo.Category;
import com.neuedu.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    CategoryMapper categoryMapper;


    @Override
    public ServerResponse get_category(Integer categoryId) {

        //step1:参数的非空判断
        if (categoryId==null){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2：根据categoryId查询类型
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category==null){
            return ServerResponse.serverResponseByError(Constant.NO_TYPE_FOR_THISID,"没有此ID对应的类型");
        }
        //step3：查询子类别

        List<Category> childCategory = categoryMapper.selectChildCategory(categoryId);
        if (childCategory==null){
            return ServerResponse.serverResponseByError("此类别已经没有子类别了！");
        }
        return ServerResponse.serverResponseBySuccess(childCategory);
    }

    @Override
    public ServerResponse create_category(Integer parentId, String categoryName) {

        //step1：参数的非空判断
        if (categoryName==null||categoryName.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2:添加节点
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(1);
        int result = categoryMapper.insert(category);
        if (result<=0){
            return ServerResponse.serverResponseByError("类别添加失败");
        }
        return ServerResponse.serverResponseBySuccess();
    }

    @Override
    public ServerResponse set_category_name(Integer categoryId, String categoryName) {

        //step1:参数的非空判断

        if (categoryId==null||categoryId.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (categoryName==null||categoryName.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }

        //step2：查询categoryId是否存在
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category==null){
            return ServerResponse.serverResponseByError("要查询的类别id不存在");
        }
        //修改
        category.setName(categoryName);
        int result = categoryMapper.updateByPrimaryKey(category);
        if (result<=0){
            return ServerResponse.serverResponseByError("修改失败");
        }
        return ServerResponse.serverResponseBySuccess();


    }

    @Override
    public ServerResponse get_deep_category(Integer categoryId) {

        //step1:参数非空校验
        if (categoryId==null){
            return ServerResponse.serverResponseByError("类别id不能为空");
        }
        //step2：查询
        Set<Category> categorySet = Sets.newHashSet();
        categorySet = findAllChildCategory(categorySet,categoryId);

        Set<Integer> integerSet = Sets.newHashSet();

        Iterator<Category> categoryIterator = categorySet.iterator();
        while (categoryIterator.hasNext()){
            Category category = categoryIterator.next();
            integerSet.add(category.getId());
        }

        return ServerResponse.serverResponseBySuccess(integerSet);
    }

    private Set<Category> findAllChildCategory(Set<Category> categorySet,Integer categoryId){


        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
            categorySet.add(category);
        }
        //查找categoryId下的子节点（儿子）
        List<Category> categoryList = categoryMapper.selectChildCategory(categoryId);
        if (categoryList!=null&&categoryList.size()>0){
            for (Category category1:categoryList){
                findAllChildCategory(categorySet,category1.getId());
            }
        }
        return categorySet;
    }
}
