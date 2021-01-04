package com.hjy.service.impl;

import com.hjy.entity.Blog;
import com.hjy.mapper.BlogMapper;
import com.hjy.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hjy
 * @since 2021-01-04
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

}
