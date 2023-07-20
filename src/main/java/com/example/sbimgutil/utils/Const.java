package com.example.sbimgutil.utils;

import java.io.FileFilter;
import java.util.List;
import java.util.Set;

public interface Const {

    List<String> SUPORTTED_FORMATS = List.of(
            "jpg", "jp2", "tif", "tiff", "gif", "png", "bmp", "jpeg", "jpe", "webp", "ico", "svg"
    );

    FileFilter SUPPORTED_FILE_FILTER = file -> {
        if (file.isDirectory())
            return true;
        String lowerCasedName = file.getName().toLowerCase();
        return Const.SUPORTTED_FORMATS.stream().anyMatch(lowerCasedName::endsWith);
    };
}
