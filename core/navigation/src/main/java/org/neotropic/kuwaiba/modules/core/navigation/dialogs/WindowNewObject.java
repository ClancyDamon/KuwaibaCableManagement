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
package org.neotropic.kuwaiba.modules.core.navigation.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Window to show the actions to create an object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowNewObject extends ConfirmDialog {
    private final BusinessObjectLight businessObject;
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    private final NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    private final NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    private final TranslationService ts;
    private ActionCompletedListener actionCompletedListener;
    
    public WindowNewObject(
        BusinessObjectLight businessObject,
        NewBusinessObjectVisualAction newBusinessObjectVisualAction, 
        NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction,
        NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction,
        TranslationService ts) {
        
        Objects.requireNonNull(businessObject);
        Objects.requireNonNull(newBusinessObjectVisualAction);
        Objects.requireNonNull(newBusinessObjectFromTemplateVisualAction);
        Objects.requireNonNull(newMultipleBusinessObjectsVisualAction);
        Objects.requireNonNull(ts);
        
        this.businessObject = businessObject;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.newBusinessObjectFromTemplateVisualAction = newBusinessObjectFromTemplateVisualAction;
        this.newMultipleBusinessObjectsVisualAction = newMultipleBusinessObjectsVisualAction;
        this.ts = ts;
    }

    @Override
    public void open() {
        AbstractAction newBusinessObjectAction = newBusinessObjectVisualAction.getModuleAction();
        AbstractAction newBusinessObjectFromTemplateAction = newBusinessObjectFromTemplateVisualAction.getModuleAction();
        AbstractAction newMultipleBusinessObjectsAction = newMultipleBusinessObjectsVisualAction.getModuleAction();
        
        ListBox<AbstractAction> lstAction = new ListBox();
        lstAction.setRenderer(new ComponentRenderer<>(action -> new Label(action.getDisplayName())));
        lstAction.setItems(newBusinessObjectAction, newBusinessObjectFromTemplateAction, newMultipleBusinessObjectsAction);
        lstAction.addValueChangeListener(valueChange -> {
            AbstractAction action = valueChange.getValue();
            if (action instanceof NewBusinessObjectAction) {
                ModuleActionParameterSet parameters =  new ModuleActionParameterSet(
                    new ModuleActionParameter(NewBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT, businessObject)
                );
                if (actionCompletedListener != null)
                    newBusinessObjectVisualAction.registerActionCompletedLister(actionCompletedListener);
                newBusinessObjectVisualAction.getVisualComponent(parameters).open();
                
            } else if (action instanceof NewBusinessObjectFromTemplateAction) {
                ModuleActionParameterSet parameters = new ModuleActionParameterSet(
                    new ModuleActionParameter(NewBusinessObjectFromTemplateVisualAction.PARAM_BUSINESS_OBJECT, businessObject)
                );
                if (actionCompletedListener != null)
                    newBusinessObjectFromTemplateVisualAction.registerActionCompletedLister(actionCompletedListener);
                newBusinessObjectFromTemplateVisualAction.getVisualComponent(parameters).open();
                
            } else if (action instanceof NewMultipleBusinessObjectsAction) {
                ModuleActionParameterSet parameters = new ModuleActionParameterSet(
                    new ModuleActionParameter(NewMultipleBusinessObjectsVisualAction.PARAM_BUSINESS_OBJECT, businessObject)
                );
                if (actionCompletedListener != null)
                    newMultipleBusinessObjectsVisualAction.registerActionCompletedLister(actionCompletedListener);
                newMultipleBusinessObjectsVisualAction.getVisualComponent(parameters).open();
            }
            close();
        });
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
        btnClose.setWidthFull();
        
        setHeader(String.format(ts.getTranslatedString("module.navigation.actions.new-business-object.new-object-title"), businessObject.getName()));
        setContent(lstAction);
        setFooter(btnClose);
        super.open();
    }
    
    public void setActionCompletedListener(ActionCompletedListener actionCompletedListener) {
        this.actionCompletedListener = actionCompletedListener;
    }
}
