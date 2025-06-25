package com.liligo.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liligo.reggie.common.Result;
import com.liligo.reggie.dto.SetmealDto;
import com.liligo.reggie.entity.Setmeal;
import com.liligo.reggie.entity.SetmealDish;
import com.liligo.reggie.service.CategoryService;
import com.liligo.reggie.service.SetmealDishService;
import com.liligo.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐，保存套餐和菜品的关联关系
     * @param setmealDto 套餐数据传输对象
     * @return 成功消息
     */
    @PostMapping
    public Result<String> add(@RequestBody SetmealDto setmealDto) {
        log.info("新增套餐: {}", setmealDto);

        setmealService.saveWithDish(setmealDto);

        return Result.success("新增套餐成功");
    }


    /**
     * 分页查询
     * @param page 页码
     * @param pageSize 每页显示的记录数
     * @param name 套餐名称
     * @return 分页查询结果
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);
        // 构造分页查询条件
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 执行分页查询
        setmealService.page(setmealPage, queryWrapper);

        // 将查询结果转换为 SetmealDto
        Page<SetmealDto> setmealDtoPage = new Page<>();
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        List<Setmeal> setmealRecords = setmealPage.getRecords();
        List<SetmealDto> setmealDtoRecords = new ArrayList<>();
        for (Setmeal setmeal : setmealRecords) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            setmealDto.setCategoryName(categoryService.getById(setmeal.getCategoryId()).getName());
            setmealDtoRecords.add(setmealDto);
        }

        setmealDtoPage.setRecords(setmealDtoRecords);

        return Result.success(setmealDtoPage);
    }

    /**
     * 根据条件查询套餐和菜品的关联关系
     *
     * @return 套餐数据传输对象
     */
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal) {
        // 设置查询条件
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        setmealQueryWrapper.eq(setmeal.getStatus()!= null, Setmeal::getStatus, setmeal.getStatus());
        setmealQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList =  setmealService.list(setmealQueryWrapper);

        return Result.success(setmealList);
    }

    /**
     * 删除套餐和菜品的关联关系
     *
     * @param ids  套餐ID列表
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("删除套餐: {}", ids);

        setmealService.removeWithDish(ids);
        return Result.success("删除套餐成功");
    }
}
