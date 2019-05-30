package com.first.project.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_user_role")
public class UserRole implements Serializable {
    private static final long serialVersionUID = 5985506977603480949L;

    private Long userId;

    private Long roleId;
}
