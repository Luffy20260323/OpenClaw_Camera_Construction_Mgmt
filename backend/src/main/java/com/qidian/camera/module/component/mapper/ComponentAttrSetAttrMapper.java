package com.qidian.camera.module.component.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.component.entity.ComponentAttrSetAttr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 属性集属性定义 Mapper
 */
@Mapper
public interface ComponentAttrSetAttrMapper extends BaseMapper<ComponentAttrSetAttr> {

    /**
     * 根据属性集 ID 查询属性定义列表
     *
     * @param attrSetId 属性集 ID
     * @return 属性定义列表
     */
    @Select("SELECT * FROM component_attr_set_attrs WHERE attr_set_id = #{attrSetId} ORDER BY sequence_no ASC")
    List<ComponentAttrSetAttr> selectByAttrSetId(@Param("attrSetId") Long attrSetId);

    /**
     * 根据属性集 ID 列表查询属性定义
     *
     * @param attrSetIds 属性集 ID 列表
     * @return 属性定义列表
     */
    @Select("SELECT * FROM component_attr_set_attrs WHERE attr_set_id IN " +
            "<foreach item='id' collection='attrSetIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "ORDER BY attr_set_id, sequence_no ASC")
    List<ComponentAttrSetAttr> selectByAttrSetIds(@Param("attrSetIds") List<Long> attrSetIds);
}
