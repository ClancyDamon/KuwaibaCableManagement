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
package org.neotropic.kuwaiba.core.configuration.proxies.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.proxies.ProxyManagerModule;
import org.neotropic.kuwaiba.core.configuration.proxies.ProxyManagerService;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new proxy action.
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProxyVisualAction extends AbstractVisualAction<Dialog> {

    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the Proxy Manager Service
     */
    @Autowired
    private ProxyManagerService pms;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewProxyAction newProxyAction;

    public NewProxyVisualAction() {
        super(ProxyManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        // Dialog to create a new proxy
        ConfirmDialog wdwNewProxy = new ConfirmDialog(ts,
                 this.newProxyAction.getDisplayName(),
                 ts.getTranslatedString("module.general.labels.create"));
        try {
            InventoryObjectPool selectedPool = null;
            if (parameters.containsKey("pool")) {
                selectedPool = (InventoryObjectPool) parameters.get("pool");
            }

            List<InventoryObjectPool> listPool = pms.getProxyPools();
            ComboBox<InventoryObjectPool> cmbPool = new ComboBox<>(ts.getTranslatedString("module.configman.proxies.label.pool"), listPool);
            cmbPool.setAllowCustomValue(false);
            cmbPool.setRequiredIndicatorVisible(true);
            cmbPool.setSizeFull();

            List<ClassMetadataLight> listClass = mem.getSubClassesLight(Constants.CLASS_GENERICPROXY, false, false);
            ComboBox<ClassMetadataLight> cmbClasses = new ComboBox<>(ts.getTranslatedString("module.configman.proxies.label.class"), listClass);
            cmbClasses.setWidthFull();
            cmbClasses.setAllowCustomValue(false);
            cmbClasses.setRequired(true);
            cmbClasses.setRequiredIndicatorVisible(true);

            // Proxy pool selected if exists
            if (selectedPool != null) {
                cmbPool.setValue(selectedPool);
                cmbPool.setAllowCustomValue(false);
            }

            TextField txtName = new TextField(ts.getTranslatedString("module.configman.proxies.label.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();

            wdwNewProxy.getBtnConfirm().addClickListener((event) -> {
                if (cmbPool.getValue() == null) {
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                } else {
                    try {
                        newProxyAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("pool", cmbPool.getValue()),
                                new ModuleActionParameter<>("class", cmbClasses.getValue()),
                                new ModuleActionParameter<>("name", txtName.getValue())
                        ));
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.configman.proxies.actions.new-proxy.succes"), NewProxyAction.class));

                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), NewProxyAction.class));
                    }
                    wdwNewProxy.close();
                }
            });

            wdwNewProxy.getBtnConfirm().setEnabled(false);
            txtName.addValueChangeListener((event) -> {
                wdwNewProxy.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbPool.isEmpty() && !cmbClasses.isEmpty());
            });

            cmbPool.addValueChangeListener((event) -> {
                wdwNewProxy.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbPool.isEmpty() && !cmbClasses.isEmpty());
            });

            cmbClasses.addValueChangeListener((event) -> {
                wdwNewProxy.getBtnConfirm().setEnabled(!txtName.isEmpty() && !cmbPool.isEmpty() && !cmbClasses.isEmpty());
            });

            wdwNewProxy.setContent(cmbPool, cmbClasses, txtName);
        } catch (MetadataObjectNotFoundException  ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return wdwNewProxy;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newProxyAction;
    }
}