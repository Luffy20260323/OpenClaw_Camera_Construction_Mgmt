package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.DataPermissionTemplate;
import com.qidian.camera.module.auth.mapper.DataPermissionTemplateMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限模板服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataPermissionTemplateService extends ServiceImpl<DataPermissionTemplateMapper, DataPermissionTemplate> {
    
    /**
     * 获取所有模板
     */
    public List<DataPermissionTemplate> getAllTemplates() {
        return list();
    }
    
    /**
     * 根据模板代码获取模板
     */
    public DataPermissionTemplate getByTemplateCode(String templateCode) {
        return lambdaQuery()
            .eq(DataPermissionTemplate::getTemplateCode, templateCode)
            .one();
    }
    
    /**
     * 获取系统内置模板
     */
    public List<DataPermissionTemplate> getSystemTemplates() {
        return lambdaQuery()
            .eq(DataPermissionTemplate::getIsSystem, true)
            .list();
    }
}
