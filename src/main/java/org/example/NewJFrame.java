package org.example;

import org.apache.jena.ontology.OntModel;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Утилитный класс: перехватывает закрытие JFrame и предлагает
// сохранить онтологию через диалог Да / Нет / Отмена.
public class NewJFrame {


// Константы


    private static final String DIALOG_MESSAGE =
            "Вы хотите сохранить онтологию перед выходом?";
    private static final String DIALOG_TITLE = "Подтверждение выхода";


// API


    // Подключает обработчик события закрытия окна (кнопка × или Alt+F4).
    // Поведение по выбору пользователя:
    //   Да - сохранить онтологию и закрыть окно
    //   Нет - закрыть окно без сохранения
    //   Отмена - ничего не делать, окно остаётся открытым
    public static void setupCloseHandler(JFrame frame, OntModel model) {
        // Отключаем автоматическое закрытие, чтобы перехватить событие вручную
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleClose(frame, model);
            }
        });
    }


// Внутренняя логика


    // Показывает диалог подтверждения и выполняет соответствующее действие.
    private static void handleClose(JFrame frame, OntModel model) {
        int choice = JOptionPane.showConfirmDialog(
                frame,
                DIALOG_MESSAGE,
                DIALOG_TITLE,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        switch (choice) {
            case JOptionPane.YES_OPTION ->
                // Сохраняем и закрываем (saveAndClose вызывает frame.dispose() внутри)
                    LLMOntologyApp.saveAndClose(model, frame);

            case JOptionPane.NO_OPTION ->
                // Закрываем без сохранения
                    frame.dispose();

            // CANCEL_OPTION и CLOSED_OPTION (крестик диалога) — окно остаётся открытым
            default -> { }
        }
    }
}