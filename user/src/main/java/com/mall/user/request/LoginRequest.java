package com.mall.user.request;

import lombok.Data;

import java.io.Serializable;

/**
 * PROJECT: user
 * DESCRIPTION: note
 *
 * @author Daoyuan
 * @date 2018/12/10
 */
@Data
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = -8562669854109829185L;

    /**
     * 手机号
     */
    private String phoneNo;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 密码
     */
    private String password;
}