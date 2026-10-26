package com.romanscad.clientmanagement.cli.command;

import com.romanscad.clientmanagement.cli.output.TableFormatter;
import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.Group;
import java.util.LinkedHashMap;
import java.util.Map;

/** Présentation des clients et des groupes (en ligne de tableau ou en fiche détaillée). */
final class Views {

    private Views() {
        // Classe utilitaire : non instanciable
    }

    static TableFormatter customerTable() {
        return new TableFormatter("ID", "FIRST NAME", "LAST NAME", "EMAIL");
    }

    static String[] customerRow(Customer customer) {
        return new String[] {String.valueOf(customer.id()), customer.firstName(), customer.lastName(), customer.email()};
    }

    static Map<String, String> customerFields(Customer customer) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("id", String.valueOf(customer.id()));
        fields.put("first name", customer.firstName());
        fields.put("last name", customer.lastName());
        fields.put("email", customer.email());
        return fields;
    }

    static TableFormatter groupTable() {
        return new TableFormatter("ID", "NAME", "DESCRIPTION");
    }

    static String[] groupRow(Group group) {
        return new String[] {String.valueOf(group.id()), group.name(), group.description()};
    }

    static Map<String, String> groupFields(Group group) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("id", String.valueOf(group.id()));
        fields.put("name", group.name());
        fields.put("description", group.description());
        return fields;
    }
}