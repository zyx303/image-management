package com.zyx.image.controller;

import com.zyx.image.dto.ChangePasswordDTO;
import com.zyx.image.dto.LoginDTO;
import com.zyx.image.dto.RegisterDTO;
import com.zyx.image.service.AuthService;
import com.zyx.image.util.JwtUtil;
import com.zyx.image.vo.LoginResponseVO;
import com.zyx.image.vo.Result;
import com.zyx.image.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            UserVO userVO = authService.register(registerDTO);
            return Result.success("注册成功", userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginResponseVO response = authService.login(loginDTO);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserVO userVO = authService.getUserInfo(userId);
            return Result.success(userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<UserVO> updateUserInfo(@RequestBody UserVO userVO, HttpServletRequest request) {
        try {
            String token = extractToken(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserVO result = authService.updateUserInfo(userId, userVO);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO, 
                                       HttpServletRequest request) {
        try {
            String token = extractToken(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            authService.changePassword(userId, changePasswordDTO);
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
    }
    
    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("未找到Token");
    }
}

