package com.qidian.camera.module.workarea.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.workarea.dto.WorkAreaDTO;
import com.qidian.camera.module.workarea.dto.CreateWorkAreaRequest;
import com.qidian.camera.module.workarea.dto.UpdateWorkAreaRequest;
import com.qidian.camera.module.workarea.service.WorkAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 作业区控制器
 */
@Tag(name = "作业区管理", description = "作业区信息管理（仅系统管理员）")
@RestController
@RequestMapping("/workarea")
@RequiredArgsConstructor
public class WorkAreaController {

    private final WorkAreaService workAreaService;

    @Operation(summary = "分页查询作业区列表", description = "支持按公司、关键词筛选")
    @GetMapping
    public Result<Page<WorkAreaDTO>> getWorkAreas(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String keyword) {
        Page<WorkAreaDTO> page = workAreaService.getWorkAreas(pageNum, pageSize, companyId, keyword);
        return Result.success(page);
    }

    @Operation(summary = "获取作业区详情", description = "根据 ID 获取作业区详细信息")
    @GetMapping("/{id}")
    public Result<WorkAreaDTO> getWorkArea(@PathVariable Long id) {
        WorkAreaDTO workArea = workAreaService.getWorkAreaById(id);
        return Result.success(workArea);
    }

    @Operation(summary = "创建作业区", description = "创建新作业区（仅系统管理员）")
    @PostMapping
    public Result<WorkAreaDTO> createWorkArea(@Valid @RequestBody CreateWorkAreaRequest request) {
        WorkAreaDTO workArea = workAreaService.createWorkArea(request);
        return Result.success(workArea);
    }

    @Operation(summary = "更新作业区", description = "更新作业区信息（仅系统管理员，系统保护的作业区不可修改）")
    @PutMapping("/{id}")
    public Result<WorkAreaDTO> updateWorkArea(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkAreaRequest request) {
        WorkAreaDTO workArea = workAreaService.updateWorkArea(id, request);
        return Result.success(workArea);
    }

    @Operation(summary = "删除作业区", description = "删除作业区（仅系统管理员，系统保护的作业区不可删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteWorkArea(@PathVariable Long id) {
        workAreaService.deleteWorkArea(id);
        return Result.success();
    }
}
