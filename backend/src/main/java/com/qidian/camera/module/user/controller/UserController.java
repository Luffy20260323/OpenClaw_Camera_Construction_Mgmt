package com.qidian.camera.module.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.user.dto.*;
import com.qidian.camera.module.user.service.UserService;
import com.qidian.camera.module.auth.filter.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的个人信息")
    @GetMapping("/profile")
    public Result<UserProfileDTO> getProfile(
            @RequestAttribute("userId") Long userId) {
        UserProfileDTO profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    @PutMapping("/profile")
    public Result<UserProfileDTO> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfileDTO profile = userService.updateUserProfile(userId, request);
        return Result.success(profile);
    }

    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return Result.success();
    }

    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码")
    @PutMapping("/reset-password/{id}")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request, operatorId);
        return Result.success();
    }

    @Operation(summary = "用户注册", description = "用户自主注册（需要审批）")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        return Result.success("注册成功，等待管理员审批", userId);
    }

    @Operation(summary = "创建用户", description = "管理员创建用户")
    @PostMapping
    public Result<UserDTO> createUser(
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request, operatorId);
        return Result.success(user);
    }

    @Operation(summary = "批量导入用户", description = "管理员批量导入用户（JSON 格式）")
    @PostMapping("/batch-import")
    public Result<UserService.BatchImportResult> batchImport(
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody BatchImportRequest request) {
        UserService.BatchImportResult result = userService.batchImport(request, operatorId);
        return Result.success(result);
    }

    @Operation(summary = "下载导入模板", description = "下载 Excel 导入模板")
    @GetMapping("/import-template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        byte[] template = userService.generateImportTemplate();
        ByteArrayResource resource = new ByteArrayResource(template);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=yonghu-daoru-moban.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(template.length)
            .body(resource);
    }

    @Operation(summary = "批量导入用户（Excel 文件）", description = "上传 Excel 文件批量导入用户")
    @PostMapping("/batch-import-file")
    public Result<UserService.BatchImportResult> batchImportFile(
            @RequestAttribute("userId") Long operatorId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "autoApprove", defaultValue = "true") Boolean autoApprove) {
        UserService.BatchImportResult result = userService.batchImportFromExcel(file, operatorId, autoApprove);
        return Result.success(result);
    }

    @Operation(summary = "审批用户", description = "管理员审批用户注册申请")
    @PostMapping("/approve")
    public Result<UserDTO> approveUser(
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody ApprovalRequest request) {
        UserDTO user = userService.approveUser(request, operatorId);
        return Result.success(user);
    }

    @Operation(summary = "分页查询用户列表", description = "支持多条件筛选")
    @GetMapping("/list")
    public Result<Page<UserDTO>> queryUsers(
            @ModelAttribute UserQueryRequest query) {
        Page<UserDTO> page = userService.queryUsers(query);
        return Result.success(page);
    }

    @Operation(summary = "获取待审批用户列表", description = "获取当前管理员有权限审批的待审批用户")
    @GetMapping("/pending-approvals")
    public Result<List<UserDTO>> getPendingApprovals(
            @RequestAttribute("userId") Long operatorId) {
        List<UserDTO> users = userService.getPendingApprovals(operatorId);
        return Result.success(users);
    }

    @Operation(summary = "获取用户详情", description = "根据 ID 获取用户详细信息")
    @GetMapping("/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "更新用户信息", description = "管理员更新用户信息")
    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUser(id, request, operatorId);
        return Result.success(user);
    }
}
