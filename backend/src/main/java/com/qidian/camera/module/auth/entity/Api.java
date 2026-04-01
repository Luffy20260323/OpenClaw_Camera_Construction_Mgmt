package com.qidian.camera.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 接口实体
 * 对应表：apis
 * 用于记录所有 API 接口
 */
@Data
@TableName("apis")
public class Api {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * HTTP 方法：GET, POST, PUT, DELETE
     */
    private String httpMethod;
    
    /**
     * API 路径，如：/api/user
     */
    private String apiPath;
    
    /**
     * API 名称
     */
    private String apiName;
    
    /**
     * API 描述
     */
    private String apiDescription;
    
    /**
     * 所属模块
     */
    private String moduleCode;
    
    /**
     * 是否已废弃
     */
    private Boolean isDeprecated;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
