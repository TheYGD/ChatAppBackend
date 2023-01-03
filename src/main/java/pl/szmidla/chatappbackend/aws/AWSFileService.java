package pl.szmidla.chatappbackend.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.szmidla.chatappbackend.service.FileService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


@Profile("aws")
@Service
@RequiredArgsConstructor
@Slf4j
public class AWSFileService implements FileService {

    @Value("${aws.bucket}")
    private String bucketName;
    private final S3Client s3;


    public byte[] download(String pathFileName) throws IOException {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(pathFileName)
                .build();

        try {
            return s3.getObject(objectRequest).readAllBytes();
        } catch (IOException e) {
            log.error("Failed to download file from s3, pathFileName={}", pathFileName);
            throw e;
        }
    }

    public void save(String pathFileName,
                     Map<String, String> metadata, InputStream inputStream) throws IOException {

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(pathFileName)
                .metadata(metadata != null ? metadata : Map.of())
                .build();

        try {
            s3.putObject(objectRequest, RequestBody.fromBytes(inputStream.readAllBytes()));
        } catch (IOException e) {
            log.error("Failed to save file to s3, pathFileName={}", pathFileName);
            throw e;
        }
    }

    public void remove(String pathFileName) {
        DeleteObjectRequest objectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(pathFileName)
                .build();
        s3.deleteObject(objectRequest);
    }
}
