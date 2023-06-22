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
package org.neotropic.kuwaiba.modules.optional.pools.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.pools.PoolsModule;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UI of move business object to pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class MoveBusinessObjectToPoolVisualAction extends AbstractVisualInventoryAction implements ActionCompletedListener {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private MoveBusinessObjectToPoolAction moveBusinessObjectToPoolAction;
    /**
     * Reference to the action that adds a new pool.
     */
    @Autowired
    private NewPoolVisualAction NewPoolVisualAction;
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
     * Parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Parameter pool.
     */
    private static final String PARAM_POOL = "pool"; //NOI18N
    /**
     * ComboBox for select a target pool.
     */
    private ComboBox<InventoryObjectPool> cmbPool;
    /**
     * Current object.
     */
    private BusinessObjectLight businessObject;
    /**
     * Dialog to move an inventory object.
     */
    private ConfirmDialog wdwMove;
    /**
     * Button to add a new pool.
     */
    private ActionButton btnAddPool;
    /**
     * Label to show information when there are no  available pools.
     */
    private Label lblInfo;
    
    public MoveBusinessObjectToPoolVisualAction() {
        super(PoolsModule.MODULE_ID);
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        refreshPools();
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        this.NewPoolVisualAction.registerActionCompletedLister(this);
        
        if (parameters.containsKey(PARAM_BUSINESS_OBJECT)) {
            try {
                businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
                
                wdwMove = new ConfirmDialog(ts
                        , String.format(ts.getTranslatedString("module.pools.actions.move-business-object-to-pool.header"), businessObject.toString())
                        , ts.getTranslatedString("module.general.labels.move"));
                wdwMove.setDraggable(true);
                wdwMove.setResizable(true);
                
                lblInfo = new Label(ts.getTranslatedString("module.pools.label.no-pools-available-for-object"));
                lblInfo.setClassName("pools-lbl-no-pools");
                lblInfo.setWidthFull();
                
                btnAddPool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.NewPoolVisualAction.getModuleAction().getDisplayName());
                btnAddPool.addClickListener(event -> this.NewPoolVisualAction.getVisualComponent(new ModuleActionParameterSet()).open());
                btnAddPool.setHeight("32px");
                
                List<InventoryObjectPool> pools = bem.getRootPools(null, ApplicationEntityManager.POOL_TYPE_GENERAL_PURPOSE, true);
                List<InventoryObjectPool> availablePools = new ArrayList<>();
                for (InventoryObjectPool pool : pools) {
                    if (mem.isSubclassOf(pool.getClassName(), businessObject.getClassName()))
                        availablePools.add(pool);
                }
                
                cmbPool = new ComboBox<>(ts.getTranslatedString("module.general.labels.pools"), availablePools);
                cmbPool.setAllowCustomValue(false);
                cmbPool.setRequiredIndicatorVisible(true);
                cmbPool.setWidthFull();
                
                wdwMove.getBtnConfirm().addClickListener(event -> {
                    try {
                        ActionResponse actionResponse = moveBusinessObjectToPoolAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter(PARAM_BUSINESS_OBJECT, businessObject),
                                new ModuleActionParameter(PARAM_POOL, cmbPool.getValue())
                        ));
                        
                        actionResponse.put(ActionResponse.ActionType.MOVE, "");
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.pools.actions.move-business-object-to-pool.success"),
                                MoveBusinessObjectToPoolAction.class,
                                actionResponse
                        ));
                        wdwMove.close();
                    } catch (ModuleActionException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                });
                wdwMove.getBtnConfirm().setEnabled(false);
                cmbPool.addValueChangeListener(event -> wdwMove.getBtnConfirm().setEnabled(cmbPool.getValue() != null));
                
                if (!availablePools.isEmpty()) {
                    HorizontalLayout lytPools = new HorizontalLayout(cmbPool, btnAddPool);
                    lytPools.setAlignSelf(FlexComponent.Alignment.END, btnAddPool);
                    lytPools.setSizeFull();
                    lytPools.setSpacing(true);
                    
                    wdwMove.setContent(lytPools);
                    wdwMove.setWidth("60%");
                } else {
                    HorizontalLayout lytInfo = new HorizontalLayout(lblInfo, btnAddPool);
                    lytInfo.setAlignSelf(FlexComponent.Alignment.END, btnAddPool);
                    lytInfo.setSizeFull();
                    lytInfo.setSpacing(true);
                    
                    wdwMove.setContent(lytInfo);  
                    wdwMove.setWidth("30%");
                }
                    
                return wdwMove;
            } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_BUSINESS_OBJECT)));
        return null;
    }

    private void refreshPools() {
        try {          
            List<InventoryObjectPool> pools = bem.getRootPools(null, ApplicationEntityManager.POOL_TYPE_GENERAL_PURPOSE, true);
            List<InventoryObjectPool> availablePools = new ArrayList<>();
            for (InventoryObjectPool pool : pools) {
                if (mem.isSubclassOf(pool.getClassName(), businessObject.getClassName()))
                    availablePools.add(pool);
            }

            cmbPool.setItems(availablePools);
            cmbPool.clear();
            
            if (!availablePools.isEmpty()) {
                HorizontalLayout lytPools = new HorizontalLayout(cmbPool, btnAddPool);
                lytPools.setSizeFull();
                lytPools.setSpacing(true);

                wdwMove.setContent(lytPools);
                wdwMove.setWidth("60%");
            } else {
                HorizontalLayout lytInfo = new HorizontalLayout(lblInfo, btnAddPool);
                lytInfo.setAlignSelf(FlexComponent.Alignment.END, btnAddPool);
                lytInfo.setSizeFull();
                lytInfo.setSpacing(true);

                wdwMove.setContent(lytInfo);
                wdwMove.setWidth("30%");
            }
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }  
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return moveBusinessObjectToPoolAction;
    }
}