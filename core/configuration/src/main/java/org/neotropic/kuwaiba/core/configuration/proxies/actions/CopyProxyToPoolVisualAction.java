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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.Command;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.configuration.proxies.ProxyManagerModule;
import org.neotropic.kuwaiba.core.configuration.proxies.ProxyManagerService;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of copy proxy to pool action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class CopyProxyToPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Proxy Manager Service
     */
    @Autowired
    private ProxyManagerService pms;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private CopyProxyToPoolAction copyProxyToPoolAction;
    /**
     * The visual action to create a new proxies pool
     */
    @Autowired
    private NewProxiesPoolVisualAction newProxiesPoolVisualAction;
    /**
     * Parameter pool
     */
    public static String PARAM_POOL = "pool";
    /**
     * Parameter proxy
     */
    public static String PARAM_PROXY = "proxy";
    /**
     * Parameter Command
     */
    private static final String PARAM_COMMAND_ADD = "commandAdd";
    private static final String PARAM_COMMAND_CLOSE = "commandClose";
    /**
     * Command to add pool from main UI
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

    public CopyProxyToPoolVisualAction() {
        super(ProxyManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey(PARAM_PROXY)) {
            BusinessObjectLight proxy = (BusinessObjectLight) parameters.get(PARAM_PROXY);
            wdwCopy = new ConfirmDialog(ts,
                    this.copyProxyToPoolAction.getDisplayName(),
                    ts.getTranslatedString("module.general.labels.copy")
            );
            wdwCopy.setDraggable(true);
            wdwCopy.setResizable(true);

            lblInfo = new Label(ts.getTranslatedString("module.configman.proxies.label.no-pools-available"));
            lblInfo.setClassName("proxies-lbl-no-pools");
            lblInfo.setWidthFull();
            
            addPoolUI = (Command) parameters.get("commandAddProxyPool");
            Command addPool = () -> refreshPool();
            btnAddPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.newProxiesPoolVisualAction.getModuleAction().getDisplayName());
            btnAddPool.addClickListener(event -> {
                this.newProxiesPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(PARAM_COMMAND_ADD, addPool),
                        new ModuleActionParameter(PARAM_COMMAND_CLOSE, addPoolUI)
                )).open();
            });
            btnAddPool.setHeight("32px");   
            
            List<InventoryObjectPool> listPool = pms.getProxyPools();
            cmbPool = new ComboBox<>(ts.getTranslatedString("module.configman.proxies.label.pool"), listPool);
            cmbPool.setAllowCustomValue(false);
            cmbPool.setRequiredIndicatorVisible(true);
            cmbPool.setWidthFull();
            
            if (!listPool.isEmpty()) {
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
                    copyProxyToPoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(PARAM_PROXY, proxy),
                            new ModuleActionParameter<>(PARAM_POOL, cmbPool.getValue())
                    ));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.configman.proxies.actions.copy-proxy-to-pool.success"), MoveProxyToPoolAction.class));
                    wdwCopy.close();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), MoveProxyToPoolAction.class));
                }
            });
            wdwCopy.getBtnConfirm().setEnabled(false);
            cmbPool.addValueChangeListener(event -> wdwCopy.getBtnConfirm().setEnabled(cmbPool.getValue() != null));

            return wdwCopy;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_PROXY)));
    }
    
    private void refreshPool() {
        List<InventoryObjectPool> listPool = pms.getProxyPools();
        cmbPool.setItems(listPool);
        cmbPool.clear();
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return copyProxyToPoolAction;
    }
}