package com.hjy.shiro;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hjy.common.lang.Result;
import com.hjy.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT过滤器
 * AuthenticatingFilter继承了shiro内置的过滤器，内置了可以自动登录方法的过滤除器
 * 主要是调用executeLogin来验证token和登录
 */
@Component
public class JwtFilter extends AuthenticatingFilter {

    @Autowired
    JwtUtils jwtUtils;


    /**
     * 实现登录，我们需要生成我们自定义支持的JwtToken
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        //用户登录的时候，从请求头拿到用户信息来生成对应的token
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = httpServletRequest.getHeader("Authorization");
        //在这边登录后，会到AccountRealm中的doGetAuthenticationInfo进行认证处理
        return jwt.isEmpty()?null:new JwtToken(jwt);
    }

    /**在上面进行登录后，到AccountRealm中的doGetAuthenticationInfo进行认证处理，在到该方法进行校验
     * 拦截校验，当头部没有Authorization时候，我们直接通过，不需要自动登录；
     * 当带有的时候，首先我们校验jwt的有效性，没问题我们就直接执行executeLogin方法实现自动登录
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = httpServletRequest.getHeader("Authorization");
        if(StringUtils.isBlank(jwt)){
            //如果没有jwt，就不需要交给shiro进行登陆的处理，直接让其调用接口，交给注解进行拦截
            //对未登录用户的访问内容进行放行，未登录的用户也是可以看得到别人的留言的
            return true;
        }
        //校验jwt，此时就用到jwt的工具类
        Claims claimByToken = jwtUtils.getClaimByToken(jwt);
        if(null==claimByToken||jwtUtils.isTokenExpired(claimByToken.getExpiration())){
            throw new ExpiredCredentialsException("token失效,请重新登录");
        }
        //执行登录 executeLogin这个方法在父类里面
        return executeLogin(servletRequest,servletResponse);
    }

    /**
     * 重写报错的方法，为了返回统一的格式给前端
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {

        Throwable throwable = e.getCause() == null ? e : e.getCause();
        Result fail = Result.fail(throwable.getMessage());//返回异常信息
        String Json = JSONUtil.toJsonStr(fail);//JSONUtil是hutool依赖中的方法
        try {
            response.getWriter().print(Json);//以JSON格式返回给前端
        } catch (IOException ex) {
//            ex.printStackTrace();
            System.out.println("登录异常返回给前端JSON格式信息错误");
        }

        return super.onLoginFailure(token, e, request, response);
    }


    /**
     * 在访问的时候也是要经过filter，所有这边也需要进行跨域的处理
     * 对跨域提供支持
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求,这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}
