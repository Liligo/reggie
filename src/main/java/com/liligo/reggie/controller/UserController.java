package com.liligo.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.liligo.reggie.common.Result;
import com.liligo.reggie.entity.User;
import com.liligo.reggie.service.UserService;
import com.liligo.reggie.utils.SMSUtils;
import com.liligo.reggie.utils.ValidateCodeUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/user")
@CacheConfig(cacheNames = "userCache")
public class UserController {
    @Autowired
    public UserService userService;

    /**
     * 发送短信验证码
     * @param user 用户信息，包含手机号
     * @return 返回操作结果
     */
    @PostMapping("/sendMsg")
    @CachePut(key = "#user.phone", unless = "#result.code != 1") // 缓存登录验证码
    public Result<String> sendMsg(@RequestBody User user) {
        String signName = "阿里云短信测试"; // 短信签名
        String templateCode = "SMS_154950909"; // 短信模板Code

        // 1. 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isEmpty(phone)) {
            return Result.error("手机号不能为空");
        }
        // 2. 生成验证码
        String validateCode = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("validateCode: {}", validateCode);
        // 3. 调用短信服务API发送验证码
        SMSUtils.sendMessage(signName, templateCode, phone, validateCode);

        return Result.success("短信发送成功");
    }

    /**
     * 用户登录
     * @param map 包含手机号(phone)和验证码(code)的键值对
     * @param session 用于存储用户登录状态的会话对象
     * @return 返回登录用户信息
     */
    @PostMapping("/login")
    @CacheEvict(key = "#map['phone']")  // 登录成功后清除验证码缓存
    public Result<User> login(@RequestBody Map<String, String> map, HttpSession session) { // 添加泛型参数
        log.info("login: {}", map);
        // 获取手机号和验证码
        String phone = map.get("phone");
        String code = map.get("code");

        // 校验手机号是否注册
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = userService.getOne(queryWrapper);
        if (user == null) {
            // 5. 如果手机号未注册，则自动注册
            user = new User();
            user.setPhone(phone);
            user.setStatus(1); // 设置用户状态为启用
            userService.save(user);
        }

        // 登录成功，将用户id存入session
        session.setAttribute("user", user.getId());

        return Result.success(user);
    }

    /**
     * 用户登出
     * @param session 当前用户会话
     * @return 返回退出结果
     */
    @PostMapping("/loginout")
    public Result<String> loginout(HttpSession session){
        // 清理session
        session.removeAttribute("user");
        return Result.success("退出成功");
    }
}
