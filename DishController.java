package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理和菜品口味管理两张表的内容都用同一个DishController
 * @author li
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增菜品
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        //需要在DishService里面拓展新的方法
        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        log.info("page = {},pageSize = {}",page,pageSize);//{}是占位符，{}里的和后面的page，pageSize一一对应

        //1.构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //2.构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //3.添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //4.添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //5.执行查询
        dishService.page(pageInfo,queryWrapper);

        //对象属性拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        //需要在DishService里面拓展新的方法
        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品信息成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){//传dish比传单个的id参数更好
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        //查询条件1：根据种类id来查
//        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//        //查询条件2：查询状态为启售的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }
    //添加上口味信息的菜品数据展示，前端会显示出口味数据，而后台不会被影响
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){//传dish比传单个的id参数更好
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件1：根据种类id来查
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //查询条件2：查询状态为启售的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();

            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?;
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }



    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") int status,@RequestParam("ids") List<Long> ids) {
        int count = getCount(ids);
        if (count > 0) {
            // 表示该菜品在其套餐中不能进行删除
            throw new CustomException("菜品在其套餐中使用无法进行停售");
        }
        ids.stream().forEach((item) -> {
            Dish dish = dishService.getById(item);
            dish.setStatus(status);
            dishService.updateById(dish);
        });

        return R.success("修改成功");
    }

    /**
     * 查看菜品是否与其套餐关联
     * @param ids
     * @return
     */
    private int getCount(List<Long> ids) {
        // 在停售前先查看是否在其套餐中使用
        QueryWrapper<SetmealDish> wrapper = new QueryWrapper<>();
        wrapper.in("dish_id", ids);

        int count = setmealDishService.count(wrapper);
        return count;
    }


    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        // 查看有没有被套餐使用
        int count = this.getCount(ids);
        if (count > 0) {
            throw new CustomException("该菜品正在套餐中售卖 无法进行删除");
        }
        // 需要将其口味信息一并进行删除

        ids.forEach((id) -> {
            Dish dish = dishService.getById(id);
            if (dish.getStatus() == 1) {
                throw new CustomException("正在售卖中无法删除");
            }
        });

        ids.stream().forEach((item) -> {
            // 删除菜品
            dishService.removeById(item);
            // 删除菜品关联的口味信息
            QueryWrapper<DishFlavor> wrapper = new QueryWrapper<>();
            wrapper.eq("dish_id", item);
            dishFlavorService.remove(wrapper);
        });

        return R.success("删除成功！！");
    }

}
