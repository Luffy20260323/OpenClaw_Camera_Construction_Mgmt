package com.qidian.camera.module.pointbatchassignment.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量分配请求 DTO
 */
@Data
public class BatchAssignmentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备模型实例 ID
     */
    private Long modelInstanceId;

    /**
     * 待分配的点位 ID 列表
     */
    private List<Long> pointIds;
}
