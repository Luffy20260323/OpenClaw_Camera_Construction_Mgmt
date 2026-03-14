package com.qidian.camera.module.company.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.common.exception.ErrorCode;
import com.qidian.camera.module.company.dto.CompanyDTO;
import com.qidian.camera.module.company.dto.CreateCompanyRequest;
import com.qidian.camera.module.company.dto.UpdateCompanyRequest;
import com.qidian.camera.module.company.entity.Company;
import com.qidian.camera.module.company.entity.CompanyType;
import com.qidian.camera.module.company.mapper.CompanyMapper;
import com.qidian.camera.module.company.mapper.CompanyTypeMapper;
import com.qidian.camera.module.user.entity.User;
import com.qidian.camera.module.user.mapper.UserMapper;
import com.qidian.camera.module.workarea.entity.WorkArea;
import com.qidian.camera.module.workarea.mapper.WorkAreaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公司服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyMapper companyMapper;
    private final CompanyTypeMapper companyTypeMapper;
    private final UserMapper userMapper;
    private final WorkAreaMapper workAreaMapper;

    /**
     * 分页查询公司列表
     */
    @Transactional(readOnly = true)
    public Page<CompanyDTO> getCompanies(Integer pageNum, Integer pageSize, Long typeId, String keyword, String status, Boolean allowAnonymousRegister) {
        Page<Company> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        
        // 按类型筛选
        if (typeId != null) {
            wrapper.eq(Company::getTypeId, typeId);
        }
        
        // 关键词搜索（公司名称、联系人）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Company::getCompanyName, keyword)
                .or()
                .like(Company::getContactPerson, keyword)
            );
        }
        
        // 按状态筛选
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Company::getStatus, status);
        }
        
        // 按是否允许匿名注册筛选
        if (allowAnonymousRegister != null && allowAnonymousRegister) {
            wrapper.eq(Company::getAllowAnonymousRegister, true);
        }
        
        Page<Company> companyPage = companyMapper.selectPage(page, wrapper);
        
        // 转换为 DTO
        List<CompanyDTO> dtoList = companyPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        Page<CompanyDTO> dtoPage = new Page<>(pageNum, pageSize, companyPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    /**
     * 获取公司详情
     */
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyById(Long id) {
        Company company = companyMapper.selectById(id);
        if (company == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公司不存在");
        }
        return convertToDTO(company);
    }

    /**
     * 创建公司
     */
    @Transactional
    public CompanyDTO createCompany(CreateCompanyRequest request) {
        // 检查公司名称是否重复
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Company::getCompanyName, request.getCompanyName());
        Long count = companyMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公司名称已存在");
        }
        
        // 检查公司类型是否存在
        CompanyType companyType = companyTypeMapper.selectById(request.getTypeId());
        if (companyType == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公司类型不存在");
        }
        
        Company company = new Company();
        company.setCompanyName(request.getCompanyName());
        company.setTypeId(request.getTypeId());
        company.setUnifiedSocialCreditCode(request.getUnifiedSocialCreditCode());
        company.setContactPerson(request.getContactPerson());
        company.setContactPhone(request.getContactPhone());
        company.setContactEmail(request.getContactEmail());
        company.setAddress(request.getAddress());
        company.setStatus("active");
        company.setDescription(request.getDescription());
        company.setIsSystemProtected(false);
        // 系统保护的公司不允许匿名注册
        company.setAllowAnonymousRegister(request.getAllowAnonymousRegister() != null ? request.getAllowAnonymousRegister() : false);
        
        companyMapper.insert(company);
        
        log.info("创建公司成功：id={}, name={}", company.getId(), company.getCompanyName());
        
        return getCompanyById(company.getId());
    }

    /**
     * 更新公司
     */
    @Transactional
    public CompanyDTO updateCompany(Long id, UpdateCompanyRequest request) {
        Company company = companyMapper.selectById(id);
        if (company == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公司不存在");
        }
        
        // 系统保护的公司不可修改
        if (company.getIsSystemProtected()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "系统保护的公司不可修改");
        }
        
        // 检查公司名称是否与其他公司重复
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Company::getCompanyName, request.getCompanyName());
        wrapper.ne(Company::getId, id);
        Long count = companyMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公司名称已存在");
        }
        
        company.setCompanyName(request.getCompanyName());
        company.setTypeId(request.getTypeId());
        company.setUnifiedSocialCreditCode(request.getUnifiedSocialCreditCode());
        company.setContactPerson(request.getContactPerson());
        company.setContactPhone(request.getContactPhone());
        company.setContactEmail(request.getContactEmail());
        company.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            company.setStatus(request.getStatus());
        }
        company.setDescription(request.getDescription());
        // 系统保护的公司不允许匿名注册
        if (company.getIsSystemProtected()) {
            company.setAllowAnonymousRegister(false);
        } else if (request.getAllowAnonymousRegister() != null) {
            company.setAllowAnonymousRegister(request.getAllowAnonymousRegister());
        }
        
        companyMapper.updateById(company);
        
        log.info("更新公司成功：id={}, name={}, allowAnonymousRegister={}", id, company.getCompanyName(), company.getAllowAnonymousRegister());
        
        return getCompanyById(id);
    }

    /**
     * 删除公司
     */
    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyMapper.selectById(id);
        if (company == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公司不存在");
        }
        
        // 系统保护的公司不可删除
        if (company.getIsSystemProtected()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "系统保护的公司不可删除");
        }
        
        // 检查是否有关联的作业区
        LambdaQueryWrapper<WorkArea> workAreaWrapper = new LambdaQueryWrapper<>();
        workAreaWrapper.eq(WorkArea::getCompanyId, id);
        Long workAreaCount = workAreaMapper.selectCount(workAreaWrapper);
        if (workAreaCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该公司下存在" + workAreaCount + "个作业区，无法删除");
        }
        
        // 检查是否有关联的用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getCompanyId, id);
        Long userCount = userMapper.selectCount(userWrapper);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该公司下存在" + userCount + "个用户，无法删除");
        }
        
        companyMapper.deleteById(id);
        
        log.info("删除公司成功：id={}", id);
    }

    /**
     * 获取所有公司类型
     */
    @Transactional(readOnly = true)
    public List<CompanyType> getCompanyTypes() {
        return companyTypeMapper.selectList(null);
    }

    /**
     * 转换为 DTO
     */
    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setCompanyName(company.getCompanyName());
        dto.setTypeId(company.getTypeId());
        System.out.println("CompanyDTO convert: id=" + company.getId() + ", name=" + company.getCompanyName() + ", typeId=" + company.getTypeId());
        dto.setUnifiedSocialCreditCode(company.getUnifiedSocialCreditCode());
        dto.setContactPerson(company.getContactPerson());
        dto.setContactPhone(company.getContactPhone());
        dto.setContactEmail(company.getContactEmail());
        dto.setAddress(company.getAddress());
        dto.setStatus(company.getStatus());
        dto.setDescription(company.getDescription());
        dto.setIsSystemProtected(company.getIsSystemProtected());
        // 系统保护的公司永远不允许匿名注册
        dto.setAllowAnonymousRegister(company.getIsSystemProtected() ? false : company.getAllowAnonymousRegister());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setUpdatedAt(company.getUpdatedAt());
        
        // 查询公司类型名称
        if (company.getTypeId() != null) {
            CompanyType companyType = companyTypeMapper.selectById(company.getTypeId());
            if (companyType != null) {
                dto.setTypeName(companyType.getTypeName());
            }
        }
        
        return dto;
    }
}
