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
package org.neotropic.kuwaiba.modules.core.templateman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.templateman.TemplateManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Deletes a template item from a template.
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class DeleteTemplateItemVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;

    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteTemplateAction deleteTemplateItemAction;

    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;

    public DeleteTemplateItemVisualAction() {
        super(TemplateManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        String templateClasName;
        String templateId;

        if (parameters.containsKey(Constants.PROPERTY_CLASSNAME)
                && parameters.containsKey(Constants.PROPERTY_ID)) {
            templateClasName = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
            templateId = (String) parameters.get(Constants.PROPERTY_ID);
            
            ConfirmDialog wdwDeleteTemplateItem = new ConfirmDialog(ts,
                    this.deleteTemplateItemAction.getDisplayName(),
                    ts.getTranslatedString("module.general.labels.confirm-delete"),
                    ts.getTranslatedString("module.general.labels.delete"));

            wdwDeleteTemplateItem.getBtnConfirm().addClickListener((ev) -> {
                try {
                    deleteTemplateItemAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(Constants.PROPERTY_CLASSNAME, templateClasName),
                            new ModuleActionParameter<>(Constants.PROPERTY_ID, templateId)));

                    ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(ActionResponse.ActionType.REMOVE, "");
                        actionResponse.put(Constants.PROPERTY_ID, templateId); //the affected node id
                        
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS
                            , ts.getTranslatedString("module.templateman.actions.delete-template.ui.item-created-success")
                            , DeleteTemplateAction.class, actionResponse));
                    wdwDeleteTemplateItem.close();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteTemplateAction.class));
                    wdwDeleteTemplateItem.close();
                }
            });
            return wdwDeleteTemplateItem;
        } else 
            return new Dialog(new Label(ts.getTranslatedString("module.templateman.error-param-template-item")));
        
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteTemplateItemAction;
    }
    
    public String getTitle(){
        return ts.getTranslatedString("module.templateman.actions.delete-template-item.name");
    }
}
