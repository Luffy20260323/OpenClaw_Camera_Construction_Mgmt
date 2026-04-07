package com.qidian.camera.module.component.service;

import com.qidian.camera.module.component.dto.ComponentTypeDTO;

import java.util.List;

/**
 * 零部件种类服务接口
 */
public interface ComponentTypeService {

    /**
     * 获取零部件种类列表
     *
     * @param isActive 是否启用（null 表示全部）
     * @return 种类列表
     */
    List<ComponentTypeDTO> getComponentTypeList(Boolean isActive);

    /**
     * 获取零部件种类详情
     *
     * @param id 种类 ID
     * @return 种类详情
     */
    ComponentTypeDTO getComponentTypeDetail(Long id);

    /**
     * 创建零部件种类
     *
     * @param dto 种类信息
     * @param userId 创建人 ID
     * @return 创建后的种类
     */
    ComponentTypeDTO createComponentType(ComponentTypeDTO dto, Long userId);

    /**
     * 更新零部件种类
     *
     * @param id 种类 ID
     * @param dto 种类信息
     * @return 更新后的种类
     */
    ComponentTypeDTO updateComponentType(Long id, ComponentTypeDTO dto);

    /**
     * 删除零部件种类
     *
     * @param id 种类 ID
     */
    void deleteComponentType(Long id);

    /**
     * 切换零部件种类状态
     *
     * @param id 种类 ID
     * @return 更新后的种类信息
     */
    ComponentTypeDTO toggleComponentTypeStatus(Long id);
}
