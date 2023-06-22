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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.dialog.Dialog;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractDeleteAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual action to delete a business object
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class DefaultDeleteBusinessObjectVisualAction extends AbstractDeleteAction {
    /**
     * business object visual action parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to module action.
     */
    @Autowired
    private DefaultDeleteBusinessObjectAction deleteBusinessObjectAction;

    public DefaultDeleteBusinessObjectVisualAction() {
        super(NavigationModule.MODULE_ID);
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        ConfirmDialog wdwDeleteBusinessObject = new ConfirmDialog(ts, getModuleAction().getDisplayName(),
                String.format(ts.getTranslatedString("module.navigation.actions.delete-business-object.confirmation-message"), businessObject),
                ts.getTranslatedString("module.general.labels.delete"));
        
        ShortcutRegistration btnOkShortcut = wdwDeleteBusinessObject.getBtnConfirm().addClickShortcut(Key.ENTER).listenOn(wdwDeleteBusinessObject);
        wdwDeleteBusinessObject.getBtnConfirm().addClickListener(e -> {
            try {
                deleteBusinessObjectAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter(DefaultDeleteBusinessObjectAction.PARAM_OBJECT_CLASS_NAME, businessObject.getClassName()),
                            new ModuleActionParameter(DefaultDeleteBusinessObjectAction.PARAM_OBJECT_OID, businessObject.getId())));

                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put(ActionResponse.ActionType.REMOVE, "");
                actionResponse.put(PARAM_BUSINESS_OBJECT, businessObject);
                wdwDeleteBusinessObject.close();                

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.navigation.actions.delete-business-object.name-success"),
                            DefaultDeleteBusinessObjectAction.class,
                            actionResponse));

            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DefaultDeleteBusinessObjectVisualAction.class));
                wdwDeleteBusinessObject.close();
            }
            btnOkShortcut.remove();
            e.unregisterListener();
        });
        wdwDeleteBusinessObject.setDraggable(true);
        return wdwDeleteBusinessObject;
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteBusinessObjectAction;
    }

    @Override
    public int getRequiredSelectedObjects() {
        return -1;
    }
}
