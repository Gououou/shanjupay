package com.shanjupay.transaction.api.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDTO;

/**
 * @author 小郭
 * @version 1.0
 */

/**
 * 交易订单相关服务接口
 */
public interface TransactionService {

    /**
     * 生成门店二维码
     * @param qrCodeDto，传入merchantId,appId、storeid、channel、subject、body
     * @return 支付入口URL，将二维码的参数组成json并用base64编码
     * @throws BusinessException
     */
    String createStoreQRCode(QRCodeDTO qrCodeDto) throws BusinessException;

    /**
     * 保存支付宝订单，1、保存订单到闪聚平台，2、调用支付渠道代理服务调用支付宝的接口
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException;

    /**
     * 更新订单支付状态
     * @throws BusinessException
     */
    void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state) throws BusinessException;
}
