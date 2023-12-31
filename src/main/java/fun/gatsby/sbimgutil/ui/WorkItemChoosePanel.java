package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.task.TaskTypeEnum;

import javax.swing.*;
import java.awt.event.ActionListener;

public class WorkItemChoosePanel extends JPanel {
    ButtonGroup group;
    JRadioButton[] jRadioButtons = new JRadioButton[TaskTypeEnum.values().length];

    public WorkItemChoosePanel() {
        super();
        group = new ButtonGroup();

        int i = 0;
        for (TaskTypeEnum taskTypeEnum : TaskTypeEnum.values()) {
            JRadioButton radioButton = new JRadioButton(taskTypeEnum.taskCnName);
            radioButton.setActionCommand(taskTypeEnum.name());
            group.add(radioButton);
            add(radioButton);
            jRadioButtons[i] = radioButton;
            i += 1;
        }
        jRadioButtons[0].setSelected(true);
    }

    public TaskTypeEnum getSelectedTaskType() {
        ButtonModel selection = group.getSelection();
        String actionCommand = selection.getActionCommand();
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.parse(actionCommand);
        return taskTypeEnum;
    }

    public void addItemListener(ActionListener actionListener) {
        for (JRadioButton radioButton : jRadioButtons) {
            radioButton.addActionListener(actionListener);
        }
    }
}
