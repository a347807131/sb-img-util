package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import fun.gatsby.sbimgutil.ChatRequest;
import fun.gatsby.sbimgutil.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DeepseekApiTask extends BaseTask{
    private static final String API_URL="https://api.deepseek.com/chat/completions";

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    public DeepseekApiTask(File inFile, File outFile, Map<String,Object> configMap) {
        super(inFile, outFile, configMap);
    }
    @Override
    public void doWork() throws Throwable {

        String apiKey = configMap.get("apiKey").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        ChatRequest.Message message = new ChatRequest.Message();
        message.setRole("user");
        message.setContent("将下列给出的语音转录文本编写为一篇正式的文章。");
        var m2=new ChatRequest.Message("assistant",getText());
        ChatRequest request = new ChatRequest();
        request.setModel("deepseek-chat");
        request.setMessages(List.of(message,m2));

        HttpEntity<ChatRequest> requestEntity = new HttpEntity<>(request, headers);

        ChatResponse chatResponse = REST_TEMPLATE.exchange(
                API_URL,
                HttpMethod.POST,
                requestEntity,
                ChatResponse.class
        ).getBody();
        var list = chatResponse.getChoices().stream().map(e->e.getMessage().getContent())
                .toList();
        FileUtil.writeLines(list,outFile, StandardCharsets.UTF_8);
    }

    public String getText() throws IOException {
        var  lines = Files.readAllLines(inFile.toPath());
        //过滤纯数字
        return lines.stream().filter(e->!e.strip().matches( "^\\d+$"))
                .filter(StrUtil::isNotEmpty)
                .filter(e->!e.contains("-->"))
                .collect(Collectors.joining("\n"));
    }
}
