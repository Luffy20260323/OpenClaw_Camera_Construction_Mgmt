package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.RoleDataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色数据范围 Mapper
 */
@Mapper
public interface RoleDataScopeMapper extends BaseMapper<RoleDataScope> {
    
    /**
     * 根据角色 ID 查询数据范围
     */
    @Select("SELECT * FROM role_data_scope WHERE role_id = #{roleId} ORDER BY id")
    List<RoleDataScope> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色 ID 和模块编码查询数据范围
     */
    @Select("SELECT * FROM role_data_scope WHERE role_id = #{roleId} AND module_code = #{moduleCode}")
    RoleDataScope findByRoleIdAndModuleCode(@Param("roleId") Long roleId, @Param("moduleCode") String moduleCode);
    
    /**
     * 查询角色的有效数据范围
     */
    @Select("SELECT * FROM role_data_scope WHERE role_id = #{roleId} AND is_effective = true ORDER BY created_at DESC")
    List<RoleDataScope> findValidByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据数据范围类型查询
     */
    @Select("SELECT * FROM role_data_scope WHERE scope_type = #{scopeType} ORDER BY id")
    List<RoleDataScope> findByScopeType(@Param("scopeType") String scopeType);
}