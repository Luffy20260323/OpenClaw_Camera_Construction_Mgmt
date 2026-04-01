package com.qidian.camera.module.pointbatchassignment.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 点位信息 DTO（用于列表展示）
 */
@Data
public class PointDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点位 ID
     */
    private Long id;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 点位编码
     */
    private String pointCode;

    /**
     * 所属作业区 ID
     */
    private Long workAreaId;

    /**
     * 所属作业区名称
     */
    private String workAreaName;

    /**
     * 所属公司 ID
     */
    private Long companyId;

    /**
     * 所属公司名称
     */
    private String companyName;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private String status;

    /**
     * 是否已分配
     */
    private Boolean isAssigned;

    /**
     * 已分配的设备模型实例 ID
     */
    private Long assignedModelInstanceId;
}
