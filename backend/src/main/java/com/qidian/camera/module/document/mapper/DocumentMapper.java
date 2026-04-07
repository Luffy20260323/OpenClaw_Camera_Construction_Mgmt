package com.qidian.camera.module.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.document.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文档 Mapper
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 查询文档列表（带上传人信息）
     *
     * @param category 分类
     * @param status   状态
     * @return 文档列表
     */
    @Select("<script>" +
            "SELECT d.*, u.real_name AS uploader_name " +
            "FROM documents d " +
            "LEFT JOIN users u ON d.uploaded_by = u.id " +
            "WHERE d.status = #{status} " +
            "<if test=\"category != null and category != ''\">" +
            "AND d.category = #{category} " +
            "</if>" +
            "ORDER BY d.featured DESC, d.uploaded_at DESC" +
            "</script>")
    List<Document> selectListWithUploader(@Param("category") String category,
                                          @Param("status") String status);

    /**
     * 查询文档详情（带上传人信息）
     *
     * @param id 文档 ID
     * @return 文档信息
     */
    @Select("SELECT d.*, u.real_name AS uploader_name " +
            "FROM documents d " +
            "LEFT JOIN users u ON d.uploaded_by = u.id " +
            "WHERE d.id = #{id}")
    Document selectByIdWithUploader(@Param("id") Long id);

    /**
     * 搜索文档
     *
     * @param keyword 搜索关键词
     * @param status  状态
     * @return 文档列表
     */
    @Select("SELECT d.*, u.real_name AS uploader_name " +
            "FROM documents d " +
            "LEFT JOIN users u ON d.uploaded_by = u.id " +
            "WHERE d.status = #{status} " +
            "AND (d.title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR d.description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR d.tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY d.uploaded_at DESC")
    List<Document> searchDocuments(@Param("keyword") String keyword,
                                   @Param("status") String status);

    /**
     * 获取文档的所有版本
     *
     * @param parentId 父文档 ID
     * @return 版本列表
     */
    @Select("SELECT d.*, u.real_name AS uploader_name " +
            "FROM documents d " +
            "LEFT JOIN users u ON d.uploaded_by = u.id " +
            "WHERE d.parent_id = #{parentId} OR d.id = #{parentId} " +
            "AND d.status = 'ACTIVE' " +
            "ORDER BY d.version DESC")
    List<Document> selectVersions(@Param("parentId") Long parentId);

    /**
     * 增加浏览次数
     *
     * @param id 文档 ID
     */
    @Select("UPDATE documents SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 增加下载次数
     *
     * @param id 文档 ID
     */
    @Select("UPDATE documents SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(@Param("id") Long id);

    /**
     * 将旧版本标记为非最新
     *
     * @param parentId 父文档 ID
     */
    @Select("UPDATE documents SET is_latest = FALSE WHERE parent_id = #{parentId} AND is_latest = TRUE")
    void markOldVersionsNotLatest(@Param("parentId") Long parentId);
}
