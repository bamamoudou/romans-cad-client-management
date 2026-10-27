package com.romanscad.clientmanagement.soap.endpoint;

import com.romanscad.clientmanagement.core.service.CustomerService;
import com.romanscad.clientmanagement.soap.SoapContract;
import com.romanscad.clientmanagement.soap.dto.CustomerDto;
import com.romanscad.clientmanagement.soap.dto.CustomerPageDto;
import com.romanscad.clientmanagement.soap.fault.ClientManagementFault;
import com.romanscad.clientmanagement.soap.fault.SoapCalls;
import com.romanscad.clientmanagement.soap.mapping.DtoMapper;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * Service SOAP de gestion des clients.
 *
 * Couche mince : elle ne contient AUCUNE règle métier. Chaque opération fait trois choses :
 * convertir les paramètres, déléguer au service métier, convertir le résultat en DTO.
 * Les erreurs sont traitées en un seul endroit (SoapCalls).
 *
 * Style document/littéral "wrapped" (valeur par défaut de JAX-WS) : c'est le style
 * recommandé par WS-I, compris par tous les outils de génération de client.
 */
@WebService(name = "CustomerWebService",
        serviceName = "CustomerWebService",
        portName = "CustomerWebServicePort",
        targetNamespace = SoapContract.NAMESPACE)
public class CustomerWebService {

    @Inject
    private CustomerService customerService;

    @WebMethod
    @WebResult(name = "customer", targetNamespace = SoapContract.NAMESPACE)
    public CustomerDto createCustomer(
            @WebParam(name = "firstName", targetNamespace = SoapContract.NAMESPACE) String firstName,
            @WebParam(name = "lastName", targetNamespace = SoapContract.NAMESPACE) String lastName,
            @WebParam(name = "email", targetNamespace = SoapContract.NAMESPACE) String email) throws ClientManagementFault {
        return SoapCalls.call(() ->
                DtoMapper.toCustomerDto(customerService.createCustomer(firstName, lastName, email)));
    }

    @WebMethod
    @WebResult(name = "customer", targetNamespace = SoapContract.NAMESPACE)
    public CustomerDto getCustomer(
            @WebParam(name = "customerId", targetNamespace = SoapContract.NAMESPACE) long customerId) throws ClientManagementFault {
        return SoapCalls.call(() -> DtoMapper.toCustomerDto(customerService.getCustomer(customerId)));
    }

    @WebMethod
    public void deleteCustomer(
            @WebParam(name = "customerId", targetNamespace = SoapContract.NAMESPACE) long customerId) throws ClientManagementFault {
        SoapCalls.run(() -> customerService.deleteCustomer(customerId));
    }

    /** Pagination par curseur : afterId et limit sont facultatifs (première page, 100 éléments). */
    @WebMethod
    @WebResult(name = "page", targetNamespace = SoapContract.NAMESPACE)
    public CustomerPageDto listCustomers(
            @WebParam(name = "afterId", targetNamespace = SoapContract.NAMESPACE) Long afterId,
            @WebParam(name = "limit", targetNamespace = SoapContract.NAMESPACE) Integer limit) throws ClientManagementFault {
        return SoapCalls.call(() ->
                DtoMapper.toCustomerPage(customerService.listCustomers(DtoMapper.toPageRequest(afterId, limit))));
    }
}