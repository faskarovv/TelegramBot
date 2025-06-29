package org.example.service;

import org.example.entity.AppFile;
import org.example.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppFile processDoc(Message externalMessage, AppUser appUser);
    AppFile processPhoto(Message externalMessage, AppUser appUser);

    String generatePresignedUrl(AppFile appFile);
}
