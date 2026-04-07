package com.qidian.camera.module.pointdevicemodelinstance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.pointdevicemodelinstance.entity.PointDeviceModelInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点位设备模型实例 Mapper
 */
@Mapper
public interface PointDeviceModelInstanceMapper extends BaseMapper<PointDeviceModelInstance> {

    /**
     * 查询点位设备模型实例列表（带创建人和模型信息）
     *
     * @param isActive 是否启用
     * @return 实例列表
     */
    @Select("<script>" +
            "SELECT pdmi.*, u.real_name AS creator_name, pdm.name AS model_name, pdm.code AS model_code " +
            "FROM point_device_model_instances pdmi " +
            "LEFT JOIN users u ON pdmi.created_by = u.id " +
            "LEFT JOIN point_device_models pdm ON pdmi.model_id = pdm.id " +
            "WHERE 1=1 " +
            "<if test='isActive != null'>" +
            "AND pdmi.is_active = #{isActive} " +
            "</if>" +
            "ORDER BY pdmi.id ASC" +
            "</script>")
    List<PointDeviceModelInstance> selectListWithCreator(@Param("isActive") Boolean isActive);

    /**
     * 查询点位设备模型实例详情（带创建人和模型信息）
     *
     * @param id 实例 ID
     * @return 实例信息
     */
    @Select("SELECT pdmi.*, u.real_name AS creator_name, pdm.name AS model_name, pdm.code AS model_code " +
            "FROM point_device_model_instances pdmi " +
            "LEFT JOIN users u ON pdmi.created_by = u.id " +
            "LEFT JOIN point_device_models pdm ON pdmi.model_id = pdm.id " +
            "WHERE pdmi.id = #{id}")
    PointDeviceModelInstance selectByIdWithCreator(@Param("id") Long id);

    /**
     * 根据编码查询实例
     *
     * @param code 实例编码
     * @return 实例信息
     */
    @Select("SELECT * FROM point_device_model_instances WHERE code = #{code}")
    PointDeviceModelInstance selectByCode(@Param("code") String code);
}
