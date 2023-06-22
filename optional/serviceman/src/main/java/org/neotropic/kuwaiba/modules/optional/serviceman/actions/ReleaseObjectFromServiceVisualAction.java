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
package org.neotropic.kuwaiba.modules.optional.serviceman.actions;

import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Front-end to the release object from service action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseObjectFromServiceVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private ReleaseObjectFromServiceAction releaseObjectFromServiceAction;
    /**
     * Parameter business object.
     */
    public static String PARAMETER_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Parameter service.
     */
    public static String PARAMETER_SERVICE = "service"; //NOI18N
    
    public ReleaseObjectFromServiceVisualAction() {
        super(ServiceManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey(PARAMETER_SERVICE)) {
            BusinessObjectLight service = (BusinessObjectLight) parameters.get(PARAMETER_SERVICE);
            if (parameters.containsKey(PARAMETER_BUSINESS_OBJECT)) {
                BusinessObjectLight object = (BusinessObjectLight) parameters.get(PARAMETER_BUSINESS_OBJECT);
                ConfirmDialog wdwRelease = new ConfirmDialog(ts,
                        this.releaseObjectFromServiceAction.getDisplayName(),
                        String.format(ts.getTranslatedString("module.serviceman.actions.release-object-from-service.ui.relationship-confirmation"),
                                object.getName(), service.getName()),
                        ts.getTranslatedString("module.general.messages.release"));
                wdwRelease.setDraggable(true);
                wdwRelease.setResizable(true);
                wdwRelease.setThemeVariants(EnhancedDialogVariant.SIZE_SMALL);

                wdwRelease.getBtnConfirm().addClickListener(event -> {
                    try {
                        releaseObjectFromServiceAction.getCallback()
                                .execute(new ModuleActionParameterSet(new ModuleActionParameter<>(PARAMETER_SERVICE, service)
                                        , new ModuleActionParameter<>(PARAMETER_BUSINESS_OBJECT, object)));
                        
                        ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(ActionResponse.ActionType.REMOVE, "");
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                String.format(ts.getTranslatedString("module.serviceman.actions.release-object-from-service.ui.relationship-success"), object.getName()),
                                ReleaseObjectFromServiceAction.class, actionResponse));
                        wdwRelease.close();
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), ReleaseObjectFromServiceAction.class));
                    }
                });

                return wdwRelease;
            } else
                return new Dialog(
                        new Label(ts.getTranslatedString("module.serviceman.actions.release-object-from-service.ui.relationship-error-param-object")));
        } else
            return new Dialog(
                    new Label(ts.getTranslatedString("module.serviceman.actions.release-object-from-service.ui.relationship-error-param-service")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return releaseObjectFromServiceAction;
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}