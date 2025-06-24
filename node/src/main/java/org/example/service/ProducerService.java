package org.example.service;

import org.hibernate.sql.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface ProducerService {
    void produceAnswer(SendMessage sendMessage);
    void produceEmailReq(String email);
}
