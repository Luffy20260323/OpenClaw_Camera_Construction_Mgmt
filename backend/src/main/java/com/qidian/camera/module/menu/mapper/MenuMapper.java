package com.qidian.camera.module.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qidian.camera.module.menu.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
    
    @Select("SELECT m.* FROM menus m " +
            "LEFT JOIN user_menu_permissions ump ON m.id = ump.menu_id AND ump.user_id = #{userId} " +
            "LEFT JOIN role_menu_permissions rmp ON m.id = rmp.menu_id " +
            "LEFT JOIN user_roles ur ON ur.user_id = #{userId} " +
            "WHERE (ump.can_view = TRUE OR (ump.id IS NULL AND rmp.can_view = TRUE AND ur.role_id = rmp.role_id)) " +
            "AND m.is_visible = TRUE " +
            "ORDER BY m.sort_order")
    List<Menu> selectVisibleMenusByUserId(Long userId);
}
