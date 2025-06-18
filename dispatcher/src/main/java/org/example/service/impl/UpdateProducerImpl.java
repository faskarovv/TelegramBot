package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.UpdateProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProducerImpl implements UpdateProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void produce(String rabbitmq, Update update) {
        log.info("Attempting to send message to queue: {}", rabbitmq);
        log.debug("Message content: {}", update.getMessage().getText());
        rabbitTemplate.convertAndSend(rabbitmq , update);
    }
}
