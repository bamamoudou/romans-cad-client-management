package com.romanscad.clientmanagement.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryCustomerRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryGroupRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryMembershipRepository;
import com.romanscad.clientmanagement.core.service.CustomerService;
import com.romanscad.clientmanagement.core.service.DefaultClientManagementService;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Teste le moteur de bout en bout, contre le VRAI service en mémoire : ni serveur ni simulation. */
class CliApplicationTest {

    private final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
    private final DefaultClientManagementService service = new DefaultClientManagementService(
            new InMemoryCustomerRepository(), new InMemoryGroupRepository(), new InMemoryMembershipRepository());
    private final CliApplication app = newApp(new Services(service, service, service));

    private CliApplication newApp(Services services) {
        return new CliApplication("cli", "[--url <address>]", services,
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
    }

    private int run(String... arguments) {
        return app.run(List.of(arguments));
    }

    private String out() {
        return outBuffer.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return errBuffer.toString(StandardCharsets.UTF_8);
    }

    private void createCustomers(int count) {
        for (int i = 1; i <= count; i++) {
            assertEquals(0, run("customer", "create", "--first-name", "First" + i, "--last-name", "Last" + i,
                    "--email", "user" + i + "@example.com"));
        }
        outBuffer.reset();
    }

    // ---- clients

    @Test
    void createsThenShowsACustomer() {
        assertEquals(0, run("customer", "create", "--first-name", "Ada", "--last-name", "Lovelace",
                "--email", "ADA@Example.com"));
        assertTrue(out().contains("Customer created:"));
        assertTrue(out().contains("email      : ada@example.com")); // normalisé par le domaine

        outBuffer.reset();
        assertEquals(0, run("customer", "get", "1"));
        assertTrue(out().contains("first name : Ada"));
    }

    @Test
    void unknownCustomerGivesExitCode1AndAMessageOnStderr() {
        assertEquals(CliApplication.EXIT_REJECTED, run("customer", "get", "99"));
        assertEquals("Error: Customer 99 not found\n", err().replace("\r\n", "\n"));
        assertEquals("", out());
    }

    @Test
    void deleteThenDeleteAgain() {
        createCustomers(1);

        assertEquals(0, run("customer", "delete", "1"));
        assertTrue(out().contains("Customer 1 deleted."));
        assertEquals(CliApplication.EXIT_REJECTED, run("customer", "delete", "1"));
    }

    // ---- erreurs d'utilisation

    @Test
    void missingOptionIsAUsageError() {
        assertEquals(CliApplication.EXIT_USAGE, run("customer", "create", "--first-name", "Ada"));
        assertTrue(err().contains("Error: Missing option --last-name"));
        assertTrue(err().contains("Run 'cli help'"));
    }

    @Test
    void unknownCommandsAreUsageErrors() {
        assertEquals(CliApplication.EXIT_USAGE, run("customer", "explode"));
        assertTrue(err().contains("Unknown command 'customer explode'"));

        errBuffer.reset();
        assertEquals(CliApplication.EXIT_USAGE, run("banana"));
        assertTrue(err().contains("Unknown command 'banana'"));
    }

    @Test
    void noCommandIsAUsageError() {
        assertEquals(CliApplication.EXIT_USAGE, run());
        assertTrue(err().contains("No command given"));
    }

    @Test
    void badNumberAndExtraArgumentsAreUsageErrors() {
        assertEquals(CliApplication.EXIT_USAGE, run("customer", "get", "abc"));
        assertTrue(err().contains("<id> must be a whole number, got 'abc'"));

        errBuffer.reset();
        assertEquals(CliApplication.EXIT_USAGE, run("customer", "get", "1", "2"));
        assertTrue(err().contains("Unexpected argument '2'"));
    }

    // ---- aide

    @Test
    void helpListsEveryCommandAndTheExitCodes() {
        assertEquals(0, run("help"));

        for (String command : List.of("customer create", "customer get", "customer delete", "customer list",
                "customer groups", "group create", "group get", "group delete", "group list", "group add",
                "group remove", "group customers", "overview")) {
            assertTrue(out().contains("  " + command), "missing in help: " + command);
        }
        assertTrue(out().contains("Usage: cli [--url <address>] <command>"));
        assertTrue(out().contains("Exit codes:"));
    }

    @Test
    void dashDashHelpIsAnAliasOfHelp() {
        assertEquals(0, run("--help"));
        assertTrue(out().contains("Commands:"));
    }

    // ---- groupes et appartenances

    @Test
    void duplicateGroupNameIsRejected() {
        assertEquals(0, run("group", "create", "VIP", "--description", "Best customers"));
        assertEquals(CliApplication.EXIT_REJECTED, run("group", "create", "vip"));
        assertTrue(err().contains("already exists"));
    }

    @Test
    void addsAndRemovesCustomersAndListsBothDirections() {
        createCustomers(3);
        run("group", "create", "VIP");
        outBuffer.reset();

        assertEquals(0, run("group", "add", "1", "1", "2", "3"));
        assertTrue(out().contains("Added 3 of 3 requested customer(s) to group 1."));

        outBuffer.reset();
        assertEquals(0, run("group", "add", "1", "1", "2", "3"));
        assertTrue(out().contains("Added 0 of 3 requested customer(s) to group 1 (3 already member(s))."));

        outBuffer.reset();
        assertEquals(0, run("group", "customers", "1"));
        assertTrue(out().contains("user2@example.com"));
        assertTrue(out().contains("3 customers shown."));

        outBuffer.reset();
        assertEquals(0, run("customer", "groups", "2"));
        assertTrue(out().contains("VIP"));

        outBuffer.reset();
        assertEquals(0, run("group", "remove", "1", "2"));
        assertTrue(out().contains("Removed 1 of 1 requested customer(s) from group 1."));

        assertEquals(CliApplication.EXIT_REJECTED, run("group", "remove", "1", "3", "9"));
        assertTrue(err().contains("Customer 9 not found"));
    }

    @Test
    void addingAnUnknownCustomerChangesNothing() {
        createCustomers(1);
        run("group", "create", "VIP");

        assertEquals(CliApplication.EXIT_REJECTED, run("group", "add", "1", "1", "42"));

        outBuffer.reset();
        run("group", "customers", "1");
        assertTrue(out().contains("No customers."));
    }

    @Test
    void addNeedsAtLeastOneCustomer() {
        run("group", "create", "VIP");

        assertEquals(CliApplication.EXIT_USAGE, run("group", "add", "1"));
        assertTrue(err().contains("Missing argument <customerId>"));
    }

    @Test
    void overviewShowsEachCustomerWithItsGroups() {
        createCustomers(2);
        run("group", "create", "VIP");
        run("group", "create", "Staff");
        run("group", "add", "1", "1");
        run("group", "add", "2", "1");
        outBuffer.reset();

        assertEquals(0, run("overview"));

        String table = out();
        assertTrue(table.contains("GROUPS"));
        assertTrue(table.contains("First1 Last1  user1@example.com  VIP, Staff"));
        assertTrue(table.contains("First2 Last2  user2@example.com"));
    }

    // ---- pagination

    @Test
    void listsOnePageAndSaysHowToContinue() {
        createCustomers(5);

        assertEquals(0, run("customer", "list", "--limit", "2"));
        assertTrue(out().contains("user1@example.com"));
        assertTrue(out().contains("user2@example.com"));
        assertFalse(out().contains("user3@example.com"));
        assertTrue(out().contains("2 customers shown. More available: add --after 2"));

        outBuffer.reset();
        assertEquals(0, run("customer", "list", "--after", "4", "--limit", "2"));
        assertTrue(out().contains("user5@example.com"));
        assertTrue(out().contains("1 customers shown."));
        assertFalse(out().contains("More available"));
    }

    @Test
    void allWalksEveryPage() {
        createCustomers(5);

        assertEquals(0, run("customer", "list", "--all", "--limit", "2"));

        for (int i = 1; i <= 5; i++) {
            assertTrue(out().contains("user" + i + "@example.com"));
        }
        assertTrue(out().contains("5 customers listed."));
        assertEquals(1, out().split("FIRST NAME", -1).length - 1, "the header is printed once");
    }

    @Test
    void emptyListSaysSo() {
        assertEquals(0, run("group", "list"));
        assertTrue(out().contains("No groups."));
    }

    @Test
    void outOfRangeLimitIsRefusedWithTheDomainMessage() {
        assertEquals(CliApplication.EXIT_REJECTED, run("customer", "list", "--limit", "5000"));
        assertTrue(err().contains("limit must be between 1 and 1000"));
    }

    // ---- pannes

    /** Faux service dont chaque appel échoue : simule un serveur injoignable ou un bug. */
    private record FailingCustomerService(RuntimeException failure) implements CustomerService {
        @Override
        public Customer createCustomer(String firstName, String lastName, String email) {
            throw failure;
        }

        @Override
        public Customer getCustomer(long customerId) {
            throw failure;
        }

        @Override
        public void deleteCustomer(long customerId) {
            throw failure;
        }

        @Override
        public Page<Customer> listCustomers(PageRequest pageRequest) {
            throw failure;
        }
    }

    @Test
    void serverUnavailableHasItsOwnExitCode() {
        CliApplication broken = newApp(new Services(
                new FailingCustomerService(new ServiceUnavailableException("Cannot reach http://x:8080", null)),
                service, service));

        assertEquals(CliApplication.EXIT_UNAVAILABLE, broken.run(List.of("customer", "get", "1")));
        assertTrue(err().contains("Cannot reach http://x:8080"));
    }

    @Test
    void unexpectedErrorHasItsOwnExitCode() {
        CliApplication broken = newApp(new Services(
                new FailingCustomerService(new IllegalStateException("boom")), service, service));

        assertEquals(CliApplication.EXIT_UNEXPECTED, broken.run(List.of("customer", "get", "1")));
        assertTrue(err().contains("Unexpected error"));
    }
}