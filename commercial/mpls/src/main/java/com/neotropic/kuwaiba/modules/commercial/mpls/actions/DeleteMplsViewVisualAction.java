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
package com.neotropic.kuwaiba.modules.commercial.mpls.actions;

import com.neotropic.kuwaiba.modules.commercial.mpls.MplsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
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
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete MPLS view action
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class DeleteMplsViewVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteMplsViewAction deleteClassAction;
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

    public DeleteMplsViewVisualAction() {
        super(MplsModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        
        Long viewId;
        if (parameters.containsKey("viewId")) {
            viewId = (Long) parameters.get("viewId");

            ConfirmDialog wdwDeleteClass = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                    ts.getTranslatedString("module.mpls.labels.confirm-remove-message"),
                    ts.getTranslatedString("module.general.messages.ok"));
            wdwDeleteClass.getBtnConfirm().addClickListener((ev) -> {
                try {
                    deleteClassAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("viewId", viewId)));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.mpls.actions.view-delete-success"), DeleteMplsViewAction.class));
                    wdwDeleteClass.close();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteMplsViewAction.class));
                    wdwDeleteClass.close();
                }
            });
            return wdwDeleteClass;
        } else 
            return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteClassAction;
    }
}
