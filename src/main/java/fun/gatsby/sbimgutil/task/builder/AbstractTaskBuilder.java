package fun.gatsby.sbimgutil.task.builder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.TypeUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.task.*;
import fun.gatsby.sbimgutil.utils.Const;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

@RequiredArgsConstructor
public abstract class AbstractTaskBuilder<T extends BaseTask> implements TaskBuilder<T>{

    final AppConfig.GlobalTaskConfig gtc;
//    final Map<String, Object> configMap;
    final AppConfig.ProcessTask processTask;


    @Override
    public List<T> build() throws IOException {

        var tasks = new LinkedList<T>();
        Map<String,Object> configMap = BeanUtil.beanToMap(processTask);
        for (File imgFile : loadImageFiles()) {
            File outFile = genOutFile(imgFile,processTask.getFormat());
            if (outFile.exists() && !gtc.isEnforce()) {
                continue;
            }
            T task = ReflectUtil.newInstance(getTaskClass(), imgFile, outFile, configMap);
            tasks.add(task);
        }
        return tasks;
        ReflectUtil.newInstance(getTaskClass(), );
    }



    @Override
    public Class<T> getTaskClass() {
        var clazz = getClass();
        var type = TypeUtil.getTypeArgument(clazz, 0);
        return (Class<T>) type;
    }

    File genPdfOutFile(File dirFilesBelong) {
        String outFileName = dirFilesBelong.getName() + ".pdf";
        String midpiece = dirFilesBelong.getAbsolutePath().replace(
                new File(gtc.getInDirPath()).getAbsolutePath(), ""
        );
        Path fleOutDirPath = Path.of(gtc.getOutDirPath(), midpiece);
        if (!StringUtils.isEmpty(midpiece)) {
            fleOutDirPath = fleOutDirPath.getParent();
        }
        Path outFilePath = fleOutDirPath.resolve(outFileName);
        return outFilePath.toFile();
    }

    File genOutFile(File inFile) {
        return genOutFile(inFile,null);
    }

    /**
     *
     * @param inFile
     * @param format 后缀 无分隔符
     * @return
     */
    File genOutFile(File inFile,String format) {
        String inFileName = inFile.getName();
        String outFileName = inFileName;
        if (Strings.isNotBlank(format)) {
            outFileName = inFileName.substring(0, inFileName.lastIndexOf(".")) + "." +format;
        }
        String olDdirPath = inFile.getParentFile().getAbsolutePath();
        String midpiece = olDdirPath.replace(
                new File(gtc.getInDirPath()).getAbsolutePath(),
                ""
        );
        return Path.of(gtc.getOutDirPath(), midpiece, outFileName).toFile();
    }

    List<File> loadImageFiles() {
        return loadSortedDirToFilesMap().values().stream().collect(
                LinkedList::new,
                LinkedList::addAll,
                LinkedList::addAll
        );
    }

    public LinkedHashMap<File, List<File>> loadSortedDirToFilesMap() {
        Path inPath = Path.of(gtc.getInDirPath());
        List<File> files ;
        if(gtc.isRecursive()){
            files = FileUtil.loopFiles(inPath.toFile());
        }else {
            files = Arrays.stream(Objects.requireNonNull(inPath.toFile().listFiles(e -> !e.isDirectory()))).toList();
        }

        //@formatter:off-->
        return FileFilterUtils.filterList(getFileFileter(),files).stream()
                .collect(
                        LinkedHashMap::new,
                        (m, k) -> {
                            File parent = k.getParentFile();
                            m.computeIfAbsent(parent, v -> new LinkedList<>()).add(k);
                        },
                        LinkedHashMap::putAll
                );
        //@formatter:on-->
    }

    public IOFileFilter getFileFileter(){
        String fileNameRegex = gtc.getFileNameRegex();
        var fileExtFileter=new FileFilter() {
            @Override
            public boolean accept(File file) {
                return Const.SUPORTTED_FORMATS.contains(FileUtil.extName(file));
            }
        };
        var fileNameFileter=new FileFilter() {
            @Override
            public boolean accept(File file) {
                return Strings.isBlank(fileNameRegex) || file.getName().matches(fileNameRegex);
            }
        };
        return FileFilterUtils.and(
                FileFilterUtils.asFileFilter(fileNameFileter)
        );
    }
}
