/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.modules.optional.reports.defaults;

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;


/**
 * Utility methods for building reports
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Util {
    public static String formatLocation (List<BusinessObjectLight> containmentHierarchy) {
        String location = "";
        if (containmentHierarchy.size() == 1 && !(Constants.NODE_DUMMYROOT).equals((containmentHierarchy.get(0).getClassName())))
            location += containmentHierarchy.get(0).toString();
        else{
            for (int i = 0; i < containmentHierarchy.size() - 1; i ++)
                location += containmentHierarchy.get(i).toString() + " | ";
        }
        return location;
    }
}
