package com.hjy.config;

import com.hjy.shiro.AccountRealm;

import com.hjy.shiro.JwtFilter;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.mgt.SecurityManager;//SecurityManager
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;//注意不要导错这个包
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import javax.servlet.Filter;//注意不要导错这个包,servlet
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * shiro启用注解拦截控制器
 *
 */
@Configuration
public class ShiroConfig  {
    @Autowired
    JwtFilter jwtFilter;


    /**
     * SessionManager主要负责创建Session和获取Session；
     * 引入RedisSessionDAO和RedisCacheManager，为了解决shiro的权限数据和会话信息能保存到redis中，实现会话共享。
     * @param redisSessionDAO
     * @return
     */
    @Bean
    public SessionManager sessionManager(RedisSessionDAO redisSessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(redisSessionDAO);
        return sessionManager;
    }

    /**
     * DefaultWebSecurityManager中为了关闭shiro自带的session方式，我们需要设置为false，
     * 这样用户就不再能通过session方式登录shiro。后面将采用jwt凭证登录。
     * @param accountRealm
     * @param sessionManager
     * @param redisCacheManager
     * @return
     */
    @Bean
    public DefaultWebSecurityManager securityManager(AccountRealm accountRealm,
                                                     SessionManager sessionManager,
                                                     RedisCacheManager redisCacheManager) {

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(accountRealm);

        //inject sessionManager
        securityManager.setSessionManager(sessionManager);

        // inject redisCacheManager
        securityManager.setCacheManager(redisCacheManager);
        return securityManager;
    }

    /**
     * Shiro过滤器链
     * 那些链接需要进行过滤
     * @return
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        Map<String, String> filterMap = new LinkedHashMap<>();
        // JWT作为前后端分离的用户凭证
        filterMap.put("/**", "jwt"); // 主要通过注解方式校验权限, authc:shiro内置过滤器,即需用户登录
        chainDefinition.addPathDefinitions(filterMap);
        return chainDefinition;
    }

    /**
     *
     * 在ShiroFilterChainDefinition中，我们不再通过编码形式拦截Controller访问路径，
     * 而是所有的路由都需要经过JwtFilter这个过滤器，然后判断请求头中是否含有jwt的信息，有就登录，没有就跳过。跳过之后，
     * 有Controller中的shiro注解进行再次拦截，比如@RequiresAuthentication，这样控制权限访问。
     * @param securityManager
     * @param shiroFilterChainDefinition
     * @return
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,
                                                         ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", jwtFilter);
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = shiroFilterChainDefinition.getFilterChainMap();

        shiroFilter.setFilterChainDefinitionMap(filterMap);
        return shiroFilter;
    }
}

