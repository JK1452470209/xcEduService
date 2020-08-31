package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Mr.JK
 * @create 2020-08-24  17:24
 */
@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi {

    @Autowired
    AuthService authService;

    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;

    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }

        //账号和密码
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        //申请令牌
        AuthToken authToken = authService.login(username,password,clientId,clientSecret);
        //用户身份令牌
        String jwt_token = authToken.getJwt_token();
        //将令牌存储到cookie
        this.savecookie(jwt_token);

        return new LoginResult(CommonCode.SUCCESS,jwt_token);
    }


    @Override
    @PostMapping("userlogout")
    public ResponseResult logout() {
        //取出cookie中的用户身份令牌
        String uid = getTokenFormCookie();
        //删除redis中的token
        boolean result = authService.delToken(uid);
        //清除cookie
        this.clearCookie(uid);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {
        //取出cookie中的用户身份令牌
        String uid = getTokenFormCookie();
        if (uid == null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        //拿身份令牌从redis中查询jwt令牌
        AuthToken userToken = authService.getUserToken(uid);
        if (userToken != null){
            //将jwt令牌返回给用户
            String access_token = userToken.getAccess_token();//返回jwt
            return new JwtResult(CommonCode.SUCCESS,access_token);
        }

        return null;
    }

    /**
     * 取出cookie中的身份令牌
     * @return
     */
    private String getTokenFormCookie(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if (map != null && map.get("uid") != null){
            String uid = map.get("uid");
            return uid;
        }
        return null;
    }

    /**
     * 将令牌存储到cookie
     * @param jwt_token
     */
    private void savecookie(String jwt_token){

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","uid",jwt_token,cookieMaxAge,false);

    }

    /**
     * 清除cookie
     * @param jwt_token
     */
    private void clearCookie(String jwt_token){
        HttpServletResponse response = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getResponse();
        // 设置maxAge为实现删除cookie
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", jwt_token, 0, false);
    }
}
