/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import java.util.HashMap;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractObjectRelatedViewWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectOptionsPanel;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Wrapper Window to the ObjectOptionsPanel
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ObjectOptionsWindow extends ConfirmDialog {
    private final CoreActionsRegistry coreActionsRegistry;
    private final AdvancedActionsRegistry advancedActionsRegistry;
    private final ViewWidgetRegistry viewWidgetRegistry;
    private final ExplorerWidgetRegistry explorerWidgetRegistry;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private BusinessObjectLight businessObject;

    public ObjectOptionsWindow(
        CoreActionsRegistry coreActionsRegistry,
        AdvancedActionsRegistry advancedActionsRegistry,
        ViewWidgetRegistry viewWidgetRegistry,
        ExplorerWidgetRegistry explorerWidgetRegistry,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        
        Objects.requireNonNull(coreActionsRegistry);
        Objects.requireNonNull(advancedActionsRegistry);
        Objects.requireNonNull(viewWidgetRegistry);
        Objects.requireNonNull(explorerWidgetRegistry);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.coreActionsRegistry = coreActionsRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        setContentSizeFull();
        setDraggable(true);
        setModal(false);
        setWidth("30%");
        setHeight("80%");
    }

    public BusinessObjectLight getBusinessObject() {
        return businessObject;
    }

    public void setBusinessObject(BusinessObjectLight businessObject) {
        this.businessObject = businessObject;
    }

    @Override
    public void open() {
        if (businessObject != null) {
            ObjectOptionsPanel pnlObjOptions = new ObjectOptionsPanel(
                businessObject, 
                coreActionsRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry, 
                mem, aem, bem, ts
            );
            pnlObjOptions.setSelectionListener(event -> {
                try {
                    switch (event.getActionCommand()) {
                        case ObjectOptionsPanel.EVENT_VIEW_SELECTION:
                            Dialog wdwView = new Dialog(((AbstractObjectRelatedViewWidget) event.getSource()).build(businessObject));
                            wdwView.setHeight("90%");
                            wdwView.setWidthFull();
                            wdwView.setResizable(true);
                            wdwView.setDraggable(true);
                            wdwView.setCloseOnEsc(true);
                            wdwView.open();
                        break;
                        case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                            ((Dialog) ((AbstractVisualInventoryAction) event.getSource()).getVisualComponent(
                                new ModuleActionParameterSet(new ModuleActionParameter("businessObject", businessObject)))
                            ).open();
                        break;
                        case ObjectOptionsPanel.EVENT_EXPLORER_SELECTION:
                            Dialog wdwExplorer = new Dialog(((AbstractExplorerWidget) event.getSource()).build(businessObject));
                            wdwExplorer.setHeight("90%");
                            wdwExplorer.setWidthFull();
                            wdwExplorer.setResizable(true);
                            wdwExplorer.setDraggable(true);
                            wdwExplorer.setCloseOnEsc(true);
                            wdwExplorer.open();
                        break;
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
            pnlObjOptions.setPropertyListener(property -> {
                try {
                    HashMap<String, String> attributes = new HashMap();
                    attributes.put(property.getName(), String.valueOf(property.getValue()));
                    bem.updateObject(businessObject.getClassName(), businessObject.getId(), attributes);
                    if (Constants.PROPERTY_NAME.equals(property.getName()))
                        businessObject.setName(String.valueOf(property.getValue()));
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            });
            Button btnClose = new Button(
                ts.getTranslatedString("module.general.messages.close"), 
                clickEvent -> close()
            );
            btnClose.setWidthFull();
            try {
                setHeader(String.format("%s [%s]", 
                    businessObject.getName(), 
                    mem.getClass(businessObject.getClassName()).toString()
                ));
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
                setHeader(businessObject.toString());
            }
            try {
                setContent(pnlObjOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser()));
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
                close();
            }
            setFooter(btnClose);
            super.open();
        }
        else
            close();
    }
}
