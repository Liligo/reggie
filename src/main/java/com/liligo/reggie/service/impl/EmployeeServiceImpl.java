package com.liligo.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // 新增导入
import com.liligo.reggie.entity.Employee;
import com.liligo.reggie.mapper.EmployeeMapper;
import com.liligo.reggie.service.EmployeeService;
import org.springframework.stereotype.Service; // 新增注解

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}
