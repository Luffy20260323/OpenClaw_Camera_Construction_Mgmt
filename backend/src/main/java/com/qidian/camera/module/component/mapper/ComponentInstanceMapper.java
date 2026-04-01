package com.qidian.camera.module.component.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.component.entity.ComponentInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 零部件实例 Mapper
 */
@Mapper
public interface ComponentInstanceMapper extends BaseMapper<ComponentInstance> {

    /**
     * 查询实例列表（带零部件种类和创建人信息）
     *
     * @param componentTypeId 零部件种类 ID（可选）
     * @param status 状态（可选）
     * @return 实例列表
     */
    @Select("SELECT ci.*, ct.name AS component_type_name, " +
            "cai.name AS attr_set_instance_name, u.real_name AS creator_name " +
            "FROM component_instances ci " +
            "LEFT JOIN component_types ct ON ci.component_type_id = ct.id " +
            "LEFT JOIN component_attr_set_instances cai ON ci.attr_set_instance_id = cai.id " +
            "LEFT JOIN users u ON ci.created_by = u.id " +
            "<where>" +
            "<if test='componentTypeId != null'>" +
            "AND ci.component_type_id = #{componentTypeId} " +
            "</if>" +
            "<if test='status != null and status != '''>" +
            "AND ci.status = #{status} " +
            "</if>" +
            "</where>" +
            "ORDER BY ci.created_at DESC")
    List<ComponentInstance> selectListWithDetails(@Param("componentTypeId") Long componentTypeId,
                                                   @Param("status") String status);

    /**
     * 查询实例详情（带零部件种类和创建人信息）
     *
     * @param id 实例 ID
     * @return 实例信息
     */
    @Select("SELECT ci.*, ct.name AS component_type_name, " +
            "cai.name AS attr_set_instance_name, u.real_name AS creator_name " +
            "FROM component_instances ci " +
            "LEFT JOIN component_types ct ON ci.component_type_id = ct.id " +
            "LEFT JOIN component_attr_set_instances cai ON ci.attr_set_instance_id = cai.id " +
            "LEFT JOIN users u ON ci.created_by = u.id " +
            "WHERE ci.id = #{id}")
    ComponentInstance selectByIdWithDetails(@Param("id") Long id);
}
