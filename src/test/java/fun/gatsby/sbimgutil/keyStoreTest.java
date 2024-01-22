package fun.gatsby.sbimgutil;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLOutput;

public class keyStoreTest {

    @Test
    void t1(){

        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,
                InputEvent.CTRL_DOWN_MASK
                        | InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        char keyChar = ks.getKeyChar();
        KeyStroke keyStroke = KeyStroke.getKeyStroke("shift ctrl alt pressed PAGE_DOWN");
        assert KeyEvent.VK_PAGE_DOWN==keyStroke.getKeyCode();
        System.out.println(keyStroke);

    }

    @Test
    void t2(){
        KeyStroke keyStroke = KeyStroke.getKeyStroke("pressed PAGE_DOWN  ");
        KeyStroke keyStroke2  = KeyStroke.getKeyStroke("pressed PAGE_DOWN  ");
        System.out.println(keyStroke);
    }
}
