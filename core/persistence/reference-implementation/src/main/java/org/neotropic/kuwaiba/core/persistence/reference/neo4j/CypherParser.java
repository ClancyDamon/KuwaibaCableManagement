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
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Cypher parser
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class CypherParser {   
    /**
     * creates the match when is only joins
     * @param isAbstract
     * @return
     */
    public String createInstanceMatch(boolean isAbstract) {
        if(isAbstract)
            return "MATCH (abstractClassmetadata:classes)<-[:" + RelTypes.EXTENDS + "*]-(classmetadata:classes)<-[:" + RelTypes.INSTANCE_OF + "]-(instance)"; /*WHERE abstractClassmetadata.name = \"" + className + "\"";*/
        else
            return "MATCH (classmetadata:classes)<-[:" + RelTypes.INSTANCE_OF + "]-(instance)"; /*WHERE classmetadata.name = \"" + className + "\"";*/
    }
    
    public String createInstanceWhere(String className, boolean isAbstract) {
        if(isAbstract)
            return " WHERE abstractClassmetadata.name = \"" + className + "\"";
        else
            return " WHERE classmetadata.name = \"" + className + "\"";
    }

     /**
     * Adds the match when the query has parent
     * @return
     */
    public String createParentMatch(){
        return ", (instance)-[:" + RelTypes.CHILD_OF + "*]->(parent)-[:" + RelTypes.INSTANCE_OF + "]-(parentclassmetadata)";//NOI18N
    }
    /**
     * add every listType into de the match a listType for every join
     * @param listTypeName
     * @param listTypeName2
     * @return
     */
    public String createListypeMatch(String listTypeName, String listTypeName2){
        if(listTypeName2.isEmpty())
            return ", (instance)-[r_"+listTypeName+":" + RelTypes.RELATED_TO + "]->(listType_" + listTypeName+")";
        if(listTypeName2.equalsIgnoreCase("parent"))
            return ", (" + listTypeName2 + ")-[r_" + listTypeName + ":" + RelTypes.RELATED_TO + "]->(listType_" + listTypeName+")";
        else
            return ", (listType_" + listTypeName2 + ")-[r_" + listTypeName + ":" + RelTypes.RELATED_TO + "]->(listType_" + listTypeName + ")";
    }
    
    /**
     * if has no relationship
     * @param listTypeName
     * @return 
     */
    public String createNoneWhere(String listTypeName){
        return " NOT (instance)-[:RELATED_TO {name:\""+listTypeName+"\"}]->()    ";
    }
    
    /**
     * Add this to the match for the parent and the parent joins
     * @param listTypeName
     * @param listTypeName2
     * @return 
     */
    public String createListypeParentMatch(String listTypeName, String listTypeName2) {
        if(listTypeName2.isEmpty())
            return ", (parent)-[r_"+listTypeName+":" + RelTypes.RELATED_TO+"]->listType_" + listTypeName;
        else
            return ", listType_" + listTypeName2 + "-[r_" + listTypeName+":" + RelTypes.RELATED_TO + "]->listType_" + listTypeName;
    }

    /**
     * Simple where without joins or parent in the query
     * @param condition
     * @param attributeName
     * @param attributeValue
     * @param attibuteType
     * @return simple where statement
     */
    public String createWhere(int condition, String attributeName, String attributeValue, String attibuteType){
        String operator = getOperator(condition);

        if(attributeName.equals("id"))
            return "instance._uuid = '"+ attributeValue +"'";
        else{
            if (attibuteType.equals("Date") || attibuteType.equals("TimeStamp")){
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);//NOI18N
                try {
                    attributeValue = Long.toString(dateFormat.parse(attributeValue).getTime());
                } catch (ParseException ex) {
                    System.out.println("Wrong date format. It is expected " + Constants.DATE_FORMAT);//NOI18N
                }
            }
            if(attibuteType.equals("String")){
                if(condition == ExtendedQuery.LIKE) {
                    attributeValue = " TOLOWER(\"".concat(attributeValue).concat("\")");
                    return "TOLOWER(instance.".concat(attributeName).concat(")").concat(operator).concat(attributeValue);
                } else
                    attributeValue = "\"".concat(attributeValue).concat("\"");
            }
                
            return "instance.".concat(attributeName).concat(operator).concat(attributeValue);
        }
    }

    /**
     * every time a list type for a join or a parent or a parent join is added
     * there must be a relation to identify every list type.
     * @param joinName
     * @return
     */
    public String createJoinRelation(String joinName){
        if(joinName.contains("_P"))
            return "r_" + joinName+".name = \"" + joinName.substring(0, joinName.length() - 2) + "\" AND ";
        else
            return "r_" + joinName+".name = \"" + joinName+"\" AND ";
    }

    /**
     * Creates a where statement for joins
     * @param condition
     * @param joinName
     * @param attributeName
     * @param attributeValue
     * @param attibuteType
     * @return where join statement
     */
    public String createJoinWhere(int condition, String joinName, String attributeName, String attributeValue, String attibuteType){
        String operator = getOperator(condition);

        if (attributeName.equals("id")) //It is a compact view-base comparison
            return " listType_" + joinName + "._uuid = '" + attributeValue +"'";
        
        else {
            if (!attibuteType.equals("String") && condition == ExtendedQuery.EQUAL)
                operator = operator.substring(0, operator.length() - 1);
            if (attibuteType.equals("Date")){
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);//NOI18N
                try {
                    attributeValue = Long.toString(dateFormat.parse(attributeValue).getTime());
                } catch (ParseException ex) {
                    System.out.println("Wrong date format. It is expected " + Constants.DATE_FORMAT);//NOI18N
                }
            }
            if(attibuteType.equals("String")) {
                if(condition == ExtendedQuery.LIKE) {
                    attributeValue = " TOLOWER(\"".concat(attributeValue).concat("\")");
                    return "TOLOWER(listType_" + joinName + "." + attributeName + ")" + operator + attributeValue;
                } else
                    attributeValue = "\"".concat(attributeValue).concat("\"");
            }
        }
        return "listType_" + joinName + "." + attributeName + operator + attributeValue;
    }

    /**
     * every time a list type for a join or a parent or a parent join is added
     * there must be a relation to identify every list type.
     * @param joinName
     * @return 
     */
    public String createParentRelation(String joinName){
        return "parentclassmetadata.name=\"" + joinName + "\" AND ";
    }

    /**
     * Creates the where statement if there is a parent
     * @param condition
     * @param joinName
     * @param attributeName
     * @param attributeValue
     * @param attibuteType
     * @return parent where statement
     */
    public String createParentWhere(int condition, String joinName, String attributeName, String attributeValue, String attibuteType){
        String operator = getOperator(condition);

        if (attributeName.equals("id")) {//is small view
            return joinName + "._uuid = '" + attributeValue + "'"; //NOI18N
        }
        else{
            if (!attibuteType.equals("String") && condition == ExtendedQuery.EQUAL)
                operator = operator.substring(0, operator.length() - 1);
            if (attibuteType.equals("Date")){
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT); //NOI18N
                try {
                    attributeValue = Long.toString(dateFormat.parse(attributeValue).getTime());
                } catch (ParseException ex) {
                    System.out.println("Wrong date format. It is expected " + Constants.DATE_FORMAT); //NOI18N
                }
            }
            if(attibuteType.equals("String")){
                if(condition == ExtendedQuery.LIKE) {
                    attributeValue = " TOLOWER(\"".concat(attributeValue).concat("\")");
                    return "TOLOWER(" + joinName + "." + attributeName + ")" + operator + attributeValue;
                } else
                    attributeValue = "\"".concat(attributeValue).concat("\"");
            }
        }
        return joinName + "." + attributeName + operator + attributeValue;
    }

    /**
     * Get the operator as a string
     * @param condition
     * @return the operator as a string
     */
    public String getOperator(int condition){
        switch (condition) {
            default: // Should never happen, but due to a bug discover circa version 2.1, EQUAL is the default operator
            case ExtendedQuery.EQUAL:
                return " =";//NOI18N
            case ExtendedQuery.EQUAL_OR_GREATER_THAN:
                return " >=";//NOI18N
            case ExtendedQuery.EQUAL_OR_LESS_THAN:
                return " <=";//NOI18N
            case ExtendedQuery.GREATER_THAN:
                return " >";//NOI18N
            case ExtendedQuery.LESS_THAN:
                return " <";//NOI18N
            case ExtendedQuery.LIKE:
                return " CONTAINS ";//NOI18N
        }
    }

    /**
     * creates the main return the "instance"
     * @return 
     */
    public String createReturn(){
        return "instance";
    }
 }
