package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.utils.PDFUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class PdfSplitTask extends BaseTask{

    private final File pdfFile;
    private final Path outDir;

    PdfSplitTask(File pdfFile, Path outDir){
        this.pdfFile = pdfFile;
        this.outDir = outDir;
    }

    @Override
    public void doWork() throws Throwable {
         PDFUtils.split(pdfFile, outDir);
    }

    public static class TaskGenerator extends BaseTaskGenerator{
        public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
            super(gtc, processTask, TaskTypeEnum.PDF_SPLIT);
        }

        public List<ITask> generate(){
            Path inPath = Path.of(gtc.getInDirPath());
            LinkedList<ITask> tasks = new LinkedList<>();
            // FIXME: 12/1/2023 递归控制
            List<File> pdfFiles = FileUtil.loopFiles(inPath, file -> {
                if (StringUtils.isEmpty(gtc.getFileNameRegex()) || !file.getName().matches(gtc.getFileNameRegex()))
                    return false;
                return file.getName().endsWith("pdf");
            });

            pdfFiles.forEach(file -> {
                var outDir = genOutFile(file).toPath();
                tasks.add(new PdfSplitTask(file, outDir));
            });
            return tasks;
        }
    }
}
