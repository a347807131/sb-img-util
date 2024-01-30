package fun.gatsby.sbimgutil.utils;


import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class GJCoolOcrApiResultVO {
    @JSONField(name = "FileName")
    String fileName;
    @JSONField(name = "ContentType")
    String contentType;
    @JSONField(name = "CharNumber")
    int charNumber;
    @JSONField(name = "LineNumber")
    int lineNumber;

    @JSONField(name = "Width")
    int width;
    @JSONField(name = "Height")
    int height;

    @JSONField(name = "Size")
    int size;
    @JSONField(name = "Layout")
    int layout;
    @JSONField(name = "Area")
    String area;

    @JSONField(name = "Compact")
    int compact;
    String[] chars;
    int[][] coors;
    @JSONField(name = "char_probs")
    int[] charProbs;
    @JSONField(name = "char_ids")
    int[] charIds;
    @JSONField(name = "line_ids")
    int[] lineIds;
    int[][] layer;
    Map<String,Float>[] option;
    String text;

    @Data
    static class OptionNode{
        String c;
        float prop;
    }
}
