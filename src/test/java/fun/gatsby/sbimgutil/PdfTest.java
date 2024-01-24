package fun.gatsby.sbimgutil;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.formdev.flatlaf.json.Json;
import fun.gatsby.sbimgutil.task.DoubleLayerPdfGenerateTask;
import fun.gatsby.sbimgutil.task.PdfImageScaleTask;
import fun.gatsby.sbimgutil.utils.FileOcrResult;
import fun.gatsby.sbimgutil.utils.ImagesConverter2;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class PdfTest {

    @Test
    public void t1(){

        var dir=Path.of("D:\\out\\27030166");
        var outFile=dir.resolve("0001_0.25.pdf").toFile();
        PdfImageScaleTask pdfImageScaleTask = new PdfImageScaleTask(dir.resolve("0001.pdf").toFile(),outFile);
        pdfImageScaleTask.run();
    }

    @Test
    public void t2() throws Exception {
        Path ocrJsonFilesDir = Path.of("D:\\ocr_data\\GJ-1686706576288\\ocr");
        Path imgFilesDir = Path.of("D:\\原始备份\\提取txt文档\\图片\\0002");

        Map<File, FileOcrResult> map=FileUtil.loopFiles(imgFilesDir.toFile()).stream()
                .collect(
                        LinkedHashMap::new,
                        (fileFileOcrResult, imageFile) -> {
                            String resultFileName = imageFile.getName()
                                    .substring(0, imageFile.getName().lastIndexOf(".") + 1)
                                    +"json"
                                    ;
                            File ocrJsonFile = ocrJsonFilesDir.resolve(resultFileName).toFile();
                            FileOcrResult fileOcrResult = JSON.parseObject(FileUtil.readUtf8String(ocrJsonFile), FileOcrResult.class);
                            fileFileOcrResult.put(imageFile, fileOcrResult);
                        },
                        LinkedHashMap::putAll
                );

        ImagesConverter2 imagesConverter2 = ImagesConverter2.of(ocrJsonFilesDir,imgFilesDir,null);
        imagesConverter2.convertToBilayerPdf(new File("temp.pdf"));
    }

    @Test
    public void t3(){
        String s = FileUtil.readUtf8String(new File("temp"));
    }
}
