package pl.szmidla.chatappbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pl.szmidla.chatappbackend.aws.AWSFileService;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.repository.UserRepository;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    AWSFileService fileService;
    @InjectMocks
    ProfileService profileService;


    private User createUser(String username, String email, String password) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();
        return user;
    }

    @Test
    void uploadUserImageFirstImage() throws IOException {
        User user = Mockito.spy(createUser("user", "sth@email.com", "password"));
        MultipartFile file = Mockito.spy(new MockMultipartFile("my-file.png", "my-file.png",
                MediaType.IMAGE_PNG_VALUE, new byte[]{1,2,3}));

        profileService.uploadUserImage(user, file);

        verify( fileService ).save( any(), any(), any() );
        verify( fileService, times(0) ).remove( user.getImageUrl() );
        verify( userRepository ).save( user );
    }

    @Test
    void uploadUserImageWithRemovePrevious() throws IOException {
        User user = Mockito.spy(createUser("user", "sth@email.com", "password"));
        String expectedUrl = "some/url/prev.jpg";
        user.setImageUrl(expectedUrl);
        MultipartFile file = Mockito.spy(new MockMultipartFile("my-file.png", "my-file.png",
                MediaType.IMAGE_PNG_VALUE, new byte[]{1,2,3}));


        profileService.uploadUserImage(user, file);

        verify( fileService ).save( any(), any(), any() );
        verify( fileService ).remove( expectedUrl );
        verify( user ).setImageUrl( null );
        verify( userRepository ).save( user );
    }

    @Test
    void uploadUserImageEmptyFile() {
        User user = Mockito.spy(createUser("user", "sth@email.com", "password"));
        user.setImageUrl("some/url");
        MultipartFile file = new MockMultipartFile("file", new byte[0]);

        Throwable exception = assertThrows( IllegalArgumentException.class,
                () -> profileService.uploadUserImage(user, file) );

        assertEquals( "The file is empty!", exception.getMessage() );
        verify( userRepository, times(0) ).save( user );
    }

    @Test
    void uploadUserImageNotSupportedExtension() {
        User user = Mockito.spy(createUser("user", "sth@email.com", "password"));
        user.setImageUrl("some/url");
        MultipartFile file = Mockito.spy(new MockMultipartFile("file", new byte[]{1,2,3}));
        when( file.getContentType() ).thenReturn( "my/own");

        Throwable exception = assertThrows( IllegalArgumentException.class,
                () -> profileService.uploadUserImage(user, file) );

        assertEquals( "File must be an image!", exception.getMessage() );
        verify( userRepository, times(0) ).save( user );
    }

    @Test
    void uploadUserImageNull() {
        User user = Mockito.spy(createUser("user", "sth@email.com", "password"));
        String expectedUrl = "some/url";
        user.setImageUrl(expectedUrl);
        MultipartFile file = null;
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when( fileService ).remove( stringCaptor.capture() );

        profileService.uploadUserImage(user, file);
        String removedUrl = stringCaptor.getValue();

        assertEquals( expectedUrl, removedUrl );
        verify( fileService ).remove( expectedUrl );
        verify( user ).setImageUrl( null );
        verify( userRepository ).save( user );
    }
}