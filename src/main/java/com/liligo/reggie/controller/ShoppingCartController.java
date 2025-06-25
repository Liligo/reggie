package com.liligo.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liligo.reggie.common.BaseContext;
import com.liligo.reggie.common.Result;
import com.liligo.reggie.entity.ShoppingCart;
import com.liligo.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    ShoppingCartService shoppingCartService;


    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("Adding item to shopping cart: {}", shoppingCart);

        // 1. 设置用户ID
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setCreateTime(LocalDateTime.now());

        // 2. 查询当前菜品或套餐是否已经在购物车中
        // 创建查询条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<ShoppingCart>();
        queryWrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());

        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart savedCartDish = shoppingCartService.getOne(queryWrapper);

        // 2.1 如果在购物车中，且两次添加的菜品口味相同，则更新数量
        if (savedCartDish != null) {
            // 更新数量
            if (dishId != null ||savedCartDish.getSetmealId() != null) {
                savedCartDish.setNumber(savedCartDish.getNumber() + 1);
                shoppingCartService.updateById(savedCartDish);
                return Result.success(savedCartDish);
            }
            else {
                return Result.error("添加失败");
            }
        }else {
            // 2.2 如果不在购物车中，则添加到购物车
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            return Result.success(shoppingCart);
        }
    }

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Result<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("subtracting item from shopping cart: {}", shoppingCart);

        // 1. 设置用户ID
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setCreateTime(LocalDateTime.now());

        // 2. 查询当前菜品或套餐是否已经在购物车中
        // 创建查询条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());

        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart savedCartDish = shoppingCartService.getOne(queryWrapper);

        // 2.1 如果在购物车中，且两次添加的菜品口味相同，则更新数量
        if (savedCartDish != null) {
            // 更新数量
            if (dishId != null ||savedCartDish.getSetmealId() != null) {
                if (savedCartDish.getNumber() == 1) {
                    savedCartDish.setNumber(0);
                    shoppingCartService.remove(queryWrapper);
                    return Result.success(new ShoppingCart());
                } else {
                    savedCartDish.setNumber(savedCartDish.getNumber() - 1);
                    shoppingCartService.updateById(savedCartDish);
                }
            }
        }
        return Result.success(savedCartDish);
    }

    /**
     * 查看购物车列表
     *
     * @return
     */
    @RequestMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("Viewing shopping cart list");
        // 1. 获取当前用户ID
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        // 2. 设置查询条件
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        // 3. 查询购物车列表
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        return Result.success(shoppingCartList);
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return Result.success("购物车已清空");
    }
}
