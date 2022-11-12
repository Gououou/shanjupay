package com.shanjupay.paymentagent.api.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;

/**
 * @author 小郭
 * @version 1.0
 */
public interface PayChannelAgentService {

    /**
     * 调用支付宝手机WAP下单接口
     * @param aliConfigParam 支付渠道参数
     * @param alipayBean 请求支付参数
     * @return
     * @throws BusinessException
     */
    PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) throws BusinessException;
}
