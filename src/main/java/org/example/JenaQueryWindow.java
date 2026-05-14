package org.example;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class JenaQueryWindow extends JFrame {

    private final Model model;
    private final JTextArea queryArea;
    private final JTextArea resultArea;

    private static final String DEFAULT_QUERY =
            "PREFIX : <http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n\n" +
            "SELECT ?modelName ?developerName ?year\n" +
            "WHERE {\n" +
            "  ?model :названиеМодели ?modelName ;\n" +
            "         :годВыпуска ?year ;\n" +
            "         :разработанаКем ?developer .\n" +
            "  ?developer :названиеОрганизации ?developerName .\n" +
            "}\n" +
            "ORDER BY ?year ?modelName";

    public JenaQueryWindow() {
        super("SPARQL-запросы через Jena");

        this.model = loadOntologyModel();

        queryArea = new JTextArea(DEFAULT_QUERY, 16, 90);
        resultArea = new JTextArea(18, 90);

        queryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        resultArea.setEditable(false);

        JButton runButton = new JButton("Выполнить запрос");
        runButton.addActionListener(e -> runQuery());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("SPARQL-запрос:"), BorderLayout.NORTH);
        topPanel.add(new JScrollPane(queryArea), BorderLayout.CENTER);
        topPanel.add(runButton, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JLabel("Результат:"), BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private Model loadOntologyModel() {
        try {
            Path ontologyPath = findOntologyFile();

            Model loadedModel = ModelFactory.createDefaultModel();

            try (InputStream inputStream = Files.newInputStream(ontologyPath)) {
                RDFDataMgr.read(loadedModel, inputStream, Lang.RDFXML);
            }

            return loadedModel;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить онтологию: " + e.getMessage(), e);
        }
    }

    private Path findOntologyFile() throws FileNotFoundException {
        List<Path> candidates = Arrays.asList(
                Paths.get("ontology", "Semantic_Web_Project.rdf"),
                Paths.get("Semantic_Web_Project.rdf"),
                Paths.get("src", "main", "resources", "Semantic_Web_Project.rdf")
        );

        for (Path path : candidates) {
            if (Files.exists(path)) {
                return path;
            }
        }

        throw new FileNotFoundException(
                "Файл Semantic_Web_Project.rdf не найден. " +
                "Положи его в папку ontology или в корень проекта."
        );
    }

    private void runQuery() {
        try {
            String queryText = queryArea.getText();
            String result = executeQuery(queryText);
            resultArea.setText(result);
        } catch (Exception e) {
            resultArea.setText("Ошибка выполнения запроса:\n" + e.getMessage());
        }
    }

    private String executeQuery(String queryText) throws IOException {
        Query query = QueryFactory.create(queryText);

        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {

            if (query.isSelectType()) {
                ResultSet results = queryExecution.execSelect();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ResultSetFormatter.out(outputStream, results, query);

                return outputStream.toString(StandardCharsets.UTF_8.name());
            }

            if (query.isAskType()) {
                boolean result = queryExecution.execAsk();
                return "ASK result: " + result;
            }

            if (query.isConstructType()) {
                Model resultModel = queryExecution.execConstruct();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                resultModel.write(outputStream, "TURTLE");

                return outputStream.toString(StandardCharsets.UTF_8.name());
            }

            if (query.isDescribeType()) {
                Model resultModel = queryExecution.execDescribe();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                resultModel.write(outputStream, "TURTLE");

                return outputStream.toString(StandardCharsets.UTF_8.name());
            }

            return "Неподдерживаемый тип запроса.";
        }
    }
}