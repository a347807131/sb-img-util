package fun.gatsby.sbimgutil.context;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


@Slf4j
public class PyApiProcessManager {

    static PyApiProcessManager instance=null;

    private Process process;
    private int port;

    private PyApiProcessManager(){

        try {
            init();
        }catch (Exception e){
            log.error("Py api manager init error",e);
            throw new RuntimeException();
        }
    }

    public static PyApiProcessManager getInstance(){
        if(instance==null){
            instance=new PyApiProcessManager();
        }
        return instance;
    }

    public String getPyApiServerUrl(){
        return "http://127.0.0.1:%s".formatted(port);
    }

    void init() throws IOException {

        port = getUsablePort(8868);

        log.info("imageProcessApiPy process start initing, please wait 10s.");
        Path appDir = Path.of("pyApi");
        Path exePath=appDir.resolve("app.exe");

        ProcessBuilder processBuilder = new ProcessBuilder(exePath.toString(),
                "--port="+port
//                "--rembg_model="+"u2net"
        );
        processBuilder.directory(appDir.toFile());
        Path modelDirPah = appDir.resolve("models");
        processBuilder.environment().put("U2NET_HOME",modelDirPah.toFile().getAbsolutePath());
        process = processBuilder.start();
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

        String line = "";
        var ocrReady = false;
        while (!ocrReady) {
            line = reader.readLine();
            log.debug("line: " + line);
            if (line.contains("Serving") || line.contains("Running")) {
                ocrReady = true;
            }
        }
        log.info("imageProcessApiPy process inited, running at port "+port);
    }
    public void destroy(){
        if(process.isAlive()) {
            process.destroy();
            log.info("imageProcessApiPy process destoryed,now alive ? {}",process.isAlive());
        }
    }

    public static boolean loaded(){
        return instance!=null;
    }


    /**
     * 根据输入端口号，递增递归查询可使用端口
     * @param port  端口号
     * @return  如果被占用，递归；否则返回可使用port
     */
    public static int getUsablePort(int port) throws IOException {
        boolean flag = false;
        Socket socket = null;
        InetAddress theAddress = InetAddress.getByName("127.0.0.1");
        try{
            socket = new Socket(theAddress, port);
            flag = true;
        } catch (IOException e) {
            //如果测试端口号没有被占用，那么会抛出异常，通过下文flag来返回可用端口
        } finally {
            if(socket!=null) {
                //new了socket最好释放
                socket.close();
            }
        }

        if (flag) {
            //端口被占用，port + 1递归
            port = port + 1;
            return getUsablePort(port);
        } else {
            //可用端口
            return port;
        }
    }
}
