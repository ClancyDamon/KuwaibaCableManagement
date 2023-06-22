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
package org.neotropic.kuwaiba.modules.commercial.softman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of release relationship action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseRelationshipVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action. 
     */
    @Autowired
    private ReleaseRelationshipAction releaseRelationshipAction;
    
    public ReleaseRelationshipVisualAction() {
        super(SoftwareManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("sourceObject")) {
            BusinessObjectLight sourceObject = (BusinessObjectLight) parameters.get("sourceObject");
            if (parameters.containsKey("targetObject")) {
                BusinessObjectLight targetObject = (BusinessObjectLight) parameters.get("targetObject");
                
                ConfirmDialog wdwRelease = new ConfirmDialog(ts,
                        String.format(this.releaseRelationshipAction.getDisplayName(), sourceObject.getName(), targetObject.getName()),
                        ts.getTranslatedString("module.softman.actions.release-relationship.confirm"),
                        ts.getTranslatedString("module.general.messages.release"));
                wdwRelease.setDraggable(true);
                wdwRelease.setResizable(true);
            
                wdwRelease.getBtnConfirm().addClickListener(event -> {
                    try {
                        releaseRelationshipAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter("sourceObject", sourceObject),
                                new ModuleActionParameter("targetObject", targetObject)));
                        wdwRelease.close();
                        
                        if (parameters.containsKey("releaseRelationship")) {
                            Command releaseRelationship = (Command) parameters.get("releaseRelationship");
                            releaseRelationship.execute();
                        }
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), ReleaseRelationshipAction.class));
                    }
                });
                return wdwRelease;
            } else
                return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "targetObject")));
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "sourceObject")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return releaseRelationshipAction;
    }   
}