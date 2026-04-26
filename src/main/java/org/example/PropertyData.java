package org.example;

import org.apache.jena.ontology.OntProperty;
import javax.swing.JCheckBox;

// Контейнер данных для одного свойства онтологии в UI выбора свойств.
// Связывает чекбокс на экране с соответствующим OntProperty из модели.
public class PropertyData {


// Поля


    // Чекбокс, отображаемый в списке свойств при создании индивидуума
    public final JCheckBox checkBox;

    // Свойство онтологии (объектное или примитивное)
    public final OntProperty property;

    // true — объектное свойство (ссылка на ресурс),
    // false — примитивное свойство (литеральное значение)
    public final boolean isObjectProperty;


// Конструктор


    // Создаёт запись о свойстве с привязанным чекбоксом.
    public PropertyData(JCheckBox checkBox, OntProperty property, boolean isObjectProperty) {
        this.checkBox        = checkBox;
        this.property        = property;
        this.isObjectProperty = isObjectProperty;
    }
}