package com.qidian.camera.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.auth.entity.PageElement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 页面元素 Mapper
 */
@Mapper
public interface PageElementMapper extends BaseMapper<PageElement> {
    
    /**
     * 查询页面的所有元素
     */
    List<PageElement> selectByPageId(@Param("pageId") Long pageId);
}
