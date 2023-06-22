/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.contractman.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.contractman.ContractManagerModule;
import org.neotropic.kuwaiba.modules.commercial.contractman.ContractManagerService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of copy contract to pool action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class CopyContractToPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the contract service.
     */
    @Autowired
    private ContractManagerService cms;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private CopyContractToPoolAction copyContractToPoolAction;
    /**
     * The visual action to create a new contracts pool.
     */
    @Autowired
    private NewContractsPoolVisualAction newContractsPoolVisualAction;
    /**
     * Parameter pool.
     */
    public static String PARAM_POOL= "pool";
    /**
     * Parameter contract.
     */
    public static String PARAM_CONTRACT = "contract";
    /**
     * Parameter command.
     */
    public static String PARAM_COMMAND = "command";
    public static final String PARAM_COMMAND_ADD = "commandAdd";
    public static final String PARAM_COMMAND_CLOSE = "commandClose";
    /**
     * Command to add pool from main UI.
     */
    private Command addPoolUI;
    /**
     *  Button to add a new pool.
     */
    private ActionButton btnAddPool;
    /**
     * ComboBox for select a target pool.
     */
    private ComboBox<InventoryObjectPool> cmbPool;
    /**
     * Dialog to copy an inventory object.
     */
    private ConfirmDialog wdwCopy;
    /**
     * Label to show information when there are no available pools.
     */
    private Label lblInfo;
    /**
     * Current contract.
     */
    private BusinessObjectLight contract;
    
    public CopyContractToPoolVisualAction() {
        super(ContractManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            if (parameters.containsKey(PARAM_CONTRACT)) {
                contract = (BusinessObjectLight) parameters.get(PARAM_CONTRACT);
                wdwCopy = new ConfirmDialog(ts,
                        this.copyContractToPoolAction.getDisplayName(),
                        ts.getTranslatedString("module.general.labels.copy"));
                wdwCopy.setDraggable(true);
                wdwCopy.setResizable(true);

                lblInfo = new Label(ts.getTranslatedString("module.contractman.pool.label.no-pools-available"));
                lblInfo.setClassName("projects-lbl-no-pools");
                lblInfo.setWidthFull();

                addPoolUI = (Command) parameters.get("commandAddContractPool");
                Command addPool = () -> refreshPool();
                btnAddPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.newContractsPoolVisualAction.getModuleAction().getDisplayName());
                btnAddPool.addClickListener(event -> {
                    this.newContractsPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(PARAM_COMMAND_ADD, addPool),
                            new ModuleActionParameter(PARAM_COMMAND_CLOSE, addPoolUI)
                    )).open();
                });
                btnAddPool.setHeight("32px");
                
                List<InventoryObjectPool> pools = cms.getContractPools();
                List<InventoryObjectPool> availablePools = new ArrayList<>();
                if (!pools.isEmpty()) {
                    pools.stream().filter(pool -> (pool.getClassName().equals(contract.getClassName())
                            || pool.getClassName().equals(Constants.CLASS_GENERICCONTRACT)))
                            .forEachOrdered(pool -> {
                                availablePools.add(pool);
                            });
                }
                
                cmbPool = new ComboBox<>(ts.getTranslatedString("module.contractman.pool.header"), availablePools);
                cmbPool.setAllowCustomValue(false);
                cmbPool.setRequiredIndicatorVisible(true);
                cmbPool.setWidthFull();
                
                if (!availablePools.isEmpty()) {
                    HorizontalLayout lytPools = new HorizontalLayout(cmbPool, btnAddPool);
                    lytPools.setAlignSelf(FlexComponent.Alignment.END, btnAddPool);
                    lytPools.setSizeFull();
                    lytPools.setSpacing(true);

                    wdwCopy.setContent(lytPools);
                    wdwCopy.setWidth("60%");
                } else {
                    HorizontalLayout lytInfo = new HorizontalLayout(lblInfo, btnAddPool);
                    lytInfo.setAlignSelf(FlexComponent.Alignment.END, btnAddPool);
                    lytInfo.setSizeFull();
                    lytInfo.setSpacing(true);

                    wdwCopy.setContent(lytInfo);
                    wdwCopy.setWidth("30%");
                }
                
                wdwCopy.getBtnConfirm().addClickListener(event -> {
                    try {
                        copyContractToPoolAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>(PARAM_CONTRACT, contract),     
                                new ModuleActionParameter<>(PARAM_POOL, cmbPool.getValue())));
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.contractman.actions.copy-contract-to-pool.success"), CopyContractToPoolAction.class));
                        wdwCopy.close();
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), CopyContractToPoolAction.class));
                    }
                });
                wdwCopy.getBtnConfirm().setEnabled(false);
                cmbPool.addValueChangeListener(event -> wdwCopy.getBtnConfirm().setEnabled(cmbPool.getValue() != null));
                
                return wdwCopy;
            } else
                return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_CONTRACT)));
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return null;
    }
    
    private void refreshPool() {
        try {
            List<InventoryObjectPool> pools = cms.getContractPools();
            List<InventoryObjectPool> availablePools = new ArrayList<>();
            if (!pools.isEmpty()) {
                pools.stream().filter(pool -> (pool.getClassName().equals(contract.getClassName())
                        || pool.getClassName().equals(Constants.CLASS_GENERICCONTRACT)))
                        .forEachOrdered(pool -> {
                            availablePools.add(pool);
                        });
            }
            cmbPool.setItems(availablePools);
            cmbPool.clear();
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    @Override
    public AbstractAction getModuleAction() {
        return copyContractToPoolAction;
    }
}