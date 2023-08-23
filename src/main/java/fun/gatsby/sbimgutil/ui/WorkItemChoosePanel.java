package fun.gatsby.sbimgutil.ui;

import fun.gatsby.sbimgutil.task.TaskTypeEnum;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WorkItemChoosePanel extends JPanel {
    ButtonGroup group;
    List<JRadioButton> jRadioButtons = new LinkedList<>();

    public WorkItemChoosePanel() {
        super();
        group = new ButtonGroup();
        List<TaskTypeEnum> typeEnums = Arrays.stream(TaskTypeEnum.values()).filter(taskTypeEnum -> taskTypeEnum != TaskTypeEnum.BOOK_IMAGE_FIX).toList();
        for (TaskTypeEnum taskTypeEnum : typeEnums) {
            JRadioButton radioButton = new JRadioButton(taskTypeEnum.taskCnName);
            radioButton.setActionCommand(taskTypeEnum.name());
            group.add(radioButton);
            add(radioButton);
            jRadioButtons.add(radioButton);
        }
        jRadioButtons.get(0).setSelected(true);
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
