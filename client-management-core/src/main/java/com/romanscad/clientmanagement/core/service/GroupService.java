package com.romanscad.clientmanagement.core.service;

import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.exception.DuplicateGroupNameException;
import com.romanscad.clientmanagement.core.exception.GroupNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;

/** Cas d'usage liés aux groupes. */
public interface GroupService {

    /**
     * Crée un groupe.
     *
     * @throws ValidationException         si un champ est invalide
     * @throws DuplicateGroupNameException si le nom est déjà pris
     */
    Group createGroup(String name, String description);

    /**
     * @throws GroupNotFoundException si le groupe n'existe pas
     */
    Group getGroup(long groupId);

    /**
     * Supprime un groupe et toutes ses associations. Les clients ne sont pas supprimés.
     *
     * @throws GroupNotFoundException si le groupe n'existe pas
     */
    void deleteGroup(long groupId);

    /** Liste paginée de tous les groupes. */
    Page<Group> listGroups(PageRequest pageRequest);
}