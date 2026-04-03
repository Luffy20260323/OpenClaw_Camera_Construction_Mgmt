package com.qidian.camera.module.resource.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 资源统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceStatsDTO {
    
    private String type;
    private Integer totalCount;
    private Integer activeCount;
    private Integer basicCount;
    private Integer rootCount;
}