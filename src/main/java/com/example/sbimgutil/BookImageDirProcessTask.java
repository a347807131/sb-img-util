package com.example.sbimgutil;

import com.example.sbimgutil.utils.Const;
import com.example.sbimgutil.utils.FileFetchUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class BookImageDirProcessTask implements Runnable{


    private final OutFileGenerator outFileGenerator;
    private final File bookDir;
    private final ProcessConfig processConfig;

    public BookImageDirProcessTask(File bookDir,ProcessConfig processConfig){
        this.bookDir=bookDir;
        this.processConfig=processConfig;
        this.outFileGenerator=new OutFileGenerator();
    }

    @Override
    public void run() {
        try {
            List<File> files = new LinkedList<>();
//            FileFilter fileFilter = file -> file.getName().endsWith(".tif");
            FileFetchUtils.fetchFileRecursively(files,bookDir);
            log.info("{}下共有{}张图片待处理",bookDir,files.size());
            for (File oriTifFile : files) {
                BufferedImage originBufferedImage = ImageIO.read(oriTifFile);
                File outFile;

                //jpg无压缩
                outFile = outFileGenerator.genJpgOutFile(oriTifFile);
                TifUtils.TransformImgToJpg(originBufferedImage,new FileOutputStream(outFile),0);

                //jp2无压缩
                outFile = outFileGenerator.genJp2OutFile(oriTifFile);
                TifUtils.tranformImgToJp2(originBufferedImage,new FileOutputStream(outFile),0);

                //jp2压缩
                outFile=outFileGenerator.genJp2WithLossOutFile(oriTifFile,false);
                TifUtils.tranformImgToJp2(originBufferedImage,new FileOutputStream(outFile), processConfig.getImageCompressLimit());
                //jp2压缩并带水印
                outFile=outFileGenerator.genJp2WithLossOutFile(oriTifFile,true);
                BufferedImage bufferedImageToPaintBlur = ImageIO.read(oriTifFile);
                TifUtils.drawBlurPic(bufferedImageToPaintBlur,
                        ImageIO.read(new File(processConfig.getBlurImagePath()))
                );
                TifUtils.tranformImgToJp2(bufferedImageToPaintBlur,new FileOutputStream(outFile), processConfig.getImageCompressLimit());

                //pdf整合


            }
            log.info("{}处理完成",bookDir);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    class OutFileGenerator{

        final File tifDir;
        final File oriJp2OutDir;
        final File pdfOutDir;
         final File oriJpgOutDir;
         final File jp2WithLossOutDir;
         final String baseOutDir;

        OutFileGenerator(){
            this.baseOutDir=processConfig.getBaseOutDirPath();
            this.tifDir=new File(baseOutDir, Const.tifDirName);
            this.oriJpgOutDir=new File(baseOutDir,Const.oriJpgOutDirName);
            this.oriJp2OutDir=new File(baseOutDir,Const.oriJp2OutDirName);
            this.jp2WithLossOutDir=new File(baseOutDir,Const.jp2WithLossOutDirName);
            this.pdfOutDir=new File(baseOutDir,Const.pdfOutDirName);
        }

        /**
         * jpg无损
         */
        public File genJpgOutFile(File oriTifFile) throws IOException {
            String newFileName=oriTifFile.getName().substring(0,oriTifFile.getName().indexOf(".tif"))+".jpg";
            String FileAbsPath = oriTifFile.getAbsolutePath();
            String newFileAbsPath = FileAbsPath.replace(tifDir.getAbsolutePath(), oriJpgOutDir.getAbsolutePath());
            newFileAbsPath=newFileAbsPath.replace(oriTifFile.getName(),newFileName);
            File outFile = new File(newFileAbsPath);
            if(!outFile.getParentFile().exists())
                FileUtils.forceMkdirParent(outFile);
            return outFile;
        }

        /**
         *  base/3 水印有损JPEG2000/有水印|无水印
         */
        public File genJp2WithLossOutFile(File oriTifFile,boolean withBlur) throws IOException {
            String newFileName=oriTifFile.getName().substring(0,oriTifFile.getName().indexOf(".tif"))+".jp2";
            String FileAbsPath = oriTifFile.getAbsolutePath();
            String subDir= withBlur?"有水印":"无水印";
            String newFileAbsPath = FileAbsPath.replace(tifDir.getAbsolutePath(), jp2WithLossOutDir.getAbsolutePath()+"/"+subDir);
            newFileAbsPath=newFileAbsPath.replace(oriTifFile.getName(),newFileName);
            File outFile = new File(newFileAbsPath);
            if(!outFile.getParentFile().exists())
                FileUtils.forceMkdirParent(outFile);
            return outFile;
        }

        /**
         *  base/2 拼图无损JPEG2000
         */
        public File genPdfOutFile(File oriTifFile) throws IOException {
            String newFileName=oriTifFile.getName().substring(0,oriTifFile.getName().indexOf(".tif"))+".jp2";
            String FileAbsPath = oriTifFile.getAbsolutePath();
            String newFileAbsPath = FileAbsPath.replace(tifDir.getAbsolutePath(), oriJp2OutDir.getAbsolutePath());
            newFileAbsPath=newFileAbsPath.replace(oriTifFile.getName(),newFileName);
            File outFile = new File(newFileAbsPath);
            if(!outFile.getParentFile().exists())
                FileUtils.forceMkdirParent(outFile);
            return outFile;
        }
        public File genJp2OutFile(File oriTifFile) throws IOException {
            return null;
        }
    }
}

