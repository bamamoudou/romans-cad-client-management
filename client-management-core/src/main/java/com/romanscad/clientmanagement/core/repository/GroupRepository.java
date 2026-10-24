package com.romanscad.clientmanagement.core.repository;

import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.Optional;

/** Stockage des groupes (port). Mêmes principes que CustomerRepository. */
public interface GroupRepository {

    /** Crée un groupe. L'identifiant est attribué par le dépôt. */
    Group create(String name, String description);

    Optional<Group> findById(long id);

    /** Recherche par nom (insensible à la casse), pour garantir l'unicité des noms. */
    Optional<Group> findByName(String name);

    /** @return true si le groupe existait et a été supprimé */
    boolean deleteById(long id);

    /** Groupes triés par identifiant croissant. */
    Page<Group> findAll(PageRequest pageRequest);

    long count();
}