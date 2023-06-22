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
package org.neotropic.util.visual.general;

import com.vaadin.flow.component.html.Div;
import java.util.Map;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * A <code>Div</code> that takes a BusinessObjectLight instance and processes its validators 
 * to generate a formatted enclosure that can be used in graphical representations of objects
 * e.g display a custom color, background, text format of the IP addresses graphical map in ipam module
  * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class FormatedBusinessObjectDiv extends Div{

    public FormatedBusinessObjectDiv(BusinessObjectLight businessObject) {
        if (businessObject != null && businessObject.getValidators() != null) {
            for (Validator aValidator : businessObject.getValidators()) {
                if (aValidator.getProperties() != null) {
                    for (Map.Entry aProperty : aValidator.getProperties().entrySet()) {
                        switch ((String)aProperty.getKey()) {
                            case Validator.PROPERTY_COLOR:
                                getStyle().set("color", String.valueOf(aProperty.getValue()));
                                break;
                            case Validator.PROPERTY_FILLCOLOR:
                                getStyle().set("background-color", String.valueOf(aProperty.getValue()));
                                break;
                        }
                    }
                }
            }
        }
    }
}
