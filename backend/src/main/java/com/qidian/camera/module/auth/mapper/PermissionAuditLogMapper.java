package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.PermissionAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限审计日志 Mapper
 */
@Mapper
public interface PermissionAuditLogMapper extends BaseMapper<PermissionAuditLog> {
    
    /**
     * 查询操作日志
     */
    List<PermissionAuditLog> selectByOperatorId(@Param("operatorId") Long operatorId);
    
    /**
     * 查询目标日志
     */
    List<PermissionAuditLog> selectByTarget(@Param("targetType") String targetType, 
                                            @Param("targetId") Long targetId);
}
