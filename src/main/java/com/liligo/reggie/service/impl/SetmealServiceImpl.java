package com.liligo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liligo.reggie.dto.SetmealDto;
import com.liligo.reggie.entity.Setmeal;
import com.liligo.reggie.entity.SetmealDish;
import com.liligo.reggie.mapper.SetmealMapper;
import com.liligo.reggie.service.SetmealDishService;
import com.liligo.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);

        // 保存套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时删除套餐和菜品的关联关系
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，确认是否可以删除
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.in(Setmeal::getId, ids);
        setmealQueryWrapper.eq(Setmeal::getStatus, 1);
        Long count = this.count(setmealQueryWrapper);
        if (count > 0) {
            throw new RuntimeException("套餐正在售卖中，不能删除");
        }

        // 删除套餐
        // 1. 删除套餐表中的数据
        this.removeByIds(ids);

        // 2. 删除套餐和菜品的关联关系
        LambdaQueryWrapper<SetmealDish> setmealDishQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishQueryWrapper.in(SetmealDish::getId, ids);
        setmealDishService.remove(setmealDishQueryWrapper);
    }
}
