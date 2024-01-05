package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.utils.CataParser;
import fun.gatsby.sbimgutil.utils.PDFUtils;
import fun.gatsby.sbimgutil.utils.PdfBookmark;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class PdfAddCataTask extends BaseTask{
    private final File pdfFile;
    private final File cataFile;
    private final File outFile;

    public PdfAddCataTask(File pdfFile, File cataFile, File outFile){
        this.pdfFile = pdfFile;
        this.cataFile = cataFile;
        this.outFile = outFile;
        this.name=pdfFile.getName()+TaskTypeEnum.PDF_ADD_CATA.taskCnName;
    }
    @Override
    public void doWork() throws Throwable {
        PDFUtils.addCata(pdfFile, cataFile, outFile);
    }

    static class TaskGenerator extends BaseTaskGenerator {

        public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
            super(gtc, processTask, TaskTypeEnum.PDF_ADD_CATA);
        }

        @Override
        public List<ITask> generate() throws IOException {

            LinkedList<ITask> tasks = new LinkedList<>();
            // FIXME: 12/1/2023 递归控制
            List<File> pdfFiles = FileUtil.loopFiles(gtc.getInDirPath(), file -> {
                if (StringUtils.isEmpty(gtc.getFileNameRegex()) || !file.getName().matches(gtc.getFileNameRegex()))
                    return false;
                return file.getName().endsWith("pdf");
            });

            for (File file : pdfFiles) {
                String txtFileRelativePath = file.getAbsolutePath().replace(
                        new File(gtc.getInDirPath()).getAbsolutePath(), "./"
                ).replace(".pdf", ".txt");
                var cataFile = Path.of(processTask.getCataDirPath(), txtFileRelativePath).toFile();
                var outFile = genOutFile(file);
                tasks.add(new PdfAddCataTask(file, cataFile, outFile));
            }
            return tasks;
        }
    }
}
