package fun.gatsby.sbimgutil;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private List<Choice> choices;

    // 构造函数、getter和setter

    @Data
    public static class Choice {
        private int index;
        private Message message;
        private String finishReason;

        // 构造函数、getter和setter

        @Data
        public static class Message {
            private String role;
            private String content;

            // 构造函数、getter和setter
        }
    }
}
