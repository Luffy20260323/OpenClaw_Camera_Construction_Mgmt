package com.qidian.camera.module.pointbatchassignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.common.exception.ErrorCode;
import com.qidian.camera.module.pointbatchassignment.dto.BatchAssignmentRequest;
import com.qidian.camera.module.pointbatchassignment.dto.PointDTO;
import com.qidian.camera.module.pointbatchassignment.dto.PointDeviceAssignmentDTO;
import com.qidian.camera.module.pointbatchassignment.entity.Point;
import com.qidian.camera.module.pointbatchassignment.entity.PointDeviceAssignment;
import com.qidian.camera.module.pointbatchassignment.entity.PointDeviceAssignmentWithPointInfo;
import com.qidian.camera.module.pointbatchassignment.mapper.PointDeviceAssignmentMapper;
import com.qidian.camera.module.pointbatchassignment.service.PointBatchAssignmentService;
import com.qidian.camera.module.workarea.entity.WorkArea;
import com.qidian.camera.module.workarea.mapper.WorkAreaMapper;
import com.qidian.camera.module.company.entity.Company;
import com.qidian.camera.module.company.mapper.CompanyMapper;
import com.qidian.camera.module.user.entity.User;
import com.qidian.camera.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点位批量分配服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointBatchAssignmentServiceImpl implements PointBatchAssignmentService {

    private final PointDeviceAssignmentMapper pointDeviceAssignmentMapper;
    private final WorkAreaMapper workAreaMapper;
    private final CompanyMapper companyMapper;
    private final UserMapper userMapper;

    @Override
    public List<PointDTO> getAvailablePoints() {
        log.info("查询未分配的点位列表");
        
        // 查询未分配的点位
        List<Point> unassignedPoints = pointDeviceAssignmentMapper.selectUnassignedPoints();
        
        // 转换为 DTO
        return unassignedPoints.stream().map(point -> {
            PointDTO dto = new PointDTO();
            dto.setId(point.getId());
            dto.setPointName(point.getPointName());
            dto.setPointCode(point.getPointCode());
            dto.setWorkAreaId(point.getWorkAreaId());
            dto.setCompanyId(point.getCompanyId());
            dto.setDescription(point.getDescription());
            dto.setStatus(point.getStatus());
            dto.setIsAssigned(point.getIsAssigned() != null ? point.getIsAssigned() : false);
            dto.setAssignedModelInstanceId(point.getAssignedModelInstanceId());
            
            // 获取作业区名称
            if (point.getWorkAreaId() != null) {
                WorkArea workArea = workAreaMapper.selectById(point.getWorkAreaId());
                if (workArea != null) {
                    dto.setWorkAreaName(workArea.getWorkAreaName());
                }
            }
            
            // 获取公司名称
            if (point.getCompanyId() != null) {
                Company company = companyMapper.selectById(point.getCompanyId());
                if (company != null) {
                    dto.setCompanyName(company.getCompanyName());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAssign(BatchAssignmentRequest request, Long userId) {
        log.info("批量分配设备模型实例，modelInstanceId={}, pointIds={}, userId={}", 
                request.getModelInstanceId(), request.getPointIds(), userId);
        
        // 参数校验
        if (request.getModelInstanceId() == null || request.getModelInstanceId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "设备模型实例 ID 不能为空");
        }
        
        if (request.getPointIds() == null || request.getPointIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "待分配的点位列表不能为空");
        }
        
        // 检查点位是否已被分配
        List<Long> assignedPointIds = pointDeviceAssignmentMapper.selectAssignedPointIds(request.getPointIds());
        if (!assignedPointIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "以下点位已分配设备：" + assignedPointIds.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }
        
        // 批量创建分配记录
        int successCount = 0;
        LocalDateTime now = LocalDateTime.now();
        
        for (Long pointId : request.getPointIds()) {
            PointDeviceAssignment assignment = new PointDeviceAssignment();
            assignment.setPointId(pointId);
            assignment.setModelInstanceId(request.getModelInstanceId());
            assignment.setAssignedBy(userId);
            assignment.setAssignedAt(now);
            
            try {
                pointDeviceAssignmentMapper.insert(assignment);
                successCount++;
                log.info("点位分配成功：pointId={}, modelInstanceId={}", pointId, request.getModelInstanceId());
            } catch (Exception e) {
                log.error("点位分配失败：pointId={}, error={}", pointId, e.getMessage());
                // 继续处理其他点位
            }
        }
        
        log.info("批量分配完成：成功{}条，失败{}条", successCount, request.getPointIds().size() - successCount);
        
        if (successCount == 0) {
            throw new BusinessException(ErrorCode.ERROR, "点位分配失败，请重试");
        }
        
        return successCount;
    }

    @Override
    public PointDeviceAssignmentDTO getPointDeviceConfig(Long pointId) {
        log.info("查询点位设备配置，pointId={}", pointId);
        
        PointDeviceAssignmentWithPointInfo assignment = pointDeviceAssignmentMapper.selectByPointIdWithPointInfo(pointId);
        
        if (assignment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "该点位未分配设备");
        }
        
        PointDeviceAssignmentDTO dto = new PointDeviceAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setPointId(assignment.getPointId());
        dto.setPointName(assignment.getPointName());
        dto.setPointCode(assignment.getPointCode());
        dto.setModelInstanceId(assignment.getModelInstanceId());
        dto.setAssignedBy(assignment.getAssignedBy());
        dto.setAssignedAt(assignment.getAssignedAt());
        
        // 获取分配人姓名
        if (assignment.getAssignedBy() != null) {
            User user = userMapper.selectById(assignment.getAssignedBy());
            if (user != null) {
                dto.setAssignedByName(user.getRealName());
            }
        }
        
        return dto;
    }
}
