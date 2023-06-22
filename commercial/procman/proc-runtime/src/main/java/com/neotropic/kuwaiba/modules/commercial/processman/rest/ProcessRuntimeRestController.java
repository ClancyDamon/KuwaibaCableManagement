/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.northbound.rest.RestConstants;
import org.neotropic.kuwaiba.northbound.rest.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Set the resources to manage the process runtime.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@RestController
@RequestMapping("/v1/proc-runtime")
public class ProcessRuntimeRestController {
    @Autowired
    private ApplicationEntityManager aem;
    
    @PostMapping("/create/process-instance")
    public ResponseEntity createProcessInstance(
        @RequestHeader(RestConstants.HEADER_TOKEN) String token, 
        @RequestBody ObjectNode payload) {
        
        try {
            aem.validateCall("ProcessRuntimeRestController#createProcessInstance", token);
            
            String processDefinitionId = payload.get("processDefinitionId").asText();
            String name = payload.get("name").asText();
            String description = payload.get("description").asText();
            
            String processInstanceId = aem.createProcessInstance(processDefinitionId, name, description);
                        
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("processInstanceId", processInstanceId);
            
            return new ResponseEntity(objectNode, HttpStatus.OK);
            
        } catch (InventoryException ex) {
            return RestUtil.getResponseEntity(ex);
        }
    }
    
    @PutMapping("/commit/activity")
    public ResponseEntity commitActivity(
        @RequestHeader(RestConstants.HEADER_TOKEN) String token, 
        @RequestBody ObjectNode payload) {
        
        try {
            aem.validateCall("ProcessRuntimeRestController#commitActivity", token);
            
            String processInstanceId = payload.get("processInstanceId").asText();
            String activityDefinitionId = payload.get("activityDefinitionId").asText();
            String id = payload.get("id").asText();
            String content = payload.get("content").asText();
            
            List<StringPair> sharedInformation = new ArrayList();
            if (payload.get("sharedInformation").isObject()) {
                ObjectNode sharedInfoNode = (ObjectNode) payload.get("sharedInformation");
                sharedInfoNode.fields().forEachRemaining(entry -> 
                    sharedInformation.add(new StringPair(entry.getKey(), entry.getValue().asText()))
                );
            }
            long creationDate = payload.get("creationDate").asLong();
            long commitDate = payload.get("commitDate").asLong();
            
            Artifact artifact = new Artifact(id, "", "", content != null ? content.getBytes() : new byte[0], sharedInformation, creationDate, commitDate);
            
            aem.commitActivity(processInstanceId, activityDefinitionId, artifact);
            
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("commit", true);
            
            return new ResponseEntity(objectNode, HttpStatus.OK);
        } catch (InventoryException ex) {
            return RestUtil.getResponseEntity(ex);
        }
    }
}
