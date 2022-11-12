package com.shanjupay.transaction.api.service;

import com.shanjupay.common.domain.BusinessException;
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
}
