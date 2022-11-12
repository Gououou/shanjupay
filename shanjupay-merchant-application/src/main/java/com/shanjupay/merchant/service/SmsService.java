package com.shanjupay.merchant.service;

/**
 * @author 小郭
 * @version 1.0
 */

import com.shanjupay.common.domain.BusinessException;

/**
 * <p>
 *     手机短信服务
 * </p>
 */
public interface SmsService {

    /**
     * 获取短信验证码
     * @param phone
     * @return
     */
    String sendMsg(String phone);

    /**
     * 校验验证码，抛出异常则校验无效
     * @param verifiyKey 验证码key
     * @param verifiyCode 验证码
     */
    void checkVerifiyCode(String verifiyKey,String verifiyCode) throws BusinessException;
}
