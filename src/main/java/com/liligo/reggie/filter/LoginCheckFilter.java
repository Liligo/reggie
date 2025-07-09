package com.liligo.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.liligo.reggie.common.BaseContext;
import com.liligo.reggie.common.Result;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*", asyncSupported = true)
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        log.info("拦截到请求：{}", request.getRequestURI());

        // 1. 获取本次请求的URI
        String requestURI = request.getRequestURI();

        // 2. 判断请求是否需要处理
        // 定义不需要处理的请求路径
        String [] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/error/",
                "/favicon.ico",
                "/user/sendMsg",
                "/user/login"
        };

        boolean check = check(requestURI, urls);

        // 3. 当请求在白名单时，则不需要处理，直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 判断登录状态，如果已登录，则直接放行
        // 4.1 后台员工登录
        Object employee = request.getSession().getAttribute("employee");
        if (employee != null) {
            log.info("用户已登录，用户id为：{}", employee);

            // 将登录用户的ID存入ThreadLocal中，供后续业务使用
            BaseContext.setCurrentId((Long) employee);

            filterChain.doFilter(request, response);
            return;
        }

        // 4.1 前台用户登录
        Object user = request.getSession().getAttribute("user");
        if (user != null) {
            log.info("用户已登录，用户id为：{}", user);

            // 将登录用户的ID存入ThreadLocal中，供后续业务使用
            BaseContext.setCurrentId((Long) user);

            filterChain.doFilter(request, response);
            return;
        }

        // 5. 如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
    }

    public boolean check(String requestURI, String[] urls) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match)
                return true;
        }
        return false;
    }
}
