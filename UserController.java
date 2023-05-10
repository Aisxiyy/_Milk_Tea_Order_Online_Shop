package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.entity.UserInfo;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author li
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){//判断手机号不为空
            //生成随机的四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();//生成四位数的验证码(是Integer类型)，把它转成String类型方便比对
            log.info("生成的验证码为code={}",code);//后台输出一下该验证码用于测试

            //调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage("瑞吉外卖","18870039666",phone,code);

            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);
            return R.success("验证码发送成功");
        }

        return R.error("短信发送失败");
    }

    /**
     * 用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        //为什么用Map：因为User里没有验证码code这个属性，而要传入code（前端传来的）这个值来比对
        //      所以使用Map键值对的方式来传参
       log.info(map.toString());//看能否传输数据过来

        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeInSession != null && codeInSession.equals(code)){
            //如果比对成功，说明登陆成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if (user == null){
                //判断当前手机号对应的用户是否为新用户，如果是就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //把user的id存到session中给过滤器判断为已登录状态（这样就能登录成功，不会被拦截了）
            session.setAttribute("user",user.getId());
            return R.success(user);

        }


        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出登陆成功");
    }

    @GetMapping("/get")
    public R<UserInfo> getUser(HttpSession session) {
        UserInfo userInfo = new UserInfo();
        Long userId = (Long) session.getAttribute("user");
        User user = userService.getById(userId);
        if (user.getName() == null) {
            userInfo.setUsername(user.getPhone());
        } else {
            userInfo.setUsername(user.getName());
        }
        if (user.getSex() == null) {
            userInfo.setSex("女");
        } else {
            userInfo.setSex("男");
        }
        return R.success(userInfo);
    }

}
