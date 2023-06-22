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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener.ActionCompletedEvent;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new business object from template action.
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NewBusinessObjectFromTemplateVisualAction extends AbstractVisualInventoryAction {
    /**
     * business object parameter, used to retrieve the parent as a parameter.
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
    private NewBusinessObjectFromTemplateAction newBusinessObjectFromTemplateAction;

    public NewBusinessObjectFromTemplateVisualAction() {
        super(NavigationModule.MODULE_ID);
    }
    
    /**
     * Creates the visual component for new object from template visual action
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        ConfirmDialog wdw = new ConfirmDialog(ts, getModuleAction().getDisplayName(),
                ts.getTranslatedString("module.general.labels.create"));
        wdw.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        wdw.setDraggable(true);
        
        if (businessObject != null) {
            try {
                //Header
                Label lblTitle = new Label(ts.getTranslatedString("module.navigation.actions.new-business-object-from-template.name"));
                lblTitle.setClassName("dialog-title");
                Label lblParent;
                if(businessObject.getClassName().equals(Constants.DUMMY_ROOT))
                    lblParent = new Label(String.format("%s [%s]"
                            , businessObject.getName()
                            , ts.getTranslatedString("module.general.labels.root")));
                else
                    lblParent = new Label(businessObject.toString());
                HorizontalLayout lytTitleHeader = new HorizontalLayout(lblTitle, lblParent);
                lytTitleHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                lytTitleHeader.setVerticalComponentAlignment(FlexComponent.Alignment.END, lblParent);
                lytTitleHeader.setSpacing(true);
                lytTitleHeader.setPadding(false);
                lytTitleHeader.setMargin(false);
                                
                ComboBox<ClassMetadataLight> cmbPossibleChildrenClass = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-class"));
                cmbPossibleChildrenClass.setItems(mem.getPossibleChildren(businessObject.getClassName(), true));
                cmbPossibleChildrenClass.setRequiredIndicatorVisible(true);
                cmbPossibleChildrenClass.setEnabled(true);
                cmbPossibleChildrenClass.setPlaceholder(ts.getTranslatedString("module.navigation.actions.new-business-object.select-class"));
                cmbPossibleChildrenClass.setItemLabelGenerator(class_ -> 
                    class_.getDisplayName() != null && !class_.getDisplayName().isEmpty() ? class_.getDisplayName() : class_.getName()
                );
                //Templantes combo
                ComboBox<TemplateObjectLight> cmbTemplate = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-template"));
                cmbTemplate.setItemLabelGenerator(TemplateObjectLight::getName);
                cmbTemplate.setRequiredIndicatorVisible(true);
                cmbTemplate.setEnabled(false);
                cmbTemplate.setSizeFull();
                
                ShortcutRegistration btnOkShortcut = wdw.getBtnConfirm().addClickShortcut(Key.ENTER).listenOn(wdw);
                
                wdw.getBtnConfirm().addClickListener(e -> {
                    try {
                        if(cmbTemplate.getValue() != null){
                            ModuleActionParameterSet params = new ModuleActionParameterSet(
                                    new ModuleActionParameter(Constants.PROPERTY_CLASSNAME, cmbPossibleChildrenClass.getValue().getName()),
                                    new ModuleActionParameter(Constants.PROPERTY_PARENT_CLASS_NAME, businessObject.getClassName()),
                                    new ModuleActionParameter(Constants.PROPERTY_TEMPLATE_ID, cmbTemplate.getValue().getId()),
                                    new ModuleActionParameter(Constants.PROPERTY_PARENT_ID, businessObject.getId() == null ? "-1" : businessObject.getId()));

                            ActionResponse actionResponse = new ActionResponse();
                            actionResponse.put(ActionResponse.ActionType.ADD, "");
                            actionResponse.put(PARAM_BUSINESS_OBJECT, businessObject);
                            actionResponse.put(Constants.PROPERTY_PARENT_ID, businessObject.getId() == null ? "-1" : businessObject.getId());
                            actionResponse.put(Constants.PROPERTY_PARENT_CLASS_NAME, businessObject.getClassName());
                            
                            //Here we create the object(s)
                            newBusinessObjectFromTemplateAction.getCallback().execute(params);
                            wdw.close();

                            fireActionCompletedEvent(new ActionCompletedEvent(
                                    ActionCompletedEvent.STATUS_SUCCESS,
                                    ts.getTranslatedString("module.navigation.actions.new-business-object.ui.success"),
                                    NewBusinessObjectAction.class, actionResponse)
                            );
                        }
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_ERROR, ex.getMessage(),
                                NewBusinessObjectFromTemplateAction.class));
                    }
                    btnOkShortcut.remove();
                    e.unregisterListener();
                });
                wdw.getBtnConfirm().setAutofocus(true);
                wdw.getBtnConfirm().setEnabled(false);

                cmbPossibleChildrenClass.addValueChangeListener(c -> {
                    if (c.getValue() != null) {
                        try {
                            cmbPossibleChildrenClass.setInvalid(false);
                            wdw.getBtnConfirm().setEnabled(false);
                            //Templates
                            List<TemplateObjectLight> templatesForSelectedClass = aem.getTemplatesForClass(c.getValue().getName());
                            
                            if(!templatesForSelectedClass.isEmpty()){
                                cmbTemplate.setItems(templatesForSelectedClass);
                                cmbTemplate.setEnabled(true);
                                cmbTemplate.setPlaceholder(ts.getTranslatedString("module.navigation.actions.new-business-object-from-template.select-template"));
                                cmbTemplate.addValueChangeListener(e -> {
                                    cmbTemplate.setInvalid(e.getValue() != null);
                                    wdw.getBtnConfirm().setEnabled(e.getValue() != null);
                                });
                            }
                        } catch (InventoryException ex) {
                            cmbTemplate.setValue(null);
                            cmbTemplate.setEnabled(false);
                            wdw.getBtnConfirm().setEnabled(false);
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    }
                    else {
                        cmbPossibleChildrenClass.setInvalid(true);
                        cmbTemplate.setValue(null);
                        cmbTemplate.setEnabled(false);
                        cmbTemplate.setPlaceholder(String.format(
                                ts.getTranslatedString("module.navigation.actions.new-business-object-from-template.class-has-no-templates")
                                , c.getValue().getName()));
                        wdw.getBtnConfirm().setEnabled(false);
                    }
                });
                
                HorizontalLayout lytFields = new HorizontalLayout();
                lytFields.addAndExpand(cmbPossibleChildrenClass, cmbTemplate);
                lytFields.setWidthFull();
                wdw.setContent(lytFields);
                
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
        return newBusinessObjectFromTemplateAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}
