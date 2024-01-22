package fun.gatsby.sbimgutil.context;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DestroyEvent implements DisposableBean {
    @Override
    public void destroy() throws Exception {
        if(PyApiProcessManager.loaded())
            PyApiProcessManager.getInstance().destroy();
    }
}
