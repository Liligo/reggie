package com.liligo.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liligo.reggie.common.Result;
import com.liligo.reggie.entity.Category;
import com.liligo.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    /**
     * 新增分类接口
     *
     * @param category the category to be added
     * @return a Result object indicating success or failure
     */
    @PostMapping
    public Result<String> add(@RequestBody Category category) {
        log.info("Adding new category: {}", category);

        // Save the category using the service layer
        boolean isSaved = categoryService.save(category);

        if (!isSaved) {
            return Result.error("新增分类失败");
        }

        return Result.success("新增分类成功");
    }

    /**
     * 分页查询接口
     *
     * @param page     the page number
     * @param pageSize the number of items per page
     * @return a Result object containing the page of categories
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize) {
        log.info("Fetching categories for page: {}, pageSize: {}", page, pageSize);

        // 创建分页对象
        Page<Category> categoryPage = new Page<>(page, pageSize);

        // 创建条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 可以根据需要添加查询条件
        queryWrapper.orderByAsc(Category::getSort);

        // 进行分页查询
        categoryService.page(categoryPage);

        // 返回查询结果
        return Result.success(categoryPage);
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam("ids") Long id) {
        log.info("Deleting category with id: {}", id);
        // Delete the category using the service layer
        boolean isDeleted = categoryService.remove(id);
        if (!isDeleted) {
            return Result.error("删除分类失败");
        }
        return Result.success("删除分类成功");
    }

    /**
     * 根据id更新分类信息
     *
     * @param category the category to be updated
     * @return a Result object indicating success or failure
     */
    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        log.info("Updating category: {}", category);
        // Update the category using the service layer
        boolean isUpdated = categoryService.updateById(category);
        if (!isUpdated) {
            return Result.error("更新分类失败");
        }
        return Result.success("更新分类成功");
    }

    @GetMapping("/list")
    public Result<List<Category>> list(Category category) {
        // 方法参数是Category对象，没有使用任何注解如@RequestParam或@RequestBody。
        // 根据Spring的约定，这会触发基于参数名的绑定，通过反射机制将请求参数匹配到Category的属性上。

        log.info("Fetching category list with criteria: {}", category);

        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.eq(category.getType()!=null, Category::getType, category.getType());
        // 添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list =  categoryService.list(queryWrapper);

        return Result.success(list);
    }
}
