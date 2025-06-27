package com.liligo.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liligo.reggie.common.Result;
import com.liligo.reggie.dto.DishDto;
import com.liligo.reggie.entity.Dish;
import com.liligo.reggie.entity.DishFlavor;
import com.liligo.reggie.service.CategoryService;
import com.liligo.reggie.service.DishFlavorService;
import com.liligo.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增菜品接口
     *
     * @param dishDto the dish data transfer object containing dish and flavor information
     * @return a Result object indicating success or failure
     */
    @PostMapping
    public Result<String> addDish(@RequestBody DishDto dishDto) {
        //log.info("Adding new dish: {}", dishDto);
        dishService.addWithFlavor(dishDto);

        // 清理当前菜品所属分类的Redis缓存
        String key = "dish_list_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success("新增菜品成功");
    }

    /**
     * 根据id查询对应的菜品信息和口味信息
     *
     * @param id the id of the dish to be queried
     * @return a Result object containing the DishDto with dish and flavor information
     */
    @GetMapping("/{id}")
    public Result<DishDto> getById(@PathVariable Long id) {
        log.info("Querying dish by id: {}", id);
        DishDto dishDto = dishService.getByIdWithCategory(id);


        return Result.success(dishDto);

    }

    /**
     * 修改菜品信息
     *
     * @param dishDto the dish data transfer object containing updated dish and flavor information
     * @return a Result object indicating success or failure
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("Updating dish: {}", dishDto);

        dishService.updateWithFlavor(dishDto);

        // 清理当前菜品所属分类的Redis缓存
        String key = "dish_list_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success("修改菜品成功");
    }

    /**
     * 分页查询菜品信息并展示菜品分类
     * @param page 当前页码
     * @param pageSize 每页显示的记录数
     * @param name 菜品名称（可选）
     * @return a Result object containing a Page of DishDto objects
     */
    @GetMapping("/page")
    public Result<Page<DishDto>> page(int page, int pageSize, String name) {
        // 创建dishDto分页构造器
        Page<DishDto> dishDtoPage = new Page<>();
        // 创建dish分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);

        // 创建条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.like(name != null, Dish::getName, name);

        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(dishPage, queryWrapper);

        // 拷贝dishPage对象到dishDtoPage对象，忽略records属性
        // records内存储的是查询到的dish信息，暂时忽略拷贝，因为需要对其中的属性进行修改
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        // 根据分类id查询分类名称
        // 将Dish对象转换为DishDto对象
        List<Dish> dishList = dishPage.getRecords();
        List<DishDto> dishDtoList = new ArrayList<>();

        for (Dish dish : dishList) {
            DishDto dishDto = new DishDto();
            // 拷贝属性
            BeanUtils.copyProperties(dish, dishDto);
            // 根据分类id查询分类名称
            Long categoryId = dish.getCategoryId();
            String categoryName = categoryService.getById(categoryId).getName();
            // 设置分类名称
            dishDto.setCategoryName(categoryName);
            // 添加到dishDtoList
            dishDtoList.add(dishDto);
        }

        // 存入dishDtoList
        dishDtoPage.setRecords(dishDtoList);

        return Result.success(dishDtoPage);
    }


    /**
     * 根据条件查询菜品列表
     *
     * @param dish the dish entity containing search criteria
     * @return a Result object containing a list of DishDto objects
     */
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish) {
        log.info("Querying dish list with criteria: {}", dish);

        List<DishDto> dishDtoList;
        // 先从Redis中查询菜品列表
        String redisKey = "dish_list_" + dish.getCategoryId() + "_" + dish.getStatus();
        @SuppressWarnings("unchecked") // 使用@SuppressWarnings注解明确告知编译器该转换是安全的
        List<DishDto> cachedList = (List<DishDto>) redisTemplate.opsForValue().get(redisKey);
        dishDtoList = cachedList;

        // 1 若存在，则直接返回
        if (dishDtoList != null) {
            return Result.success(dishDtoList);
        }

        // 2 若不存在，则查询数据库并存入Redis，并返回结果
        // 2.1 创建条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 2.2 添加查询条件
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1); // 只查询状态为1（启售）的菜品
        // 2.3 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList =  dishService.list(queryWrapper);

        // 2.4 遍历菜品列表，设置分类名称和口味信息
        dishDtoList = new ArrayList<>();
        for (Dish dishItem : dishList) {
            DishDto dishDto = new DishDto();
            // 拷贝属性
            BeanUtils.copyProperties(dishItem, dishDto);

            // 根据分类id查询分类名称
            Long categoryId = dishItem.getCategoryId();
            String categoryName = categoryService.getById(categoryId).getName();
            // 设置分类名称
            dishDto.setCategoryName(categoryName);

            // 查询口味信息
            LambdaQueryWrapper<DishFlavor> dishFlavorQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorQueryWrapper.eq(DishFlavor::getDishId, dishItem.getId());
            dishDto.setFlavors(dishFlavorService.list(dishFlavorQueryWrapper));
            // 添加到dishDtoList
            dishDtoList.add(dishDto);
        }

        // 2.5 将查询结果存入Redis，设置有效期为1小时
        redisTemplate.opsForValue().set(redisKey, dishDtoList, 1, TimeUnit.HOURS);
        return Result.success(dishDtoList);
    }
}
