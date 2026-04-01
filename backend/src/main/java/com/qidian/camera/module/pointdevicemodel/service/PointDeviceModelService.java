package com.qidian.camera.module.pointdevicemodel.service;

import com.qidian.camera.module.pointdevicemodel.dto.PointDeviceModelDTO;

import java.util.List;

/**
 * 点位设备模型服务接口
 */
public interface PointDeviceModelService {

    /**
     * 获取点位设备模型列表
     *
     * @param isActive 是否启用（null 表示全部）
     * @return 模型列表
     */
    List<PointDeviceModelDTO> getModelList(Boolean isActive);

    /**
     * 获取点位设备模型详情（含所有模型项）
     *
     * @param id 模型 ID
     * @return 模型详情
     */
    PointDeviceModelDTO getModelDetail(Long id);

    /**
     * 创建点位设备模型（含模型项）
     *
     * @param dto    模型 DTO
     * @param userId 创建人 ID
     * @return 创建后的模型
     */
    PointDeviceModelDTO createModel(PointDeviceModelDTO dto, Long userId);

    /**
     * 更新点位设备模型（含模型项）
     *
     * @param id  模型 ID
     * @param dto 模型 DTO
     * @return 更新后的模型
     */
    PointDeviceModelDTO updateModel(Long id, PointDeviceModelDTO dto);

    /**
     * 删除点位设备模型
     *
     * @param id 模型 ID
     */
    void deleteModel(Long id);
}
