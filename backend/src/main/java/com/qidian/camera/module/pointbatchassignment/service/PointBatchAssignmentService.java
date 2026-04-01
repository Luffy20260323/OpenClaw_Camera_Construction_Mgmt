package com.qidian.camera.module.pointbatchassignment.service;

import com.qidian.camera.module.pointbatchassignment.dto.BatchAssignmentRequest;
import com.qidian.camera.module.pointbatchassignment.dto.PointDTO;
import com.qidian.camera.module.pointbatchassignment.dto.PointDeviceAssignmentDTO;

import java.util.List;

/**
 * 点位批量分配服务接口
 */
public interface PointBatchAssignmentService {

    /**
     * 获取未分配的点位列表
     *
     * @return 未分配的点位列表
     */
    List<PointDTO> getAvailablePoints();

    /**
     * 批量分配设备模型实例给点位
     *
     * @param request 批量分配请求
     * @param userId  当前用户 ID
     * @return 分配成功的记录数
     */
    int batchAssign(BatchAssignmentRequest request, Long userId);

    /**
     * 获取点位的设备配置
     *
     * @param pointId 点位 ID
     * @return 设备分配信息
     */
    PointDeviceAssignmentDTO getPointDeviceConfig(Long pointId);
}
