package com.romanscad.clientmanagement.cli;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.romanscad.clientmanagement.cli.args.Arguments;
import com.romanscad.clientmanagement.cli.args.UsageException;
import com.romanscad.clientmanagement.cli.command.CommandSpec;
import com.romanscad.clientmanagement.cli.command.CustomerCommands;
import com.romanscad.clientmanagement.cli.command.GroupCommands;
import com.romanscad.clientmanagement.cli.command.MembershipCommands;
import com.romanscad.clientmanagement.cli.output.Console;
import com.romanscad.clientmanagement.core.exception.ClientManagementException;

/**
 * Le moteur de la ligne de commande, commun aux clients SOAP et REST : il trouve la commande,
 * analyse ses arguments, l'exécute, et transforme toute erreur en message et en code de sortie.
 *
 * Il ne dépend que des interfaces de service : il ignore si elles sont servies par SOAP, par
 * REST ou par la mémoire locale (c'est ce qui permet de le tester sans serveur).
 */
public final class CliApplication {

    public static final int EXIT_OK = 0;
    /** Le serveur a refusé la demande : introuvable, doublon, données invalides... */
    public static final int EXIT_REJECTED = 1;
    /** La ligne de commande est incorrecte. */
    public static final int EXIT_USAGE = 2;
    /** Le serveur est injoignable ou n'a pas répondu à temps. */
    public static final int EXIT_UNAVAILABLE = 3;
    /** Erreur inattendue : un bug. */
    public static final int EXIT_UNEXPECTED = 4;

    private final String programName;
    private final String globalOptionsHelp;
    private final PrintStream err;
    private final Console console;
    private final Map<String, CommandSpec> commands = new LinkedHashMap<>();

    /**
     * @param programName       nom affiché dans l'aide
     * @param globalOptionsHelp options propres au programme, déjà traitées avant ce moteur
     *                          (par exemple "[--url <address>]"), ou chaîne vide
     */
    public CliApplication(String programName, String globalOptionsHelp, Services services,
                          PrintStream out, PrintStream err) {
        this.programName = programName;
        this.globalOptionsHelp = globalOptionsHelp;
        this.err = err;
        this.console = new Console(out);

        List<CommandSpec> specs = new ArrayList<>();
        specs.addAll(new CustomerCommands(services.customers()).specs());
        specs.addAll(new GroupCommands(services.groups()).specs());
        specs.addAll(new MembershipCommands(services.memberships()).specs());
        specs.add(CommandSpec.of("help", "help", "Show this help", (arguments, output) -> printHelp()));
        for (CommandSpec spec : specs) {
            if (commands.put(spec.name(), spec) != null) {
                throw new IllegalStateException("Duplicate command: " + spec.name());
            }
        }
    }

    /** Exécute une ligne de commande. @return le code de sortie du programme */
    public int run(List<String> arguments) {
        try {
            execute(arguments);
            return EXIT_OK;
        } catch (UsageException e) {
            err.println("Error: " + e.getMessage());
            err.println("Run '" + programName + " help' to list the commands.");
            return EXIT_USAGE;
        } catch (ClientManagementException e) {
            err.println("Error: " + e.getMessage());
            return EXIT_REJECTED;
        } catch (ServiceUnavailableException e) {
            err.println("Error: " + e.getMessage());
            return EXIT_UNAVAILABLE;
        } catch (RuntimeException e) {
            err.println("Unexpected error: " + e);
            return EXIT_UNEXPECTED;
        }
    }

    private void execute(List<String> tokens) {
        if (tokens.isEmpty()) {
            throw new UsageException("No command given");
        }
        String first = tokens.get(0);
        if (first.equals("--help") || first.equals("-h")) {
            printHelp();
            return;
        }

        CommandSpec spec;
        List<String> rest;
        if (tokens.size() >= 2 && commands.containsKey(first + " " + tokens.get(1))) {
            spec = commands.get(first + " " + tokens.get(1));
            rest = tokens.subList(2, tokens.size());
        } else if (commands.containsKey(first)) {
            spec = commands.get(first);
            rest = tokens.subList(1, tokens.size());
        } else {
            boolean knownFamily = commands.keySet().stream().anyMatch(name -> name.startsWith(first + " "));
            throw new UsageException("Unknown command '"
                    + (knownFamily && tokens.size() >= 2 ? first + " " + tokens.get(1) : first) + "'");
        }

        Arguments parsed = Arguments.parse(rest, spec.valueOptions(), spec.flagOptions());
        spec.handler().run(parsed, console);
    }

    /** L'aide est construite à partir des descriptions des commandes : elle ne peut pas être obsolète. */
    private void printHelp() {
        console.println("Usage: " + programName + (globalOptionsHelp.isEmpty() ? "" : " " + globalOptionsHelp)
                + " <command> [arguments]");
        console.println("");
        console.println("Commands:");
        for (CommandSpec spec : commands.values()) {
            console.println("  " + spec.usage());
            console.println("      " + spec.description());
        }
        console.println("");
        console.println("Lists are paginated: use --after <id> to continue after a given id, or --all to list everything.");
        console.println("");
        console.println("Exit codes: " + EXIT_OK + " success, " + EXIT_REJECTED + " request refused by the server, "
                + EXIT_USAGE + " usage error, " + EXIT_UNAVAILABLE + " server unavailable, "
                + EXIT_UNEXPECTED + " unexpected error.");
    }
}