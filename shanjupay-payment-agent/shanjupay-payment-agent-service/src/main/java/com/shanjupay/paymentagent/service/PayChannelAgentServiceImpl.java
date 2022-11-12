package com.shanjupay.paymentagent.service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.service.PayChannelAgentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

/**
 * @author 小郭
 * @version 1.0
 */
@Slf4j
@Service
public class PayChannelAgentServiceImpl implements PayChannelAgentService {

    /**
     * 调用支付宝手机WAP下单接口
     * @param aliConfigParam 支付渠道参数
     * @param alipayBean 请求支付参数
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) throws BusinessException {
        //支付宝渠道参数
        String gateway = aliConfigParam.getUrl();//支付宝下单接口地址
        String appId = aliConfigParam.getAppId();//appid
        String rsaPrivateKey = aliConfigParam.getRsaPrivateKey();//私钥
        String format = aliConfigParam.getFormat();//数据格式json
        String charest = aliConfigParam.getCharest();//字符编码
        String alipayPublicKey = aliConfigParam.getAlipayPublicKey(); //公钥
        String signtype = aliConfigParam.getSigntype();//签名算法类型
        String notifyUrl = aliConfigParam.getNotifyUrl();//支付结果通知地址
        String returnUrl = aliConfigParam.getReturnUrl();//支付完成返回商户地址
        //支付宝sdk客户端
        AlipayClient client = new DefaultAlipayClient(gateway, appId, rsaPrivateKey, format, charest, alipayPublicKey, signtype);
        // 封装请求支付信息
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(alipayBean.getOutTradeNo());//闪聚平台订单
        model.setSubject(alipayBean.getSubject());//订单标题
        model.setTotalAmount(alipayBean.getTotalAmount());//订单金额
        model.setBody(alipayBean.getBody());//订单内容
        model.setTimeoutExpress(alipayBean.getExpireTime());//订单过期时间
        model.setProductCode(alipayBean.getProductCode());//商户与支付宝签定的产品码，固定为QUICK_WAP_WAY
        alipayRequest.setBizModel(model);//请求参数集合
        String jsonString = JSON.toJSONString(alipayBean);
        log.info("createPayOrderByAliWAP..alipayRequest:{}",jsonString);
        // 设置异步通知地址
        alipayRequest.setNotifyUrl(notifyUrl);
        // 设置同步地址
        alipayRequest.setReturnUrl(returnUrl);
        try {
            // 调用SDK提交表单
            AlipayTradeWapPayResponse response = client.pageExecute(alipayRequest);
            log.info("支付宝手机网站支付预支付订单信息" + response);
            PaymentResponseDTO res = new PaymentResponseDTO();
            res.setContent(response.getBody());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_400002);//支付宝确认支付失败
        }
    }
}
