package com.qidian.camera.module.component.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 属性集属性定义 DTO
 */
@Data
public class ComponentAttrSetAttrDTO {

    /**
     * 属性定义 ID
     */
    private Long id;

    /**
     * 关联属性集 ID
     */
    private Long attrSetId;

    /**
     * 属性名称
     */
    private String attrName;

    /**
     * 属性编码
     */
    private String attrCode;

    /**
     * 属性类型：TEXT/NUMBER/DATE/SELECT/MULTI_SELECT/FILE
     */
    private String attrType;

    /**
     * 单位
     */
    private String unit;

    /**
     * 选项值 JSON（用于 SELECT 类型）
     */
    private String options;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 显示序号
     */
    private Integer sequenceNo;

    /**
     * 是否必填
     */
    private Boolean isRequired;

    /**
     * 验证规则
     */
    private String validationRule;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
