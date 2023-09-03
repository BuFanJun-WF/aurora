package com.aurora.consumer;

import com.alibaba.fastjson.JSON;
import com.aurora.model.dto.EmailDTO;
import com.aurora.util.EmailUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import static com.aurora.constant.RabbitMQConstant.EMAIL_QUEUE;

/**
 * 评论通知消费者
 */
@Component
// 用于指定监听的队列名
@RabbitListener(queues = EMAIL_QUEUE)
public class CommentNoticeConsumer {

    @Autowired
    private EmailUtil emailUtil;

    @RabbitHandler // 用来处理接收到的消息
    public void process(byte[] data) {
        // 将接收的数据转化未
        EmailDTO emailDTO = JSON.parseObject(new String(data), EmailDTO.class);
        emailUtil.sendHtmlMail(emailDTO);
    }

}
