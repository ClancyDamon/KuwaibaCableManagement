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
package org.neotropic.kuwaiba.modules.core.datamodelman.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.datamodelman.DataModelManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new Attribute action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewAttributeVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * New class visual action parameter class.
     */
    public static String PARAM_CLASS = "class"; //NOI18N
    public static String PARAM_ATTRIBUTE = "attribute"; //NOI18N
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewAttributeAction newAttributeAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;

    public NewAttributeVisualAction() {
        super(DataModelManagerModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey(PARAM_CLASS)) {
            ClassMetadataLight seletedClass = (ClassMetadataLight) parameters.get(PARAM_CLASS);
            try {
                ConfirmDialog wdwAdd = new ConfirmDialog(ts,
                        String.format(ts.getTranslatedString("module.datamodelman.actions.new-attribute-for-class"), seletedClass.getName()),
                        ts.getTranslatedString("module.general.labels.create"));
                wdwAdd.setResizable(true);
                wdwAdd.setDraggable(true);
                
                TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
                txtName.setRequiredIndicatorVisible(true);
                txtName.setWidthFull();

                TextField txtDisplayName = new TextField(ts.getTranslatedString("module.general.labels.display-name"));
                txtDisplayName.setWidthFull();

                List<ClassMetadataLight> listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
                List<String> lstListTypes = listTypes.stream().map(ClassMetadataLight::getName).collect(Collectors.toList());
                List<String> lstAllTypes = new ArrayList(Arrays.asList(Constants.DATA_TYPES));
                lstAllTypes.addAll(lstListTypes);

                ComboBox cmbType = new ComboBox<>(ts.getTranslatedString("module.general.labels.type"));
                cmbType.setWidthFull();
                cmbType.setItems(lstAllTypes);
                cmbType.setClearButtonVisible(true);
                cmbType.setValue(Constants.DATA_TYPE_STRING);
                cmbType.setAllowCustomValue(false);
                cmbType.setRequired(true);
                cmbType.setRequiredIndicatorVisible(true);

                wdwAdd.getBtnConfirm().addClickListener(event -> {
                    try {
                        if (txtName.getValue() == null || txtName.getValue().trim().isEmpty()) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                    String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                            ts.getTranslatedString("module.general.labels.name")),
                                    AbstractNotification.NotificationType.WARNING, ts).open();
                        } else if (cmbType.getValue() == null) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                                    String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                            ts.getTranslatedString("module.general.labels.type")),
                                    AbstractNotification.NotificationType.WARNING, ts).open();
                        } else {
                            newAttributeAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>("className", seletedClass.getName()),
                                    new ModuleActionParameter<>("attributeName", txtName.getValue()),
                                    new ModuleActionParameter<>("attributeDisplayName", txtDisplayName.getValue()),
                                    new ModuleActionParameter<>("attributeType", cmbType.getValue())));

                            ActionResponse actionResponse = new ActionResponse();
                            actionResponse.put(ActionResponse.ActionType.ADD, "");
                            actionResponse.put(Constants.PROPERTY_PARENT_ID, seletedClass.getId() <= 0 ? "-1" : seletedClass.getId());
                            actionResponse.put(Constants.PROPERTY_PARENT_CLASS_NAME, seletedClass.getName());
                            actionResponse.put(PARAM_CLASS, seletedClass);
                            actionResponse.put(PARAM_ATTRIBUTE, "");

                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                    ts.getTranslatedString("module.datamodelman.actions.new-class-attribute-success"), NewAttributeAction.class, actionResponse));

                            wdwAdd.close();
                        }
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), NewAttributeAction.class));
                    }
                });

                wdwAdd.setContent(txtName, txtDisplayName, cmbType);
                return wdwAdd;
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                return new Dialog(new Label(ex.getMessage()));
            }
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_CLASS)));
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newAttributeAction;
    }
}