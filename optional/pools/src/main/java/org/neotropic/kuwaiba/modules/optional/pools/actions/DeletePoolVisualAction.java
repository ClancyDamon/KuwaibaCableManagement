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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.pools.PoolsModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete pool action
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class DeletePoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeletePoolAction deletePoolAction;
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

    public DeletePoolVisualAction() {
        super(PoolsModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        InventoryObjectPool selectedPool;

        if (parameters.containsKey(PARAMETER_POOL)) {
            selectedPool = (InventoryObjectPool) parameters.get(PARAMETER_POOL);

            ConfirmDialog wdwDeleteClass = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                    String.format(ts.getTranslatedString("module.pools.confirm-delete-pool"), selectedPool.getName()),
                    ts.getTranslatedString("module.general.labels.delete"));
            wdwDeleteClass.setResizable(true);
            wdwDeleteClass.setDraggable(true);

            wdwDeleteClass.getBtnConfirm().addClickListener((ev) -> {
                try {

                    deletePoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("poolId", selectedPool.getId())));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.pools.pool-deleted"), DeletePoolAction.class));
                    wdwDeleteClass.close();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeletePoolAction.class));
                    wdwDeleteClass.close();
                }
            });
            return wdwDeleteClass;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deletePoolAction;
    }
}