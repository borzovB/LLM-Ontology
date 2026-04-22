package org.example;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

public class LLMOntologyApp {

    private static String ontology_Path = "ontology/Semantic_Web_Project.rdf";

    public static void main(String[] args) {
        // Создаём модель с параметрами: OWL DL, хранение в памяти, без логического вывода
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        load_obt(model);

        System.out.println("Модель создана успешно");
    }

    private static void load_obt(OntModel model){
        model.read(ontology_Path);
        System.out.println("Онтология загружена из файла");
    }
}
