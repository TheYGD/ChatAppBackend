package pl.szmidla.chatappbackend.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface FileService {

    byte[] download(String pathFileName) throws IOException;

    void save(String pathFileName, Map<String, String> metadata, InputStream inputStream) throws IOException;

    void remove(String pathFileName);
}
