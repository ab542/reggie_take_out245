package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    //新增套餐连同菜品套餐关系表
   public void saveWithDish(SetmealDto setmealDto);
   //删除连同菜品与套餐关联表数据
   public void removeWithDish(List<Long> ids);
   //查看套餐中的菜品
   public List<Dish> getDishBySetmealId(Long id);
}
