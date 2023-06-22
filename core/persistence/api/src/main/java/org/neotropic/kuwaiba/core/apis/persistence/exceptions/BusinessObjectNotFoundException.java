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
package org.neotropic.kuwaiba.core.apis.persistence.exceptions;

/**
 * Thrown if you're trying to access to a non existing object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BusinessObjectNotFoundException extends InventoryException {

    public BusinessObjectNotFoundException(String objectClass, String oid) {
        super(String.format("Object of class %s and id %s could not be found. It might have been deleted already", objectClass, oid));
        setPrefix("api.bem.error");
        setCode(1);
        setMessageArgs(objectClass, oid);
    }
    
    public BusinessObjectNotFoundException(String message) {
        super(message);
    }
}
