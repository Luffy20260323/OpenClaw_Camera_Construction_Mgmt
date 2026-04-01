package com.qidian.camera.module.component.service;

import com.qidian.camera.module.component.dto.ComponentAttrSetDTO;

import java.util.List;

/**
 * 零部件属性集服务接口
 */
public interface ComponentAttrSetService {

    /**
     * 获取属性集列表
     *
     * @param componentTypeId 零部件种类 ID（可选）
     * @param isActive        是否启用（可选）
     * @return 属性集列表
     */
    List<ComponentAttrSetDTO> getAttrSetList(Long componentTypeId, Boolean isActive);

    /**
     * 获取属性集详情（含属性定义）
     *
     * @param id 属性集 ID
     * @return 属性集详情
     */
    ComponentAttrSetDTO getAttrSetDetail(Long id);

    /**
     * 创建属性集
     *
     * @param dto    属性集 DTO
     * @param userId 创建人 ID
     * @return 创建的属性集
     */
    ComponentAttrSetDTO createAttrSet(ComponentAttrSetDTO dto, Long userId);

    /**
     * 更新属性集
     *
     * @param id     属性集 ID
     * @param dto    属性集 DTO
     * @return 更新后的属性集
     */
    ComponentAttrSetDTO updateAttrSet(Long id, ComponentAttrSetDTO dto);

    /**
     * 删除属性集
     *
     * @param id 属性集 ID
     */
    void deleteAttrSet(Long id);
}
