package com.qidian.camera.module.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.role.entity.RoleResource;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色资源 Mapper
 */
@Mapper
public interface RoleResourceMapper extends BaseMapper<RoleResource> {

    /**
     * 根据角色类型获取缺省资源 ID 列表
     */
    @Select("SELECT resource_id FROM role_type_default_permissions WHERE role_type = #{roleType} ORDER BY sort_order")
    List<Long> findDefaultResourceIdsByType(@Param("roleType") String roleType);
    
    /**
     * 插入角色缺省权限
     */
    @Insert("INSERT INTO role_resource (role_id, resource_id, permission_type, created_at, created_by) " +
            "VALUES (#{roleId}, #{resourceId}, 'default', CURRENT_TIMESTAMP, #{createdBy}) " +
            "ON CONFLICT (role_id, resource_id) DO NOTHING")
    void insertDefaultPermission(@Param("roleId") Long roleId, 
                                  @Param("resourceId") Long resourceId, 
                                  @Param("createdBy") Long createdBy);
    
    /**
     * 统计角色关联的用户数
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE role_id = #{roleId}")
    Long countUsersByRoleId(@Param("roleId") Long roleId);
}
