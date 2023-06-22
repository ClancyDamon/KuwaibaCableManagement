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
package org.neotropic.kuwaiba.modules.commercial.sync.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.SynchronizationModule;
import org.neotropic.kuwaiba.modules.commercial.sync.model.TemplateDataSource;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete a data source template
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
@Component
public class DeleteTemplateDataSourceVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Parameter, template data source configuration.
     */
    public static String PARAM_TEMPLATE_DATA_SOURCE = "templateDataSource"; //NOI18N
    /**
     * Parameter command close.
     */
    public static String PARAM_COMMANDCLOSE = "commandClose"; //NOI18N
    /**
     * Parameter exception.
     */
    public static String PARAM_EXCEPTION = "exception"; //NOI18N
    /**
     * Close action command
     */
    private Command commandClose;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteTemplateDataSourceAction deleteTemplateDataSourceAction;

    public DeleteTemplateDataSourceVisualAction() {
        super(SynchronizationModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        TemplateDataSource template;
        if (parameters.containsKey(PARAM_TEMPLATE_DATA_SOURCE)) {
            template = (TemplateDataSource) parameters.get(PARAM_TEMPLATE_DATA_SOURCE);
            commandClose = (Command) parameters.get(PARAM_COMMANDCLOSE);

            ConfirmDialog wdwDeleteConfiguration = new ConfirmDialog(ts
                    , this.deleteTemplateDataSourceAction.getDisplayName()
                    , String.format(ts.getTranslatedString("module.sync.actions.delete-template-data-source.confirm"), template.getName())
                    , ts.getTranslatedString("module.general.labels.delete"));
            wdwDeleteConfiguration.setDraggable(true);

            wdwDeleteConfiguration.getBtnConfirm().addClickListener((event) -> {
                try {
                    ActionResponse actionResponse
                            = deleteTemplateDataSourceAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(PARAM_TEMPLATE_DATA_SOURCE, template)));

                    if (actionResponse.containsKey(PARAM_EXCEPTION))
                        throw new ModuleActionException(((Exception) actionResponse.get(PARAM_EXCEPTION)).getLocalizedMessage());

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.sync.actions.delete-template-data-source.success"), DeleteSyncDataSourceConfigurationAction.class));
                    wdwDeleteConfiguration.close();
                    //refresh related grid
                    getCommandClose().execute();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteSyncDataSourceConfigurationAction.class));
                }
            });
            return wdwDeleteConfiguration;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.sync.actions.delete-template-data-source.error")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteTemplateDataSourceAction;
    }

    /**
     * refresh grid
     *
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