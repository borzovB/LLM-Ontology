package org.example;

import org.apache.jena.ontology.OntModel;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;

public class NewJFrame {

    public static void setupCloseHandler(JFrame frame, OntModel model) {
        // Отключаем стандартное закрытие, чтобы перехватить событие
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Добавляем слушатель закрытия окна
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Показываем диалог с тремя вариантами: Да / Нет / Отмена
                int choice = JOptionPane.showConfirmDialog(
                        frame,
                        "Вы хотите сохранить онтологию перед выходом?",
                        "Подтверждение выхода",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    LLMOntologyApp llmOntologyApp = new LLMOntologyApp();
                    llmOntologyApp.saveFile(model, frame);
                }
                else if (choice == JOptionPane.NO_OPTION) {
                    // ПОЛЬЗОВАТЕЛЬ НАЖАЛ "НЕТ" → Закрываем без сохранения
                    frame.dispose();
                }
                // ️ Если нажата "Отмена" или крестик в диалоге → ничего не делаем, окно остаётся открытым
            }
        });
    }

}
