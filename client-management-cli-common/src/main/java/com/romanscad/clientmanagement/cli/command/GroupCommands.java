package com.romanscad.clientmanagement.cli.command;

import com.romanscad.clientmanagement.cli.args.Arguments;
import com.romanscad.clientmanagement.cli.output.Console;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.service.GroupService;
import java.util.List;
import java.util.Set;

/** Commandes liées aux groupes (hors appartenances, voir MembershipCommands). */
public final class GroupCommands {

    private final GroupService groups;

    public GroupCommands(GroupService groups) {
        this.groups = groups;
    }

    public List<CommandSpec> specs() {
        return List.of(
                new CommandSpec("group create", "group create <name> [--description <text>]",
                        "Create a group (names are unique, case-insensitive)",
                        Set.of("description"), Set.of(), this::create),
                CommandSpec.of("group get", "group get <id>", "Show a group", this::get),
                CommandSpec.of("group delete", "group delete <id>",
                        "Delete a group (its customers are kept)", this::delete),
                new CommandSpec("group list", "group list " + PagedListing.USAGE,
                        "List groups, one page at a time",
                        PagedListing.VALUE_OPTIONS, PagedListing.FLAG_OPTIONS, this::list));
    }

    private void create(Arguments arguments, Console console) {
        String name = arguments.positional(0, "name");
        arguments.requireNoMorePositionalsThan(1);
        Group group = groups.createGroup(name, arguments.option("description").orElse(null));
        console.println("Group created:");
        console.printFields(Views.groupFields(group));
    }

    private void get(Arguments arguments, Console console) {
        long id = arguments.positionalLong(0, "id");
        arguments.requireNoMorePositionalsThan(1);
        console.printFields(Views.groupFields(groups.getGroup(id)));
    }

    private void delete(Arguments arguments, Console console) {
        long id = arguments.positionalLong(0, "id");
        arguments.requireNoMorePositionalsThan(1);
        groups.deleteGroup(id);
        console.println("Group " + id + " deleted.");
    }

    private void list(Arguments arguments, Console console) {
        arguments.requireNoMorePositionalsThan(0);
        PagedListing.run(arguments, console, Views.groupTable(), "groups",
                groups::listGroups, Views::groupRow);
    }
}