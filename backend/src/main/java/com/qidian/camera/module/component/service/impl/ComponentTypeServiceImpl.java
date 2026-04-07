package com.qidian.camera.module.component.service.impl;

import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.module.component.dto.ComponentTypeDTO;
import com.qidian.camera.module.component.entity.ComponentType;
import com.qidian.camera.module.component.mapper.ComponentTypeMapper;
import com.qidian.camera.module.component.service.ComponentTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 零部件种类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentTypeServiceImpl implements ComponentTypeService {

    private final ComponentTypeMapper componentTypeMapper;

    /**
     * 实体转 DTO
     */
    private ComponentTypeDTO entityToDTO(ComponentType entity) {
        if (entity == null) {
            return null;
        }
        ComponentTypeDTO dto = new ComponentTypeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO 转实体
     */
    private ComponentType dtoToEntity(ComponentTypeDTO dto) {
        if (dto == null) {
            return null;
        }
        ComponentType entity = new ComponentType();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    public List<ComponentTypeDTO> getComponentTypeList(Boolean isActive) {
        List<ComponentType> componentTypes = componentTypeMapper.selectListWithCreator(isActive);
        return componentTypes.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComponentTypeDTO getComponentTypeDetail(Long id) {
        ComponentType componentType = componentTypeMapper.selectByIdWithCreator(id);
        if (componentType == null) {
            throw new BusinessException("零部件种类不存在");
        }
        return entityToDTO(componentType);
    }

    @Override
    @Transactional
    public ComponentTypeDTO createComponentType(ComponentTypeDTO dto, Long userId) {
        // 验证编码是否已存在
        ComponentType existing = componentTypeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ComponentType>()
                        .eq(ComponentType::getCode, dto.getCode()));
        if (existing != null) {
            throw new BusinessException("种类编码已存在：" + dto.getCode());
        }

        // 验证名称是否已存在
        ComponentType existingByName = componentTypeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ComponentType>()
                        .eq(ComponentType::getName, dto.getName()));
        if (existingByName != null) {
            throw new BusinessException("种类名称已存在：" + dto.getName());
        }

        ComponentType entity = dtoToEntity(dto);
        if (entity.getSequenceNo() == null) {
            entity.setSequenceNo(10);
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        entity.setCreatedBy(userId);

        componentTypeMapper.insert(entity);
        log.info("零部件种类创建成功：{} ({}) by user {}", entity.getName(), entity.getCode(), userId);

        return entityToDTO(entity);
    }

    @Override
    @Transactional
    public ComponentTypeDTO updateComponentType(Long id, ComponentTypeDTO dto) {
        ComponentType existing = componentTypeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("零部件种类不存在");
        }

        // 如果修改了编码，验证新编码是否已被其他记录使用
        if (dto.getCode() != null && !dto.getCode().equals(existing.getCode())) {
            ComponentType codeConflict = componentTypeMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ComponentType>()
                            .eq(ComponentType::getCode, dto.getCode())
                            .ne(ComponentType::getId, id));
            if (codeConflict != null) {
                throw new BusinessException("种类编码已存在：" + dto.getCode());
            }
        }

        // 如果修改了名称，验证新名称是否已被其他记录使用
        if (dto.getName() != null && !dto.getName().equals(existing.getName())) {
            ComponentType nameConflict = componentTypeMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ComponentType>()
                            .eq(ComponentType::getName, dto.getName())
                            .ne(ComponentType::getId, id));
            if (nameConflict != null) {
                throw new BusinessException("种类名称已存在：" + dto.getName());
            }
        }

        // 更新字段
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getCode() != null) {
            existing.setCode(dto.getCode());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getSequenceNo() != null) {
            existing.setSequenceNo(dto.getSequenceNo());
        }
        if (dto.getIsActive() != null) {
            existing.setIsActive(dto.getIsActive());
        }

        componentTypeMapper.updateById(existing);
        log.info("零部件种类更新成功：{}", id);

        return entityToDTO(existing);
    }

    @Override
    @Transactional
    public void deleteComponentType(Long id) {
        ComponentType componentType = componentTypeMapper.selectById(id);
        if (componentType == null) {
            throw new BusinessException("零部件种类不存在");
        }

        componentTypeMapper.deleteById(id);
        log.info("零部件种类已删除：{}", id);
    }

    @Override
    @Transactional
    public ComponentTypeDTO toggleComponentTypeStatus(Long id) {
        ComponentType componentType = componentTypeMapper.selectById(id);
        if (componentType == null) {
            throw new BusinessException("零部件种类不存在");
        }

        // 切换状态
        Boolean newStatus = !Boolean.TRUE.equals(componentType.getIsActive());
        componentType.setIsActive(newStatus);

        componentTypeMapper.updateById(componentType);
        log.info("零部件种类状态已切换：{} -> isActive={}", id, newStatus);

        return entityToDTO(componentType);
    }
}
