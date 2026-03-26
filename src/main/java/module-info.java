module com.endeleya.kazisafex {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.web;

    requires java.desktop;
    requires java.prefs;
    requires java.logging;
    requires java.sql;
    requires java.xml;
    requires java.compiler;

    requires jakarta.json;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires jakarta.ws.rs;
    requires jakarta.websocket.client;
    requires jakarta.xml.bind;

    requires org.apache.poi.ooxml;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;

    requires retrofit2;
    requires retrofit2.converter.jackson;
    requires okhttp3;
    requires okio;
    requires okhttp.eventsource;
    requires launchdarkly.logging;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires org.hibernate.orm.core;
    requires org.eclipse.persistence.core;
    requires eclipselink.antlr;

    requires com.fazecast.jSerialComm;
    requires escpos.coffee;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    requires kernel;
    requires layout;
    requires sign;
    requires styled.xml.parser;
    requires hyph;
    requires pdfa;
    requires barcodes;
    requires controlsfx;
    requires pdftest;

    requires swing.toast.notifications;

    opens com.endeleya.kazisafex to javafx.fxml, javafx.graphics;
    opens data to org.hibernate.orm.core, com.fasterxml.jackson.databind, javafx.base;
    opens data.finance to com.fasterxml.jackson.databind;
    opens data.helpers to com.fasterxml.jackson.databind;

    exports com.endeleya.kazisafex;
    exports tools;

    requires org.slf4j;
}
