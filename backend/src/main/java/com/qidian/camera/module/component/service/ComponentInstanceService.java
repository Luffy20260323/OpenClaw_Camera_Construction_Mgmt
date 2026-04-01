package com.qidian.camera.module.component.service;

import com.qidian.camera.module.component.dto.ComponentInstanceDTO;

import java.util.List;

/**
 * 零部件实例服务接口
 */
public interface ComponentInstanceService {

    /**
     * 获取实例列表
     *
     * @param componentTypeId 零部件种类 ID（可选）
     * @param status 状态（可选）
     * @return 实例列表
     */
    List<ComponentInstanceDTO> getInstanceList(Long componentTypeId, String status);

    /**
     * 获取实例详情
     *
     * @param id 实例 ID
     * @return 实例详情
     */
    ComponentInstanceDTO getInstanceDetail(Long id);

    /**
     * 创建实例
     *
     * @param dto 实例 DTO
     * @param userId 创建人 ID
     * @return 创建的实例 DTO
     */
    ComponentInstanceDTO createInstance(ComponentInstanceDTO dto, Long userId);

    /**
     * 更新实例
     *
     * @param id 实例 ID
     * @param dto 实例 DTO
     * @return 更新后的实例 DTO
     */
    ComponentInstanceDTO updateInstance(Long id, ComponentInstanceDTO dto);

    /**
     * 删除实例
     *
     * @param id 实例 ID
     */
    void deleteInstance(Long id);

    /**
     * 更新实例状态
     *
     * @param id 实例 ID
     * @param status 新状态
     * @return 更新后的实例 DTO
     */
    ComponentInstanceDTO updateStatus(Long id, String status);
}
