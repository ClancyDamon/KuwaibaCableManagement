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
package org.neotropic.kuwaiba.modules.optional.reports.actions;

import com.vaadin.flow.component.dialog.Dialog;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.reports.ReportsModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete a report parameter .
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class DeleteParameterVisualAction extends AbstractVisualAction<Dialog>  {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteParameterAction deleteParameterAction;

    public DeleteParameterVisualAction() {
        super(ReportsModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        String selectedParameter;
        ReportMetadataLight selectedReport;

        if (parameters.containsKey("parameter")) 
            selectedParameter = (String) parameters.get("parameter");
        else 
            return null;

        if (parameters.containsKey("report"))
            selectedReport = (ReportMetadataLight) parameters.get("report");
        else 
            return null;
   
        ConfirmDialog wdwDeleteParameter = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                ts.getTranslatedString("module.reporting.confirm-delete-parameter"),
                ts.getTranslatedString("module.general.labels.delete"));

        wdwDeleteParameter.getBtnConfirm().addClickListener((event) -> {
            try {
                deleteParameterAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>("report", selectedReport.getId()),
                        new ModuleActionParameter<>("key", selectedParameter)
                ));

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString(ts.getTranslatedString("module.reporting.actions.parameter-deleted")), DeleteParameterAction.class));
                wdwDeleteParameter.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteParameterAction.class));
                wdwDeleteParameter.close();
            }
        });
        return wdwDeleteParameter;
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteParameterAction;
    }
    
}
