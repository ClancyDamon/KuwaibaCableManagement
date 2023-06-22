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
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;

/**
 * Wrapper of {@link org.neotropic.kuwaiba.core.apis.persistence.application.process.Artifact}. Every process activity has at least one artifact. An artifact is the result of 
 * executing an activity. Most of the times, an artifact is simply a form filled in by a user
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteArtifact implements Serializable {
    /**
     * Artifact id
     */
    private String id;
    /**
     * The name of the artifact
     */
    private String name;
    /**
     * What kind of artifact is it. This value helps a process renderer to know how to interpret the content
     */
    private String contentType;
    /**
     * An XML document that contains the actual artifact. It may be a form already filled in, or an XML with CDATA section 
     * containing a binary file
     */
    private byte[] content;
    /**
     * In the current process. Information which can be shared between an activity 
     * instance and to other activity instances or the process instance.
     */
    private List<StringPair> sharedInformation;
    /**
     * The artifact creation date
     */
    private long creationDate = 0;
    /**
     * The artifact commit date
     */
    private long commitDate = 0;

    public RemoteArtifact() { }
    
    public RemoteArtifact(String id, String name, String contentType, byte[] content, List<StringPair> sharedInformation, long creationDate, long commitDate) {
        this.name = name;
        this.contentType = contentType;
        this.content = content;
        this.id = id;
        this.sharedInformation = sharedInformation;
        this.creationDate = creationDate;
        this.commitDate = commitDate;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public List<StringPair> getSharedInformation() {
        return sharedInformation;
    }
    
    public void setSharedInformation(List<StringPair> sharedInformation) {
        this.sharedInformation = sharedInformation;
    }
    
    public long getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
    
    public long getCommitDate() {
        return commitDate;
    }
    
    public void setCommitDate(long commitDate) {
        this.commitDate = commitDate;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.id);
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
        final RemoteArtifact other = (RemoteArtifact) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}