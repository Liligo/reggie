package com.liligo.reggie.common;

/**
 * 基于 ThreadLocal 的上下文类，用于存储和获取当前线程的用户ID。
 * 该类使用泛型来确保存储的是 Long 类型的用户ID。
 */
public class BaseContext{
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID。
     *
     * @param id 用户ID
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取当前线程的用户ID。
     *
     * @return 当前线程的用户ID
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
