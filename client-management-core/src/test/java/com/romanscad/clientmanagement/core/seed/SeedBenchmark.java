package com.romanscad.clientmanagement.core.seed;

import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryCustomerRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryGroupRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryMembershipRepository;
import com.romanscad.clientmanagement.core.service.DefaultClientManagementService;
import java.util.function.Supplier;

/**
 * Mesure manuelle (à lancer avec "Run as Java Application", ce n'est pas un test automatique) :
 * temps de génération, mémoire utilisée et durée des opérations sur 1 million de clients.
 * Argument facultatif : nombre de clients.
 */
public final class SeedBenchmark {

    private SeedBenchmark() {
    }

    public static void main(String[] args) {
        int customerCount = args.length > 0 ? Integer.parseInt(args[0]) : SeedConfiguration.DEFAULT_CUSTOMER_COUNT;
        DefaultClientManagementService service = new DefaultClientManagementService(
                new InMemoryCustomerRepository(), new InMemoryGroupRepository(), new InMemoryMembershipRepository());

        long heapBefore = usedHeapMegabytes();
        SeedReport report = new SampleDataSeeder(service, service, service)
                .seed(new SeedConfiguration(customerCount, SeedConfiguration.DEFAULT_GROUP_COUNT,
                        SeedConfiguration.DEFAULT_RANDOM_SEED));
        long heapAfter = usedHeapMegabytes();

        System.out.println("Génération : " + report);
        System.out.println("Mémoire utilisée par les données : " + (heapAfter - heapBefore) + " Mo");
        System.out.println();
        System.out.println("Temps moyen par opération (en microsecondes) :");
        measure("getCustomer (id au milieu)", () -> service.getCustomer(customerCount / 2));
        measure("listCustomers, 1re page (100)", () -> service.listCustomers(PageRequest.firstPage()));
        measure("listCustomers, page profonde", () -> service.listCustomers(PageRequest.after(customerCount - 200)));
        measure("listCustomersOfGroup(plus gros groupe), 1re page", () -> service.listCustomersOfGroup(1, PageRequest.firstPage()));
        measure("listCustomersOfGroup(plus gros groupe), page profonde", () -> service.listCustomersOfGroup(1, PageRequest.after(customerCount / 2)));
        measure("listGroupsOfCustomer", () -> service.listGroupsOfCustomer(customerCount / 2, PageRequest.firstPage()));
        measure("listCustomersWithGroups, page de 100", () -> service.listCustomersWithGroups(PageRequest.after(customerCount / 2)));
        measure("listCustomersWithGroups, page de 1000", () -> service.listCustomersWithGroups(new PageRequest(customerCount / 2, 1000)));

        System.out.println();
        System.out.println("Taille des 5 plus gros groupes :");
        for (long groupId = 1; groupId <= 5; groupId++) {
            System.out.println("  groupe " + groupId + " : " + countMembers(service, groupId));
        }
    }

    private static long countMembers(DefaultClientManagementService service, long groupId) {
        long count = 0;
        Page<?> page = service.listCustomersOfGroup(groupId, new PageRequest(0, PageRequest.MAX_LIMIT));
        count += page.items().size();
        while (page.hasNext()) {
            page = service.listCustomersOfGroup(groupId, new PageRequest(page.nextCursor(), PageRequest.MAX_LIMIT));
            count += page.items().size();
        }
        return count;
    }

    private static void measure(String label, Supplier<?> operation) {
        for (int i = 0; i < 2_000; i++) { // échauffement : laisse la JVM optimiser le code
            operation.get();
        }
        int iterations = 5_000;
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            operation.get();
        }
        double micros = (System.nanoTime() - start) / 1_000.0 / iterations;
        System.out.printf("  %-58s %9.1f%n", label, micros);
    }

    private static long usedHeapMegabytes() {
        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i < 3; i++) {
            System.gc(); // plusieurs passes pour une mesure plus stable
        }
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
}