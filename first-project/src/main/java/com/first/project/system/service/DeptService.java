package com.first.project.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.first.project.common.domain.QueryRequest;
import com.first.project.system.domain.Dept;
import com.first.project.system.domain.Menu;

import java.util.List;
import java.util.Map;

public interface DeptService extends IService<Dept> {
    Map<String, Object> findDepts(QueryRequest request, Dept dept);

    List<Dept> findDepts(Dept dept, QueryRequest request);

    Dept findByDeptId(Long deptId);

    List<Dept> findByDeptIds(String[] deptIds);

    void createDept(Dept dept);

    void updateDept(Dept dept);

    void deleteDepts(String[] deptIds);
}
