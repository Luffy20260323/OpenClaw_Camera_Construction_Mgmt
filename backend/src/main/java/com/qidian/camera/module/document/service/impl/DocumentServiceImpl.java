package com.qidian.camera.module.document.service.impl;

import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.module.document.dto.DocumentDTO;
import com.qidian.camera.module.document.dto.UpdateDocumentRequest;
import com.qidian.camera.module.document.dto.UploadDocumentRequest;
import com.qidian.camera.module.document.entity.Document;
import com.qidian.camera.module.document.mapper.DocumentMapper;
import com.qidian.camera.module.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文档服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentMapper documentMapper;

    @Value("${document.upload.path:./uploads/documents}")
    private String uploadPath;

    /**
     * 获取分类标签文字
     */
    private String getCategoryLabel(String category) {
        return switch (category) {
            case "design" -> "设计文档";
            case "implementation" -> "实施报告";
            case "manual" -> "用户手册";
            case "technical" -> "技术文档";
            case "report" -> "测试报告";
            default -> category;
        };
    }

    /**
     * 实体转 DTO
     */
    private DocumentDTO entityToDTO(Document entity) {
        if (entity == null) {
            return null;
        }
        DocumentDTO dto = new DocumentDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setCategoryLabel(getCategoryLabel(entity.getCategory()));
        return dto;
    }

    @Override
    public List<DocumentDTO> getDocumentList(String category) {
        List<Document> documents = documentMapper.selectListWithUploader(category, "ACTIVE");
        return documents.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDTO getDocumentDetail(Long id) {
        Document document = documentMapper.selectByIdWithUploader(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        return entityToDTO(document);
    }

    @Override
    @Transactional
    public DocumentDTO uploadDocument(UploadDocumentRequest request, Long userId) {
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        MultipartFile file = request.getFile();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                : "";

        // 验证文件类型
        List<String> allowedTypes = List.of(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".md", ".html", ".txt");
        if (!allowedTypes.contains(fileExtension)) {
            throw new BusinessException("不支持的文件类型，仅支持：" + String.join(", ", allowedTypes));
        }

        String fileType = fileExtension.replace(".", "");
        String filename = UUID.randomUUID().toString() + fileExtension;

        // 保存文件
        String filePath = saveFileToLocal(file, request.getCategory(), filename);

        // 创建文档记录
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setCategory(request.getCategory());
        document.setFilename(filename);
        document.setOriginalFilename(originalFilename);
        document.setFilePath(filePath);
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setVersion(1);
        document.setVersionComment(request.getVersionComment());
        document.setParentId(null); // 新版本时设置
        document.setIsLatest(true);
        document.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);
        document.setStatus("ACTIVE");
        document.setTags(request.getTags());
        document.setUploadedBy(userId);
        document.setDownloadCount(0);
        document.setViewCount(0);

        documentMapper.insert(document);
        log.info("文档上传成功：{} by user {}", document.getTitle(), userId);

        return entityToDTO(document);
    }

    @Override
    @Transactional
    public DocumentDTO updateDocument(Long id, UpdateDocumentRequest request) {
        Document existing = documentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("文档不存在");
        }

        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            existing.setCategory(request.getCategory());
        }
        if (request.getTags() != null) {
            existing.setTags(request.getTags());
        }
        if (request.getFeatured() != null) {
            existing.setFeatured(request.getFeatured());
        }

        documentMapper.updateById(existing);
        log.info("文档信息更新成功：{}", id);

        return entityToDTO(existing);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 软删除：标记为 DELETED
        document.setStatus("DELETED");
        documentMapper.updateById(document);

        // 删除文件
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("文档文件已删除：{}", document.getFilePath());
        } catch (IOException e) {
            log.warn("删除文档文件失败：{}", document.getFilePath(), e);
        }

        log.info("文档已删除：{}", id);
    }

    @Override
    public List<DocumentDTO> searchDocuments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getDocumentList(null);
        }
        List<Document> documents = documentMapper.searchDocuments(keyword.trim(), "ACTIVE");
        return documents.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDTO> getDocumentVersions(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        Long parentId = document.getParentId() != null ? document.getParentId() : id;
        List<Document> versions = documentMapper.selectVersions(parentId);
        return versions.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        documentMapper.incrementViewCount(id);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long id) {
        documentMapper.incrementDownloadCount(id);
    }

    @Override
    public String saveFileToLocal(MultipartFile file, String category, String filename) {
        try {
            // 创建目录
            String categoryPath = Paths.get(uploadPath, category).toString();
            File dir = new File(categoryPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(categoryPath, filename);
            file.transferTo(filePath.toFile());

            log.info("文件保存成功：{}", filePath.toString());
            return filePath.toString();
        } catch (IOException e) {
            log.error("保存文件失败", e);
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }
    }
}
