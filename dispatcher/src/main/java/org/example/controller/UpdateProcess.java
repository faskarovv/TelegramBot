package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.RabbitMqQueue;
import org.example.service.UpdateProducer;
import org.example.utils.MessageUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateProcess {

    private  TelegramBot telegramBot;
    private final UpdateProducer updateProducer;

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        log.info("Processing update: {}", update);
        if (update == null) {
            log.error("Received update is null");
            return;
        }

        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.error("Received unsupported message  " + update);
        }

    }

    private void distributeMessageByType(Update update) {

        var message = update.getMessage();
        if (message.hasText()) {
            processTextMessage(update);
        } else if (message.hasDocument()) {
            processDocMessage(update);
        } else if (message.hasPhoto()) {
            processPhotoMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }

    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendMessage = MessageUtils.generateSendMessageWithText(update, "Not supported type");
        setView(sendMessage);
    }

    private void setFileIsReceivedView(Update update) {
        var sendMessage = MessageUtils.generateSendMessageWithText(update, "Great! Wait until the process ends");
        setView(sendMessage);
    }


    private void processPhotoMessage(Update update) {
        updateProducer.produce(RabbitMqQueue.PHOTO_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(RabbitMqQueue.DOC_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
        log.info("chatId {}" , update.getMessage().getChatId());
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(RabbitMqQueue.TEXT_MESSAGE_UPDATE, update);
    }
}
