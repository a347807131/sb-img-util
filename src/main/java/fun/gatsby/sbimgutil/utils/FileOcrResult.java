package fun.gatsby.sbimgutil.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class FileOcrResult {
    /**
     * 文件名
     */
    String name;
    /**
     * 顺序号，从0开始
     */
    int order;
    int remainAmount;
    int size;
    int timeTake;
    Page page;

    public static FileOcrResult parse(GJCoolOcrApiResult gJCoolOcrApiResult) {

        FileOcrResult ocrResult = new FileOcrResult();
        ocrResult.name=gJCoolOcrApiResult.getFileName();
        ocrResult.size=gJCoolOcrApiResult.getSize();

        Page page = new Page();
        page.direction=gJCoolOcrApiResult.getLayout() == 1 ? "H" : "V";
        page.width=gJCoolOcrApiResult.getWidth()+"";
        page.height=gJCoolOcrApiResult.getHeight()+"";
        gJCoolOcrApiResult.
    }

    @Data
    public static class Page {
        /**
         * V H
         */
        String direction;
        String width;
        String height;
        List<Line> lines;

    /**
     * 提取纯文本
     * @return
     */
    public String toString() {
        if (CollUtil.isEmpty(lines)) {
            return "";
        }
        return lines.stream().map(Line::toString).collect(Collectors.joining(StrUtil.LF));
    }
//        public String extractRawText(){
//            StringBuilder out = new StringBuilder();
//            Comparator<Line> lineComparator;
//            if(StringUtils.equals("V",this.getDirection()))
//                lineComparator= FileOcrResult.Page.Line::compareToWhenVertical;
//            else
//                lineComparator= FileOcrResult.Page.Line::compareToWhenHorizen;
//            var sortedLines = this.getLines().stream().sorted(
//                    lineComparator
//            ).collect(Collectors.toList());
//            sortedLines.forEach(e->{
//                List<FileOcrResult.Page.Line.Word> words = e.getWords();
//                words.forEach(word-> out.append(word.getText()));
//            });
//            return out.toString();
//        }

        @Data
        public static class Line{
            List<Word> words;
            int[] position;

            @Override
            public String toString() {
                Line line=this;
                StringBuilder sb = new StringBuilder();
                List<Word> bigWords = line.getWords().stream().filter(word -> "Big".equals(word.getWordFlag())).collect(Collectors.toList());
                List<Word> smallLeftWords = line.getWords().stream().filter(word -> "LeftSmall".equals(word.getWordFlag())).collect(Collectors.toList());
                List<Word> smallRightWords = line.getWords().stream().filter(word -> "RightSmall".equals(word.getWordFlag())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(bigWords)) {
                    sb.append(bigWords.stream().map(Word::getText).collect(Collectors.joining()));
                }
                if (CollUtil.isNotEmpty(smallRightWords)) {
                    sb.append("[").append(smallRightWords.stream().map(Word::getText).collect(Collectors.joining()));
                }
                if (CollUtil.isNotEmpty(smallLeftWords)) {
                    sb.append(smallLeftWords.stream().map(Word::getText).collect(Collectors.joining())).append(")");
                }
                return sb.toString();
            }

            public int compareToWhenVertical(Line o) {
                Line e1 = this;
                Line e2 = o;
                int xGap = e1.position[0] - e2.position[0];
                if (xGap != 0)
                    return -xGap;
                int yGap = e1.position[1] - e2.position[1];
                return yGap;
            }

            public int compareToWhenHorizen(Line o) {
                Line e1 = this;
                Line e2 = o;
                int yGap = e1.position[1] - e2.position[1];
                if (yGap != 0)
                    return yGap;
                int xGap = e1.position[0] - e2.position[0];
                return xGap;
            }

            @Data
            public static class Word {
                String text;
                int score;
                String choices;
                Double[] scores;
                String WordType;
                String WordFlag;
                int[] position;
            }
        }
    }
}
