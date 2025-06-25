package com.liligo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liligo.reggie.dto.DishDto;
import com.liligo.reggie.entity.Dish;
import com.liligo.reggie.entity.DishFlavor;
import com.liligo.reggie.mapper.DishMapper;
import com.liligo.reggie.service.DishFlavorService;
import com.liligo.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.util.List;


@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private ResourceUrlProvider resourceUrlProvider;

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDto the dish data transfer object containing dish and flavor information
     */
    @Override
    @Transactional
    public void addWithFlavor(DishDto dishDto) {
        // 1. 保存菜品基本信息到菜品表dish
        this.save(dishDto);

        // 2. 保存菜品口味到菜品口味表dish_flavor
        // 2.1 遍历 dishDto.getFlavors()，设置每个口味的 dishId
        for (DishFlavor flavor : dishDto.getFlavors()) {
            flavor.setDishId(dishDto.getId());
        }
        log.info("dishDto.flavors: {}", dishDto);
        // 2.2 批量保存口味数据
        dishFlavorService.saveBatch(dishDto.getFlavors());

    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id the id of the dish to be queried
     * @return a Result object containing the DishDto with dish and flavor information
     */
    @Override
    public DishDto getByIdWithCategory(Long id) {
        DishDto dishDto = new DishDto();

        // 1. 查询菜品基本信息
        Dish dish = this.getById(id);
        BeanUtils.copyProperties(dish, dishDto);

        // 2. 查询菜品对应的口味信息
        // 2.1 创建条件构造器
        LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
        flavorQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> dishFlavors = dishFlavorService.list(flavorQueryWrapper);

        // 2.2 设置口味信息到 dishDto
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    /**
     * 修改菜品信息，同时更新对应的口味数据
     *
     * @param dishDto the dish data transfer object containing updated dish and flavor information
     * @return a boolean indicating success or failure of the update operation
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 1. 更新菜品基本信息到菜品表dish
        this.updateById(dishDto);

        // 2. 更新菜品口味到菜品口味表dish_flavor
        // 2.1 清理当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
        flavorQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(flavorQueryWrapper);
        // 2.2 遍历 dishDto.getFlavors()，设置每个口味的 dishId
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        for (DishFlavor flavor : dishFlavors) {
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(dishFlavors);
    }
}
