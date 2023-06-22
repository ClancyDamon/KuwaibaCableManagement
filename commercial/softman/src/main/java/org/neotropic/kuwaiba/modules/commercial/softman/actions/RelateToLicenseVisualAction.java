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
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerModule;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.selectors.ObjectRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Visual wrapper of relate object action.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class RelateToLicenseVisualAction extends AbstractVisualAdvancedAction {
    /**
     *  The selected object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     *  The selected license to relate the object to.
     */
    public static String PARAM_LICENSE = "license"; //NOI18N
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Contract Manager Service
    */
    @Autowired
    private SoftwareManagerService sms;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private RelateToLicenseAction relateToLicenseAction;
    
    public RelateToLicenseVisualAction() {
        super(SoftwareManagerModule.MODULE_ID);
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey(PARAM_BUSINESS_OBJECT)) {
            BusinessObjectLight selectedObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
            try {
                ConfirmDialog wdwRelateToLicense = new ConfirmDialog(ts, 
                ts.getTranslatedString(ts.getTranslatedString("module.softman.actions.relate-to-license.name")),
                ts.getTranslatedString("module.general.messages.relate"));
                wdwRelateToLicense.setDraggable(true);
                wdwRelateToLicense.setResizable(true);
                wdwRelateToLicense.setWidth("40%");
                
                ComboBox<InventoryObjectPool> cmbPools = new ComboBox<>(ts.getTranslatedString("module.softman.filter-license-pool.choose-license-pool"));
                cmbPools.setRequiredIndicatorVisible(true);
                cmbPools.setItems(sms.getLicensePools());
                cmbPools.setWidthFull();
                
                ComboBox<BusinessObjectLight> cmbLicenses = new ComboBox<>(ts.getTranslatedString("module.softman.filter-license.choose-license"));
                cmbLicenses.setRequiredIndicatorVisible(true);
                cmbLicenses.setRenderer(new ObjectRenderer(mem, ts));
                cmbLicenses.setEnabled(false);
                cmbLicenses.setWidthFull();
                
                wdwRelateToLicense.getBtnConfirm().addClickListener(event -> {
                    try {
                        relateToLicenseAction.getCallback().execute(new ModuleActionParameterSet(
                                        new ModuleActionParameter<>(PARAM_BUSINESS_OBJECT, selectedObject),
                                        new ModuleActionParameter<>(PARAM_LICENSE, cmbLicenses.getValue())));
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            String.format(ts.getTranslatedString("module.softman.actions.relate-to-license.success"), cmbLicenses.getValue(), selectedObject), 
                                RelateToLicenseVisualAction.class));
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), RelateToLicenseVisualAction.class));
                    }
                    wdwRelateToLicense.close();
                });
                wdwRelateToLicense.setEnableBtnConfirm(false);

                cmbPools.addValueChangeListener(listener -> {
                    if(cmbPools.getValue() != null) {
                        cmbLicenses.setEnabled(cmbPools.getValue() != null);
                        try {
                            List<BusinessObjectLight> licensesInPool = sms.getLicensesInPool(cmbPools.getValue().getId(), -1);
                            cmbLicenses.setItems(licensesInPool);
                        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                            cmbLicenses.setEnabled(false);
                            cmbLicenses.clear();
                            new Dialog(new Label(ex.getMessage()));
                        }
                        wdwRelateToLicense.setEnableBtnConfirm(cmbPools.getValue() != null && cmbLicenses.getValue() != null);
                    } else {
                        cmbLicenses.setEnabled(false);
                        cmbLicenses.clear();
                        wdwRelateToLicense.setEnableBtnConfirm(false);
                    }
                });
                
                cmbLicenses.addValueChangeListener(listener -> wdwRelateToLicense.setEnableBtnConfirm(listener.getValue() != null));
                
                wdwRelateToLicense.setContent(cmbPools, cmbLicenses);
                return wdwRelateToLicense;
            } catch (InvalidArgumentException ex) {
                return new Dialog(new Label(ex.getMessage()));
            }
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
    }

    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICCOMMUNICATIONSELEMENT;
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return relateToLicenseAction;
    }   
}