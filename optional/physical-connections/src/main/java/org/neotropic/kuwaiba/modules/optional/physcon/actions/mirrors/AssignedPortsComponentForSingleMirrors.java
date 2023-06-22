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
package org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Component to show the assigned ports for single mirror.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class AssignedPortsComponentForSingleMirrors extends AbstractAssignedPortsComponent {
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    
    private final BusinessObjectLight businessObject;
    
    private final Command cmdUpdateFreePorts;
    
    public AssignedPortsComponentForSingleMirrors(BusinessObjectLight businessObject, Command cmdUpdateFreePorts, BusinessEntityManager bem, TranslationService ts) {
        this.bem = bem;
        this.ts = ts;
        this.businessObject = businessObject;
        this.cmdUpdateFreePorts = cmdUpdateFreePorts;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        try {
            List<BusinessObjectLight> ports = bem.getChildrenOfClassLightRecursive(businessObject.getId(), businessObject.getClassName(), Constants.CLASS_GENERICPORT, null, -1, -1);
            Collections.sort(ports);

            List<BusinessObjectLight> assignedPorts = new ArrayList();

            ports.forEach(port -> {
                try {
                    if (bem.hasSpecialRelationship(port.getClassName(), port.getId(), "mirror", 1)) { //NOI18N
                        BusinessObjectLight mirror = bem.getSpecialAttribute(port.getClassName(), port.getId(), "mirror").get(0); //NOI18N
                        if (!assignedPorts.contains(mirror))
                            assignedPorts.add(port);
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            });
            assignedPorts.forEach(port -> {
                List<BusinessObjectLight> mirrors;
                try {
                    mirrors = bem.getSpecialAttribute(port.getClassName(), port.getId(), "mirror"); //NOI18N
                    mirrors.forEach(mirror -> add(new SingleMirrorEditor(
                        new PortComponent(port, bem, ts), 
                        new PortComponent(mirror, bem, ts),
                        this,
                        cmdUpdateFreePorts,
                        bem, ts
                    )));
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            });
        } catch (InventoryException ex) {
            new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR,
                    ts
            ).open();
        }
    }
}
