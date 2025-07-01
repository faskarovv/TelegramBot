package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.controller.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramBotConfig {



    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) throws TelegramApiException {
        SetWebhook setWebhook = SetWebhook.builder()
                .url(telegramBot.getBotUri())
                .build();

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        log.info("setting up connection");
        botsApi.registerBot(telegramBot , setWebhook);
        return botsApi;
    }
}
