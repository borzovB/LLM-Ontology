package org.example;

import org.apache.jena.ontology.OntModel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Вспомогательное окно — отображает дерево классов и индивидуумов онтологии.
// Открывается поверх родительского окна; закрывается вместе с ним.
public class WindowClassTree {


// Константы


    private static final String WINDOW_TITLE  = "Дерево классов и индивидуумов";
    private static final int    WINDOW_WIDTH  = 700;
    private static final int    WINDOW_HEIGHT = 500;


// Поля


    // Окно-владелец: дерево позиционируется относительно него и
    // закрывается при его закрытии
    private final JFrame         parentFrame;
    private final OntModel        model;
    private final LLMOntologyApp app;

    // Компоненты окна дерева (null — пока окно не открыто)
    private JFrame   treeFrame;
    private JTextArea treeArea;


// Конструктор


    public WindowClassTree(JFrame parentFrame, OntModel model, LLMOntologyApp app) {
        this.parentFrame = parentFrame;
        this.model       = model;
        this.app         = app;
    }


// Открытие окна


    // Открывает окно дерева. Если оно уже видимо — выводит на передний план.
    public void open() {
        if (isAlreadyOpen()) {
            treeFrame.toFront();
            treeFrame.requestFocus();
            return;
        }

        treeFrame = createTreeFrame();
        treeArea  = createTreeArea();

        treeFrame.add(new JScrollPane(treeArea), BorderLayout.CENTER);

        // Закрываем дерево вместе с родительским окном
        attachParentCloseListener();

        // Загружаем иерархию в EDT после того как окно уже отображено
        SwingUtilities.invokeLater(() ->
                app.printFullClassHierarchy(model, treeArea));

        treeFrame.setVisible(true);
    }


// Создание компонентов


    // Создаёт и настраивает JFrame окна дерева.
    private JFrame createTreeFrame() {
        JFrame frame = new JFrame(WINDOW_TITLE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(parentFrame);
        frame.setLayout(new BorderLayout());
        // Закрытие крестиком уничтожает только это окно, не родительское
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return frame;
    }

    // Создаёт нередактируемую текстовую область с контекстным меню «Копировать».
    private JTextArea createTreeArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem copy  = new JMenuItem("Копировать");
        copy.addActionListener(e -> area.copy());
        menu.add(copy);
        area.setComponentPopupMenu(menu);

        return area;
    }


// Вспомогательные методы


    // Возвращает true, если окно дерева уже создано и отображается на экране.
    private boolean isAlreadyOpen() {
        return treeFrame != null && treeFrame.isDisplayable();
    }

    // Подписывается на закрытие родительского окна:
    // при его закрытии окно дерева тоже уничтожается.
    private void attachParentCloseListener() {
        parentFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (treeFrame != null) treeFrame.dispose();
            }
        });
    }
}