package com.romanscad.clientmanagement.rest.resource;

import com.romanscad.clientmanagement.rest.dto.CreateGroupRequest;
import com.romanscad.clientmanagement.rest.dto.CustomerResponse;
import com.romanscad.clientmanagement.rest.dto.GroupResponse;
import com.romanscad.clientmanagement.rest.dto.MembershipRequest;
import com.romanscad.clientmanagement.rest.dto.MembershipResult;
import com.romanscad.clientmanagement.rest.dto.PageResponse;
import com.romanscad.clientmanagement.rest.gateway.GroupGateway;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Ressource /groups, avec les appartenances sous /groups/{id}/customers.
 *
 * La dissociation en masse est un POST sur ".../customers/removal" et non un DELETE avec corps :
 * beaucoup de clients et de proxys ignorent ou refusent le corps d'une requête DELETE.
 */
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class GroupResource {

    @Inject
    GroupGateway groups;

    @Inject
    MembershipGateway memberships;

    @POST
    public Response create(CreateGroupRequest request, @Context UriInfo uriInfo) {
        GroupResponse created = groups.create(Requests.requireBody(request));
        URI location = uriInfo.getAbsolutePathBuilder().path(Long.toString(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @GET
    @Path("{id}")
    public GroupResponse get(@PathParam("id") long id) {
        return groups.get(id);
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) {
        groups.delete(id);
        return Response.noContent().build();
    }

    @GET
    public PageResponse<GroupResponse> list(@QueryParam("after") Long after, @QueryParam("limit") Integer limit) {
        return groups.list(after, limit);
    }

    @GET
    @Path("{id}/customers")
    public PageResponse<CustomerResponse> customersOf(
            @PathParam("id") long id, @QueryParam("after") Long after, @QueryParam("limit") Integer limit) {
        return memberships.listCustomersOfGroup(id, after, limit);
    }

    @POST
    @Path("{id}/customers")
    public MembershipResult addCustomers(@PathParam("id") long id, MembershipRequest request) {
        return new MembershipResult(memberships.addCustomersToGroup(id, Requests.requireCustomerIds(request)));
    }

    @POST
    @Path("{id}/customers/removal")
    public MembershipResult removeCustomers(@PathParam("id") long id, MembershipRequest request) {
        return new MembershipResult(memberships.removeCustomersFromGroup(id, Requests.requireCustomerIds(request)));
    }
}