package fun.gatsby.sbimgutil.task.builder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.task.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractTaskBuilder<T extends BaseTask<?extends Serializable>> implements TaskBuilder<T>{

    final AppConfig.GlobalTaskConfig gtc;
    final Map<String, Object> configMap;
    private final Class<Serializable> configClass;
    private final Class<T> taskClass;

    static  final CopyOptions COPY_OPTIONS = CopyOptions.create()
            .setIgnoreCase(true)  // 忽略大小写
            .setFieldNameEditor(name -> {
                // 自定义名称转换：先转小写，然后处理横杆和下划线
                name = name.toLowerCase();
                return StrUtil.toCamelCase(name.replace('-', '_'));
            });

    private final Object config;

    public AbstractTaskBuilder(AppConfig.GlobalTaskConfig gtc, Map<String, Object> configMap) {
        this.gtc = gtc;
        this.configMap = configMap;

        configClass = getConfigClass();
        taskClass = getTaskClass();
        config = BeanUtil.toBean(configMap, configClass,COPY_OPTIONS);
    }

    @Override
    public List<T> build() throws IOException {
        var tasks = new LinkedList<T>();
        for (File inFile : loadInFiles()) {
            File outFile = outFile(inFile);
            if (outFile.exists() && !gtc.isEnforce()) {
                continue;
            }
            tasks.add(build(inFile));
        }
        return tasks;
    }

    @Override
    public T build(File inFile) throws IOException {
        File outFile = outFile(inFile);
        return ReflectUtil.newInstance(getTaskClass(), inFile, outFile, config);
    }

    protected Class<T> getTaskClass() {
        var clazz = getClass();
        var type = TypeUtil.getTypeArgument(clazz, 0);
        return (Class<T>) type;
    }

    protected Class<Serializable> getConfigClass() {
        var taskClass = getTaskClass();
        return (Class<Serializable>) TypeUtil.getTypeArgument(taskClass.getGenericSuperclass(), 0);
    }

    @Override
    public File outFile(File inFile) {
        return outFile(inFile,(String) configMap.get("ext"));
    }

    @Override
    public File outFile(File inFile, String format) {
        String inFileName = inFile.getName();
        String outFileName = inFileName;
        if (Strings.isNotBlank(format)) {
            outFileName = inFileName.substring(0, inFileName.lastIndexOf(".")) + "." +format;
        }
        String olDdirPath = inFile.getParentFile().getAbsolutePath();
        String midpiece = olDdirPath.replace(
                new File(gtc.getInDir()).getAbsolutePath(),
                ""
        );
        return Path.of(gtc.getOutDir(), midpiece, outFileName).toFile();
    }

    @Override
    public List<File> loadInFiles() {
        return loadSortedDirToFilesMap().values().stream().collect(
            LinkedList::new,
            LinkedList::addAll,
            LinkedList::addAll
        );
    }

    public LinkedHashMap<File, List<File>> loadSortedDirToFilesMap() {
        Path inPath = Path.of(gtc.getInDir());
        List<File> files =gtc.isRecursive()?
            FileUtil.loopFiles(inPath.toFile()) :
            Arrays.stream(Objects.requireNonNull(inPath.toFile().listFiles(e -> !e.isDirectory()))).toList();
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

    @Override
    public Set<String> getSupportedExts(){
        return null;
    }

    @Override
    public IOFileFilter getFileFileter(){
        String fileNameRegex = gtc.getFileNameRegex();
        var fileExtFileter=new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(getSupportedExts()==null) return true;
                return getSupportedExts().contains(FileUtil.extName(file));
            }
        };

        var fileNameFileter=new FileFilter() {
            @Override
            public boolean accept(File file) {
                return Strings.isBlank(fileNameRegex) || file.getName().matches(fileNameRegex);
            }
        };

        return FileFilterUtils.and(
            FileFilterUtils.asFileFilter(fileExtFileter),
            FileFilterUtils.asFileFilter(fileNameFileter)
        );
    }
}
