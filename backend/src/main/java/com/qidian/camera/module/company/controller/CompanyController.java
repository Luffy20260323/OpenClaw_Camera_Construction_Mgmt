package com.qidian.camera.module.company.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qidian.camera.common.response.Result;
import com.qidian.camera.module.company.dto.CompanyDTO;
import com.qidian.camera.module.company.dto.CreateCompanyRequest;
import com.qidian.camera.module.company.dto.UpdateCompanyRequest;
import com.qidian.camera.module.company.entity.CompanyType;
import com.qidian.camera.module.company.service.CompanyService;
import com.qidian.camera.module.auth.annotation.ApiPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公司控制器
 */
@Tag(name = "公司管理", description = "公司信息管理（仅系统管理员）")
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final com.qidian.camera.module.user.service.UserService userService;

    @Operation(summary = "分页查询公司列表", description = "支持按类型、关键词、状态、是否允许匿名注册筛选")
    @ApiPermission("company:list")
    @GetMapping
    public Result<Page<CompanyDTO>> getCompanies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean allowAnonymousRegister) {
        Page<CompanyDTO> page = companyService.getCompanies(pageNum, pageSize, typeId, keyword, status, allowAnonymousRegister);
        return Result.success(page);
    }

    @Operation(summary = "获取公司详情", description = "根据 ID 获取公司详细信息")
    @ApiPermission("company:view")
    @GetMapping("/{id}")
    public Result<CompanyDTO> getCompany(@PathVariable Long id) {
        CompanyDTO company = companyService.getCompanyById(id);
        return Result.success(company);
    }

    @Operation(summary = "创建公司", description = "创建新公司（仅系统管理员）")
    @ApiPermission("company:create")
    @PostMapping
    public Result<CompanyDTO> createCompany(
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody CreateCompanyRequest request) {
        userService.validateCompanyManagementPermission(operatorId);
        CompanyDTO company = companyService.createCompany(request);
        return Result.success(company);
    }

    @Operation(summary = "更新公司", description = "更新公司信息（仅系统管理员，系统保护的公司不可修改）")
    @ApiPermission("company:edit")
    @PutMapping("/{id}")
    public Result<CompanyDTO> updateCompany(
            @PathVariable Long id,
            @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody UpdateCompanyRequest request) {
        userService.validateCompanyManagementPermission(operatorId);
        CompanyDTO company = companyService.updateCompany(id, request);
        return Result.success(company);
    }

    @Operation(summary = "删除公司", description = "删除公司（仅系统管理员，系统保护的公司不可删除）")
    @ApiPermission("company:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCompany(
            @PathVariable Long id,
            @RequestAttribute("userId") Long operatorId) {
        userService.validateCompanyManagementPermission(operatorId);
        companyService.deleteCompany(id);
        return Result.success();
    }

    @Operation(summary = "获取所有公司类型", description = "获取公司类型列表（甲方/乙方/监理/软件所有者）")
    @ApiPermission("company:type:list")
    @GetMapping(value = "/types", produces = "application/json")
    @ResponseBody
    public Result<List<CompanyType>> getCompanyTypes() {
        List<CompanyType> types = companyService.getCompanyTypes();
        return Result.success(types);
    }
}
