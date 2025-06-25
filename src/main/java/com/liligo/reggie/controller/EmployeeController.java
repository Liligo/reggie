package com.liligo.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liligo.reggie.common.Result;
import com.liligo.reggie.entity.Employee;

import com.liligo.reggie.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * Handles employee login requests.
     *
     * @param request the HTTP request
     * @param employee the employee details from the request body
     * @return a Result object containing the logged-in employee information
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("Employee login endpoint hit");

        // 1. 对密码进行加密处理
        String password = employee.getPassword();
        // 使用Spring工具类进行MD5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2. 根据用户名查询数据库
        // 使用Mybatis-Plus的LambdaQueryWrapper来构建查询条件
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 使用lambda表达式指定查询条件：WHERE username = ?
        // Employee::getUsername 是方法引用，等价于表字段username
        employeeLambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        // 执行查询，获取满足条件的单个结果
        Employee emp = employeeService.getOne(employeeLambdaQueryWrapper);

        // 3. 如果没有查询到，返回登录失败结果
        if (emp == null) {
            log.info("Employee login failed: username not found");
            return Result.error("登录失败, 用户名不存在");
        }

        // 4. 密码比对，如果不一致，返回登录失败结果
        if(!emp.getPassword().equals(password)){
            log.info("Employee login failed: password mismatch");
            return Result.error("登录失败, 密码错误");
        }

        // 5. 查看员工状态，如果为已禁用状态，返回登录失败结果
        if(emp.getStatus() == 0){
            log.info("Employee login failed: account disabled");
            return Result.error("登录失败, 账号已禁用");
        }

        // 6. 登录成功，将员工ID存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return Result.success(emp);
    }

    /**
     * Handles employee logout requests.
     *
     * @param request the HTTP request
     * @return a Result object indicating successful logout
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return Result.success("退出成功");
    }

    /**
     * add employee.
     *
     * @param employee the updated employee details from the request body
     * @return a Result object indicating success or failure of the update operation
     */
    @PostMapping
    public Result<String> add(HttpServletRequest request, @RequestBody Employee employee){
        // 1. 设置初始密码为123456，并进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 2.调用Service方法保存员工信息
        employeeService.save(employee);
        log.info("新增员工, 员工信息:{}", employee.toString());
        return Result.success("新增员工成功");
    }

    /**
     * Update employee information.
     *
     * @return a Result object indicating success or failure of the update operation
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息, id: {}", id);
        Employee employee = employeeService.getById(id);
        if(employee == null){
            log.info("查询员工信息失败, 员工不存在, id: {}", id);
            return Result.error("查询员工信息失败, 员工不存在");
        }
        return Result.success(employee);
    }

    /**
     * 分页查询 employee information.
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        log.info("分页查询员工信息, page: {}, pageSize: {}, name: {}", page, pageSize, name);

        // 1. 创建分页构造器
        Page pageInfo = new Page(page, pageSize);
        // 2. 创建条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        // 3. 添加查询条件
        queryWrapper.like(name != null, Employee::getName, name);
        // 4. 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 5. 执行查询
        // 将查询结果 直接设置到入参 的pageInfo对象中（通过setRecords()和setTotal()）
        employeeService.page(pageInfo, queryWrapper);

        return Result.success(pageInfo);
    }



    /**
     * 更新员工信息.
     *
     * @param employee the updated employee details from the request body
     * @return a Result object indicating success or failure of the update operation
     */
    @PutMapping
    public Result<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("更新员工信息, 员工信息: {}", employee.toString());

        // 调用Service方法更新员工信息
        employeeService.updateById(employee);
        return Result.success("员工信息更新成功");
    }
}
