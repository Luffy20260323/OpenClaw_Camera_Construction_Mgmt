package com.qidian.camera.module.pointdevicemodelinstance.service;

import com.qidian.camera.module.pointdevicemodelinstance.dto.PointDeviceModelInstanceDTO;

import java.util.List;

/**
 * 点位设备模型实例服务接口
 */
public interface PointDeviceModelInstanceService {

    /**
     * 获取点位设备模型实例列表
     *
     * @param isActive 是否启用（null 表示全部）
     * @return 实例列表
     */
    List<PointDeviceModelInstanceDTO> getInstanceList(Boolean isActive);

    /**
     * 获取点位设备模型实例详情（含所有实例项）
     *
     * @param id 实例 ID
     * @return 实例详情
     */
    PointDeviceModelInstanceDTO getInstanceDetail(Long id);

    /**
     * 从模型创建点位设备模型实例
     *
     * @param modelId 模型 ID
     * @param name    实例名称
     * @param code    实例编码
     * @param description 描述
     * @param userId  创建人 ID
     * @return 创建后的实例
     */
    PointDeviceModelInstanceDTO createInstanceFromModel(Long modelId, String name, String code, String description, Long userId);

    /**
     * 创建点位设备模型实例（自定义实例项）
     *
     * @param dto    实例 DTO
     * @param userId 创建人 ID
     * @return 创建后的实例
     */
    PointDeviceModelInstanceDTO createInstance(PointDeviceModelInstanceDTO dto, Long userId);

    /**
     * 更新点位设备模型实例
     *
     * @param id  实例 ID
     * @param dto 实例 DTO
     * @return 更新后的实例
     */
    PointDeviceModelInstanceDTO updateInstance(Long id, PointDeviceModelInstanceDTO dto);

    /**
     * 删除点位设备模型实例
     *
     * @param id 实例 ID
     */
    void deleteInstance(Long id);

    /**
     * 更新实例项（更换零部件实例）
     *
     * @param instanceId 实例 ID
     * @param itemId 实例项 ID
     * @param componentInstanceId 新的零部件实例 ID
     * @return 更新后的实例项
     */
    PointDeviceModelInstanceDTO updateInstanceItem(Long instanceId, Long itemId, Long componentInstanceId);
}
