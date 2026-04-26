package org.example;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class WindowCreatingIndividual {

    private static int w;
    private static int h;
    private static OntModel model;
    public static LLMOntologyApp llmOntologyApp = new LLMOntologyApp();
    private static String NS = "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";
    // Хранилище чекбоксов: имя свойства → данные
    private static final Map<String, PropertyData> propertyCheckboxes = new HashMap<>();

    public WindowCreatingIndividual(int w, int h, OntModel model) {
        this.w = w;
        this.h = h;
        this.model = model;
    }

    public static void insert_start(JFrame frame) {

        JTextField textField = new JTextField(20);
        int result = JOptionPane.showConfirmDialog(
                frame,
                textField,
                "Имя родительского класса",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION){
            String nameParent = textField.getText().trim();

            // Проверка на пустой ввод
            if (nameParent == null || nameParent.isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Необходимо ввести текст!",
                        "Ошибка ввода",
                        JOptionPane.WARNING_MESSAGE
                );
            }else {
                String ParentUri = NS + nameParent;
                // Проверка на существование
                if (model.getOntClass(ParentUri) == null) {
                    JOptionPane.showMessageDialog(frame,
                            " Класс '" + nameParent + "' не существует!",
                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                }else {
                    frame.dispose();
                    JFrame frame_1 = new JFrame("Выбор свойств индивидуума");
                    NewJFrame newJFrame = new NewJFrame();
                    newJFrame.setupCloseHandler(frame, model);
                    frame_1.setSize(w, h);
                    frame_1.setLocationRelativeTo(null);
                    frame_1.setLayout(new BorderLayout(10, 10));

                    JPanel propertiesPanel = new JPanel(new BorderLayout());
                    propertiesPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(),
                            "Выберите свойства для заполнения",
                            TitledBorder.LEFT, TitledBorder.TOP));

                    // Кнопки управления
                    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JButton selectAllBtn = new JButton("Выбрать все");
                    JButton deselectAllBtn = new JButton("Снять все");
                    buttonsPanel.add(selectAllBtn);
                    buttonsPanel.add(deselectAllBtn);
                    propertiesPanel.add(buttonsPanel, BorderLayout.NORTH);

                    JPanel namePanel = new JPanel(new BorderLayout(5, 5));
                    namePanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEtchedBorder(),
                            "Имя нового индивидуума",
                            TitledBorder.LEFT, TitledBorder.TOP));

                    JTextField nameField = new JTextField();
                    nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    namePanel.add(new JLabel("Имя: "), BorderLayout.WEST);
                    namePanel.add(nameField, BorderLayout.CENTER);
                    frame_1.add(namePanel, BorderLayout.NORTH);

                    JPopupMenu popupForTextArea = new JPopupMenu();
                    JMenuItem copyItem1 = new JMenuItem("Копировать");
                    JMenuItem pasteItem1 = new JMenuItem("Вставить");
                    copyItem1.addActionListener(e -> nameField.copy());
                    pasteItem1.addActionListener(e -> nameField.paste());
                    popupForTextArea.add(copyItem1);
                    popupForTextArea.add(pasteItem1);
                    nameField.setComponentPopupMenu(popupForTextArea);

                    // Скроллируемая область с чекбоксами
                    JPanel checkboxesContainer = new JPanel();
                    checkboxesContainer.setLayout(new BoxLayout(checkboxesContainer, BoxLayout.Y_AXIS));
                    JScrollPane scrollPane = new JScrollPane(checkboxesContainer);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    propertiesPanel.add(scrollPane, BorderLayout.CENTER);

                    // Загружаем свойства из онтологии
                    llmOntologyApp.loadProperties(checkboxesContainer, model, propertyCheckboxes);

                    frame_1.add(propertiesPanel, BorderLayout.CENTER);

                    // Панель кнопок действий
                    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

                    JButton createBtn = new JButton(" Создать индивидуума");
                    createBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
                    createBtn.setBackground(new Color(70, 180, 70));
                    createBtn.setForeground(Color.WHITE);

                    JButton cancelBtn = new JButton(" Отмена");
                    cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));

                    JButton jButton_1 = new JButton("Вернуться");
                    jButton_1.addActionListener(e -> {
                        int w = frame_1.getWidth();
                        int h = frame_1.getHeight();
                        MainWindow mainWindow = new MainWindow(model, w, h);
                        mainWindow.startProgramm();
                        frame_1.dispose();
                    });

                    // Обработчики кнопок
                    selectAllBtn.addActionListener(e -> {
                        for (PropertyData pd : propertyCheckboxes.values()) {
                            pd.checkBox.setSelected(true);
                        }
                    });

                    JButton jButton_7 = new JButton("Сохранить");
                    jButton_7.addActionListener(e ->
                    {

                        llmOntologyApp.confirmAndSaveOntology(model, frame_1);

                    });

                    cancelBtn.addActionListener(e -> {

                        int w = frame_1.getWidth();
                        int h = frame_1.getHeight();

                        MainWindow mainWindow = new MainWindow(model, w, h);

                        mainWindow.startProgramm();

                        frame_1.dispose();

                    });

                    deselectAllBtn.addActionListener(e -> {
                        for (PropertyData pd : propertyCheckboxes.values()) {
                            pd.checkBox.setSelected(false);
                        }
                    });


                    createBtn.addActionListener(e -> {
                        String individualName = nameField.getText().trim();
                        // Собираем выбранные свойства
                        List<PropertyData> selected = new ArrayList<>();
                        for (PropertyData pd : propertyCheckboxes.values()) {
                            if (pd.checkBox.isSelected()) {
                                selected.add(pd);
                            }
                        }

                        // Подтверждение, если ничего не выбрано
                        if (selected.isEmpty()) {
                            int confirm = JOptionPane.showConfirmDialog(frame_1,
                                    "Вы не выбрали ни одного свойства.\nСоздать индивидуума без свойств?",
                                    "Подтверждение", JOptionPane.YES_NO_OPTION);
                            if (confirm != JOptionPane.YES_OPTION) return;
                        }

                        // Создаем индивидуума
                        String individualUri = NS + individualName;

                        // Проверка на существование
                        if (model.getIndividual(individualUri) != null) {
                            JOptionPane.showMessageDialog(frame_1,
                                    " Индивидуум '" + individualName + "' уже существует!",
                                    "Ошибка", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        int w = frame.getWidth();
                        int h = frame.getHeight();
                        WindowFillingProperties windowFillingProperties = new WindowFillingProperties(
                                w, h, model, ParentUri, individualName, selected
                        );
                        windowFillingProperties.open(frame_1);

                        frame_1.dispose();
                    });

                    actionPanel.add(createBtn);
                    actionPanel.add(cancelBtn);
                    actionPanel.add(jButton_7);
                    actionPanel.add(jButton_1);
                    frame_1.add(actionPanel, BorderLayout.SOUTH);

                    frame_1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame_1.setVisible(true);
                }

            }

        }

    }
}