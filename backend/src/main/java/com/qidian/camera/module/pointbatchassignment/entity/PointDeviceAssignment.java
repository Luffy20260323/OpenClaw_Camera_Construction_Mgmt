package com.qidian.camera.module.pointbatchassignment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点位设备分配记录实体
 */
@Data
@TableName("point_device_assignments")
public class PointDeviceAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分配记录 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 点位 ID
     */
    private Long pointId;

    /**
     * 设备模型实例 ID
     */
    private Long modelInstanceId;

    /**
     * 分配人
     */
    private Long assignedBy;

    /**
     * 分配时间
     */
    private LocalDateTime assignedAt;
}
