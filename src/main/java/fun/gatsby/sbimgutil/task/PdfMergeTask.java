package fun.gatsby.sbimgutil.task;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.utils.PDFUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class PdfMergeTask extends BaseTask {

    private List<File> inFiles;
    private File cataFile;

    private float imageScale=1f;

    public PdfMergeTask(List<File> inFiles, File outFile, File cataFile,float scale) {
        this.inFiles = inFiles;
        this.outFile = outFile;
        this.cataFile = cataFile;
        this.imageScale=scale;
        this.name = "合并pdf -> " + outFile.getAbsolutePath();
    }
    public PdfMergeTask(List<File> inFiles, File outFile) {
        this.inFiles = inFiles;
        this.outFile = outFile;
    }

    @Override
    public void doWork() {
        inFiles = inFiles.stream().sorted(Comparator.comparing(File::getName)).toList();
        try (OutputStream os = Files.newOutputStream(outFile.toPath())) {
            if (cataFile == null) {
                PDFUtils.mergeIntoPdf(inFiles, os);
                return;
            } else if (!cataFile.exists()) {
                log.error("cataFile is null" + outFile.getAbsolutePath());
                throw new IOException("cataFile is null" + outFile.getAbsolutePath());
            } else {
                if(imageScale>=1)
                    PDFUtils.mergeIntoPdf(inFiles, cataFile, os);
                else
                    PDFUtils.mergeIntoPdfWithScale(inFiles, cataFile, os, imageScale);
                File copiedCataFile = new File(outFile.getParentFile(), cataFile.getName());
                Files.copy(cataFile.toPath(), copiedCataFile.toPath());
                return;
            }
        } catch (Exception e) {
            log.error("merge pdf:{} error", outFile, e);
            throw new RuntimeException(e);
        }
    }

    public static class TaskGenerator extends BaseTaskGenerator {
        public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
            super(gtc, processTask,TaskTypeEnum.PDF_MERGE);
        }

        public List<ITask> generate() {
            List<ITask> tasks = new LinkedList<>();
            for (Map.Entry<File, List<File>> entry : loadSortedDirToFilesMap().entrySet()) {
                File dirThatFilesBelong = entry.getKey();
                File outFile =
                        genPdfOutFile(dirThatFilesBelong);
                if (outFile.exists() && !gtc.isEnforce())
                    continue;
                List<File> imgs = entry.getValue();
                String cataDirPath = processTask.getCataDirPath();
                File cataFile = null;
                if (Strings.isNotBlank(cataDirPath)) {
                    String cataFileName = dirThatFilesBelong.getAbsolutePath().replace(new File(gtc.getInDirPath()).getAbsolutePath(), "") + ".txt";
                    cataFile = new File(cataDirPath, cataFileName);
                }
                PdfMergeTask task = new PdfMergeTask(imgs, outFile, cataFile,processTask.getPdfImageScale());
                tasks.add(task);
            }
            return tasks;
        }
    }
}
