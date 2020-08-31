package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Mr.JK
 * @create 2020-08-24  17:26
 */
@Service
public class AuthService {

    //Eureka负载均衡客户端
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;

    /**
     * 用户认证申请令牌,将令牌存储到redis
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //请求spring security申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if (authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FALT);
        }
        //用户身份令牌
        String jwt_token = authToken.getJwt_token();
        String jsonString = JSON.toJSONString(authToken);
        //将令牌存储到redis
        boolean result = this.saveToken(jwt_token, jsonString, tokenValiditySeconds);
        if (!result){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFALT);
        }

        return authToken;
    }

    /**
     * 从redis从查询jwt令牌 根据短令牌
     * @param token
     * @return
     */
    public AuthToken getUserToken(String token){
        String key = "user_token:" + token;
        //从redis中取到的令牌信息
        String value = stringRedisTemplate.opsForValue().get(key);
        //转成对象
        try {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除指定usertoken在redis中的用户令牌信息
     * @param jwt_token usertoken
     * @return
     */
    public boolean delToken(String jwt_token) {
        String key = "user_token:" + jwt_token;
        stringRedisTemplate.delete(key);
        return true;
    }


    /**
     * 将token保存到redis
     * @param jwt_token 用户身份令牌
     * @param content   内容就是authtoken对象的内容
     * @param ttl   过期时间
     * @return
     */
    private boolean saveToken(String jwt_token,String content,long ttl){
        String key = "user_token:" + jwt_token;
        stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire > 0;
    }

    /**
     * 申请令牌
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    private AuthToken applyToken(String username,String password,String clientId,String clientSecret){
        //采用客户端负载均衡的方式从eureka获取认证服务的ip和端口
        ServiceInstance serviceInstance = loadBalancerClient.choose("XC-SERVICE-UCENTER-AUTH");
        URI uri = serviceInstance.getUri();
        String authUrl = uri + "/auth/oauth/token";

        //使用LinkedMultiValueMap储存多个header信息
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        //设置basic认证信息
        String basicAuth = this.getHttpBasic(clientId, clientSecret);
        headers.add("Authorization",basicAuth);

        //设置请求中的body信息
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        //凭证信息错误时候, 指定restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或者401时也要正常响应,不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        //远程调用令牌
        ResponseEntity<Map> responseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        Map responseBody = responseEntity.getBody();
        System.out.println(responseBody);
        if (responseBody == null || responseBody.get("access_token") == null
                || responseBody.get("refresh_token") == null || responseBody.get("jti") == null){

            //解析spring security返回的错误信息
            String error_description = (String) responseBody.get("error_description");
            if(StringUtils.isNotEmpty(error_description)){
                if(error_description.equals("坏的凭证")){
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }else if(error_description.indexOf("UserDetailsService returned null")>=0){
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
        }
        AuthToken authToken = new AuthToken();
        //
        authToken.setAccess_token((String) responseBody.get("access_token"));//用户jwt令牌
        authToken.setRefresh_token((String) responseBody.get("refresh_token"));//刷新令牌
        authToken.setJwt_token((String) responseBody.get("jti"));//用户身份令牌
        return authToken;
    }

    /**
     * 获取httpbasic串
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String getHttpBasic(String clientId,String clientSecret){
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId+":"+clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic "+new String(encode);
    }
}
