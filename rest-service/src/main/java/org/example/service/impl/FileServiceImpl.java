package org.example.service.impl;

import org.example.entity.AppDocument;
import org.example.entity.AppPhoto;
import org.example.entity.BinaryContent;
import org.example.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public AppDocument getDocument(String id) {
        return null;
    }

    @Override
    public AppPhoto getPhoto(String id) {
        return null;
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        return null;
    }
}
