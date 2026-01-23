package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.utils.PDFUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class PdfSplitTask extends BaseTask<Serializable>{

    private final File pdfFile;
    private final Path outDir;

    public PdfSplitTask(File pdfFile, Path outDir){
        super(null,null,null);
        this.pdfFile = pdfFile;
        this.outDir = outDir;
    }

    @Override
    public void doWork() throws Throwable {
         PDFUtils.split(pdfFile, outDir);
    }
}
