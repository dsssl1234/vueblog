package com.hjy.controller;


import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hjy.common.lang.Result;
import com.hjy.entity.Blog;
import com.hjy.service.BlogService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hjy
 * @since 2021-01-04
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    BlogService blogService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1")Integer currentPage){

        Page page =new Page(currentPage,5);
        IPage pageData = blogService.page(page, new QueryWrapper<Blog>().orderByDesc("create_time"));
        return Result.success(pageData);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable(name = "id")Long id){
        Blog blog = blogService.getById(id);
        return Result.success(blog);
    }

//    @RequiresAuthentication
    @PostMapping("/edit")
    public Result edit(@Validated @RequestBody Blog blog){
        Blog temp = null;
        Blog principal = (Blog) SecurityUtils.getSubject().getPrincipal();
        if(null!=blog.getId()){
            temp = blogService.getById(blog.getId());
            // 只能编辑自己的文章
            Assert.isTrue(temp.getUserId().longValue()==principal.getId().longValue(),"无编辑权限");
        }else {
            blog.setUserId(1L);
            blog.setStatus(0);
        }
        blogService.save(blog);
        return Result.success("成功");
    }
}
