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
package org.neotropic.kuwaiba.modules.core.queries.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.queries.QueriesModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new pool action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewScriptedQueryPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewScriptedQueryPoolAction newPoolAction;
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

    public NewScriptedQueryPoolVisualAction() {
        super(QueriesModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        ConfirmDialog wdwAdd = new ConfirmDialog(ts,
                this.newPoolAction.getDisplayName(),
                ts.getTranslatedString("module.general.labels.create"));
        wdwAdd.setDraggable(true);
        wdwAdd.setResizable(true);      
        
        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();
        
        TextField txtDescription = new TextField(ts.getTranslatedString("module.general.labels.description"));
        txtDescription.setSizeFull();
        
        wdwAdd.getBtnConfirm().addClickListener(event -> {
            try {
                if (txtName.getValue() == null || txtName.getValue().trim().isEmpty()) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                            String.format(ts.getTranslatedString("module.general.messages.attribute-not-empty"),
                                    ts.getTranslatedString("module.general.labels.name")),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                } else {
                    newPoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("poolName", txtName.getValue()),
                            new ModuleActionParameter<>("description", txtDescription.getValue())));
                    
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString(ts.getTranslatedString("module.pools.actions.pool-created")), NewScriptedQueryPoolAction.class));
                    wdwAdd.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewScriptedQueryPoolAction.class));
            }
        });
        
        wdwAdd.setContent(txtName, txtDescription);
        return wdwAdd;        
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newPoolAction;
    }
}