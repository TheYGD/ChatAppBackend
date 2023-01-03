package pl.szmidla.chatappbackend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.szmidla.chatappbackend.aws.FileService;
import pl.szmidla.chatappbackend.data.User;
import pl.szmidla.chatappbackend.repository.UserRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@Service
@AllArgsConstructor
@Slf4j
public class ProfileService {

    private UserRepository userRepository;
    private FileService fileService;


    public void uploadUserImage(User user, MultipartFile file) {
        if (file == null) {
            removeUsersImage(user);
        }
        else {
            checkIfImageIsValidElseThrow(user, file);
            if (user.getImageUrl() != null) {
                removeUsersImage(user);
            }
            saveUsersImage(user, file);
        }
        userRepository.save(user);
    }

    private void saveUsersImage(User user, MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        String pathFileName = "users/images/" + user.getId() + "/" + file.getOriginalFilename();
        try {
            fileService.save(pathFileName, metadata, file.getInputStream());
        } catch (IOException e) {
            log.error("User id={} tried to upload image, file.getInputStream() exception", user.getId());
            throw new IllegalArgumentException("File must be an image!");
        }

        user.setImageUrl(pathFileName);
    }

    private void checkIfImageIsValidElseThrow(User user, MultipartFile file) {
        if (file.isEmpty()) {
            log.error("User id={} tried to upload empty file.", user.getId());
            throw new IllegalArgumentException("The file is empty!");
        }
        if (!List.of(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE).contains(file.getContentType())) {
            log.error("User id={} tried to upload {} file.", user.getId(), file.getContentType());
            throw new IllegalArgumentException("File must be an image!");
        }
    }

    private void removeUsersImage(User user) {
        fileService.remove(user.getImageUrl());
        user.setImageUrl(null);
    }
}
