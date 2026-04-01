package com.qidian.camera.module.pointbatchassignment.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点位设备分配 DTO
 */
@Data
public class PointDeviceAssignmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分配记录 ID
     */
    private Long id;

    /**
     * 点位 ID
     */
    private Long pointId;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 点位编码
     */
    private String pointCode;

    /**
     * 设备模型实例 ID
     */
    private Long modelInstanceId;

    /**
     * 分配人 ID
     */
    private Long assignedBy;

    /**
     * 分配人姓名
     */
    private String assignedByName;

    /**
     * 分配时间
     */
    private LocalDateTime assignedAt;
}
