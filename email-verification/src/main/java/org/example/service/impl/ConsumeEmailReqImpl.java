package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.ConsumeEmailReq;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static org.example.RabbitMqQueue.EMAIL_SEND_QUEUE;
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumeEmailReqImpl implements ConsumeEmailReq {

    private final EmailSendImpl emailSend;

    @Override
    @RabbitListener(queues = EMAIL_SEND_QUEUE )
    public void consumeEmailReq(String email) {
        log.info("Attempting to consume them email {}" , email);
        emailSend.sendEmail(email);
    }
}
