package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import fun.gatsby.sbimgutil.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatApiTask extends BaseTask<ChatApiTask.Config>{
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    @Data
    public static class Config {
        String apiKey;
        String model;
    }

    public ChatApiTask(File inFile, File outFile, Config config) {
        super(inFile, outFile, config);
    }
    @Override
    public void doWork() throws Throwable {
        var list  = new GeminiApi().call();
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

    class GeminiApi implements IApi{
        static final String API_KEY = "AIzaSyA31UwZXXJS9mUpY7FTCIXZe-fkW24q0Qc";
        static final String MODEL = "gemini-2.5-flash-preview-04-17";
         public String getApiUrl(){
            return "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s"
                   .formatted(config.model==null? MODEL:config.model,API_KEY);
        }

        @Override
        public HttpEntity<?> newRequestEntity(String content) throws IOException {
            Param param = new Param();
            Param.Content.Part part1 = new Param.Content.Part();
            part1.text=getPromote();
            Param.Content.Part part2 = new Param.Content.Part();
            part2.text=getText();
            param.contents=List.of(new Param.Content(List.of(part1,part2)));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new HttpEntity<>(param, headers);
        }

        public String getPromote(){
             return
             """
             将下列给出的语音转录文本编写为篇偏学术的文章。
             """;
        }

        public List<String> call() throws IOException {
             var requestEntity = newRequestEntity(getText());
            var  ret=REST_TEMPLATE.postForObject(getApiUrl(),requestEntity,ChatRet.class);
            assert ret != null;

            return ret.getCandidates().stream()
                .map(e->
                    e.getContent().getParts().stream()
                        .map(e1->e1.getText())
                        .collect(Collectors.joining("\n"))
                ).toList();
//            String outText = ret.getCandidates().get(0).getContent().getParts().get(0).getText();
//            return List.of(outText);
        }

        @Data
         public static class Param {
             List<Content> contents;
             @Data
             @NoArgsConstructor
             @AllArgsConstructor
            public static class Content {
                 List<Part> parts;
                 @AllArgsConstructor
                 @NoArgsConstructor
                 @Data
                 public static class Part {
                     String text;
                 }
             }
        }
        @Data
        public static class ChatRet{
            private List<Candidate> candidates;

            @Data
            public static class Candidate{
                private Content content;
                @Data
                public static class Content{
                    private List<Part> parts;
                    @Data
                    public static class Part{
                        private String text;
                    }
                }
            }
        }
    }

    class DeepseekApi implements IApi{
        static final String API_URL="https://api.deepseek.com/chat/completions";
        static final String API_KEY="sk-237d194906e3476fb8249e89ce9366de";

        @Override
        public List<String> call() throws IOException {
            return null;
        }

        @Override
        public String getApiUrl() {
            return API_URL;
        }

        @Override
        public HttpEntity<?> newRequestEntity(String content) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);
            ChatRequest.Message message = new ChatRequest.Message();
            message.setRole("user");
            message.setContent(getPromote());
            ChatRequest.Message m2= null;
            try {
                m2 = new ChatRequest.Message("assistant",getText());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            ChatRequest request = new ChatRequest();
            request.setModel("deepseek-chat");
            if(config.model!=null)
                request.setModel(config.model);
            request.setMessages(List.of(message,m2));
            return new HttpEntity<>(request, headers);
        }

        @Override
        public String getPromote() {
            return
                """
                将下列给出的语音转录文本编写为篇偏学术的文章。
                """;
        }
    }

    interface IApi{
        List<String> call() throws IOException;
        String getApiUrl();
        HttpEntity<?> newRequestEntity(String content) throws IOException;
        String getPromote();
    }
}
