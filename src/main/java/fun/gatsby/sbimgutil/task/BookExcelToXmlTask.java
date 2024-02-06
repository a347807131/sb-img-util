package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.task.BaseTask;
import fun.gatsby.sbimgutil.task.extracxml.ExcelToXml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BookExcelToXmlTask extends BaseTask {
    private final File excelFile;
    private final Path outDir;

    public BookExcelToXmlTask(File excelFile, Path outDir) {
        this.excelFile = excelFile;
        this.outDir = outDir;
        this.name = "BookExcelToXmlTask" + excelFile.getName();
    }
    @Override
    public void doWork() throws Throwable {
        ExcelToXml.changeSingleBook(excelFile.getAbsolutePath(),false, outDir);
    }

    public static class TaskGenerator extends BaseTaskGenerator {
        public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
            super(gtc, processTask, TaskTypeEnum.BOOK_EXCEL_TO_XML);
        }

        @Override
        public List<ITask> generate() throws IOException {
            ArrayList<ITask> tasks = new ArrayList<>();
            List<File> excelFiles = loadImageFiles().stream()
                    .filter(file -> file.getName().endsWith(".xls") || file.getName().endsWith(".xlsx")).toList();
            for (File excelFile : excelFiles) {
                var outDir = genOutFile(excelFile, FileUtil.extName(excelFile)).getParentFile().toPath();
                tasks.add(new BookExcelToXmlTask(excelFile, outDir));
            }
            return tasks;
        }
    }
}
