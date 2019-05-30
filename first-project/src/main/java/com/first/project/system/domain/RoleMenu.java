package com.first.project.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_role_menu")
public class RoleMenu implements Serializable {
    private static final long serialVersionUID = -8209179952576701350L;

    private Long roleId;

    private Long menuId;
}
