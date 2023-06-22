/*
 *  Copyright 2010-2022 Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        https://apache.org/licenses/LICENSE-2.0.txt
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.neotropic.kuwaiba.northbound.ws.model.application;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Simple version of RemoteQuery
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteQueryLight implements Serializable {

    /**
     * Object id
     */
    protected long id;
    private String name;
    private String description;
    private boolean isPublic;

    //No-arg constructor required
    public RemoteQueryLight() {    }


    public RemoteQueryLight(long id, String name, String description, boolean isPublic) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }

    public RemoteQueryLight(Query query) {
        this.id = query.getId();
        this.description = query.getDescription();
        this.isPublic = (query.getOwner() == null);
    }

    public long getId() {
        return id;
    }

    public void setOid(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
