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

package org.neotropic.kuwaiba.modules.core.navigation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractObjectRelatedViewWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.util.visual.properties.PropertySheet;

/**
 * A side panel composed by set of sections with detailed information and options 
 * on an object. These sections are: A property sheet, a list of explorers, a list of views, a help 
 * and a contextual information panel.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectOptionsPanel {
    /**
     * This event occurs when a view is selected.
     */
    public static final String EVENT_VIEW_SELECTION = "view-selection";
    /**
     * This event occurs when a view, action or explorer is selected.
     */
    public static final String EVENT_ACTION_SELECTION = "action-selection";
    /**
     * This event occurs when a explorer is selected.
     */
    public static final String EVENT_EXPLORER_SELECTION = "explorer-selection";
    /**
     * The object whose details are displayed by the present details panel. 
     */
    private BusinessObjectLight selectedObject;
    /**
     * Should the panel show the object's property sheet? By default this is set to true.
     */
    private boolean showPropertySheet = true;
    /**
     * Should the panel show the relationships, special containment and attachment explorers? By default this is set to true.
     */
    private boolean showExplorers = true;
    /**
     * Should the panel show the custom views (object, rack, etc)? By default this is set to true.
     */
    private boolean showViews = true;
    /**
     * Should the panel show the core actions, such as new delete or explore object? By default this is set to true.
     */
    private boolean showCoreActions = true;
    /**
     * Should the panel show the actions provided by each module and loaded dynamically depending on the type of object selected? 
     * By default this is set to true.
     */
    private boolean showCustomActions = true;
    /**
     * Should the panel show the help panel? By default this is set to false.
     */
    private boolean showHelp = false;
    /**
     * Should the panel show the context information panel? By default this is set to false.
     */
    private boolean showContext = false;
    /**
     * The main accordion containing the sections.
     */
    private final Accordion accOptions;
    /**
     * Reference to the metadata entity manager.
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Reference to the action registry.
     */
    private final CoreActionsRegistry coreActionRegistry;
    /**
     * All non-general purpose actions provided by other modules than Navigation.
     */
    private final AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * All the object-related views exposed by other modules.
     */
    private final ViewWidgetRegistry viewWidgetRegistry;
    /**
     * All the registered object explorers.
     */
    private final ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * Listener to item selection events.
     */
    private ActionListener selectionListener; 
    /**
     * Listener to property change events.
     */
    private PropertySheet.IPropertyValueChangedListener propertyListener;
    /**
     * Property sheet
     */
    private PropertySheet shtMain;
    
    public ObjectOptionsPanel(BusinessObjectLight selectedObject, CoreActionsRegistry coreActionRegistry, AdvancedActionsRegistry advancedActionsRegistry,
            ViewWidgetRegistry viewWidgetRegistry, ExplorerWidgetRegistry explorerWidgetRegistry, MetadataEntityManager mem, 
            ApplicationEntityManager aem, BusinessEntityManager bem, TranslationService ts) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.ts = ts;
        this.coreActionRegistry = coreActionRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.selectedObject = selectedObject;
        this.accOptions = new Accordion();
    }
    
    /**
     * Builds the actual component ready to be embedded.Make sure you set the showXXX flags to 
 the values you need depending on what do you want to be displayed. Every flag has a default, see 
 their respective documentation for details.
     * @param user The user taken from the application session. It will be used to match its privileges to activate or deactivate actions.
     * @return The component to be embedded. Main implementation uses an accordion.
     * @throws InventoryException In case that building some of the sections results in 
     */
    public Component build(UserProfile user) throws InventoryException {
        if (showPropertySheet) {
            shtMain = new PropertySheet(ts, PropertyFactory.
                propertiesFromBusinessObject(bem.getObject(selectedObject.getClassName(), selectedObject.getId()), ts, aem, mem));
            shtMain.addPropertyValueChangedListener((property) -> {
                if (propertyListener != null)
                    propertyListener.updatePropertyChanged(property);
            });
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-properties"), shtMain);
        }
        
        if (showCoreActions) {
            Grid<AbstractVisualInventoryAction> tblCoreActions = new Grid<>();
            // For now we just concatenate the different types of core actions. This 
            // should be decorated properly in future versions.
            tblCoreActions.setItems(Stream.of(coreActionRegistry.getMiscActions(), 
                    coreActionRegistry.getRelationshipManagementActions(), coreActionRegistry.getDeleteActions()).
                    flatMap(Collection::stream));
            tblCoreActions.setSelectionMode(Grid.SelectionMode.SINGLE);
            tblCoreActions.addColumn(AbstractVisualInventoryAction::getName);
            
            tblCoreActions.addItemClickListener(evt -> {
                if (evt.getItem() != null) {
                    if (selectionListener != null)
                        selectionListener.actionPerformed(new ActionEvent(evt.getItem(), 0 /* Core action */, EVENT_ACTION_SELECTION));
                }
            });
            
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-core-actions"), tblCoreActions);
        }
        
        if (showCustomActions) {
            Grid<AbstractVisualAdvancedAction> tblCustomActions = new Grid<>();
            List<AbstractVisualAdvancedAction> actions = advancedActionsRegistry.getActionsApplicableToRecursive(selectedObject.getClassName(), true);
            List<AbstractVisualAdvancedAction> filteredActions = new ArrayList<>();
            actions.stream().forEach(action -> {
               if (user.getPrivileges().stream().filter(item -> 
                        item.getFeatureToken().equals(action.getModuleId()) && item.getAccessLevel() == Privilege.ACCESS_LEVEL_READ_WRITE).findAny().isPresent())
                   filteredActions.add(action);
            });
            tblCustomActions.setItems(filteredActions);
            tblCustomActions.setSelectionMode(Grid.SelectionMode.SINGLE);
            tblCustomActions.addColumn(AbstractVisualInventoryAction::getName);
            
            tblCustomActions.addItemClickListener( evt -> {
                if (evt.getItem() != null) {
                    if (selectionListener != null)
                        selectionListener.actionPerformed(new ActionEvent(evt.getItem(), 1 /* Custom action */, EVENT_ACTION_SELECTION));
                }
            });
            
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-custom-actions"), tblCustomActions);
        }
        
        if (showViews) {
            Grid<AbstractObjectRelatedViewWidget> tblViews = new Grid<>();
            tblViews.setItems(viewWidgetRegistry.getViewWidgetsApplicableTo(this.selectedObject.getClassName()));
            tblViews.addColumn(AbstractObjectRelatedViewWidget::getName);
            tblViews.setSelectionMode(Grid.SelectionMode.SINGLE);

            tblViews.addItemClickListener((evt) -> {
                if (evt.getItem() != null) {
                    if (selectionListener != null)
                        selectionListener.actionPerformed(new ActionEvent(evt.getItem(), 0, EVENT_VIEW_SELECTION));
                }
            });

            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-views"), tblViews);
        }
        
        if (showExplorers) {
            Grid<AbstractExplorerWidget> tblExplorers = new Grid<>();
            tblExplorers.setItems(explorerWidgetRegistry.getExplorerWidgetsApplicableToRecursive(this.selectedObject.getClassName()));
            tblExplorers.setSelectionMode(Grid.SelectionMode.SINGLE);
            tblExplorers.addColumn(AbstractExplorerWidget::getName);
            
            tblExplorers.addItemClickListener( evt -> {
                if (evt.getItem() != null) {
                    if (selectionListener != null)
                        selectionListener.actionPerformed(new ActionEvent(evt.getItem(), 0, EVENT_EXPLORER_SELECTION));
                }
            });
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-explorers"), tblExplorers);
        }
        
        if (showHelp)
            accOptions.add(ts.getTranslatedString("module.general.labels.help"), 
                    new Label(ts.getTranslatedString("module.general.labels.nothing-to-show")));
        
        if (showContext)
            accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard.object-context"), 
                    new Label(ts.getTranslatedString("module.general.labels.nothing-to-show")));

        //accOptions.setMaxWidth("450px"); //TODO check in all modules
        accOptions.setWidth("100%");
        accOptions.setHeightFull();
        return this.accOptions;
    }

    public BusinessObjectLight getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(BusinessObjectLight selectedObject) {
        this.selectedObject = selectedObject;
    }

    public boolean isShowPropertySheet() {
        return showPropertySheet;
    }

    public void setShowPropertySheet(boolean showPropertySheet) {
        this.showPropertySheet = showPropertySheet;
    }

    public boolean isShowExplorers() {
        return showExplorers;
    }

    public void setShowExplorers(boolean showExplorers) {
        this.showExplorers = showExplorers;
    }

    public boolean isShowViews() {
        return showViews;
    }

    public void setShowViews(boolean showViews) {
        this.showViews = showViews;
    }

    public boolean isShowCoreActions() {
        return showCoreActions;
    }

    public void setShowCoreActions(boolean showCoreActions) {
        this.showCoreActions = showCoreActions;
    }

    public boolean isShowCustomActions() {
        return showCustomActions;
    }

    public void setShowCustomActions(boolean showCustomActions) {
        this.showCustomActions = showCustomActions;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }

    public boolean isShowContext() {
        return showContext;
    }

    public void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }

    public void setSelectionListener(ActionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void setPropertyListener(PropertySheet.IPropertyValueChangedListener propertyListener) {
        this.propertyListener = propertyListener;
    }
    
    public void UndoLastEdit() {
        this.shtMain.undoLastEdit();
    }
    
    public Object lastValue(String propertyName) {
        return this.shtMain.lastValue(propertyName);
    }
}