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
package org.neotropic.kuwaiba.northbound.rest.todeserialize;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;

/**
 * Every process activity has at least one artifact. An artifact is the result of 
 * executing an activity. Most of the times, an artifact is simply a form filled in by a user
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Setter
@Getter
public class TransientArtifact implements Serializable {
    public static final String SHARED_KEY_IDLE = "__idle__"; //NOI18N
    public static final String SHARED_KEY_INTERRUPTED = "__interrupted__"; //NOI18N
    /**
     * modified date of an not idle activity
     */
    public static final String SHARED_KEY_IDLE_MODIFIED = "__idleModified__"; //NOI18N
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
     * The content of the artifact as string Base64, from an XML document that contains the actual artifact. It may be a form already filled in, or an XML with CDATA section 
     * containing a binary file
     */
    private String content;
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

    public TransientArtifact() { }

    public TransientArtifact(String id, String name, String contentType, String content, List<StringPair> sharedInformation, long creationDate, long commitDate) {
        this.id = id;
        this.name = name;
        this.contentType = contentType;
        this.content = content;
        this.sharedInformation = sharedInformation;
        this.creationDate = creationDate;
        this.commitDate = commitDate;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final Artifact other = (Artifact) obj;
        if (!Objects.equals(this.id, other.getId())) {
            return false;
        }
        return true;
    }
}