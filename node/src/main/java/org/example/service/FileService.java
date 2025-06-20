package org.example.service;

import org.example.entity.AppFile;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppFile processDoc(Message externalMessage);
    AppFile processPhoto(Message externalMessage);
}
