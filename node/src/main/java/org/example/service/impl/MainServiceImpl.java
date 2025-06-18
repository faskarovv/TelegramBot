package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.entity.AppUser;
import org.example.entity.RawData;
import org.example.entity.enums.UserState;
import org.example.exception.UploadFileException;
import org.example.repo.AppUserRepo;
import org.example.repo.RawDataRepo;
import org.example.service.FileService;
import org.example.service.MainService;
import org.example.service.ProducerService;
import org.example.service.enums.ServiceCommands;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.example.service.enums.ServiceCommands.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final RawDataRepo rawDataRepo;
    private final ProducerService producerService;
    private final AppUserRepo appUserRepo;
    private final FileService fileService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var getState = appUser.getUserState();
        var textMessage = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommands.fromValue(textMessage);

        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (UserState.BASIC_STATE.equals(getState)) {
            output = processServiceCommands(appUser, textMessage);
        } else if (UserState.WAIT_FOR_EMAIL_STATE.equals(getState)) {
            //TODO after implementing the registration
            output = "waiting for confirmation";
        } else {
            log.error("Unknown user state : {} ", getState);
            output = "Unknown command use /cancel again ";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(chatId, output);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        if (isNotAllowedToSendMessage(appUser, chatId)) {
            return;
        }
        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            var answer = "Document succesufuly downloaded here is the link \n"
                    + "httptpptptptptppt";
            sendAnswer(update.getMessage().getChatId(), answer);
        } catch (UploadFileException e) {
            log.error(e.getMessage());
            String error = "Unfortunately could not generate the photo";
            sendAnswer(update.getMessage().getChatId(), error);
        }
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        if (isNotAllowedToSendMessage(appUser, chatId)) {
            return;
        }
        try {
            AppDocument appDocument = fileService.processDoc(update.getMessage());

            //TODO add a way to generate the link

            var answer = "Photo successfully downloaded here is the link \n"
                    + "httptpptptptptppt";

            sendAnswer(update.getMessage().getChatId(), answer);
        } catch (UploadFileException e) {
            log.error(e.getMessage());
            String error = "Unfortunately could not generate the file";
            sendAnswer(update.getMessage().getChatId(), error);
        }
    }

    private boolean isNotAllowedToSendMessage(AppUser appUser, Long chatId) {
        var userState = appUser.getUserState();
        if (!appUser.getIsActive()) {
            var error = "please register in order to post your content";
            sendAnswer(chatId, error);
            return true;
        } else if (UserState.BASIC_STATE.equals(userState)) {
            var error = "please register in order to post your content or activate your acc";
            sendAnswer(chatId, error);
            return true;
        }
        return false;
    }


    private void sendAnswer(Long chatId, String output) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommands(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            //todo add registration
            return "temperately accessed";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Hello! in order to see all the command refer to /help";
        } else {
            return "not a valid command";
        }

    }

    private String help() {
        return """
                List of possible commands:\s
                /cancel - cancellation of the current command\s
                /registration - registration of the user""";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setUserState(UserState.BASIC_STATE);
        appUserRepo.save(appUser);
        return "command canceled";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser existingUser = appUserRepo.findAppUserByTelegramBotId(telegramUser.getId());

        if (existingUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramBotId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO after implementing registration change this variable
                    .isActive(true)
                    .userState(UserState.BASIC_STATE)
                    .build();
            return appUserRepo.save(transientAppUser);
        }

        return existingUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .update(update)
                .build();

        rawDataRepo.save(rawData);
    }

}
