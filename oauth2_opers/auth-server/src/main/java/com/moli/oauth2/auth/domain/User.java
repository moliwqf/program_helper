package com.moli.oauth2.auth.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author moli
 * @time 2024-04-12 16:55:44
 * @description 用户
 */
@Data
@TableName("t_user")
public class User {
    private Integer id;
    private String username;
    private String password;
}
