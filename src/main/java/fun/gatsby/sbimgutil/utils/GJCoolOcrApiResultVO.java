package fun.gatsby.sbimgutil.utils;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.*;

@Data
public class GJCoolOcrApiResultVO {
    @JsonProperty( "FileName")
    String fileName;
    @JsonProperty( "ContentType")
    String contentType;
    @JsonProperty( "CharNumber")
    int charNumber;
    @JsonProperty( "LineNumber")
    int lineNumber;

    @JsonProperty( "Width")
    int width;
    @JsonProperty( "Height")
    int height;

    @JsonProperty( "Size")
    int size;
    @JsonProperty( "Layout")
    int layout;
    @JsonProperty( "Area")
    String area;

    @JsonProperty( "Compact")
    int compact;
    String[] chars;
    int[][] coors;
    @JsonProperty( "char_probs")
    float[] charProbs;
    @JsonProperty( "char_ids")
    int[] charIds;
    @JsonProperty( "line_ids")
    int[] lineIds;
    int[][] Layer;
    @JsonProperty( "coor_probs")
    float[] coorProbs;
    List<LinkedHashMap<String,Float>> option;
    String text;


    @Data
    static class OptionNode{
        String c;
        float prop;
    }

    public FileOcrResultVO toPageOcrResult(){
        ArrayList<FileOcrResultVO.Page.Line.Word> words = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            var aChar=chars[i].substring(0,1);
            FileOcrResultVO.Page.Line.Word word = new FileOcrResultVO.Page.Line.Word();
            word.setText(aChar);
            word.setPosition(coors[i]);
            word.setScore((int) charProbs[i]*100);

            Set<String> keySet = option.get(i).keySet();
            //合并为字符串
            word.setChoices(String.join("", keySet));
            Collection<Float> values = option.get(i).values();
            word.setScores(values.stream().mapToDouble(Float::doubleValue).toArray());
            word.setWordFlag("RightSmall");
            words.add(word);
        }

        ArrayList<FileOcrResultVO.Page.Line> lines = new ArrayList<>();

        for (int wordNum = 0; wordNum < lineIds.length; wordNum++) {
            int lineNum = lineIds[wordNum];
            FileOcrResultVO.Page.Line line =  null;
            try {
                line=lines.get(lineNum);
            }catch (IndexOutOfBoundsException e){
                lines.add(lineNum,new FileOcrResultVO.Page.Line());
                line=lines.get(lineNum);
            }

            List<FileOcrResultVO.Page.Line.Word> wordsF = line.getWords();
            if (wordsF==null){
                wordsF=new ArrayList<>();
                line.setWords(wordsF);
            }
            line.getWords().add(words.get(wordNum));
        }

        for (FileOcrResultVO.Page.Line line : lines) {
            List<FileOcrResultVO.Page.Line.Word> wordsF = line.getWords();
            int[] position = wordsF.get(0).getPosition();
            int[] position1 = wordsF.get(wordsF.size() - 1).getPosition();
            line.setPosition(new int[]{position[0],position[1],position1[2],position1[3]});
        }

        FileOcrResultVO.Page page = new FileOcrResultVO.Page();
        page.setLines(lines);
        page.setDirection("V");
        page.setWidth(String.valueOf(width));
        page.setHeight(String.valueOf(height));

        FileOcrResultVO vo = new FileOcrResultVO();
        vo.setName(fileName);
        vo.setPage(page);
        vo.setSize(1);
        vo.setOrder(0);
        return vo;
    }
}
