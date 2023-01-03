package pl.szmidla.chatappbackend.aws;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AWSFileServiceTest {

    @Mock
    S3Client s3;
    @InjectMocks
    AWSFileService fileService;


    @Test
    void downloadSuccess() throws IOException {
        String pathFileName = "asd/asd/file";
        byte[] expectedArray = new byte[]{1,1,9};
        ResponseInputStream<GetObjectResponse> inputStream = Mockito.mock(ResponseInputStream.class);
        ArgumentCaptor<GetObjectRequest> objectRequestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        when( inputStream.readAllBytes() ).thenReturn( expectedArray );
        when( s3.getObject(objectRequestCaptor.capture()) ).thenReturn( inputStream );

        byte[] responseArray = fileService.download(pathFileName);
        GetObjectRequest objectRequest = objectRequestCaptor.getValue();

        assertEquals( expectedArray, responseArray );
        assertEquals( pathFileName, objectRequest.key() );
    }

    @Test
    void downloadException() throws IOException {
        String pathFileName = "asd/asd/file";
        ResponseInputStream<GetObjectResponse> inputStream = Mockito.mock(ResponseInputStream.class);
        when( inputStream.readAllBytes() ).thenThrow( new IOException() );
        when( s3.getObject((GetObjectRequest) any()) ).thenReturn( inputStream );

        assertThrows( IOException.class, () -> fileService.download(pathFileName) );
    }

    @Test
    void saveSuccess() throws IOException {
        String pathFileName = "asd/asd/file";
        Map<String, String> metadata = new HashMap<>();
        byte[] expectedArray = new byte[]{2,2,1,8};
        InputStream inputStream = Mockito.mock(InputStream.class);
        ArgumentCaptor<PutObjectRequest> objectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        when( inputStream.readAllBytes() ).thenReturn( expectedArray );
        when( s3.putObject(objectRequestCaptor.capture(), (RequestBody) any())).thenReturn( null );

        fileService.save(pathFileName, metadata, inputStream);
        PutObjectRequest objectRequest = objectRequestCaptor.getValue();

        assertEquals( pathFileName, objectRequest.key() );
        assertEquals( metadata, objectRequest.metadata() );
    }

    @Test
    void saveException() throws IOException {
        String pathFileName = "asd/asd/file";
        Map<String, String> metadata = new HashMap<>();
        InputStream inputStream = Mockito.mock(InputStream.class);
        when( inputStream.readAllBytes() ).thenThrow( new IOException() );

        assertThrows( IOException.class, () -> fileService.save(pathFileName, metadata, inputStream) );
    }

    @Test
    void remove() {
        String pathFileName = "asd/asd/file";
        ArgumentCaptor<DeleteObjectRequest> objectRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        when( s3.deleteObject(objectRequestCaptor.capture()) ).thenReturn( null );

        fileService.remove(pathFileName);
        DeleteObjectRequest objectRequest = objectRequestCaptor.getValue();

        assertEquals( pathFileName, objectRequest.key() );
    }
}