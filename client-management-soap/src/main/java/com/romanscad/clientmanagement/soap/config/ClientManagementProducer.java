package com.romanscad.clientmanagement.soap.config;

import com.romanscad.clientmanagement.core.repository.memory.InMemoryCustomerRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryGroupRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryMembershipRepository;
import com.romanscad.clientmanagement.core.service.CustomerService;
import com.romanscad.clientmanagement.core.service.DefaultClientManagementService;
import com.romanscad.clientmanagement.core.service.GroupService;
import com.romanscad.clientmanagement.core.service.MembershipService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Racine de composition (composition root) : le SEUL endroit qui sait que la base est en
 * mémoire. C'est ici qu'on assemble dépôts et service, puis qu'on les offre à CDI.
 *
 * Le module core ne contient aucune annotation CDI : c'est cette classe qui le branche sur
 * Jakarta EE. Pour passer à une vraie base de données, on ne changerait que init().
 *
 * Un seul objet DefaultClientManagementService est créé pour toute l'application : il porte
 * le verrou qui protège les règles métier, il doit donc être unique. Les trois producteurs
 * exposent ce même objet sous chacune des trois interfaces de service.
 */
@ApplicationScoped
public class ClientManagementProducer {

    private DefaultClientManagementService service;

    /**
     * Appelé par le conteneur sur le vrai objet, pas sur son proxy. Pour cela on n'initialise pas
     * le champ dans le constructeur : les beans à portée applicative sont enveloppés d'un proxy
     * que CDI construit aussi, et on ne veut pas créer une seconde base vide pour rien.
     */
    @PostConstruct
    void init() {
        service = new DefaultClientManagementService(
                new InMemoryCustomerRepository(),
                new InMemoryGroupRepository(),
                new InMemoryMembershipRepository());
    }

    @Produces
    @ApplicationScoped
    public CustomerService customerService() {
        return service;
    }

    @Produces
    @ApplicationScoped
    public GroupService groupService() {
        return service;
    }

    @Produces
    @ApplicationScoped
    public MembershipService membershipService() {
        return service;
    }
}