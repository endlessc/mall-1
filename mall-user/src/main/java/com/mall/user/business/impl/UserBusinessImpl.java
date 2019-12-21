package com.mall.user.business.impl;

import com.mall.common.response.GenericResponse;
import com.mall.common.util.DesSecretUtil;
import com.mall.common.util.RandomUtil;
import com.mall.user.business.UserBusiness;
import com.mall.user.common.constant.UserConstant;
import com.mall.user.common.enums.UserExceptionEnum;
import com.mall.user.common.pojo.request.LoginRequest;
import com.mall.user.common.pojo.response.LoginSuccessVO;
import com.mall.user.dao.entity.UserEntity;
import com.mall.user.dao.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PROJECT: user
 * DESCRIPTION: note
 *
 * @author Daoyuan
 * @date 2018/12/11
 */
@Component("userBusiness")
public class UserBusinessImpl implements UserBusiness {

    @Resource
    private UserMapper userMapper;

    @Override
    public GenericResponse register(LoginRequest loginRequest) {
        Optional<UserEntity> userOptional = userMapper.findByAccount(loginRequest.getAccount());

        if (userOptional.isPresent()) {
            return new GenericResponse(UserExceptionEnum.REGISTER_ACCOUNT_EXISTED);
        }

        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(loginRequest, userEntity);
        String pwd = loginRequest.getPassword();
        String salt = RandomUtil.generateString(8);
        String encryptPwd = DesSecretUtil.encrypt(pwd, salt);
        userEntity.setSalt(salt);
        userEntity.setPassword(encryptPwd);
        userEntity.setCreateTime(LocalDateTime.now());
        UserEntity entity = userMapper.save(userEntity);

        return new GenericResponse<>(generateLoginSuccessVO(entity));
    }

    @Override
    public GenericResponse login(LoginRequest loginRequest) {
        Optional<UserEntity> userOptional = userMapper.findByAccount(loginRequest.getAccount());

        if (!userOptional.isPresent()) {
            return new GenericResponse(UserExceptionEnum.LOGIN_ACCOUNT_NOT_FOUND);
        }
        UserEntity userEntity = userOptional.get();
        String encryptPwd = DesSecretUtil.encrypt(loginRequest.getPassword(), userEntity.getSalt());
        if (!userEntity.getPassword().equals(encryptPwd)) {
            return new GenericResponse(UserExceptionEnum.LOGIN_PASSWORD_ERROR);
        }

        return new GenericResponse<>(generateLoginSuccessVO(userEntity));
    }

    private LoginSuccessVO generateLoginSuccessVO(UserEntity userEntity) {
        LoginSuccessVO successVO = new LoginSuccessVO();
        BeanUtils.copyProperties(userEntity, successVO);
        successVO.setToken(DesSecretUtil.encrypt(userEntity.getId(), UserConstant.TOKEN_SECRET));
        return successVO;
    }


}