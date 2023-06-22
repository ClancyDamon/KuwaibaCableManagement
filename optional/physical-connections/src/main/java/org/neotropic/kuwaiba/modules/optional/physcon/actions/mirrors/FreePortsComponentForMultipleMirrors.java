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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Component to show the free ports for multiple mirrors
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FreePortsComponentForMultipleMirrors extends AbstractFreePortsComponent<MultipleMirrorEditor> {

    public FreePortsComponentForMultipleMirrors(BusinessObjectLight businessObject, BusinessEntityManager bem, TranslationService ts, VerticalLayoutDropTarget lytUnassignedSourcePorts, VerticalLayoutDropTarget lytUnassignedTargetPorts, VerticalLayout lytAssignedPorts, Command cmdUpdateFreePorts) {
        super(businessObject, bem, ts, lytUnassignedSourcePorts, lytUnassignedTargetPorts, lytAssignedPorts, cmdUpdateFreePorts);
    }

    @Override
    public MultipleMirrorEditor getMirrorEditor(PortComponent source, PortComponent target) {
        return new MultipleMirrorEditor(source, target, lytAssignedPorts, cmdUpdateFreePorts, bem, ts);
    }

    @Override
    public void createMirror(BusinessObjectLight source, BusinessObjectLight target) {
        try {
            bem.createSpecialRelationship(
                source.getClassName(), source.getId(),
                target.getClassName(), target.getId(),
                "mirrorMultiple", true //NOI18N
            );
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.information"), 
                ts.getTranslatedString("module.physcon.mirror-man.notification.info.mirror-has-been-created"), 
                AbstractNotification.NotificationType.INFO, 
                ts
            ).open();
        }
    }

    @Override
    public void onPortComponentClick(PortComponent portComponent) {
        try {
            if (lytUnassignedSourcePorts.indexOf(portComponent) > -1) {
                if (target == null && !bem.hasSpecialRelationship(portComponent.getPort().getClassName(), portComponent.getPort().getId(), "mirror", 1)) { //NOI18N
                    source = portComponent;
                    source.setSelected(true);
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"),
                        ts.getTranslatedString("module.physcon.mirror-man.notification.info.the-port-is-mirrored"),
                        AbstractNotification.NotificationType.INFO,
                        ts
                    ).open();
                }
            } else if (source != null && lytUnassignedTargetPorts.indexOf(portComponent) > -1) {
                if (!bem.hasSpecialRelationship(portComponent.getPort().getClassName(), portComponent.getPort().getId(), "mirror", 1) && 
                    !bem.hasSpecialRelationship(portComponent.getPort().getClassName(), portComponent.getPort().getId(), "mirrorMultiple", 1)) { //NOI18N
                    target = portComponent;
                    target.setSelected(true);
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"),
                        ts.getTranslatedString("module.physcon.mirror-man.notification.info.the-port-is-mirrored"),
                        AbstractNotification.NotificationType.INFO,
                        ts
                    ).open();
                }
            }
            if (source != null && target != null) {
                lytAssignedPorts.add(getMirrorEditor(source, target));
                source.setSelected(false);
                target.setSelected(false);

                createMirror(source.getPort(), target.getPort());
                source = null;
                target = null;
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.physcon.mirror-man.notification.info.multiple-mirror-has-been-created"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(),
                AbstractNotification.NotificationType.ERROR,
                ts
            ).open();
        }
    }

    @Override
    public List<BusinessObjectLight> getFreePorts() throws InventoryException {
        List<BusinessObjectLight> thePorts = bem.getChildrenOfClassLightRecursive(
            businessObject.getId(), businessObject.getClassName(), 
            Constants.CLASS_GENERICPORT, null, -1, -1
        );
        Collections.sort(thePorts, Comparator.comparing(BusinessObjectLight::getName));

        List<BusinessObjectLight> freePorts = new ArrayList();
        for (BusinessObjectLight port : thePorts) {
            if (!bem.hasSpecialRelationship(port.getClassName(), port.getId(), "mirror", 1)) //NOI18N
                freePorts.add(port);
        }
        return freePorts;
    }
    
}
