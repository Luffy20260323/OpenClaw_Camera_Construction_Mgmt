package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.ProtectedObject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 保护对象 Mapper
 */
@Mapper
public interface ProtectedObjectMapper extends BaseMapper<ProtectedObject> {
}
