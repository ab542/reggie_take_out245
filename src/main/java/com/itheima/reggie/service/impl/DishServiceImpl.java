package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //新增菜品(由于dishdto是继承于dish所以有dish的属性)
        this.save(dishDto);
        //新增口味(id利用雪花算法生成)
        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
//        for (DishFlavor flavor : flavors) {
//            flavor.setDishId(dishId);
//        }
        flavors=flavors.stream().map(item->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        System.out.println(flavors);
        //批量保存菜品口味数据
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);//将dish里的数据全部拷贝到dto
        //还需要口味表中的数据
        //查询当前菜品对应的口味信息，从dish_flavor表查询
        Long dishId = dish.getId();
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);
        //先将口味表数据删除
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //然后再添加
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors=flavors.stream().map(item->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        //批量保存菜品口味数据
        dishFlavorService.saveBatch(flavors);
       // dishDto.setFlavors(flavors);
    }
}
