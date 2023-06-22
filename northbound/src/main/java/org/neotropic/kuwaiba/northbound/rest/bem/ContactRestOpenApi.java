/*
 * Copyright 2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.northbound.rest.bem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.HashMap;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.Contact;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for contact manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(ContactRestController.PATH)
public interface ContactRestOpenApi {
    // <editor-fold desc="contact-manager" defaultstate="collapsed">
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/contact-manager/"; //NOI18N
    
    @Operation(summary = "Creates a contact. Contacts are always associated to a customer.", description = "The id of the newly created contact.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,value = "createContact/{contactClassName}/{customerClassName}/{customerId}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createContact(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Class of the contact. This class should always be a subclass of GenericContact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_CLASS_NAME, required = true) String contactClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the customer this contact will be associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CUSTOMER_CLASS_NAME, required = true) String customerClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the customer this contact will be associated to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CUSTOMER_ID, required = true) String customerId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The user name of the session.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates a contact's information.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "updateContact/{contactClassName}/{contactId}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateContact(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_CLASS_NAME, required = true) String contactClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_ID, required = true) String contactId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The attributes to be updated. The list types require only the id of the linked list type as a string.",
                    required = false, content = @Content(array = @ArraySchema(schema = @Schema(implementation = StringPair.class)))) 
            @Valid @RequestBody List<StringPair> properties,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The user name of the session.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a contact.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE, value = "deleteContact/{contactClassName}/{contactId}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteContact(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_CLASS_NAME, required = true) String contactClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_ID, required = true) String contactId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The user name of the session.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the entire information of a given contact.", description = "The contact.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Contact.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getContact/{contactClassName}/{contactId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Contact getContact(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_CLASS_NAME, required = true) String contactClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_ID, required = true) String contactId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of contacts associated to a customer.", description = "The list of contacts.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Contact.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getContactsForCustomer/{customerClassName}/{customerId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Contact> getContactsForCustomer(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the customer the contacts belong to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CUSTOMER_CLASS_NAME, required = true) String customerClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the customer the contacts belong to.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CUSTOMER_ID, required = true) String customerId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Searches in all the properties of a contact for a given string.", description = "The list of contacts that matches the search criteria.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Contact.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "searchForContacts/{searchString}/{maxResults}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Contact> searchForContacts(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The string to be matched.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SEARCH_STRING, required = true) String searchString,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The max number of results. Use -1 to retrieve al results.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.MAX_RESULTS, required = true) int maxResults,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of contacts that matches the search criteria.", description = "The list of contacts that matches the search criteria.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Contact.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    public List<Contact> getContacts(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Current page.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) int page,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "Limit of results per page. -1 to retrieve them all.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The response may be filtered "
                    + " by customer (use key <code>customer</code>, value the customer name, a String)"
                    + " by type (use key <code>type</code>, value the type name, a String)"
                    + " by contact name (use key <code>contact_name</code>, value the contact name, a String)"
                    + " by contact email1 (use key <code>contact_email1</code>, value the contact email1, a String)"
                    + " by contact email2 (use key <code>contact_email2</code>, value the contact email2, a String)",
                    required = false, content = @Content(schema = @Schema(implementation = StringPair.class)))
            @Valid @RequestBody HashMap<String, Object> filters,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Relates an inventory object to a contact.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "relateObjectToContact/{objectClassName}/{objectId}/{contactClassName}/{contactId}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void relateObjectToContact(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_CLASS_NAME, required = true) String contactClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_ID, required = true) String contactId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The user name of the session.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Releases an inventory object from a contact.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT, value = "releaseObjectFromContact/{objectClassName}/{objectId}/{contactClassName}/{contactId}/{userName}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void releaseObjectFromContact(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the object.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_CLASS_NAME, required = true) String contactClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_ID, required = true) String contactId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The user name of the session.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Retrieves the list of resources (inventory objects) related to a contact.", description = "List of related resources.", tags = {"contact-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred. Contact your administrator for details", content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getContactResources/{contactClassName}/{contactId}/{sessionId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getContactResources(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The class of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_CLASS_NAME, required = true) String contactClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the contact.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTACT_ID, required = true) String contactId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.", required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}