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
package org.neotropic.kuwaiba.modules.core.favorites.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.favorites.FavoritesModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new favorite action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewFavoriteVisualAction extends AbstractVisualAction<Dialog> {
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
     * Reference to the New Favorite Action
     */
    @Autowired
    private NewFavoriteAction newFavoriteAction;
    /**
     * Dialog to create new favorite folder
     */
    private ConfirmDialog wdwNewFavorite;
    
    public NewFavoriteVisualAction() {
        super(FavoritesModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("user")) {
            UserProfile user = (UserProfile) parameters.get("user");
            commandClose = (Command) parameters.get("commandClose");

            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            // Dialog
            wdwNewFavorite = new ConfirmDialog(ts
                    , this.getModuleAction().getDisplayName()
                    , ts.getTranslatedString("module.general.labels.create"));
            wdwNewFavorite.setResizable(true);
            wdwNewFavorite.setDraggable(true);
            wdwNewFavorite.setMinWidth("40%");
            
            wdwNewFavorite.getBtnConfirm().addClickListener(event -> {
                try {
                    ActionResponse actionResponse = newFavoriteAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>("name", txtName.getValue()),
                                    new ModuleActionParameter<>("user", user)));
                    
                    if (actionResponse.containsKey("exception"))
                            throw new ModuleActionException(((Exception)actionResponse.get("exception")).getLocalizedMessage());
                    
                    wdwNewFavorite.close();
                    //refresh related grid
                    getCommandClose().execute();
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.favorites.actions.favorite-new-favorite.success"), NewFavoriteAction.class));
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewFavoriteAction.class));
                }
            });
            wdwNewFavorite.getBtnConfirm().setEnabled(false);
            wdwNewFavorite.getBtnConfirm().setThemeName("primary");
            wdwNewFavorite.getBtnConfirm().setClassName("primary-button");
          
            txtName.addValueChangeListener((event) -> {
                wdwNewFavorite.getBtnConfirm().setEnabled(!txtName.getValue().isEmpty() && txtName.getValue() != null);
            });
         
            // Add content to window
            wdwNewFavorite.setContent(txtName);
            return wdwNewFavorite;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "user")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return newFavoriteAction;
    }
    
    /**
     * refresh grid
     * @return commandClose;Command; refresh action 
     */
    public Command getCommandClose() {
        return commandClose;
    }

    /**
     * @param commandClose; Command; refresh action 
     */
    public void setCommandClose(Command commandClose) {
        this.commandClose = commandClose;
    }
}