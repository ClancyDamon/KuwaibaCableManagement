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

package org.neotropic.kuwaiba.northbound.ws.model.application;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.neotropic.kuwaiba.core.apis.persistence.application.CompactQuery;

/**
 * Wrapper for entity.queries.ExtendedQuery class. Don't confuse this with TransientQuery, which is used
 * only for query execution purposes (this one is used as a wrapper for a query which is actually
 * stored at the database)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteQuery extends RemoteQueryLight{
    private byte[] content;

    //No-arg constructor required
    public RemoteQuery() {    }

    public RemoteQuery(CompactQuery query) {
        super (query.getId(), query.getName(),query.getDescription(),query.getIsPublic());
        this.content = query.getContent();
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
