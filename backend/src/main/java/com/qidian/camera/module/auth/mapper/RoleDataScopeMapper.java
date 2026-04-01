package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.RoleDataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色数据权限 Mapper
 */
@Mapper
public interface RoleDataScopeMapper extends BaseMapper<RoleDataScope> {
    
    /**
     * 查询角色的数据权限配置
     */
    @Select("SELECT * FROM role_data_scope WHERE role_id = #{roleId}")
    List<RoleDataScope> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询角色指定资源的数据权限
     */
    @Select("SELECT * FROM role_data_scope WHERE role_id = #{roleId} AND resource_id = #{resourceId}")
    RoleDataScope findByRoleIdAndResourceId(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId);
}