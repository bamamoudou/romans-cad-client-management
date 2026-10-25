package com.romanscad.clientmanagement.core.seed;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.service.CustomerService;
import com.romanscad.clientmanagement.core.service.GroupService;
import com.romanscad.clientmanagement.core.service.MembershipService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Génère un jeu de données de test, de façon déterministe : la même configuration produit
 * toujours exactement les mêmes clients, groupes et associations (tests reproductibles).
 *
 * Le générateur passe par les INTERFACES de service, comme n'importe quel client : il ne
 * contourne ni les validations ni les verrous, et il ignore tout du stockage en mémoire.
 *
 * Forme des données :
 * - clients : prénom et nom tirés au hasard (graine fixe), e-mail unique grâce à l'identifiant
 * - groupes : noms fixes, du plus grand au plus petit
 * - appartenances : le groupe n° k contient environ 40 % / k des clients (loi de Zipf).
 *   On obtient un groupe énorme (~400 000 clients), puis des groupes de plus en plus petits,
 *   ce qui teste la pagination sur de gros ensembles. Un client appartient en moyenne à ~1,4 groupe.
 */
public final class SampleDataSeeder {

    /** Nombre maximal de groupes générables (un nom par groupe). */
    public static final int MAX_GROUPS = 20;

    /** Taille des lots d'association : limite la mémoire utilisée et la durée de chaque verrou d'écriture. */
    private static final int BATCH_SIZE = 10_000;

    /** Part des clients appartenant au plus grand groupe. */
    private static final double LARGEST_GROUP_RATIO = 0.40;

    private static final String[] FIRST_NAMES = {
        "Mamoudou", "Maimouna", "Chloe", "David", "Emma", "Felix", "Grace", "Hugo", "Ines", "Jules",
        "Kevin", "Lea", "Marc", "Nina", "Oscar", "Paula", "Quentin", "Rose", "Samuel", "Tina"
    };

    private static final String[] LAST_NAMES = {
        "Ba", "Sall", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand", "Leroy", "Moreau",
        "Simon", "Laurent", "Lefebvre", "Michel", "Garcia", "David", "Bertrand", "Roux", "Vincent", "Fournier"
    };

    private static final String[] GROUP_NAMES = {
        "Newsletter", "Prospect", "Premium", "Partenaire", "Beta testeur", "VIP", "Employe", "Presse",
        "fournisseur", "Alumni", "Etudiant", "Early adopters", "Referent", "Inactive", "media",
        "Enterprise", "publique", "Trial", "Association", "Revendeur"
    };

    private final CustomerService customerService;
    private final GroupService groupService;
    private final MembershipService membershipService;

    public SampleDataSeeder(CustomerService customerService,
                            GroupService groupService,
                            MembershipService membershipService) {
        this.customerService = customerService;
        this.groupService = groupService;
        this.membershipService = membershipService;
    }

    public SeedReport seed(SeedConfiguration configuration) {
        long start = System.nanoTime();

        long[] customerIds = createCustomers(configuration);
        List<Group> groups = createGroups(configuration.groupCount());
        long memberships = createMemberships(configuration, customerIds, groups);

        return new SeedReport(customerIds.length, groups.size(), memberships,
                Duration.ofNanos(System.nanoTime() - start));
    }

    /** @return les identifiants attribués, dans l'ordre de création (8 Mo pour 1 million de clients) */
    private long[] createCustomers(SeedConfiguration configuration) {
        SplittableRandom random = new SplittableRandom(configuration.randomSeed());
        long[] ids = new long[configuration.customerCount()];
        for (int i = 0; i < ids.length; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            // L'indice garantit l'unicité des e-mails malgré le nombre limité de noms.
            String email = firstName + "." + lastName + "." + (i + 1) + "@example.com";
            Customer customer = customerService.createCustomer(firstName, lastName, email);
            ids[i] = customer.id();
        }
        return ids;
    }

    private List<Group> createGroups(int groupCount) {
        List<Group> groups = new ArrayList<>(groupCount);
        for (int i = 0; i < groupCount; i++) {
            groups.add(groupService.createGroup(GROUP_NAMES[i], "Sample group #" + (i + 1)));
        }
        return groups;
    }

    private long createMemberships(SeedConfiguration configuration, long[] customerIds, List<Group> groups) {
        long total = 0;
        for (int index = 0; index < groups.size(); index++) {
            long groupId = groups.get(index).id();
            double ratio = LARGEST_GROUP_RATIO / (index + 1);
            // Un générateur par groupe : le contenu d'un groupe ne dépend pas des autres.
            SplittableRandom random = new SplittableRandom(configuration.randomSeed() + index + 1);

            List<Long> batch = new ArrayList<>(BATCH_SIZE);
            for (long customerId : customerIds) {
                if (random.nextDouble() < ratio) {
                    batch.add(customerId);
                    if (batch.size() == BATCH_SIZE) {
                        total += membershipService.addCustomersToGroup(groupId, batch);
                        batch.clear(); // le service a déjà copié les identifiants
                    }
                }
            }
            if (!batch.isEmpty()) {
                total += membershipService.addCustomersToGroup(groupId, batch);
            }
        }
        return total;
    }
}