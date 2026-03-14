package com.qidian.camera.module.workarea.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作业区实体
 */
@Data
@TableName("work_areas")
public class WorkArea implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作业区 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业区名称
     */
    private String workAreaName;

    /**
     * 作业区编码
     */
    private String workAreaCode;

    /**
     * 公司 ID
     */
    private Long companyId;

    /**
     * 负责人姓名
     */
    private String leaderName;

    /**
     * 负责人电话
     */
    private String leaderPhone;

    /**
     * 地理范围
     */
    private String geographicRange;

    /**
     * 最大容量
     */
    private Integer maxCapacity;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否系统保护
     */
    private Boolean isSystemProtected;

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
}
