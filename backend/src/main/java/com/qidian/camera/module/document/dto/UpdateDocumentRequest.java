package com.qidian.camera.module.document.dto;

import lombok.Data;

/**
 * 更新文档请求
 */
@Data
public class UpdateDocumentRequest {

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档描述
     */
    private String description;

    /**
     * 文档分类
     */
    private String category;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 是否推荐
     */
    private Boolean featured;
}
