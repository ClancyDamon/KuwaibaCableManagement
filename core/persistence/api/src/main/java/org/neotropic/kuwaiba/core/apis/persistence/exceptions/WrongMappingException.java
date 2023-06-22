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
 *
 */

package org.neotropic.kuwaiba.core.apis.persistence.exceptions;

/**
 * This exception is raised when a pair attribute type - attribute value is not valid
 * (i.e. type Integer, value "aaaaa")
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class WrongMappingException extends InventoryException{

    public WrongMappingException(String className, String attributeName,
            String attributeType, String attributeValue) {
        super (String.format("Value %s can't mapped into type %s for attribute %s in class %s",
                attributeValue, attributeType, attributeName, className));
    }

}
