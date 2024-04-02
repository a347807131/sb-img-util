package fun.gatsby.sbimgutil;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import fun.gatsby.sbimgutil.task.PdfImageScaleTask;
import fun.gatsby.sbimgutil.utils.FileOcrResultVO;
import fun.gatsby.sbimgutil.utils.GJCoolOcrApiResultVO;
import fun.gatsby.sbimgutil.utils.ImagesConverter2;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
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

        Map<File, FileOcrResultVO> map=FileUtil.loopFiles(imgFilesDir.toFile()).stream()
                .collect(
                        LinkedHashMap::new,
                        (fileFileOcrResult, imageFile) -> {
                            String resultFileName = imageFile.getName()
                                    .substring(0, imageFile.getName().lastIndexOf(".") + 1)
                                    +"json"
                                    ;
                            File ocrJsonFile = ocrJsonFilesDir.resolve(resultFileName).toFile();
                            FileOcrResultVO fileOcrResultVO = JSON.parseObject(FileUtil.readUtf8String(ocrJsonFile), FileOcrResultVO.class);
                            fileFileOcrResult.put(imageFile, fileOcrResultVO);
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


    @Test
    public void t4(){

        Path path=Path.of("C:\\Users\\Gatsby\\OneDrive - bupt.edu.cn\\桌面\\msq work temp\\古籍酷ocr示例");
        var file = path.resolve("0010.json").toFile();
        var resultVO = JSON.parseObject(FileUtil.readString(file, "gbk"), GJCoolOcrApiResultVO.class);
    }
}
