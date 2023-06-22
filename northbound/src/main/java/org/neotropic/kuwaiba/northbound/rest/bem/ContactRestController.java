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

import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.Contact;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The definition of the Contact Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(ContactRestController.PATH)
public class ContactRestController implements ContactRestOpenApi {
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    
    /**
     * Reference to the default logger.
     */
    public static Logger LOG = LoggerFactory.getLogger(ContactRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and core
     */
    public static final String PATH = "/v2.1/contact-manager/"; //NOI18N
    
    // <editor-fold desc="contact-manager" defaultstate="collapsed">
    /**
     * Creates a contact. Contacts are always associated to a customer.
     * @param contactClassName Class of the contact. This class should always be a subclass of GenericContact.
     * @param customerClassName The class of the customer this contact will be associated to.
     * @param customerId The id of the customer this contact will be associated to.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     * @return The id of the newly created contact.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createContact/{contactClassName}/{customerClassName}/{customerId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createContact(
            @PathVariable(RestConstants.CONTACT_CLASS_NAME) String contactClassName,
            @PathVariable(RestConstants.CUSTOMER_CLASS_NAME) String customerClassName,
            @PathVariable(RestConstants.CUSTOMER_ID) String customerId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createContact", "127.0.0.1", sessionId);
            return bem.createContact(contactClassName, customerClassName, customerId, userName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates a contact's information.
     * @param contactClassName The class of the contact.
     * @param contactId The id of the contact.
     * @param properties The attributes to be updated. The list types require only the id of the linked list type as a string.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateContact/{contactClassName}/{contactId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateContact(
            @PathVariable(RestConstants.CONTACT_CLASS_NAME) String contactClassName,
            @PathVariable(RestConstants.CONTACT_ID) String contactId,
            @RequestBody List<StringPair> properties,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateContact", "127.0.0.1", sessionId);
            bem.updateContact(contactClassName, contactId, properties, userName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Deletes a contact.
     * @param contactClassName The class of the contact.
     * @param contactId The id of the contact.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = "deleteContact/{contactClassName}/{contactId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteContact(
            @PathVariable(RestConstants.CONTACT_CLASS_NAME) String contactClassName,
            @PathVariable(RestConstants.CONTACT_ID) String contactId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteContact", "127.0.0.1", sessionId);
            bem.deleteContact(contactClassName, contactId, userName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Gets the entire information of a given contact.
     * @param contactClassName The class of the contact.
     * @param contactId The id of the contact
     * @param sessionId The session token id.
     * @return The contact.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getContact/{contactClassName}/{contactId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Contact getContact(
            @PathVariable(RestConstants.CONTACT_CLASS_NAME) String contactClassName,
            @PathVariable(RestConstants.CONTACT_ID) String contactId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContact", "127.0.0.1", sessionId);
            return bem.getContact(contactClassName, contactId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves the list of contacts associated to a customer.
     * @param customerClassName The class of the customer the contacts belong to.
     * @param customerId The id of the customer the contacts belong to.
     * @param sessionId The session token id.
     * @return The list of contacts.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getContactsForCustomer/{customerClassName}/{customerId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Contact> getContactsForCustomer(
            @PathVariable(RestConstants.CUSTOMER_CLASS_NAME) String customerClassName,
            @PathVariable(RestConstants.CUSTOMER_ID) String customerId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContactsForCustomer", "127.0.0.1", sessionId);
            return bem.getContactsForCustomer(customerClassName, customerId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Searches in all the properties of a contact for a given string.
     * @param searchString The string to be matched.
     * @param maxResults The max number of results. Use -1 to retrieve al results.
     * @param sessionId The session token id.
     * @return The list of contacts that matches the search criteria.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "searchForContacts/{searchString}/{maxResults}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Contact> searchForContacts(
            @PathVariable(RestConstants.SEARCH_STRING) String searchString,
            @PathVariable(RestConstants.MAX_RESULTS) int maxResults,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("searchForContacts", "127.0.0.1", sessionId);
            return bem.searchForContacts(searchString, maxResults);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves the list of contacts that matches the search criteria.
     * @param page Current page.
     * @param limit Limit of results per page. -1 to retrieve them all.
     * @param filters The response may be filtered 
     * by customer (use key <code>customer</code>, value the customer name, a String)
     * by type (use key <code>type</code>, value the type name, a String)
     * by contact name (use key <code>contact_name</code>, value the contact name, a String)
     * by contact email1 (use key <code>contact_email1</code>, value the contact email1, a String)
     * by contact email2 (use key <code>contact_email2</code>, value the contact email2, a String)
     * @param sessionId The session token id.
     * @return The list of contacts that matches the search criteria.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "getContacts/{page}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Contact> getContacts(
            @PathVariable(RestConstants.PAGE) int page,
            @PathVariable(RestConstants.LIMIT) int limit,
            @RequestBody(required = false) HashMap<String, Object> filters,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContacts", "127.0.0.1", sessionId);
            return bem.getContacts(page, limit, filters);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Relates an inventory object to a contact.
     * @param objectClassName The class of the object.
     * @param objectId The id of the object.
     * @param contactClassName The class of the contact.
     * @param contactId The id of the contact.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "relateObjectToContact/{objectClassName}/{objectId}/{contactClassName}/{contactId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void relateObjectToContact(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CONTACT_CLASS_NAME) String contactClassName,
            @PathVariable(RestConstants.CONTACT_ID) String contactId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("relateObjectToContact", "127.0.0.1", sessionId);
            bem.relateObjectToContact(objectClassName, objectId, contactClassName, contactId, userName);
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Releases an inventory object from a contact.
     * @param objectClassName The class of the object.
     * @param objectId The id of the object.
     * @param contactClassName The class of the contact.
     * @param contactId The id of the contact.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "releaseObjectFromContact/{objectClassName}/{objectId}/{contactClassName}/{contactId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void releaseObjectFromContact(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CONTACT_CLASS_NAME) String contactClassName,
            @PathVariable(RestConstants.CONTACT_ID) String contactId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("releaseObjectFromContact", "127.0.0.1", sessionId);
            bem.releaseObjectFromContact(objectClassName, objectId, contactClassName, contactId, userName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Retrieves the list of resources (inventory objects) related to a contact. 
     * @param contactClassName The class of the contact.
     * @param contactId The id of the contact.
     * @param sessionId The session token id.
     * @return List of related resources.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getContactResources/{contactClassName}/{contactId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getContactResources(
            @PathVariable(RestConstants.CONTACT_CLASS_NAME) String contactClassName,
            @PathVariable(RestConstants.CONTACT_ID) String contactId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContactResources", "127.0.0.1", sessionId);
            return bem.getContactResources(contactClassName, contactId);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    // </editor-fold>
}