package fun.gatsby.sbimgutil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DragExample extends JFrame {
    private DragPanel panel;
    private JLabel label;

    public DragExample() {
        panel = new DragPanel();
        label = new JLabel("拖拽我");
        label.setOpaque(true); // 设置为不透明，以便显示背景色
        label.setBackground(Color.GREEN);

        // 添加鼠标监听器来处理拖拽
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // 鼠标按下时记录位置
                panel.setDragStart(e.getX(), e.getY());
            }
        });

        label.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                // 鼠标移动时更新标签位置
                panel.dragLabel(e.getX() - panel.getDragOffsetX(), e.getY() - panel.getDragOffsetY());
            }
        });

        panel.add(label);
        add(panel);
    }

    public static void main(String[] args) {
        DragExample example = new DragExample();
        example.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        example.setSize(300, 200);
        example.setVisible(true);
    }
}

class DragPanel extends JPanel {
    private Point dragStart = new Point();
    private Point dragOffset = new Point();

    public void setDragStart(int x, int y) {
        dragStart.setLocation(x, y);
    }

    public int getDragOffsetX() {
        return dragOffset.x;
    }

    public int getDragOffsetY() {
        return dragOffset.y;
    }

    public void dragLabel(int x, int y) {
        // 计算偏移量
        dragOffset.setLocation(x - dragStart.x, y - dragStart.y);
        // 更新组件位置
        for (Component comp : getComponents()) {
            comp.setLocation(comp.getX() + dragOffset.x, comp.getY() + dragOffset.y);
        }
        repaint();
    }
}
