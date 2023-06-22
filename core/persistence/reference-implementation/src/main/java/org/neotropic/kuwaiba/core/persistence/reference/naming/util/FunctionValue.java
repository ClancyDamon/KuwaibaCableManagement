/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
 */
package org.neotropic.kuwaiba.core.persistence.reference.naming.util;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A dynamic section function used to get an attribute value from an object
 * given the object id.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FunctionValue extends DynamicSectionFunction {
    public static final String FUNCTION_PATTERN = "value\\([0-9]+,[a-zA-Z]+,[a-zA-Z]+\\)";
    /**
     * Attribute Name
     */
    private String attribute;
    /**
     * Remote business object representation of the object with the given id
     */
    private BusinessObject remoteBusinessObject;
        
    public FunctionValue(String dynamicSectionFunction) throws InvalidArgumentException {
        super(FUNCTION_PATTERN, dynamicSectionFunction);
        
        Pattern pattern = Pattern.compile("[0-9]+,[a-zA-Z]+,[a-zA-Z]+");
        Matcher matcher = pattern.matcher(dynamicSectionFunction);
        if (matcher.find()) {
            String id = matcher.group().split(",")[0];
            String className = matcher.group().split(",")[1];
            
            attribute = matcher.group().split(",")[2];
                    
//            try {
//                BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
//                remoteBusinessObject = bem.getObject(className, id);
//
//                if (!remoteBusinessObject.getAttributes().containsKey(attribute))
//                    throw new InvalidArgumentException(String.format("The attribute \"%s\" can not be found in object with id %s", attribute, id));
//
//            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
//                throw new InvalidArgumentException(String.format("The object with id %s can not be found", id));
//            }
        }
    }
    
    @Override
    public List<String> getPossibleValues() {
        List<String> dynamicSections = new ArrayList();
//        try {
//            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
//            String attributeValue = null;
//            
//            if (remoteBusinessObject != null)
//                attributeValue = bem.getAttributeValueAsString(remoteBusinessObject.getClassName(), remoteBusinessObject.getId(), attribute);
//                        
//            if (attributeValue == null)
//                attributeValue = "__"; // If the attribute value is null
//                            
//            dynamicSections.add(attributeValue);
//            
//        } catch (Exception ex) {
//            dynamicSections.add("__"); // If the attribute value is null
//        }
        return dynamicSections;
    }
}
