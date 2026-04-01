package com.qidian.camera.module.pointbatchassignment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.pointbatchassignment.entity.Point;
import com.qidian.camera.module.pointbatchassignment.entity.PointDeviceAssignment;
import com.qidian.camera.module.pointbatchassignment.entity.PointDeviceAssignmentWithPointInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 点位设备分配 Mapper
 */
@Mapper
public interface PointDeviceAssignmentMapper extends BaseMapper<PointDeviceAssignment> {

    /**
     * 查询未分配的点位列表
     *
     * @return 未分配的点位列表
     */
    @Select("SELECT p.*, " +
            "CASE WHEN pda.id IS NOT NULL THEN true ELSE false END AS is_assigned, " +
            "pda.model_instance_id AS assigned_model_instance_id " +
            "FROM points p " +
            "LEFT JOIN point_device_assignments pda ON p.id = pda.point_id " +
            "WHERE pda.id IS NULL " +
            "AND p.status = 'active' " +
            "ORDER BY p.id ASC")
    List<Point> selectUnassignedPoints();

    /**
     * 查询点位的设备配置（含点位信息）
     *
     * @param pointId 点位 ID
     * @return 设备分配记录
     */
    @Select("SELECT pda.id, pda.point_id, pda.model_instance_id, pda.assigned_by, pda.assigned_at, " +
            "p.point_name, p.point_code " +
            "FROM point_device_assignments pda " +
            "INNER JOIN points p ON pda.point_id = p.id " +
            "WHERE pda.point_id = #{pointId}")
    PointDeviceAssignmentWithPointInfo selectByPointIdWithPointInfo(@Param("pointId") Long pointId);

    /**
     * 根据点位 ID 列表查询已分配的点位
     *
     * @param pointIds 点位 ID 列表
     * @return 已分配的点位 ID 列表
     */
    @Select("SELECT point_id FROM point_device_assignments WHERE point_id IN " +
            "<foreach item='id' collection='pointIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    List<Long> selectAssignedPointIds(@Param("pointIds") List<Long> pointIds);
}
