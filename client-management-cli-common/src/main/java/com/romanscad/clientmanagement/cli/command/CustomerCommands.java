package com.romanscad.clientmanagement.cli.command;

import com.romanscad.clientmanagement.cli.args.Arguments;
import com.romanscad.clientmanagement.cli.output.Console;
import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.service.CustomerService;
import java.util.List;
import java.util.Set;

/** Commandes liées aux clients. */
public final class CustomerCommands {

    private final CustomerService customers;

    public CustomerCommands(CustomerService customers) {
        this.customers = customers;
    }

    public List<CommandSpec> specs() {
        return List.of(
                new CommandSpec("customer create",
                        "customer create --first-name <text> --last-name <text> --email <text>",
                        "Create a customer",
                        Set.of("first-name", "last-name", "email"), Set.of(), this::create),
                CommandSpec.of("customer get", "customer get <id>", "Show a customer", this::get),
                CommandSpec.of("customer delete", "customer delete <id>",
                        "Delete a customer and remove it from all its groups", this::delete),
                new CommandSpec("customer list", "customer list " + PagedListing.USAGE,
                        "List customers, one page at a time",
                        PagedListing.VALUE_OPTIONS, PagedListing.FLAG_OPTIONS, this::list));
    }

    private void create(Arguments arguments, Console console) {
        arguments.requireNoMorePositionalsThan(0);
        Customer customer = customers.createCustomer(
                arguments.requiredOption("first-name"),
                arguments.requiredOption("last-name"),
                arguments.requiredOption("email"));
        console.println("Customer created:");
        console.printFields(Views.customerFields(customer));
    }

    private void get(Arguments arguments, Console console) {
        long id = arguments.positionalLong(0, "id");
        arguments.requireNoMorePositionalsThan(1);
        console.printFields(Views.customerFields(customers.getCustomer(id)));
    }

    private void delete(Arguments arguments, Console console) {
        long id = arguments.positionalLong(0, "id");
        arguments.requireNoMorePositionalsThan(1);
        customers.deleteCustomer(id);
        console.println("Customer " + id + " deleted.");
    }

    private void list(Arguments arguments, Console console) {
        arguments.requireNoMorePositionalsThan(0);
        PagedListing.run(arguments, console, Views.customerTable(), "customers",
                customers::listCustomers, Views::customerRow);
    }
}