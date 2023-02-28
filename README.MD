## 程序说明：

该程序是以java编写的图书tif文件处理软件，可完成tif格式转换(jp2 jpg)，图片压缩(500k)，图片合并成带目录pdf，以及可断点续处理功能的软件。

### 使用步骤：

- 1，解压缩程序包
- 2，更改配置文件
- 3，双击app.exe启动程序
- 4，等待运行结果

### 配置项说明

程序的配置文件为程序目录下的`aolication.yml`文件，默认已配有8项任务，每项已附的说明，开关为enable的值,false或者true。

配置文件中的tif-dir-path变更为目标tif文件书籍所在的目录，默认输出为该目录的上级目录，也可自行修改，其配置为`base-out-dir-path`。

程序支持断点运行，保存的颗粒度为书的卷，只有在一个书卷完整处理后才会保存进度，未保存的会在下次运行时重新处理。

max-worker-num为进行处理的线程数，一般配置为8即可，实际线程数为卷数和该值相比的最小值。若运行时资源占用过大，可调小该值。

程序的处理顺序为每卷进行配置的每项任务进行循环处理。

```yml

logging:
  file:
    name: ${app.base-out-dir-path}/log.txt

app:
  #tif文件目录
  tif-dir-path:
  #输出位置
  base-out-dir-path:
  #水印文件位置
  blur-image-path:
  cata-dir-path: ${app.tif-dir-path}/../5 目录
  #处理最大线程数
  max-worker-num: 8
  process-items:
    #无损jp2
    - format: jp2
      #开关
      enable: false
      #是否带水印
      with-blur: false
      compress-limit: 0 #单位kb
      blur-image-path: ${app.blur-image-path}
      out-dir-path: ${app.base-out-dir-path}/2 拼图无损JP2000

    #有损jp2无水印
    - format: jp2
      enable: true
      with-blur: false
      compress-limit: 500 #单位kb
      blur-image-path: ${app.blur-image-path}
      out-dir-path: ${app.base-out-dir-path}/3 水印有损JP2000/无水印

    #有损jp2有水印
    - format: jp2
      enable: false
      with-blur: true
      compress-limit: 500 #单位kb
      blur-image-path: ${app.blur-image-path}
      out-dir-path: ${app.base-out-dir-path}/3 水印有损JP2000/有水印

    #封面
    - format: jpg
      enable: false
      with-blur: true
      compress-limit: 0 #单位kb 0表示不压缩
      blur-image-path: ${app.blur-image-path}
      out-dir-path: ${app.base-out-dir-path}\7 提交平台\封面
      #文件名匹配的正则表达式
      file-name-regex: ^0001.*$ #已0001开头的文件名

      #jpg无损
    - format:  jpg
      enable: false
      with-blur: false
      compress-limit: 0 #单位kb 0表示不压缩
      blur-image-path: ${app.blur-image-path}
      out-dir-path: ${app.base-out-dir-path}\JPG无损

    ###################################
    #pdf是书籍处理完成后的后置处理
    ###################################

    #pdf无水印
    - format: pdf
      enable: false
      #      compress-limit: 0 #单位kb 0表示不压缩
      #      with-blur: false
      #      blur-image-path: ${app.blur-image-path}
      out-dir-path: ${app.base-out-dir-path}/4 PDF/无水印
      resource-dir-path: ${app.base-out-dir-path}/3 水印有损JP2000/无水印
      cata-dir-path: ${app.cata-dir-path}
      file-name-regex: ^0001.*$ #已0001开头的文件名

    #pdf带水印
    - format: pdf
      enable: false
      #      compress-limit: 0 #单位kb 0表示不压缩
      #      with-blur: false
      #      blur-image-path: ${app.blur-image-path}
      out-dir-path: ${app.base-out-dir-path}/4 PDF/有水印
      resource-dir-path: ${app.base-out-dir-path}/3 水印有损JP2000/有水印
      cata-dir-path: ${app.cata-dir-path}
      file-name-regex: ^(?!seka).*$ #不以seka开头的文件名
```

