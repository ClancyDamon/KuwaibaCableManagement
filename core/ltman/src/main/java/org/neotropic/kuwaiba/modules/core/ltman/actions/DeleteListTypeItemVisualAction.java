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
package org.neotropic.kuwaiba.modules.core.ltman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.ltman.ListTypeManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete list type item action
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class DeleteListTypeItemVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteListTypeItemAction deleteListTypeItemAction;
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

    public DeleteListTypeItemVisualAction() {
        super(ListTypeManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight seletedListTypeItem;

        if (parameters.containsKey("listTypeItem")) {
            seletedListTypeItem = (BusinessObjectLight) parameters.get("listTypeItem");      

        ConfirmDialog wdwDeleteListTypeItem = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                ts.getTranslatedString("module.ltman.actions.confirm-delete-lti"),
                ts.getTranslatedString("module.general.labels.delete"));
        
        wdwDeleteListTypeItem.getBtnConfirm().addClickListener((ev) -> {
            try {
                ActionResponse response = deleteListTypeItemAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>("className", seletedListTypeItem.getClassName()),
                        new ModuleActionParameter<>("oid", seletedListTypeItem.getId())));
                response.put(ActionResponse.ActionType.REMOVE, "");

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("module.ltman.actions.delete-list-type-item.ui.item-created-success"), DeleteListTypeItemAction.class, response));
                wdwDeleteListTypeItem.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteListTypeItemAction.class));
                wdwDeleteListTypeItem.close();
            }
        });
        return wdwDeleteListTypeItem; 
        } else 
            return new Dialog(new Label(ts.getTranslatedString("module.ltman.error-param-list-type-item")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteListTypeItemAction;
    }
}
