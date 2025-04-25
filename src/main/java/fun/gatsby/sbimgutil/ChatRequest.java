package fun.gatsby.sbimgutil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class ChatRequest {
    private String model;
    private List<Message> messages;
    
    // 构造函数、getter和setter

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
        
        // 构造函数、getter和setter
    }
}

