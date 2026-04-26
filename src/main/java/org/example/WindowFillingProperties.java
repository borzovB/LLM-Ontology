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

public class WindowFillingProperties {

    private static final String NS =
            "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";
    private static int w;
    private static int h;
    private static OntModel model;
    private static String parentUri;
    private static String individualName;
    private static List<PropertyData> selected;

    public WindowFillingProperties(int w, int h, OntModel model, String parentUri,
                                   String individualName, List<PropertyData> selected) {
        this.w = w;
        this.h = h;
        this.model = model;
        this.parentUri = parentUri;
        this.individualName = individualName;
        this.selected = selected;
    }

    public static void open(JFrame parentFrame) {

        LLMOntologyApp llmOntologyApp = new LLMOntologyApp();

        JFrame valueFrame = new JFrame("Заполнение значений свойств");
        NewJFrame newJFrame = new NewJFrame();
        newJFrame.setupCloseHandler(valueFrame, model);
        valueFrame.setSize(w, h);
        valueFrame.setLocationRelativeTo(parentFrame);
        valueFrame.setLayout(new BorderLayout(10, 10));

        // Главная панель со скроллом
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        // Хранилище полей:
        // имя свойства -> поле ввода значения
        Map<String, JTextField> valueFields = new HashMap<>();

        // Создаем строки для каждого свойства
        for (PropertyData pd : selected) {

            JPanel rowPanel = new JPanel(new GridLayout(1, 3, 10, 5));
            rowPanel.setBorder(
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            );
            rowPanel.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 50)
            );

            // Название свойства
            JLabel propertyNameLabel =
                    new JLabel(pd.property.getLocalName());

            // Тип свойства
            String typeText = "unknown";

            ExtendedIterator<? extends org.apache.jena.rdf.model.RDFNode> rangeIter = pd.property.listRange();
            try {
                while (rangeIter.hasNext()) {
                    org.apache.jena.rdf.model.RDFNode node = rangeIter.next();

                    // Проверяем, что это ресурс (класс), а не тип данных
                    if (node.isResource()) {
                        org.apache.jena.rdf.model.Resource res = node.asResource();

                        // Пытаемся получить как класс
                        if (res.canAs(org.apache.jena.ontology.OntClass.class)) {
                            org.apache.jena.ontology.OntClass rangeCls = res.as(org.apache.jena.ontology.OntClass.class);
                            typeText = rangeCls.getLocalName() != null
                                    ? rangeCls.getLocalName()
                                    : rangeCls.getURI();
                            break; // берём первый подходящий класс
                        }
                    }
                }
            } finally {
                rangeIter.close();
            }

            JLabel propertyTypeLabel =
                    new JLabel(typeText);

            // Поле ввода значения
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

            container.add(rowPanel);
        }

        valueFrame.add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель кнопок
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER)
        );

        JButton saveButton = new JButton("Внести изменение");
        JButton cancelButton = new JButton("Отмена");

        JButton jButton_7 = new JButton("Сохранить");
        jButton_7.addActionListener(e ->
        {

            llmOntologyApp.confirmAndSaveOntology(model, valueFrame);

        });

        JButton jButton_8 = new JButton("Вывести дерево с классами и индивидуумами");
        jButton_8.addActionListener(e ->
        {

            WindowClassTree windowClassTree = new WindowClassTree(valueFrame, model, llmOntologyApp);
            windowClassTree.open();

        });

        JButton jButton_1 = new JButton("Вернуться");
        jButton_1.addActionListener(e -> {
            int w = valueFrame.getWidth();
            int h = valueFrame.getHeight();
            MainWindow mainWindow = new MainWindow(model, w, h);
            mainWindow.startProgramm();
            valueFrame.dispose();
        });

        saveButton.addActionListener(e -> {

            String individualUri = NS + individualName;

            // Получаем родительский класс
            OntClass parentClass =
                    model.getOntClass(parentUri);

            // Создаем индивидуума
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

                    // ObjectProperty → создаем ресурс
                    Resource linkedResource =
                            model.createResource(NS + value);

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
                    "Индивидуум '" + individualName + "' успешно создан!",
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE
            );

            valueFrame.dispose();

            int w = valueFrame.getWidth();
            int h = valueFrame.getHeight();
            MainWindow mainWindow = new MainWindow(model, w, h);

            mainWindow.startProgramm();
        });

        cancelButton.addActionListener(
                e -> {
                    MainWindow mainWindow = new MainWindow(model, w, h);

                    mainWindow.startProgramm();
                    valueFrame.dispose();
                }
        );

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(jButton_7);
        buttonPanel.add(jButton_1);
        buttonPanel.add(jButton_8);

        valueFrame.add(buttonPanel, BorderLayout.SOUTH);

        valueFrame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        valueFrame.setVisible(true);
    }
}