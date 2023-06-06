package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.ws.Action;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;
    /**
     * 新增操作
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //新增套餐
        this.save(setmealDto);
        //新增套餐菜品关系表中的数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes=setmealDishes.stream().map(item->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态是否可删除
        //select count(*) from setmeal where id in(id1,id2) and status=1
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId,ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);//状态为1 代表出售中
        int count = this.count(lambdaQueryWrapper);
        if(count>0){//说明有出售的 不能删除 抛出业务异常
            throw  new CustomException("存在有商品出售 不可删除！！");
        }
        //可以删除
        //delete from setmealdish where setmeal_id in(id1,id2);
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据
        setmealDishService.remove(lambdaQueryWrapper1);
        //删除套餐表中的数据
        this.removeByIds(ids);

    }

    @Override
    public List<Dish> getDishBySetmealId(Long id) {
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        lambdaQueryWrapper.orderByAsc(SetmealDish::getSort);
        List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);
        List<Dish> dishes= list.stream().map(item->{
            //根据item里面的dishid获取菜品dish类
            Dish dish = new Dish();
            Long dishId = item.getDishId();
            dish = dishService.getById(dishId);
            return dish;
        }).collect(Collectors.toList());
        return dishes;
    }
}
