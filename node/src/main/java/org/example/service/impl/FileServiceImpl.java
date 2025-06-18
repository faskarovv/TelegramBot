package org.example.service.impl;

import org.example.entity.AppPhoto;
import org.example.entity.BinaryContent;
import org.example.exception.UploadFileException;
import org.example.repo.AppPhotoRepo;
import org.json.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.AppDocument;
import org.example.repo.AppDocumentRepo;
import org.example.repo.BinaryContentRepo;
import org.example.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AppPhotoRepo appPhotoRepo;
    @Value("${bot.token}")
    private String botToken;
    @Value("${file-service.file-info.uri}")
    private String filePathUri;
    @Value("${file-service.download.uri}")
    private String downloadUrl;

    private final AppDocumentRepo appDocumentRepo;
    private final BinaryContentRepo binaryContentRepo;
    private final AppPhotoRepo photoRepo;

    @Override
    public AppDocument processDoc(Message externalMessage) {
        Document telegramDocument = externalMessage.getDocument();
        String fileId = telegramDocument.getFileId();

        ResponseEntity<String> response = getFilePath(fileId);

        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistantBinaryContent = getBinaryContent(response);

            AppDocument appDocument = createAppDocument(telegramDocument, persistantBinaryContent);

            return appDocumentRepo.save(appDocument);
        } else {
            throw new UploadFileException("Bad response from telegram service");
        }
    }


    @Override
    public AppPhoto processPhoto(Message externalMessage) {
        PhotoSize telegramPhoto = externalMessage.getPhoto().get(0);
        String fileId = telegramPhoto.getFileId();

        ResponseEntity<String> response = getFilePath(fileId);

        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistantBinaryContent = getBinaryContent(response);

            AppPhoto appPhoto = createAppPhoto(telegramPhoto, persistantBinaryContent);

            return appPhotoRepo.save(appPhoto);
        } else {
            throw new UploadFileException("Bad response from telegram service");
        }
    }

    private BinaryContent getBinaryContent(ResponseEntity<String> response) {
        String filePath = getFilePath(response);
        byte[] downloadedFile = fileDownload(filePath);
        BinaryContent temporaryBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(downloadedFile)
                .build();
        return binaryContentRepo.save(temporaryBinaryContent);
    }

    private static String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());

        return String.valueOf(
                jsonObject.getJSONObject("result")
                        .getString("file-path")
        );
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return template.exchange(
                filePathUri,
                HttpMethod.GET,
                request,
                String.class,
                botToken, fileId
        );
    }

    private byte[] fileDownload(String filePath) {
        String fileUri = downloadUrl.replace("{token}", botToken)
                .replace("{filePath}", filePath);

        URL object = null;

        try {
            object = new URL(fileUri);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try (InputStream stream = object.openStream()) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(object.toExternalForm(), e);
        }
    }

    private AppDocument createAppDocument(Document telegramDocument, BinaryContent persistantBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDocument.getFileId())
                .docName(telegramDocument.getFileName())
                .binaryContent(persistantBinaryContent)
                .mimeType(telegramDocument.getMimeType())
                .fileSize(telegramDocument.getFileSize())
                .build();
    }
    private AppPhoto createAppPhoto(PhotoSize telegramAppPhoto , BinaryContent persistantBinaryContent){
        return AppPhoto.builder()
                .telegramFileId(telegramAppPhoto.getFileId())
                .binaryContent(persistantBinaryContent)
                .fileSize(telegramAppPhoto.getFileSize())
                .build();
    }
}
