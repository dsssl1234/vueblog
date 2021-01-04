package com.hjy.mapper;

import com.hjy.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hjy
 * @since 2021-01-04
 */
@Repository
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

}
