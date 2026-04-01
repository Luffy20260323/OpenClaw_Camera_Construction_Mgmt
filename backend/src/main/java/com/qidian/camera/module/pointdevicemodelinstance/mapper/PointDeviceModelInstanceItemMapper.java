package com.qidian.camera.module.pointdevicemodelinstance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.pointdevicemodelinstance.entity.PointDeviceModelInstanceItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点位设备模型实例项 Mapper
 */
@Mapper
public interface PointDeviceModelInstanceItemMapper extends BaseMapper<PointDeviceModelInstanceItem> {

    /**
     * 根据实例 ID 查询实例项列表（带零部件种类和实例信息）
     *
     * @param instanceId 实例 ID
     * @return 实例项列表
     */
    @Select("SELECT pdmii.*, ct.name AS component_type_name, ct.code AS component_type_code, " +
            "       ci.name AS component_instance_name, ci.code AS component_instance_code " +
            "FROM point_device_model_instance_items pdmii " +
            "LEFT JOIN component_types ct ON pdmii.component_type_id = ct.id " +
            "LEFT JOIN component_instances ci ON pdmii.component_instance_id = ci.id " +
            "WHERE pdmii.instance_id = #{instanceId} " +
            "ORDER BY pdmii.sequence_no ASC, pdmii.id ASC")
    List<PointDeviceModelInstanceItem> selectByInstanceIdWithComponent(@Param("instanceId") Long instanceId);

    /**
     * 根据实例 ID 删除所有实例项
     *
     * @param instanceId 实例 ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM point_device_model_instance_items WHERE instance_id = #{instanceId}")
    int deleteByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 根据实例 ID 和零部件种类 ID 查询实例项
     *
     * @param instanceId 实例 ID
     * @param componentTypeId 零部件种类 ID
     * @return 实例项
     */
    @Select("SELECT * FROM point_device_model_instance_items " +
            "WHERE instance_id = #{instanceId} AND component_type_id = #{componentTypeId}")
    PointDeviceModelInstanceItem selectByInstanceIdAndComponentTypeId(
            @Param("instanceId") Long instanceId,
            @Param("componentTypeId") Long componentTypeId);
}
