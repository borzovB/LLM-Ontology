package org.example;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import javax.swing.*;
import java.awt.*;

public class MainWindow {

    public static LLMOntologyApp llmOntologyApp = new LLMOntologyApp();
    private static OntModel model;
    private static int w;
    private static int h;

    public MainWindow(OntModel model, int w, int h) {
        this.model = model;
        this.w = w;
        this.h = h;
    }

    public static void startProgramm(){
        JFrame frame = new JFrame("Антология");

        // --- Компоненты ---
        JTextArea jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);

        JPopupMenu jPopupMenu = new JPopupMenu();
        JPopupMenu jPopupMenu_2 = new JPopupMenu();

        JMenuItem jMenuBar  = new JMenuItem("Копировать");
        JMenuItem jMenuBar_2  = new JMenuItem("Вставить");
        JMenuItem jMenuBar_3  = new JMenuItem("Копировать");
        jMenuBar.addActionListener(e -> jTextArea.copy());
        jMenuBar_2.addActionListener(e -> jTextArea.paste());

        jTextArea.setComponentPopupMenu(jPopupMenu);

        JTextArea jTextArea_print = new JTextArea();
        jTextArea_print.setLineWrap(true);
        jTextArea_print.setWrapStyleWord(true);
        jTextArea_print.setEditable(false);

        jMenuBar_3.addActionListener(e -> jTextArea_print.copy());


        jTextArea_print.setEditable(false);

        jPopupMenu.add(jMenuBar);
        jPopupMenu.add(jMenuBar_2);
        jPopupMenu_2.add(jMenuBar_3);
        jTextArea.setComponentPopupMenu(jPopupMenu);
        jTextArea_print.setComponentPopupMenu(jPopupMenu_2);

        JPanel j_in = new JPanel(new BorderLayout());
        j_in.setBorder(BorderFactory.createTitledBorder("Ввод"));
        j_in.add(new JScrollPane(jTextArea), BorderLayout.CENTER);

        JPanel j_out = new JPanel(new BorderLayout());
        j_out.setBorder(BorderFactory.createTitledBorder("Вывод"));
        j_out.add(new JScrollPane(jTextArea_print), BorderLayout.CENTER);

        JPanel bat = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton jButton = new JButton("Вывести классы");
        jButton.addActionListener(e ->
        {
            jTextArea_print.setText("");
            llmOntologyApp.print_name_class(model, jTextArea_print);
        });
        bat.add(jButton);

        JButton jButton_1 = new JButton("Вывести дерево с классами и индивидуумами");
        jButton_1.addActionListener(e ->
        {
            jTextArea_print.setText("");
            llmOntologyApp.printFullClassHierarchy(model, jTextArea_print);
        });
        bat.add(jButton_1);

        JButton jButton_2 = new JButton("Вывести индивидуумов");
        jButton_2.addActionListener(e ->
        {
            jTextArea_print.setText("");
            llmOntologyApp.print_Ind(model, jTextArea_print);
        });
        bat.add(jButton_2);

        JButton jButton_3 = new JButton("Вывести свойства индивидуумов");
        jButton_3.addActionListener(e ->
        {

            String text = jTextArea.getText();
            jTextArea_print.setText("");
            llmOntologyApp.print_Ind_Prop(model, jTextArea_print, text);
        });
        bat.add(jButton_3);

        JButton jButton_4 = new JButton("Вставить класс");
        jButton_4.addActionListener(e ->
        {

            int w = frame.getWidth();
            int h = frame.getHeight();

            InsertPartClass insertPartClass = new InsertPartClass(w, h, model);
            insertPartClass.insert_start();
            frame.dispose();

        });
        bat.add(jButton_4);

        JButton jButton_5 = new JButton("Вставить индивидуума");
        jButton_5.addActionListener(e ->
        {

            int w = frame.getWidth();
            int h = frame.getHeight();

            WindowCreatingIndividual windowCreatingIndividual = new WindowCreatingIndividual(w, h, model);
            windowCreatingIndividual.insert_start(frame);

        });
        bat.add(jButton_5);

        JButton jButton_6 = new JButton("Удалить объект");
        jButton_6.addActionListener(e ->
        {

            llmOntologyApp.delete(model, jTextArea_print, frame);

        });
        bat.add(jButton_6);

        JButton jButton_7 = new JButton("Сохранить");
        jButton_7.addActionListener(e ->
        {

            jTextArea_print.setText("");
            llmOntologyApp.saveWithConfirmation(model, frame, jTextArea_print);

        });
        bat.add(jButton_7);

        // --- Главная панель с GridBagLayout ---
        JPanel jPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // растягивать по ширине и высоте
        gbc.insets = new Insets(5, 5, 5, 5); // отступы между панелями

        // j_out — верхняя, занимает 60% высоты
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.87; // 87%
        jPanel.add(j_out, gbc);

        // j_in — средняя, занимает 30% высоты
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.03; // 3%
        jPanel.add(j_in, gbc);

        // bat — нижняя, занимает 10% высоты
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1; // 10%
        jPanel.add(bat, gbc);

        frame.add(jPanel);

        NewJFrame newJFrame = new NewJFrame();

        newJFrame.setupCloseHandler(frame, model);

        frame.setSize(w, h);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
