/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.views.util;

import java.awt.Color;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

/**
 * Set of methods commonly used by the business objects in views.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class BusinessObjectUtil {
    /**
     * Gets the object color. The object color can be:
     * <pre>
     * The value of the attribute value of type String of the list type item ColorType as the color attribute of the object.
     * The value of the attribute color of type String of the object.
     * The class color.
     * </pre>
     * @param objectLight The object to get the color.
     * @param aem The Application Entity Manager.
     * @param bem The Business Entity Manager.
     * @param mem The Metadata Entity Manager.
     * @throws InventoryException
     * @return The object color.
     */
    public static String getBusinessObjectColor(BusinessObjectLight objectLight, ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem) throws InventoryException {
        Objects.requireNonNull(objectLight);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        
        final String attrNameColor = "color"; //NOI18N
        final String attrNameValue = "value"; //NOI18N
        
        ClassMetadata objectClass = mem.getClass(objectLight.getClassName());
        if (objectClass.hasAttribute(attrNameColor)) {
            AttributeMetadata attrColor = objectClass.getAttribute(attrNameColor);
            
            if (!AttributeMetadata.isPrimitive(attrColor.getType())) {
                
                ClassMetadata colorClass = mem.getClass(attrColor.getType());
                if (colorClass.hasAttribute(attrNameValue)) {
                    AttributeMetadata attrValue = colorClass.getAttribute(attrNameValue);
                    if (Constants.DATA_TYPE_STRING.equals(attrValue.getType())) {
                        BusinessObject object  = bem.getObject(objectLight.getClassName(), objectLight.getId());
                        
                        if (object.getAttributes().containsKey(attrNameColor)) {
                            String colorId = (String) object.getAttributes().get(attrNameColor);
                            BusinessObject colorType = aem.getListTypeItem(attrColor.getType(), colorId);
                            if (colorType.getAttributes().containsKey(attrNameValue))
                                return (String) colorType.getAttributes().get(attrNameValue);
                        }
                    }
                }
            } else if (Constants.DATA_TYPE_STRING.equals(attrColor.getType())) {
                String colorValue = bem.getAttributeValueAsString(objectLight.getClassName(), objectLight.getId(), attrNameColor);
                if (colorValue != null)
                    return colorValue;
            }
        }
        return UtilHtml.toHexString(new Color(objectClass.getColor()));
    }
}
