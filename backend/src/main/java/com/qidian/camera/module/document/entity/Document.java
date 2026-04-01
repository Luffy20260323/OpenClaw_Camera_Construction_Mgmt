package com.qidian.camera.module.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档实体
 */
@Data
@TableName("documents")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 文档分类：design-设计文档，implementation-实施报告，manual-用户手册，technical-技术文档，report-测试报告
     */
    private String category;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件类型：pdf, doc, docx, xls, xlsx, md, html, txt
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
     * 父文档 ID（用于版本关联）
     */
    private Long parentId;

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
     * 状态：ACTIVE-活跃，ARCHIVED-归档，DELETED-已删除
     */
    private String status;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 上传人 ID
     */
    private Long uploadedBy;

    /**
     * 上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadedAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 上传人信息（非数据库字段）
     */
    @TableField(exist = false)
    private String uploaderName;
}
