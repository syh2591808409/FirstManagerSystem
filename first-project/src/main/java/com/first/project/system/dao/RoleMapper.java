package com.first.project.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.first.project.system.domain.Role;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {
    List<Role> findUserRole(String userName);
}
