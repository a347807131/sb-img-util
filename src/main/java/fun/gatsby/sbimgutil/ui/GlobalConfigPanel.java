package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.config.StaticBeanGet;
import fun.gatsby.sbimgutil.ui.util.GuiUtils;

import javax.swing.*;
import java.awt.*;

import static fun.gatsby.sbimgutil.ui.util.Insertable.BLOCK_SEPARATOR;
import static fun.gatsby.sbimgutil.ui.util.Insertable.ITEM_SEPARATOR_SMALL;

public class GlobalConfigPanel extends JPanel {
    private FilePathInputPanel pathInputPanel;
    private FilePathInputPanel pathOutPanel;
    private CommonInputPanel workNumInputPanel;
    private CommonInputPanel fileNameRegInputPanel;
    private JRadioButton recursiveChooseBtn;
    private JRadioButton enforceChooseBtn;
    private AppConfig.GlobalTaskConfig gtc;

    public GlobalConfigPanel(AppConfig.GlobalTaskConfig gtc) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.gtc = gtc;
        JbInit();
    }

    void JbInit() {
        pathInputPanel = new FilePathInputPanel("输入文件夹", 10);
        pathInputPanel.setFilePath(gtc.getInDirPath());
        pathOutPanel = new FilePathInputPanel("输出文件夹", 10);
        pathOutPanel.setFilePath(gtc.getOutDirPath());
        add(
                GuiUtils.getFlowLayoutPanel(
                        FlowLayout.TRAILING, BLOCK_SEPARATOR, ITEM_SEPARATOR_SMALL, pathInputPanel, pathOutPanel)
        );

        workNumInputPanel = new CommonInputPanel("最大线程数", String.valueOf(gtc.getMaxWorkerNum()));
        fileNameRegInputPanel = new CommonInputPanel("文件名正则表达式", gtc.getFileNameRegex(), 10);
        recursiveChooseBtn = new JRadioButton("递归文件处理", gtc.isRecursive());
        enforceChooseBtn = new JRadioButton("强制覆盖处理", gtc.isEnforce());
        add(
                GuiUtils.getFlowLayoutPanel(
                        FlowLayout.TRAILING, BLOCK_SEPARATOR, ITEM_SEPARATOR_SMALL,
                        workNumInputPanel,
                        fileNameRegInputPanel,
                        recursiveChooseBtn,
                        enforceChooseBtn)
        );
    }

    public AppConfig.GlobalTaskConfig getValidGlobalConfig() {
        gtc.setInDirPath(pathInputPanel.getFilePath());
        gtc.setOutDirPath(pathOutPanel.getFilePath());
        gtc.setMaxWorkerNum(Integer.parseInt(workNumInputPanel.getValue()));
        gtc.setFileNameRegex(fileNameRegInputPanel.getValue());
        gtc.setRecursive(recursiveChooseBtn.isSelected());
        gtc.setEnforce(enforceChooseBtn.isSelected());
        return gtc;
    }
}
