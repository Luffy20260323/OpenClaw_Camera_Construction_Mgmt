package com.qidian.camera.module.document.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档 DTO
 */
@Data
public class DocumentDTO {

    /**
     * 文档 ID
     */
    private Long id;

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
     * 分类标签文字
     */
    private String categoryLabel;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 版本说明
     */
    private String versionComment;

    /**
     * 是否最新版本
     */
    private Boolean isLatest;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 是否推荐
     */
    private Boolean featured;

    /**
     * 状态
     */
    private String status;

    /**
     * 标签
     */
    private String tags;

    /**
     * 上传人 ID
     */
    private Long uploadedBy;

    /**
     * 上传人姓名
     */
    private String uploaderName;

    /**
     * 上传时间
     */
    private LocalDateTime uploadedAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
