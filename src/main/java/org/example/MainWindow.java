package org.example;

import org.apache.jena.ontology.OntModel;
import javax.swing.*;
import java.awt.*;

// Главное окно приложения — точка входа в UI.
// Содержит панель вывода, поле ввода имени индивидуума и все кнопки управления.
public class MainWindow {


// Константы


    // Заголовок главного окна
    private static final String WINDOW_TITLE = "Онтология";

    // Доля высоты для каждой строки GridBagLayout
    private static final double WEIGHT_OUTPUT  = 0.87; // область вывода — 87%
    private static final double WEIGHT_INPUT   = 0.03; // поле ввода — 3%
    private static final double WEIGHT_BUTTONS = 0.10; // панель кнопок — 10%

    private static final Insets PANEL_INSETS = new Insets(5, 5, 5, 5);


// Поля


    private static final LLMOntologyApp app = new LLMOntologyApp();

    // Модель онтологии, разделяемая между всеми окнами приложения
    private static OntModel model;
    private static int w;
    private static int h;


// Конструктор


    public MainWindow(OntModel model, int w, int h) {
        MainWindow.model = model;
        MainWindow.w = w;
        MainWindow.h = h;
    }


// Точка входа — создаёт и показывает главное окно

    public static void startProgramm() {
        JFrame frame = new JFrame(WINDOW_TITLE);

        // Создаём текстовые области и навешиваем контекстные меню
        JTextArea inputArea  = createInputArea();
        JTextArea outputArea = createOutputArea();
        attachEditMenu(inputArea,  true);   // копировать + вставить
        attachEditMenu(outputArea, false);  // только копировать

        // Собираем корневую панель и добавляем в окно
        frame.add(buildLayout(frame, inputArea, outputArea));

        // Подключаем обработчик закрытия с предложением сохранить онтологию
        new NewJFrame().setupCloseHandler(frame, model);

        frame.setSize(w, h);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


// Создание компонентов


    // Создаёт редактируемую текстовую область с переносом строк.
    private static JTextArea createInputArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    // Создаёт нередактируемую область для вывода результатов.
    private static JTextArea createOutputArea() {
        JTextArea area = createInputArea();
        area.setEditable(false);
        return area;
    }

    // Навешивает контекстное меню на текстовую область.
    // withPaste=true → «Копировать» + «Вставить», иначе только «Копировать».
    private static void attachEditMenu(JTextArea area, boolean withPaste) {
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


    // Собирает корневую панель GridBagLayout: вывод - ввод - кнопки.
    private static JPanel buildLayout(JFrame frame,
                                      JTextArea input,
                                      JTextArea output) {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = defaultGbc();

        // Строка 0 — область вывода результатов (основная часть окна)
        gbc.gridy = 0; gbc.weighty = WEIGHT_OUTPUT;
        root.add(titledScrollPanel("Вывод", output), gbc);

        // Строка 1 — поле ввода имени индивидуума для поиска свойств
        gbc.gridy = 1; gbc.weighty = WEIGHT_INPUT;
        root.add(titledScrollPanel("Имя индивидуума", input), gbc);

        // Строка 2 — панель кнопок управления
        gbc.gridy = 2; gbc.weighty = WEIGHT_BUTTONS;
        root.add(buildButtonPanel(frame, input, output), gbc);

        return root;
    }

    // Оборачивает компонент в панель с прокруткой и рамкой с заголовком.
    private static JPanel titledScrollPanel(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    // Возвращает преднастроенный GridBagConstraints:
    // растяжение по обеим осям, единственная колонка (gridx=0).
    private static GridBagConstraints defaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.insets  = PANEL_INSETS;
        gbc.gridx   = 0;
        gbc.weightx = 1.0;
        return gbc;
    }


// Панель кнопок


    // Создаёт панель со всеми кнопками управления онтологией.
    private static JPanel buildButtonPanel(JFrame frame,
                                           JTextArea input,
                                           JTextArea output) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));

        // «Вывести классы» — плоский список всех классов онтологии
        panel.add(button("Вывести классы", e -> {
            output.setText("");
            app.printAllClasses(model, output);
        }));

        // «Вывести дерево» — иерархия классов с индивидуумами
        panel.add(button("Вывести дерево", e -> {
            output.setText("");
            app.printFullClassHierarchy(model, output);
        }));

        // «Вывести индивидуумов» — список всех индивидуумов с их классами
        panel.add(button("Вывести индивидуумов", e -> {
            output.setText("");
            app.printAllIndividuals(model, output);
        }));

        // «Свойства» — выводит свойства индивидуума, имя которого введено в поле ввода
        panel.add(button("Свойства индивидуума", e -> {
            output.setText("");
            app.printIndividualProperties(model, output, input.getText().trim());
        }));

        // «Вставить класс» — открывает окно добавления подкласса
        panel.add(button("Вставить класс", e -> {
            new InsertPartClass(frame.getWidth(), frame.getHeight(), model)
                    .insert_start();
            frame.dispose(); // закрываем главное окно при переходе
        }));

        // «Вставить индивидуума» — открывает окно создания индивидуума
        panel.add(button("Вставить индивидуума", e ->
                new WindowCreatingIndividual(frame.getWidth(), frame.getHeight(), model)
                        .insert_start(frame)));

        // «Удалить объект» — диалог ввода имени и удаление класса или индивидуума
        panel.add(button("Удалить объект", e ->
                app.delete(model, output, frame)));

        // «Сохранить» — сохраняет онтологию с диалогом подтверждения
        panel.add(button("Сохранить", e -> {
            output.setText("");
            app.saveWithConfirmation(model, frame, output);
        }));

        return panel;
    }

    // Фабричный метод — создаёт JButton с заданным текстом и обработчиком.
    private static JButton button(String label, java.awt.event.ActionListener action) {
        JButton btn = new JButton(label);
        btn.addActionListener(action);
        return btn;
    }
}