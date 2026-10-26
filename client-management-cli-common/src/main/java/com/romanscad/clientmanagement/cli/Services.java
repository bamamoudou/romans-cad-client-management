package com.romanscad.clientmanagement.cli;

import com.romanscad.clientmanagement.core.service.CustomerService;
import com.romanscad.clientmanagement.core.service.GroupService;
import com.romanscad.clientmanagement.core.service.MembershipService;
import java.util.Objects;

/**
 * Les trois services dont le moteur a besoin. Une implémentation par transport (SOAP, REST) ;
 * le moteur ne sait pas laquelle il utilise.
 */
public record Services(CustomerService customers, GroupService groups, MembershipService memberships) {

    public Services {
        Objects.requireNonNull(customers, "customers must not be null");
        Objects.requireNonNull(groups, "groups must not be null");
        Objects.requireNonNull(memberships, "memberships must not be null");
    }
}