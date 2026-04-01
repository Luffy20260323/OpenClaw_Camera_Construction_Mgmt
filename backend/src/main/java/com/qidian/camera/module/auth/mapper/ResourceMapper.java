package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资源 Mapper
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
    
    /**
     * 查询所有启用的资源
     */
    @Select("SELECT * FROM resource WHERE status = 1 ORDER BY sort_order")
    List<Resource> findAllEnabled();
    
    /**
     * 查询指定类型的资源
     */
    @Select("SELECT * FROM resource WHERE type = #{type} AND status = 1 ORDER BY sort_order")
    List<Resource> findByType(@Param("type") String type);
    
    /**
     * 查询指定父资源的子资源
     */
    @Select("SELECT * FROM resource WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order")
    List<Resource> findByParentId(@Param("parentId") Long parentId);
    
    /**
     * 查询基本权限资源
     */
    @Select("SELECT * FROM resource WHERE is_basic = 1 AND status = 1")
    List<Resource> findBasicResources();
    
    /**
     * 根据 permission_key 查询资源
     */
    @Select("SELECT * FROM resource WHERE permission_key = #{permissionKey}")
    Resource findByPermissionKey(@Param("permissionKey") String permissionKey);
    
    /**
     * 根据 URI 和 Method 查询 API 资源
     */
    @Select("SELECT * FROM resource WHERE type = 'API' AND uri_pattern = #{uriPattern} AND method = #{method}")
    Resource findByUriAndMethod(@Param("uriPattern") String uriPattern, @Param("method") String method);
    
    /**
     * 查询资源树（所有模块和菜单）
     */
    @Select("SELECT * FROM resource WHERE type IN ('MODULE', 'MENU') AND status = 1 ORDER BY sort_order")
    List<Resource> findMenuTree();
}