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
package com.neotropic.kuwaiba.modules.commercial.processman.tools;

import com.neotropic.kuwaiba.modules.commercial.processman.actions.AbstractVisualElementAction;
import com.neotropic.kuwaiba.modules.commercial.processman.actions.ElementActionsRegistry;
import com.neotropic.kuwaiba.modules.commercial.processman.components.ElementPropertyEditorDialog;
import com.neotropic.kuwaiba.modules.commercial.processman.elementUi.ElementUi;
import com.neotropic.kuwaiba.modules.commercial.processman.service.ArtifactDefinitionFunction;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.server.Command;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * A side panel composed by set of sections with detailed information and options on an element.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class ElementOptionsPanel {
    /**
     * This event occurs when an action is selected.
     */
    public static final String EVENT_ACTION_SELECTION = "action-selection";    
    /**
     * The main accordion containing the sections.
     */
    private final Accordion accOptions;
    /**
     * Reference to the action registry.
     */
    private final ElementActionsRegistry elementActionsRegistry;
    /**
     * Should the panel show the element's property sheet? By default this is set to true.
     */
    private boolean showPropertySheet = true;
    private boolean showEvents = true;
    /**
     * Should the panel show the core actions, such as new delete or explore element? By default this is set to true.
     */
    private boolean showCoreActions = true;
    /**
     * The object whose details are displayed by the present details panel. 
     */
    private ElementUi selectedElement;
    /**
     * Listener to item selection events.
     */
    private ActionListener selectionListener;
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Reference to the Element Property Editor Dialog
     */
    private final ElementPropertyEditorDialog elementPropertyEditorDialog;  
    /**
     * 
     */
    private final List<ArtifactDefinitionFunction> functions;
    /**
     * 
     */
    private final Command command;
    /**
     * 
     */
    private final String path;
    
    public ElementOptionsPanel(ElementUi elementUi, ElementActionsRegistry elementActionsRegistry
            , TranslationService ts, ElementPropertyEditorDialog elementPropertyEditorDialog, List<ArtifactDefinitionFunction> functions, Command command, String path) {
        this.elementPropertyEditorDialog = elementPropertyEditorDialog;
        this.elementActionsRegistry = elementActionsRegistry;
        this.accOptions = new Accordion();
        this.selectedElement = elementUi;
        this.ts = ts;
        this.command = command;
        this.functions = functions;
        this.path = path;
    }
    
    public Component build() {
        if (showPropertySheet) {
            Command notification = ()
                    -> new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();            
            accOptions.add(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-element-properties"), ElementPropertyFactory.propertiesFromElementUi(selectedElement, notification, ts, elementPropertyEditorDialog));
        }
        
        if (showEvents) {
            Command event = () -> command.execute();
            accOptions.add(ts.getTranslatedString("module.processeditor.editor-form-type-artifact-element-events"), ElementPropertyFactory.eventsFromElementUi(selectedElement, ts, elementPropertyEditorDialog, functions, event, path));
        } 
            
        if (showCoreActions) {
            Grid<AbstractVisualElementAction> tblBasicActions = new Grid<>();
            tblBasicActions.setItems(Stream.of(elementActionsRegistry.getMiscActions()).flatMap(Collection::stream));
            tblBasicActions.setSelectionMode(Grid.SelectionMode.SINGLE);
            tblBasicActions.addColumn(AbstractVisualElementAction::getName);
            tblBasicActions.setHeight("33px");
            
            tblBasicActions.addItemClickListener(evt -> {
                if (evt.getItem() != null) {
                    if (selectionListener != null)
                        selectionListener.actionPerformed(new ActionEvent(evt.getItem(), 0 /* Core action */, EVENT_ACTION_SELECTION));
                }
            });
            
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-core-actions"), tblBasicActions);
        }
        
        accOptions.setWidth("100%");
        accOptions.setHeightFull();
        return this.accOptions;
    }
    
    public ElementUi getSelectedElement() {
        return selectedElement;
    }
    
    public void setSelectedElement(ElementUi selectedElement) {
        this.selectedElement = selectedElement;
    }

    public boolean isShowPropertySheet() {
        return showPropertySheet;
    }

    public void setShowPropertySheet(boolean showPropertySheet) {
        this.showPropertySheet = showPropertySheet;
    }

    public boolean isShowEvents() {
        return showEvents;
    }

    public void setShowEvents(boolean showEvents) {
        this.showEvents = showEvents;
    }
    
    public boolean isShowCoreActions() {
        return showCoreActions;
    }

    public void setShowCoreActions(boolean showCoreActions) {
        this.showCoreActions = showCoreActions;
    }
    
    public void setSelectionListener(ActionListener selectionListener) {
        this.selectionListener = selectionListener;
    }
}