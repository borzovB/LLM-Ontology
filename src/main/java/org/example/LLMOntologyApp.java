package org.example;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Основной сервисный класс: загрузка, отображение, добавление,
// удаление и сохранение элементов OWL-онтологии через Apache Jena.
public class LLMOntologyApp {


// Константы


    // Пространство имён онтологии (используется для построения полных URI)
    private static final String NS =
            "http://www.semanticweb.org/user/ontologies/2026/3/untitled-ontology-11#";

    // Путь к файлу онтологии на диске
    private static final String ONTOLOGY_PATH = "ontology/Semantic_Web_Project.rdf";

    // Формат записи RDF при сохранении
    private static final String RDF_FORMAT = "RDF/XML-ABBREV";

    // Корневой класс онтологии, с которого начинается обход иерархии
    private static final String ROOT_CLASS = "БольшаяЯзыковаяМодель";


// Загрузка


    // Загружает онтологию из файла ONTOLOGY_PATH в переданную модель.
    // При ошибке выводит сообщение в stderr и стек вызовов.
    public static void loadOntology(OntModel model) {
        try {
            model.read(ONTOLOGY_PATH);
            System.out.println("Онтология загружена: " + ONTOLOGY_PATH);
        } catch (Exception e) {
            System.err.println("Файл не найден: " + ONTOLOGY_PATH);
            e.printStackTrace();
        }
    }


// Отображение классов


    // Выводит плоский список всех классов онтологии (без иерархии).
    public static void printAllClasses(OntModel model, JTextArea output) {
        log(output, "=== Все классы онтологии ===");
        ExtendedIterator<OntClass> iter = model.listClasses();
        try {
            while (iter.hasNext()) {
                OntClass cls = iter.next();
                if (cls.getLocalName() != null)
                    log(output, "- " + cls.getLocalName());
            }
        } finally {
            iter.close();
        }
    }

    // Выводит дерево классов начиная с корневого класса ROOT_CLASS.
    // Рекурсивно обходит подклассы и индивидуумов каждого класса.
    public static void printFullClassHierarchy(OntModel model, JTextArea output) {
        log(output, "\n=== Иерархия классов онтологии ===");
        log(output, "|- " + ROOT_CLASS);

        OntClass root = model.getOntClass(NS + ROOT_CLASS);
        if (root == null) return; // корневой класс не найден — выходим

        ExtendedIterator<? extends OntClass> iter = root.listSubClasses();
        try {
            while (iter.hasNext())
                printClassHierarchy(iter.next(), 2, output);
        } finally {
            iter.close();
        }
    }

    // Рекурсивно печатает класс, его индивидуумов и подклассы.
    // index — текущая глубина отступа (шаг 2 пробела).
    private static void printClassHierarchy(OntClass cls, int depth, JTextArea output) {
        String indent = "  ".repeat(depth);
        String name = cls.getLocalName() != null ? cls.getLocalName() : cls.getURI();
        log(output, indent + "|- " + name);

        // Выводим индивидуумов текущего класса
        printIndividualsOfClass(cls, indent, output);

        // Рекурсивный обход прямых подклассов
        ExtendedIterator<? extends OntClass> subIter = cls.listSubClasses();
        try {
            while (subIter.hasNext())
                printClassHierarchy(subIter.next(), depth + 1, output);
        } finally {
            subIter.close(); // закрываем итератор перед выходом
        }
    }

    // Выводит всех именованных индивидуумов переданного класса со звёздочкой.
    private static void printIndividualsOfClass(OntClass cls, String indent, JTextArea output) {
        ExtendedIterator<? extends OntResource> iter = cls.listInstances();
        try {
            while (iter.hasNext()) {
                OntResource res = iter.next();
                // Пропускаем анонимные ресурсы — нас интересуют только именованные индивидуумы
                if (!res.isIndividual()) continue;
                Individual ind = res.asIndividual();
                String indName = ind.getLocalName() != null ? ind.getLocalName() : ind.getURI();
                log(output, indent + "  * " + indName);
            }
        } finally {
            iter.close();
        }
    }


// Отображение индивидуумов


    // Выводит список всех индивидуумов с указанием их класса в квадратных скобках.
    public static void printAllIndividuals(OntModel model, JTextArea output) {
        log(output, "\n=== Все индивидуумы онтологии ===");
        ExtendedIterator<Individual> iter = model.listIndividuals();
        try {
            while (iter.hasNext()) {
                Individual ind = iter.next();
                String name      = localNameOrUri(ind);
                String className = resolveIndividualClass(ind);
                log(output, "- " + name + " [" + className + "]");
            }
        } finally {
            iter.close();
        }
    }

    // Определяет имя класса индивидуума, пропуская служебный тип owl:NamedIndividual.
    // Возвращает "unknown", если подходящий класс не найден.
    private static String resolveIndividualClass(Individual ind) {
        ExtendedIterator<? extends OntClass> iter = ind.listOntClasses(true);
        try {
            while (iter.hasNext()) {
                try {
                    OntClass cls = iter.next();
                    // Пропускаем мета-тип, который Jena добавляет автоматически
                    if (cls.getURI().contains("owl#NamedIndividual")) continue;
                    return cls.getLocalName() != null ? cls.getLocalName() : cls.getURI();
                } catch (ConversionException ignored) {
                    // Не все ресурсы можно привести к OntClass — пропускаем
                }
            }
        } finally {
            iter.close();
        }
        return "unknown";
    }

    // Выводит все свойства индивидуума, найденного по локальному имени.
    // Свойство "type" (rdf:type) скрывается — оно служебное.
    public static void printIndividualProperties(OntModel model, JTextArea output, String name) {
        if (name == null || name.isEmpty()) {
            log(output, "Введите имя индивидуума для поиска!");
            return;
        }

        Individual target = findIndividualByName(model, name);
        if (target == null) {
            log(output, "В онтологии нет индивидуума: " + name);
            return;
        }

        log(output, "Свойства индивидуума \"" + name + "\":");
        ExtendedIterator<? extends Statement> iter = target.listProperties();
        try {
            while (iter.hasNext()) {
                Statement stmt = iter.next();
                String propName = localNameOrUri(stmt.getPredicate());
                if (propName.equals("type")) continue; // rdf:type не показываем пользователю
                log(output, "  • " + propName + ": " + rdfNodeToString(stmt.getObject()));
            }
        } finally {
            iter.close();
        }
    }

    // Ищет индивидуума по локальному имени перебором всех индивидуумов модели.
    // Возвращает null, если не найден.
    private static Individual findIndividualByName(OntModel model, String name) {
        ExtendedIterator<Individual> iter = model.listIndividuals();
        try {
            while (iter.hasNext()) {
                Individual ind = iter.next();
                if (name.equals(ind.getLocalName())) return ind;
            }
        } finally {
            iter.close();
        }
        return null;
    }


// Добавление класса


    // Создаёт новый подкласс с именем newClassName внутри parentClass.
    // Перед созданием проверяет: класс не существует, родитель существует.
    public static void createSubClassSafely(OntModel model,
                                            String parentClass,
                                            String newClassName,
                                            JTextArea output) {
        String newUri    = NS + newClassName;
        String parentUri = NS + parentClass;

        // Проверка 1: новый класс не должен уже существовать
        if (model.getOntClass(newUri) != null) {
            log(output, "Отмена: класс '" + newClassName + "' уже существует!");
            return;
        }

        OntClass parent = model.getOntClass(parentUri);
        // Проверка 2: родительский класс должен существовать в онтологии
        if (parent == null) {
            log(output, "Ошибка: родительский класс '" + parentClass + "' не найден.");
            return;
        }

        // Создаём класс и устанавливаем иерархическую связь
        OntClass newClass = model.createClass(newUri);
        parent.addSubClass(newClass);
        log(output, "Класс '" + newClassName + "' добавлен в '" + parent.getLocalName() + "'");
    }


// Удаление объектов


    // Открывает диалог ввода имени и удаляет индивидуума или класс.
    // Сначала ищет индивидуума; если не найден — пытается удалить класс.
    public static void delete(OntModel model, JTextArea output, JFrame frame) {
        output.setText("");

        String name = promptForName(frame, "Введите имя объекта для удаления");
        if (name == null) {
            log(output, "Удаление отменено.");
            return;
        }

        // Сначала пытаемся найти и удалить индивидуума
        Individual ind = findIndividualByName(model, name);
        if (ind != null) {
            ind.remove(); // remove() удаляет все тройки RDF с участием этого ресурса
            log(output, "Удалён индивидуум: " + name);
        } else {
            // Индивидуум не найден — пробуем удалить класс
            deleteClass(model, output, name);
        }
    }

    // Удаляет класс из онтологии:
    //   1. Удаляет всех индивидуумов класса.
    //   2. Отвязывает подклассы (убирает superClass-связь).
    //   3. Удаляет сам класс.
    public static void deleteClass(OntModel model, JTextArea output, String className) {
        OntClass cls = model.getOntClass(NS + className);
        if (cls == null) {
            log(output, "Объект не найден: " + className);
            return;
        }

        // Шаг 1: Собираем и удаляем все экземпляры класса
        // (итератор закрываем до удаления, чтобы избежать ConcurrentModificationException)
        List<Individual> instances = collectInstances(cls);
        instances.forEach(Individual::remove);

        // Шаг 2: Отвязываем подклассы от удаляемого класса
        List<OntClass> subClasses = collectSubClasses(cls);
        subClasses.forEach(sub -> sub.removeSuperClass(cls));

        // Шаг 3: Удаляем сам класс
        cls.remove();
        log(output, "Удалён класс: " + className);
    }

    // Собирает всех именованных индивидуумов класса в список (итератор закрывается внутри).
    private static List<Individual> collectInstances(OntClass cls) {
        List<Individual> result = new ArrayList<>();
        ExtendedIterator<? extends OntResource> iter = cls.listInstances();
        try {
            while (iter.hasNext()) {
                OntResource res = iter.next();
                if (res.isIndividual()) result.add(res.asIndividual());
            }
        } finally {
            iter.close();
        }
        return result;
    }

    // Собирает прямые подклассы в список (итератор закрывается внутри).
    private static List<OntClass> collectSubClasses(OntClass cls) {
        List<OntClass> result = new ArrayList<>();
        ExtendedIterator<? extends OntClass> iter = cls.listSubClasses();
        try {
            while (iter.hasNext()) result.add(iter.next());
        } finally {
            iter.close();
        }
        return result;
    }


// Сохранение


    // Сохраняет онтологию и закрывает окно (используется при выходе из приложения).
    public static void saveAndClose(OntModel model, JFrame frame) {
        if (writeOntology(model, frame)) frame.dispose();
    }

    // Предлагает пользователю подтвердить сохранение через диалог Да/Нет.
    // НЕ закрывает окно — только записывает файл.
    public static void saveWithConfirmation(OntModel model, JFrame frame, JTextArea log) {
        if (!checkModelLoaded(model, frame)) return;

        int choice = JOptionPane.showConfirmDialog(frame,
                "Вы хотите сохранить текущую онтологию?",
                "Подтверждение сохранения",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean ok = writeOntology(model, frame);
            log(log, ok ? "Онтология сохранена: " + ONTOLOGY_PATH : "Ошибка сохранения.");
        } else {
            log(log, "Сохранение отменено.");
        }
    }

    // Предлагает подтвердить сохранение без дополнительного лога в текстовой области.
    public static void confirmAndSaveOntology(OntModel model, JFrame frame) {
        saveWithConfirmation(model, frame, null);
    }

    // Записывает RDF-файл на диск. Возвращает true при успехе, false при ошибке.
    private static boolean writeOntology(OntModel model, JFrame frame) {
        try (FileOutputStream out = new FileOutputStream(ONTOLOGY_PATH)) {
            model.write(out, RDF_FORMAT);
            JOptionPane.showMessageDialog(frame, "Сохранено!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame,
                    "Ошибка при сохранении:\n" + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }


// Свойства (чекбоксы для UI создания индивидуума)


    // Загружает все объектные и примитивные свойства онтологии,
    // создаёт для каждого чекбокс и добавляет в контейнер.
    public void loadProperties(JPanel container, OntModel model, Map<String, PropertyData> checkboxes) {
        container.removeAll();
        checkboxes.clear();

        // Объектные свойства (связывают индивидуума с другим ресурсом)
        loadPropertyGroup(model.listObjectProperties(), container, checkboxes, true);

        // Примитивные свойства (связывают индивидуума с литеральным значением)
        loadPropertyGroup(model.listDatatypeProperties(), container, checkboxes, false);

        // Если свойств не найдено — показываем предупреждение
        if (checkboxes.isEmpty()) {
            JLabel label = new JLabel("В онтологии не найдено свойств");
            label.setForeground(Color.RED);
            label.setFont(new Font("SansSerif", Font.ITALIC, 12));
            container.add(label);
        }

        container.revalidate();
        container.repaint();
    }

    // Перебирает свойства из итератора и регистрирует чекбокс для каждого.
    // Свойства с префиксом "Jena" являются служебными — пропускаем.
    private <T extends OntProperty> void loadPropertyGroup(
            ExtendedIterator<T> iter,
            JPanel container,
            Map<String, PropertyData> checkboxes,
            boolean isObject) {
        try {
            while (iter.hasNext()) {
                OntProperty prop = iter.next();
                String name = prop.getLocalName();
                if (name != null && !name.startsWith("Jena"))
                    addPropertyCheckbox(container, prop, isObject, checkboxes);
            }
        } finally {
            iter.close();
        }
    }

    // Создаёт чекбокс для свойства и регистрирует его в карте checkboxes.
    private void addPropertyCheckbox(JPanel container, OntProperty prop,
                                     boolean isObject, Map<String, PropertyData> checkboxes) {
        String name  = prop.getLocalName() != null ? prop.getLocalName() : prop.getURI();
        String label = name + (isObject ? " [ссылка]" : " [значение]");
        JCheckBox cb = new JCheckBox(label);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        checkboxes.put(name, new PropertyData(cb, prop, isObject));
        container.add(cb);
    }


// Вспомогательные утилиты


    // Возвращает локальное имя ресурса, или полный URI если локального нет.
    private static String localNameOrUri(Resource r) {
        return r.getLocalName() != null ? r.getLocalName() : r.getURI();
    }

    // Преобразует RDF-узел в читаемую строку:
    // литерал → лексическая форма; ресурс → локальное имя или URI.
    private static String rdfNodeToString(RDFNode node) {
        if (node.isLiteral())  return node.asLiteral().getLexicalForm();
        if (node.isResource()) return localNameOrUri(node.asResource());
        return node.toString();
    }

    // Выводит сообщение одновременно в System.out и в текстовую область (если не null).
    private static void log(JTextArea area, String msg) {
        System.out.println(msg);
        if (area != null) area.append(msg + "\n");
    }

    // Проверяет, загружена ли модель. При null показывает диалог и возвращает false.
    private static boolean checkModelLoaded(OntModel model, JFrame frame) {
        if (model != null) return true;
        JOptionPane.showMessageDialog(frame, "Модель не загружена!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // Открывает диалог ввода строки. Возвращает введённый текст или null при отмене/пустом вводе.
    private static String promptForName(JFrame frame, String title) {
        JTextField field = new JTextField(20);
        int res = JOptionPane.showConfirmDialog(frame, field, title, JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return null;
        String name = field.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Необходимо ввести текст!",
                    "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return name;
    }
}