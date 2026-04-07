package com.qidian.camera.module.component.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.component.entity.ComponentType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 零部件种类 Mapper
 */
@Mapper
public interface ComponentTypeMapper extends BaseMapper<ComponentType> {

    /**
     * 查询零部件种类列表（带创建人信息）
     *
     * @param isActive 是否启用
     * @return 种类列表
     */
    @Select("<script>" +
            "SELECT ct.*, u.real_name AS creator_name " +
            "FROM component_types ct " +
            "LEFT JOIN users u ON ct.created_by = u.id " +
            "WHERE 1=1 " +
            "<if test='isActive != null'>" +
            "AND ct.is_active = #{isActive} " +
            "</if>" +
            "ORDER BY ct.sequence_no ASC, ct.id ASC" +
            "</script>")
    List<ComponentType> selectListWithCreator(@Param("isActive") Boolean isActive);

    /**
     * 查询零部件种类详情（带创建人信息）
     *
     * @param id 种类 ID
     * @return 种类信息
     */
    @Select("SELECT ct.*, u.real_name AS creator_name " +
            "FROM component_types ct " +
            "LEFT JOIN users u ON ct.created_by = u.id " +
            "WHERE ct.id = #{id}")
    ComponentType selectByIdWithCreator(@Param("id") Long id);
}
