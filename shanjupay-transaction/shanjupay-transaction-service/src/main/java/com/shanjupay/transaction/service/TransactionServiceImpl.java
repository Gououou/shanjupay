package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.merchant.api.service.AppService;
import com.shanjupay.merchant.api.service.MerchantService;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDTO;
import com.shanjupay.transaction.api.service.PayChannelService;
import com.shanjupay.transaction.api.service.TransactionService;
import com.shanjupay.transaction.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author 小郭
 * @version 1.0
 */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    //从配置文件读取支付入口地址
    @Value("${shanjupay.payurl}")
    String payurl;

    /*@Value("${weixin.oauth2RequestUrl}")
    String oauth2RequestUrl;
    @Value("${weixin.oauth2CodeReturnUrl}")
    String oauth2CodeReturnUrl;
    @Value("${weixin.oauth2Token}")
    String oauth2Token;*/

    @Reference
    AppService appService;

    @Reference
    MerchantService merchantService;

    @Autowired
    PayOrderMapper payOrderMapper;

    //@Reference
    //PayChannelAgentService payChannelAgentService;

    @Autowired
    PayChannelService payChannelService;

    /**
     * 生成门店二维码的url
     *
     * @param qrCodeDto@return 支付入口（url），要携带参数（将传入的参数转成json，用base64编码）
     * @throws BusinessException
     */
    @Override
    public String createStoreQRCode(QRCodeDTO qrCodeDto) throws BusinessException {
        //校验商户id和应用id和门店id的合法性
        verifyAppAndStore(qrCodeDto.getMerchantId(),qrCodeDto.getAppId(),qrCodeDto.getStoreId());

        //组装url所需要的数据
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setMerchantId(qrCodeDto.getMerchantId());
        payOrderDTO.setAppId(qrCodeDto.getAppId());
        payOrderDTO.setStoreId(qrCodeDto.getStoreId());
        payOrderDTO.setSubject(qrCodeDto.getSubject());//显示订单标题
        payOrderDTO.setChannel("shanju_c2b");//服务类型，要写为c扫b的服务类型
        payOrderDTO.setBody(qrCodeDto.getBody());//订单内容
        //转成json
        String jsonString = JSON.toJSONString(payOrderDTO);
        //base64编码
        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);

        //目标是生成一个支付入口 的url，需要携带参数将传入的参数转成json，用base64编码
        String url=payurl+ticket;
        return url;
    }

    //私有，校验商户id和应用id和门店id的合法性
    private void verifyAppAndStore(Long merchantId, String appId, Long storeId) {
        //根据 应用id和商户id查询
        Boolean aBoolean = appService.queryAppInMerchant(appId, merchantId);
        if(!aBoolean){
            throw new BusinessException(CommonErrorCode.E_200005);
        }
        //根据 门店id和商户id查询
        Boolean aBoolean1 = merchantService.queryStoreInMerchant(storeId, merchantId);
        if(!aBoolean1){
            throw new BusinessException(CommonErrorCode.E_200006);
        }
    }


}
