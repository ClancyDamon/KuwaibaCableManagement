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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Component to show the free ports to mirror.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <T> Type of the mirror editor.
 */
public abstract class AbstractFreePortsComponent<T extends AbstractMirrorEditor> extends VerticalLayout {
    protected final BusinessObjectLight businessObject;
    protected final BusinessEntityManager bem;
    protected final TranslationService ts;
    
    protected final VerticalLayoutDropTarget lytUnassignedSourcePorts;
    protected final VerticalLayoutDropTarget lytUnassignedTargetPorts;
    protected final VerticalLayout lytAssignedPorts;
    protected final Command cmdUpdateFreePorts;
    
    protected PortComponent source;
    protected PortComponent target;
    
    public AbstractFreePortsComponent(BusinessObjectLight businessObject, 
        BusinessEntityManager bem, TranslationService ts, 
        VerticalLayoutDropTarget lytUnassignedSourcePorts, 
        VerticalLayoutDropTarget lytUnassignedTargetPorts, 
        VerticalLayout lytAssignedPorts, Command cmdUpdateFreePorts) {
        
        this.businessObject = businessObject;
        this.bem = bem;
        this.ts = ts;
        
        this.lytUnassignedSourcePorts = lytUnassignedSourcePorts;
        this.lytUnassignedTargetPorts = lytUnassignedTargetPorts;
        this.lytAssignedPorts = lytAssignedPorts;
        this.cmdUpdateFreePorts = cmdUpdateFreePorts;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setSizeFull();
        try {
            List<BusinessObjectLight> freePorts = getFreePorts();
            
            freePorts.forEach(port -> {
                PortComponent portComponent = new PortComponent(port, bem, ts);
                portComponent.addClickListener(clickEvent -> onPortComponentClick(portComponent));
                add(portComponent);
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
    /**
     * Gets the free ports.
     * @return the free ports.
     * @throws InventoryException If cannot found free ports
     */
    public abstract List<BusinessObjectLight> getFreePorts() throws InventoryException;
    /**
     * Gets a mirror editor.
     * @param source Mirror source port component.
     * @param target Mirror target port component.
     * @return The mirror editor.
     */
    public abstract T getMirrorEditor(PortComponent source, PortComponent target);
    /**
     * Creates the mirror given the source and target.
     * @param source Mirror source port.
     * @param target Mirror target port.
     */
    public abstract void createMirror(BusinessObjectLight source, BusinessObjectLight target);
    /**
     * Executed on port component click event.
     * @param portComponent The port component
     */
    public abstract void onPortComponentClick(PortComponent portComponent);
}
