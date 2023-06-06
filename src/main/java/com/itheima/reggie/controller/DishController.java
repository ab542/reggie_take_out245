package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 菜品管理
     */

    /**
     * 新增菜品,同时保存对应的口味数据
     * 需对dish表和dish_flavor两张表进行操作
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("dishdto:"+dishDto);
        //保存菜品数据
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Dish> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(name!=null,Dish::getName,name);
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,lambdaQueryWrapper);

        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);//拷贝属性
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> dishDtoList=records.stream().map(item->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);//拷贝属性

            //根据id查询分类name
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String name1 = category.getName();
                dishDto.setCategoryName(name1);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        log.info(id.toString());
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("dishdto:"+dishDto);
        //保存菜品数据
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }


    /**
     * 根据分类categoryId来查询对应的菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus,1);//状态为在售
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lambdaQueryWrapper);

        //返回DishDto
        List<DishDto> dishDtoList =null;
        //查询每一个菜品的口味 根据dishid查询
        dishDtoList=list.stream().map(item->{
            DishDto dishDto = new DishDto();
            Long dishId = item.getId();
            BeanUtils.copyProperties(item,dishDto);
            //根据dishId查询菜品口味规格
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId,dishId);
            lambdaQueryWrapper1.orderByAsc(DishFlavor::getUpdateTime);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper1);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }
}
