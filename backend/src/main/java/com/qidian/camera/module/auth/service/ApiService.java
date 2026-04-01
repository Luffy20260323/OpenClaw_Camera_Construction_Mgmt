package com.qidian.camera.module.auth.service;

import com.qidian.camera.module.auth.entity.Api;
import com.qidian.camera.module.auth.mapper.ApiMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * API 接口服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService extends ServiceImpl<ApiMapper, Api> {
    
    /**
     * 根据模块获取 API 列表
     */
    public List<Api> getByModule(String moduleCode) {
        return lambdaQuery()
            .eq(Api::getModuleCode, moduleCode)
            .list();
    }
    
    /**
     * 根据路径和方法获取 API
     */
    public Api getByPathAndMethod(String apiPath, String httpMethod) {
        return lambdaQuery()
            .eq(Api::getApiPath, apiPath)
            .eq(Api::getHttpMethod, httpMethod)
            .one();
    }
}
