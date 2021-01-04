package com.hjy.shiro;


import com.hjy.entity.User;
import com.hjy.service.UserService;
import com.hjy.utils.JwtUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * AccountRealm是shiro进行登录或者权限校验的逻辑所在
 */
@Component
public class AccountRealm  extends AuthorizingRealm {

    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    UserService userService;

    /**
     * supports：为了让realm支持jwt的凭证校验
     * @param token
     * @return
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        //获取到的token应该进行判断，必须是属于JwtToken的token
        return token instanceof JwtToken;
    }

    /**授权
     * doGetAuthorizationInfo：权限校验
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }


    /**认证
     * doGetAuthenticationInfo：登录认证校验
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

       JwtToken jwtToken = (JwtToken) authenticationToken;
        String userId = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal()).getSubject();
        User user = userService.getById(Long.valueOf(userId));
        if(null==user){
            throw new UnknownAccountException("账户不存在");
        }
        if(-1==user.getStatus()){
            throw new LockedAccountException("账户被锁定");
        }
        //密码认证，shiro帮我们验证  第一个参数是：获取当前用户的认证 第二个是：密码 第三个是：认证名
        return new SimpleAuthenticationInfo(user,jwtToken.getPrincipal(),getName());
    }
}
