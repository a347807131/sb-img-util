package com.example.sbimgutil.utils;

import java.util.Set;

public interface Const {
    String TIF_DIR_NAME="1 扫描原图（TIFF格式）";


    Set<String> SUPORTTED_FORMATS = Set.of(
            "jp2", "jpg","tif","tiff","gif","png","bmp","jpeg","jpe", "webp","ico","svg"
    );
}
