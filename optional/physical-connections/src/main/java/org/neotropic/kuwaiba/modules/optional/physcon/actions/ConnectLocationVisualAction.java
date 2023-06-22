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
package org.neotropic.kuwaiba.modules.optional.physcon.actions;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsModule;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.selectors.BusinessObjectSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Action to connect inventory objects with the class which is subclasses of 
 * GenericLocation
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class ConnectLocationVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Physical Connections Service.
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionsService;
    /**
     * Reference to the Connect Location Action.
     */
    @Autowired
    private ConnectLocationAction connectLocationAction;
    /**
     * Reference to the edit connection visual action.
     */
    @Autowired
    private EditConnectionsVisualAction editConnectionsVisualAction;
    
    private BusinessObjectLight target;

    public ConnectLocationVisualAction() {
        super(PhysicalConnectionsModule.MODULE_ID);
    }

    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICLOCATION;
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public ConfirmDialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            target = null;
            BusinessObjectLight source = (BusinessObjectLight) parameters.get("businessObject"); //NOI18N
            if (source == null)
                return null;
            
            ConfirmDialog wdw = new ConfirmDialog();
            wdw.setCloseOnOutsideClick(false);
            wdw.setDraggable(true);
            wdw.setResizable(true);
            wdw.setWidth("40%");
            
            TextField txtName = new TextField();
            txtName.setWidthFull();
            txtName.setClearButtonVisible(true);
            txtName.setPlaceholder(ts.getTranslatedString("module.physcon.actions.connect-location-to.label.connection-name"));
            
            ComboBox<ClassMetadataLight> cmbClasses = new ComboBox();
            cmbClasses.setWidthFull();
            cmbClasses.setClearButtonVisible(true);
            cmbClasses.setPlaceholder(ts.getTranslatedString("module.physcon.actions.connect-location-to.placeholder.classes"));
            cmbClasses.setItems(mem.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false));
            
            ComboBox<TemplateObjectLight> cmbTemplates = new ComboBox();
            cmbTemplates.setWidthFull();
            cmbTemplates.setClearButtonVisible(true);
            cmbTemplates.setPlaceholder(ts.getTranslatedString("module.physcon.actions.connect-location-to.placeholder.templates"));
            cmbTemplates.setEnabled(false);
            
            cmbClasses.addValueChangeListener(valueChangeEvent -> {
                ClassMetadataLight value = valueChangeEvent.getValue();
                cmbTemplates.setEnabled(false);
                if (value != null) {
                    try {
                        cmbTemplates.setItems(aem.getTemplatesForClass(value.getName()));
                        cmbTemplates.setEnabled(true);
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, 
                            ts
                        ).open();
                    }
                }
                else
                    cmbTemplates.clear();
            });
            BusinessObjectSelector targetSelector = new BusinessObjectSelector(
                ts.getTranslatedString("module.physcon.actions.connect-location-to.target-selector-placeholder"), 
                aem, bem, mem, ts
            );
            targetSelector.addSelectedObjectChangeListener(changeEvent -> {
                target = changeEvent.getSelectedObject();
                
                if (target != null)
                    txtName.setValue(String.format("%s--%s", source.getName(), target != null ? target.getName() : ""));
                else
                    txtName.setValue("");
            });
            
            Button btnCancel = new Button(
                ts.getTranslatedString("module.general.messages.cancel"), 
                clickEvent -> {
                    target = null;
                    wdw.close();
                }
            );
            Button btnOk = new Button(
                ts.getTranslatedString("module.general.messages.ok"), 
                clickEvent -> {
                    String cxnName = txtName.getValue();
                    if (cxnName == null || cxnName.isEmpty()) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.information"), 
                            ts.getTranslatedString("module.physcon.actions.connect-location-to.warning.connection-name"), 
                            AbstractNotification.NotificationType.INFO, 
                            ts
                        ).open();
                        return;
                    }
                    ClassMetadataLight cxnType = cmbClasses.getValue();
                    if (cxnType == null) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.information"), 
                            ts.getTranslatedString("module.physcon.actions.connect-location-to.warning.connection-type"), 
                            AbstractNotification.NotificationType.INFO, 
                            ts
                        ).open();
                        return;
                    }
                    if (target == null) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.information"), 
                            ts.getTranslatedString("module.physcon.actions.connect-location-to.warning.connection-target"), 
                            AbstractNotification.NotificationType.INFO, 
                            ts
                        ).open();
                        return;
                    }
                    try {
                        if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALNODE, target.getClassName())) {
                            try {
                                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                                TemplateObjectLight cxnTemplate = cmbTemplates.getValue();
                                
                                String cxnId = physicalConnectionsService.createPhysicalConnection(
                                    source.getClassName(), source.getId(),
                                    target.getClassName(), target.getId(), 
                                    cxnName, cxnType.getName(),
                                    cxnTemplate != null ? cxnTemplate.getId() : null,
                                    session.getUser().getUserName()
                                );
                                wdw.close();
                                new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.success"), 
                                    ts.getTranslatedString("module.physcon.actions.connect-location-to.success.connection-created"), 
                                    AbstractNotification.NotificationType.INFO, 
                                    ts
                                ).open();
                                
                                BusinessObjectLight cxnObject = bem.getObjectLight(cxnType.getName(), cxnId);
                                new ConfirmDialogEditConnections(cxnObject, editConnectionsVisualAction, ts).open();
                            } catch (Exception ex) {
                                new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"), 
                                    ex.getLocalizedMessage(), 
                                    AbstractNotification.NotificationType.ERROR, 
                                    ts
                                ).open();
                            }
                        } else {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.information"), 
                                String.format(
                                    ts.getTranslatedString("module.physcon.actions.connect-location-to.warning.cxn-target-type"), 
                                    target.getClassName(), appliesTo()
                                ), 
                                AbstractNotification.NotificationType.INFO, 
                                ts
                            ).open();
                        }
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, 
                            ts
                        ).open();
                    }
                }
            );
            VerticalLayout lytContent = new VerticalLayout(
                cmbClasses, cmbTemplates, targetSelector, txtName
            );
            HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnOk);
            lytFooter.setWidthFull();
            lytFooter.setFlexGrow(1, btnCancel, btnOk);
            
            wdw.setHeader(String.format(
                ts.getTranslatedString("module.physcon.actions.connect-location-to.header"), 
                source.getName()
            ));
            wdw.setContent(lytContent);
            wdw.setFooter(lytFooter);
            return wdw;
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        return null;
    }

    @Override
    public AbstractAction getModuleAction() {
        return connectLocationAction;
    }
    
}
