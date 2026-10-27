package com.romanscad.clientmanagement.soap.endpoint;

import com.romanscad.clientmanagement.core.service.GroupService;
import com.romanscad.clientmanagement.soap.SoapContract;
import com.romanscad.clientmanagement.soap.dto.GroupDto;
import com.romanscad.clientmanagement.soap.dto.GroupPageDto;
import com.romanscad.clientmanagement.soap.fault.ClientManagementFault;
import com.romanscad.clientmanagement.soap.fault.SoapCalls;
import com.romanscad.clientmanagement.soap.mapping.DtoMapper;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * Service SOAP de gestion des groupes. Même principe que CustomerWebService :
 * aucune règle métier ici, seulement conversion, délégation et conversion.
 */
@WebService(name = "GroupWebService",
        serviceName = "GroupWebService",
        portName = "GroupWebServicePort",
        targetNamespace = SoapContract.NAMESPACE)
public class GroupWebService {

    @Inject
    private GroupService groupService;

    @WebMethod
    @WebResult(name = "group", targetNamespace = SoapContract.NAMESPACE)
    public GroupDto createGroup(
            @WebParam(name = "name", targetNamespace = SoapContract.NAMESPACE) String name,
            @WebParam(name = "description", targetNamespace = SoapContract.NAMESPACE) String description) throws ClientManagementFault {
        return SoapCalls.call(() -> DtoMapper.toGroupDto(groupService.createGroup(name, description)));
    }

    @WebMethod
    @WebResult(name = "group", targetNamespace = SoapContract.NAMESPACE)
    public GroupDto getGroup(
            @WebParam(name = "groupId", targetNamespace = SoapContract.NAMESPACE) long groupId) throws ClientManagementFault {
        return SoapCalls.call(() -> DtoMapper.toGroupDto(groupService.getGroup(groupId)));
    }

    @WebMethod
    public void deleteGroup(
            @WebParam(name = "groupId", targetNamespace = SoapContract.NAMESPACE) long groupId) throws ClientManagementFault {
        SoapCalls.run(() -> groupService.deleteGroup(groupId));
    }

    /** Pagination par curseur : afterId et limit sont facultatifs (première page, 100 éléments). */
    @WebMethod
    @WebResult(name = "page", targetNamespace = SoapContract.NAMESPACE)
    public GroupPageDto listGroups(
            @WebParam(name = "afterId", targetNamespace = SoapContract.NAMESPACE) Long afterId,
            @WebParam(name = "limit", targetNamespace = SoapContract.NAMESPACE) Integer limit) throws ClientManagementFault {
        return SoapCalls.call(() ->
                DtoMapper.toGroupPage(groupService.listGroups(DtoMapper.toPageRequest(afterId, limit))));
    }
}