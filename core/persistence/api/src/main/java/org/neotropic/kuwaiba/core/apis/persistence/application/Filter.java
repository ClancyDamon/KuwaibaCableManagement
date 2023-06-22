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

package org.neotropic.kuwaiba.core.apis.persistence.application;

import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;

/**
 * A filter is a query that, given a set of input parameters, returns a list of inventory assets. Queries
 * are usually complex and can not be done using the Query Manager alone. These queries can be embedded 
 * in multiple modules, so they can be used to filter what information is displayed. A typical use case is the 
 * navigation module: Filters applicable per class are created and, when a user selects an object, it is possible to
 * expand de navigation tree showing its direct children or apply a filter to display only a predefined set (for example, 
 * in a network device show only the ports and skip cards and slots). A {@link FilterDefinition} instance contains 
 * the metadata of a Filter (name, if it's enabled, the corresponding script), while a filter instance can 
 * execute code. Filters are Groovy scripts containing subclasses of Filter. These classes are compiled 
 * on-the-fly and the instances cached to be executed on demand.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class Filter {
    /**
     * Reference to the Metadata Entity Manager.
     */
    protected MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    protected ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    protected BusinessEntityManager bem;
    /**
     * Reference to the Connection Manager.
     */
    protected ConnectionManager cm;

    /**
     * Default mandatory constructor.
     * @param mem Reference to the Metadata Entity Manager.
     * @param aem Reference to the Application Entity Manager.
     * @param bem Reference to the Business Entity Manager.
     * @param cm  Reference to the Connection Manager. Useful to access directly the database, bypassing the Persistence API.
     */
    public Filter(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, ConnectionManager cm) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.cm = cm;
    }
    
    /**
     * This method should be implemented by all filter definition scripts (which must define a subclass of Filter), 
     * and contains the logic to evaluate the condition.
     * @param objectId The id of the object from which the filter is ran.
     * @param objectClass The class of the object from which the filter is ran.
     * @param parameters Extra parameters useful for the filter to complete its task. For example, a consumer would like to retrieve all the ports in a 
     * network device recursively, but only those containing a given string. That could be provided as a parameter. The parameters are handed as String and 
     * it's expected that the filter script perform the respective validations. This might be null or an empty hash, depending on the filter.
     * @param page -1 to avoid pagination, the page of the results.
     * @param limit -1 to no limit, the  limit of results per page.
     * @return The list of objects product of running the filter. 
     * @throws InvalidArgumentException If the script had a problem (managed or not) in its execution.
     */
    public abstract List<BusinessObjectLight> run(String objectId, String objectClass, 
            HashMap<String, String> parameters, int page, int limit) throws InvalidArgumentException;
}
