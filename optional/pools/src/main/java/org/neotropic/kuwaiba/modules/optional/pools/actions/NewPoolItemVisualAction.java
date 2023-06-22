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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.pools.PoolsModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.layout.MandatoryAttributesLayout;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new pool item action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewPoolItemVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewPoolItemAction newPoolItemAction;
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
    /**
     * Parameter pool.
     */
    public static String PARAMETER_POOL = "pool";
    /**
     * Parameter pool item.
     */
    public static String PARAMETER_POOL_ITEM = "poolItem";
    /**
     * Dialog to add new pool item.
     */
    private ConfirmDialog wdwNewPoolItem;
    
    public NewPoolItemVisualAction() {
        super(PoolsModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            HashMap<String, String> attributes = new HashMap();
            
            InventoryObjectPool pool = (InventoryObjectPool) parameters.get(PARAMETER_POOL);

            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setValueChangeMode(ValueChangeMode.EAGER);

            List<ClassMetadataLight> lstClasses = mem.getSubClassesLight(pool.getClassName(), false, true);

            ComboBox<ClassMetadataLight> cmbClassType = new ComboBox<>(ts.getTranslatedString("module.general.labels.class-name"));
            cmbClassType.setItems(lstClasses);
            cmbClassType.setClearButtonVisible(true);
            cmbClassType.setRequiredIndicatorVisible(true);
            cmbClassType.setAllowCustomValue(false);
            cmbClassType.setItemLabelGenerator(ClassMetadataLight::getName);

            wdwNewPoolItem = new ConfirmDialog(ts,
                     this.newPoolItemAction.getDisplayName(),
                     ts.getTranslatedString("module.general.labels.create"));
            wdwNewPoolItem.setDraggable(true);
            wdwNewPoolItem.setResizable(true);

            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            wdwNewPoolItem.getBtnConfirm().addClickListener(event -> {
                try {
                    attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                    
                    if (cmbClassType.getValue() == null || (txtName.getValue() == null && txtName.isEmpty()))
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                        ActionResponse actionResponse = newPoolItemAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>(Constants.PROPERTY_ATTRIBUTES, attributes),
                                new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, cmbClassType.getValue().getName()),
                                new ModuleActionParameter<>(PARAMETER_POOL, pool)));

                        actionResponse.put(ActionResponse.ActionType.ADD, "");
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString(ts.getTranslatedString("module.pools.actions.new-pool-item-created"))
                                , NewPoolItemAction.class, actionResponse));
                        
                        if (parameters.containsKey(PARAMETER_POOL_ITEM)) {
                                Command addPoolItem = (Command) parameters.get(PARAMETER_POOL_ITEM);
                                addPoolItem.execute();
                        }
                        
                        wdwNewPoolItem.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewPoolAction.class));
                }
            });
            wdwNewPoolItem.getBtnConfirm().setEnabled(false);

            txtName.addValueChangeListener(e -> wdwNewPoolItem.getBtnConfirm().setEnabled(!txtName.isEmpty() && cmbClassType.getValue() != null));
            
            HorizontalLayout lytExtraFields = new HorizontalLayout();
            lytExtraFields.setWidthFull();
            
            cmbClassType.addValueChangeListener(valueChangeEvent -> {
                wdwNewPoolItem.getBtnConfirm().setEnabled(false);
                lytExtraFields.removeAll();
                lytExtraFields.setSizeUndefined();
                
                if (valueChangeEvent.getValue() != null) {
                    txtName.setRequired(false);
                    txtName.clear();
                    try {
                        List<AttributeMetadata> mandatoryAttributesInSelectedClass = mem.getMandatoryAttributesInClass(valueChangeEvent.getValue().getName());
                        if (!mandatoryAttributesInSelectedClass.isEmpty())
                            lytExtraFields.add(new MandatoryAttributesLayout(txtName, wdwNewPoolItem, attributes, mandatoryAttributesInSelectedClass, aem));
                    } catch(InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                }
            });
            FormLayout lytFields = new FormLayout(cmbClassType, txtName);
            lytFields.setWidthFull();
            lytFields.setResponsiveSteps(
                new ResponsiveStep("40em", 2),
                new ResponsiveStep("60em", 3)
            );
            wdwNewPoolItem.setContent(lytFields, lytExtraFields);

        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return wdwNewPoolItem;
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newPoolItemAction;
    }
}