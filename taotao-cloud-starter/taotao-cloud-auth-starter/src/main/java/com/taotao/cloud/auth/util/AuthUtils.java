package com.taotao.cloud.auth.util;

import com.taotao.cloud.auth.model.SecurityUser;
import com.taotao.cloud.common.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;

/**
 * 认证授权相关工具类
 *
 * @author dengtao
 * @date 2020/4/29 16:44
 */
@Slf4j
public class AuthUtils {

    private AuthUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String BASIC_ = "Basic ";

    /**
     * 获取request(header/param)中的token
     *
     * @param request request
     * @return java.lang.String
     * @author dengtao
     * @date 2020/4/29 17:10
     */
    public static String extractToken(HttpServletRequest request) {
        String token = extractHeaderToken(request);
        if (token == null) {
            token = request.getParameter(OAuth2AccessToken.ACCESS_TOKEN);
            if (token == null) {
                log.debug("Token not found in request parameters.  Not an OAuth2 request.");
            }
        }
        return token;
    }

    /**
     * 功能描述
     *
     * @param newPass
     * @param passwordEncoderOldPass
     * @return boolean
     * @author dengtao
     * @date 2020/5/13 16:19
    */
    public static boolean validatePass(String newPass, String passwordEncoderOldPass) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(newPass, passwordEncoderOldPass);
    }

    /**
     * 解析head中的token
     *
     * @param request request
     * @return java.lang.String
     * @author dengtao
     * @date 2020/4/29 17:10
     */
    private static String extractHeaderToken(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(CommonConstant.TOKEN_HEADER);
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if ((value.startsWith(OAuth2AccessToken.BEARER_TYPE))) {
                String authHeaderValue = value.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
                int commaIndex = authHeaderValue.indexOf(',');
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex);
                }
                return authHeaderValue;
            }
        }
        return null;
    }

    /**
     * 从header 请求中的clientId:clientSecret
     *
     * @param request 请求数据
     * @return java.lang.String[]
     * @author dengtao
     * @date 2020/4/29 16:42
     */
    public static String[] extractClient(HttpServletRequest request) {
        String header = request.getHeader("BasicAuthorization");
        if (header == null || !header.startsWith(BASIC_)) {
            throw new UnapprovedClientAuthenticationException("请求头中client信息为空");
        }
        return extractHeaderClient(header);
    }


    /**
     * 从header 请求中的clientId:clientSecret
     *
     * @param header header中的参数
     * @return java.lang.String[]
     * @author dengtao
     * @date 2020/4/29 17:12
     */
    public static String[] extractHeaderClient(String header) {
        byte[] base64Client = header.substring(BASIC_.length()).getBytes(StandardCharsets.UTF_8);
        byte[] decoded = Base64.getDecoder().decode(base64Client);
        String clientStr = new String(decoded, StandardCharsets.UTF_8);
        String[] clientArr = clientStr.split(":");
        if (clientArr.length != 2) {
            throw new RuntimeException("Invalid basic authentication token");
        }
        return clientArr;
    }

    /**
     * 获取登陆的用户名
     *
     * @param authentication
     * @return java.lang.String
     * @author dengtao
     * @date 2020/4/29 17:13
     */
    public static String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String username = null;
        if (principal instanceof SecurityUser) {
            username = ((SecurityUser) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }
        return username;
    }
}
