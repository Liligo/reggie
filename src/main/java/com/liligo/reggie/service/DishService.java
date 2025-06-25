package com.liligo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liligo.reggie.dto.DishDto;
import com.liligo.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    // 新增菜品，同时保存对应的口味数据
    public void addWithFlavor(DishDto dishDto);

    // 根据id查询菜品信息和口味信息
    public DishDto getByIdWithCategory(Long id);

    // 修改菜品信息，同时更新对应的口味数据
    public void updateWithFlavor(DishDto dishDto);
}
