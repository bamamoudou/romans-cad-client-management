package com.romanscad.clientmanagement.cli.soap;

import com.romanscad.clientmanagement.cli.soap.generated.GroupPage;
import com.romanscad.clientmanagement.cli.soap.generated.GroupWebService;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.service.GroupService;
import java.util.function.Supplier;

/** Proxy distant du service des groupes (même principe que SoapCustomerService). */
final class SoapGroupService implements GroupService {

    private final Supplier<GroupWebService> port;
    private final SoapInvoker invoker;

    SoapGroupService(Supplier<GroupWebService> port, SoapInvoker invoker) {
        this.port = port;
        this.invoker = invoker;
    }

    @Override
    public Group createGroup(String name, String description) {
        // Le nom est transmis à l'invocateur : la faute de doublon ne le contient pas.
        return invoker.invoke(name, () -> SoapMapper.toGroup(port.get().createGroup(name, description)));
    }

    @Override
    public Group getGroup(long groupId) {
        return invoker.invoke(() -> SoapMapper.toGroup(port.get().getGroup(groupId)));
    }

    @Override
    public void deleteGroup(long groupId) {
        invoker.invokeVoid(() -> port.get().deleteGroup(groupId));
    }

    @Override
    public Page<Group> listGroups(PageRequest pageRequest) {
        return invoker.invoke(() -> {
            GroupPage page = port.get().listGroups(pageRequest.afterId(), pageRequest.limit());
            return SoapMapper.toPage(page.getItem(), page.getNextCursor(), SoapMapper::toGroup);
        });
    }
}