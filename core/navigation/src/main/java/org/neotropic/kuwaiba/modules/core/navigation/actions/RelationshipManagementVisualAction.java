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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractRelationshipManagementAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Merges the former Relate To.../Release From... actions. This action will list, and eventually, launch the registered, 
 * applicable {@link AbstractRelationshipManagementAction} instances available at the moment.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class RelationshipManagementVisualAction extends AbstractVisualInventoryAction {
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the object that holds the relateTo... registered actions.
     */
    @Autowired
    private CoreActionsRegistry coreActionsRegistry;

    public RelationshipManagementVisualAction() {
        super(NavigationModule.MODULE_ID);
    }

    @Override
    public int getRequiredSelectedObjects() {
        return SELECTION_ANY_OBJECTS;
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException(ts.getTranslatedString("module.general.messages.no-module-action")); 
    }
    
    @Override
    public ConfirmDialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight)parameters.get(Constants.PROPERTY_RELATED_OBJECT);
        if (businessObject == null)
            return new ConfirmDialog(ts, ts.getTranslatedString("module.serviceman.actions.manage-relationships.name-for"), 
                    new Label(ts.getTranslatedString("module.navigation.widgets.object-dashboard.no-object-selected")), 
                    ts.getTranslatedString("module.general.messages.ok"));
        
        return new RelationshipManagementWizard(String.format(ts.getTranslatedString("module.serviceman.actions.manage-relationships.name-for"), 
                businessObject), businessObject);
    }
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.navigation.actions.manage-relationships.name");
    }
    
    private class RelationshipManagementWizard extends ConfirmDialog implements Wizard {
        private List<Step> stepsSoFar;
        private RelationshipManagementWizard(String title, BusinessObjectLight selectedObject) {
            super(ts, title, "");
            this.stepsSoFar = new ArrayList<>();
            ChooseActionStep pnlChooseAction = new ChooseActionStep(selectedObject);
            this.stepsSoFar.add(pnlChooseAction);
            this.setContent(pnlChooseAction);
            this.setFooter(new ButtonsPanel(this));
            this.setDraggable(true);
            this.setResizable(true);
        }
        
        @Override
        public void dispose() {
            close();
        }

        @Override
        public List<Step> getStepsSoFar() {
            return this.stepsSoFar;
        }
    }
    
    /**
     * The first step of the wizard: Choose if you want to create, remove or explore relationships.
     */
    private class ChooseActionStep extends VerticalLayout implements Step {
        private BusinessObjectLight sourceObject;
        private ListBox<OptionWrapper<String>> lstActions;

        public ChooseActionStep(BusinessObjectLight sourceObject) {
            setSizeFull();
            lstActions = new ListBox<>();
            lstActions.setItems(new OptionWrapper(1, ts.getTranslatedString("module.navigation.actions.manage-relationships.create-relationship")),
                    new OptionWrapper(2, ts.getTranslatedString("module.navigation.actions.manage-relationships.release-relationship")),
                    new OptionWrapper(3, ts.getTranslatedString("module.navigation.actions.manage-relationships.explore-relationships")));
            lstActions.setMaxWidth("300px");
            
            HorizontalLayout lytLeftPadding = new HorizontalLayout(), lytRightPadding = new HorizontalLayout();
            lytLeftPadding.setSizeFull();
            lytRightPadding.setSizeFull();
            
            add(lytLeftPadding, lstActions, lytRightPadding);
            this.sourceObject = sourceObject;
        }

        @Override
        public Step next() throws IllegalArgumentException {
            if (lstActions.getValue() == null)
                throw new IllegalArgumentException(ts.getTranslatedString("module.visual-utilities.messages.must-select-an-option"));
            
            switch (lstActions.getValue().getOptionId()) {
                case 1: // Create relationship
                    return new ChooseRelationshipTypeStep(sourceObject, lstActions.getValue().getOptionId());
                case 2: // Release relationship
                    return new ChooseRelationshipTypeStep(sourceObject, lstActions.getValue().getOptionId());
                case 3: // Explore relationships
                    return new ExploreRelationshipsStep(sourceObject);
                default:
                    throw new IllegalArgumentException(ts.getTranslatedString("module.visual-utilities.messages.invalid-option"));
            }
            
        }
    }
    
    /**
     * In this step the user chooses the type of relationship to be established with the target object.
     */
    private class ChooseRelationshipTypeStep extends VerticalLayout implements Step {
        private int path;
        private BusinessObjectLight sourceObject;
        private ListBox<AbstractRelationshipManagementAction> lstActions;
        public ChooseRelationshipTypeStep(BusinessObjectLight sourceObject, int path) {
            List<AbstractRelationshipManagementAction> applicableActions = coreActionsRegistry.
                    getRelationshipManagementActionsApplicableToRecursive(sourceObject.getClassName());
            this.lstActions = new ListBox<>();
            lstActions.setItems(applicableActions);
            lstActions.setMaxWidth("300px");
        }

        
        
        @Override
        public Step next() throws IllegalArgumentException {
            if (lstActions.getValue() == null)
                throw new IllegalArgumentException(ts.getTranslatedString("module.visual-utilities.messages.must-select-an-option"));
            
            switch (path) {
                case 1:
                    return new CreateRelationshipStep(sourceObject, (sourceObjects, targetObjects) -> {
                        lstActions.getValue().createRelationship(sourceObjects, targetObjects);
                        return null;
                    });
                case 2:
                    
            }
            return null;
        }
    }
    
    /**
     * Here the user finds the object or objects to establish the relationship with.
     */
    private class CreateRelationshipStep extends VerticalLayout implements Step {

        public CreateRelationshipStep(BusinessObjectLight sourceObject, 
                BiFunction<List<BusinessObjectLight>, List<BusinessObjectLight>, Void> callBack) {
        }
        
        @Override
        public Step next() throws IllegalArgumentException {
            return null; // This is the last step
        }
    }
    
    /**
     * 
     */
    private class ReleaseRelationshipStep extends VerticalLayout implements Step {

        public ReleaseRelationshipStep(BusinessObjectLight sourceObject, 
                BiFunction<BusinessObjectLight, List<BusinessObjectLight>, Void> callBack) {
        }
        
        @Override
        public Step next() {
            return null;
        }
    }
    
    private class ExploreRelationshipsStep extends VerticalLayout implements Step {

        public ExploreRelationshipsStep(BusinessObjectLight sourceObject) {
        }
        
        @Override
        public Step next() {
            return null;
        }
    }
    
    
    private interface Wizard {
        public List<Step> getStepsSoFar();
        public void dispose();
        
    }
    
    private interface Step {
        public Step next() throws IllegalArgumentException;
    }
    
    
    private class ButtonsPanel extends HorizontalLayout {
        private Button btnBack;
        private Button btnNext;
        private Button btnFinish;
        private Button btnClose;

        public ButtonsPanel(Wizard parentWindow) {
            setSizeFull();
            btnBack = new Button(ts.getTranslatedString("module.general.messages.previous"));
            btnBack.setWidth("300px");
            btnBack.setEnabled(false);
            btnNext = new Button(ts.getTranslatedString("module.general.messages.next"));
            btnNext.setWidth("300px");
            btnFinish = new Button(ts.getTranslatedString("module.general.messages.finish"));
            btnFinish.setWidth("300px");
            btnClose = new Button(ts.getTranslatedString("module.general.messages.close"));
            btnClose.setWidth("300px");
            btnClose.addClickListener(event -> parentWindow.dispose());
        }

        public Button getBtnBack() {
            return btnBack;
        }

        public void setBtnBack(Button btnBack) {
            this.btnBack = btnBack;
        }

        public Button getBtnNext() {
            return btnNext;
        }

        public void setBtnNext(Button btnNext) {
            this.btnNext = btnNext;
        }

        public Button getBtnFinish() {
            return btnFinish;
        }

        public void setBtnFinish(Button btnFinish) {
            this.btnFinish = btnFinish;
        }

        public Button getBtnClose() {
            return btnClose;
        }

        public void setBtnClose(Button btnClose) {
            this.btnClose = btnClose;
        }
    }
    
    private class OptionWrapper<T> {
        private int optionId;
        private T optionValue;

        public OptionWrapper(int optionId, T optionValue) {
            this.optionId = optionId;
            this.optionValue = optionValue;
        }

        public int getOptionId() {
            return optionId;
        }

        public void setOptionId(int optionId) {
            this.optionId = optionId;
        }

        public T getOptionValue() {
            return optionValue;
        }

        public void setOptionValue(T optionValue) {
            this.optionValue = optionValue;
        }
        
        @Override
        public String toString() {
            return optionValue.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof OptionWrapper))
                return false;
            else
                return optionId == ((OptionWrapper)obj).getOptionId();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + this.optionId;
            return hash;
        }
    }
}