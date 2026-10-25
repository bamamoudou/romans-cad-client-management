package com.romanscad.clientmanagement.soap.endpoint;

import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.service.MembershipService;
import com.romanscad.clientmanagement.soap.SoapContract;
import com.romanscad.clientmanagement.soap.dto.CustomerPageDto;
import com.romanscad.clientmanagement.soap.dto.CustomerWithGroupsPageDto;
import com.romanscad.clientmanagement.soap.dto.GroupPageDto;
import com.romanscad.clientmanagement.soap.fault.ClientManagementFault;
import com.romanscad.clientmanagement.soap.fault.SoapCalls;
import com.romanscad.clientmanagement.soap.mapping.DtoMapper;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import java.util.List;

/**
 * Service SOAP de gestion des appartenances client/groupe : association, dissociation et
 * consultation dans les deux sens.
 */
@WebService(name = "MembershipWebService",
        serviceName = "MembershipWebService",
        portName = "MembershipWebServicePort",
        targetNamespace = SoapContract.NAMESPACE)
public class MembershipWebService {

    /**
     * Nombre maximal de clients par appel d'association ou de dissociation.
     * Le service métier n'a pas de limite ; c'est la frontière du système qui protège le
     * serveur contre une requête démesurée (même logique que la taille maximale de page).
     */
    public static final int MAX_BATCH_SIZE = 1_000;

    @Inject
    private MembershipService membershipService;

    /** @return le nombre d'associations réellement créées (les clients déjà membres sont ignorés) */
    @WebMethod
    @WebResult(name = "added")
    public int addCustomersToGroup(
            @WebParam(name = "groupId") long groupId,
            @WebParam(name = "customerId") List<Long> customerIds) throws ClientManagementFault {
        return SoapCalls.call(() ->
                membershipService.addCustomersToGroup(groupId, requireWithinBatchLimit(customerIds)));
    }

    /** @return le nombre d'associations réellement supprimées */
    @WebMethod
    @WebResult(name = "removed")
    public int removeCustomersFromGroup(
            @WebParam(name = "groupId") long groupId,
            @WebParam(name = "customerId") List<Long> customerIds) throws ClientManagementFault {
        return SoapCalls.call(() ->
                membershipService.removeCustomersFromGroup(groupId, requireWithinBatchLimit(customerIds)));
    }

    @WebMethod
    @WebResult(name = "page")
    public CustomerPageDto listCustomersOfGroup(
            @WebParam(name = "groupId") long groupId,
            @WebParam(name = "afterId") Long afterId,
            @WebParam(name = "limit") Integer limit) throws ClientManagementFault {
        return SoapCalls.call(() -> DtoMapper.toCustomerPage(
                membershipService.listCustomersOfGroup(groupId, DtoMapper.toPageRequest(afterId, limit))));
    }

    @WebMethod
    @WebResult(name = "page")
    public GroupPageDto listGroupsOfCustomer(
            @WebParam(name = "customerId") long customerId,
            @WebParam(name = "afterId") Long afterId,
            @WebParam(name = "limit") Integer limit) throws ClientManagementFault {
        return SoapCalls.call(() -> DtoMapper.toGroupPage(
                membershipService.listGroupsOfCustomer(customerId, DtoMapper.toPageRequest(afterId, limit))));
    }

    /** Vue « clients et groupes associés » : la pagination porte sur les clients. */
    @WebMethod
    @WebResult(name = "page")
    public CustomerWithGroupsPageDto listCustomersWithGroups(
            @WebParam(name = "afterId") Long afterId,
            @WebParam(name = "limit") Integer limit) throws ClientManagementFault {
        return SoapCalls.call(() -> DtoMapper.toCustomerWithGroupsPage(
                membershipService.listCustomersWithGroups(DtoMapper.toPageRequest(afterId, limit))));
    }

    /**
     * Refuse un lot trop gros. Une liste nulle ou vide passe : c'est le service métier qui la
     * refuse, avec son propre message.
     */
    static List<Long> requireWithinBatchLimit(List<Long> customerIds) {
        if (customerIds != null && customerIds.size() > MAX_BATCH_SIZE) {
            throw new ValidationException("customerId must not contain more than " + MAX_BATCH_SIZE + " identifiers");
        }
        return customerIds;
    }
}