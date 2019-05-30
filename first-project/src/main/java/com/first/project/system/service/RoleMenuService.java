package com.first.project.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.first.project.system.domain.RoleMenu;

import java.util.List;

public interface RoleMenuService extends IService<RoleMenu> {
    void deleteRoleMenusByRoleId(String[] roleIds);

    void deleteRoleMenusByMenuId(String[] menuIds);

    List<RoleMenu> getRoleMenusByRoleId(String roleId);
}
