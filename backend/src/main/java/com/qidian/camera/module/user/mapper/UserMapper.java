package com.qidian.camera.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据 ID 查询用户（关联公司信息）
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @Select("SELECT u.*, c.company_name, c.type_id " +
            "FROM users u " +
            "LEFT JOIN companies c ON u.company_id = c.id " +
            "WHERE u.id = #{id}")
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT u.*, c.company_name, c.type_id " +
            "FROM users u " +
            "LEFT JOIN companies c ON u.company_id = c.id " +
            "WHERE u.username = #{username}")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User selectByPhone(@Param("phone") String phone);
}
