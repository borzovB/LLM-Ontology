package org.example;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Окно заполнения значений свойств нового индивидуума.
// После выбора свойств пользователь вводит значения,
// после чего создаётся новый индивидуум в онтологии.
public class WindowFillingProperties {


// Константы


    private static final String WINDOW_TITLE =
            "Заполнение значений свойств";

    private static final String NS =
            "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";


// Поля


    private static int w;
    private static int h;
    private static OntModel model;

    // URI родительского класса
    private static String parentUri;

    // Имя создаваемого индивидуума
    private static String individualName;

    // Выбранные свойства для заполнения
    private static List<PropertyData> selected;


// Конструктор


    public WindowFillingProperties(
            int w,
            int h,
            OntModel model,
            String parentUri,
            String individualName,
            List<PropertyData> selected
    ) {
        WindowFillingProperties.w = w;
        WindowFillingProperties.h = h;
        WindowFillingProperties.model = model;
        WindowFillingProperties.parentUri = parentUri;
        WindowFillingProperties.individualName = individualName;
        WindowFillingProperties.selected = selected;
    }


// Открытие окна


    // Открывает окно заполнения значений свойств.
    public static void open(JFrame parentFrame) {

        LLMOntologyApp llmOntologyApp = new LLMOntologyApp();

        JFrame valueFrame = createMainFrame(parentFrame);

        // Хранилище полей:
        // имя свойства → поле ввода значения
        Map<String, JTextField> valueFields = new HashMap<>();

        JPanel propertiesPanel =
                createPropertiesPanel(valueFields);

        valueFrame.add(
                new JScrollPane(propertiesPanel),
                BorderLayout.CENTER
        );

        JPanel buttonPanel =
                createButtonPanel(
                        valueFrame,
                        valueFields,
                        llmOntologyApp
                );

        valueFrame.add(buttonPanel, BorderLayout.SOUTH);

        valueFrame.setVisible(true);
    }


// Создание компонентов


    // Создаёт основное окно.
    private static JFrame createMainFrame(JFrame parentFrame) {

        JFrame frame = new JFrame(WINDOW_TITLE);

        new NewJFrame().setupCloseHandler(frame, model);

        frame.setSize(w, h);
        frame.setLocationRelativeTo(parentFrame);
        frame.setLayout(new BorderLayout(10, 10));

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        return frame;
    }

    // Создаёт прокручиваемую панель
    // со строками заполнения свойств.
    private static JPanel createPropertiesPanel(
            Map<String, JTextField> valueFields
    ) {
        JPanel container = new JPanel();

        container.setLayout(
                new BoxLayout(container, BoxLayout.Y_AXIS)
        );

        for (PropertyData pd : selected) {
            JPanel rowPanel =
                    createPropertyRow(pd, valueFields);

            container.add(rowPanel);
        }

        return container;
    }

    // Создаёт одну строку:
    // название свойства + тип + поле ввода.
    private static JPanel createPropertyRow(
            PropertyData pd,
            Map<String, JTextField> valueFields
    ) {
        JPanel rowPanel =
                new JPanel(new GridLayout(1, 3, 10, 5));

        rowPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        5, 10, 5, 10
                )
        );

        rowPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        50
                )
        );

        JLabel propertyNameLabel =
                new JLabel(
                        pd.property.getLocalName()
                );

        JLabel propertyTypeLabel =
                new JLabel(
                        getPropertyRangeType(pd)
                );

        JTextField valueField = new JTextField();

        valueField.setPreferredSize(
                new Dimension(200, 30)
        );

        valueFields.put(
                pd.property.getLocalName(),
                valueField
        );

        rowPanel.add(propertyNameLabel);
        rowPanel.add(propertyTypeLabel);
        rowPanel.add(valueField);

        return rowPanel;
    }

    // Создаёт нижнюю панель кнопок управления.
    private static JPanel createButtonPanel(
            JFrame valueFrame,
            Map<String, JTextField> valueFields,
            LLMOntologyApp llmOntologyApp
    ) {
        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER
                        )
                );

        JButton createButton =
                new JButton("Внести изменение");

        createButton.setBackground(new Color(70, 180, 70));
        createButton.setForeground(Color.WHITE);
        createButton.setFont(new Font("SansSerif", Font.BOLD, 13));

        JButton cancelButton =
                new JButton("Отмена");

        JButton saveOntologyButton =
                new JButton("Сохранить");

        JButton showTreeButton =
                new JButton(
                        "Вывести дерево с классами и индивидуумами"
                );

        JButton backButton =
                new JButton("Вернуться");

        createButton.addActionListener(e ->
                createIndividual(
                        valueFrame,
                        valueFields
                )
        );

        cancelButton.addActionListener(e -> {
            MainWindow mainWindow =
                    new MainWindow(model, w, h);

            mainWindow.startProgramm();
            valueFrame.dispose();
        });

        saveOntologyButton.addActionListener(e ->
                llmOntologyApp.confirmAndSaveOntology(
                        model,
                        valueFrame
                )
        );

        showTreeButton.addActionListener(e -> {
            WindowClassTree windowClassTree =
                    new WindowClassTree(
                            valueFrame,
                            model,
                            llmOntologyApp
                    );

            windowClassTree.open();
        });

        backButton.addActionListener(e -> {
            MainWindow mainWindow =
                    new MainWindow(
                            model,
                            valueFrame.getWidth(),
                            valueFrame.getHeight()
                    );

            mainWindow.startProgramm();
            valueFrame.dispose();
        });

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveOntologyButton);
        buttonPanel.add(backButton);
        buttonPanel.add(showTreeButton);

        return buttonPanel;
    }


// Логика создания индивидуума


    // Создаёт нового индивидуума
    // и заполняет его выбранными свойствами.
    private static void createIndividual(
            JFrame valueFrame,
            Map<String, JTextField> valueFields
    ) {
        String individualUri =
                NS + individualName;

        // Получаем родительский класс
        OntClass parentClass =
                model.getOntClass(parentUri);

        // Создаём индивидуума
        Individual newIndividual =
                model.createIndividual(
                        individualUri,
                        parentClass
                );

        // Заполняем свойства
        for (PropertyData pd : selected) {

            String propertyName =
                    pd.property.getLocalName();

            String value =
                    valueFields.get(propertyName)
                            .getText()
                            .trim();

            // Пустые значения пропускаем
            if (value.isEmpty()) {
                continue;
            }

            if (pd.isObjectProperty) {

                // ObjectProperty → ссылка на ресурс
                Resource linkedResource =
                        model.createResource(
                                NS + value
                        );

                newIndividual.addProperty(
                        pd.property,
                        linkedResource
                );

            } else {

                // DatatypeProperty → литерал
                newIndividual.addLiteral(
                        pd.property,
                        value
                );
            }
        }

        JOptionPane.showMessageDialog(
                valueFrame,
                "Индивидуум '" + individualName +
                        "' успешно создан!",
                "Успех",
                JOptionPane.INFORMATION_MESSAGE
        );

        valueFrame.dispose();

        MainWindow mainWindow =
                new MainWindow(
                        model,
                        valueFrame.getWidth(),
                        valueFrame.getHeight()
                );

        mainWindow.startProgramm();
    }


// Вспомогательные методы


    // Определяет тип свойства
    // по диапазону range в онтологии.
    private static String getPropertyRangeType(
            PropertyData pd
    ) {
        String typeText = "unknown";

        ExtendedIterator<? extends org.apache.jena.rdf.model.RDFNode>
                rangeIter = pd.property.listRange();

        try {
            while (rangeIter.hasNext()) {

                org.apache.jena.rdf.model.RDFNode node =
                        rangeIter.next();

                // Проверяем, что это ресурс
                if (node.isResource()) {

                    org.apache.jena.rdf.model.Resource res =
                            node.asResource();

                    // Проверяем, можно ли привести к классу
                    if (res.canAs(OntClass.class)) {

                        OntClass rangeClass =
                                res.as(OntClass.class);

                        typeText =
                                rangeClass.getLocalName() != null
                                        ? rangeClass.getLocalName()
                                        : rangeClass.getURI();

                        break;
                    }
                }
            }

        } finally {
            rangeIter.close();
        }

        return typeText;
    }
}