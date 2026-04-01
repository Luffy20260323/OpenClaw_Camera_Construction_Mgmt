package com.qidian.camera.module.component.service;

import com.qidian.camera.module.component.dto.ComponentAttrSetInstanceDTO;

import java.util.List;

/**
 * 零部件属性集实例服务接口
 */
public interface ComponentAttrSetInstanceService {

    /**
     * 获取实例列表
     *
     * @param attrSetId 属性集 ID（可选）
     * @return 实例列表
     */
    List<ComponentAttrSetInstanceDTO> getInstanceList(Long attrSetId);

    /**
     * 获取实例详情
     *
     * @param id 实例 ID
     * @return 实例详情
     */
    ComponentAttrSetInstanceDTO getInstanceDetail(Long id);

    /**
     * 创建实例
     *
     * @param dto 实例 DTO
     * @param userId 用户 ID
     * @return 创建的实例
     */
    ComponentAttrSetInstanceDTO createInstance(ComponentAttrSetInstanceDTO dto, Long userId);

    /**
     * 更新实例
     *
     * @param id 实例 ID
     * @param dto 实例 DTO
     * @return 更新后的实例
     */
    ComponentAttrSetInstanceDTO updateInstance(Long id, ComponentAttrSetInstanceDTO dto);

    /**
     * 删除实例
     *
     * @param id 实例 ID
     */
    void deleteInstance(Long id);
}
