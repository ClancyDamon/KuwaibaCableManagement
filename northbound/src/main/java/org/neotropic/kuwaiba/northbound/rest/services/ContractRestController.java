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
package org.neotropic.kuwaiba.northbound.rest.services;

import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.*;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.contractman.ContractManagerService;
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
 * Contract Manager Rest Controller.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RestController
@RequestMapping(ContractRestController.PATH)
public class ContractRestController implements ContractRestOpenApi {
    /**
     * Reference to the Contract Manager Service
     */
    @Autowired 
    private ContractManagerService cms;
    
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
    public static Logger LOG = LoggerFactory.getLogger(ContractRestController.class);
    
    /**
     * Path that includes the Kuwaiba version and module
     */
    public static final String PATH = "/v2.1/contract-manager/"; //NOI18N
    
    // <editor-fold desc="contract-manager" defaultstate="collapsed">
    /**
     * Creates a contract pool.
     * @param poolName The pool name. 
     * @param poolDescription The pool description.
     * @param poolClassName The pool class. What kind of objects can this pool contain? 
     * Must be subclass of GenericContract.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     * @return The id of the newly created contract pool.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createContractPool/{poolName}/{poolDescription}/{poolClassName}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createContractPool(
            @PathVariable(RestConstants.POOL_NAME) String poolName,
            @PathVariable(RestConstants.POOL_DESCRIPTION) String poolDescription,
            @PathVariable(RestConstants.POOL_CLASS_NAME) String poolClassName,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createContractPool", "127.0.0.1", sessionId);
            return cms.createContractPool(poolName, poolDescription, poolClassName, userName);
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException| MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Updates the attributes of a contract pool.
     * @param poolId The id of the pool to be updated.
     * @param poolClassName The pool class name.
     * @param poolName The attribute value for pool name.
     * @param poolDescription The attribute value for pool description.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateContractPool/{poolId}/{poolClassName}/{poolName}/{poolDescription}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateContractPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.POOL_CLASS_NAME) String poolClassName,
            @PathVariable(RestConstants.POOL_NAME) String poolName,
            @PathVariable(RestConstants.POOL_DESCRIPTION) String poolDescription,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateContractPool", "127.0.0.1", sessionId);
            cms.updateContractPool(poolId, poolClassName, poolName, poolDescription, userName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException| MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Deletes a contract pool.
     * @param poolId The pool id.
     * @param poolClassName The pool class name.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = "deleteContractPool/{poolId}/{poolClassName}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteContractPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.POOL_CLASS_NAME) String poolClassName,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteContractPool", "127.0.0.1", sessionId);
            cms.deleteContractPool(poolId, poolClassName, userName);
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException| MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Gets the contract pools properties.
     * @param sessionId The session token id.
     * @return The pools properties.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getContractPools/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getContractPools(@PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContractPools", "127.0.0.1", sessionId);
            return cms.getContractPools();
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
     * Gets the contract pools properties.
     * @param poolId The pool id.
     * @param poolClassName The pool class name.
     * @param sessionId The session token id.
     * @return The pool properties.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getContractPool/{poolId}/{poolClassName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public InventoryObjectPool getContractPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.POOL_CLASS_NAME) String poolClassName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContractPool", "127.0.0.1", sessionId);
            return cms.getContractPool(poolId, poolClassName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException| MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /**
     * Creates a contract.
     * @param poolId The contract pool id.
     * @param contractClassName The contract class name. Must be subclass of GenericContract.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     * @return The id of the newly created contract.
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "createContract/{poolId}/{contractClassName}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createContract(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @RequestBody(required = false) HashMap<String, String> attributes,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("createContract", "127.0.0.1", sessionId);
            return cms.createContract(
                    poolId,
                    contractClassName,
                    attributes != null ? attributes : new HashMap<String, String>(),
                    userName
            );
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
     * Updates one or many contract attributes.
     * @param contractClassName The contract class name.
     * @param contractId The contract id.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateContract/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateContract(
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @RequestBody(required = false) HashMap<String, String> attributes,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("updateContract", "127.0.0.1", sessionId);
            cms.updateContract(
                    contractClassName,
                    contractId,
                    attributes != null ? attributes : new HashMap<String, String>(),
                    userName
            );
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
     * Deletes a contract and delete its association with the related inventory objects.
     * These objects will remain untouched.
     * @param contractClassName The contract class name.
     * @param contractId The contract id.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = "deleteContract/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteContract(
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("deleteContract", "127.0.0.1", sessionId);
            cms.deleteContract(contractClassName, contractId, userName);
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
     * Get all contracts, without filters.
     * @param page Page number of results to skip. Use -1 to retrieve all.
     * @param limit Max number of results per page. Use -1 to retrieve all.
     * @param sessionId The session token id.
     * @return The contracts list.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getAllContracts/{page}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getAllContracts(
            @PathVariable(RestConstants.PAGE) long page,
            @PathVariable(RestConstants.LIMIT) long limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId) 
    {
        try {
            aem.validateCall("getAllContracts", "127.0.0.1", sessionId);
            return cms.getAllContracts(page, limit);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }
    
    /** 
     * Gets the contracts inside a contract pool.
     * @param poolId The pool id.
     * @param limit The results limit per page. Use -1 to retrieve all.
     * @param sessionId The session token id.
     * @return The contracts list.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getContractsInPool/{poolId}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getContractsInPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.LIMIT) int limit,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContractsInPool", "127.0.0.1", sessionId);
            return cms.getContractsInPool(poolId, limit);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }

    /**
     * Get contract properties.
     * @param contractClassName The contract class name. Must be subclass of GenericContract.
     * @param contractId The contract id.
     * @param sessionId The session token id.
     * @return The contract properties.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getContract/{contractClassName}/{contractId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObject getContract(
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContract", "127.0.0.1", sessionId);
            return cms.getContract(contractClassName, contractId);
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
     * Relates a set of objects to a contract.
     * @param contractClassName The contract class name.
     * @param contractId The contract id.
     * @param objectsClassNames The objects class names.
     * @param objectsIds The objects ids.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "relateObjectsToContract/{contractClassName}/{contractId}/{objectsClassNames}/{objectsIds}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void relateObjectsToContract(
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @PathVariable(RestConstants.OBJECTS_CLASS_NAMES) String[] objectsClassNames,
            @PathVariable(RestConstants.OBJECTS_IDS) String[] objectsIds,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("relateObjectsToContract", "127.0.0.1", sessionId);
            cms.relateObjectsToContract(contractClassName, contractId, objectsClassNames, objectsIds, userName);
        } catch (InvalidArgumentException | OperationNotPermittedException | ArraySizeMismatchException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException
                 | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }

    /**
     * Relates an object to a contract.
     * @param contractClassName The contract class name.
     * @param contractId The contract id.
     * @param objectClassName The object class name.
     * @param objectId The object id.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "relateObjectToContract/{contractClassName}/{contractId}/{objectClassName}/{objectId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void relateObjectToContract(
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("relateObjectToContract", "127.0.0.1", sessionId);
            cms.relateObjectToContract(contractClassName, contractId, objectClassName, objectId, userName);
        } catch (InvalidArgumentException | OperationNotPermittedException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException
                 | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }

    /**
     * Releases an object from contract.
     * @param objectClassName The object class name.
     * @param objectId The object id.
     * @param contractClassName The contract class name.
     * @param contractId The contract id.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "releaseObjectFromContract/{objectClassName}/{objectId}/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void releaseObjectFromContract(
            @PathVariable(RestConstants.OBJECT_CLASS_NAME) String objectClassName,
            @PathVariable(RestConstants.OBJECT_ID) String objectId,
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("releaseObjectFromContract", "127.0.0.1", sessionId);
            cms.releaseObjectFromContract(objectClassName, objectId, contractClassName, contractId, userName);
        } catch (InvalidArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NotAuthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ApplicationObjectNotFoundException | BusinessObjectNotFoundException
                 | MetadataObjectNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            LOG.debug("", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ts.getTranslatedString("api.rest.general.error.unexpected-error"));
        }
    }

    /**
     * Gets the resources related to a contract.
     * @param contractClassName The contract class name.
     * @param contractId The contract id.
     * @param sessionId The session token id.
     * @return The contract resources list.
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "getContractResources/{contractClassName}/{contractId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getContractResources(
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("getContractResources", "127.0.0.1", sessionId);
            return cms.getContractResources(contractClassName, contractId);
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
     * Creates a copy of a contract.
     * @param poolId The pool id.
     * @param contractClassName The contract class name.
     * @param contractId The contract id.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     * @return The newly created contract id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "copyContractToPool/{poolId}/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String copyContractToPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("copyContractToPool", "127.0.0.1", sessionId);
            return cms.copyContractToPool(poolId, contractClassName, contractId, userName);
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
     * Moves a contract from a pool to another pool.
     * @param poolId The pool id.
     * @param contractClassName The contract class name.
     * @param contractId The contract id.
     * @param userName The user name of the session.
     * @param sessionId The session token id.
     */
    @RequestMapping(method = RequestMethod.PUT,
            value = "moveContractToPool/{poolId}/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void moveContractToPool(
            @PathVariable(RestConstants.POOL_ID) String poolId,
            @PathVariable(RestConstants.CONTRACT_CLASS_NAME) String contractClassName,
            @PathVariable(RestConstants.CONTRACT_ID) String contractId,
            @PathVariable(RestConstants.USER_NAME) String userName,
            @PathVariable(RestConstants.SESSION_ID) String sessionId)
    {
        try {
            aem.validateCall("moveContractToPool", "127.0.0.1", sessionId);
            cms.moveContractToPool(poolId, contractClassName, contractId, userName);
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
    // </editor-fold>
}