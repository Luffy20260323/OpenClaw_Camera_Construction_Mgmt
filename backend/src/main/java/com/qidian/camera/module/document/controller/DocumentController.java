package com.qidian.camera.module.document.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import com.qidian.camera.module.document.dto.DocumentDTO;
import com.qidian.camera.module.document.dto.UpdateDocumentRequest;
import com.qidian.camera.module.document.dto.UploadDocumentRequest;
import com.qidian.camera.module.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档中心控制器
 */
@Tag(name = "文档中心管理", description = "文档上传、下载、分类、版本管理")
@Slf4j
@RestController
@RequestMapping("/system/docs")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "获取文档列表", description = "获取指定分类的文档列表")
    @ApiPermission("system:docs:view")
    @GetMapping
    public Result<List<DocumentDTO>> getDocumentList(
            @RequestParam(required = false) String category) {
        List<DocumentDTO> documents = documentService.getDocumentList(category);
        return Result.success(documents);
    }

    @Operation(summary = "搜索文档", description = "根据关键词搜索文档")
    @ApiPermission("system:docs:view")
    @GetMapping("/search")
    public Result<List<DocumentDTO>> searchDocuments(
            @RequestParam String keyword) {
        List<DocumentDTO> documents = documentService.searchDocuments(keyword);
        return Result.success(documents);
    }

    @Operation(summary = "获取文档详情", description = "获取指定文档的详细信息")
    @ApiPermission("system:docs:view")
    @GetMapping("/{id}")
    public Result<DocumentDTO> getDocumentDetail(@PathVariable Long id) {
        DocumentDTO document = documentService.getDocumentDetail(id);
        // 增加浏览次数
        documentService.incrementViewCount(id);
        return Result.success(document);
    }

    @Operation(summary = "获取文档版本历史", description = "获取文档的所有版本")
    @ApiPermission("system:docs:view")
    @GetMapping("/{id}/versions")
    public Result<List<DocumentDTO>> getDocumentVersions(@PathVariable Long id) {
        List<DocumentDTO> versions = documentService.getDocumentVersions(id);
        return Result.success(versions);
    }

    @Operation(summary = "上传文档", description = "上传新文档")
    @ApiPermission("system:docs:upload")
    @PostMapping
    public Result<DocumentDTO> uploadDocument(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String versionComment,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean featured,
            @RequestAttribute("userId") Long userId) {
        
        UploadDocumentRequest request = new UploadDocumentRequest();
        request.setTitle(title);
        request.setCategory(category);
        request.setDescription(description);
        request.setVersionComment(versionComment);
        request.setTags(tags);
        request.setFeatured(featured);
        request.setFile(file);

        DocumentDTO document = documentService.uploadDocument(request, userId);
        return Result.success(document);
    }

    @Operation(summary = "更新文档信息", description = "更新文档的基本信息")
    @ApiPermission("system:docs:edit")
    @PutMapping("/{id}")
    public Result<DocumentDTO> updateDocument(
            @PathVariable Long id,
            @RequestBody UpdateDocumentRequest request) {
        DocumentDTO document = documentService.updateDocument(id, request);
        return Result.success(document);
    }

    @Operation(summary = "删除文档", description = "删除指定文档")
    @ApiPermission("system:docs:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success(null);
    }

    @Operation(summary = "查看文档", description = "在线查看文档内容")
    @ApiPermission("system:docs:view")
    @GetMapping("/view/{filename}")
    public ResponseEntity<byte[]> viewDocument(@PathVariable String filename) {
        try {
            // 解码文件名
            String decodedFilename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);
            
            // 查找文档
            List<DocumentDTO> documents = documentService.getDocumentList(null);
            DocumentDTO targetDoc = documents.stream()
                    .filter(doc -> doc.getFilename().equals(decodedFilename))
                    .findFirst()
                    .orElse(null);
            
            if (targetDoc == null) {
                return ResponseEntity.notFound().build();
            }

            File file = new File(targetDoc.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] content = Files.readAllBytes(Paths.get(targetDoc.getFilePath()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(getMediaType(targetDoc.getFileType())));
            headers.setContentLength(content.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (IOException e) {
            log.error("查看文档失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "下载文档", description = "下载指定文档")
    @ApiPermission("system:docs:view")
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String filename) {
        try {
            // 解码文件名
            String decodedFilename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);
            
            // 查找文档
            List<DocumentDTO> documents = documentService.getDocumentList(null);
            DocumentDTO targetDoc = documents.stream()
                    .filter(doc -> doc.getFilename().equals(decodedFilename))
                    .findFirst()
                    .orElse(null);
            
            if (targetDoc == null) {
                return ResponseEntity.notFound().build();
            }

            File file = new File(targetDoc.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] content = Files.readAllBytes(Paths.get(targetDoc.getFilePath()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    URLEncoder.encode(targetDoc.getOriginalFilename(), StandardCharsets.UTF_8));
            headers.setContentLength(content.length);
            
            // 增加下载次数
            documentService.incrementDownloadCount(targetDoc.getId());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (IOException e) {
            log.error("下载文档失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "获取 Markdown 内容", description = "获取 Markdown 文档渲染后的 HTML 内容")
    @ApiPermission("system:docs:view")
    @GetMapping("/markdown/{filename}")
    public Result<String> getMarkdownContent(@PathVariable String filename) {
        try {
            // 解码文件名
            String decodedFilename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);
            
            // 查找文档
            List<DocumentDTO> documents = documentService.getDocumentList(null);
            DocumentDTO targetDoc = documents.stream()
                    .filter(doc -> doc.getFilename().equals(decodedFilename) && "md".equals(doc.getFileType()))
                    .findFirst()
                    .orElse(null);
            
            if (targetDoc == null) {
                return Result.error(404, "文档不存在");
            }

            File file = new File(targetDoc.getFilePath());
            if (!file.exists()) {
                return Result.error(404, "文件不存在");
            }

            String content = Files.readString(Paths.get(targetDoc.getFilePath()));
            
            // 简单的 Markdown 转 HTML（生产环境建议使用 proper Markdown 解析器）
            String html = convertMarkdownToHtml(content);
            
            return Result.success(html);
        } catch (IOException e) {
            log.error("获取 Markdown 内容失败", e);
            return Result.error(500, "读取文件失败");
        }
    }

    /**
     * 简单的 Markdown 转 HTML
     */
    private String convertMarkdownToHtml(String markdown) {
        if (markdown == null) {
            return "";
        }
        
        String html = markdown;
        
        // 标题
        html = html.replaceAll("^###### (.*$)", "<h6>$1</h6>");
        html = html.replaceAll("^##### (.*$)", "<h5>$1</h5>");
        html = html.replaceAll("^#### (.*$)", "<h4>$1</h4>");
        html = html.replaceAll("^### (.*$)", "<h3>$1</h3>");
        html = html.replaceAll("^## (.*$)", "<h2>$1</h2>");
        html = html.replaceAll("^# (.*$)", "<h1>$1</h1>");
        
        // 粗体
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
        
        // 斜体
        html = html.replaceAll("\\*(.+?)\\*", "<em>$1</em>");
        
        // 代码块
        html = html.replaceAll("```([\\s\\S]*?)```", "<pre><code>$1</code></pre>");
        
        // 行内代码
        html = html.replaceAll("`(.+?)`", "<code>$1</code>");
        
        // 链接
        html = html.replaceAll("\\[(.+?)\\]\\((.+?)\\)", "<a href=\"$2\">$1</a>");
        
        // 换行
        html = html.replaceAll("\n", "<br>");
        
        return html;
    }

    /**
     * 根据文件类型获取 MIME 类型
     */
    private String getMediaType(String fileType) {
        return switch (fileType != null ? fileType.toLowerCase() : "") {
            case "pdf" -> "application/pdf";
            case "html" -> "text/html";
            case "md" -> "text/markdown";
            case "txt" -> "text/plain";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            default -> "application/octet-stream";
        };
    }
}
