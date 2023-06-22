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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener.ActionCompletedEvent;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.layout.MandatoryAttributesLayout;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new business object action.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class NewBusinessObjectVisualAction extends AbstractVisualInventoryAction {
    /**
     * New business object visual action parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
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
     * Reference to module action.
     */
    @Autowired
    private NewBusinessObjectAction newBusinessObjectAction;
    
    public NewBusinessObjectVisualAction() {
        super(NavigationModule.MODULE_ID);
    }

    /**
     * Creates the visual component for new object visual action
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        HashMap<String, String>  attributes = new HashMap();
        
        HorizontalLayout lytExtraFields =  new HorizontalLayout();
        lytExtraFields.setWidthFull();
        
        BusinessObjectLight parentBusinessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        ConfirmDialog wdw = new ConfirmDialog(ts, getModuleAction().getDisplayName(), ts.getTranslatedString("module.general.labels.create"));
        wdw.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        wdw.setDraggable(true);
        wdw.getBtnConfirm().setEnabled(false);
        
        if (parentBusinessObject != null) {
            try {
                Label lblTitle = new Label(newBusinessObjectAction.getDisplayName());
                lblTitle.setClassName("dialog-title");
                Label lblParent;
                if(parentBusinessObject.getClassName().equals(Constants.DUMMY_ROOT))
                    lblParent = new Label(String.format("%s [%s]"
                            , parentBusinessObject.getName()
                            , ts.getTranslatedString("module.general.labels.root")));
                else
                    lblParent = new Label(parentBusinessObject.toString());
                HorizontalLayout lytTitleHeader = new HorizontalLayout(lblTitle, lblParent);
                lytTitleHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                lytTitleHeader.setVerticalComponentAlignment(FlexComponent.Alignment.END, lblParent);
                lytTitleHeader.setSpacing(true);
                lytTitleHeader.setPadding(false);
                lytTitleHeader.setMargin(false);
                //Content
                ComboBox<ClassMetadataLight> cmbPossibleChildrenClass = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-class"));
                cmbPossibleChildrenClass.setItems(mem.getPossibleChildren(parentBusinessObject.getClassName(), true));
                cmbPossibleChildrenClass.setRequired(true);
                cmbPossibleChildrenClass.setEnabled(true);
                cmbPossibleChildrenClass.setPlaceholder(ts.getTranslatedString("module.navigation.actions.new-business-object.select-class"));
                cmbPossibleChildrenClass.setItemLabelGenerator(class_ -> 
                    class_.getDisplayName() != null && !class_.getDisplayName().isEmpty() ? class_.getDisplayName() : class_.getName()
                );
                TextField txtName = new TextField(ts.getTranslatedString("module.navigation.actions.new-single-business-object.ui.object-name"));
                txtName.setClearButtonVisible(true);
                txtName.setValueChangeMode(ValueChangeMode.EAGER);
                
                cmbPossibleChildrenClass.addValueChangeListener(event -> {
                    if (event.getValue() != null) {
                        try {
                            lytExtraFields.removeAll();
                            txtName.setRequired(false);
                            txtName.clear();
                            //Mandatory attribtues
                            List<AttributeMetadata> mandatoryAttributesInSelectedClass = mem.getMandatoryAttributesInClass(event.getValue().getName());
                            if(!mandatoryAttributesInSelectedClass.isEmpty())
                                lytExtraFields.add(new MandatoryAttributesLayout(txtName, wdw, attributes, mandatoryAttributesInSelectedClass, aem));
                            else
                                wdw.getBtnConfirm().setEnabled(true);
                            lytExtraFields.setSizeUndefined();
                        } catch (InventoryException ex) {
                            lytExtraFields.removeAll();
                            wdw.getBtnConfirm().setEnabled(false);
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    }
                    else
                        wdw.getBtnConfirm().setEnabled(false);
                });
                FormLayout lytFields = new FormLayout(cmbPossibleChildrenClass, txtName);
                lytFields.setWidthFull();
                lytFields.setResponsiveSteps(
                    new ResponsiveStep("40em", 2),
                    new ResponsiveStep("60em", 3));
                
                wdw.setContent(lytFields, lytExtraFields);
                //footer Buttons
                ShortcutRegistration btnOkShortcut = wdw.getBtnConfirm().addClickShortcut(Key.ENTER).listenOn(wdw);
                wdw.getBtnConfirm().addClickListener(event -> {
                    try {
                        attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                        
                        ModuleActionParameterSet params = new ModuleActionParameterSet(
                                new ModuleActionParameter(Constants.PROPERTY_CLASSNAME, cmbPossibleChildrenClass.getValue().getName()),
                                new ModuleActionParameter(Constants.PROPERTY_PARENT_CLASS_NAME, parentBusinessObject.getClassName()),
                                new ModuleActionParameter(Constants.PROPERTY_PARENT_ID, parentBusinessObject.getId() == null ? "-1" : parentBusinessObject.getId()));
                        
                        params.put(Constants.PROPERTY_ATTRIBUTES, attributes);
                        
                        ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(ActionResponse.ActionType.ADD, "");
                        actionResponse.put(Constants.PROPERTY_PARENT_ID, parentBusinessObject.getId() == null ? "-1" : parentBusinessObject.getId());
                        actionResponse.put(Constants.PROPERTY_PARENT_CLASS_NAME, parentBusinessObject.getClassName());
                        actionResponse.put(PARAM_BUSINESS_OBJECT, parentBusinessObject);
                        
                        //Here we create the object(s)                                                
                        newBusinessObjectAction.getCallback().execute(params);
                        wdw.close();
                        
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.navigation.actions.new-business-object.ui.success"),
                                NewBusinessObjectAction.class, actionResponse)
                        );
                        
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_ERROR, ex.getMessage(),
                                NewBusinessObjectAction.class));
                    }
                    btnOkShortcut.remove();
                    event.unregisterListener();
                });
            } catch (InventoryException ex) {
                fireActionCompletedEvent(new ActionCompletedEvent(
                    ActionCompletedEvent.STATUS_ERROR, ex.getMessage(), 
                    NewBusinessObjectAction.class)
                );
                wdw.add(new Label(ex.getMessage()));
            }
        }
        return wdw;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newBusinessObjectAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}
