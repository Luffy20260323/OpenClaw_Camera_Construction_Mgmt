package com.qidian.camera.module.pointdevicemodel.service.impl;

import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.module.pointdevicemodel.dto.PointDeviceModelDTO;
import com.qidian.camera.module.pointdevicemodel.dto.PointDeviceModelItemDTO;
import com.qidian.camera.module.pointdevicemodel.entity.PointDeviceModel;
import com.qidian.camera.module.pointdevicemodel.entity.PointDeviceModelItem;
import com.qidian.camera.module.pointdevicemodel.mapper.PointDeviceModelItemMapper;
import com.qidian.camera.module.pointdevicemodel.mapper.PointDeviceModelMapper;
import com.qidian.camera.module.pointdevicemodel.service.PointDeviceModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 点位设备模型服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointDeviceModelServiceImpl implements PointDeviceModelService {

    private final PointDeviceModelMapper pointDeviceModelMapper;
    private final PointDeviceModelItemMapper pointDeviceModelItemMapper;

    /**
     * 实体转 DTO
     */
    private PointDeviceModelDTO entityToDTO(PointDeviceModel entity) {
        if (entity == null) {
            return null;
        }
        PointDeviceModelDTO dto = new PointDeviceModelDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 模型项实体转 DTO
     */
    private PointDeviceModelItemDTO itemEntityToDTO(PointDeviceModelItem entity) {
        if (entity == null) {
            return null;
        }
        PointDeviceModelItemDTO dto = new PointDeviceModelItemDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO 转实体
     */
    private PointDeviceModel dtoToEntity(PointDeviceModelDTO dto) {
        if (dto == null) {
            return null;
        }
        PointDeviceModel entity = new PointDeviceModel();
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "updatedAt", "creatorName", "items");
        return entity;
    }

    /**
     * 模型项 DTO 转实体
     */
    private PointDeviceModelItem itemDTOToEntity(PointDeviceModelItemDTO dto) {
        if (dto == null) {
            return null;
        }
        PointDeviceModelItem entity = new PointDeviceModelItem();
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "updatedAt", "componentTypeName", "componentTypeCode");
        return entity;
    }

    @Override
    public List<PointDeviceModelDTO> getModelList(Boolean isActive) {
        List<PointDeviceModel> models = pointDeviceModelMapper.selectListWithCreator(isActive);
        return models.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PointDeviceModelDTO getModelDetail(Long id) {
        PointDeviceModel model = pointDeviceModelMapper.selectByIdWithCreator(id);
        if (model == null) {
            throw new BusinessException("点位设备模型不存在");
        }

        PointDeviceModelDTO dto = entityToDTO(model);

        // 查询模型项列表
        List<PointDeviceModelItem> items = pointDeviceModelItemMapper.selectByModelIdWithComponent(id);
        List<PointDeviceModelItemDTO> itemDTOs = items.stream()
                .map(this::itemEntityToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    @Override
    @Transactional
    public PointDeviceModelDTO createModel(PointDeviceModelDTO dto, Long userId) {
        // 检查编码是否已存在
        PointDeviceModel existing = pointDeviceModelMapper.selectByCode(dto.getCode());
        if (existing != null) {
            throw new BusinessException("模型编码已存在：" + dto.getCode());
        }

        // 检查必填字段
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException("模型名称不能为空");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new BusinessException("模型编码不能为空");
        }

        // 创建模型
        PointDeviceModel entity = dtoToEntity(dto);
        entity.setCreatedBy(userId);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        pointDeviceModelMapper.insert(entity);
        log.info("点位设备模型创建成功：{} (ID: {}) by user {}", entity.getName(), entity.getId(), userId);

        // 创建模型项
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PointDeviceModelItemDTO itemDTO : dto.getItems()) {
                if (itemDTO.getComponentTypeId() == null) {
                    throw new BusinessException("模型项的零部件种类 ID 不能为空");
                }
                if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                    throw new BusinessException("模型项的数量必须大于 0");
                }

                PointDeviceModelItem itemEntity = itemDTOToEntity(itemDTO);
                itemEntity.setModelId(entity.getId());
                if (itemEntity.getSequenceNo() == null) {
                    itemEntity.setSequenceNo(10);
                }

                pointDeviceModelItemMapper.insert(itemEntity);
            }
            log.info("点位设备模型项创建成功：{} 项", dto.getItems().size());
        }

        return entityToDTO(entity);
    }

    @Override
    @Transactional
    public PointDeviceModelDTO updateModel(Long id, PointDeviceModelDTO dto) {
        PointDeviceModel existing = pointDeviceModelMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("点位设备模型不存在");
        }

        // 如果更新了编码，检查新编码是否已被其他记录使用
        if (dto.getCode() != null && !dto.getCode().equals(existing.getCode())) {
            PointDeviceModel codeExists = pointDeviceModelMapper.selectByCode(dto.getCode());
            if (codeExists != null && !codeExists.getId().equals(id)) {
                throw new BusinessException("模型编码已存在：" + dto.getCode());
            }
        }

        // 更新模型字段
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

        pointDeviceModelMapper.updateById(existing);
        log.info("点位设备模型更新成功：{} (ID: {})", existing.getName(), existing.getId());

        // 如果提供了模型项，则更新模型项（全量替换）
        if (dto.getItems() != null) {
            // 删除原有模型项
            pointDeviceModelItemMapper.deleteByModelId(id);

            // 插入新模型项
            for (PointDeviceModelItemDTO itemDTO : dto.getItems()) {
                if (itemDTO.getComponentTypeId() == null) {
                    throw new BusinessException("模型项的零部件种类 ID 不能为空");
                }
                if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                    throw new BusinessException("模型项的数量必须大于 0");
                }

                PointDeviceModelItem itemEntity = itemDTOToEntity(itemDTO);
                itemEntity.setModelId(id);
                if (itemEntity.getSequenceNo() == null) {
                    itemEntity.setSequenceNo(10);
                }

                pointDeviceModelItemMapper.insert(itemEntity);
            }
            log.info("点位设备模型项更新成功：{} 项", dto.getItems().size());
        }

        return entityToDTO(existing);
    }

    @Override
    @Transactional
    public void deleteModel(Long id) {
        PointDeviceModel model = pointDeviceModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException("点位设备模型不存在");
        }

        // 删除模型（模型项会通过 ON DELETE CASCADE 自动删除）
        pointDeviceModelMapper.deleteById(id);
        log.info("点位设备模型已删除：{} (ID: {})", model.getName(), id);
    }
}
