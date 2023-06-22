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

package org.neotropic.kuwaiba.northbound.ws.todeserialize;

import java.io.Serializable;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This is a representation in a code friendly flavor of a complex query.
 * Pay attention that the JOIN will be treated as small queries, ie.:<br><br>
 * <code>SELECT * FROM building b. vendor v WHERE b.name LIKE '%my_building%' INNER JOIN vendor ON v.vendor_id=b.id and v.name ='Nokia'</code><br>
 * There will be two queries: One (the master) having the condition "name LIKE '%my_building%'" and a "subquery"
 * with the join information.<br><br>
 * Don't confuse with the representation of entitity.queries.Query (RemoteQuery). That one is used
 * to <b>store</b> the queries and eventually to send them to the client to be reconstructed, This
 * one is used only for query execution purposes
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TransientQuery implements Serializable {
    
    /**
     * OR logical connector
     */
    public static final int CONNECTOR_OR = 0;
    /**
     * AND logical connector
     */
    public static final int CONNECTOR_AND = 1;

    /**
     * The following constants must be synchronized with those at client side
     */

    /**
     * Equal comparison
     */
    public static final int EQUAL = 0;
    /**
     * Less than comparison
     */
    public static final int LESS_THAN = 1;
    /**
     * Less than or equal to comparison
     */
    public static final int EQUAL_OR_LESS_THAN = 2;
    /**
     * Greater than comparison
     */
    public static final int GREATER_THAN = 3;
    /**
     * Less than or equal to comparison
     */
    public static final int EQUAL_OR_GREATER_THAN = 4;
    /**
     * Between comparison (used for numbers and dates)
     */
    public static final int BETWEEN = 5;
    /**
     * Like comparison (used for strings)
     */
    public static final int LIKE = 6;

    /**
     * Instances of this class will be searched
     */
    private String className;

    private int logicalConnector;
    /**
     * Attributes that will be used to build the criteria
     */
    private ArrayList<String> attributeNames;
    /**
     * Attributes to be shown in the final result (read this as "SELECT visibleAttributesNames" FROM...).
     * If this is the master query(see @isJoin) and the it's empty or null, all attributes will be shown; if
     * this is a join, none will be shown
     */
    private ArrayList<String> visibleAttributeNames;

    /**
     * Attributes values to be shown in the final result (read this as "SELECT visibleAttributesNames" FROM...).
     * If this is the master query(see @isJoin) and the it's empty or null, all attributes will be shown; if
     * this is a join, none will be shown
     */
    private ArrayList<String> attributeValues;
    /**
     * Equal to, less than, like, etc
     */
    private ArrayList<Integer> conditions;
    /**
     * As stated before, joins will be treated like simple subqueries
     */
    private ArrayList<TransientQuery> joins;
    /**
     * As stated before, parent will be treated like simple subqueries
     */
    private TransientQuery parent;
    /**
     * Indicates if the current LocalQuery object is a join or the master query. It will
     * be used later to determine if
     */
    private boolean join = false;

    /**
     * Results limit. Not used if @isJoin is true. Default value is 10
     */
    private int limit = 10;
    /**
     * Current result page. If its value is less than 1, means that no pagination should be used
     */
    private int page = 1;

    public TransientQuery() {
    }

    public TransientQuery(String className, int logicalConnector, ArrayList<String> attributeNames, ArrayList<String> visibleAttributeNames, ArrayList<String> attributeValues, ArrayList<Integer> conditions, ArrayList<TransientQuery> joins) {
        this.className = className;
        this.logicalConnector = logicalConnector;
        this.attributeNames = attributeNames;
        this.visibleAttributeNames = visibleAttributeNames;
        this.attributeValues = attributeValues;
        this.conditions = conditions;
        this.joins = joins;
    }

    public ArrayList<String> getAttributeNames() {
        return attributeNames;
    }

    public ArrayList<String> getAttributeValues() {
        return attributeValues;
    }

    public ArrayList<Integer> getConditions() {
        return conditions;
    }

    public boolean isJoin() {
        return join;
    }

    public ArrayList<TransientQuery> getJoins() {
        return joins;
    }

    public int getLogicalConnector() {
        return logicalConnector;
    }

    public ArrayList<String> getVisibleAttributeNames() {
        return visibleAttributeNames;
    }

    public String getClassName() {
        return className;
    }

    public int getLimit() {
        return limit;
    }
    public int getPage(){
        return page;
    }

    public void setAttributeNames(ArrayList<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setConditions(ArrayList<Integer> conditions) {
        this.conditions = conditions;
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public void setJoins(ArrayList<TransientQuery> joins) {
        this.joins = joins;
    }

    public TransientQuery getParent() {
        return parent;
    }

    public void setParent(TransientQuery parent) {
        this.parent = parent;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setLogicalConnector(int logicalConnector) {
        this.logicalConnector = logicalConnector;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setVisibleAttributeNames(ArrayList<String> visibleAttributeNames) {
        this.visibleAttributeNames = visibleAttributeNames;
    }

    public void setAttributeValues(ArrayList<String> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
