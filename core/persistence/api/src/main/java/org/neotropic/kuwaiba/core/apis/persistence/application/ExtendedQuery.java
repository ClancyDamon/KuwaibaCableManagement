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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complex query to be executed. This is the code friendly version of the query.
 * The store-friendly can is @CompactQuery.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ExtendedQuery implements Serializable{
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
    private List<String> attributeNames;
    /**
     * Attributes to be shown in the final result (read this as "SELECT visibleAttributesNames" FROM...).
     * If this is the master query(see @join) and the it's empty or null, all attributes will be shown; if
     * this is a join, none will be shown
     */
    private List<String> visibleAttributeNames;

    private List<String> attributeValues;
    /**
     * Equal to, less than, like, etc
     */
    private List<Integer> conditions;
    /**
     * As stated before, joins will be treated like simple subqueries
     */
    private List<ExtendedQuery> joins;
    /*
        Used to save the reference to the a-side of the join
    */
    private ExtendedQuery queryJoin;
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

    public ExtendedQuery() {
        this.attributeNames = new ArrayList<>();
        this.visibleAttributeNames = new ArrayList<>();
        this.attributeValues = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.joins = new ArrayList<>();
    }

    public ExtendedQuery(String className, int logicalConnector, List<String> attributeNames,
            List<String> visibleAttributeNames, List<String> attributeValues, List<Integer> conditions, List<ExtendedQuery> joins, int page, int limit) {
        this.className = className;
        this.logicalConnector = logicalConnector;
        this.attributeNames = attributeNames;
        this.visibleAttributeNames = visibleAttributeNames;
        this.attributeValues = attributeValues;
        this.conditions = conditions;
        this.joins = joins;
        this.page = page;
        this.limit = limit;
    }
    
     public ExtendedQuery(String className, int logicalConnector,
            boolean isJoin, int limit, int page) {
        this();
        this.className = className;
        this.logicalConnector = logicalConnector;
        this.join = isJoin;
        this.limit = limit;
        this.page = page;
    }
 
    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public List<String> getAttributeValues() {
        return attributeValues;
    }

    public List<Integer> getConditions() {
        return conditions;
    }

    public boolean isJoin() {
        return join;
    }

    public List<ExtendedQuery> getJoins() {
        return joins;
    }

    public int getLogicalConnector() {
        return logicalConnector;
    }

    public List<String> getVisibleAttributeNames() {
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

    public void setClassName(String className) {
        this.className = className;
    }

    public void setLogicalConnector(int logicalConnector) {
        this.logicalConnector = logicalConnector;
    }

    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public void setVisibleAttributeNames(List<String> visibleAttributeNames) {
        this.visibleAttributeNames = visibleAttributeNames;
    }

    public void setAttributeValues(List<String> attributeValues) {
        this.attributeValues = attributeValues;
    }

    public void setConditions(List<Integer> conditions) {
        this.conditions = conditions;
    }
    
    public void addAttributeName(String attributeName) {
        this.attributeNames.add(attributeName);
    }

    public void addVisibleAttributeName(String visibleAttributeName) {
        this.visibleAttributeNames.add(visibleAttributeName);
    }

    public void addAttributeValue(String attributeValue) {
        this.attributeValues.add(attributeValue);
    }

    public void addConditions(Integer condition) {
        this.conditions.add(condition);
    }
    
    public void addJoin(ExtendedQuery join) {
        this.joins.add(join);
    }

    public void setJoins(List<ExtendedQuery> joins) {
        this.joins = joins;
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ExtendedQuery getQueryJoin() {
        return queryJoin;
    }

    public void setQueryJoin(ExtendedQuery queryJoin) {
        this.queryJoin = queryJoin;
    }

    public void removeAttribute(String name) {
        int index = getAttributeNames().indexOf(name);
        if (index >= 0) {
            attributeNames.remove(index);
            attributeValues.remove(index);
            conditions.remove(index);
            joins.remove(index);
       }
    }
    
    public void editAttribute(String name, String value, Integer condition) {
        int index = getAttributeNames().indexOf(name);
        if (index >= 0) {
            attributeValues.set(index, value);
            conditions.set(index, condition);
       }
    }
     
}
