package com.shanjupay.paymentagent.service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import com.shanjupay.paymentagent.api.service.PayChannelAgentService;
import com.shanjupay.paymentagent.common.constant.AliCodeConstants;
import com.shanjupay.paymentagent.message.PayProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 小郭
 * @version 1.0
 */
@Slf4j
@Service
public class PayChannelAgentServiceImpl implements PayChannelAgentService {

    @Autowired
    private PayProducer payProducer;

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
            //发送支付结果查询延迟消息
            PaymentResponseDTO<AliConfigParam> notice = new PaymentResponseDTO<AliConfigParam>();
            notice.setOutTradeNo(alipayBean.getOutTradeNo());
            notice.setContent(aliConfigParam);
            notice.setMsg("ALIPAY_WAP");
            payProducer.payOrderNotice(notice);
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


    /**
     * 查询支付宝交易订单状态
     * @param aliConfigParam 支付渠道参数
     * @param outTradeNo 闪聚平台订单号
     * @return
     */
    @Override
    public PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam, String outTradeNo) throws BusinessException {
        String url = aliConfigParam.getUrl();//支付宝接口网关地址
        String appId = aliConfigParam.getAppId();//支付宝应用id
        String rsaPrivateKey = aliConfigParam.getRsaPrivateKey();//应用私钥
        String format = aliConfigParam.getFormat();//json格式
        String charest = aliConfigParam.getCharest();//编码
        String alipayPublicKey = aliConfigParam.getAlipayPublicKey();//支付宝公钥
        String signtype = aliConfigParam.getSigntype();//签名算法
        String returnUrl = aliConfigParam.getReturnUrl();//支付成功跳转的url
        String notifyUrl = aliConfigParam.getNotifyUrl();//支付结果异步通知的url

        //构造sdk的客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(url, appId, rsaPrivateKey, format, charest, alipayPublicKey, signtype); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeWapPayModel model  = new AlipayTradeWapPayModel();
        model.setOutTradeNo(outTradeNo);//商户的订单，就是闪聚平台的订单
        request.setBizModel(model);

        AlipayTradeQueryResponse response = null;
        try {
            //请求支付宝订单状态查询接口
            response = alipayClient.execute(request);
            //支付宝响应的code，10000表示接口调用成功
            String code = response.getCode();
            if(AliCodeConstants.SUCCESSCODE.equals(code)){
                String tradeStatusString = response.getTradeStatus();
                //解析支付宝返回的状态，解析成闪聚平台的TradeStatus
                TradeStatus tradeStatus = covertAliTradeStatusToShanjuCode(tradeStatusString);
                //String tradeNo(支付宝订单号), String outTradeNo（闪聚平台的订单号）, TradeStatus tradeState（订单状态）, String msg（返回信息）
                return PaymentResponseDTO.success(response.getTradeNo(),response.getOutTradeNo(),tradeStatus,response.getMsg());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //String msg, String outTradeNo, TradeStatus tradeState
        return PaymentResponseDTO.fail("支付宝订单状态查询失败",outTradeNo,TradeStatus.UNKNOWN);
    }

    //解析支付宝的订单状态为闪聚平台的状态
    private TradeStatus covertAliTradeStatusToShanjuCode(String aliStatusString) {
        switch (aliStatusString){
            case AliCodeConstants.TRADE_FINISHED:
            case AliCodeConstants.TRADE_SUCCESS:
                return TradeStatus.SUCCESS;//业务交易支付 明确成功
            case AliCodeConstants.TRADE_CLOSED:
                return TradeStatus.REVOKED;//交易已撤销
            case    AliCodeConstants.WAIT_BUYER_PAY:
                return TradeStatus.USERPAYING;//交易新建，等待支付
            default:
                return TradeStatus.FAILED;//交易失败
        }
    }
}
