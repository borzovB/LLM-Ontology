package org.example;

import org.apache.jena.ontology.OntModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowClassTree {

    private JFrame treeFrame;
    private JTextArea treeTextArea;

    private JFrame valueFrame;
    private OntModel model;
    private LLMOntologyApp llmOntologyApp;

    public WindowClassTree(
            JFrame valueFrame,
            OntModel model,
            LLMOntologyApp llmOntologyApp
    ) {
        this.valueFrame = valueFrame;
        this.model = model;
        this.llmOntologyApp = llmOntologyApp;
    }

    public void open() {

        // Если окно уже открыто
        if (treeFrame != null && treeFrame.isDisplayable()) {
            treeFrame.toFront();
            treeFrame.requestFocus();
            return;
        }

        treeFrame = new JFrame("Дерево классов и индивидуумов");
        treeFrame.setSize(700, 500);
        treeFrame.setLocationRelativeTo(valueFrame);
        treeFrame.setLayout(new BorderLayout());

        treeTextArea = new JTextArea();
        treeTextArea.setEditable(false);

        // Popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("Копировать");
        copyItem.addActionListener(e -> treeTextArea.copy());

        popupMenu.add(copyItem);

        treeTextArea.setComponentPopupMenu(popupMenu);

        JScrollPane scrollPane = new JScrollPane(treeTextArea);
        treeFrame.add(scrollPane, BorderLayout.CENTER);

        // Закрытие только этого окна
        treeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Если закрывается главное окно
        valueFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (treeFrame != null) {
                    treeFrame.dispose();
                }
            }
        });

        // Отдельный поток
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                treeTextArea.setText("");
                llmOntologyApp.printFullClassHierarchy(model, treeTextArea);
            });
        }).start();

        treeFrame.setVisible(true);
    }
}