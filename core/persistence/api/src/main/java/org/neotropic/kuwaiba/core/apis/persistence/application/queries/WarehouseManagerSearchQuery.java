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

package org.neotropic.kuwaiba.core.apis.persistence.application.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
//import org.neo4j.graphdb.GraphDatabaseService;
//import org.neo4j.graphdb.Transaction;

/**
 * Temporary class that implements the search in the Warehouse module that shows 
 * suggestions in the search text field
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class WarehouseManagerSearchQuery {
    public List<Properties> execute(Properties properties) {
        List<Properties> res = new ArrayList<>();
//        GraphDatabaseService graphDb = (GraphDatabaseService)PersistenceService.getInstance().getConnectionManager().getConnectionHandler();
//        try (Transaction tc = graphDb.beginTx()) {
//            //This cypher query searches for all inventory objects instances of any class under GenericPhysicalLocaltion or GenericBox, whose 
//            //name contains the searched term
//            String cypherQuery = "MATCH (obj:inventoryObject)-[:INSTANCE_OF]->(aClass:classes)-[:EXTENDS*]->(aSuperClass:classes) "
//                    + "WHERE obj.name =~ '.*" + properties.get("searchTerm") + ".*' AND (aSuperClass.name='GenericPhysicalLocation' "
//                    + "OR aSuperClass.name = 'GenericBox') RETURN obj.name AS objName, aClass.name AS className LIMIT 5";
//
//            cypherQuery = "MATCH (lti:listTypeItems)-[:INSTANCE_OF]->(aClass:classes) "
//                    + "WHERE aClass.name = 'EquipmentModel' AND lti.name =~ '.*" + properties.get("searchTerm") + ".*' "
//                    + "RETURN lti.name AS ltiName, aClass.name AS className LIMIT 5";
//        }
        return res;
    }
}
