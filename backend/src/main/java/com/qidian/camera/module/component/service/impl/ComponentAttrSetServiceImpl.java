package com.qidian.camera.module.component.service.impl;

import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.module.component.dto.ComponentAttrSetAttrDTO;
import com.qidian.camera.module.component.dto.ComponentAttrSetDTO;
import com.qidian.camera.module.component.entity.ComponentAttrSet;
import com.qidian.camera.module.component.entity.ComponentAttrSetAttr;
import com.qidian.camera.module.component.mapper.ComponentAttrSetAttrMapper;
import com.qidian.camera.module.component.mapper.ComponentAttrSetMapper;
import com.qidian.camera.module.component.service.ComponentAttrSetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 零部件属性集服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentAttrSetServiceImpl implements ComponentAttrSetService {

    private final ComponentAttrSetMapper componentAttrSetMapper;
    private final ComponentAttrSetAttrMapper componentAttrSetAttrMapper;

    /**
     * 实体转 DTO
     */
    private ComponentAttrSetDTO entityToDTO(ComponentAttrSet entity) {
        if (entity == null) {
            return null;
        }
        ComponentAttrSetDTO dto = new ComponentAttrSetDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 属性定义实体转 DTO
     */
    private ComponentAttrSetAttrDTO attrEntityToDTO(ComponentAttrSetAttr entity) {
        if (entity == null) {
            return null;
        }
        ComponentAttrSetAttrDTO dto = new ComponentAttrSetAttrDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * DTO 转实体
     */
    private ComponentAttrSet dtoToEntity(ComponentAttrSetDTO dto) {
        if (dto == null) {
            return null;
        }
        ComponentAttrSet entity = new ComponentAttrSet();
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "updatedAt", "componentTypeName", "creatorName", "attributes");
        return entity;
    }

    @Override
    public List<ComponentAttrSetDTO> getAttrSetList(Long componentTypeId, Boolean isActive) {
        List<ComponentAttrSet> attrSets = componentAttrSetMapper.selectListWithDetails(componentTypeId, isActive);
        return attrSets.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComponentAttrSetDTO getAttrSetDetail(Long id) {
        ComponentAttrSet attrSet = componentAttrSetMapper.selectByIdWithDetails(id);
        if (attrSet == null) {
            throw new BusinessException("属性集不存在");
        }

        ComponentAttrSetDTO dto = entityToDTO(attrSet);

        // 查询属性定义列表
        List<ComponentAttrSetAttr> attrs = componentAttrSetAttrMapper.selectByAttrSetId(id);
        List<ComponentAttrSetAttrDTO> attrDTOs = attrs.stream()
                .map(this::attrEntityToDTO)
                .collect(Collectors.toList());
        dto.setAttributes(attrDTOs);

        return dto;
    }

    @Override
    @Transactional
    public ComponentAttrSetDTO createAttrSet(ComponentAttrSetDTO dto, Long userId) {
        // 检查编码是否已存在
        ComponentAttrSet existing = componentAttrSetMapper.selectByCode(dto.getCode());
        if (existing != null) {
            throw new BusinessException("属性集编码已存在：" + dto.getCode());
        }

        // 检查必填字段
        if (dto.getComponentTypeId() == null) {
            throw new BusinessException("零部件种类 ID 不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException("属性集名称不能为空");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new BusinessException("属性集编码不能为空");
        }

        ComponentAttrSet entity = dtoToEntity(dto);
        entity.setCreatedBy(userId);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        entity.setSequenceNo(dto.getSequenceNo() != null ? dto.getSequenceNo() : 10);

        componentAttrSetMapper.insert(entity);
        log.info("属性集创建成功：{} (ID: {}) by user {}", entity.getName(), entity.getId(), userId);

        return entityToDTO(entity);
    }

    @Override
    @Transactional
    public ComponentAttrSetDTO updateAttrSet(Long id, ComponentAttrSetDTO dto) {
        ComponentAttrSet existing = componentAttrSetMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("属性集不存在");
        }

        // 如果更新了编码，检查新编码是否已被其他记录使用
        if (dto.getCode() != null && !dto.getCode().equals(existing.getCode())) {
            ComponentAttrSet codeExists = componentAttrSetMapper.selectByCode(dto.getCode());
            if (codeExists != null && !codeExists.getId().equals(id)) {
                throw new BusinessException("属性集编码已存在：" + dto.getCode());
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
        if (dto.getComponentTypeId() != null) {
            existing.setComponentTypeId(dto.getComponentTypeId());
        }
        if (dto.getSequenceNo() != null) {
            existing.setSequenceNo(dto.getSequenceNo());
        }
        if (dto.getIsActive() != null) {
            existing.setIsActive(dto.getIsActive());
        }

        componentAttrSetMapper.updateById(existing);
        log.info("属性集更新成功：{} (ID: {})", existing.getName(), existing.getId());

        return entityToDTO(existing);
    }

    @Override
    @Transactional
    public void deleteAttrSet(Long id) {
        ComponentAttrSet attrSet = componentAttrSetMapper.selectById(id);
        if (attrSet == null) {
            throw new BusinessException("属性集不存在");
        }

        componentAttrSetMapper.deleteById(id);
        log.info("属性集已删除：{} (ID: {})", attrSet.getName(), id);
    }

    @Override
    @Transactional
    public ComponentAttrSetDTO updateAttrSetSequence(Long id, Integer sequenceNo) {
        ComponentAttrSet attrSet = componentAttrSetMapper.selectById(id);
        if (attrSet == null) {
            throw new BusinessException("属性集不存在");
        }

        if (sequenceNo == null) {
            throw new BusinessException("序号不能为空");
        }

        attrSet.setSequenceNo(sequenceNo);
        componentAttrSetMapper.updateById(attrSet);
        log.info("属性集序号已更新：{} -> sequenceNo={}", id, sequenceNo);

        return entityToDTO(attrSet);
    }
}
