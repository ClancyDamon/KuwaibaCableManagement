/*
 *  Copyright 2023 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.northbound.rest;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringEscapeUtils;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.northbound.rest.todeserialize.TransientArtifact;
import org.neotropic.kuwaiba.northbound.rest.todeserialize.TransientScriptedQueryParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Set of methods to use in the REST Controllers
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RestUtil {    
    /**
     * Gets a no content response.
     * @param ex Throw exception
     * @return A no content response with header error.
     */
    public static ResponseEntity getResponseEntity(Exception ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(RestConstants.HEADER_ERROR, Arrays.asList(StringEscapeUtils.escapeHtml4(ex.getLocalizedMessage())));
        return new ResponseEntity(headers, HttpStatus.NO_CONTENT);
    }
    
    /**
     * Evaluates if a string is in Base64 format.
     * @param string String to be evaluated.
     * @return True if the string is in Base64 format otherwise false.
     */
    public static boolean isBase64(String string) {
        String regex = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        return matcher.find();
    }
    
    /**
     * Helper class to parse from a TransientScriptedQueryParameter into a ScriptedQueryParameter.
     * @param parameter Transient ScriptedQuery Parameter.
     * @return ScriptedQueryParameter.
     */
    public static ScriptedQueryParameter transientScriptedQueryParameterToScriptedQueryParameter(TransientScriptedQueryParameter parameter) {
        if (parameter == null)
            return null;
        else {
            if (parameter.getId() != null && parameter.getName() != null && parameter.getDescription() != null
                    && parameter.getType() != null && parameter.getDefaultValue() != null) {
                return new ScriptedQueryParameter(
                        parameter.getId(),
                        parameter.getName(),
                        parameter.getDescription(),
                        parameter.getType(),
                        parameter.isMandatory(),
                        parameter.getDefaultValue()
                );
            } else if (parameter.getName() != null && parameter.getValue() != null) {
                return new ScriptedQueryParameter(
                        parameter.getName(),
                        parameter.getValue()
                );
            } else   
                return null;
        }            
    }
    
    /**
     * Helper class to parse from a TransientArtifact into a Artifact.
     * @param artifact Transient Artifact.
     * @return Artifact.
     */
    public static Artifact transientArtifactToArtifact(TransientArtifact artifact) {
        if (artifact == null)
            return null;
        
        return new Artifact(
                artifact.getId(),
                artifact.getName(),
                artifact.getContentType(),
                Base64.decodeBase64(artifact.getContent()),
                artifact.getSharedInformation(),
                artifact.getCreationDate(),
                artifact.getCommitDate()
        );
    }
}