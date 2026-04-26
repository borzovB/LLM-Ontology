package org.example;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.ontology.OntProperty;
import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class LLMOntologyApp {

    private static String ontology_Path = "ontology/Semantic_Web_Project.rdf";

    public static void load_obt(OntModel model){
        try {
            model.read(ontology_Path);
            System.out.println("Онтология загружена из файла");
        }catch (Exception e){
            System.err.println("Файл не найден: " + ontology_Path);
            e.printStackTrace();
        }
    }

    public static void print_name_class(OntModel model, JTextArea jTextArea_print){
        System.out.println("Вывод всех классов: ");
        jTextArea_print.append("Вывод всех классов: " + "\n");
        ExtendedIterator<OntClass> ontClassExtendedIterator = model.listClasses();
        while (ontClassExtendedIterator.hasNext()){
            OntClass ontClass = ontClassExtendedIterator.next();
            if (ontClass.getLocalName()!=null){
                System.out.println("- " + ontClass.getLocalName());
                jTextArea_print.append("- " + ontClass.getLocalName() + "\n");
            }

        }

        ontClassExtendedIterator.close();
    }

    public static void printFullClassHierarchy(OntModel model, JTextArea jTextArea_print){
        System.out.println("\n=== Иерархия классов онтологии ===\n");
        jTextArea_print.append("\n=== Иерархия классов онтологии ===\n");
        // Получаем корневой класс owl:Thing
        String NS = "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";
        OntClass ontClass = model.getOntClass(NS + "БольшаяЯзыковаяМодель");
        System.out.println("|- БольшаяЯзыковаяМодель");
        jTextArea_print.append("|- БольшаяЯзыковаяМодель" + "\n");
        if(ontClass != null){
            ExtendedIterator <? extends OntClass> extendedIterator = ontClass.listSubClasses();
            try {
                while (extendedIterator.hasNext()){
                    OntClass rootClass = extendedIterator.next();
                    printClassHierarchy(rootClass, 2, jTextArea_print);
                }
            }finally {
                extendedIterator.close();
            }
        }

    }

    private static void printClassHierarchy(OntClass rootClass, int index, JTextArea jTextArea_print){
        // Формируем отступы для визуализации иерархии
        String indent = "  ".repeat(index);
        // Выводим имя класса (локальное, без URI)
        String className = rootClass.getLocalName() != null ? rootClass.getLocalName() : rootClass.getURI();
        System.out.println(indent + "|- " + className);
        jTextArea_print.append(indent + "|- " + className + "\n");

        ExtendedIterator<? extends OntResource> instIter = rootClass.listInstances();

        try {
           while (instIter.hasNext()){
               OntResource resource = instIter.next();
               // Проверяем, что это именно индивидуум (а не анонимный ресурс)
               if (resource.isIndividual()) {
                   Individual individual = resource.asIndividual();
                   String name = individual.getLocalName() != null
                           ? individual.getLocalName()
                           : individual.getURI();
                   System.out.println(indent + "  * " + name);
                   jTextArea_print.append(indent + "  * " + name + "\n");
               }
           }
        }finally {
            instIter.close();
        }

        // Рекурсивно обходим прямые подклассы
        ExtendedIterator<? extends OntClass> subIter = rootClass.listSubClasses();
        try {
            while (subIter.hasNext()) {
                OntClass subClass = subIter.next();
                printClassHierarchy(subClass, index + 1, jTextArea_print);
            }
        } finally {
            subIter.close(); // Обязательно закрываем итератор!
        }

    }

    public static void print_Ind(OntModel model, JTextArea jTextArea_print){
        System.out.println("\n=== Все индивидуумы онтологии ===\n");
        jTextArea_print.append("\n=== Все индивидуумы онтологии ===\n");

        // Получаем итератор по всем индивидуумам в модели
        ExtendedIterator<Individual> iter = model.listIndividuals();

        try {

            while (iter.hasNext()){
                Individual individual = iter.next();
                String name = individual.getLocalName()!=null ? individual.getLocalName() : individual.getURI();
                String type_class = "unknown";

                ExtendedIterator<? extends OntClass> classIter = individual.listOntClasses(true);
                try {
                    while (classIter.hasNext()) {
                        try {
                            OntClass ontClass = classIter.next();
                            // Пропускаем служебные типы
                            if (ontClass.getURI().contains("owl#NamedIndividual")) continue;

                            type_class = ontClass.getLocalName() != null
                                    ? ontClass.getLocalName()
                                    : ontClass.getURI();
                            break; // берём первый подходящий класс
                        } catch (ConversionException e) {
                            // Пропускаем неконвертируемые типы
                            continue;
                        }
                    }
                } finally {
                    classIter.close();
                }

                System.out.println("- " + name + " [" + type_class + "]");
                jTextArea_print.append("- " + name + " [" + type_class + "]" + "\n");
            }

        }finally {
            iter.close();
        }

    }

    public static void print_Ind_Prop(OntModel model, JTextArea jTextArea_print, String text){

        if(text == null || text.isEmpty()){
            jTextArea_print.append("Введите текст для запроса!" + "\n");
        }else {

            // Получаем итератор по всем индивидуумам в модели
            ExtendedIterator<Individual> iter = model.listIndividuals();

            boolean dr = false;

            Individual individual = null;
            try {

                while (iter.hasNext()){
                    individual = iter.next();
                    String name = individual.getLocalName()!=null ? individual.getLocalName() : individual.getURI();

                    if (!name.equals(text)){
                        dr =true;
                    }else {
                        dr = false;
                        break;
                    }

                }

            }finally {
                iter.close();
            }

            if(dr){
                jTextArea_print.append("В антологии нет такого индивидуума!" + "\n");
            }else {
                jTextArea_print.append("Вывод свойств индивидуума" + "\n");
                ExtendedIterator<? extends Statement> propIter = individual.listProperties();
                try {
                    while (propIter.hasNext()){
                        Statement statement = propIter.next();
                        Property prop = statement.getPredicate();
                        RDFNode object = statement.getObject();

                        // Имя свойства
                        String propName = prop.getLocalName() != null
                                ? prop.getLocalName()
                                : prop.getURI();

                        // Значение в зависимости от типа
                        String value;
                        if (object.isLiteral()) {
                            value = object.asLiteral().getLexicalForm();
                        } else if (object.isResource()) {
                            Resource res = object.asResource();
                            value = res.getLocalName() != null
                                    ? res.getLocalName()
                                    : res.getURI();
                        } else {
                            value = object.toString();
                        }

                        if (!propName.equals("type")){
                            jTextArea_print.append("  • " + propName + ": " + value + "\n");
                        }
                    }
                }finally {
                    propIter.close();
                }
            }

        }

    }

    // Добавление элемента (класса)

    /*
     * Создает новый подкласс, предварительно проверяя его наличие в онтологии.
     *
     * model Модель онтологии
     * parentClass Родительский класс, к которому будет привязан новый класс
     * newClassName Имя нового класса (только имя, без URI)
     */
    public static void createSubClassSafely(OntModel model, String parentClass, String newClassName,
                                            JTextArea jTextAreaPrint) {

        // Пространство имен вашей онтологии
        String NS = "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";
        String fullUri = NS + newClassName;
        String parentUri = NS + parentClass;

        OntClass parent = model.getOntClass(parentUri);

        // ПРОВЕРКА: Ищем класс по полному URI
        // Если getOntClass возвращает не null, значит класс уже есть
        if (model.getOntClass(fullUri) != null) {
            System.out.println("Отмена: Класс '" + newClassName + "' уже существует в онтологии!");
            jTextAreaPrint.append("Отмена: Класс '" + newClassName + "' уже существует в онтологии!" + "\n");
            return;
        }

        // Дополнительная проверка: существует ли родительский класс
        if (parent == null) {
            jTextAreaPrint.append("Ошибка: Родительский класс не найден." + "\n");
            System.out.println("Ошибка: Родительский класс не найден.");
            return;
        }

        // СОЗДАНИЕ: Если проверки прошли успешно — создаем класс
        System.out.println("Создание нового класса: " + newClassName);
        jTextAreaPrint.append("Создание нового класса: " + newClassName + "\n");
        OntClass newClass = model.createClass(fullUri);

        // ИЕРАРХИЯ: Указываем, что это подкласс родительского
        parent.addSubClass(newClass);

        System.out.println("Класс '" + newClassName + "' успешно добавлен в '" + parent.getLocalName() + "'");
        jTextAreaPrint.append("Класс '" + newClassName + "' успешно добавлен в '" + parent.getLocalName() + "'" + "\n");
    }

    // Удаление элемента (класса)

    public static void delete(OntModel model, JTextArea output, JFrame frame){
        String NS = "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";

        output.setText("");

        JTextField textField = new JTextField(20);
        int result = JOptionPane.showConfirmDialog(
                frame,
                textField,
                "Введите текст",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = textField.getText().trim();

            // Проверка на пустой ввод
            if (name == null || name.isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Необходимо ввести текст!",
                        "Ошибка ввода",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                // Текст введен корректно - продолжаем работу
                System.out.println("Введен текст: " + name);
                Individual individual = model.getIndividual(NS + name);

                if (individual != null) {
                    // Пробуем поиск по локальному имени
                    ExtendedIterator<Individual> iter = model.listIndividuals();
                    try {
                        while (iter.hasNext()) {
                            Individual ind = iter.next();
                            String name_in = ind.getLocalName();
                            if (name_in != null && name_in.equals(name)) {
                                individual = ind;
                                break;
                            }
                        }
                    } finally {
                        iter.close();
                    }

                    if(individual != null){
                        // Удаляем индивидуума (метод remove() удаляет все тройки, где он участвует)
                        individual.remove();
                        output.append(" Удалён индивидуум: " + name + "\n");
                    }else {
                        deleteClass(model, output, name);
                    }
                } else {
                    deleteClass(model, output, name);
                }
            }
        } else {
            // Пользователь нажал Cancel или закрыл окно
            output.append("Ввод отменен" + "\n");
        }

    }

    /*
     * Удаляет класс из онтологии с очисткой всех связанных утверждений
     */
    public static void deleteClass(OntModel model, JTextArea output, String className) {
        String NS = "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";

        OntClass cls = model.getOntClass(NS + className);

        if (cls == null) {
            output.append(" Объект не найден: " + className + "\n");
            return;
        }

        // Собираем индивидуумов этого класса в список
        java.util.List<org.apache.jena.ontology.Individual> instancesToDelete = new java.util.ArrayList<>();
        ExtendedIterator<? extends org.apache.jena.ontology.OntResource> instIter = cls.listInstances();
        try {
            while (instIter.hasNext()) {
                org.apache.jena.ontology.OntResource res = instIter.next();
                if (res.isIndividual()) {
                    instancesToDelete.add(res.asIndividual());
                }
            }
        } finally {
            instIter.close();
        }

        // Удаляем собранные индивидуумы (итератор уже закрыт)
        for (org.apache.jena.ontology.Individual ind : instancesToDelete) {
            ind.remove();
        }

        // Собираем подклассы для отвязки
        java.util.List<OntClass> subClasses = new java.util.ArrayList<>();
        ExtendedIterator<? extends OntClass> subIter = cls.listSubClasses();
        try {
            while (subIter.hasNext()) {
                subClasses.add(subIter.next());
            }
        } finally {
            subIter.close(); // Закрываем перед модификацией
        }

        // Отвязываем подклассы от удаляемого класса
        for (OntClass sub : subClasses) {
            sub.removeSuperClass(cls);
        }

        // Удаляем сам класс
        cls.remove();

        output.append(" Удалён класс: " + className + "\n");
    }

    public static void saveFile(OntModel model, JFrame frame){
        // ПОЛЬЗОВАТЕЛЬ НАЖАЛ "ДА" → Сохраняем и закрываем
        try (FileOutputStream out = new FileOutputStream(ontology_Path)) {
            model.write(out, "RDF/XML-ABBREV"); // или "TURTLE", "OWL/XML"
            JOptionPane.showMessageDialog(frame,
                    "Онтология успешно сохранена!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose(); // Корректное закрытие окна и освобождение ресурсов

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame,
                    " Ошибка при сохранении:\n" + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * Сохраняет онтологию с диалогом подтверждения.
     * НЕ закрывает окно и НЕ завершает приложение.
     */
    public static void saveWithConfirmation(OntModel model, JFrame parentFrame, JTextArea logArea) {
        if (model == null) {
            JOptionPane.showMessageDialog(parentFrame, "Модель не загружена!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Показываем диалог Да/Нет
        int choice = JOptionPane.showConfirmDialog(
                parentFrame,
                "Вы хотите сохранить текущую онтологию?",
                "Подтверждение сохранения",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // Если нажали "Да" - выполняем сохранение
        if (choice == JOptionPane.YES_OPTION) {
            try (FileOutputStream out = new FileOutputStream(ontology_Path)) {

                model.write(out, "RDF/XML-ABBREV");

                String msg = " Онтология успешно сохранена:\n" + ontology_Path;
                if (logArea != null) logArea.append(msg + "\n");
                JOptionPane.showMessageDialog(parentFrame, "Сохранено!", "Успех", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                String msg = " Ошибка сохранения: " + e.getMessage();
                if (logArea != null) logArea.append(msg + "\n");
                JOptionPane.showMessageDialog(parentFrame, msg, "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        // Если нажали "Нет" или закрыли диалог → просто выходим
        else {
            if (logArea != null) logArea.append(" Сохранение отменено.\n");
        }
    }

    /*
     * Показывает окно подтверждения сохранения онтологии.
     * Если пользователь нажимает "Да" — онтология сохраняется.
     * Если "Нет" или закрытие окна — сохранение отменяется.
     */
    public static void confirmAndSaveOntology(OntModel model, JFrame parentFrame) {

        if (model == null) {
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Модель онтологии не загружена!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                parentFrame,
                "Вы хотите сохранить текущую онтологию?",
                "Сохранение онтологии",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try (FileOutputStream out = new FileOutputStream(ontology_Path)) {

                model.write(out, "RDF/XML-ABBREV");

                JOptionPane.showMessageDialog(
                        parentFrame,
                        "Онтология успешно сохранена!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        parentFrame,
                        "Ошибка при сохранении:\n" + e.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /*
     * Загружает все свойства онтологии и создаёт для них чекбоксы
     */
    public void loadProperties(JPanel container, OntModel model, Map<String, PropertyData> propertyCheckboxes) {
        container.removeAll();
        propertyCheckboxes.clear();

        // Объектные свойства
        ExtendedIterator<? extends ObjectProperty> objIter = model.listObjectProperties();
        try {
            while (objIter.hasNext()) {
                ObjectProperty prop = objIter.next();
                String name = prop.getLocalName();
                if (name != null && !name.startsWith("Jena")) {
                    addPropertyCheckbox(container, prop, true, propertyCheckboxes);
                }
            }
        } finally {
            objIter.close();
        }

        // Примитивные свойства
        ExtendedIterator<? extends DatatypeProperty> dataIter = model.listDatatypeProperties();
        try {
            while (dataIter.hasNext()) {
                DatatypeProperty prop = dataIter.next();
                String name = prop.getLocalName();
                if (name != null && !name.startsWith("Jena")) {
                    addPropertyCheckbox(container, prop, false, propertyCheckboxes);
                }
            }
        } finally {
            dataIter.close();
        }

        if (propertyCheckboxes.isEmpty()) {
            JLabel noProps = new JLabel(" В онтологии не найдено свойств");
            noProps.setForeground(Color.RED);
            noProps.setFont(new Font("SansSerif", Font.ITALIC, 12));
            container.add(noProps);
        }

        container.revalidate();
        container.repaint();
    }

    // Измените тип параметра и локальной переменной
    private void addPropertyCheckbox(JPanel container, OntProperty prop, boolean isObjectProperty,
                                     Map<String, PropertyData> propertyCheckboxes) {
        String name = prop.getLocalName() != null ? prop.getLocalName() : prop.getURI();
        String type = isObjectProperty ? "[ссылка]" : "[значение]";

        JCheckBox checkBox = new JCheckBox(name + " " + type);
        checkBox.setFont(new Font("SansSerif", Font.PLAIN, 12));

        propertyCheckboxes.put(name, new PropertyData(checkBox, prop, isObjectProperty));
        container.add(checkBox);
    }


}