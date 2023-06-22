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
package org.neotropic.util.visual.selectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Port selector.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PortSelector {
    private final String header;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    private BusinessObjectLight selectedObject;
    private List<BusinessObjectLight> selectedObjects;
    private boolean hasWarnings = true;
    
    public PortSelector(String header, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        Objects.requireNonNull(header);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.header = header;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
    }
    
    public void setHasWarnings(boolean hasWarning) {
        this.hasWarnings = hasWarning;
    }
    
    public ConfirmDialog getPortSelector() throws InventoryException {
        ConfirmDialog wdw = new ConfirmDialog();
        
        CellPortSelector lytContent = new CellPortSelector(selectedObject, selectedObjects, header, aem, bem, mem, ts);
        lytContent.addSelectedObjectChangeListener(event -> {
            selectedObject = event.getSelectedObject();
            selectedObjects = event.getSelectedObjects();
        });
        Button btnCancel = new Button(
            ts.getTranslatedString("module.general.messages.cancel"), 
            clickEvent -> {
                selectedObject = null;
                selectedObjects = null;
                wdw.close();
            }
        );
        Button btnSelect = new Button(header, clickEvent -> {
            try {
                if (selectedObject != null && mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedObject.getClassName())) {
                    if (!bem.getSpecialAttributes(selectedObject.getClassName(), selectedObject.getId(), "endpointA", "endpointB").isEmpty()) { //NOI18N
                        if (hasWarnings) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.warning"), 
                                ts.getTranslatedString("module.connectivity-manager.action.connected-port"), 
                                AbstractNotification.NotificationType.WARNING, 
                                ts
                            ).open();
                        }
                    }
                    wdw.close();
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.connectivity-manager.action.selected-object-is-not-port"), 
                        AbstractNotification.NotificationType.WARNING, 
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
                wdw.close();
            }
        });
        FlexLayout lytFooter = new FlexLayout(btnCancel, btnSelect);
        lytFooter.setFlexGrow(1, btnCancel, btnSelect);
        
        wdw.setHeader(header);
        wdw.setContent(lytContent);
        wdw.setFooter(lytFooter);
        return wdw;
    }
    
    public BusinessObjectLight getSelectedObject() {
        return selectedObject;
    }
    
    public void setSelectedObject(BusinessObjectLight selectedObject) {
        this.selectedObject = selectedObject;
    }
    
    public List<BusinessObjectLight> getSelectedObjects() {
        return selectedObjects;
    }
    
    public void setSelectedObjects(List<BusinessObjectLight> selectedObjects) {
        if (selectedObjects != null) {
            this.selectedObjects = new ArrayList();
            selectedObjects.forEach(selectedObject -> this.selectedObjects.add(selectedObject));
        } else {
            this.selectedObjects = null;
        }
    }
}
