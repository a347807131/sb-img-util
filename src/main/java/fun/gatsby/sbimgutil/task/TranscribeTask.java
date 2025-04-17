package fun.gatsby.sbimgutil.task;

import com.alibaba.fastjson2.JSON;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

public class TranscribeTask extends BaseTask{
    record TranscribeResult(
            String filename,
            String subtitle
    ){ }

    private final String apiUrl;

    public TranscribeTask(File inFile, File outFile, Map<String,Object> configMap) {
        super(inFile, outFile, configMap);
        this.apiUrl=configMap.get("apiUrl").toString();
    }
    @Override
    public void doWork() throws Throwable {
        var restTemplate =(RestTemplate) configMap.get("restTemplate");
// 2. 准备文件资源
        FileSystemResource fileResource = new FileSystemResource(inFile);
        // 3. 设置请求体，构建 multipart/form-data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource); // "file" 是服务器端接收文件的参数名
        // 4. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 5. 创建 HTTP 实体
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        var res = restTemplate.postForEntity(new URI(apiUrl), requestEntity, TranscribeResult.class);
        TranscribeResult ret = res.getBody();
        assert ret != null;
        Files.writeString(outFile.toPath(),ret.subtitle());
    }
}
