/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.softman.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerModule;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerService;
import org.neotropic.kuwaiba.modules.commercial.softman.components.EActionParameter;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of relate object action.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NewLicenseVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Contract Manager Service.
    */
    @Autowired
    private SoftwareManagerService sms;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewLicenseAction newLicenseAction;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    
    public NewLicenseVisualAction() {
        super(SoftwareManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            if (parameters.containsKey(EActionParameter.POOL.getPropertyValue())) {
                InventoryObjectPool pool = (InventoryObjectPool) parameters.get(EActionParameter.POOL.getPropertyValue());
                
                ConfirmDialog wdwNewLicense = new ConfirmDialog(ts,
                        ts.getTranslatedString(ts.getTranslatedString("module.softman.actions.new-license.name")),
                        ts.getTranslatedString("module.general.labels.create"));
                wdwNewLicense.setDraggable(true);
                wdwNewLicense.setResizable(true);
                wdwNewLicense.setWidth("40%");
                
                if (pool != null) {
                    TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
                    txtName.setRequiredIndicatorVisible(true);
                    txtName.setWidthFull();
                    
                    ComboBox<ClassMetadataLight> cmbLicenseTypes = new ComboBox<>(ts.getTranslatedString("module.softman.filter-license.choose-license-type"));
                    cmbLicenseTypes.setRequiredIndicatorVisible(true);
                    cmbLicenseTypes.setWidthFull();
                    ClassMetadata poolClass = mem.getClass(pool.getClassName());
                    if (poolClass.getName().equals(Constants.CLASS_GENERICSOFTWAREASSET) || poolClass.isAbstract())
                        cmbLicenseTypes.setItems(sms.getAllLicenseTypes());
                    else {
                        cmbLicenseTypes.setLabel(ts.getTranslatedString("module.softman.label-license-type"));
                        cmbLicenseTypes.setItems(poolClass);
                        cmbLicenseTypes.setValue(poolClass);
                        cmbLicenseTypes.setReadOnly(true);
                    }

                    ComboBox<BusinessObjectLight> cmbProducts = new ComboBox<>(ts.getTranslatedString("module.softman.filter-license.choose-product"));
                    cmbProducts.setRequiredIndicatorVisible(true);
                    cmbProducts.setItems(sms.getAllProducts());
                    cmbProducts.setRenderer(new TextRenderer<>(BusinessObjectLight::getName));
                    cmbProducts.setWidthFull();

                    wdwNewLicense.getBtnConfirm().addClickListener(event -> {
                        try {
                            newLicenseAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>(EActionParameter.LICENSE_POOL.getPropertyValue(), pool.getId()),
                                    new ModuleActionParameter<>(EActionParameter.LICENSE_NAME.getPropertyValue(), txtName.getValue()),
                                    new ModuleActionParameter<>(EActionParameter.LICENSE_TYPE.getPropertyValue(), cmbLicenseTypes.getValue().getName()),
                                    new ModuleActionParameter<>(EActionParameter.LICENSE_PRODUCT.getPropertyValue(), cmbProducts.getValue().getId())));

                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                    ts.getTranslatedString("module.softman.actions.new-license.success"), NewLicenseVisualAction.class));
                            wdwNewLicense.close();

                            if (parameters.containsKey(EActionParameter.ADD_LICENSE.getPropertyValue())) {
                                Command addLicense = (Command) parameters.get(EActionParameter.ADD_LICENSE.getPropertyValue());
                                addLicense.execute();
                            }
                        } catch (ModuleActionException ex) {
                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                    ex.getMessage(), NewLicenseVisualAction.class));
                        }
                    });
                    wdwNewLicense.getBtnConfirm().setEnabled(false);
                    
                    txtName.addValueChangeListener(listener
                            -> wdwNewLicense.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty()
                                    && cmbLicenseTypes.getValue() != null && cmbProducts.getValue() != null));

                    cmbLicenseTypes.addValueChangeListener(listener
                            -> wdwNewLicense.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty()
                                    && cmbLicenseTypes.getValue() != null && cmbProducts.getValue() != null));

                    cmbProducts.addValueChangeListener(listener
                            -> wdwNewLicense.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty()
                                    && cmbLicenseTypes.getValue() != null && cmbProducts.getValue() != null));

                    wdwNewLicense.setContent(txtName, cmbLicenseTypes, cmbProducts);
                }
                return wdwNewLicense;
            } else
                return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"),
                        EActionParameter.POOL.getPropertyValue())));
        } catch (MetadataObjectNotFoundException ex) {
            return new Dialog(new Label(ex.getMessage()));
        }
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newLicenseAction;
    }   
}