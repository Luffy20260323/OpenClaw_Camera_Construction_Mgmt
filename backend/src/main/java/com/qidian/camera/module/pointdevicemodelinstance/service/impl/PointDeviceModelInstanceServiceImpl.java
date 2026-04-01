package com.qidian.camera.module.pointdevicemodelinstance.service.impl;

import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.module.pointdevicemodel.dto.PointDeviceModelDTO;
import com.qidian.camera.module.pointdevicemodel.dto.PointDeviceModelItemDTO;
import com.qidian.camera.module.pointdevicemodel.service.PointDeviceModelService;
import com.qidian.camera.module.pointdevicemodelinstance.dto.PointDeviceModelInstanceDTO;
import com.qidian.camera.module.pointdevicemodelinstance.dto.PointDeviceModelInstanceItemDTO;
import com.qidian.camera.module.pointdevicemodelinstance.entity.PointDeviceModelInstance;
import com.qidian.camera.module.pointdevicemodelinstance.entity.PointDeviceModelInstanceItem;
import com.qidian.camera.module.pointdevicemodelinstance.mapper.PointDeviceModelInstanceItemMapper;
import com.qidian.camera.module.pointdevicemodelinstance.mapper.PointDeviceModelInstanceMapper;
import com.qidian.camera.module.pointdevicemodelinstance.service.PointDeviceModelInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 点位设备模型实例服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointDeviceModelInstanceServiceImpl implements PointDeviceModelInstanceService {

    private final PointDeviceModelInstanceMapper pointDeviceModelInstanceMapper;
    private final PointDeviceModelInstanceItemMapper pointDeviceModelInstanceItemMapper;
    private final PointDeviceModelService pointDeviceModelService;

    /**
     * 实体转 DTO
     */
    private PointDeviceModelInstanceDTO entityToDTO(PointDeviceModelInstance entity) {
        if (entity == null) {
            return null;
        }
        PointDeviceModelInstanceDTO dto = new PointDeviceModelInstanceDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 实例项实体转 DTO
     */
    private PointDeviceModelInstanceItemDTO itemEntityToDTO(PointDeviceModelInstanceItem entity) {
        if (entity == null) {
            return null;
        }
        PointDeviceModelInstanceItemDTO dto = new PointDeviceModelInstanceItemDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO 转实体
     */
    private PointDeviceModelInstance dtoToEntity(PointDeviceModelInstanceDTO dto) {
        if (dto == null) {
            return null;
        }
        PointDeviceModelInstance entity = new PointDeviceModelInstance();
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "updatedAt", "creatorName", "modelName", "modelCode", "items");
        return entity;
    }

    /**
     * 实例项 DTO 转实体
     */
    private PointDeviceModelInstanceItem itemDTOToEntity(PointDeviceModelInstanceItemDTO dto) {
        if (dto == null) {
            return null;
        }
        PointDeviceModelInstanceItem entity = new PointDeviceModelInstanceItem();
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "updatedAt", 
                "componentTypeName", "componentTypeCode", "componentInstanceName", "componentInstanceCode");
        return entity;
    }

    @Override
    public List<PointDeviceModelInstanceDTO> getInstanceList(Boolean isActive) {
        List<PointDeviceModelInstance> instances = pointDeviceModelInstanceMapper.selectListWithCreator(isActive);
        return instances.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PointDeviceModelInstanceDTO getInstanceDetail(Long id) {
        PointDeviceModelInstance instance = pointDeviceModelInstanceMapper.selectByIdWithCreator(id);
        if (instance == null) {
            throw new BusinessException("点位设备模型实例不存在");
        }

        PointDeviceModelInstanceDTO dto = entityToDTO(instance);

        // 查询实例项列表
        List<PointDeviceModelInstanceItem> items = pointDeviceModelInstanceItemMapper.selectByInstanceIdWithComponent(id);
        List<PointDeviceModelInstanceItemDTO> itemDTOs = items.stream()
                .map(this::itemEntityToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    @Override
    @Transactional
    public PointDeviceModelInstanceDTO createInstanceFromModel(Long modelId, String name, String code, String description, Long userId) {
        // 检查模型是否存在
        PointDeviceModelDTO model = pointDeviceModelService.getModelDetail(modelId);
        if (model == null) {
            throw new BusinessException("点位设备模型不存在");
        }

        // 检查编码是否已存在
        PointDeviceModelInstance existing = pointDeviceModelInstanceMapper.selectByCode(code);
        if (existing != null) {
            throw new BusinessException("实例编码已存在：" + code);
        }

        // 检查必填字段
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("实例名称不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException("实例编码不能为空");
        }

        // 创建实例
        PointDeviceModelInstance entity = new PointDeviceModelInstance();
        entity.setModelId(modelId);
        entity.setName(name);
        entity.setCode(code);
        entity.setDescription(description);
        entity.setIsActive(true);
        entity.setCreatedBy(userId);

        pointDeviceModelInstanceMapper.insert(entity);
        log.info("点位设备模型实例创建成功：{} (ID: {}) from model {} by user {}", 
                entity.getName(), entity.getId(), model.getName(), userId);

        // 从模型复制模型项，创建实例项（componentInstanceId 需要另外指定，这里先不创建）
        // 实例项需要在创建后通过 updateInstanceItem 来关联具体的零部件实例
        if (model.getItems() != null && !model.getItems().isEmpty()) {
            for (PointDeviceModelItemDTO itemDTO : model.getItems()) {
                PointDeviceModelInstanceItem itemEntity = new PointDeviceModelInstanceItem();
                itemEntity.setInstanceId(entity.getId());
                itemEntity.setComponentTypeId(itemDTO.getComponentTypeId());
                itemEntity.setComponentInstanceId(null); // 初始化为 null，需要后续指定
                itemEntity.setSequenceNo(itemDTO.getSequenceNo());

                pointDeviceModelInstanceItemMapper.insert(itemEntity);
            }
            log.info("点位设备模型实例项创建成功：{} 项（待关联零部件实例）", model.getItems().size());
        }

        return entityToDTO(entity);
    }

    @Override
    @Transactional
    public PointDeviceModelInstanceDTO createInstance(PointDeviceModelInstanceDTO dto, Long userId) {
        // 检查编码是否已存在
        PointDeviceModelInstance existing = pointDeviceModelInstanceMapper.selectByCode(dto.getCode());
        if (existing != null) {
            throw new BusinessException("实例编码已存在：" + dto.getCode());
        }

        // 检查必填字段
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException("实例名称不能为空");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new BusinessException("实例编码不能为空");
        }
        if (dto.getModelId() == null) {
            throw new BusinessException("关联模型 ID 不能为空");
        }

        // 创建实例
        PointDeviceModelInstance entity = dtoToEntity(dto);
        entity.setCreatedBy(userId);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        pointDeviceModelInstanceMapper.insert(entity);
        log.info("点位设备模型实例创建成功：{} (ID: {}) by user {}", entity.getName(), entity.getId(), userId);

        // 创建实例项
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PointDeviceModelInstanceItemDTO itemDTO : dto.getItems()) {
                if (itemDTO.getComponentTypeId() == null) {
                    throw new BusinessException("实例项的零部件种类 ID 不能为空");
                }

                PointDeviceModelInstanceItem itemEntity = itemDTOToEntity(itemDTO);
                itemEntity.setInstanceId(entity.getId());
                if (itemEntity.getSequenceNo() == null) {
                    itemEntity.setSequenceNo(10);
                }

                pointDeviceModelInstanceItemMapper.insert(itemEntity);
            }
            log.info("点位设备模型实例项创建成功：{} 项", dto.getItems().size());
        }

        return entityToDTO(entity);
    }

    @Override
    @Transactional
    public PointDeviceModelInstanceDTO updateInstance(Long id, PointDeviceModelInstanceDTO dto) {
        PointDeviceModelInstance existing = pointDeviceModelInstanceMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("点位设备模型实例不存在");
        }

        // 如果更新了编码，检查新编码是否已被其他记录使用
        if (dto.getCode() != null && !dto.getCode().equals(existing.getCode())) {
            PointDeviceModelInstance codeExists = pointDeviceModelInstanceMapper.selectByCode(dto.getCode());
            if (codeExists != null && !codeExists.getId().equals(id)) {
                throw new BusinessException("实例编码已存在：" + dto.getCode());
            }
        }

        // 更新实例字段
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getCode() != null) {
            existing.setCode(dto.getCode());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getIsActive() != null) {
            existing.setIsActive(dto.getIsActive());
        }

        pointDeviceModelInstanceMapper.updateById(existing);
        log.info("点位设备模型实例更新成功：{} (ID: {})", existing.getName(), existing.getId());

        return entityToDTO(existing);
    }

    @Override
    @Transactional
    public void deleteInstance(Long id) {
        PointDeviceModelInstance instance = pointDeviceModelInstanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("点位设备模型实例不存在");
        }

        // 删除实例（实例项会通过 ON DELETE CASCADE 自动删除）
        pointDeviceModelInstanceMapper.deleteById(id);
        log.info("点位设备模型实例已删除：{} (ID: {})", instance.getName(), id);
    }

    @Override
    @Transactional
    public PointDeviceModelInstanceDTO updateInstanceItem(Long instanceId, Long itemId, Long componentInstanceId) {
        PointDeviceModelInstance instance = pointDeviceModelInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("点位设备模型实例不存在");
        }

        PointDeviceModelInstanceItem item = pointDeviceModelInstanceItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("实例项不存在");
        }

        if (!item.getInstanceId().equals(instanceId)) {
            throw new BusinessException("实例项不属于该实例");
        }

        if (componentInstanceId == null) {
            throw new BusinessException("零部件实例 ID 不能为空");
        }

        // 更新零部件实例 ID
        item.setComponentInstanceId(componentInstanceId);
        pointDeviceModelInstanceItemMapper.updateById(item);
        log.info("点位设备模型实例项更新成功：实例项 ID {} 关联零部件实例 ID {}", itemId, componentInstanceId);

        return getInstanceDetail(instanceId);
    }
}
