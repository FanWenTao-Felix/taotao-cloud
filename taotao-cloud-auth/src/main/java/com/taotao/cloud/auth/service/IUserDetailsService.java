package com.taotao.cloud.auth.service;

import com.taotao.cloud.auth.enums.LoginType;
import com.taotao.cloud.auth.model.SecurityUser;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.social.security.SocialUserDetailsService;

/**
 * IUserDetailsService
 *
 * @author dengtao
 * @date 2020/4/29 17:33
 */
public interface IUserDetailsService extends UserDetailsService, SocialUserDetailsService {

    /**
     * 根据电话号码查询用户
     *
     * @param mobile 手机号码
     * @return org.springframework.security.core.userdetails.UserDetails
     * @author dengtao
     * @date 2020/4/29 22:02
     */
    SecurityUser loadUserByMobile(String mobile);

    /**
     * 根据openId查询用户
     *
     * @param openId openId
     * @return org.springframework.security.core.userdetails.UserDetails
     * @author dengtao
     * @date 2020/4/29 22:02
     */
    SecurityUser loadUserByOpenId(String openId);

    /**
     * 获取用户
     *
     * @param para      para
     * @param loginType loginType
     * @return com.taotao.cloud.auth.model.SecurityUser
     * @author dengtao
     * @date 2020/4/29 21:25
     */
    SecurityUser loadThirdUser(String para, LoginType loginType);
}
