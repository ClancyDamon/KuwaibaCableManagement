/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.io.Serializable;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper of {@link org.neotropic.kuwaiba.core.apis.persistence.application.process.ArtifactDefinition}. Represents an artifact associated to an activity. An artifact is the product of the execution of an activity. 
 * Most of the times, it will be a form filled in by the user.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteArtifactDefinition implements Serializable {
    /**
     * Artifact id
     */
    private String id;
    /**
     * Artifact name
     */
    private String name;
    /**
     * Artifact description
     */
    private String description;
    /**
     * The version of the artifact, expressed as a three numeric sections separated by a dot (e.g. 1.3.1)
     */
    private String version;
    /**
     * Artifact type. See ArtifactDefinition.TYPE_* for valid values
     */
    private int type;
    /**
     * The actual definition. It's an XML document 
     */
    private byte[] definition;
    /**
     * Script to verify preconditions to execute the Artifact
     */
    private byte[] preconditionsScript;
    /**
     * Script to verify postconditions to execute the Artifact
     */
    private byte[] postconditionsScript;  
    /**
     * Defines if the artifact can be printed
     */
    private Boolean printable = false;
    /**
     * Is the template used to print the artifact
     */
    private String printableTemplate;
    /**
     * List of relative path to external scripts separated by space
     */
    private String externalScripts;

    public RemoteArtifactDefinition(String id, String name, String description, String version, int type, byte[] definition, byte[] preconditionsScript, byte[] postconditionsScript, Boolean printable, String printableTemplate, String externalScripts) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.type = type;
        this.definition = definition;
        this.preconditionsScript = preconditionsScript;
        this.postconditionsScript = postconditionsScript;
        this.printable = printable;
        this.printableTemplate = printableTemplate;
        this.externalScripts = externalScripts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public byte[] getDefinition() {
        return definition;
    }

    public void setDefinition(byte[] definition) {
        this.definition = definition;
    }
    
    public void setPreconditionsScript(byte[] preconditionsScript) {
        this.preconditionsScript = preconditionsScript;
    }
    
    public byte[] getPreconditionsScript() {
        return preconditionsScript;
    }
    
    public void setPostconditionsScript(byte[] postconditionsScript) {
        this.postconditionsScript = postconditionsScript;
    }
    
    public byte[] getPostconditionsScript() {
        return postconditionsScript;
    }
    
    public Boolean isPrintable() {
        return printable;
    }
    
    public void setPrintable(boolean printable) {
        this.printable = printable;
    }
    
    public String getPrintableTemplate() {
        return printableTemplate;
    }
    
    public void setPrintableTemplate(String printableTemplate) {
        this.printableTemplate = printableTemplate;
    }
    
    public String getExternalScripts() {
        return externalScripts;
    }
    
    public void setExternalScripts(String externalScripts) {
        this.externalScripts = externalScripts;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteArtifactDefinition other = (RemoteArtifactDefinition) obj;
        return this.id != null && this.id.equals(other.id);
    }
    
}
