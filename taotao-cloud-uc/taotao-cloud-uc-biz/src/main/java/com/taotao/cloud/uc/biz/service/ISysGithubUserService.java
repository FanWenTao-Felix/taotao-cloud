package com.taotao.cloud.uc.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.cloud.common.model.PageResult;
import com.taotao.cloud.uc.api.entity.SysGithubUser;

import java.util.Map;

/**
 * github用户表
 *
 * @author taotao
 * @date 2020-05-14 14:36:39
 */
public interface ISysGithubUserService extends IService<SysGithubUser> {
    /**
     * 列表
     * @param params
     * @return
     */
    PageResult<SysGithubUser> findList(Map<String, Object> params);
}

