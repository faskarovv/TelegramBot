package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.AppFile;
import org.example.entity.AppUser;
import org.example.entity.enums.UserState;
import org.example.exception.UploadFileException;
import org.example.repo.AppFileRepo;
import org.example.repo.AppUserRepo;
import org.example.service.FileService;
import org.example.service.MainService;
import org.example.service.ProducerService;
import org.example.service.enums.ServiceCommands;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.example.service.enums.ServiceCommands.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final AppUserRepo appUserRepo;
    private final FileService fileService;
    private final AppFileRepo appFileRepo;


    @Override
    public void processTextMessage(Update update) {
        var userEmail = update.getMessage().getText();
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
            output = sendEmailToUser(userEmail, appUser);
        } else if (UserState.APPROVED_STATE.equals(getState)) {
            output = processServiceCommands(appUser, textMessage);
        } else if (UserState.WAIT_FOR_DATE.equals(appUser.getUserState())) {
            output = processDateHistory(appUser ,textMessage);
            appUser.setUserState(UserState.APPROVED_STATE);
            appUserRepo.save(appUser);
        } else {
            log.error("Unknown user state : {} ", getState);
            output = "Unknown command use /cancel again ";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(chatId, output);
    }

    private String processDateHistory(AppUser appUser, String textMessage) {
        try{
            int days = Integer.parseInt(textMessage.trim());

            if(days <= 0){
                return "please provide a valid date (for example 2 is going to show archive of past 2 days)";
            }
            Instant endDate = Instant.now();
            Instant startDate = endDate.minus(days , ChronoUnit.DAYS);

            List<AppFile> files = appFileRepo.findAllByAppUserAndUploadedAtBetween(
                    appUser,
                    startDate,
                    endDate
            );

            if (files.isEmpty()){
                return "no files were uploaded";
            }

            files.forEach(file -> {
                String presignedUrl = fileService.generatePresignedUrl(file);
                sendAnswer(appUser.getTelegramBotId() , presignedUrl);
            });

            return "Here are the files you requested" + days;
        } catch (RuntimeException e) {
            return "please just enter a number";
        }
    }

    private String sendEmailToUser(String userEmail, AppUser appUser) {
        String output;
        if (userEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            appUser.setEmail(userEmail);
            appUserRepo.save(appUser);
            producerService.produceEmailReq(userEmail);

            output = "email sent to user please check your email";
        } else {
            output = "provide a valid email!!!!!!!";
        }
        return output;
    }


    @Override
    public void processPhotoMessage(Update update) {
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        var message = update.getMessage();
        if (message.getPhoto() == null) {
            return;
        }
        if (isNotAllowedToSendMessage(appUser, chatId)) {
            return;
        }
        try {
            AppFile processedDocument = fileService.processPhoto(update.getMessage() , appUser);
            String presignedUrl = fileService.generatePresignedUrl(processedDocument);

            var answer = "Document successfully downloaded here is the link \n"
                    + presignedUrl;
            sendAnswer(update.getMessage().getChatId(), answer);
        } catch (UploadFileException e) {
            log.error(e.getMessage());
            String error = "Unfortunately could not generate the photo";
            sendAnswer(update.getMessage().getChatId(), error);
        }
    }

    @Override
    public void processDocMessage(Update update) {
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        var message = update.getMessage();
        if (message.getDocument() == null) {
            return;
        }
        if (isNotAllowedToSendMessage(appUser, chatId)) {
            return;
        }
        try {
            AppFile processedPhoto = fileService.processDoc(update.getMessage() , appUser);
            String presignedUrl = fileService.generatePresignedUrl(processedPhoto);

            var answer = "Photo successfully downloaded here is the link \n"
                    + presignedUrl;

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


    private String processServiceCommands(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            return changeUserState(appUser);
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Hello! in order to see all the command refer to /help";
        } else if (HISTORY.equals(cmd)) {
            if (appUser.getUserState().equals(UserState.APPROVED_STATE)) {
                return history(appUser);
            }
            return "user should be approved";
        } else if (HISTORY_RANGE.equals(cmd)) {
            return changeUserStateHistory(appUser);
        } else {
            return "not a valid command";
        }
    }

    private String changeUserStateHistory(AppUser appUser) {
        appUser.setUserState(UserState.WAIT_FOR_DATE);
        appUserRepo.save(appUser);
        return "How many days should I look up";
    }

    private String history(AppUser appUser) {
        List<AppFile> userAppFiles = appFileRepo.findAllByAppUser(appUser);

        if(userAppFiles.isEmpty()){
            return "no files were uploaded";
        }

        userAppFiles.forEach(file -> {
            String presignedUrl = fileService.generatePresignedUrl(file);
            sendAnswer(appUser.getTelegramBotId() , presignedUrl);
        });

        return "Here are the files you uploaded";
    }

    private String changeUserState(AppUser appUser) {
        appUser.setUserState(UserState.WAIT_FOR_EMAIL_STATE);
        appUserRepo.save(appUser);
        return "please provide a valid email";
    }

    private String help() {
        return """
                List of possible commands:\s
                /cancel - cancellation of the current command\s
                /registration - registration of the user\s
                /history - resends the past urls
                /historyRange - resends the past urls depending on date""";
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
                    .isActive(false)
                    .userState(UserState.BASIC_STATE)
                    .build();
            return appUserRepo.save(transientAppUser);
        }

        return existingUser;
    }


    private void sendAnswer(Long chatId, String output) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

}