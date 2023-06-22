/**
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

package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.io.Serializable;

/**
 * The store-friendly version of an ExtendedQuery. Its structure is not meant to be executed, but
 * to be transported and stored
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CompactQuery implements Serializable{

    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_QUERYNAME = "name"; //NOI18N
    public static final String PROPERTY_DESCRIPTION = "description"; //NOI18N
    public static final String PROPERTY_QUERYSTRUCTURE = "querystructure"; //NOI18N
    public static final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N
    public static final String PROPERTY_IS_PUBLIC = "ispublic"; //NOI18N
    /**
     * Query id
     */
    protected Long id;
    /**
     * Query description
     */
    protected String name;
    /**
     * Query description
     */
    protected String description;
    /**
     * Query owner
     */
    protected Long ownerId;
    
    /**
     * Is a public query
     */
    protected Boolean isPublic;
    /**
     * Query body
     */
    protected byte[] content;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] body) {
        this.content = body;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
 
}
