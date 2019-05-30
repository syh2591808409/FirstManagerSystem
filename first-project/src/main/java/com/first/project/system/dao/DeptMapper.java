package com.first.project.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.first.project.system.domain.Dept;

public interface DeptMapper extends BaseMapper<Dept> {
    /**
     * 递归删除部门
     *
     * @param deptId deptId
     */
    void deleteDepts(String deptId);
}
