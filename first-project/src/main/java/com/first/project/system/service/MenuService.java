package com.first.project.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.first.project.system.domain.Menu;
import com.first.project.system.domain.Role;

import java.util.List;
import java.util.Map;

public interface MenuService extends IService<Menu> {
    List<Menu> findUserPermissions(String username);

    List<Menu> findUserMenus(String username);

    List<Menu> findByMenuIds(String[] menuIds);

    Map<String, Object> findMenus(Menu menu);

    List<Menu> findMenuList(Menu menu);

    void createMenu(Menu menu);

    void updateMenu(Menu menu) throws Exception;

    /**
     * 递归删除菜单/按钮
     *
     * @param menuIds menuIds
     */
    void deleteMeuns(String[] menuIds) throws Exception;

}
