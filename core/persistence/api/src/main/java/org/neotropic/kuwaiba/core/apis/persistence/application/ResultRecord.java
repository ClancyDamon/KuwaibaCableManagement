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

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import java.util.List;

/**
 * Represents a single record resulting from a query. It basically contains the very basic
 * information about an object, as well extra columns based on the "visibleAttributes" argument
 * provided when the query was executed
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ResultRecord extends BusinessObjectLight {
    private List<String> extraColumns;

    public ResultRecord(String className, String id, String name) {
        super(className, id, name);
    }
    
    public ResultRecord(String className, String id, String name, List<String> extraColumns) {
        super(className, id, name);
        this.extraColumns = extraColumns;
    }

    public List<String> getExtraColumns() {
        return extraColumns;
    }

    public void setExtraColumns(List<String> extraColumns) {
        this.extraColumns = extraColumns;
    }
}
