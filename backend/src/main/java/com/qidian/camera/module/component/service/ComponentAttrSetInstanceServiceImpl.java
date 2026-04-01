package com.qidian.camera.module.component.service;

import com.qidian.camera.module.component.service.ComponentAttrSetInstanceService;
import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.module.component.dto.ComponentAttrSetInstanceDTO;
import com.qidian.camera.module.component.entity.ComponentAttrSetInstance;
import com.qidian.camera.module.component.mapper.ComponentAttrSetInstanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 零部件属性集实例服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentAttrSetInstanceServiceImpl implements ComponentAttrSetInstanceService {

    private final ComponentAttrSetInstanceMapper componentAttrSetInstanceMapper;

    /**
     * 实体转 DTO
     */
    private ComponentAttrSetInstanceDTO entityToDTO(ComponentAttrSetInstance entity) {
        if (entity == null) {
            return null;
        }
        ComponentAttrSetInstanceDTO dto = new ComponentAttrSetInstanceDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public List<ComponentAttrSetInstanceDTO> getInstanceList(Long attrSetId) {
        List<ComponentAttrSetInstance> instances = componentAttrSetInstanceMapper.selectListWithDetails(attrSetId);
        return instances.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComponentAttrSetInstanceDTO getInstanceDetail(Long id) {
        ComponentAttrSetInstance instance = componentAttrSetInstanceMapper.selectByIdWithDetails(id);
        if (instance == null) {
            throw new BusinessException("实例不存在");
        }
        return entityToDTO(instance);
    }

    @Override
    @Transactional
    public ComponentAttrSetInstanceDTO createInstance(ComponentAttrSetInstanceDTO dto, Long userId) {
        if (dto.getAttrSetId() == null) {
            throw new BusinessException("属性集 ID 不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException("实例名称不能为空");
        }

        ComponentAttrSetInstance entity = new ComponentAttrSetInstance();
        entity.setAttrSetId(dto.getAttrSetId());
        entity.setName(dto.getName());
        entity.setAttrValues(dto.getAttrValues());
        entity.setCreatedBy(userId);

        componentAttrSetInstanceMapper.insert(entity);
        log.info("属性集实例创建成功：{} by user {}", entity.getName(), userId);

        return getInstanceDetail(entity.getId());
    }

    @Override
    @Transactional
    public ComponentAttrSetInstanceDTO updateInstance(Long id, ComponentAttrSetInstanceDTO dto) {
        ComponentAttrSetInstance existing = componentAttrSetInstanceMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("实例不存在");
        }

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getAttrValues() != null) {
            existing.setAttrValues(dto.getAttrValues());
        }

        componentAttrSetInstanceMapper.updateById(existing);
        log.info("属性集实例更新成功：{}", id);

        return getInstanceDetail(id);
    }

    @Override
    @Transactional
    public void deleteInstance(Long id) {
        ComponentAttrSetInstance instance = componentAttrSetInstanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("实例不存在");
        }

        componentAttrSetInstanceMapper.deleteById(id);
        log.info("属性集实例已删除：{}", id);
    }
}
