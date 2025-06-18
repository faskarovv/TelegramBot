package org.example.service;

import org.example.entity.AppPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.example.entity.AppDocument;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
    AppPhoto processPhoto(Message externalMessage);
}
