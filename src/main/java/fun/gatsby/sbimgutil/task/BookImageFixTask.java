package fun.gatsby.sbimgutil.task;

import cn.hutool.core.lang.Assert;
import fun.gatsby.sbimgutil.context.PyApiProcessManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

// TODO: 8/23/2023  
@Slf4j
public class BookImageFixTask extends BaseTask{
    static String url="http://127.0.0.1:8868";
   static final String API_ADDRESS=  "/img_process/book_image_fix";

    static RestTemplate restTemplate=new RestTemplate();
    File inFile;
//
    static {
//        url=PyApiProcessManager.getInstance().getPyApiServerUrl()+API_ADDRESS;
    }

    public BookImageFixTask(File inFile, File outFile) {
        this.inFile = inFile;
        this.outFile = outFile;
        this.name="修复图片:"+inFile.getAbsolutePath();
    }

    @Override
    public void doWork() throws Throwable {
        var bytes = reqApi();
        Files.write(outFile.toPath(), bytes);
    }

    private byte[] reqApi() {
        File file = inFile;
        // 1、封装请求头
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("multipart/form-data");
        headers.setContentType(type);
        headers.setContentLength(file.length());
        headers.setContentDispositionFormData("media", file.getName());
        // 2、封装请求体
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        FileSystemResource resource = new FileSystemResource(file);
        param.add("image", resource);
        // 3、封装整个请求报文
        HttpEntity<MultiValueMap<String, Object>> formEntity = new HttpEntity<>(param, headers);
        ResponseEntity<byte[]> respe = restTemplate.postForEntity(url, formEntity,
                byte[].class
        );
        Assert.equals(respe.getStatusCode(), HttpStatus.OK);
        return respe.getBody();
    }
}
