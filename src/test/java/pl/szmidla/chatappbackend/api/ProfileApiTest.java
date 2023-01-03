package pl.szmidla.chatappbackend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.data.dto.ProfileInfo;
import pl.szmidla.chatappbackend.service.ProfileService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfileApiTest {

    @Mock
    ProfileService profileService;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ProfileApi profileApi = new ProfileApi(profileService);
        mockMvc = MockMvcBuilders.standaloneSetup(profileApi).build();
    }


    @Test
    void getOwnAccountInfo() throws Exception {
        String path = "/api/profile/info";
        User user = createUser("username", "email@email.com", "password");
        addUserToSecurityContext(user);
        ProfileInfo expectedObject = ProfileInfo.fromUser(user);
        String expectedJson = objectMapper.writeValueAsString(expectedObject);

        String actualResponse = mockMvc.perform( get(path) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType(MediaType.APPLICATION_JSON) )
                .andReturn().getResponse().getContentAsString();

        assertEquals( expectedJson, actualResponse );
    }

    @Test
    void uploadUserImage() throws Exception {
        String path = "/api/profile/image";
        User user = createUser("username", "email@email.com", "password");
        addUserToSecurityContext(user);
        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        MockMultipartFile file = new MockMultipartFile("file", new byte[1]);
        doNothing().when( profileService ).uploadUserImage( any(), fileCaptor.capture() );

        mockMvc.perform(
                        multipart(path)
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                )
                .andExpect( status().isOk() )
                .andReturn().getResponse().getContentAsString();

        MultipartFile actualFile = fileCaptor.getValue();
        assertEquals( file, actualFile );
    }

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
    private void addUserToSecurityContext(User user) {
        Authentication auth = Mockito.spy(Authentication.class);
        when( auth.getPrincipal() ).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}