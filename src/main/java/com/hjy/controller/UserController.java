package com.hjy.controller;


import cn.hutool.core.lang.Assert;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hjy.common.lang.Result;
import com.hjy.entity.User;
import com.hjy.service.UserService;
import com.hjy.utils.JwtUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hjy
 * @since 2021-01-04
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;

    @RequiresAuthentication //必须登录
    @GetMapping("/index")
    public Result index(){
        User user = userService.getById(1L);
        return Result.success(user);
    }

    @PostMapping("/save")
    public Result save(@Validated @RequestBody User user){
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result login(@Validated @RequestBody User user, HttpServletResponse response){
        //getOne()，这个是方法返回结果不止一条则会抛出异常，如果想默认取第一条结果，可以给这方法传第二个参数为false。
        //判断用户是否存在
        User user2 = userService.getOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        Assert.notNull(user2, "账号不存在");
        //判断密码是否正确
        if (user == null || !user.getPassword().equals(SecureUtil.md5(user.getPassword()))) {
            return Result.fail("密码错误");
        }

        return null;
    }

}
