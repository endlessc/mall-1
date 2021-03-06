package com.mall.gateway.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.mall.common.auth.JwtBaseUtils;
import com.mall.common.pojo.response.AuthUserVO;
import com.mall.common.util.RedisUtils;
import com.mall.gateway.common.constant.JwtConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * PROJECT: mall
 * DESCRIPTION: jwt utils
 *
 * @author amos
 * @date 2019/7/23
 */
public class JwtUtils extends JwtBaseUtils {

    /**
     * 登录
     */
    public static String sign(AuthUserVO authUserVO, String loginId) {
        Algorithm algorithm = Algorithm.HMAC256(authUserVO.getPassword());
        saveLoginId(authUserVO.getUserId(), loginId);
        saveOperational(authUserVO.getUserId(), authUserVO.getAccount());
        return JWT.create()
                .withClaim(JwtConstant.ACCOUNT, authUserVO.getAccount())
                .withClaim(JwtConstant.USERNAME, authUserVO.getUsername())
                .withClaim(JwtConstant.USER_ID, authUserVO.getUserId())
                .withClaim(JwtConstant.LOGIN_ID, loginId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtConstant.JWT_EXPIRE_TIME))
                .sign(algorithm);
    }

    /**
     * 根据 token 获取 账号
     */
    public static String getAccount(String token) {
        try {
            DecodedJWT decode = JWT.decode(token);
            return decode.getClaim(JwtConstant.ACCOUNT).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 根据 token 获取 用户名字
     */
    public static String getUsername(String token) {
        try {
            DecodedJWT decode = JWT.decode(token);
            return decode.getClaim(JwtConstant.USERNAME).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 根据 token 获取 用户ID
     */
    public static Long getUserId(String token) {
        try {
            DecodedJWT decode = JWT.decode(token);
            return decode.getClaim(JwtConstant.USER_ID).asLong();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 根据 token 获取 用户ID
     */
    public static String getLoginId(String token) {
        try {
            DecodedJWT decode = JWT.decode(token);
            return decode.getClaim(JwtConstant.LOGIN_ID).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 校验账号密码
     */
    public static boolean isLoginExpired(String token, String account, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim(JwtConstant.ACCOUNT, account)
                    .build();
            verifier.verify(token);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 将当前 userId 与 loginId 存入 redis
     *
     * @param userId  用户编号
     * @param loginId 登录ID
     */
    private static void saveLoginId(String userId, String loginId) {
        RedisUtils.setByIndex(JwtConstant.REDIS_KEY_LOGIN_ID + userId, loginId, 1);
    }

    /**
     * 设置n分钟无操作强制退出
     *
     * @param userId  用户编号
     * @param account 用户账号
     */
    public static void saveOperational(String userId, String account) {
        RedisUtils.set(JwtConstant.REDIS_KEY_OPERATIONAL + userId, account, 1, JwtConstant.OPERATIONAL_EXPIRE_TIME);
    }

    /**
     * 判断当前 token 对应 loginId, 与 redis里存储的 loginId 是否一致
     *
     * @param userId 用户ID
     * @param token  请求token
     * @return true loginId错误; false loginId正确
     */
    public static boolean isLoginElsewhere(String userId, String token) {
        String loginId = getLoginId(token);
        if (loginId == null) {
            return false;
        }
        String savedLoginId = RedisUtils.get(JwtConstant.REDIS_KEY_LOGIN_ID + userId, 1);
        if (StringUtils.isBlank(savedLoginId)) {
            return false;
        }
        return !savedLoginId.equals(loginId);
    }

}
