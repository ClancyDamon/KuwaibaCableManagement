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

package org.neotropic.kuwaiba.core.persistence.reference.neo4j;

import org.neotropic.kuwaiba.core.apis.persistence.application.ExtendedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultRecord;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;

/**
 * Creates cypher Query
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class CypherQueryBuilder {
    public static final String INSTANCE = "instance"; //NOI18N
    public static final String PARENT = "parent"; //NOI18N
    public static final String LISTTYPE = "listType_"; //NOI18N
    /**
     * nodes selected for query
     */
    public Map<String, Node> classNodes = new HashMap<>();
    /**
     * attributes selected
     */
    public Map<String, List<String>> visibleAttributes = new HashMap<>();
    /**
     * match statements
     */
    private String match = "";
    /**
     * where statements
     */
    private String where = "";
    /**
     * return statements
     */
    private String _return = "";
    /**
     * Cypher parser to execute the queries
     */
    private CypherParser cp;
    /**
     * result list
     */
    public List<ResultRecord> resultList = new ArrayList<>();

    /**
     * read the parent and his joins
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readParent(String listTypeName, String listTypeName2, ExtendedQuery query) {
        Node classNode = classNodes.get(query.getClassName());

        match = match.concat(cp.createParentMatch());
        where = where.concat(cp.createParentRelation(query.getClassName()));
        _return = _return.concat(", ".concat(PARENT));

        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                    if(query.getAttributeValues().get(i) != null){
                        where = where.concat(cp.createParentWhere(query.getConditions().get(i), listTypeName,
                                                            query.getAttributeNames().get(i),
                                                            query.getAttributeValues().get(i),
                                                            query.getAttributeNames().get(i).equals(Constants.PROPERTY_ID) ? 
                                                                    Long.toString(classNode.getId()) : Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                    }
                    else{
                        readJoins(query.getAttributeNames().get(i)+"_P", listTypeName, query.getJoins().get(i));
                    }
            }//end for
        }//end if
    }

    /**
     * read the sub queries (joins)
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readJoins(String listTypeName, String listTypeName2, ExtendedQuery query) {
        if(query == null)
            where = where.concat(cp.createNoneWhere(listTypeName));
        else{
            Node classNode = classNodes.get(query.getClassName());
            match = match.concat(cp.createListypeMatch(listTypeName, listTypeName2));
            where = where.concat(cp.createJoinRelation(listTypeName));
            _return = _return.concat(", ").concat(LISTTYPE).concat(listTypeName);

            if(query.getAttributeNames() != null){
                for(int i = 0; i<query.getAttributeNames().size(); i++){
                        if(query.getAttributeValues().get(i) != null){
                            where = where.concat(cp.createJoinWhere(query.getConditions().get(i), listTypeName,
                                                                query.getAttributeNames().get(i),
                                                                query.getAttributeValues().get(i),
                                                                query.getAttributeNames().get(i).equals(Constants.PROPERTY_ID) ? 
                                                                        Long.toString(classNode.getId()) : Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                                ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                        }
                        else{
                            readJoins(query.getAttributeNames().get(i), listTypeName, query.getJoins().get(i));
                        }
                }//end for
            }//end if
        }
    }

    /**
     * reads the sub sub queries
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readJoinQuery(String listTypeName, String listTypeName2, ExtendedQuery query) {
        Node classNode = classNodes.get(query.getClassName());
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) != null){
                    where = where.concat(cp.createJoinWhere(query.getConditions().get(i), listTypeName,
                                            query.getAttributeNames().get(i),
                                            query.getAttributeValues().get(i),
                                            query.getAttributeNames().get(i).equals(Constants.PROPERTY_ID) ? Long.toString(classNode.getId()) : Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                }
                else{
                   readJoins(query.getAttributeNames().get(i), listTypeName, query);
                }
            }
        }
    }

    /**
     * Reads the query main recursively
     * @param query
     */
    public void readQuery(ExtendedQuery query) {
        _return = cp.createReturn();
        Node classNode = classNodes.get(query.getClassName());
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) != null){
                    if(query.getAttributeValues().get(i) != null){ 
                        where = where.concat(cp.createWhere(query.getConditions().get(i),
                                                            query.getAttributeNames().get(i),
                                                            query.getAttributeValues().get(i),
                                                            query.getAttributeNames().get(i).equals(Constants.PROPERTY_ID) ? 
                                                                    Long.toString(classNode.getId()) : Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                    }
                }
               else{
                    if( query.getAttributeNames().get(i).equalsIgnoreCase(PARENT))
                        readParent(query.getAttributeNames().get(i), "", query.getJoins().get(i));
                    else
                        readJoins(query.getAttributeNames().get(i), "", query.getJoins().get(i));
                }
            }//end for
        }//end if
    }

    /**
     * Reads the visible attributes for the main query and its joins
     * @param query
     */
    public void readVissibleAttributes(ExtendedQuery query){
        if(query.getVisibleAttributeNames() != null)
             visibleAttributes.put(INSTANCE, query.getVisibleAttributeNames());
        else
            visibleAttributes.put(INSTANCE, new ArrayList<>());
        if(query.getAttributeNames() != null)
        {
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) == null){
                    if(query.getAttributeNames().get(i).equalsIgnoreCase(PARENT))
                        readVissibleAttributeParent(query.getJoins().get(i));
                    else{
                        if(query.getJoins().get(i) != null)
                            readVissibleAttributeJoins(query.getAttributeNames().get(i), query.getJoins().get(i));
                    }
                }
            }//end for
        }
    }

    /**
     * Reads the visible attributes for the parent and its joins
     * @param query
     */
    public void readVissibleAttributeParent(ExtendedQuery query){
        if(query.getVisibleAttributeNames() != null)
            visibleAttributes.put(PARENT, query.getVisibleAttributeNames());
        else
            visibleAttributes.put(PARENT, new ArrayList<String>() {{ add("name");}});
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getJoins().get(i) != null)
                    readVissibleAttributeJoins(query.getAttributeNames().get(i)+"_P", query.getJoins().get(i));
            }
        }//listTypeName, listTypeName2;
    }

    /**
     * 
     * @param listTypeName
     * @param query
     */
    public void readVissibleAttributeJoins(String listTypeName, ExtendedQuery query){
        if(query.getVisibleAttributeNames() != null)
             visibleAttributes.put(LISTTYPE.concat(listTypeName), query.getVisibleAttributeNames());
        else
            visibleAttributes.put(LISTTYPE.concat(listTypeName), new ArrayList<String>() {{ add("name");}});
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getJoins().get(i) != null)
                    readVissibleAttributeJoins(query.getAttributeNames().get(i), query.getJoins().get(i));
            }
        }
    }

    /**
     * Creates the query
     * @param query 
     * @throws InvalidArgumentException If any object does not have uuid
     */
    public void createQuery(ExtendedQuery query) throws InvalidArgumentException {
        cp = new CypherParser();
        Node classNode = classNodes.get(query.getClassName());
        try(Transaction tx = classNode.getGraphDatabase().beginTx()) {
            boolean isAbstract = (Boolean) classNode.getProperty(Constants.PROPERTY_ABSTRACT);

            String cypherQuery = cp.createInstanceMatch(isAbstract);
            readQuery(query);
            if(!match.isEmpty())
                cypherQuery = cypherQuery.concat(match);
            
            cypherQuery = cypherQuery.concat(cp.createInstanceWhere(query.getClassName(),isAbstract));
            if(!where.isEmpty())
                cypherQuery = cypherQuery.concat(" AND ".concat(where.substring(0, where.length() - 4)));
            
            cypherQuery = cypherQuery.concat(" RETURN ".concat(_return));

            cypherQuery = cypherQuery.concat(" ORDER BY instance.name ASC");

            if(query.getPage() > 0){
                int min = ((query.getPage() - 1) * query.getLimit()) + (query.getPage() - 1);
                int max = min + (query.getLimit());
                cypherQuery = cypherQuery.concat(" SKIP " + min + " LIMIT " + max);//NOI18N
            }

            readVissibleAttributes(query);
            executeQuery(classNode, cypherQuery);
        }
    }

    /**
     * Executes the query
     * @param classNode
     * @param cypherQuery
     * @throws InvalidArgumentException If any object does not have uuid
     */
    public void executeQuery(Node classNode, String cypherQuery) throws InvalidArgumentException {
        Result result = classNode.getGraphDatabase().execute(cypherQuery, new HashMap<>());
        readResult(result);
    }

    /**
     * Read the results
     * @param queryResult
     * @throws InvalidArgumentException If any object does not have uuid
     */
    public void readResult(Result queryResult) throws InvalidArgumentException {
        List<ResultRecord> onlyResults =  new ArrayList<>();
        ResultRecord rr;
        List<String> vissibleAttibutesTitles = new ArrayList<>();

        String[] split = _return.split(", ");
        for(int g = 0; g < split.length; g++){
            for(String va: (List<String>)visibleAttributes.get(split[g]))
                vissibleAttibutesTitles.add(va);
        }

        while(queryResult.hasNext()){ // Iterates by row
            Map<String, Object> column = queryResult.next();
            List<String> extraColumns = new ArrayList<>();
            //create the class
            Node instanceNode = (Node)column.get(split[0]);
            String instanceNodeUuid = instanceNode.hasProperty(Constants.PROPERTY_UUID) ? (String) instanceNode.getProperty(Constants.PROPERTY_UUID) : null;
            if (instanceNodeUuid == null)
                throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", instanceNode.getId()));
            
            rr = new ResultRecord(Util.getClassName(instanceNode), instanceNodeUuid, Util.getAttributeFromNode(instanceNode, Constants.PROPERTY_NAME));
            //iterates by column
            for(int lu =  0; lu < split.length; lu++){
                for(String va: visibleAttributes.get(split[lu])){
                    Node node = (Node)column.get(split[lu]);
                    if(va.equals(Constants.PROPERTY_ID))
                        extraColumns.add(Long.toString(node.getId()));
                    else
                        extraColumns.add(Util.getAttributeFromNode(node, va));
                }
            }
            rr.setExtraColumns(extraColumns);
            onlyResults.add(rr);
        }
        ResultRecord resltRcrdHeader = new ResultRecord(null, null, null);
        resltRcrdHeader.setExtraColumns(vissibleAttibutesTitles);
        resultList.add(resltRcrdHeader);

        if(onlyResults.size() > 0) {
            for(ResultRecord orr: onlyResults)
                resultList.add(orr);
        }
    }

    public List<ResultRecord> getResultList() {
        return resultList;
    }

    public void setClassNodes(Map<String, Node> classNodes) {
        this.classNodes = classNodes;
    }
}
