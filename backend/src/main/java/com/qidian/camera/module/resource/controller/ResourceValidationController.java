package com.qidian.camera.module.resource.controller;

import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.auth.entity.Resource;
import com.qidian.camera.module.auth.mapper.ResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源验证控制器
 * 提供孤儿资源检测、循环引用检测等功能
 */
@Tag(name = "资源验证", description = "资源数据完整性检查")
@RestController
@RequestMapping("/resource/validation")
@RequiredArgsConstructor
@Slf4j
public class ResourceValidationController {
    
    private final ResourceMapper resourceMapper;
    
    @Operation(summary = "检测孤儿资源", description = "查找父节点不存在的资源")
    @GetMapping("/orphaned")
    public Result<Map<String, Object>> findOrphanedResources() {
        try {
            // 获取所有资源
            List<Resource> allResources = resourceMapper.selectList(null);
            
            // 建立 ID 到资源的映射
            Map<Long, Resource> resourceMap = allResources.stream()
                .collect(Collectors.toMap(Resource::getId, r -> r));
            
            // 查找孤儿资源（有 parentId 但父节点不存在）
            List<Resource> orphaned = allResources.stream()
                .filter(r -> r.getParentId() != null && !resourceMap.containsKey(r.getParentId()))
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", orphaned.size());
            result.put("resources", orphaned);
            
            if (orphaned.isEmpty()) {
                result.put("message", "未发现孤儿资源");
            } else {
                result.put("message", "发现 " + orphaned.size() + " 个孤儿资源");
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检测孤儿资源失败", e);
            return Result.error(500, "检测失败：" + e.getMessage());
        }
    }
    
    @Operation(summary = "检测循环引用", description = "查找形成循环依赖的资源")
    @GetMapping("/circular")
    public Result<Map<String, Object>> findCircularReferences() {
        try {
            // 获取所有资源
            List<Resource> allResources = resourceMapper.selectList(null);
            
            // 建立 ID 到资源的映射
            Map<Long, Resource> resourceMap = allResources.stream()
                .collect(Collectors.toMap(Resource::getId, r -> r));
            
            List<Map<String, Object>> circularRefs = new ArrayList<>();
            Set<Long> visited = new HashSet<>();
            
            // 检测每个资源
            for (Resource resource : allResources) {
                if (visited.contains(resource.getId())) {
                    continue;
                }
                
                List<Long> path = new ArrayList<>();
                if (hasCycle(resource.getId(), resourceMap, visited, path)) {
                    // 找到循环引用
                    Map<String, Object> cycleInfo = new HashMap<>();
                    cycleInfo.put("resource", resource);
                    cycleInfo.put("path", path.stream()
                        .map(id -> resourceMap.get(id))
                        .filter(Objects::nonNull)
                        .map(r -> r.getName() + " (" + r.getId() + ")")
                        .collect(Collectors.toList()));
                    circularRefs.add(cycleInfo);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", circularRefs.size());
            result.put("references", circularRefs);
            
            if (circularRefs.isEmpty()) {
                result.put("message", "未发现循环引用");
            } else {
                result.put("message", "发现 " + circularRefs.size() + " 个循环引用");
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检测循环引用失败", e);
            return Result.error(500, "检测失败：" + e.getMessage());
        }
    }
    
    /**
     * 检测是否存在循环引用
     */
    private boolean hasCycle(Long resourceId, Map<Long, Resource> resourceMap, 
                            Set<Long> visited, List<Long> path) {
        if (path.contains(resourceId)) {
            return true; // 找到循环
        }
        
        if (visited.contains(resourceId)) {
            return false; // 已访问过，无循环
        }
        
        visited.add(resourceId);
        path.add(resourceId);
        
        Resource resource = resourceMap.get(resourceId);
        if (resource != null && resource.getParentId() != null) {
            if (hasCycle(resource.getParentId(), resourceMap, visited, path)) {
                return true;
            }
        }
        
        path.remove(path.size() - 1);
        return false;
    }
    
    @Operation(summary = "资源完整性检查", description = "执行所有检查并返回报告")
    @GetMapping("/check")
    public Result<Map<String, Object>> runAllChecks() {
        try {
            Map<String, Object> report = new HashMap<>();
            
            // 孤儿资源检查
            Result<Map<String, Object>> orphanedResult = findOrphanedResources();
            report.put("orphaned", orphanedResult.getData());
            
            // 循环引用检查
            Result<Map<String, Object>> circularResult = findCircularReferences();
            report.put("circular", circularResult.getData());
            
            // 总体评估
            Map<String, Object> orphanedData = (Map<String, Object>) orphanedResult.getData();
            Map<String, Object> circularData = (Map<String, Object>) circularResult.getData();
            
            int orphanedCount = (int) orphanedData.get("count");
            int circularCount = (int) circularData.get("count");
            
            if (orphanedCount == 0 && circularCount == 0) {
                report.put("status", "healthy");
                report.put("message", "资源数据完整性良好");
            } else {
                report.put("status", "issues_found");
                report.put("message", "发现 " + (orphanedCount + circularCount) + " 个问题");
            }
            
            return Result.success(report);
        } catch (Exception e) {
            log.error("资源完整性检查失败", e);
            return Result.error(500, "检查失败：" + e.getMessage());
        }
    }
}
