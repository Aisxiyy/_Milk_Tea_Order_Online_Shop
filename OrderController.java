package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrderStatus;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    @PostMapping("/submit2")
    public R<String> submit2(@RequestBody Orders orders) {
        ordersService.submit2(orders);
        return R.success("下单成功");
    }


    /**
     * 后台管理订单信息分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){
        log.info("page = {},pageSize = {}",page,pageSize);

        //构造分页构造器
        Page<Orders> pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(number != null,Orders::getId, number);
        queryWrapper.ge(beginTime != null,Orders::getOrderTime,beginTime);
        queryWrapper.le(endTime != null,Orders::getOrderTime,endTime);
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //执行查询
        orderService.page(pageInfo,queryWrapper);

        List<Orders> records = pageInfo.getRecords();
        records = records.stream().map((item) -> {

            item.setUserName(item.getConsignee());
            if (item.getAddress()==null){
                item.setUserName("用户"+item.getUserId());
                item.setAddress("自取");
            }

            return item;
        }).collect(Collectors.toList());

        return R.success(pageInfo);
    }



//    @GetMapping("/page")
//    public R<Page<OrdersDto>> page(int page, int pageSize, String number,Date beginTime,Date endTime) {
//        LocalDateTime localDateTimeBegin = null;
//        LocalDateTime localDateTimeEnd = null;
//        // 对其时间参数进行处理
//        if (beginTime != null && endTime != null) {
//            // beginTime处理
//            Instant instant = beginTime.toInstant();
//            ZoneId zoneId = ZoneId.systemDefault();
//            localDateTimeBegin = instant.atZone(zoneId).toLocalDateTime();
//            //formatBeginTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//
//            // endTime 进行处理
//            Instant instant1 = endTime.toInstant();
//            ZoneId zoneId1 = ZoneId.systemDefault();
//            localDateTimeEnd = instant1.atZone(zoneId1).toLocalDateTime();
//            //formatEndTime = localDateTime1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        }
//
//        Page<Orders> pageInfo = new Page<>(page, pageSize);
//        Page<OrdersDto> pageDto = new Page<>();
//
//        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
//        if (!StringUtils.isEmpty(number)) {
//            wrapper.eq("number", number);
//        }
//        if (!StringUtils.isEmpty(localDateTimeBegin)) {
//            wrapper.ge("order_time", localDateTimeBegin);
//        }
//        if (!StringUtils.isEmpty(localDateTimeEnd)) {
//            wrapper.le("order_time", localDateTimeEnd);
//        }
//        wrapper.orderByDesc("order_time");
//        ordersService.page(pageInfo, wrapper);
//        // 将其除了records中的内存复制到pageDto中
//        BeanUtils.copyProperties(pageInfo, pageDto, "records");
//
//        List<Orders> records = pageInfo.getRecords();
//
//        List<OrdersDto> collect = records.stream().map((order) -> {
//            OrdersDto ordersDto = new OrdersDto();
//
//            BeanUtils.copyProperties(order, ordersDto);
//            // 根据订单id查询订单详细信息
//
//            QueryWrapper<OrderDetail> wrapperDetail = new QueryWrapper<>();
//            wrapperDetail.eq("order_id", order.getId());
//
//            List<OrderDetail> orderDetails = orderDetailService.list(wrapperDetail);
//            ordersDto.setOrderDetails(orderDetails);
//
//            if(order.getUserId()!= null)
//            {
//                Long userId = order.getUserId();
//                User user = userService.getById(userId);
//                if (user!=null){
////                    ordersDto.setUserName(user.getName());
//                    ordersDto.setPhone(user.getPhone());
//                }
//
//            }
//            // 根据userId 查询用户姓名
////            Long userId = order.getUserId();
////            User user = userService.getById(userId);
////            ordersDto.setUserName(user.getName());
////            ordersDto.setPhone(user.getPhone());
//
//
//            if(order.getAddressBookId()!=null){
//                Long addressBookId = order.getAddressBookId();
//                AddressBook addressBook = addressBookService.getById(addressBookId);
//                if (addressBook!=null){
//                    ordersDto.setAddress(addressBook.getDetail());
////                    ordersDto.setConsignee(addressBook.getConsignee());
//                    if (addressBook.getConsignee()!=null){
//                        ordersDto.setConsignee(addressBook.getConsignee());
//                    }
//                    Long userId = order.getUserId();
//                    User user = userService.getById(userId);
//                    if (user!=null){
//                        ordersDto.setUserName(addressBook.getConsignee());
//                    }
//                }
//            }
//            // 获取地址信息
////            Long addressBookId = order.getAddressBookId();
////            AddressBook addressBook = addressBookService.getById(addressBookId);
////            ordersDto.setAddress(addressBook.getDetail());
////            ordersDto.setConsignee(addressBook.getConsignee());
//
//            return ordersDto;
//        }).collect(Collectors.toList());
//
//        pageDto.setRecords(collect);
//
//        return R.success(pageDto);
//    }

    @PutMapping
    public R<String> statusOrder(@RequestBody OrderStatus orderStatus) {
        Orders orders = ordersService.getById(orderStatus.getId());
        orders.setStatus(orderStatus.getStatus());
        ordersService.updateById(orders);
        return R.success("派送完成");
    }

    //抽离的一个方法，通过订单id查询订单明细，得到一个订单明细的集合
    //这里抽离出来是为了避免在stream中遍历的时候直接使用构造条件来查询导致eq叠加，从而导致后面查询的数据都是null
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId){
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        return orderDetailList;
    }

    /**
     * 用户端展示自己的订单分页查询
     * @param page
     * @param pageSize
     * @return
     * 直接从分页对象中获取订单id
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> pageDto = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId());
        //这里是直接把当前用户分页的全部结果查询出来，要添加用户id作为查询条件，否则会出现用户可以查询到其他用户的订单情况
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);

        //通过OrderId查询对应的OrderDetail
        LambdaQueryWrapper<OrderDetail> queryWrapper2 = new LambdaQueryWrapper<>();

        //对OrderDto进行需要的属性赋值
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> orderDtoList = records.stream().map((item) ->{
            OrdersDto orderDto = new OrdersDto();
            //此时的orderDto对象里面orderDetails属性还是空 下面准备为它赋值
            Long orderId = item.getId();//获取订单id
            List<OrderDetail> orderDetailList = this.getOrderDetailListByOrderId(orderId);
            BeanUtils.copyProperties(item,orderDto);
            //对orderDto进行OrderDetails属性的赋值
            orderDto.setOrderDetails(orderDetailList);
            return orderDto;
        }).collect(Collectors.toList());

        //使用dto的分页有点难度.....需要重点掌握
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        pageDto.setRecords(orderDtoList);
        return R.success(pageDto);
    }


    //客户端点击再来一单
    /**
     * 前端点击再来一单是直接跳转到购物车的，所以为了避免数据有问题，再跳转之前我们需要把购物车的数据给清除
     * ①通过orderId获取订单明细
     * ②把订单明细的数据的数据塞到购物车表中，不过在此之前要先把购物车表中的数据给清除(清除的是当前登录用户的购物车表中的数据)，
     * 不然就会导致再来一单的数据有问题；
     */
    @PostMapping("/again")
    public R<String> againSubmit(@RequestBody Map<String,String> map){
        String ids = map.get("id");

        long id = Long.parseLong(ids);

        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        //获取该订单对应的所有的订单明细表
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空，这里的clean方法是视频中讲过的,建议抽取到service中,那么这里就可以直接调用了
        //shoppingCartService.clean();
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ShoppingCart::getUserId,currentId);
        shoppingCartService.remove(queryWrapper1);

        //获取用户id
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();
            if (dishId != null) {
                //如果是菜品那就添加菜品的查询条件
                shoppingCart.setDishId(dishId);
            } else {
                //添加到购物车的是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //把携带数据的购物车批量插入购物车表  这个批量保存的方法要使用熟练！！！
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");
    }
}