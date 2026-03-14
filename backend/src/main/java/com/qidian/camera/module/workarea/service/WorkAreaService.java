package com.qidian.camera.module.workarea.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qidian.camera.common.exception.BusinessException;
import com.qidian.camera.common.exception.ErrorCode;
import com.qidian.camera.module.workarea.dto.WorkAreaDTO;
import com.qidian.camera.module.workarea.dto.CreateWorkAreaRequest;
import com.qidian.camera.module.workarea.dto.UpdateWorkAreaRequest;
import com.qidian.camera.module.workarea.entity.WorkArea;
import com.qidian.camera.module.workarea.mapper.WorkAreaMapper;
import com.qidian.camera.module.company.mapper.CompanyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 作业区服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkAreaService {

    private final WorkAreaMapper workAreaMapper;
    private final CompanyMapper companyMapper;

    /**
     * 分页查询作业区列表
     */
    @Transactional(readOnly = true)
    public Page<WorkAreaDTO> getWorkAreas(Integer pageNum, Integer pageSize, Long companyId, String keyword) {
        Page<WorkArea> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<WorkArea> wrapper = new LambdaQueryWrapper<>();
        
        // 按公司筛选
        if (companyId != null) {
            wrapper.eq(WorkArea::getCompanyId, companyId);
        }
        
        // 关键词搜索（作业区名称、负责人）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(WorkArea::getWorkAreaName, keyword)
                .or()
                .like(WorkArea::getLeaderName, keyword)
            );
        }
        
        Page<WorkArea> workAreaPage = workAreaMapper.selectPage(page, wrapper);
        
        // 转换为 DTO
        List<WorkAreaDTO> dtoList = workAreaPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        Page<WorkAreaDTO> dtoPage = new Page<>(pageNum, pageSize, workAreaPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    /**
     * 获取作业区详情
     */
    @Transactional(readOnly = true)
    public WorkAreaDTO getWorkAreaById(Long id) {
        WorkArea workArea = workAreaMapper.selectById(id);
        if (workArea == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "作业区不存在");
        }
        return convertToDTO(workArea);
    }

    /**
     * 创建作业区
     */
    @Transactional
    public WorkAreaDTO createWorkArea(CreateWorkAreaRequest request) {
        // 检查作业区编码是否重复
        if (request.getWorkAreaCode() != null && !request.getWorkAreaCode().trim().isEmpty()) {
            LambdaQueryWrapper<WorkArea> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkArea::getWorkAreaCode, request.getWorkAreaCode());
            wrapper.eq(WorkArea::getCompanyId, request.getCompanyId());
            Long count = workAreaMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该作业区编码已存在");
            }
        }
        
        // 检查公司是否存在
        var company = companyMapper.selectById(request.getCompanyId());
        if (company == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公司不存在");
        }
        
        // 检查公司类型是否为甲方（typeId=1）
        if (company.getTypeId() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有甲方公司才能创建作业区");
        }
        
        WorkArea workArea = new WorkArea();
        workArea.setWorkAreaName(request.getWorkAreaName());
        workArea.setWorkAreaCode(request.getWorkAreaCode());
        workArea.setCompanyId(request.getCompanyId());
        workArea.setLeaderName(request.getLeaderName());
        workArea.setLeaderPhone(request.getLeaderPhone());
        workArea.setGeographicRange(request.getGeographicRange());
        workArea.setMaxCapacity(request.getMaxCapacity() != null ? request.getMaxCapacity() : 1000);
        workArea.setDescription(request.getDescription());
        workArea.setIsSystemProtected(false);
        
        workAreaMapper.insert(workArea);
        
        log.info("创建作业区成功：id={}, name={}", workArea.getId(), workArea.getWorkAreaName());
        
        return getWorkAreaById(workArea.getId());
    }

    /**
     * 更新作业区
     */
    @Transactional
    public WorkAreaDTO updateWorkArea(Long id, UpdateWorkAreaRequest request) {
        WorkArea workArea = workAreaMapper.selectById(id);
        if (workArea == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "作业区不存在");
        }
        
        // 系统保护的作业区不可修改
        if (workArea.getIsSystemProtected()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "系统保护的作业区不可修改");
        }
        
        // 检查作业区编码是否与其他作业区重复
        if (request.getWorkAreaCode() != null && !request.getWorkAreaCode().trim().isEmpty()) {
            LambdaQueryWrapper<WorkArea> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkArea::getWorkAreaCode, request.getWorkAreaCode());
            wrapper.eq(WorkArea::getCompanyId, request.getCompanyId());
            wrapper.ne(WorkArea::getId, id);
            Long count = workAreaMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该作业区编码已存在");
            }
        }
        
        workArea.setWorkAreaName(request.getWorkAreaName());
        workArea.setWorkAreaCode(request.getWorkAreaCode());
        workArea.setCompanyId(request.getCompanyId());
        workArea.setLeaderName(request.getLeaderName());
        workArea.setLeaderPhone(request.getLeaderPhone());
        workArea.setGeographicRange(request.getGeographicRange());
        workArea.setMaxCapacity(request.getMaxCapacity() != null ? request.getMaxCapacity() : workArea.getMaxCapacity());
        workArea.setDescription(request.getDescription());
        
        workAreaMapper.updateById(workArea);
        
        log.info("更新作业区成功：id={}, name={}", id, workArea.getWorkAreaName());
        
        return getWorkAreaById(id);
    }

    /**
     * 删除作业区
     */
    @Transactional
    public void deleteWorkArea(Long id) {
        WorkArea workArea = workAreaMapper.selectById(id);
        if (workArea == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "作业区不存在");
        }
        
        // 系统保护的作业区不可删除
        if (workArea.getIsSystemProtected()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "系统保护的作业区不可删除");
        }
        
        // TODO: 检查是否有关联的用户、点位等
        
        workAreaMapper.deleteById(id);
        
        log.info("删除作业区成功：id={}", id);
    }

    /**
     * 转换为 DTO
     */
    private WorkAreaDTO convertToDTO(WorkArea workArea) {
        WorkAreaDTO dto = new WorkAreaDTO();
        dto.setId(workArea.getId());
        dto.setWorkAreaName(workArea.getWorkAreaName());
        dto.setWorkAreaCode(workArea.getWorkAreaCode());
        dto.setCompanyId(workArea.getCompanyId());
        dto.setLeaderName(workArea.getLeaderName());
        dto.setLeaderPhone(workArea.getLeaderPhone());
        dto.setGeographicRange(workArea.getGeographicRange());
        dto.setMaxCapacity(workArea.getMaxCapacity());
        dto.setDescription(workArea.getDescription());
        dto.setIsSystemProtected(workArea.getIsSystemProtected());
        dto.setCreatedAt(workArea.getCreatedAt());
        dto.setUpdatedAt(workArea.getUpdatedAt());
        
        // 查询公司名称
        if (workArea.getCompanyId() != null) {
            var company = companyMapper.selectById(workArea.getCompanyId());
            if (company != null) {
                dto.setCompanyName(company.getCompanyName());
            }
        }
        
        return dto;
    }
}
