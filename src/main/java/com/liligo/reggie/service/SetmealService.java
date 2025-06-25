package com.liligo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liligo.reggie.dto.SetmealDto;
import com.liligo.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal>{
    // 新增套餐，同时保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    // 删除套餐，同时删除套餐和菜品的关联关系
    public void removeWithDish(List<Long> ids);
}
