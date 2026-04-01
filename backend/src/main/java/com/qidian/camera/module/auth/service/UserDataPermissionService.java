package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.UserDataPermission;
import com.qidian.camera.module.auth.mapper.UserDataPermissionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户数据权限服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDataPermissionService extends ServiceImpl<UserDataPermissionMapper, UserDataPermission> {
    
    /**
     * 获取用户数据权限
     */
    public UserDataPermission getByUserId(Long userId) {
        return lambdaQuery()
            .eq(UserDataPermission::getUserId, userId)
            .one();
    }
}
