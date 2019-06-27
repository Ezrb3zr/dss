package com.cognizant.dss.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import com.cognizant.dss.utilities.KeyGenerator;
import com.cognizant.dss.services.FileSystemStorageService;
import com.cognizant.dss.services.StorageFileNotFoundException;

@Controller
@RequestMapping("/storage/documents")
public class FileUploadController {

    @Autowired
    private final FileSystemStorageService storageService;

    @Autowired
    public FileUploadController(FileSystemStorageService storageService, KeyGenerator keyGenerator) {
        this.storageService = storageService;
    }

    @GetMapping("/{key:.+}")
    @ResponseBody
    public ResponseEntity<?> serveFile(@PathVariable String key) {
        if(storageService.fileExists(key)){
            Resource file = storageService.loadAsResource(key);
            return ResponseEntity.ok().header(HttpHeaders.ACCEPT,
                "attachment; file=\"" + file.getFilename() + "\"").contentType(MediaType.TEXT_PLAIN).body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        String key = KeyGenerator.generateFileKey();
        while(storageService.fileExists(key)){
            //Keeps creating new keys until we get a unique one
            key = KeyGenerator.generateFileKey();
        }
        storageService.store(file, key);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.TEXT_PLAIN).body(key);
    }

    @DeleteMapping("/{key:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String key) {
        if(storageService.fileExists(key)){
            if(storageService.delete(key)){
                return ResponseEntity.noContent().build();
            } 
            return ResponseEntity.status(500).body("Something went wrong in an attempt to delete the file");
        }
        return ResponseEntity.badRequest().body("Invalid Key");
    }


    @PutMapping("/")
    public ResponseEntity<?> replaceFile(@RequestParam("file") MultipartFile file, @RequestParam("key") String key){
        if(storageService.fileExists(key)){
            storageService.replace(file, key);
        }
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
