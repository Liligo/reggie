package com.liligo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liligo.reggie.common.CustomException;
import com.liligo.reggie.entity.Category;
import com.liligo.reggie.entity.Dish;
import com.liligo.reggie.entity.Setmeal;
import com.liligo.reggie.mapper.CategoryMapper;
import com.liligo.reggie.service.CategoryService;
import com.liligo.reggie.service.DishService;
import com.liligo.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     * @param id
     */
    @Override
    public boolean remove(Long id) {
        // 查询当前分类是否关联了菜品，如果已经关联，则抛出业务异常
        // 1. 创建查询条件构造器
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        // 2. 设置查询条件，根据分类id进行查询
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        // 3. 查询当前分类是否关联了菜品
        long dishCount = dishService.count(dishQueryWrapper);
        if (dishCount > 0) {
            // 如果关联了菜品，则抛出业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 查询当前分类是否关联了套餐，如果已经关联，则抛出业务异常
        // 1. 创建查询条件构造器
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        // 2. 设置查询条件，根据分类id进行查询
        setmealQueryWrapper.eq(Setmeal::getCategoryId, id);
        // 3. 查询当前分类是否关联了套餐
        long setmealCount = setmealService.count(setmealQueryWrapper);

        if (setmealCount > 0) {
            // 如果关联了套餐，则抛出业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        // 如果都没有关联，则可以删除分类
        // 调用父类的removeById方法进行删除
        super.removeById(id);
        return true;
    }
}
