package io.kgu.chatservice.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AmazonS3Service {

    String upload(MultipartFile image, String key) throws IOException;
    String upload(String url, String key) throws IOException;
    void delete(String key);
    String getUrlFromUserId(String key);

}
