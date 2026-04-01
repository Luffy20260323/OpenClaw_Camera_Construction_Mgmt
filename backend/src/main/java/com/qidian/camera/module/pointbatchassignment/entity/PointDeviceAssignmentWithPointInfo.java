package com.qidian.camera.module.pointbatchassignment.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点位设备分配记录（含点位信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PointDeviceAssignmentWithPointInfo extends PointDeviceAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 点位编码
     */
    private String pointCode;
}
