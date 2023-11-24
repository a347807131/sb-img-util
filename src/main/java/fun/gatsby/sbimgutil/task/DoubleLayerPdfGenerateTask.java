package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.utils.Const;
import fun.gatsby.sbimgutil.utils.ImagesConverter;
import fun.gatsby.sbimgutil.utils.Label;
import fun.gatsby.sbimgutil.utils.PDFUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DoubleLayerPdfGenerateTask extends BaseTask{

    private final File cataFile;
    private final File outPdfFile;
    private final List<Label> labels;

    public DoubleLayerPdfGenerateTask(Path datasetPath,List<Label> labels, File cataFile, File outPdfFile){
        this.name = "双层pdf制作 -> " + outPdfFile.getAbsolutePath();
        this.labels=labels;
        this.outPdfFile=outPdfFile;
        this.cataFile = cataFile;
    }
    @Override
    public void doWork() throws Throwable {

        File outXmlFile = new File(outPdfFile.getParentFile(),outPdfFile.getName().replace(".pdf",".xml"));
        PDFUtils.createOutXmlbyLabels(outXmlFile,labels);
        ImagesConverter imagesConverter = new ImagesConverter(labels,cataFile);
        imagesConverter.convertToBilayerPdf(outPdfFile);
    }

     public static class TaskGenerator extends BaseTask.TaskGenerator {
         public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
             super(gtc, processTask,TaskTypeEnum.DOUBLE_LAYER_PDF_GENERATE);
         }

         public List<ITask> generate() {
            Path outPath = Path.of(gtc.getOutDirPath());
            Path inPath = Path.of(gtc.getInDirPath());
            var cataDirPath = Path.of(processTask.getCataDirPath());
            LinkedHashMap<File, List<File>> dirToImgFilesMap = loadSortedDirToImgFilesMap();
            List<ITask> tasks = new LinkedList<>();
            for (Map.Entry<File, List<File>> entry : dirToImgFilesMap.entrySet()) {
                File dirThatFilesBelong = entry.getKey();
                List<File> imgFiles = entry.getValue();
                String txtFileRelativePath = dirThatFilesBelong.getAbsolutePath().replace(
                        inPath.toFile().getAbsolutePath(), "./"
                ) + ".txt";

                File labelFile = new File(processTask.getLabelDirPath(), txtFileRelativePath);
                if(!labelFile.exists()){
                    continue;
                }
                List<Label> labels ;
                try {
                    labels = Label.parse(dirThatFilesBelong.getParentFile().toPath(), labelFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                labels = labels.stream().filter(e -> e.getMarkedImageFile().exists()).collect(Collectors.toList());

                if(!labelFile.exists()) continue;
                File cataFile = cataDirPath.resolve(txtFileRelativePath).toFile();
                if(!cataFile.exists()) cataFile=null;
                File outFile = genPdfOutFile(dirThatFilesBelong);
                if (outFile.exists() && !gtc.isEnforce())
                    continue;

                var task = new DoubleLayerPdfGenerateTask(
                        dirThatFilesBelong.getParentFile().toPath(),
                        labels,
                        cataFile,
                        outFile
                );
                tasks.add(task);
            }
            return tasks;
        }
     }
}
