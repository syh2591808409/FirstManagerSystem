package com.first.project.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.first.project.common.domain.QueryRequest;
import com.first.project.system.domain.Role;
import com.first.project.system.domain.User;

import java.util.List;

public interface RoleService extends IService<Role> {
    IPage<Role> findRoles(Role role, QueryRequest request);

    List<Role> findUserRole(String userName);

    Role findByRoleId(String roleId);

    List<Role> findByRoleIds(String[] roleIds);

    Role findByName(String roleName);

    void createRole(Role role);

    void deleteRoles(String[] roleIds) throws Exception;

    void updateRole(Role role) throws Exception;
}
