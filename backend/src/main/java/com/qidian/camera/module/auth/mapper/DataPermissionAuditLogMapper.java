package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.DataPermissionAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据权限审计日志 Mapper
 */
@Mapper
public interface DataPermissionAuditLogMapper extends BaseMapper<DataPermissionAuditLog> {
}
