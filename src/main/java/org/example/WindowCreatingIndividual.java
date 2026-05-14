package org.example;

import org.apache.jena.ontology.OntModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

// Окно создания нового индивидуума.
// Сначала запрашивается родительский класс,
// затем открывается форма выбора свойств и имени индивидуума.
public class WindowCreatingIndividual {


// Константы


    private static final String WINDOW_TITLE = "Выбор свойств индивидуума";
    private static final String NS =
            "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";

    // Хранилище чекбоксов:
    // имя свойства → данные о свойстве
    private static final Map<String, PropertyData> propertyCheckboxes =
            new HashMap<>();


// Поля


    private static int w;
    private static int h;
    private static OntModel model;

    // Основной обработчик логики онтологии
    public static LLMOntologyApp llmOntologyApp = new LLMOntologyApp();


// Конструктор


    public WindowCreatingIndividual(int w, int h, OntModel model) {
        WindowCreatingIndividual.w = w;
        WindowCreatingIndividual.h = h;
        WindowCreatingIndividual.model = model;
    }


// Запуск окна


    // Запрашивает имя родительского класса
    // и открывает окно создания индивидуума.
    public static void insert_start(JFrame frame) {

        JTextField textField = new JTextField(20);

        int result = JOptionPane.showConfirmDialog(
                frame,
                textField,
                "Имя родительского класса",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String nameParent = textField.getText().trim();

        // Проверка на пустой ввод
        if (nameParent.isEmpty()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Необходимо ввести имя класса!",
                    "Ошибка ввода",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String parentUri = NS + nameParent;

        // Проверка существования класса
        if (model.getOntClass(parentUri) == null) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Класс '" + nameParent + "' не существует!",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        openCreationWindow(frame, parentUri);
    }


// Создание основного окна


    // Открывает основное окно выбора свойств
    // и задания имени нового индивидуума.
    private static void openCreationWindow(JFrame parentFrame, String parentUri) {

        parentFrame.dispose();

        JFrame frame = new JFrame(WINDOW_TITLE);

        new NewJFrame().setupCloseHandler(frame, model);

        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        frame.add(createNamePanel(frame), BorderLayout.NORTH);
        frame.add(createPropertiesPanel(), BorderLayout.CENTER);
        frame.add(createActionPanel(frame, parentUri), BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }


// Создание компонентов


    // Панель ввода имени нового индивидуума.
    private static JPanel createNamePanel(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Имя нового индивидуума",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));

        JTextField nameField = new JTextField();
        nameField.setName("individualNameField");
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(new JLabel("Имя: "), BorderLayout.WEST);
        panel.add(nameField, BorderLayout.CENTER);

        addTextPopupMenu(nameField);

        return panel;
    }

    // Панель выбора свойств с чекбоксами.
    private static JPanel createPropertiesPanel() {
        JPanel propertiesPanel = new JPanel(new BorderLayout());

        propertiesPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Выберите свойства для заполнения",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton selectAllBtn = new JButton("Выбрать все");
        JButton deselectAllBtn = new JButton("Снять все");

        selectAllBtn.addActionListener(e -> {
            for (PropertyData pd : propertyCheckboxes.values()) {
                pd.checkBox.setSelected(true);
            }
        });

        deselectAllBtn.addActionListener(e -> {
            for (PropertyData pd : propertyCheckboxes.values()) {
                pd.checkBox.setSelected(false);
            }
        });

        buttonsPanel.add(selectAllBtn);
        buttonsPanel.add(deselectAllBtn);

        JPanel checkboxesContainer = new JPanel();
        checkboxesContainer.setLayout(
                new BoxLayout(checkboxesContainer, BoxLayout.Y_AXIS)
        );

        JScrollPane scrollPane = new JScrollPane(checkboxesContainer);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        // Загружаем свойства из онтологии
        llmOntologyApp.loadProperties(
                checkboxesContainer,
                model,
                propertyCheckboxes
        );

        propertiesPanel.add(buttonsPanel, BorderLayout.NORTH);
        propertiesPanel.add(scrollPane, BorderLayout.CENTER);

        return propertiesPanel;
    }

    // Нижняя панель кнопок действий.
    private static JPanel createActionPanel(JFrame frame, String parentUri) {
        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 10)
        );

        JButton createBtn = new JButton("Создать индивидуума");
        JButton cancelBtn = new JButton("Отмена");
        JButton saveBtn = new JButton("Сохранить");
        JButton backBtn = new JButton("Вернуться");

        createBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        createBtn.setBackground(new Color(70, 180, 70));
        createBtn.setForeground(Color.WHITE);

        createBtn.addActionListener(e ->
                handleCreateIndividual(frame, parentUri));

        cancelBtn.addActionListener(e -> {
            MainWindow mainWindow =
                    new MainWindow(model, frame.getWidth(), frame.getHeight());

            mainWindow.startProgramm();
            frame.dispose();
        });

        saveBtn.addActionListener(e ->
                llmOntologyApp.confirmAndSaveOntology(model, frame));

        backBtn.addActionListener(e -> {
            MainWindow mainWindow =
                    new MainWindow(model, frame.getWidth(), frame.getHeight());

            mainWindow.startProgramm();
            frame.dispose();
        });

        panel.add(createBtn);
        panel.add(cancelBtn);
        panel.add(saveBtn);
        panel.add(backBtn);

        return panel;
    }


// Логика создания индивидуума


    // Обрабатывает создание нового индивидуума.
    private static void handleCreateIndividual(
            JFrame frame,
            String parentUri
    ) {
        JTextField nameField = findTextField(frame);

        if (nameField == null) {
            return;
        }

        String individualName = nameField.getText().trim();

        if (individualName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Введите имя индивидуума!",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        List<PropertyData> selected = new ArrayList<>();

        for (PropertyData pd : propertyCheckboxes.values()) {
            if (pd.checkBox.isSelected()) {
                selected.add(pd);
            }
        }

        // Подтверждение при отсутствии выбранных свойств
        if (selected.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Вы не выбрали ни одного свойства.\n" +
                            "Создать индивидуума без свойств?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String individualUri = NS + individualName;

        // Проверка существования индивидуума
        if (model.getIndividual(individualUri) != null) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Индивидуум '" + individualName + "' уже существует!",
                    "Ошибка",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        WindowFillingProperties window =
                new WindowFillingProperties(
                        frame.getWidth(),
                        frame.getHeight(),
                        model,
                        parentUri,
                        individualName,
                        selected
                );

        window.open(frame);
        frame.dispose();
    }


// Вспомогательные методы


    // Добавляет контекстное меню
    // «Копировать / Вставить».
    private static void addTextPopupMenu(JTextField field) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("Копировать");
        JMenuItem pasteItem = new JMenuItem("Вставить");

        copyItem.addActionListener(e -> field.copy());
        pasteItem.addActionListener(e -> field.paste());

        popup.add(copyItem);
        popup.add(pasteItem);

        field.setComponentPopupMenu(popup);
    }

    // Поиск текстового поля по имени.
    private static JTextField findTextField(JFrame frame) {
        for (Component component : frame.getContentPane().getComponents()) {
            if (component instanceof JPanel panel) {
                for (Component child : panel.getComponents()) {
                    if (child instanceof JTextField textField &&
                            "individualNameField".equals(textField.getName())) {
                        return textField;
                    }
                }
            }
        }
        return null;
    }
}