/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
 * Visual wrapper of delete license action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteLicenseVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action. 
     */
    @Autowired
    private DeleteLicenseAction deleteLicenseAction;
    
    public DeleteLicenseVisualAction() {
        super(SoftwareManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("license")) {
            BusinessObjectLight license = (BusinessObjectLight) parameters.get("license");
            
            ConfirmDialog wdwDelete = new ConfirmDialog(ts, this.deleteLicenseAction.getDisplayName(),
                    String.format(ts.getTranslatedString("module.softman.actions.delete-license.confirm"), license.getName()),
                    ts.getTranslatedString("module.general.labels.delete"));
            wdwDelete.setDraggable(true);
            wdwDelete.setResizable(true);
            
            wdwDelete.getBtnConfirm().addClickListener(event -> {
                try {
                    deleteLicenseAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter("license", license)));
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.softman.actions.delete-license.success"), DeleteLicenseAction.class));
                    wdwDelete.close();
                    
                    if (parameters.containsKey("deleteLicense")) {
                        Command deleteLicense = (Command) parameters.get("deleteLicense");
                        deleteLicense.execute();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteLicenseAction.class));
                }
            });
            return wdwDelete;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "license")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteLicenseAction;
    }   
}