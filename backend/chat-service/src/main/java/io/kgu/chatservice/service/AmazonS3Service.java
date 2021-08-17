package io.kgu.chatservice.service;

import java.io.IOException;

public interface AmazonS3Service {

    String upload(Object image, String key) throws IOException;
    String upload(String url, String key) throws IOException;
    void delete(String key);

}
