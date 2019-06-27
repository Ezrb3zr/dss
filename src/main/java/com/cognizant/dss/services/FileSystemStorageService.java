package com.cognizant.dss.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.dss.entities.FileDB;

@Service
public class FileSystemStorageService {

    private static final FileDB fileDb = new FileDB();

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    public void store(MultipartFile file, String key) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + fileName);
            }
            if (fileName.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + fileName);
            }
            try (InputStream inputStream = file.getInputStream()) {
                
                fileDb.storeFile(fileName, key);

                Files.copy(inputStream, this.rootLocation.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file " + fileName, e);
        }
    }

    public Path load(String key) {
        String fileName = fileDb.getFileName(key);
        if(fileName.equals("FILE NOT FOUND!")){
            
        }
        return rootLocation.resolve(fileName);
    }

    public Resource loadAsResource(String key) {
        try {
            Path file = load(key);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + key);
            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + key, e);
        }
    }

    public void replace(MultipartFile file, String key){
        if(delete(key)){
            store(file, key);
        } else {
            System.out.println("Invalid key");
            throw new StorageFileNotFoundException("Invalid key or failed to delete previous file.");
        }
    }

    public Boolean delete(String key){
        if(fileDb.fileExists(key)){
            String fileName = fileDb.getFileName(key);
            Path path = rootLocation.resolve(fileName);
            try {
                fileDb.deleteFile(key);
                return FileSystemUtils.deleteRecursively(path);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public Boolean fileExists(String key){
        return fileDb.fileExists(key);
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
