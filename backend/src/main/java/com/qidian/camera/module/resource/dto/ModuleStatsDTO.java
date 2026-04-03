package com.qidian.camera.module.resource.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 模块资源统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleStatsDTO {
    
    private String moduleCode;
    private Integer totalCount;
    private Integer moduleCount;
    private Integer menuCount;
    private Integer pageCount;
    private Integer elementCount;
    private Integer apiCount;
    private Integer permissionCount;
    
    /**
     * 模块下的资源列表
     */
    private List<ResourceTreeDTO> resources;
}