package com.qidian.camera.module.document.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文档请求
 */
@Data
public class UploadDocumentRequest {

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
     * 版本说明
     */
    private String versionComment;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 是否推荐
     */
    private Boolean featured;

    /**
     * 上传的文件
     */
    private MultipartFile file;
}
