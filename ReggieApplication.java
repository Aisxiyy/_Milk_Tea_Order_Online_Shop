package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author li
 */
@Slf4j  //lombok里带的，用于输出日志
@SpringBootApplication
@ServletComponentScan//该注解用于扫描我们配置的拦截器的注解@WebFilter
@EnableTransactionManagement//该注解开启事务的支持
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);
        log.info("项目启动成功");
    }
}
