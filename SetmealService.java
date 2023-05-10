package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @author li
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     *新增套餐同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐同时删除和菜品的关联信息
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 修改套餐信息
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);
}
