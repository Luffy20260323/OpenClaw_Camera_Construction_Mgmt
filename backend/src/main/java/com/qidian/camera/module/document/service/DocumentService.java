package com.qidian.camera.module.document.service;

import com.qidian.camera.module.document.dto.DocumentDTO;
import com.qidian.camera.module.document.dto.UpdateDocumentRequest;
import com.qidian.camera.module.document.dto.UploadDocumentRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档服务接口
 */
public interface DocumentService {

    /**
     * 获取文档列表
     *
     * @param category 分类
     * @return 文档列表
     */
    List<DocumentDTO> getDocumentList(String category);

    /**
     * 获取文档详情
     *
     * @param id 文档 ID
     * @return 文档详情
     */
    DocumentDTO getDocumentDetail(Long id);

    /**
     * 上传文档
     *
     * @param request 上传请求
     * @param userId  上传人 ID
     * @return 文档 DTO
     */
    DocumentDTO uploadDocument(UploadDocumentRequest request, Long userId);

    /**
     * 更新文档信息
     *
     * @param id      文档 ID
     * @param request 更新请求
     * @return 文档 DTO
     */
    DocumentDTO updateDocument(Long id, UpdateDocumentRequest request);

    /**
     * 删除文档
     *
     * @param id 文档 ID
     */
    void deleteDocument(Long id);

    /**
     * 搜索文档
     *
     * @param keyword 搜索关键词
     * @return 文档列表
     */
    List<DocumentDTO> searchDocuments(String keyword);

    /**
     * 获取文档版本历史
     *
     * @param id 文档 ID
     * @return 版本列表
     */
    List<DocumentDTO> getDocumentVersions(Long id);

    /**
     * 增加浏览次数
     *
     * @param id 文档 ID
     */
    void incrementViewCount(Long id);

    /**
     * 增加下载次数
     *
     * @param id 文档 ID
     */
    void incrementDownloadCount(Long id);

    /**
     * 保存文件到本地
     *
     * @param file      上传的文件
     * @param category  分类
     * @param filename  文件名
     * @return 文件路径
     */
    String saveFileToLocal(MultipartFile file, String category, String filename);
}
