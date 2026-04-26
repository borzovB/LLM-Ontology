package org.example;

import org.apache.jena.ontology.OntProperty;

import javax.swing.*;

public class PropertyData {
    JCheckBox checkBox;
    public OntProperty property;
    boolean isObjectProperty;

    PropertyData(JCheckBox cb, OntProperty p, boolean isObj) {
        this.checkBox = cb;
        this.property = p;
        this.isObjectProperty = isObj;
    }
}
