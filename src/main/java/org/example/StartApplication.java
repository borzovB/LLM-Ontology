package org.example;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import javax.swing.SwingUtilities;

// Точка входа в приложение.
// Создаёт OWL-модель, загружает онтологию и запускает главное окно.
public class StartApplication {


// Константы


    // Начальный размер главного окна в пикселях
    private static final int WINDOW_WIDTH  = 800;
    private static final int WINDOW_HEIGHT = 800;


// Точка входа


    public static void main(String[] args) {
        // Создаём модель: OWL DL, хранение в памяти, без логического вывода
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        // Загружаем онтологию из файла в созданную модель
        LLMOntologyApp.loadOntology(model);

        // Запускаем UI в потоке Event Dispatch Thread — требование Swing
        SwingUtilities.invokeLater(() ->
                new MainWindow(model, WINDOW_WIDTH, WINDOW_HEIGHT).startProgramm()
        );
    }
}