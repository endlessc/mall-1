package com.mall.gateway.controller;

import com.mall.gateway.exception.ExceptionEnum;
import com.mall.gateway.exception.GenericException;
import com.mall.gateway.exception.GenericResponse;
import com.mall.gateway.feign.UserFeignClient;
import com.mall.gateway.request.LoginRequest;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * PROJECT: gateway
 * DESCRIPTION: note
 *
 * @author Daoyuan
 * @date 2018/12/11
 */
@RestController
@RequestMapping("user")
public class UserController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    /**
     * 登录类型 - 注册
     */
    private static final String LOGIN_TYPE_REGISTER = "register";

    /**
     * 改用 feign
     * {@code RestTemplate restTemplate;}
     */
    @Resource
    private UserFeignClient userFeignClient;

    /*
     * 注册
     * {
     * 	"type": "register",
     * 	"phoneNo": "18937128861",
     * 	"email": "",
     * 	"password": "666666"
     * }
     * 登录
     * {
     * 	"phoneNo": "18937128861",
     * 	"email": "",
     * 	"password": "666666"
     * }
     */

    /**
     * 登录注册接口
     *
     * @param login         表单
     * @param bindingResult 表单验证
     * @return 通用状态
     * ignoreExceptions: 指定不触发回退的异常
     */
    @HystrixCommand(fallbackMethod = "loginFallback", ignoreExceptions = {GenericException.class})
    @PostMapping("login")
    public GenericResponse login(@Valid @RequestBody LoginRequest login, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new GenericResponse<>(ExceptionEnum.ERROR_PARAM_FORMAT).buildParam(bindingResult.getAllErrors());
        }
        if (StringUtils.isAllBlank(login.getPhoneNo(), login.getEmail())) {
            return new GenericResponse<>(ExceptionEnum.REGISTER_PHONE_EMAIL_BOTH_NULL);
        }
        if (login.getType() != null && LOGIN_TYPE_REGISTER.equalsIgnoreCase(login.getType())) {
            return userFeignClient.register(login);
        } else {
            return userFeignClient.login(login);
        }
    }

    /**
     * 容错配置，如果user服务器挂掉，则进入本方法
     * 注意：参数要和原方法一直，包括后边的 BindingResult
     */
    public GenericResponse loginFallback(LoginRequest login, BindingResult bindingResult, Throwable throwable) {
        LOGGER.error("Fallback catch Exception with: >>> {}", throwable.getMessage());
        return new GenericResponse(ExceptionEnum.SERVICE_BUSY);
    }
}
