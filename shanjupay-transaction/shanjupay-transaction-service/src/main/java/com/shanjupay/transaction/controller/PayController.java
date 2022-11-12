package com.shanjupay.transaction.controller;

/**
 * @author 小郭
 * @version 1.0
 */

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.ParseURLPairUtil;
import com.shanjupay.merchant.api.service.AppService;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.http.HttpServletRequest;

/**
 * 支付相关接口
 * @author Administrator
 * @version 1.0
 **/
@Slf4j
@Controller
public class PayController {

    @Autowired
    TransactionService transactionService;

    @Reference
    AppService appService;


    /**
     * 支付入口
     * @param ticket  传入数据，对json数据进行的base64编码
     * @param request
     * @return
     */
    @RequestMapping("/pay-entry/{ticket}")
    public String payEntry(@PathVariable("ticket")String ticket, HttpServletRequest request) throws Exception {
        //1、准备确认页面所需要的数据
        String jsonString = EncryptUtil.decodeUTF8StringBase64(ticket);
        //将json串转成对象
        PayOrderDTO payOrderDTO = JSON.parseObject(jsonString, PayOrderDTO.class);
        //将对象的属性和值组成一个url的key/value串
        String params = ParseURLPairUtil.parseURLPair(payOrderDTO);
        //2、解析客户端的类型（微信、支付宝）
        //得到客户端类型
        BrowserType browserType = BrowserType.valueOfUserAgent(request.getHeader("user-agent"));
        switch (browserType){
            case ALIPAY:
                //转发到确认页面
                return "forward:/pay-page?"+params;
            case WECHAT:
                //先获取授权码，申请openid，再到支付确认页面
//                return transactionService.getWXOAuth2Code(payOrderDTO);
                return null;
            default:

        }
        //不支持客户端类型，转发到错误页面
        return "forward:/pay-page-error";
    }
}
