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
package com.neotropic.kuwaiba.modules.commercial.whman.actions;

import com.neotropic.kuwaiba.modules.commercial.whman.WarehousesManagerModule;
import com.neotropic.kuwaiba.modules.commercial.whman.persistence.WarehousesService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UI of move object to spare pool action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class MoveObjectToWarehouseVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Warehouses Services
     */
    @Autowired
    private WarehousesService ws;
    /**
     * Reference to the underlying action
     */
    @Autowired
    private MoveObjectToWarehouseAction moveObjectToWarehouseVisualAction;
    /**
     * The visual action to create a new warehouse
     */
    @Autowired
    private NewWarehouseVisualAction newWarehouseVisualAction;
    /**
     * The visual action to create a new spare pool
     */
    @Autowired
    private NewSparePoolVisualAction newSparePoolVisualAction;
    /**
     * Parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Parameter pool.
     */
    public static String PARAM_POOL = "pool"; //NOI18N
    /**
     * ComboBox for select a target warehouse.
     */
    private ComboBox<BusinessObjectLight> cmbWarehouse;
    /**
     * ComboBox for select a target spare pool.
     */
    private ComboBox<InventoryObjectPool> cmbSparePool;
    /**
     * Current object.
     */
    private BusinessObjectLight businessObject;
    /**
     * Dialog to copy an inventory object.
     */
    private ConfirmDialog wdwMove;
    /**
     * Button to add a new warehouse.
     */
    private ActionButton btnAddWarehouse;
    /**
     * Button to add a new spare pool.
     */
    private ActionButton btnAddSparePool;
    /**
     * Label to show information when there are no available warehouses.
     */
    private Label lblInfoWarehouse;
    /**
     * Label to show information when there are no  available pools.
     */
    private Label lblInfoSparePool;
    /**
     * Layout for warehouse.
     */
    private HorizontalLayout lytWarehouse;
    /**
     * Layout for sparePool.
     */
    private HorizontalLayout lytSparePool;
    /**
     * Layout for dialog content.
     */
    private VerticalLayout lytContent;
    /**
     * Save the current warehouse.
     */
    private BusinessObjectLight currentWarehouse;
    
    public MoveObjectToWarehouseVisualAction() {
        super(WarehousesManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey(PARAM_BUSINESS_OBJECT)) {
            businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
            try {
                wdwMove = new ConfirmDialog(ts,
                        String.format(ts.getTranslatedString("module.whman.actions.move-object-to-warehouse.header"), businessObject.toString()),
                        ts.getTranslatedString("module.general.labels.move"));
                wdwMove.setDraggable(true);
                wdwMove.setResizable(true);

                lblInfoWarehouse = new Label(ts.getTranslatedString("module.whman.label-no-warehouses-available"));
                lblInfoWarehouse.setClassName("whman-lbl-no-warehouse");
                lblInfoWarehouse.setWidthFull();
                
                lblInfoSparePool = new Label(ts.getTranslatedString("module.whman.label-no-spare-pools-available"));
                lblInfoSparePool.setClassName("whman-lbl-no-warehouse");
                lblInfoSparePool.setWidthFull();
                
                Command newWarehouse = () -> refreshWarehouses();
                btnAddWarehouse = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.newWarehouseVisualAction.getModuleAction().getDisplayName());
                btnAddWarehouse.addClickListener(event -> {
                    this.newWarehouseVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("pool", null),
                            new ModuleActionParameter("commandClose", newWarehouse)
                    )).open();
                });
                btnAddWarehouse.setHeight("32px");        
                
                Command newSparePool = () -> refreshSparePools();
                btnAddSparePool = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), this.newSparePoolVisualAction.getModuleAction().getDisplayName());
                btnAddSparePool.addClickListener(event -> {
                    this.newSparePoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("warehouse", currentWarehouse),
                            new ModuleActionParameter("commandClose", newSparePool)
                    )).open();
                });
                btnAddSparePool.setEnabled(false);
                btnAddSparePool.setHeight("32px");
                
                List<InventoryObjectPool> rootPools = ws.getWarehouseRootPools();
                List<BusinessObjectLight> warehousePools = new ArrayList<>();           
                if (!rootPools.isEmpty()) {
                    for (InventoryObjectPool rootPool : rootPools) {
                        List<BusinessObjectLight> warehouses = ws.getWarehousesInPool(rootPool.getId(), -1);
                        if (!warehouses.isEmpty())
                            warehouses.forEach(warehouse -> warehousePools.add(warehouse));
                    }
                }
                
                cmbWarehouse = new ComboBox<>(ts.getTranslatedString("module.whman.label-warehouse-name"));
                cmbWarehouse.setRequiredIndicatorVisible(true);
                cmbWarehouse.setAllowCustomValue(false);
                cmbWarehouse.setItems(warehousePools);
                cmbWarehouse.setWidthFull();
                
                cmbSparePool = new ComboBox<>(ts.getTranslatedString("module.whman.label-spare-name"));
                cmbSparePool.setRequiredIndicatorVisible(true);
                cmbSparePool.setAllowCustomValue(false);
                cmbSparePool.setEnabled(false);
                cmbSparePool.setWidthFull();
                       
                lytContent = new VerticalLayout();
                lytContent.setWidthFull();
                lytContent.setHeightFull();
                
                lytWarehouse = new HorizontalLayout();
                lytSparePool = new HorizontalLayout();
                
                if (!warehousePools.isEmpty()) {
                    lytWarehouse = new HorizontalLayout(cmbWarehouse, btnAddWarehouse);
                    lytWarehouse.setSizeFull();
                    lytWarehouse.setSpacing(true);
                    
                    wdwMove.setWidth("60%");
                } else {
                    lytWarehouse = new HorizontalLayout(lblInfoWarehouse, btnAddWarehouse);
                    lytWarehouse.setSizeFull();
                    lytWarehouse.setSpacing(true);
        
                    wdwMove.setWidth("30%");
                }
                lytWarehouse.setAlignSelf(FlexComponent.Alignment.END, btnAddWarehouse);                
                lytContent.add(lytWarehouse, lytSparePool);
                
                cmbWarehouse.addValueChangeListener(event -> {
                    if (cmbWarehouse.getValue() != null) {
                        currentWarehouse = event.getValue();
                        lytContent.remove(lytSparePool);
                        try {
                            List<InventoryObjectPool> sparePools = ws.getPoolsInWarehouse(cmbWarehouse.getValue().getClassName(), cmbWarehouse.getValue().getId());
                            List<InventoryObjectPool> availableSparePools = new ArrayList<>();
                            if (!sparePools.isEmpty()) {
                                for (InventoryObjectPool sparePool : sparePools) {
                                    if (mem.isSubclassOf(sparePool.getClassName(), businessObject.getClassName()))
                                        availableSparePools.add(sparePool);
                                }
                            }
                            cmbSparePool.setItems(availableSparePools);
                            cmbSparePool.setEnabled(true);
                            
                            if (!availableSparePools.isEmpty()) {
                                lytSparePool = new HorizontalLayout(cmbSparePool, btnAddSparePool);
                                lytSparePool.setSizeFull();
                                lytSparePool.setSpacing(true);
                            } else {
                                lytSparePool = new HorizontalLayout(lblInfoSparePool, btnAddSparePool);
                                lytSparePool.setSizeFull();
                                lytSparePool.setSpacing(true);
                            }
                            
                            btnAddSparePool.setEnabled(true);                            
                            lytSparePool.setAlignSelf(FlexComponent.Alignment.END, btnAddSparePool);     
                            lytContent.add(lytSparePool);
                        } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    } else {
                        cmbSparePool.clear();
                        cmbSparePool.setEnabled(false);
                        lytSparePool.setVisible(false);
                    }
                });
                
                wdwMove.getBtnConfirm().addClickListener(event -> {
                    try {
                        ActionResponse actionResponse = moveObjectToWarehouseVisualAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter(PARAM_BUSINESS_OBJECT, businessObject),
                                new ModuleActionParameter(PARAM_POOL, cmbSparePool.getValue())
                        ));
                        
                        actionResponse.put(ActionResponse.ActionType.MOVE, "");
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                String.format(ts.getTranslatedString("module.whman.actions.move-object-to-warehouse.success")
                                        , businessObject.toString(), cmbSparePool.getValue()),
                                CopyObjectToWarehouseAction.class,
                                actionResponse
                        ));
                        wdwMove.close();
                    } catch (ModuleActionException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                });
                wdwMove.getBtnConfirm().setEnabled(false);
                cmbSparePool.addValueChangeListener(event -> wdwMove.getBtnConfirm().setEnabled(cmbSparePool.getValue() != null));
  
                wdwMove.setContent(lytContent);
                return wdwMove;
            } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_BUSINESS_OBJECT)));
        return null;
    }
    
    private void refreshWarehouses() {
        try {
            List<InventoryObjectPool> rootPools = ws.getWarehouseRootPools();
            List<BusinessObjectLight> warehousePools = new ArrayList<>();
            if (!rootPools.isEmpty()) {
                for (InventoryObjectPool rootPool : rootPools) {
                    List<BusinessObjectLight> warehouses = ws.getWarehousesInPool(rootPool.getId(), -1);
                    if (!warehouses.isEmpty())
                        warehouses.forEach(warehouse -> warehousePools.add(warehouse));
                }
            }
            cmbWarehouse.setItems(warehousePools);
            cmbWarehouse.clear();
            
            lytContent.removeAll();
            if (!warehousePools.isEmpty()) {
                lytWarehouse = new HorizontalLayout(cmbWarehouse, btnAddWarehouse);
                lytWarehouse.setSizeFull();
                lytWarehouse.setSpacing(true);
                
                wdwMove.setWidth("60%");
            } else {
                lytWarehouse = new HorizontalLayout(lblInfoWarehouse, btnAddWarehouse);
                lytWarehouse.setSizeFull();
                lytWarehouse.setSpacing(true);

                wdwMove.setWidth("30%");
            }
            lytWarehouse.setAlignSelf(FlexComponent.Alignment.END, btnAddWarehouse);
            lytContent.add(lytWarehouse, lytSparePool);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }  
    }
    
    private void refreshSparePools() {
        if (cmbWarehouse.getValue() != null) {
            currentWarehouse = cmbWarehouse.getValue();
            lytContent.remove(lytSparePool);
            try {
                List<InventoryObjectPool> sparePools = ws.getPoolsInWarehouse(cmbWarehouse.getValue().getClassName(), cmbWarehouse.getValue().getId());
                List<InventoryObjectPool> availableSparePools = new ArrayList<>();
                if (!sparePools.isEmpty()) {
                    for (InventoryObjectPool sparePool : sparePools) {
                        if (mem.isSubclassOf(sparePool.getClassName(), businessObject.getClassName())) {
                            availableSparePools.add(sparePool);
                        }
                    }
                }
                cmbSparePool.setItems(availableSparePools);
                cmbSparePool.clear();
                cmbSparePool.setEnabled(true);

                if (!availableSparePools.isEmpty()) {
                    lytSparePool = new HorizontalLayout(cmbSparePool, btnAddSparePool);
                    lytSparePool.setSizeFull();
                    lytSparePool.setSpacing(true);
                } else {
                    lytSparePool = new HorizontalLayout(lblInfoSparePool, btnAddSparePool);
                    lytSparePool.setSizeFull();
                    lytSparePool.setSpacing(true);
                }

                btnAddSparePool.setEnabled(true);
                lytSparePool.setAlignSelf(FlexComponent.Alignment.END, btnAddSparePool);
                lytContent.add(lytSparePool);
            } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else {
            cmbSparePool.clear();
            cmbSparePool.setEnabled(false);
            lytSparePool.setVisible(false);
        }
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return moveObjectToWarehouseVisualAction;
    }
    
    @Override
    public String appliesTo() {
        return Constants.CLASS_INVENTORYOBJECT;
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}