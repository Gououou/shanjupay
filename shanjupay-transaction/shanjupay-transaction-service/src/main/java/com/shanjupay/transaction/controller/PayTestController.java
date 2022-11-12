package com.shanjupay.transaction.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 小郭
 * @version 1.0
 */
@Slf4j
@Controller
//@RestController//请求方法响应统一json格式
public class PayTestController {

    //应用id
    String APP_ID = "2021000121653267";
    //应用私钥
    String APP_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCI7NqDS2EfNpOsN3C/XML2K0alJzfT31g3rdBoxNWzV2EbLG1ZggX6uccTCczPQnYhh5LIpGgzK+1GwLiFbTip35x0uP84kE6B8eh27HMY3rgi5AI0Dp6WXVbGskzEh2ywJ0FEdu1MRrFrNnH6myHBiytpOtCYC26LOEXaVSSu2PUbqhjarInKc504JB4jIl+g7M0FNJ85cUcKIX+XWnrTGOg1f3/nKjtoqWpcFXCG1m9ncL7lry8lnbGNmCvCYJ7DAcReT/P/+SwHsiA6qHHJUVooO4JOKg64xe9DCmiUfU0yKVU773r3d4gFTk6fga2F092CnUti/eqjWEMA2Xy7AgMBAAECggEAOemr9lJAjtoflXyRdG7Aiyv8oke3JibWAh7DAj0WgdcYXEzrJyuGaeh/mb7QYG0k18OmwrQ+2j3fCsjigpSSnc7VNs00LXFjszRk9T/G4qL40NENWYVBcCmkVT/+ljUNNGg2A8YlUdiom8cYaD2LBnhdcRGOPnC/XaT8bn8Kl+TNk3yd1SrW9+2skuTRGXl143ucetFgNF/Z5rBM2mmhsUByEDXzzZMRRVEXNLG4EMj5re4PDOojQaUK1N/BlK8KSPighD0DZqQ1krcqXOaOBReHxWkqqngrZQFd9qZ6qZWHFObuoquVi9TN9NqUKrVB3lIMfBZps169xjIJQQmDCQKBgQDZBp/4Bw+09y7c0l/dVY3CdKe1ddiYyvDrGO4JznzRYX9+gwhuYt7hvFkHuLuL5NjMP2VzCPtWIk5L2z9t1IGIHuA3bEPMyhOqulTdw2D65d4iHtEALCuLX873Qwbb+V/gPKeGdhCgRxsKCA73bHhGOm0CDulrVyGy1gSAp56yHwKBgQChg76F670njNd0GPN0wBKvW7VQPmlJgv6bQY6/mQVymE3c/NIAXuj5hw3zADcYhctWFjG5wY66fdEdgy8+Dx0YnhBqBJZpngWILvHgk+tZj0Wyu0P1XjYhehOLNuWH5SxJ5/oTxB97il9NPOvqcGAS4FgZw04CVKaTnPi9Htz55QKBgCWns5vKRdxlQvlHeyChFn6V1P90GgewtopkAfErpZaCrydZPt5AQ2GxV1UypT/JjiAqI8UI2ur/+svIl8HLrEnc6ZRUMZNXwH/P4qeznaEKvzb2V9cK0udU0UkvlQIXy53RiFOZKptUmwlrioU9icTNoFpKEg/wx0ULQy67ZpqLAoGALvBAoF7gO+w8eSr/JcDkLT4ztTxOEOUW5TvqJlhsMt98UWrQBPeQMWREc4huepv3ee89SESuHBiQe0kyo5B03kCheI4YwF0CMo+iD6gtT3vJv6WYnGhixtvZbi3oCtRZSKk4DiKDG74c8C9ySFGcOiAkumf99cNwHZoS17NueNkCgYA7G01D+wPUYl+2VZj26Aq8EFhN5w+PKD8Uz7tZeiMCFyd8Vvy57iBYkWkAVwsMw338Pfwrmm0VHrgnfRzDQNB5LIDS6mu0oFFAF6XKjfcqxCPIOeCdpe0GlSqv+cRFgg30xc49OpOUaRtcDF9Et8YFb3c/t24YICMPP75FVSccKg==";
    //支付宝公钥
    String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiINu9rhYN+ViIUBaj6lQW6nw+BU/gIbOQCkJu5bcyt1ukmX1b1etbmCTEjX/QfJwrIreT6H4hROGwSA6UWR68SHNKr8TKQ4XG5XynVLjAVJR+nGaidK3gVIR+RRCam+c6bc6w7+0nnX4DCDjz2+gHEz7u7S7OBojDqVmlXGAY0yHQ08OJJ3HJERpTPulE4GDjh5eu8H6GBuG4iSO3NFIq93hol7D5gma+3slsn58OBbWMRonsdM01TBuBR1XaSn1xS+RcUKpbL9JOtX7wSOqkfun7FNSsyrBxcKjmfcKgoxlY/WzcLPXCneIRCr/Rxx7XWQQn3lAY4fv3MAMmSDBHQIDAQAB";
    String CHARSET = "UTF-8";
    //支付宝接口的网关地址，正式"https://openapi.alipay.com/gateway.do"
    String URL = "https://openapi.alipaydev.com/gateway.do";
    //签名算法类型
    String sign_type = "RSA2";

//    @GetMapping("/alipaytest")
    @RequestMapping(value = "/alipaytest", produces = {"text/html;charset=UTF-8"},method = RequestMethod.GET)
    public void alipaytest(HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse) throws ServletException, IOException {
        //构造sdk的客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, "JSON", CHARSET, ALIPAY_PUBLIC_KEY, sign_type); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
//        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"20221111010100550\"," +
                " \"total_amount\":\"2655500\"," +
                " \"subject\":\"GTR\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form="";
        try {
            //请求支付宝下单接口,发起http请求
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();



    }

}