package com.qidian.camera.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.common.exception.ErrorCode;
import com.qidian.camera.module.user.dto.*;
import com.qidian.camera.module.user.entity.User;
import com.qidian.camera.module.user.mapper.UserMapper;
import com.qidian.camera.module.role.entity.Role;
import com.qidian.camera.module.role.mapper.RoleMapper;
import com.qidian.camera.module.company.entity.Company;
import com.qidian.camera.module.company.mapper.CompanyMapper;
import com.qidian.camera.module.company.entity.CompanyType;
import com.qidian.camera.module.company.mapper.CompanyTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final CompanyMapper companyMapper;
    private final CompanyTypeMapper companyTypeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    // 公司类型常量
    private static final Integer COMPANY_TYPE_JIAFANG = 1;    // 甲方
    private static final Integer COMPANY_TYPE_YIFANG = 2;     // 乙方
    private static final Integer COMPANY_TYPE_JIANLIFANG = 3; // 监理方
    private static final Integer COMPANY_TYPE_SYSTEM = 4;     // 系统管理员

    /**
     * 用户自主注册
     *
     * @param request 注册请求
     * @return 用户 ID
     */
    @Transactional
    public Long register(RegisterRequest request) {
        // 1. 验证密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 2. 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 3. 验证公司信息（邮箱和手机号允许重复，不检查唯一性）
        Company company = null;
        Integer companyTypeId = null;
        if (request.getCompanyId() != null) {
            company = companyMapper.selectById(request.getCompanyId());
            if (company == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "公司不存在");
            }
            companyTypeId = company.getTypeId().intValue();
        }

        // 6. 创建用户（待审批状态）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCompanyId(request.getCompanyId());
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setApprovalStatus(0); // 待审批
        user.setStatus(0); // 待审批
        user.setIsSystemProtected(false);

        userMapper.insert(user);

        // 7. 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                    user.getId(),
                    roleId
                );
            }
        }

        // 8. 分配作业区
        if (request.getWorkAreaIds() != null && !request.getWorkAreaIds().isEmpty()) {
            for (Long workAreaId : request.getWorkAreaIds()) {
                jdbcTemplate.update(
                    "INSERT INTO user_work_areas (user_id, work_area_id) VALUES (?, ?)",
                    user.getId(),
                    workAreaId
                );
            }
        }

        log.info("用户注册成功，等待审批：userId={}, username={}, companyId={}, roleIds={}, workAreaIds={}", 
            user.getId(), user.getUsername(), request.getCompanyId(), request.getRoleIds(), request.getWorkAreaIds());

        return user.getId();
    }

    /**
     * 管理员创建用户
     *
     * @param request 创建用户请求
     * @param operatorId 操作人 ID
     * @return 用户 DTO
     */
    @Transactional
    public UserDTO createUser(CreateUserRequest request, Long operatorId) {
        // 1. 验证密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 2. 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 3. 验证公司信息（邮箱和手机号允许重复，不检查唯一性）
        Company company = null;
        Integer companyTypeId = null;
        if (request.getCompanyId() != null) {
            company = companyMapper.selectById(request.getCompanyId());
            if (company == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "公司不存在");
            }
            companyTypeId = company.getTypeId().intValue();
        }

        // 6. 权限校验：管理员只能创建自己公司范围内的用户
        validateUserCreationPermission(operatorId, companyTypeId);

        // 7. 验证角色
        List<Role> roles = new ArrayList<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                Role role = roleMapper.selectById(roleId);
                if (role == null) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "角色不存在：roleId=" + roleId);
                }
                roles.add(role);
            }
        }

        // 7. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCompanyId(request.getCompanyId());
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setIsSystemProtected(false);

        // 根据 autoApprove 决定审批状态
        if (Boolean.TRUE.equals(request.getAutoApprove())) {
            user.setApprovalStatus(1); // 已通过
            user.setStatus(1); // 正常
            user.setApprovedBy(operatorId);
            user.setApprovedAt(LocalDateTime.now());
        } else {
            user.setApprovalStatus(0); // 待审批
            user.setStatus(0); // 待审批
        }

        userMapper.insert(user);

        // 8. 分配角色
        if (!roles.isEmpty()) {
            for (Role role : roles) {
                jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                    user.getId(),
                    role.getId()
                );
            }
        }

        // 9. 分配作业区
        if (request.getWorkAreaIds() != null && !request.getWorkAreaIds().isEmpty()) {
            for (int i = 0; i < request.getWorkAreaIds().size(); i++) {
                Long workAreaId = request.getWorkAreaIds().get(i);
                Boolean isPrimary = (i == 0); // 第一个作业区设为主要作业区
                jdbcTemplate.update(
                    "INSERT INTO user_work_areas (user_id, work_area_id, is_primary) VALUES (?, ?, ?)",
                    user.getId(),
                    workAreaId,
                    isPrimary
                );
            }
        }

        log.info("管理员创建用户成功：userId={}, username={}, companyId={}, roleIds={}, workAreaIds={}, autoApprove={}", 
            user.getId(), user.getUsername(), request.getCompanyId(), request.getRoleIds(), request.getWorkAreaIds(), request.getAutoApprove());

        return convertToDTO(user);
    }

    /**
     * 批量导入用户
     *
     * @param request 批量导入请求
     * @param operatorId 操作人 ID
     * @return 导入结果
     */
    @Transactional
    public BatchImportResult batchImport(BatchImportRequest request, Long operatorId) {
        if (request.getUsers() == null || request.getUsers().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "导入用户列表不能为空");
        }

        BatchImportResult result = new BatchImportResult();
        result.setTotal(request.getUsers().size());
        result.setSuccess(0);
        result.setFailed(0);
        List<String> errors = new ArrayList<>();

        boolean autoApprove = Boolean.TRUE.equals(request.getAutoApprove());

        for (int i = 0; i < request.getUsers().size(); i++) {
            BatchImportRequest.ImportUserDTO importUser = request.getUsers().get(i);
            try {
                // 转换为 CreateUserRequest
                CreateUserRequest createRequest = new CreateUserRequest();
                createRequest.setUsername(importUser.getUsername());
                createRequest.setPassword(importUser.getPassword());
                createRequest.setConfirmPassword(importUser.getPassword());
                createRequest.setRealName(importUser.getRealName());
                createRequest.setEmail(importUser.getEmail());
                createRequest.setPhone(importUser.getPhone());
                createRequest.setCompanyId(importUser.getCompanyId());
                createRequest.setRoleIds(importUser.getRoleIds());
                createRequest.setGender(importUser.getGender());
                createRequest.setAutoApprove(autoApprove);

                createUser(createRequest, operatorId);
                result.setSuccess(result.getSuccess() + 1);
            } catch (Exception e) {
                result.setFailed(result.getFailed() + 1);
                errors.add("第" + (i + 1) + "条记录失败：" + e.getMessage());
                log.error("批量导入用户失败，第{}条：{}", i + 1, e.getMessage());
            }
        }

        result.setErrors(errors);
        log.info("批量导入用户完成：总数={}, 成功={}, 失败={}", 
            result.getTotal(), result.getSuccess(), result.getFailed());

        return result;
    }

    /**
     * 批量导入结果
     */
    @lombok.Data
    public static class BatchImportResult {
        private Integer total;
        private Integer success;
        private Integer failed;
        private List<String> errors;
        private List<ImportResultDetail> results;
    }

    /**
     * 导入结果详情
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ImportResultDetail {
        private Integer rowNum;
        private String username;
        private boolean success;
        private String error;
    }

    /**
     * 生成导入模板
     *
     * @return Excel 模板字节数组
     */
    public byte[] generateImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建用户数据 Sheet
            Sheet dataSheet = workbook.createSheet("用户数据");
            createDataSheet(dataSheet, workbook);
            
            // 创建说明信息 Sheet
            Sheet infoSheet = workbook.createSheet("说明信息");
            createInfoSheet(infoSheet, workbook);
            
            // 写入输出流
            try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            log.error("生成导入模板失败", e);
            throw new BusinessException(ErrorCode.ERROR, "生成导入模板失败：" + e.getMessage());
        }
    }

    /**
     * 创建用户数据 Sheet
     */
    private void createDataSheet(Sheet sheet, Workbook workbook) {
        // 创建表头（必填项加星号）
        Row headerRow = sheet.createRow(0);
        String[] headers = {"username*", "password*", "realName*", "email", "phone", "companyName*", "roleNames*", "workAreaNames**", "gender"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }
        
        // 设置列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 20 * 256);
        }
        
        // 添加必填说明
        Row noteRow = sheet.createRow(1);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("* 必填项；** 作业区角色时必填；邮箱电话可为空或重复；性别缺省为 1（男）");
        CellStyle noteStyle = workbook.createCellStyle();
        Font noteFont = workbook.createFont();
        noteFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        noteFont.setFontHeightInPoints((short) 9);
        noteStyle.setFont(noteFont);
        noteCell.setCellStyle(noteStyle);
    }

    /**
     * 从 Excel 文件批量导入用户
     *
     * @param file Excel 文件
     * @param operatorId 操作人 ID
     * @param autoApprove 是否自动审批
     * @return 导入结果
     */
    @Transactional
    public BatchImportResult batchImportFromExcel(MultipartFile file, Long operatorId, boolean autoApprove) {
        BatchImportResult result = new BatchImportResult();
        result.setResults(new ArrayList<>());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();

            result.setTotal(totalRows - 1); // 减去表头
            result.setSuccess(0);
            result.setFailed(0);

            // 从第 2 行开始读取数据（第 1 行是表头）
            for (int i = 1; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    BatchImportRequest.ImportUserDTO importUser = parseRow(row, i + 1);
                    CreateUserRequest createRequest = new CreateUserRequest();
                    createRequest.setUsername(importUser.getUsername());
                    createRequest.setPassword(importUser.getPassword());
                    createRequest.setConfirmPassword(importUser.getPassword());
                    createRequest.setRealName(importUser.getRealName());
                    createRequest.setEmail(importUser.getEmail());
                    createRequest.setPhone(importUser.getPhone());
                    createRequest.setCompanyId(importUser.getCompanyId());
                    createRequest.setRoleIds(importUser.getRoleIds());
                    createRequest.setWorkAreaIds(importUser.getWorkAreaIds());
                    createRequest.setGender(importUser.getGender());
                    createRequest.setAutoApprove(autoApprove);

                    createUser(createRequest, operatorId);
                    result.setSuccess(result.getSuccess() + 1);
                    result.getResults().add(new ImportResultDetail(i + 1, importUser.getUsername(), true, null));
                } catch (Exception e) {
                    result.setFailed(result.getFailed() + 1);
                    result.getResults().add(new ImportResultDetail(i + 1, 
                        row.getCell(0) != null ? row.getCell(0).getStringCellValue() : "未知用户", 
                        false, e.getMessage()));
                    log.error("批量导入用户失败，第{}行：{}", i + 1, e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            throw new BusinessException(ErrorCode.ERROR, "读取 Excel 文件失败：" + e.getMessage());
        }

        log.info("批量导入用户完成：总数={}, 成功={}, 失败={}", 
            result.getTotal(), result.getSuccess(), result.getFailed());

        return result;
    }

    /**
     * 解析 Excel 行数据
     *
     * @param row Excel 行
     * @param rowNum 行号
     * @return 导入用户 DTO
     */
    private BatchImportRequest.ImportUserDTO parseRow(Row row, int rowNum) {
        BatchImportRequest.ImportUserDTO dto = new BatchImportRequest.ImportUserDTO();

        // 必填字段检查
        String username = getCellValueAsString(row.getCell(0));
        String password = getCellValueAsString(row.getCell(1));
        String realName = getCellValueAsString(row.getCell(2));
        String companyName = getCellValueAsString(row.getCell(5));
        String roleStr = getCellValueAsString(row.getCell(6));
        
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "第" + rowNum + "行：用户名不能为空");
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "第" + rowNum + "行：密码不能为空且至少 6 位");
        }
        if (!StringUtils.hasText(realName)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "第" + rowNum + "行：姓名不能为空");
        }
        if (!StringUtils.hasText(companyName)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "第" + rowNum + "行：公司名称不能为空");
        }
        if (!StringUtils.hasText(roleStr)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "第" + rowNum + "行：角色不能为空");
        }

        dto.setUsername(username);
        dto.setPassword(password);
        dto.setRealName(realName);
        dto.setEmail(getCellValueAsString(row.getCell(3)));
        dto.setPhone(getCellValueAsString(row.getCell(4)));

        // 查询公司信息
        Company company = companyMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Company>()
            .eq(Company::getCompanyName, companyName));
        if (company == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "第" + rowNum + "行：公司\"" + companyName + "\"不存在");
        }
        dto.setCompanyId(company.getId());
        Long companyTypeId = company.getTypeId();

        // 解析角色名称列表（分号分隔）
        List<Long> roleIds = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();
        boolean hasWorkAreaRole = false;
        
        for (String roleName : roleStr.split(";")) {
            String trimmedName = roleName.trim();
            if (StringUtils.hasText(trimmedName)) {
                Role role = roleMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                    .eq(Role::getRoleName, trimmedName));
                if (role == null) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "第" + rowNum + "行：角色\"" + trimmedName + "\"不存在");
                }
                // 校验角色公司类型匹配
                Long roleCompanyTypeId = role.getCompanyTypeId();
                if (!roleCompanyTypeId.equals(companyTypeId)) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, 
                        "第" + rowNum + "行：角色\"" + trimmedName + "\"与公司\"" + companyName + "\"类型不匹配（公司是" + 
                        getCompanyTypeName(companyTypeId) + "，角色要求" + getCompanyTypeName(roleCompanyTypeId) + "）");
                }
                roleIds.add(role.getId());
                roleNames.add(role.getRoleName());
                // 检查是否为作业区角色
                if (role.getRoleName().contains("作业区")) {
                    hasWorkAreaRole = true;
                }
            }
        }
        dto.setRoleIds(roleIds);

        // 解析作业区名称列表（作业区角色必填）
        String workAreaStr = getCellValueAsString(row.getCell(7));
        List<Long> workAreaIds = new ArrayList<>();
        
        if (hasWorkAreaRole) {
            if (!StringUtils.hasText(workAreaStr)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "第" + rowNum + "行：作业区角色必须指定作业区，请在 workAreaNames 列填写作业区名称");
            }
            
            for (String workAreaName : workAreaStr.split(";")) {
                String trimmedName = workAreaName.trim();
                if (StringUtils.hasText(trimmedName)) {
                    Map<String, Object> wa = jdbcTemplate.queryForMap(
                        "SELECT wa.id, wa.company_id, c.company_name FROM work_areas wa " +
                        "JOIN companies c ON wa.company_id = c.id WHERE wa.work_area_name = ?", trimmedName);
                    if (wa == null) {
                        throw new BusinessException(ErrorCode.PARAM_ERROR, 
                            "第" + rowNum + "行：作业区\"" + trimmedName + "\"不存在");
                    }
                    // 校验作业区所属公司匹配
                    Long workAreaCompanyId = ((Number) wa.get("company_id")).longValue();
                    if (!workAreaCompanyId.equals(company.getId().longValue())) {
                        String workAreaCompanyName = (String) wa.get("company_name");
                        throw new BusinessException(ErrorCode.PARAM_ERROR, 
                            "第" + rowNum + "行：作业区\"" + trimmedName + "\"属于\"" + workAreaCompanyName + 
                            "\"，与当前公司\"" + companyName + "\"不匹配");
                    }
                    workAreaIds.add(((Number) wa.get("id")).longValue());
                }
            }
        } else if (StringUtils.hasText(workAreaStr)) {
            // 非作业区角色但填写了作业区，警告但允许
            log.warn("第{}行：非作业区角色但填写了作业区，将忽略作业区信息", rowNum);
        }
        
        dto.setWorkAreaIds(workAreaIds);

        // 性别（缺省为 1）
        Cell genderCell = row.getCell(8);
        if (genderCell != null) {
            try {
                dto.setGender((int) genderCell.getNumericCellValue());
            } catch (Exception e) {
                dto.setGender(1);
            }
        } else {
            dto.setGender(1); // 缺省为男
        }

        // 校验手机号格式（如果是数字格式，转换为字符串）
        if (StringUtils.hasText(dto.getPhone())) {
            String phone = dto.getPhone().trim();
            // 如果是科学计数法格式，尝试转换
            if (phone.contains("E")) {
                try {
                    long phoneNum = (long) Double.parseDouble(phone);
                    dto.setPhone(String.valueOf(phoneNum));
                } catch (Exception e) {
                    // 保持原值，让后续校验处理
                }
            }
        }

        return dto;
    }

    /**
     * 获取公司类型名称
     */
    private String getCompanyTypeName(Long typeId) {
        if (typeId == null) return "未知";
        switch (typeId.intValue()) {
            case 1: return "甲方公司";
            case 2: return "乙方公司";
            case 3: return "监理公司";
            case 4: return "软件所有者公司";
            default: return "类型" + typeId;
        }
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (int) num) {
                    return String.valueOf((int) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建说明信息 Sheet
     */
    private void createInfoSheet(Sheet sheet, Workbook workbook) {
        int rowNum = 0;
        
        // 标题
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("用户导入模板 - 说明信息");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        rowNum++; // 空行
        
        // 字段说明标题
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("【字段说明】");
        CellStyle sectionStyle = workbook.createCellStyle();
        Font sectionFont = workbook.createFont();
        sectionFont.setBold(true);
        sectionFont.setFontHeightInPoints((short) 12);
        sectionStyle.setFont(sectionFont);
        sectionCell.setCellStyle(sectionStyle);
        
        // 字段说明表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] infoHeaders = {"字段", "说明", "必填"};
        for (int i = 0; i < infoHeaders.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(infoHeaders[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }
        
        // 字段说明内容
        String[][] fieldInfos = {
            {"username*", "用户名（3-50 字符，不能重复）", "是"},
            {"password*", "密码（至少 6 位）", "是"},
            {"realName*", "真实姓名", "是"},
            {"email", "邮箱（可为空或重复）", "否"},
            {"phone", "手机号（可为空或重复）", "否"},
            {"companyName*", "公司名称（从公司列表选择）", "是"},
            {"roleNames*", "角色名称（多个用分号隔开，必须与公司类型匹配）", "是"},
            {"workAreaNames**", "作业区名称（作业区角色时必填，多个用分号隔开，必须与公司匹配）", "角色为作业区时必填"},
            {"gender", "性别（0:未知 1:男 2:女，缺省为 1）", "否"}
        };
        
        for (String[] info : fieldInfos) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < info.length; i++) {
                row.createCell(i).setCellValue(info[i]);
            }
        }
        
        rowNum++; // 空行
        
        // 公司列表
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("【可选公司列表】");
        sectionCell.setCellStyle(sectionStyle);
        
        Row companyHeaderRow = sheet.createRow(rowNum++);
        companyHeaderRow.createCell(0).setCellValue("公司名称");
        companyHeaderRow.createCell(1).setCellValue("公司类型");
        companyHeaderRow.getCell(0).setCellStyle(createHeaderStyle(workbook));
        companyHeaderRow.getCell(1).setCellStyle(createHeaderStyle(workbook));
        
        // 查询公司列表（关联公司类型）
        String companySql = "SELECT c.company_name, ct.type_name FROM companies c LEFT JOIN company_types ct ON c.type_id = ct.id ORDER BY c.company_name";
        List<Map<String, Object>> companies = jdbcTemplate.queryForList(companySql);
        for (Map<String, Object> company : companies) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) company.get("company_name"));
            row.createCell(1).setCellValue((String) company.get("type_name"));
        }
        
        rowNum++; // 空行
        
        // 角色列表
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("【可选角色列表】");
        sectionCell.setCellStyle(sectionStyle);
        
        Row roleHeaderRow = sheet.createRow(rowNum++);
        roleHeaderRow.createCell(0).setCellValue("角色名称");
        roleHeaderRow.createCell(1).setCellValue("角色编码");
        roleHeaderRow.createCell(2).setCellValue("公司类型");
        for (int i = 0; i < 3; i++) {
            roleHeaderRow.getCell(i).setCellStyle(createHeaderStyle(workbook));
        }
        
        // 查询角色列表（关联公司类型）
        String roleSql = "SELECT r.role_name, r.role_code, ct.type_name FROM roles r LEFT JOIN company_types ct ON r.company_type_id = ct.id ORDER BY r.role_name";
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(roleSql);
        for (Map<String, Object> role : roles) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) role.get("role_name"));
            row.createCell(1).setCellValue((String) role.get("role_code"));
            row.createCell(2).setCellValue((String) role.get("type_name"));
        }
        
        rowNum++; // 空行
        
        // 作业区列表
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("【可选作业区列表（仅甲方公司）】");
        sectionCell.setCellStyle(sectionStyle);
        
        Row workAreaHeaderRow = sheet.createRow(rowNum++);
        workAreaHeaderRow.createCell(0).setCellValue("作业区名称");
        workAreaHeaderRow.createCell(1).setCellValue("作业区编码");
        workAreaHeaderRow.createCell(2).setCellValue("所属公司");
        for (int i = 0; i < 3; i++) {
            workAreaHeaderRow.getCell(i).setCellStyle(createHeaderStyle(workbook));
        }
        
        // 查询作业区列表
        String workAreaSql = "SELECT wa.work_area_name, wa.work_area_code, c.company_name " +
                            "FROM work_areas wa JOIN companies c ON wa.company_id = c.id " +
                            "ORDER BY c.company_name, wa.work_area_name";
        List<Map<String, Object>> workAreas = jdbcTemplate.queryForList(workAreaSql);
        for (Map<String, Object> wa : workAreas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) wa.get("work_area_name"));
            row.createCell(1).setCellValue((String) wa.get("work_area_code"));
            row.createCell(2).setCellValue((String) wa.get("company_name"));
        }
        
        rowNum++; // 空行
        
        // 使用说明
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("【使用说明】");
        sectionCell.setCellStyle(sectionStyle);
        
        String[] instructions = {
            "1. 在用户数据 Sheet 中填写用户信息，每行一个用户",
            "2. 带 * 号的字段为必填项",
            "3. 公司名称和角色名称必须从上方列表中选择，完全匹配",
            "4. 多个角色用分号 (;) 隔开，如：乙方管理员;乙方普通用户",
            "5. 作业区角色需指定作业区，在 workAreaNames 列填写，多个用分号隔开",
            "6. 作业区名称必须从上方作业区列表中选择，完全匹配",
            "7. 单次导入用户数量不能超过 100 个",
            "8. 保存文件后，在系统中上传导入"
        };
        
        for (String instruction : instructions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(instruction);
        }
        
        // 自动调整列宽
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 删除用户
     *
     * @param userId 用户 ID
     * @param operatorId 操作人 ID
     */
    @Transactional
    public void deleteUser(Long userId, Long operatorId) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 检查是否为系统保护用户
        if (Boolean.TRUE.equals(user.getIsSystemProtected())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "系统保护用户不可删除");
        }

        // 3. 删除关联数据（级联删除）
        // 删除用户角色关联
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId);
        
        // 删除用户作业区关联
        jdbcTemplate.update("DELETE FROM user_work_areas WHERE user_id = ?", userId);

        // 4. 删除用户
        userMapper.deleteById(userId);

        log.info("删除用户：userId={}, username={}, operatorId={}", userId, user.getUsername(), operatorId);
    }

    /**
     * 审批用户
     *
     * @param request 审批请求
     * @param operatorId 操作人 ID（审批人）
     * @return 用户 DTO
     */
    @Transactional
    public UserDTO approveUser(ApprovalRequest request, Long operatorId) {
        // 1. 查询用户
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 检查用户是否已经审批过
        if (user.getApprovalStatus() != null && user.getApprovalStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该用户已经审批过，无法重复审批");
        }

        // 3. 权限校验：管理员只能审批自己公司范围内的用户
        validateApprovalPermission(operatorId, request.getUserId());

        // 4. 执行审批
        if (Boolean.TRUE.equals(request.getApproved())) {
            // 通过审批
            user.setApprovalStatus(1);
            user.setStatus(1);
            user.setApprovedBy(operatorId);
            user.setApprovedAt(LocalDateTime.now());
            user.setRejectionReason(null);
        } else {
            // 拒绝审批
            if (!StringUtils.hasText(request.getRejectionReason())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "拒绝审批时必须填写拒绝原因");
            }
            user.setApprovalStatus(2);
            user.setStatus(0); // 保持待审批状态，但标记为拒绝
            user.setApprovedBy(operatorId);
            user.setApprovedAt(LocalDateTime.now());
            user.setRejectionReason(request.getRejectionReason());
        }

        userMapper.updateById(user);

        log.info("用户审批{}：userId={}, operatorId={}", 
            request.getApproved() ? "通过" : "拒绝", request.getUserId(), operatorId);

        return convertToDTO(user);
    }

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 用户分页列表
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> queryUsers(UserQueryRequest query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        // 构建查询条件（关联作业区表以便关键字搜索）
        StringBuilder sql = new StringBuilder(
            "SELECT u.*, c.company_name, c.type_id, ct.type_name as company_type_name, " +
            "STRING_AGG(DISTINCT wa.work_area_name, ',') FILTER (WHERE wa.work_area_name IS NOT NULL) as work_area_names " +
            "FROM users u " +
            "LEFT JOIN companies c ON u.company_id = c.id " +
            "LEFT JOIN company_types ct ON c.type_id = ct.id " +
            "LEFT JOIN user_work_areas uwa ON u.id = uwa.user_id " +
            "LEFT JOIN work_areas wa ON uwa.work_area_id = wa.id " +
            "WHERE 1=1"
        );
        
        List<Object> params = new ArrayList<>();
        
        if (StringUtils.hasText(query.getUsername())) {
            sql.append(" AND u.username LIKE ?");
            params.add("%" + query.getUsername() + "%");
        }
        
        if (StringUtils.hasText(query.getRealName())) {
            sql.append(" AND u.real_name LIKE ?");
            params.add("%" + query.getRealName() + "%");
        }
        
        if (query.getCompanyId() != null) {
            sql.append(" AND u.company_id = ?");
            params.add(query.getCompanyId());
        }
        
        if (query.getCompanyTypeId() != null) {
            sql.append(" AND c.type_id = ?");
            params.add(query.getCompanyTypeId());
        }
        
        if (query.getApprovalStatus() != null) {
            sql.append(" AND u.approval_status = ?");
            params.add(query.getApprovalStatus());
        }
        
        if (query.getStatus() != null) {
            sql.append(" AND u.status = ?");
            params.add(query.getStatus());
        }
        
        if (StringUtils.hasText(query.getKeyword())) {
            // 关键字搜索覆盖：用户名、姓名、手机、邮箱、公司名、作业区名
            sql.append(" AND (u.username LIKE ? OR u.real_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ? OR c.company_name LIKE ? OR wa.work_area_name LIKE ?)");
            String keyword = "%" + query.getKeyword() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }
        
        // 添加 GROUP BY（因为使用了 STRING_AGG）
        sql.append(" GROUP BY u.id, c.company_name, c.type_id, ct.type_name");
        
        // 查询总数
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") as tmp";
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());
        
        // 分页查询
        sql.append(" ORDER BY u.created_at DESC LIMIT ? OFFSET ?");
        params.add(query.getPageSize());
        params.add((query.getPageNum() - 1) * query.getPageSize());
        
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        
        // 转换为 DTO
        List<UserDTO> dtoList = userList.stream()
            .map(this::convertMapToDTO)
            .collect(Collectors.toList());
        
        // 查询每个用户的角色和作业区
        for (UserDTO dto : dtoList) {
            // 查询角色
            List<Role> roles = roleMapper.selectByUserId(dto.getId());
            if (!roles.isEmpty()) {
                dto.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
                dto.setRoleCodes(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
                dto.setRoleNames(roles.stream().map(Role::getRoleName).collect(Collectors.toList()));
            }
            
            // 查询作业区
            String workAreaSql = "SELECT wa.id, wa.work_area_name FROM user_work_areas uwa " +
                                "JOIN work_areas wa ON uwa.work_area_id = wa.id " +
                                "WHERE uwa.user_id = ? ORDER BY uwa.is_primary DESC, wa.work_area_name ASC";
            List<Map<String, Object>> workAreaList = jdbcTemplate.queryForList(workAreaSql, dto.getId());
            if (!workAreaList.isEmpty()) {
                dto.setWorkAreaIds(workAreaList.stream()
                    .map(row -> ((Number) row.get("id")).longValue())
                    .collect(Collectors.toList()));
                dto.setWorkAreaNames(workAreaList.stream()
                    .map(row -> (String) row.get("work_area_name"))
                    .collect(Collectors.toList()));
            }
        }
        
        Page<UserDTO> result = new Page<>(query.getPageNum(), query.getPageSize(), total != null ? total : 0);
        result.setRecords(dtoList);
        
        return result;
    }

    /**
     * 获取待审批用户列表（根据审批人权限）
     *
     * @param operatorId 操作人 ID
     * @return 待审批用户列表
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getPendingApprovals(Long operatorId) {
        // 获取审批人的公司信息
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 获取审批人的角色
        List<Role> operatorRoles = roleMapper.selectByUserId(operatorId);
        boolean isSystemAdmin = operatorRoles.stream()
            .anyMatch(role -> "system_admin".equals(role.getRoleCode()));

        StringBuilder sql = new StringBuilder(
            "SELECT u.*, c.company_name, c.type_id, ct.type_name as company_type_name " +
            "FROM users u " +
            "LEFT JOIN companies c ON u.company_id = c.id " +
            "LEFT JOIN company_types ct ON c.type_id = ct.id " +
            "WHERE u.approval_status = 0"
        );

        List<Object> params = new ArrayList<>();

        // 系统管理员可以审批所有用户，非系统管理员只能审批本公司的用户
        if (!isSystemAdmin && operator.getCompanyId() != null) {
            // 只能审批同一个公司的用户
            sql.append(" AND u.company_id = ?");
            params.add(operator.getCompanyId());
        }

        sql.append(" ORDER BY u.created_at ASC");

        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        return userList.stream()
            .map(this::convertMapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取用户详情
     *
     * @param userId 用户 ID
     * @return 用户 DTO
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return convertToDTO(user);
    }

    /**
     * 管理员更新用户信息
     *
     * @param userId 用户 ID
     * @param request 更新请求
     * @param operatorId 操作人 ID
     * @return 更新后的用户信息
     */
    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserRequest request, Long operatorId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查邮箱是否已被其他用户使用（仅在邮箱有变化时检查）
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            User existingUser = userMapper.selectByEmail(request.getEmail());
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
            user.setEmail(request.getEmail());
        }

        // 检查手机号是否已被其他用户使用（仅在手机号有变化时检查）
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            User existingUser = userMapper.selectByPhone(request.getPhone());
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
            user.setPhone(request.getPhone());
        }

        // 更新用户信息（仅在字段有值时更新）
        if (request.getRealName() != null && !request.getRealName().trim().isEmpty()) {
            user.setRealName(request.getRealName());
        }
        
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        
        // 更新邮箱和手机号（允许重复，不检查唯一性）
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        // 更新审批状态
        if (request.getApprovalStatus() != null) {
            user.setApprovalStatus(request.getApprovalStatus());
        }
        
        // 更新拒绝原因
        if (request.getRejectionReason() != null) {
            user.setRejectionReason(request.getRejectionReason());
        }
        
        // 更新用户状态
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        userMapper.updateById(user);

        // 更新用户角色
        if (request.getRoleIds() != null) {
            // 删除原有角色
            jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId);
            // 添加新角色
            if (!request.getRoleIds().isEmpty()) {
                for (Long roleId : request.getRoleIds()) {
                    jdbcTemplate.update(
                        "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                        userId, roleId
                    );
                }
            }
        }

        // 更新用户作业区
        if (request.getWorkAreaIds() != null) {
            // 删除原有作业区
            jdbcTemplate.update("DELETE FROM user_work_areas WHERE user_id = ?", userId);
            // 添加新作业区
            if (!request.getWorkAreaIds().isEmpty()) {
                for (int i = 0; i < request.getWorkAreaIds().size(); i++) {
                    Long workAreaId = request.getWorkAreaIds().get(i);
                    Boolean isPrimary = (i == 0); // 第一个作业区设为主要作业区
                    jdbcTemplate.update(
                        "INSERT INTO user_work_areas (user_id, work_area_id, is_primary) VALUES (?, ?, ?)",
                        userId, workAreaId, isPrimary
                    );
                }
            }
        }

        log.info("管理员更新用户信息：userId={}, realName={}, email={}, phone={}, approvalStatus={}, status={}, roleIds={}, workAreaIds={}", 
            userId, request.getRealName(), request.getEmail(), request.getPhone(), 
            request.getApprovalStatus(), request.getStatus(), request.getRoleIds(), request.getWorkAreaIds());

        return getUserById(userId);
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户 ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    @Transactional
    public UserProfileDTO updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新用户信息（邮箱和手机号允许重复）
        user.setRealName(request.getRealName());
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        userMapper.updateById(user);

        log.info("用户信息已更新：userId={}, realName={}, email={}, phone={}", 
            userId, request.getRealName(), request.getEmail(), request.getPhone());

        return getUserProfile(userId);
    }

    /**
     * 修改密码
     *
     * @param userId 用户 ID
     * @param request 修改密码请求
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }

        // 更新密码
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userMapper.updateById(user);

        log.info("用户密码已修改：userId={}", userId);
    }

    /**
     * 管理员重置用户密码
     *
     * @param userId 用户 ID
     * @param request 重置密码请求
     * @param operatorId 操作人 ID
     */
    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request, Long operatorId) {
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新密码
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userMapper.updateById(user);

        log.info("管理员重置用户密码：userId={}, operatorId={}", userId, operatorId);
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        // 直接使用 JdbcTemplate 查询完整用户信息（包括公司）
        String sql = "SELECT u.*, c.company_name, c.type_id, ct.type_name as company_type " +
                     "FROM users u " +
                     "LEFT JOIN companies c ON u.company_id = c.id " +
                     "LEFT JOIN company_types ct ON c.type_id = ct.id " +
                     "WHERE u.id = ?";
        
        Map<String, Object> userData = jdbcTemplate.queryForMap(sql, userId);
        
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(((Number) userData.get("id")).longValue());
        dto.setUsername((String) userData.get("username"));
        dto.setRealName((String) userData.get("real_name"));
        dto.setGender(((Number) userData.get("gender")).intValue());
        dto.setEmail((String) userData.get("email"));
        dto.setPhone((String) userData.get("phone"));
        dto.setCompanyName((String) userData.get("company_name"));
        dto.setCompanyType((String) userData.get("company_type"));
        Object typeIdObj = userData.get("type_id");
        if (typeIdObj != null) {
            dto.setCompanyTypeId(((Number) typeIdObj).intValue());
        } else {
            dto.setCompanyTypeId(null);
        }
        log.debug("用户 {} 公司类型 ID: {}", userId, dto.getCompanyTypeId());
        
        log.info("用户 {} 公司信息：name={}, type={}", userId, 
            dto.getCompanyName(), dto.getCompanyType());

        // 查询角色
        List<String> roles = roleMapper.selectByUserId(userId).stream()
                .map(r -> r.getRoleCode())
                .toList();
        dto.setRoles(roles);

        // 查询作业区（仅甲方公司，type_id=1）
        if (dto.getCompanyTypeId() != null && dto.getCompanyTypeId() == 1) {
            String workAreaSql = "SELECT wa.id, wa.work_area_name, wa.work_area_code, uwa.is_primary " +
                                "FROM user_work_areas uwa " +
                                "INNER JOIN work_areas wa ON uwa.work_area_id = wa.id " +
                                "WHERE uwa.user_id = ? " +
                                "ORDER BY uwa.is_primary DESC, wa.work_area_name";
            
            try {
                List<Map<String, Object>> workAreaList = jdbcTemplate.queryForList(workAreaSql, userId);
                List<UserProfileDTO.WorkAreaInfo> workAreas = workAreaList.stream()
                    .map(wa -> new UserProfileDTO.WorkAreaInfo(
                        ((Number) wa.get("id")).longValue(),
                        (String) wa.get("work_area_name"),
                        (String) wa.get("work_area_code"),
                        (Boolean) wa.get("is_primary")
                    ))
                    .toList();
                dto.setWorkAreas(workAreas);
                log.info("用户 {} 的作业区数量：{}", userId, workAreas.size());
            } catch (Exception e) {
                log.warn("查询作业区失败：{}", e.getMessage());
                dto.setWorkAreas(new java.util.ArrayList<>());
            }
        }

        return dto;
    }

    /**
     * 将 User 实体转换为 DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
        dto.setCompanyId(user.getCompanyId());
        dto.setApprovalStatus(user.getApprovalStatus());
        dto.setApprovedBy(user.getApprovedBy());
        dto.setApprovedAt(user.getApprovedAt());
        dto.setRejectionReason(user.getRejectionReason());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // 查询公司信息
        if (user.getCompanyId() != null) {
            Company company = companyMapper.selectById(user.getCompanyId());
            if (company != null) {
                dto.setCompanyName(company.getCompanyName());
                dto.setCompanyTypeId(company.getTypeId().intValue());
                
                // 查询公司类型名称
                CompanyType companyType = companyTypeMapper.selectById(company.getTypeId());
                if (companyType != null) {
                    dto.setCompanyTypeName(companyType.getTypeName());
                }
            }
        }

        // 查询角色
        List<Role> roles = roleMapper.selectByUserId(user.getId());
        if (!roles.isEmpty()) {
            dto.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
            dto.setRoleCodes(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
            dto.setRoleNames(roles.stream().map(Role::getRoleName).collect(Collectors.toList()));
        }

        // 查询审批人姓名
        if (user.getApprovedBy() != null) {
            User approver = userMapper.selectById(user.getApprovedBy());
            if (approver != null) {
                dto.setApprovedByName(approver.getRealName());
            }
        }

        return dto;
    }

    /**
     * 将 Map 转换为 DTO
     */
    private UserDTO convertMapToDTO(Map<String, Object> data) {
        UserDTO dto = new UserDTO();
        dto.setId(((Number) data.get("id")).longValue());
        dto.setUsername((String) data.get("username"));
        dto.setRealName((String) data.get("real_name"));
        dto.setEmail((String) data.get("email"));
        dto.setPhone((String) data.get("phone"));
        dto.setGender(((Number) data.get("gender")).intValue());
        dto.setCompanyId(data.get("company_id") != null ? ((Number) data.get("company_id")).longValue() : null);
        dto.setCompanyName((String) data.get("company_name"));
        dto.setCompanyTypeId(data.get("type_id") != null ? ((Number) data.get("type_id")).intValue() : null);
        dto.setCompanyTypeName((String) data.get("company_type_name"));
        dto.setApprovalStatus((Integer) data.get("approval_status"));
        dto.setApprovedBy(data.get("approved_by") != null ? ((Number) data.get("approved_by")).longValue() : null);
        dto.setApprovedAt(data.get("approved_at") != null ? 
            ((java.sql.Timestamp) data.get("approved_at")).toLocalDateTime() : null);
        dto.setRejectionReason((String) data.get("rejection_reason"));
        dto.setStatus((Integer) data.get("status"));
        dto.setCreatedAt(data.get("created_at") != null ? 
            ((java.sql.Timestamp) data.get("created_at")).toLocalDateTime() : null);
        dto.setUpdatedAt(data.get("updated_at") != null ? 
            ((java.sql.Timestamp) data.get("updated_at")).toLocalDateTime() : null);

        return dto;
    }

    /**
     * 校验用户创建权限
     * 管理员只能创建自己公司范围内的用户
     *
     * @param operatorId 操作人 ID
     * @param targetCompanyTypeId 目标公司类型 ID
     */
    private void validateUserCreationPermission(Long operatorId, Integer targetCompanyTypeId) {
        if (operatorId == null || targetCompanyTypeId == null) {
            return;
        }

        // 获取操作人信息
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 获取操作人的公司类型
        Integer operatorCompanyTypeId = null;
        if (operator.getCompanyId() != null) {
            Company operatorCompany = companyMapper.selectById(operator.getCompanyId());
            if (operatorCompany != null) {
                operatorCompanyTypeId = operatorCompany.getTypeId().intValue();
            }
        }

        // 系统管理员可以创建任何用户
        if (COMPANY_TYPE_SYSTEM.equals(operatorCompanyTypeId)) {
            return;
        }

        // 其他管理员只能创建同公司类型的用户
        if (!operatorCompanyTypeId.equals(targetCompanyTypeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, 
                "您没有权限创建该公司类型的用户。系统管理员可创建所有用户，其他管理员只能创建本公司类型的用户");
        }
    }

    /**
     * 校验用户审批权限
     * 管理员只能审批自己公司范围内的用户
     *
     * @param operatorId 操作人 ID（审批人）
     * @param targetUserId 目标用户 ID
     */
    private void validateApprovalPermission(Long operatorId, Long targetUserId) {
        if (operatorId == null || targetUserId == null) {
            return;
        }

        // 获取审批人信息
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 获取审批人的公司类型
        Integer operatorCompanyTypeId = null;
        if (operator.getCompanyId() != null) {
            Company operatorCompany = companyMapper.selectById(operator.getCompanyId());
            if (operatorCompany != null) {
                operatorCompanyTypeId = operatorCompany.getTypeId().intValue();
            }
        }

        // 系统管理员可以审批所有用户
        if (COMPANY_TYPE_SYSTEM.equals(operatorCompanyTypeId)) {
            return;
        }

        // 获取待审批用户的公司类型
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Integer targetCompanyTypeId = null;
        if (targetUser.getCompanyId() != null) {
            Company targetCompany = companyMapper.selectById(targetUser.getCompanyId());
            if (targetCompany != null) {
                targetCompanyTypeId = targetCompany.getTypeId().intValue();
            }
        }

        // 只能审批同公司类型的用户
        if (operatorCompanyTypeId != null && !operatorCompanyTypeId.equals(targetCompanyTypeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, 
                "您没有权限审批该公司类型的用户。系统管理员可审批所有用户，其他管理员只能审批本公司类型的用户");
        }
    }

    /**
     * 校验公司管理权限
     * 只有系统管理员可以管理公司
     *
     * @param operatorId 操作人 ID
     */
    public void validateCompanyManagementPermission(Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Integer operatorCompanyTypeId = null;
        if (operator.getCompanyId() != null) {
            Company operatorCompany = companyMapper.selectById(operator.getCompanyId());
            if (operatorCompany != null) {
                operatorCompanyTypeId = operatorCompany.getTypeId().intValue();
            }
        }

        // 只有系统管理员可以管理公司（公司类型 ID = 4）
        if (!COMPANY_TYPE_SYSTEM.equals(operatorCompanyTypeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, 
                "只有系统管理员才有权限管理公司");
        }
    }

    /**
     * 校验角色管理权限
     * 只有系统管理员可以管理角色
     *
     * @param operatorId 操作人 ID
     */
    public void validateRoleManagementPermission(Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Integer operatorCompanyTypeId = null;
        if (operator.getCompanyId() != null) {
            Company operatorCompany = companyMapper.selectById(operator.getCompanyId());
            if (operatorCompany != null) {
                operatorCompanyTypeId = operatorCompany.getTypeId().intValue();
            }
        }

        // 只有系统管理员可以管理角色（公司类型 ID = 4）
        if (!COMPANY_TYPE_SYSTEM.equals(operatorCompanyTypeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, 
                "只有系统管理员才有权限管理角色");
        }
    }

    /**
     * 校验作业区管理权限
     * 只有系统管理员可以管理作业区
     *
     * @param operatorId 操作人 ID
     */
    public void validateWorkAreaManagementPermission(Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Integer operatorCompanyTypeId = null;
        if (operator.getCompanyId() != null) {
            Company operatorCompany = companyMapper.selectById(operator.getCompanyId());
            if (operatorCompany != null) {
                operatorCompanyTypeId = operatorCompany.getTypeId().intValue();
            }
        }

        // 只有系统管理员可以管理作业区（公司类型 ID = 4）
        if (!COMPANY_TYPE_SYSTEM.equals(operatorCompanyTypeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, 
                "只有系统管理员才有权限管理作业区");
        }
    }
}
