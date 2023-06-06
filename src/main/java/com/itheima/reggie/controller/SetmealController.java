package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {


    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐连同套餐菜品关联表也要新增
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("setmealdto:{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 对套餐进行分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页查询
        Page<Setmeal>  setmealPage = new Page<>(page,pageSize);
        //查询条件
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(name!=null,Setmeal::getName,name);
        setmealService.page(setmealPage, lambdaQueryWrapper);

        //对应套餐分类的名字 需要查出
        Page<SetmealDto> setmealDtoPage = new Page<>(page,pageSize);
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");
        //查询分类名称
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> setmealDtos=records.stream().map(item->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //查询分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtos);

        return R.success(setmealPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> remove(@RequestParam List<Long> ids){
        log.info("ids：{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据分类categoryId来查询对应的套餐
     * @param list
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);//状态为在售
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);
        return R.success(list);
    }

    /**
     * 根据套餐id获取套餐中的菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<Dish>> getDishBySetmealId(@PathVariable Long id){
        //在菜品套餐关系表中根据套餐id获取所有的菜品
        log.info(id.toString());
        List<Dish> dishes = setmealService.getDishBySetmealId(id);
        log.info("jj:"+dishes);
        return R.success(dishes);
    }


}
