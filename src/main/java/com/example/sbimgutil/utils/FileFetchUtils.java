package com.example.sbimgutil.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;

public class FileFetchUtils {

    public static void fetchFileRecursively(List<File> container, File dir) {
        fetchFileRecursively(container, dir, null);
    }

    public static void fetchFileRecursively(List<File> container, File dir, FileFilter filter) {
        File[] files = dir.listFiles(filter);
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory())
                fetchFileRecursively(container,file,filter);
            else
                container.add(file);
        }
    }

    public static int countFileRecursively(File dir){
        return countFileRecursively(dir,null);
    }

    public static int countFileRecursively(File dir, FilenameFilter filter){
        int count = 0;
        File[] files = dir.listFiles(filter);
        if (files==null) return count;
        for (File file : files) {
            if(file.isDirectory())
                count+=countFileRecursively(file,filter);
            else
                count++;
        }
        return count;
    }
}
