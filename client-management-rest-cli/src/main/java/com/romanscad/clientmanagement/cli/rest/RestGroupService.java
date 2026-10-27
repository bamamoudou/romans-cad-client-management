package com.romanscad.clientmanagement.cli.rest;

import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.service.GroupService;

/** Proxy distant du service des groupes (même principe que RestCustomerService). */
final class RestGroupService implements GroupService {

    private final RestClient client;

    RestGroupService(RestClient client) {
        this.client = client;
    }

    @Override
    public Group createGroup(String name, String description) {
        // Le nom est transmis pour la traduction d'erreur : la réponse « nom déjà pris » ne le contient pas.
        String body = client.post("/groups", RestJson.groupBody(name, description)).bodyOrThrow(name);
        return RestJson.read(body, RestJson::toGroup);
    }

    @Override
    public Group getGroup(long groupId) {
        String body = client.get("/groups/" + groupId).bodyOrThrow();
        return RestJson.read(body, RestJson::toGroup);
    }

    @Override
    public void deleteGroup(long groupId) {
        client.delete("/groups/" + groupId).bodyOrThrow();
    }

    @Override
    public Page<Group> listGroups(PageRequest pageRequest) {
        String body = client.get("/groups" + RestClient.pageQuery(pageRequest)).bodyOrThrow();
        return RestJson.read(body, json -> RestJson.toPage(json, RestJson::toGroup));
    }
}