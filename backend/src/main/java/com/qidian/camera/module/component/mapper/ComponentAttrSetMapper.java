package com.qidian.camera.module.component.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.component.entity.ComponentAttrSet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 零部件属性集 Mapper
 */
@Mapper
public interface ComponentAttrSetMapper extends BaseMapper<ComponentAttrSet> {

    /**
     * 查询属性集列表（带零部件种类名称和创建人信息）
     *
     * @param componentTypeId 零部件种类 ID（可选）
     * @param isActive        是否启用（可选）
     * @return 属性集列表
     */
    @Select("SELECT cas.*, ct.name AS component_type_name, u.real_name AS creator_name " +
            "FROM component_attr_sets cas " +
            "LEFT JOIN component_types ct ON cas.component_type_id = ct.id " +
            "LEFT JOIN users u ON cas.created_by = u.id " +
            "WHERE 1=1 " +
            "<if test='componentTypeId != null'>" +
            "AND cas.component_type_id = #{componentTypeId} " +
            "</if>" +
            "<if test='isActive != null'>" +
            "AND cas.is_active = #{isActive} " +
            "</if>" +
            "ORDER BY cas.sequence_no ASC, cas.created_at DESC")
    List<ComponentAttrSet> selectListWithDetails(@Param("componentTypeId") Long componentTypeId,
                                                  @Param("isActive") Boolean isActive);

    /**
     * 查询属性集详情（带零部件种类名称和创建人信息）
     *
     * @param id 属性集 ID
     * @return 属性集信息
     */
    @Select("SELECT cas.*, ct.name AS component_type_name, u.real_name AS creator_name " +
            "FROM component_attr_sets cas " +
            "LEFT JOIN component_types ct ON cas.component_type_id = ct.id " +
            "LEFT JOIN users u ON cas.created_by = u.id " +
            "WHERE cas.id = #{id}")
    ComponentAttrSet selectByIdWithDetails(@Param("id") Long id);

    /**
     * 根据编码查询属性集
     *
     * @param code 属性集编码
     * @return 属性集信息
     */
    @Select("SELECT * FROM component_attr_sets WHERE code = #{code}")
    ComponentAttrSet selectByCode(@Param("code") String code);
}
