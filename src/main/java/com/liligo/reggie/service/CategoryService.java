package com.liligo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liligo.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    boolean remove(Long id);
}
