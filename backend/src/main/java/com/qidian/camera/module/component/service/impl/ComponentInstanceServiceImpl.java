package com.qidian.camera.module.component.service.impl;

import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.module.component.dto.ComponentInstanceDTO;
import com.qidian.camera.module.component.entity.ComponentInstance;
import com.qidian.camera.module.component.mapper.ComponentInstanceMapper;
import com.qidian.camera.module.component.service.ComponentInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 零部件实例服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentInstanceServiceImpl implements ComponentInstanceService {

    private final ComponentInstanceMapper componentInstanceMapper;

    /**
     * 有效的状态列表
     */
    private static final List<String> VALID_STATUSES = Arrays.asList("normal", "replaced", "scrapped");

    /**
     * 实体转 DTO
     */
    private ComponentInstanceDTO entityToDTO(ComponentInstance entity) {
        if (entity == null) {
            return null;
        }
        ComponentInstanceDTO dto = new ComponentInstanceDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 设置状态标签
        if (entity.getStatus() != null) {
            dto.setStatusLabel(getStatusLabel(entity.getStatus()));
        }
        
        return dto;
    }

    /**
     * 获取状态标签
     */
    private String getStatusLabel(String status) {
        return switch (status) {
            case "normal" -> "正常使用";
            case "replaced" -> "已更换";
            case "scrapped" -> "已报废";
            default -> status;
        };
    }

    /**
     * 填充 DTO 中的关联信息
     */
    private void fillRelatedInfo(ComponentInstanceDTO dto, ComponentInstance entity) {
        if (entity == null) {
            return;
        }
        // 从 entity 中获取关联信息（通过 Mapper 查询时已填充）
        // 这里可以根据需要进一步处理
    }

    @Override
    public List<ComponentInstanceDTO> getInstanceList(Long componentTypeId, String status) {
        List<ComponentInstance> instances = componentInstanceMapper.selectListWithDetails(componentTypeId, status);
        return instances.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComponentInstanceDTO getInstanceDetail(Long id) {
        ComponentInstance instance = componentInstanceMapper.selectByIdWithDetails(id);
        if (instance == null) {
            throw new BusinessException("零部件实例不存在");
        }
        ComponentInstanceDTO dto = entityToDTO(instance);
        fillRelatedInfo(dto, instance);
        return dto;
    }

    @Override
    @Transactional
    public ComponentInstanceDTO createInstance(ComponentInstanceDTO dto, Long userId) {
        // 参数校验
        if (dto.getComponentTypeId() == null) {
            throw new BusinessException("零部件种类 ID 不能为空");
        }
        if (!StringUtils.hasText(dto.getSerialNumber())) {
            throw new BusinessException("序列号不能为空");
        }

        // 检查序列号是否已存在
        // 这里可以添加序列号唯一性检查逻辑

        ComponentInstance entity = new ComponentInstance();
        entity.setComponentTypeId(dto.getComponentTypeId());
        entity.setAttrSetInstanceId(dto.getAttrSetInstanceId());
        entity.setSerialNumber(dto.getSerialNumber());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "normal");
        entity.setCreatedBy(userId);

        componentInstanceMapper.insert(entity);
        log.info("零部件实例创建成功：{} (序列号：{}) by user {}", entity.getId(), entity.getSerialNumber(), userId);

        return getInstanceDetail(entity.getId());
    }

    @Override
    @Transactional
    public ComponentInstanceDTO updateInstance(Long id, ComponentInstanceDTO dto) {
        ComponentInstance existing = componentInstanceMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("零部件实例不存在");
        }

        // 更新字段
        if (dto.getComponentTypeId() != null) {
            existing.setComponentTypeId(dto.getComponentTypeId());
        }
        if (dto.getAttrSetInstanceId() != null) {
            existing.setAttrSetInstanceId(dto.getAttrSetInstanceId());
        }
        if (StringUtils.hasText(dto.getSerialNumber())) {
            existing.setSerialNumber(dto.getSerialNumber());
        }
        if (StringUtils.hasText(dto.getStatus()) && VALID_STATUSES.contains(dto.getStatus())) {
            existing.setStatus(dto.getStatus());
        }

        componentInstanceMapper.updateById(existing);
        log.info("零部件实例更新成功：{}", id);

        return getInstanceDetail(id);
    }

    @Override
    @Transactional
    public void deleteInstance(Long id) {
        ComponentInstance instance = componentInstanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("零部件实例不存在");
        }

        componentInstanceMapper.deleteById(id);
        log.info("零部件实例已删除：{}", id);
    }

    @Override
    @Transactional
    public ComponentInstanceDTO updateStatus(Long id, String status) {
        if (!StringUtils.hasText(status) || !VALID_STATUSES.contains(status)) {
            throw new BusinessException("无效的状态值，有效值为：" + String.join(", ", VALID_STATUSES));
        }

        ComponentInstance instance = componentInstanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("零部件实例不存在");
        }

        instance.setStatus(status);
        componentInstanceMapper.updateById(instance);
        log.info("零部件实例状态已更新：{} -> {}", id, status);

        return getInstanceDetail(id);
    }
}
