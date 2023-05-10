package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author li
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     *新增套餐同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional//加入事务注解，因为要操作两张表，要么全成功，要么全失败
    public void saveWithDish(SetmealDto setmealDto) {
        //1.保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //2.保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);//saveBatch保存多条(批量插入)
    }

    /**
     * 删除套餐同时删除和菜品的关联信息
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //判断该套餐能否删除的sql：select count（*） from setmeal where id in (1,2,3) and statue = 1;//若查询count不为零则不能删除
        //查询套餐的状态，确定是否可以删除
        //添加查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);//使用in关键字,
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);

        //如果不能删除，则抛出一个业务异常
        if (count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);
        //删除关系表中的数据
        //添加删除条件
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }

    @Override
    @Transactional//加入事务注解，因为要操作两张表，要么全成功，要么全失败
    public void updateWithDish(SetmealDto setmealDto) {
        //1.修改套餐的基本信息，操作setmeal，执行update操作
        this.updateById(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //2.保存套餐和菜品的关联信息，操作setmeal_dish，执行update操作
        setmealDishService.updateBatchById(setmealDishes);//saveBatch修改多条
    }

}
