package fun.gatsby.sbimgutil.task;

import cn.hutool.core.io.FileUtil;
import com.sun.security.auth.module.UnixSystem;
import fun.gatsby.sbimgutil.utils.FileFetchUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class LabeledDatasetCollectTask extends BaseTask{
    static final String LABEL_SEPRATOR="\t";

    private final File labelFile;
    final float rateOfTrain;
    final String datasetName;
    private final Path rootOutPath;

//    public LabeledDatasetCollectTask(File labelFile, Path rootDatasetOutPath) {
//
//    }

    public LabeledDatasetCollectTask(File labelFile, Path rootOutPath,float rateOfTrain){
        this.rootOutPath=rootOutPath;
        this.labelFile=labelFile;
        datasetName=labelFile.getParentFile().getParentFile().getName();
        super.name=TaskTypeEnum.LABELED_DATASET_COLLECT.taskCnName+" -> "+rootOutPath+datasetName;
        this.rateOfTrain=rateOfTrain;
    }
    @Override
    public void doWork() throws IOException {
        extractDetDataset();
        extractRecDataset();
    }

    void extractRecDataset() throws IOException {

        final var outDirPath = rootOutPath.resolve("rec");

        File recGtTxtFile=labelFile.toPath().resolve("../rec_gt.txt").toFile();
        LinkedList<String> newLines = new LinkedList<>();
        var sublines = Files.readAllLines(recGtTxtFile.toPath());
        for (String subline : sublines) {
            if (StringUtils.isEmpty(subline)) continue;
            String[] split = subline.split("\t");
            if(split.length!=2){
                log.warn(recGtTxtFile.getPath()+"格式错误: "+ Arrays.toString(split));
                continue;
            }
            String picRelativePath = split[0];
            String text = split[1];
            String picFileName = Path.of(picRelativePath).getFileName().toString();
            var newRelativePath = datasetName+"/"+picFileName;
            String newLine = newRelativePath + "\t" + text;
            newLines.add(newLine);
            Path oriPicPath=recGtTxtFile.toPath().getParent().resolve(picRelativePath);
            Path outPicPath = outDirPath.resolve(newRelativePath);
            if(!Files.exists(outPicPath))
                FileUtil.copyFile(oriPicPath.toFile(), outPicPath.toFile());
        }
        writeLabelFiles(newLines,outDirPath);
    }

    /**
     *  从ppocrlabel制作的数据集中提取出det数据集
     * @throws IOException
     */
    void extractDetDataset() throws IOException {
        final var outDirPath = rootOutPath.resolve("det");
        LinkedList<String> newLines = new LinkedList<>();
        var sublines = Files.readAllLines(labelFile.toPath());
        for (String subline : sublines) {
            if (StringUtils.isEmpty(subline)) continue;
            String[] split = subline.split("\t");
            String picRelativePath = split[0];
            String text = split[1];
            String picFileName = Path.of(picRelativePath).getFileName().toString();
            var newRelativePath = datasetName+"/"+picFileName;
            String newLine = newRelativePath + "\t" + text;
            newLines.add(newLine);

            Path oriPicPath=labelFile.toPath().getParent().getParent().resolve(picRelativePath);
            Path outPicPath = outDirPath.resolve(newRelativePath);
            if(Files.exists(outPicPath))
                FileUtil.del(outPicPath.toFile());
            FileUtil.copyFile(oriPicPath.toFile(), outPicPath.toFile());
        }
        writeLabelFiles(newLines,outDirPath);
    }

    void writeLabelFiles(List<String> lines,Path outDirPath) throws IOException {
        int trainSize = (int) (lines.size() * rateOfTrain);
        File outLabelFile = outDirPath.resolve(datasetName).resolve("Labels.txt").toFile();
        File outTrtainLabelFile = outDirPath.resolve(datasetName).resolve("train_labels.txt").toFile();
        File outTestLabelFile = outDirPath.resolve(datasetName).resolve("test_labels.txt").toFile();
        Files.writeString(outLabelFile.toPath(), String.join("\n",lines));
        Files.writeString(outTrtainLabelFile.toPath(), String.join("\n",lines.subList(0,trainSize)));
        Files.writeString(outTestLabelFile.toPath(), String.join("\n",lines.subList(trainSize,lines.size())));
    }
}
