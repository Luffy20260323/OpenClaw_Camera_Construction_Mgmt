package com.qidian.camera.module.pointbatchassignment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点位实体
 */
@Data
@TableName("points")
public class Point implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点位 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 所属公司 ID
     */
    private Long companyId;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：active/inactive
     */
    private String status;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 是否已分配设备（非数据库字段，用于查询）
     */
    @TableField(exist = false)
    private Boolean isAssigned;

    /**
     * 已分配的设备模型实例 ID（非数据库字段，用于查询）
     */
    @TableField(exist = false)
    private Long assignedModelInstanceId;
}
