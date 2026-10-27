package com.romanscad.clientmanagement.cli.soap.generated;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.romanscad.clientmanagement.cli.soap.generated package. 
 * <p>An ObjectFactory allows you to programmatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private static final QName _AddCustomersToGroup_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "addCustomersToGroup");
    private static final QName _AddCustomersToGroupResponse_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "addCustomersToGroupResponse");
    private static final QName _ListCustomersOfGroup_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "listCustomersOfGroup");
    private static final QName _ListCustomersOfGroupResponse_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "listCustomersOfGroupResponse");
    private static final QName _ListCustomersWithGroups_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "listCustomersWithGroups");
    private static final QName _ListCustomersWithGroupsResponse_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "listCustomersWithGroupsResponse");
    private static final QName _ListGroupsOfCustomer_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "listGroupsOfCustomer");
    private static final QName _ListGroupsOfCustomerResponse_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "listGroupsOfCustomerResponse");
    private static final QName _RemoveCustomersFromGroup_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "removeCustomersFromGroup");
    private static final QName _RemoveCustomersFromGroupResponse_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "removeCustomersFromGroupResponse");
    private static final QName _ClientManagementFault_QNAME = new QName("http://clientmanagement.romanscad.com/v1", "ClientManagementFault");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.romanscad.clientmanagement.cli.soap.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AddCustomersToGroup }
     * 
     * @return
     *     the new instance of {@link AddCustomersToGroup }
     */
    public AddCustomersToGroup createAddCustomersToGroup() {
        return new AddCustomersToGroup();
    }

    /**
     * Create an instance of {@link AddCustomersToGroupResponse }
     * 
     * @return
     *     the new instance of {@link AddCustomersToGroupResponse }
     */
    public AddCustomersToGroupResponse createAddCustomersToGroupResponse() {
        return new AddCustomersToGroupResponse();
    }

    /**
     * Create an instance of {@link ListCustomersOfGroup }
     * 
     * @return
     *     the new instance of {@link ListCustomersOfGroup }
     */
    public ListCustomersOfGroup createListCustomersOfGroup() {
        return new ListCustomersOfGroup();
    }

    /**
     * Create an instance of {@link ListCustomersOfGroupResponse }
     * 
     * @return
     *     the new instance of {@link ListCustomersOfGroupResponse }
     */
    public ListCustomersOfGroupResponse createListCustomersOfGroupResponse() {
        return new ListCustomersOfGroupResponse();
    }

    /**
     * Create an instance of {@link ListCustomersWithGroups }
     * 
     * @return
     *     the new instance of {@link ListCustomersWithGroups }
     */
    public ListCustomersWithGroups createListCustomersWithGroups() {
        return new ListCustomersWithGroups();
    }

    /**
     * Create an instance of {@link ListCustomersWithGroupsResponse }
     * 
     * @return
     *     the new instance of {@link ListCustomersWithGroupsResponse }
     */
    public ListCustomersWithGroupsResponse createListCustomersWithGroupsResponse() {
        return new ListCustomersWithGroupsResponse();
    }

    /**
     * Create an instance of {@link ListGroupsOfCustomer }
     * 
     * @return
     *     the new instance of {@link ListGroupsOfCustomer }
     */
    public ListGroupsOfCustomer createListGroupsOfCustomer() {
        return new ListGroupsOfCustomer();
    }

    /**
     * Create an instance of {@link ListGroupsOfCustomerResponse }
     * 
     * @return
     *     the new instance of {@link ListGroupsOfCustomerResponse }
     */
    public ListGroupsOfCustomerResponse createListGroupsOfCustomerResponse() {
        return new ListGroupsOfCustomerResponse();
    }

    /**
     * Create an instance of {@link RemoveCustomersFromGroup }
     * 
     * @return
     *     the new instance of {@link RemoveCustomersFromGroup }
     */
    public RemoveCustomersFromGroup createRemoveCustomersFromGroup() {
        return new RemoveCustomersFromGroup();
    }

    /**
     * Create an instance of {@link RemoveCustomersFromGroupResponse }
     * 
     * @return
     *     the new instance of {@link RemoveCustomersFromGroupResponse }
     */
    public RemoveCustomersFromGroupResponse createRemoveCustomersFromGroupResponse() {
        return new RemoveCustomersFromGroupResponse();
    }

    /**
     * Create an instance of {@link ErrorDetail }
     * 
     * @return
     *     the new instance of {@link ErrorDetail }
     */
    public ErrorDetail createErrorDetail() {
        return new ErrorDetail();
    }

    /**
     * Create an instance of {@link CustomerWithGroupsPage }
     * 
     * @return
     *     the new instance of {@link CustomerWithGroupsPage }
     */
    public CustomerWithGroupsPage createCustomerWithGroupsPage() {
        return new CustomerWithGroupsPage();
    }

    /**
     * Create an instance of {@link CustomerWithGroups }
     * 
     * @return
     *     the new instance of {@link CustomerWithGroups }
     */
    public CustomerWithGroups createCustomerWithGroups() {
        return new CustomerWithGroups();
    }

    /**
     * Create an instance of {@link Customer }
     * 
     * @return
     *     the new instance of {@link Customer }
     */
    public Customer createCustomer() {
        return new Customer();
    }

    /**
     * Create an instance of {@link Group }
     * 
     * @return
     *     the new instance of {@link Group }
     */
    public Group createGroup() {
        return new Group();
    }

    /**
     * Create an instance of {@link CustomerPage }
     * 
     * @return
     *     the new instance of {@link CustomerPage }
     */
    public CustomerPage createCustomerPage() {
        return new CustomerPage();
    }

    /**
     * Create an instance of {@link GroupPage }
     * 
     * @return
     *     the new instance of {@link GroupPage }
     */
    public GroupPage createGroupPage() {
        return new GroupPage();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddCustomersToGroup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AddCustomersToGroup }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "addCustomersToGroup")
    public JAXBElement<AddCustomersToGroup> createAddCustomersToGroup(AddCustomersToGroup value) {
        return new JAXBElement<>(_AddCustomersToGroup_QNAME, AddCustomersToGroup.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddCustomersToGroupResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AddCustomersToGroupResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "addCustomersToGroupResponse")
    public JAXBElement<AddCustomersToGroupResponse> createAddCustomersToGroupResponse(AddCustomersToGroupResponse value) {
        return new JAXBElement<>(_AddCustomersToGroupResponse_QNAME, AddCustomersToGroupResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListCustomersOfGroup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ListCustomersOfGroup }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "listCustomersOfGroup")
    public JAXBElement<ListCustomersOfGroup> createListCustomersOfGroup(ListCustomersOfGroup value) {
        return new JAXBElement<>(_ListCustomersOfGroup_QNAME, ListCustomersOfGroup.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListCustomersOfGroupResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ListCustomersOfGroupResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "listCustomersOfGroupResponse")
    public JAXBElement<ListCustomersOfGroupResponse> createListCustomersOfGroupResponse(ListCustomersOfGroupResponse value) {
        return new JAXBElement<>(_ListCustomersOfGroupResponse_QNAME, ListCustomersOfGroupResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListCustomersWithGroups }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ListCustomersWithGroups }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "listCustomersWithGroups")
    public JAXBElement<ListCustomersWithGroups> createListCustomersWithGroups(ListCustomersWithGroups value) {
        return new JAXBElement<>(_ListCustomersWithGroups_QNAME, ListCustomersWithGroups.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListCustomersWithGroupsResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ListCustomersWithGroupsResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "listCustomersWithGroupsResponse")
    public JAXBElement<ListCustomersWithGroupsResponse> createListCustomersWithGroupsResponse(ListCustomersWithGroupsResponse value) {
        return new JAXBElement<>(_ListCustomersWithGroupsResponse_QNAME, ListCustomersWithGroupsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListGroupsOfCustomer }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ListGroupsOfCustomer }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "listGroupsOfCustomer")
    public JAXBElement<ListGroupsOfCustomer> createListGroupsOfCustomer(ListGroupsOfCustomer value) {
        return new JAXBElement<>(_ListGroupsOfCustomer_QNAME, ListGroupsOfCustomer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListGroupsOfCustomerResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ListGroupsOfCustomerResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "listGroupsOfCustomerResponse")
    public JAXBElement<ListGroupsOfCustomerResponse> createListGroupsOfCustomerResponse(ListGroupsOfCustomerResponse value) {
        return new JAXBElement<>(_ListGroupsOfCustomerResponse_QNAME, ListGroupsOfCustomerResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemoveCustomersFromGroup }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RemoveCustomersFromGroup }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "removeCustomersFromGroup")
    public JAXBElement<RemoveCustomersFromGroup> createRemoveCustomersFromGroup(RemoveCustomersFromGroup value) {
        return new JAXBElement<>(_RemoveCustomersFromGroup_QNAME, RemoveCustomersFromGroup.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemoveCustomersFromGroupResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link RemoveCustomersFromGroupResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "removeCustomersFromGroupResponse")
    public JAXBElement<RemoveCustomersFromGroupResponse> createRemoveCustomersFromGroupResponse(RemoveCustomersFromGroupResponse value) {
        return new JAXBElement<>(_RemoveCustomersFromGroupResponse_QNAME, RemoveCustomersFromGroupResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrorDetail }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ErrorDetail }{@code >}
     */
    @XmlElementDecl(namespace = "http://clientmanagement.romanscad.com/v1", name = "ClientManagementFault")
    public JAXBElement<ErrorDetail> createClientManagementFault(ErrorDetail value) {
        return new JAXBElement<>(_ClientManagementFault_QNAME, ErrorDetail.class, null, value);
    }

}
