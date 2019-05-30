package com.first.project.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.first.project.common.domain.QueryRequest;
import com.first.project.system.domain.SysLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface LogService extends IService<SysLog> {

    IPage<SysLog> findLogs(QueryRequest request, SysLog sysLog);

    void deleteLogs(String[] logIds);
    List<SysLog> findByLogId(String[] logIds);
    @Async
    void saveLog(ProceedingJoinPoint point, SysLog log) throws JsonProcessingException;
}
