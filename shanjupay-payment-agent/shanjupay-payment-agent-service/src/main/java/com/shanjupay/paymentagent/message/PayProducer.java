package com.shanjupay.paymentagent.message;

import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 小郭
 * @version 1.0
 */
@Slf4j
@Component
public class PayProducer {

    //消息Topic
    private static final String TOPIC_ORDER = "TP_PAYMENT_ORDER";

    private static final String TOPIC_RESULT = "TP_PAYMENT_RESULT";

    @Resource
    private RocketMQTemplate rocketMQTemplate;


    public void payOrderNotice(PaymentResponseDTO result) {
        log.info("支付通知发送延迟消息:{}", result);
        try {
            //处理消息存储格式
            Message<PaymentResponseDTO> message = MessageBuilder.withPayload(result).build();
            SendResult sendResult = rocketMQTemplate.syncSend(TOPIC_ORDER, message, 1000, 3);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 发送支付结果消息
     * @param result
     */
    public void payResultNotice(PaymentResponseDTO result){
        rocketMQTemplate.convertAndSend(TOPIC_RESULT,result);
    }
}
