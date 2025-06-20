package org.example.service.impl;

import org.example.entity.AppFile;
import org.example.exception.UploadFileException;
import org.example.repo.AppFileRepo;
import org.json.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${bot.token}")
    private String botToken;
    @Value("${file-service.file-info.uri}")
    private String filePathUri;
    @Value("${file-service.download.uri}")
    private String downloadUrl;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Value("${app.s3.presigned-expiration-minutes}")
    private int expirationTime;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AppFileRepo appFileRepo;


    @Override
    public AppFile processDoc(Message externalMessage) {
        Document document = externalMessage.getDocument();
        String fileId = document.getFileId();

        ResponseEntity<String> response = getFileJson(fileId);

        if (response.getStatusCode() == HttpStatus.OK) {
            String filePath = getFileJson(response);
            byte[] documentContent = fileDownload(filePath);

            String s3Key = UUID.randomUUID() + "_" + document.getFileName();
            putToS3(s3Key, documentContent);

            AppFile appFile = AppFile.builder()
                    .telegramFileId(fileId)
                    .s3Key(s3Key)
                    .fileName(document.getFileName())
                    .fileSize(document.getFileSize())
                    .mimeType(document.getMimeType())
                    .build();

            return appFileRepo.save(appFile);
        } else {
            log.error("could not upload document");
            throw new UploadFileException("could not upload the document");
        }
    }


    @Override
    public AppFile processPhoto(Message externalMessage) {
        PhotoSize photo = externalMessage.getPhoto().get(0);
        String fileId = photo.getFileId();

        ResponseEntity<String> response = getFileJson(fileId);

        if(response.getStatusCode() == HttpStatus.OK){
            String filePath = getFileJson(response);
            byte[] photoContent = fileDownload(filePath);

            String fileName = "telegram_photo_" + fileId + ".jpeg";
            String s3Key = UUID.randomUUID() + "_" + fileName;
            putToS3(s3Key , photoContent);

            AppFile appFile = AppFile.builder()
                    .telegramFileId(fileId)
                    .s3Key(s3Key)
                    .fileName(fileName)
                    .fileSize(Long.valueOf(photo.getFileSize()))
                    .mimeType("jpeg")
                    .build();

            return appFileRepo.save(appFile);
        }else {
            log.error("could not upload photo");
            throw new UploadFileException("could not upload photo");
        }
    }

    private void putToS3(String s3Key, byte[] file) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file));
    }

    private String generatePresignedUrl(AppFile appFile) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .key(appFile.getS3Key())
                .bucket(bucketName)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(expirationTime))
                .build();

        URL presignedUrl = s3Presigner.presignGetObject(getObjectPresignRequest).url();

        return presignedUrl.toString();
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

    private static String getFileJson(ResponseEntity<String> response) {
        assert response.getBody() != null;
        JSONObject jsonObject = new JSONObject(response.getBody());

        return String.valueOf(
                jsonObject.getJSONObject("result")
                        .getString("file-path")
        );
    }

    private ResponseEntity<String> getFileJson(String fileId) {
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
}
