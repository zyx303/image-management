package com.zyx.image.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginResponseVO {
    private String token;
    private UserVO user;
}

