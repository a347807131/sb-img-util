package com.example.sbimgutil;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;

@Slf4j
public class FileTest {
    @Test
    public void t1() {

        File file = new File("D:");

        log.debug("{}", file.exists());
    }
}
