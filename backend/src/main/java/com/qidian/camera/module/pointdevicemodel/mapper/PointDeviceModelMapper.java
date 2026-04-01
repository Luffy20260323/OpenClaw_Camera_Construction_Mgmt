package com.qidian.camera.module.pointdevicemodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.pointdevicemodel.entity.PointDeviceModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点位设备模型 Mapper
 */
@Mapper
public interface PointDeviceModelMapper extends BaseMapper<PointDeviceModel> {

    /**
     * 查询点位设备模型列表（带创建人信息）
     *
     * @param isActive 是否启用
     * @return 模型列表
     */
    @Select("SELECT pdm.*, u.real_name AS creator_name " +
            "FROM point_device_models pdm " +
            "LEFT JOIN users u ON pdm.created_by = u.id " +
            "WHERE 1=1 " +
            "<if test='isActive != null'>" +
            "AND pdm.is_active = #{isActive} " +
            "</if>" +
            "ORDER BY pdm.id ASC")
    List<PointDeviceModel> selectListWithCreator(@Param("isActive") Boolean isActive);

    /**
     * 查询点位设备模型详情（带创建人信息）
     *
     * @param id 模型 ID
     * @return 模型信息
     */
    @Select("SELECT pdm.*, u.real_name AS creator_name " +
            "FROM point_device_models pdm " +
            "LEFT JOIN users u ON pdm.created_by = u.id " +
            "WHERE pdm.id = #{id}")
    PointDeviceModel selectByIdWithCreator(@Param("id") Long id);

    /**
     * 根据编码查询模型
     *
     * @param code 模型编码
     * @return 模型信息
     */
    @Select("SELECT * FROM point_device_models WHERE code = #{code}")
    PointDeviceModel selectByCode(@Param("code") String code);
}
