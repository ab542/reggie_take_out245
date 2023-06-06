package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;

public interface DishService extends IService<Dish> {
    //保存菜品同时保存口味
    public void saveWithFlavor(DishDto dishDto);
    //根据id查询菜品并口味
    public DishDto getByIdWithFlavor(Long id);

    //根据id更新dish和dish口味表
    public void updateWithFlavor(DishDto dishDto);
}
