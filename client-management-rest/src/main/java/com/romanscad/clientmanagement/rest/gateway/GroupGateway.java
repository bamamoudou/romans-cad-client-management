package com.romanscad.clientmanagement.rest.gateway;

import com.romanscad.clientmanagement.rest.dto.CreateGroupRequest;
import com.romanscad.clientmanagement.rest.dto.GroupResponse;
import com.romanscad.clientmanagement.rest.dto.PageResponse;
import com.romanscad.clientmanagement.rest.soap.generated.GroupPage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Passerelle vers le service SOAP des groupes. */
@ApplicationScoped
public class GroupGateway {

    @Inject
    SoapPorts ports;

    public GroupResponse create(CreateGroupRequest request) {
        return SoapCalls.call(() -> GatewayMapper.toGroup(
                ports.groups().createGroup(request.name(), request.description())));
    }

    public GroupResponse get(long groupId) {
        return SoapCalls.call(() -> GatewayMapper.toGroup(ports.groups().getGroup(groupId)));
    }

    public void delete(long groupId) {
        SoapCalls.run(() -> ports.groups().deleteGroup(groupId));
    }

    public PageResponse<GroupResponse> list(Long afterId, Integer limit) {
        return SoapCalls.call(() -> {
            GroupPage page = ports.groups().listGroups(afterId, limit);
            return GatewayMapper.toPage(page.getItem(), page.getNextCursor(), GatewayMapper::toGroup);
        });
    }
}