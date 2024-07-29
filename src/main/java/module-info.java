 module com.endeleya.kazisafex {
    requires javafx.controls;
    requires java.prefs;
    requires java.logging;
    requires jakarta.json;
    requires tyrus.standalone.client;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.lang3;
    requires okhttp3;
    requires retrofit2;
    requires java.xml;
    requires escpos.coffee;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires kernel;
    requires layout;
    requires sign;
    requires styled.xml.parser;
    requires hyph;
    requires pdfa;
    requires controlsfx;
    requires pdftest;
    requires barcodes;
    requires swing.toast.notifications;
    requires org.eclipse.persistence.core;
    requires org.hibernate.commons.annotations;
    requires org.hibernate.orm.core;
    requires java.compiler;
    requires jakarta.persistence;
    requires jakarta.json.bind;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    
    requires org.apache.commons.io;
    requires hibernate.entitymanager;
    requires eclipselink.antlr;
    requires retrofit2.converter.jackson;
  
    opens com.endeleya.kazisafex to javafx.fxml,javafx.graphics;
    exports com.endeleya.kazisafex;
    exports tools;
    requires jakarta.xml.bind;
}
