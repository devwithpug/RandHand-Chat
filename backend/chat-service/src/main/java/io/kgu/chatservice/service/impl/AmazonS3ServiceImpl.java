package io.kgu.chatservice.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.kgu.chatservice.service.AmazonS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service {

    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile image, String key) throws IOException {

        File uploadFile = convert(image)
                .orElseThrow(() -> new IllegalArgumentException("Invalid File format"));

        PutObjectRequest request = new PutObjectRequest(bucket, key, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        amazonS3Client.putObject(request);

        uploadFile.delete();

        return amazonS3Client.getUrl(bucket, key).toString();
    }

    @Override
    public String upload(String url, String key) throws IOException {

        URL requestUrl = new URL(url);

        if (ImageIO.read(requestUrl) == null) {
            throw new IllegalArgumentException("Invalid File format : NON-IMAGE FILE");
        }

        File uploadFile = new File("temp.jpg");

        if (uploadFile.createNewFile()) {
            FileUtils.copyURLToFile(requestUrl, uploadFile);
        } else {
            throw new IOException("Could not create new File, internal server error");
        }

        PutObjectRequest request = new PutObjectRequest(bucket, key, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        amazonS3Client.putObject(request);

        uploadFile.delete();

        return amazonS3Client.getUrl(bucket, key).toString();
    }

    @Override
    public void delete(String key) throws AmazonServiceException {

        DeleteObjectRequest request = new DeleteObjectRequest(bucket, key);

        amazonS3Client.deleteObject(request);

    }

    @Override
    public String getUrlFromUserId(String key) {
        return amazonS3Client.getUrl(bucket, key).toString();
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }
}
