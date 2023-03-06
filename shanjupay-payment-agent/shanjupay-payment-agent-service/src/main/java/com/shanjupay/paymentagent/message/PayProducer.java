package com.shanjupay.paymentagent.message;
import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;

import javax.annotation.Resource;


@Slf4j
@Component
public class PayProducer {
    private final String TOPIC_ORDER ="TP_PAYMENT_ORDER";

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /*** 发送支付状态消息 * @param result */
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

    //订单结果 主题
    private static final String TOPIC_RESULT ="TP_PAYMENT_RESULT";
    //发送消息（支付结果）
    public void payResultNotice(PaymentResponseDTO paymentResponseDTO){
        rocketMQTemplate.convertAndSend(TOPIC_RESULT,paymentResponseDTO);
        log.info("支付渠道代理服务向mq支付结果消息：{}", JSON.toJSONString(paymentResponseDTO));
    }

}
