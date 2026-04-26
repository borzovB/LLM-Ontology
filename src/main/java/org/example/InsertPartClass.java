package org.example;

import org.apache.jena.ontology.OntModel;
import javax.swing.*;
import java.awt.*;

public class InsertPartClass {

    private int w;
    private int h;
    private OntModel model;
    public LLMOntologyApp llmOntologyApp = new LLMOntologyApp();

    public InsertPartClass(int w, int h, OntModel model) {
        this.w = w;
        this.h = h;
        this.model = model;
    }

    public void insert_start() {
        JFrame frame = new JFrame("Вставка класса");

        // --- Компоненты ---
        JTextArea jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);

        JTextArea jTextArea_2 = new JTextArea();
        jTextArea_2.setLineWrap(true);
        jTextArea_2.setWrapStyleWord(true);

        JTextArea jTextArea_print = new JTextArea();
        jTextArea_print.setLineWrap(true);
        jTextArea_print.setWrapStyleWord(true);
        jTextArea_print.setEditable(false);

        // --- Контекстное меню для jTextArea (копировать/вставить) ---
        JPopupMenu popupForTextArea = new JPopupMenu();
        JMenuItem copyItem1 = new JMenuItem("Копировать");
        JMenuItem pasteItem1 = new JMenuItem("Вставить");
        copyItem1.addActionListener(e -> jTextArea.copy());
        pasteItem1.addActionListener(e -> jTextArea.paste());
        popupForTextArea.add(copyItem1);
        popupForTextArea.add(pasteItem1);
        jTextArea.setComponentPopupMenu(popupForTextArea);

        // --- Контекстное меню для jTextArea_2 (копировать/вставить) ---
        JPopupMenu popupForTextArea2 = new JPopupMenu();
        JMenuItem copyItem2 = new JMenuItem("Копировать");
        JMenuItem pasteItem2 = new JMenuItem("Вставить");
        copyItem2.addActionListener(e -> jTextArea_2.copy());
        pasteItem2.addActionListener(e -> jTextArea_2.paste());
        popupForTextArea2.add(copyItem2);
        popupForTextArea2.add(pasteItem2);
        jTextArea_2.setComponentPopupMenu(popupForTextArea2);

        // --- Контекстное меню для вывода (только копировать) ---
        JPopupMenu popupForPrint = new JPopupMenu();
        JMenuItem copyPrint = new JMenuItem("Копировать");
        copyPrint.addActionListener(e -> jTextArea_print.copy());
        popupForPrint.add(copyPrint);
        jTextArea_print.setComponentPopupMenu(popupForPrint);

        // === Панели ввода ===
        JPanel j_in = new JPanel(new BorderLayout());
        j_in.setBorder(BorderFactory.createTitledBorder("Ввод имени класса"));
        j_in.add(new JScrollPane(jTextArea), BorderLayout.CENTER);

        JPanel j_in_2 = new JPanel(new BorderLayout());
        j_in_2.setBorder(BorderFactory.createTitledBorder("Родительский класс"));
        j_in_2.add(new JScrollPane(jTextArea_2), BorderLayout.CENTER);

        JPanel j_out = new JPanel(new BorderLayout());
        j_out.setBorder(BorderFactory.createTitledBorder("Вывод"));
        j_out.add(new JScrollPane(jTextArea_print), BorderLayout.CENTER);

        // === Кнопки ===
        JPanel bat = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton jButton = new JButton("Вставить класс");
        jButton.addActionListener(e -> {
            jTextArea_print.setText("");
            String newClassName = jTextArea.getText().trim();
            String parentClassName = jTextArea_2.getText().trim();
            llmOntologyApp.createSubClassSafely(model, parentClassName, newClassName, jTextArea_print);
        });
        bat.add(jButton);

        JButton jButton_2 = new JButton("Вывести дерево");
        jButton_2.addActionListener(e -> {
            jTextArea_print.setText("");
            llmOntologyApp.printFullClassHierarchy(model, jTextArea_print);
        });
        bat.add(jButton_2);

        JButton jButton_1 = new JButton("Вернуться");
        jButton_1.addActionListener(e -> {
            int w = frame.getWidth();
            int h = frame.getHeight();
            MainWindow mainWindow = new MainWindow(model, w, h);
            mainWindow.startProgramm();
            frame.dispose();
        });
        bat.add(jButton_1);

        JButton jButton_3 = new JButton("Удалить объект");
        jButton_3.addActionListener(e ->
        {

            llmOntologyApp.delete(model, jTextArea_print, frame);

        });
        bat.add(jButton_3);

        JButton jButton_7 = new JButton("Сохранить");
        jButton_7.addActionListener(e ->
        {

            jTextArea_print.setText("");
            llmOntologyApp.saveWithConfirmation(model, frame, jTextArea_print);

        });
        bat.add(jButton_7);

        JButton jButton_5 = new JButton("Вставить индивидуума");
        jButton_5.addActionListener(e ->
        {

            int w = frame.getWidth();
            int h = frame.getHeight();

            WindowCreatingIndividual windowCreatingIndividual = new WindowCreatingIndividual(w, h, model);
            windowCreatingIndividual.insert_start(frame);

        });
        bat.add(jButton_5);

        // === Главная панель с GridBagLayout ===
        JPanel jPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;

        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.80;
        jPanel.add(j_out, gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.05;
        jPanel.add(j_in, gbc);

        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.05;
        jPanel.add(j_in_2, gbc);

        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.10;
        jPanel.add(bat, gbc);

        frame.add(jPanel);

        NewJFrame newJFrame = new NewJFrame();

        newJFrame.setupCloseHandler(frame, model);
        frame.setSize(w, h);

        jTextArea_print.setText("");
        llmOntologyApp.printFullClassHierarchy(model, jTextArea_print);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}