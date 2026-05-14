package org.example;

import org.apache.jena.ontology.OntModel;
import javax.swing.*;
import java.awt.*;

// Окно для вставки нового класса в онтологию.
// Отображает иерархию классов и позволяет добавлять подклассы.
public class InsertPartClass {

    // Константы
    private static final int INPUT_PANEL_HEIGHT_WEIGHT = 5;   // % высоты для полей ввода
    private static final int OUTPUT_PANEL_HEIGHT_WEIGHT = 80;  // % высоты для области вывода
    private static final int BUTTON_PANEL_HEIGHT_WEIGHT = 10;  // % высоты для кнопок
    private static final Insets PANEL_INSETS = new Insets(5, 5, 5, 5);

    // Поля
    private final int w;
    private final int h;
    private final OntModel model;
    private final LLMOntologyApp app = new LLMOntologyApp();

    // Конструктор
    public InsertPartClass(int w, int h, OntModel model) {
        this.w = w;
        this.h = h;
        this.model = model;
    }

    // Точка входа — создаёт и отображает окно
    public void insert_start() {
        JFrame frame = createFrame();
        JTextArea classNameInput  = createInputArea();
        JTextArea parentNameInput = createInputArea();
        JTextArea outputArea       = createOutputArea();

        // Навешиваем контекстные меню (копировать / вставить)
        attachEditMenu(classNameInput,  true);
        attachEditMenu(parentNameInput, true);
        attachEditMenu(outputArea,       false); // только копировать

        JPanel root = buildLayout(frame, classNameInput, parentNameInput, outputArea);
        frame.add(root);

        // Подключаем обработчик закрытия окна с предложением сохранить
        new NewJFrame().setupCloseHandler(frame, model);

        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Выводим иерархию при открытии окна
        app.printFullClassHierarchy(model, outputArea);

        frame.setVisible(true);
    }

    // Создание компонентов

    // Создаёт главное окно с заголовком
    private JFrame createFrame() {
        return new JFrame("Вставка класса");
    }

    // Создаёт редактируемую текстовую область с переносом строк
    private JTextArea createInputArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    // Создаёт нередактируемую область для вывода результатов
    private JTextArea createOutputArea() {
        JTextArea area = createInputArea();
        area.setEditable(false);
        return area;
    }

    // Навешивает контекстное меню: если withPaste=true — «Копировать» + «Вставить»,
    // иначе только «Копировать»
    private void attachEditMenu(JTextArea area, boolean withPaste) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copy = new JMenuItem("Копировать");
        copy.addActionListener(e -> area.copy());
        menu.add(copy);
        if (withPaste) {
            JMenuItem paste = new JMenuItem("Вставить");
            paste.addActionListener(e -> area.paste());
            menu.add(paste);
        }
        area.setComponentPopupMenu(menu);
    }

    // Сборка интерфейса

    // Собирает корневую панель: вывод → поля ввода → кнопки
    private JPanel buildLayout(JFrame frame,
                               JTextArea classInput,
                               JTextArea parentInput,
                               JTextArea output) {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = defaultGbc();

        // Строка 0 — область вывода (занимает большую часть высоты)
        gbc.gridy = 0; gbc.weighty = OUTPUT_PANEL_HEIGHT_WEIGHT / 100.0;
        root.add(titledScrollPanel("Вывод", output), gbc);

        // Строка 1 — поле для имени нового класса
        gbc.gridy = 1; gbc.weighty = INPUT_PANEL_HEIGHT_WEIGHT / 100.0;
        root.add(titledScrollPanel("Имя нового класса", classInput), gbc);

        // Строка 2 — поле для имени родительского класса
        gbc.gridy = 2; gbc.weighty = INPUT_PANEL_HEIGHT_WEIGHT / 100.0;
        root.add(titledScrollPanel("Родительский класс", parentInput), gbc);

        // Строка 3 — панель кнопок
        gbc.gridy = 3; gbc.weighty = BUTTON_PANEL_HEIGHT_WEIGHT / 100.0;
        root.add(buildButtonPanel(frame, classInput, parentInput, output), gbc);

        return root;
    }

    // Оборачивает компонент в панель с прокруткой и заголовком рамки
    private JPanel titledScrollPanel(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    // Возвращает преднастроенный GridBagConstraints:
    // растяжение по обеим осям, единая ширина колонки
    private GridBagConstraints defaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.insets  = PANEL_INSETS;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;
        return gbc;
    }

    // Панель кнопок

    // Создаёт панель со всеми кнопками управления
    private JPanel buildButtonPanel(JFrame frame,
                                    JTextArea classInput,
                                    JTextArea parentInput,
                                    JTextArea output) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));

        // «Вставить класс» — добавляет подкласс в онтологию
        panel.add(button("Вставить класс", e -> {
            output.setText("");
            app.createSubClassSafely(model,
                    parentInput.getText().trim(),
                    classInput.getText().trim(),
                    output);
        }));

        // «Вывести дерево» — отображает полную иерархию классов
        panel.add(button("Вывести дерево", e -> {
            output.setText("");
            app.printFullClassHierarchy(model, output);
        }));

        // «Вставить индивидуума» — открывает окно создания индивидуума
        panel.add(button("Вставить индивидуума", e -> {
            new WindowCreatingIndividual(frame.getWidth(), frame.getHeight(), model)
                    .insert_start(frame);
        }));

        // «Удалить объект» — удаляет класс или индивидуума по имени
        panel.add(button("Удалить объект", e ->
                app.delete(model, output, frame)));

        // «Сохранить» — сохраняет онтологию с подтверждением
        panel.add(button("Сохранить", e -> {
            output.setText("");
            app.saveWithConfirmation(model, frame, output);
        }));

        // «Вернуться» — закрывает окно и возвращает в главное меню
        panel.add(button("Вернуться", e -> {
            new MainWindow(model, frame.getWidth(), frame.getHeight())
                    .startProgramm();
            frame.dispose();
        }));

        return panel;
    }

    // Фабричный метод — создаёт кнопку с переданным слушателем
    private JButton button(String label, java.awt.event.ActionListener action) {
        JButton btn = new JButton(label);
        btn.addActionListener(action);
        return btn;
    }
}