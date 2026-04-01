package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.ElementPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 元素 - 权限关联 Mapper
 */
@Mapper
public interface ElementPermissionMapper extends BaseMapper<ElementPermission> {
}
