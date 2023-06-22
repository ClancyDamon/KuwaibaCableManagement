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
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Swagger documentation for contract manager.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@RequestMapping(ContractRestController.PATH)
public interface ContractRestOpenApi {
    // <editor-fold desc="contract-manager" defaultstate="collapsed">
    /**
     * Path that includes the Kuwaiba version and module
     */
    public static final String PATH = "/v2.1/contract-manager/"; //NOI18N
    
    @Operation(summary = "Creates a contract pool.", description = "The id of the newly created contract pool.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500",
                description = "An unexpected error occurred. Contact your administrator for details",
                content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,
            value = "createContractPool/{poolName}/{poolDescription}/{poolClassName}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createContractPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_NAME, required = true) String poolName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool description.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_DESCRIPTION, required = true) String poolDescription,
            @NotNull @Parameter(in = ParameterIn.PATH,
                    description = "The pool class. What kind of objects can this pool contain? "
                            + "Must be subclass of GenericContract.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_CLASS_NAME, required = true) String poolClassName,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Updates the attributes of a contract pool.", tags = {"contract-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500",
                description = "An unexpected error occurred. Contact your administrator for details",
                content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateContractPool/{poolId}/{poolClassName}/{poolName}/{poolDescription}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateContractPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The id of the pool to be updated.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_CLASS_NAME, required = true) String poolClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The attribute value for pool name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_NAME, required = true) String poolName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The attribute value for pool description.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_DESCRIPTION, required = true) String poolDescription,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Deletes a contract pool.", tags = {"contract-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500",
                description = "An unexpected error occurred. Contact your administrator for details",
                content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE,
            value = "deleteContractPool/{poolId}/{poolClassName}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteContractPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_CLASS_NAME, required = true) String poolClassName,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the contract pools properties.", description = "The pools properties.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array = @ArraySchema(schema = @Schema(implementation = InventoryObjectPool.class)))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500",
                description = "An unexpected error occurred. Contact your administrator for details",
                content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET, value = "getContractPools/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<InventoryObjectPool> getContractPools(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the contract pools properties.", description = "The pools properties.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = InventoryObjectPool.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
        @ApiResponse(responseCode = "500",
                description = "An unexpected error occurred. Contact your administrator for details",
                content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET,
            value = "getContractPool/{poolId}/{poolClassName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public InventoryObjectPool getContractPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_CLASS_NAME, required = true) String poolClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);

    @Operation(summary = "Creates a contract.", description = "The id of the newly created contract.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.POST,
            value = "createContract/{poolId}/{contractClassName}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createContract(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract pool id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH,
                    description = "The contract class name. Must be subclass of GenericContract.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The set of initial attributes. If no attribute name is specified, an empty string will be used.",
                    required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);

    @Operation(summary = "Updates one or many contract attributes.", tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "updateContract/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void updateContract(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The set of initial attributes. If no attribute name is specified, an empty string will be used.",
                    required = false, content = @Content(schema = @Schema(implementation = HashMap.class)))
            @Valid @RequestBody HashMap<String, String> attributes,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);

    @Operation(summary = "Deletes a contract and delete its association with the related inventory objects. " +
            "These objects will remain untouched.", tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.DELETE,
            value = "deleteContract/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteContract(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);

    @Operation(summary = "Get all contracts, without filters.", description = "The contracts list.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET,
            value = "getAllContracts/{page}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getAllContracts(
            @NotNull @Parameter(in = ParameterIn.PATH,
                    description = "Page number of results to skip. Use -1 to retrieve all.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.PAGE, required = true) long page,
            @NotNull @Parameter(in = ParameterIn.PATH,
                    description = "Max number of results per page. Use -1 to retrieve all.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) long limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);

    @Operation(summary = "Gets the contracts inside a contract pool.", description = "The contracts list.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET,
            value = "getContractsInPool/{poolId}/{limit}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getContractsInPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH,
                    description = "The results limit per page. Use -1 to retrieve all.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.LIMIT, required = true) int limit,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);

    @Operation(summary = "Get contract properties.", description = "The contract properties.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BusinessObject.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET,
            value = "getContract/{contractClassName}/{contractId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public BusinessObject getContract(
            @NotNull @Parameter(in = ParameterIn.PATH,
                    description = "The contract class name. Must be subclass of GenericContract.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Relates a set of objects to a contract.", tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "relateObjectsToContract/{contractClassName}/{contractId}/{objectsClassNames}/{objectsIds}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void relateObjectsToContract(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The objects class names.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECTS_CLASS_NAMES, required = true) String[] objectsClassNames,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The objects ids.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECTS_IDS, required = true) String[] objectsIds,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Relates an object to a contract.", tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "relateObjectToContract/{contractClassName}/{contractId}/{objectClassName}/{objectId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void relateObjectToContract(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The object class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The object id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Releases an object from contract.", tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "releaseObjectFromContract/{objectClassName}/{objectId}/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void releaseObjectFromContract(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The object class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_CLASS_NAME, required = true) String objectClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The object id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.OBJECT_ID, required = true) String objectId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Gets the resources related to a contract.", description = "The contract resources list.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BusinessObjectLight.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.GET,
            value = "getContractResources/{contractClassName}/{contractId}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<BusinessObjectLight> getContractResources(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Creates a copy of a contract.", description = "The newly created contract id.",
            tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "copyContractToPool/{poolId}/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public String copyContractToPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    
    @Operation(summary = "Moves a contract from a pool to another pool.", tags = {"contract-manager"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "405", description = "Method not allowed", content = @Content),
            @ApiResponse(responseCode = "500",
                    description = "An unexpected error occurred. Contact your administrator for details",
                    content = @Content)
    })
    @RequestMapping(method = RequestMethod.PUT,
            value = "moveContractToPool/{poolId}/{contractClassName}/{contractId}/{userName}/{sessionId}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public void moveContractToPool(
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The pool id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.POOL_ID, required = true) String poolId,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract class name.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_CLASS_NAME, required = true) String contractClassName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The contract id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.CONTRACT_ID, required = true) String contractId,
            @Parameter(in = ParameterIn.PATH, description = "The user name of the session.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.USER_NAME, required = true) String userName,
            @NotNull @Parameter(in = ParameterIn.PATH, description = "The session token id.",
                    required = true, schema = @Schema())
            @Valid @PathVariable(value = RestConstants.SESSION_ID, required = true) String sessionId);
    // </editor-fold>
}