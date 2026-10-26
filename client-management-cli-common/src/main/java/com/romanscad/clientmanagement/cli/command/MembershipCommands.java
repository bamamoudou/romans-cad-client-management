package com.romanscad.clientmanagement.cli.command;

import com.romanscad.clientmanagement.cli.args.Arguments;
import com.romanscad.clientmanagement.cli.args.UsageException;
import com.romanscad.clientmanagement.cli.output.Console;
import com.romanscad.clientmanagement.cli.output.TableFormatter;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.service.MembershipService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/** Commandes liées aux appartenances client/groupe, et la vue « clients et leurs groupes ». */
public final class MembershipCommands {

    private final MembershipService memberships;

    public MembershipCommands(MembershipService memberships) {
        this.memberships = memberships;
    }

    public List<CommandSpec> specs() {
        return List.of(
                CommandSpec.of("group add", "group add <groupId> <customerId>...",
                        "Add customers to a group (all or nothing)", this::add),
                CommandSpec.of("group remove", "group remove <groupId> <customerId>...",
                        "Remove customers from a group (all or nothing)", this::remove),
                new CommandSpec("group customers", "group customers <groupId> " + PagedListing.USAGE,
                        "List the customers of a group",
                        PagedListing.VALUE_OPTIONS, PagedListing.FLAG_OPTIONS, this::customersOfGroup),
                new CommandSpec("customer groups", "customer groups <customerId> " + PagedListing.USAGE,
                        "List the groups of a customer",
                        PagedListing.VALUE_OPTIONS, PagedListing.FLAG_OPTIONS, this::groupsOfCustomer),
                new CommandSpec("overview", "overview " + PagedListing.USAGE,
                        "List customers together with the groups they belong to",
                        PagedListing.VALUE_OPTIONS, PagedListing.FLAG_OPTIONS, this::overview));
    }

    private void add(Arguments arguments, Console console) {
        long groupId = arguments.positionalLong(0, "groupId");
        List<Long> customerIds = requireCustomerIds(arguments);
        int requested = new LinkedHashSet<>(customerIds).size(); // sans doublons : c'est ce que traite le serveur
        int added = memberships.addCustomersToGroup(groupId, customerIds);
        console.println("Added " + added + " of " + requested + " requested customer(s) to group " + groupId
                + (added < requested ? " (" + (requested - added) + " already member(s))" : "") + ".");
    }

    private void remove(Arguments arguments, Console console) {
        long groupId = arguments.positionalLong(0, "groupId");
        List<Long> customerIds = requireCustomerIds(arguments);
        int requested = new LinkedHashSet<>(customerIds).size();
        int removed = memberships.removeCustomersFromGroup(groupId, customerIds);
        console.println("Removed " + removed + " of " + requested + " requested customer(s) from group " + groupId
                + (removed < requested ? " (" + (requested - removed) + " were not members)" : "") + ".");
    }

    private void customersOfGroup(Arguments arguments, Console console) {
        long groupId = arguments.positionalLong(0, "groupId");
        arguments.requireNoMorePositionalsThan(1);
        PagedListing.run(arguments, console, Views.customerTable(), "customers",
                request -> memberships.listCustomersOfGroup(groupId, request), Views::customerRow);
    }

    private void groupsOfCustomer(Arguments arguments, Console console) {
        long customerId = arguments.positionalLong(0, "customerId");
        arguments.requireNoMorePositionalsThan(1);
        PagedListing.run(arguments, console, Views.groupTable(), "groups",
                request -> memberships.listGroupsOfCustomer(customerId, request), Views::groupRow);
    }

    private void overview(Arguments arguments, Console console) {
        arguments.requireNoMorePositionalsThan(0);
        PagedListing.run(arguments, console, new TableFormatter("ID", "NAME", "EMAIL", "GROUPS"), "customers",
                memberships::listCustomersWithGroups, MembershipCommands::overviewRow);
    }

    private static String[] overviewRow(CustomerWithGroups entry) {
        String groupNames = entry.groups().stream().map(Group::name).collect(Collectors.joining(", "));
        return new String[] {
            String.valueOf(entry.customer().id()),
            entry.customer().firstName() + " " + entry.customer().lastName(),
            entry.customer().email(),
            groupNames};
    }

    /** Les identifiants de clients : tout ce qui suit le numéro de groupe, au moins un. */
    private static List<Long> requireCustomerIds(Arguments arguments) {
        List<Long> customerIds = arguments.positionalLongsFrom(1, "customerId");
        if (customerIds.isEmpty()) {
            throw new UsageException("Missing argument <customerId>");
        }
        return customerIds;
    }
}