package com.taotao.cloud.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.taotao.cloud.auth.service.IUserDetailsService;
import com.taotao.cloud.auth.enums.LoginType;
import com.taotao.cloud.auth.model.SecurityUser;
import com.taotao.cloud.common.exception.BaseException;
import com.taotao.cloud.common.model.Result;
import com.taotao.cloud.social.entity.GiteeUserInfo;
import com.taotao.cloud.social.service.GiteeService;
import com.taotao.cloud.uc.api.entity.SysGiteeUser;
import com.taotao.cloud.uc.api.feign.RemoteSocialService;
import com.taotao.cloud.uc.api.feign.RemoteUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * UserServiceDetail
 *
 * @author dengtao
 * @date 2020/4/29 17:46
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements IUserDetailsService {

    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private RemoteSocialService remoteSocialService;

    @Autowired
    private GiteeService giteeService;

    /**
     * 通过用户名查询用户
     *
     * @param username 用户名
     * @return org.springframework.security.core.userdetails.UserDetails
     * @author dengtao
     * @date 2020/4/29 17:46
     */
    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        Result<SecurityUser> info = remoteUserService.getUserInfoByUsername(username);

        if (ObjectUtil.isNull(info) || info.getCode() == HttpStatus.NOT_FOUND.value()) {
            log.debug("登录用户：" + username + " 不存在.");
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }

        return info.getData();
    }

    /**
     * 手机验证码登录
     *
     * @param mobile 手机号码
     * @return org.springframework.security.core.userdetails.UserDetails
     * @author dengtao
     * @date 2020/4/29 18:02
     */
    @Override
    public SecurityUser loadUserByMobile(String mobile) throws UsernameNotFoundException {
        //  通过手机号mobile去数据库里查找用户以及用户权限
        Result<SecurityUser> info = remoteUserService.getUserInfoByMobile(mobile);
        if (ObjectUtil.isNull(info) || info.getCode() == HttpStatus.NOT_FOUND.value()) {
            log.info("登录手机号：" + mobile + " 不存在.");
            throw new UsernameNotFoundException("登录手机号：" + mobile + " 不存在");
        }
        return info.getData();
    }

    @Override
    public SecurityUser loadUserByOpenId(String openId) {
        // todo 这里要实现
        return null;
    }

    @Override
    public SecurityUser loadThirdUser(String code, LoginType loginType) {
        String type = loginType.getType();
        GiteeUserInfo userInfo = giteeService.getUserInfo(code);
        SysGiteeUser giteeUser = new SysGiteeUser();
        BeanUtil.copyProperties(userInfo, giteeUser);
        Result<Boolean> booleanResult = remoteSocialService.saveGiteeUser(giteeUser);
        Boolean data = booleanResult.getData();
        if (data) {
            Result<SecurityUser> securityUserResult = remoteUserService.getUserInfoBySocial(type, userInfo.getProviderUserId());
            return securityUserResult.getData();
        }
        return null;
    }

    @Override
    public SocialUserDetails loadUserByUserId(String s) throws UsernameNotFoundException {
        return null;
    }
}

