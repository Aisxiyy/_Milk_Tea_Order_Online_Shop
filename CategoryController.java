package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author li
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     *新增分类
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 员工信息分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        log.info("page = {},pageSize = {}",page,pageSize);//{}是占位符，{}里的和后面的page，pageSize一一对应

        //1.构造分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //2.构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //3.添加排序条件,以更新时间为排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //4.执行查询
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }
    /**
     * 根据id删除分类
     */
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类，id为：{}",id);

//        categoryService.removeById(id);
        categoryService.remove(id);

        return R.success("分类信息删除成功");
    }

    /**
     * 修改菜品分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类，id为：{}",category.getId());

        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 添加菜品时根据条件查询出分类数据以供选择
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){//传参使用实体类的方式（也可以只传一个type属性），因为实体类的通用性更强（方便之后修改）
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件:先根据sort属性排，若sort一样则根据UpdateTime排
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
