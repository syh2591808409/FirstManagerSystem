package com.first.project.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.first.project.system.domain.LoginLog;

public interface LoginLogService extends IService<LoginLog> {

    void saveLoginLog (LoginLog loginLog);
}