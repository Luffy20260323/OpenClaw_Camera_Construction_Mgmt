package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.*;
import com.qidian.camera.module.auth.mapper.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限分组服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionGroupService extends ServiceImpl<PermissionGroupMapper, PermissionGroup> {
    
    /**
     * 获取所有权限分组
     */
    public List<PermissionGroup> getAllGroups() {
        return list();
    }
    
    /**
     * 根据分组代码获取分组
     */
    public PermissionGroup getByGroupCode(String groupCode) {
        return lambdaQuery()
            .eq(PermissionGroup::getGroupCode, groupCode)
            .one();
    }
}
