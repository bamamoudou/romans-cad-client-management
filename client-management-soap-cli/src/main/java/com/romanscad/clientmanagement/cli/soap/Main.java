package com.romanscad.clientmanagement.cli.soap;

import com.romanscad.clientmanagement.cli.CliApplication;
import com.romanscad.clientmanagement.cli.Services;
import com.romanscad.clientmanagement.cli.args.LeadingOption;
import com.romanscad.clientmanagement.cli.args.UsageException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Point d'entrée du client SOAP en ligne de commande.
 *
 * Usage : {@code java -jar client-management-soap-cli.jar [--url <adresse>] <commande> ...}
 */
public final class Main {

    static final String PROGRAM = "client-management-soap-cli";

    /** Référence forte : sans elle, le réglage du niveau de journalisation peut être perdu par le ramasse-miettes. */
    private static final Logger CXF_LOGGER = Logger.getLogger("org.apache.cxf");

    private Main() {
        // Classe de démarrage : non instanciable
    }

    public static void main(String[] args) {
        System.exit(run(List.of(args), System.out, System.err));
    }

    /** Séparée de main() pour pouvoir être testée : renvoie le code de sortie au lieu de quitter. */
    static int run(List<String> args, PrintStream out, PrintStream err) {
        // CXF journalise beaucoup à son niveau par défaut ; un outil en ligne de commande doit rester silencieux.
        CXF_LOGGER.setLevel(Level.SEVERE);
        try {
            LeadingOption url = LeadingOption.extract(args, "url");
            Services services = SoapServices.create(url.value().orElse(SoapServices.DEFAULT_BASE_URL));
            CliApplication application = new CliApplication(PROGRAM, "[--url <address>]", services, out, err);
            return application.run(url.remaining());
        } catch (UsageException e) {
            err.println("Error: " + e.getMessage());
            return CliApplication.EXIT_USAGE;
        }
    }
}