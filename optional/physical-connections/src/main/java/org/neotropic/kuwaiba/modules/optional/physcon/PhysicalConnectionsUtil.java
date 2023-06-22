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
package org.neotropic.kuwaiba.modules.optional.physcon;

import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PhysicalConnectionsUtil {
    
    public static String getLinkName(BusinessObjectLight sourcePort, BusinessObjectLight targetPort, 
        BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        try {
            if (sourcePort != null && targetPort != null && 
                mem.isSubclassOf(Constants.CLASS_GENERICPORT, sourcePort.getClassName()) &&
                mem.isSubclassOf(Constants.CLASS_GENERICPORT, targetPort.getClassName())) {
                
                BusinessObjectLight aSourcePortParent;
                BusinessObjectLight aTargetPortParent;

                List<BusinessObjectLight> sourcePortParents = bem.getParentsUntilFirstOfClass(sourcePort.getClassName(), sourcePort.getId(), 
                    Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME, Constants.CLASS_GENERICSPLICINGDEVICE
                );
                if (!sourcePortParents.isEmpty()) {
                    aSourcePortParent = sourcePortParents.get(sourcePortParents.size() - 1);
                } else {
                    aSourcePortParent = bem.getParent(sourcePort.getClassName(), sourcePort.getId());
                }
                List<BusinessObjectLight> targetPortParents = bem.getParentsUntilFirstOfClass(targetPort.getClassName(), targetPort.getId(), 
                    Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME, Constants.CLASS_GENERICSPLICINGDEVICE
                );
                if (!targetPortParents.isEmpty()) {
                    aTargetPortParent = targetPortParents.get(targetPortParents.size() - 1);
                } else {
                    aTargetPortParent = bem.getParent(targetPort.getClassName(), targetPort.getId());
                }
                if (aSourcePortParent != null && aTargetPortParent != null) {
                    return String.format("%s %s -- %s %s", 
                        aSourcePortParent.getName(), sourcePort.getName(), 
                        aTargetPortParent.getName(), targetPort.getName()
                    );
                }
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        return "";
    }
}
