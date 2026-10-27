package com.romanscad.clientmanagement.rest.resource;

import com.romanscad.clientmanagement.rest.dto.CreateCustomerRequest;
import com.romanscad.clientmanagement.rest.dto.CustomerResponse;
import com.romanscad.clientmanagement.rest.dto.CustomerWithGroupsResponse;
import com.romanscad.clientmanagement.rest.dto.GroupResponse;
import com.romanscad.clientmanagement.rest.dto.PageResponse;
import com.romanscad.clientmanagement.rest.gateway.CustomerGateway;
import com.romanscad.clientmanagement.rest.gateway.MembershipGateway;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import java.net.URI;

/**
 * Ressource /customers. Elle ne contient que du HTTP (chemins, statuts, en-têtes) : le travail est
 * délégué aux passerelles, qui ne connaissent pas HTTP.
 *
 * Le chemin littéral "with-groups" l'emporte sur "{id}" : JAX-RS essaie d'abord les chemins fixes.
 */
@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CustomerResource {

    @Inject
    CustomerGateway customers;

    @Inject
    MembershipGateway memberships;

    @POST
    public Response create(CreateCustomerRequest request, @Context UriInfo uriInfo) {
        CustomerResponse created = customers.create(Requests.requireBody(request));
        URI location = uriInfo.getAbsolutePathBuilder().path(Long.toString(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @GET
    @Path("{id}")
    public CustomerResponse get(@PathParam("id") long id) {
        return customers.get(id);
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) {
        customers.delete(id);
        return Response.noContent().build();
    }

    @GET
    public PageResponse<CustomerResponse> list(@QueryParam("after") Long after, @QueryParam("limit") Integer limit) {
        return customers.list(after, limit);
    }

    @GET
    @Path("with-groups")
    public PageResponse<CustomerWithGroupsResponse> listWithGroups(
            @QueryParam("after") Long after, @QueryParam("limit") Integer limit) {
        return memberships.listCustomersWithGroups(after, limit);
    }

    @GET
    @Path("{id}/groups")
    public PageResponse<GroupResponse> groupsOf(
            @PathParam("id") long id, @QueryParam("after") Long after, @QueryParam("limit") Integer limit) {
        return memberships.listGroupsOfCustomer(id, after, limit);
    }
}