package com.romanscad.clientmanagement.cli.rest;

import com.romanscad.clientmanagement.cli.CliApplication;
import com.romanscad.clientmanagement.cli.Services;
import com.romanscad.clientmanagement.cli.args.LeadingOption;
import com.romanscad.clientmanagement.cli.args.UsageException;
import java.io.PrintStream;
import java.util.List;

/**
 * Point d'entrée du client REST en ligne de commande.
 *
 * Usage : {@code java -jar client-management-rest-cli.jar [--url <adresse>] <commande> ...}
 */
public final class Main {

    static final String PROGRAM = "client-management-rest-cli";

    private Main() {
        // Classe de démarrage : non instanciable
    }

    public static void main(String[] args) {
        System.exit(run(List.of(args), System.out, System.err));
    }

    /** Séparée de main() pour pouvoir être testée : renvoie le code de sortie au lieu de quitter. */
    static int run(List<String> args, PrintStream out, PrintStream err) {
        try {
            LeadingOption url = LeadingOption.extract(args, "url");
            Services services = RestServices.create(url.value().orElse(RestServices.DEFAULT_BASE_URL));
            CliApplication application = new CliApplication(PROGRAM, "[--url <address>]", services, out, err);
            return application.run(url.remaining());
        } catch (UsageException | IllegalArgumentException e) {
            // Option --url sans valeur, ou adresse invalide : c'est une erreur d'utilisation.
            err.println("Error: " + e.getMessage());
            return CliApplication.EXIT_USAGE;
        }
    }
}