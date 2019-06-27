package com.cognizant.dss.entities;

import java.util.HashMap;

public final class FileDB {

    // Essentially a table to represent a SQL table with Unique keys assigned to each filename
    private HashMap<String, String> fileSystem = new HashMap<>();

    public Boolean storeFile(String fileName, String key){
        if(fileSystem.containsValue(fileName)){
           // Reject due to duplicate names 
           return false;
        }
        if(fileSystem.containsKey(key)){
            return false;
        }
        fileSystem.put(key, fileName);
        return true;

    }

    public Boolean deleteFile(String key){
        if(fileSystem.containsKey(key)){
            fileSystem.remove(key);
            return true;
        }
        return false;
    }

    public String getFileName(String key){
        if(fileSystem.containsKey(key)){
            return fileSystem.get(key);
        } else {
            return ("FILE NOT FOUND!");
        }
    }

    public Boolean fileExists(String key){
        if(fileSystem.containsKey(key)){
            return true;
        }
        return false;
    }

}