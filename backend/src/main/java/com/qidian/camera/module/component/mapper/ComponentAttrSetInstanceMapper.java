package com.qidian.camera.module.component.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.component.entity.ComponentAttrSetInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 零部件属性集实例 Mapper
 */
@Mapper
public interface ComponentAttrSetInstanceMapper extends BaseMapper<ComponentAttrSetInstance> {

    /**
     * 查询实例列表（带属性集和创建人信息）
     *
     * @param attrSetId 属性集 ID（可选）
     * @return 实例列表
     */
    @Select("<script>" +
            "SELECT cai.*, cas.name AS attr_set_name, cas.component_type_id, " +
            "ct.name AS component_type_name, u.real_name AS creator_name " +
            "FROM component_attr_set_instances cai " +
            "LEFT JOIN component_attr_sets cas ON cai.attr_set_id = cas.id " +
            "LEFT JOIN component_types ct ON cas.component_type_id = ct.id " +
            "LEFT JOIN users u ON cai.created_by = u.id " +
            "<if test='attrSetId != null'>" +
            "WHERE cai.attr_set_id = #{attrSetId} " +
            "</if>" +
            "ORDER BY cai.created_at DESC" +
            "</script>")
    List<ComponentAttrSetInstance> selectListWithDetails(@Param("attrSetId") Long attrSetId);

    /**
     * 查询实例详情（带属性集和创建人信息）
     *
     * @param id 实例 ID
     * @return 实例信息
     */
    @Select("SELECT cai.*, cas.name AS attr_set_name, cas.component_type_id, " +
            "ct.name AS component_type_name, u.real_name AS creator_name " +
            "FROM component_attr_set_instances cai " +
            "LEFT JOIN component_attr_sets cas ON cai.attr_set_id = cas.id " +
            "LEFT JOIN component_types ct ON cas.component_type_id = ct.id " +
            "LEFT JOIN users u ON cai.created_by = u.id " +
            "WHERE cai.id = #{id}")
    ComponentAttrSetInstance selectByIdWithDetails(@Param("id") Long id);
}
