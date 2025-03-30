package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.utils.CataParser;
import fun.gatsby.sbimgutil.utils.PdfBookmark;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FiveBackspaceReplaceTask extends BaseTask{

    StringBuilder sbOut=new StringBuilder();

    public FiveBackspaceReplaceTask(File inFile, File outFile, Map<String,Object> configMap) {
        super(inFile, outFile, configMap);
    }

    @Override
    public String getName() {
        return "FiveBackspaceReplaceTask" + outFile;
    }

    @Override
    public void doWork() throws Throwable {
        PdfBookmark pdfBookmark = CataParser.parseTxt(inFile);
        append(pdfBookmark);
        sbOut.delete(sbOut.length()-1,sbOut.length());
        byte[] bom = {(byte) 0xFE,(byte) 0xFF};
        Files.write(outFile.toPath(), bom);
        Files.writeString(outFile.toPath(), sbOut.toString(), StandardCharsets.UTF_16BE, StandardOpenOption.APPEND);
    }

    private void append(PdfBookmark pdfBookmark) {
        Integer level = pdfBookmark.getLevel();
        String title = pdfBookmark.getTitle();
        Integer page = pdfBookmark.getPage();
        String prefix = "";
        for (int i = 1; i < level; i++) {
            prefix += "\t";
        }
        prefix+=title+"\t" +page+ "\n";
        sbOut.append(prefix);
        for (PdfBookmark children : pdfBookmark.getChildrens()) {
            append(children);
        }
    }
}
