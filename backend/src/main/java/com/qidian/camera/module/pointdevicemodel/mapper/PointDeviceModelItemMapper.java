package com.qidian.camera.module.pointdevicemodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.pointdevicemodel.entity.PointDeviceModelItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点位设备模型项 Mapper
 */
@Mapper
public interface PointDeviceModelItemMapper extends BaseMapper<PointDeviceModelItem> {

    /**
     * 根据模型 ID 查询模型项列表（带零部件种类信息）
     *
     * @param modelId 模型 ID
     * @return 模型项列表
     */
    @Select("SELECT pdmi.*, ct.name AS component_type_name, ct.code AS component_type_code " +
            "FROM point_device_model_items pdmi " +
            "LEFT JOIN component_types ct ON pdmi.component_type_id = ct.id " +
            "WHERE pdmi.model_id = #{modelId} " +
            "ORDER BY pdmi.sequence_no ASC, pdmi.id ASC")
    List<PointDeviceModelItem> selectByModelIdWithComponent(@Param("modelId") Long modelId);

    /**
     * 根据模型 ID 删除所有模型项
     *
     * @param modelId 模型 ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM point_device_model_items WHERE model_id = #{modelId}")
    int deleteByModelId(@Param("modelId") Long modelId);
}
