package org.example;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

public class StartApplication {

    public static void main(String[] args) {

        LLMOntologyApp llmOntologyApp = new LLMOntologyApp();

        // Создаём модель с параметрами: OWL DL, хранение в памяти, без логического вывода
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        llmOntologyApp.load_obt(model);

        MainWindow mainWindow = new MainWindow(model, 800, 800);

        mainWindow.startProgramm();

        System.out.println("Модель создана успешно");
    }
}