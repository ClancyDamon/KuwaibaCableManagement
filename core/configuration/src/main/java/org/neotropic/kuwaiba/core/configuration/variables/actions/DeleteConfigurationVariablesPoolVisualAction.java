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
package org.neotropic.kuwaiba.core.configuration.variables.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.variables.ConfigurationVariablesModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete configuration variables pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteConfigurationVariablesPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Action command after finish delete pool over configuration variable layout
     */
    private Command commandClose;
    /**
     * Action command after finish delete pool over poll configuration dialog
     */
    private Command commandDelete;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteConfigurationVariablesPoolAction deleteConfigurationVariablesPoolAction;
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

    public DeleteConfigurationVariablesPoolVisualAction() {
        super(ConfigurationVariablesModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        InventoryObjectPool selectedConfigurationVariablePool;
        
        if(parameters.containsKey("configurationVariablePool")){
            selectedConfigurationVariablePool = (InventoryObjectPool) parameters.get("configurationVariablePool");
            commandClose = (Command) parameters.get("commandClose");
            commandDelete = (Command) parameters.get("commandDelete");
            
            ConfirmDialog wdwDeleteConfigurationVariablePool = new ConfirmDialog(ts
                    , this.deleteConfigurationVariablesPoolAction.getDisplayName()
                    , String.format(ts.getTranslatedString("module.configman.actions.delete-configuration-variable-pool.ui.pool-deleted-confirm")
                            , selectedConfigurationVariablePool.getName())
                    , ts.getTranslatedString("module.general.labels.delete"));
            wdwDeleteConfigurationVariablePool.setDraggable(true);
            wdwDeleteConfigurationVariablePool.setResizable(true);
            
            wdwDeleteConfigurationVariablePool.getBtnConfirm().addClickListener((event) -> { 
               try{
                   deleteConfigurationVariablesPoolAction.getCallback().execute(new ModuleActionParameterSet(
                   new ModuleActionParameter<>(Constants.PROPERTY_ID, selectedConfigurationVariablePool.getId())));
                   
                   //refresh related grid
                   getCommandClose().execute();
                   commandDelete.execute();
                   fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                           ts.getTranslatedString("module.configman.actions.delete-configuration-variable-pool.ui.pool-deleted-success"), DeleteConfigurationVariablesPoolAction.class));
                   wdwDeleteConfigurationVariablePool.close();
               }catch(ModuleActionException ex){
                   fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteConfigurationVariablesPoolAction.class));
                   wdwDeleteConfigurationVariablePool.close();
               } 
        });
            return wdwDeleteConfigurationVariablePool; 
         }else 
            return new Dialog(new Label(ts.getTranslatedString("module.configman.error-param-configuration-variable-pool")));    }

    @Override
    public AbstractAction getModuleAction() {
       return deleteConfigurationVariablesPoolAction;
    }
    
    /**
     * refresh grid
     * @return commandClose;Command; refresh action 
     */
    public Command getCommandClose() {
        return commandClose;
    }

    /**
     * @param commandClose;Command; refresh action 
     */
    public void setCommandClose(Command commandClose) {
        this.commandClose = commandClose;
    }
}