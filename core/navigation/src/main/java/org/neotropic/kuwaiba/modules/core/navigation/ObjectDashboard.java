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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractObjectRelatedViewWidget;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The main object editing interface. This dashboard provides a property sheet, custom actions,
 * views, explorers (special children, relationships, etc) as well as some room to add widgets 
 * in order to display contextual information about the selected object.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value= "object-dashboard", layout = NavigationLayout.class)
public class ObjectDashboard extends HorizontalLayout implements  HasDynamicTitle, AbstractUI {
    /**
     * Reference to the object being edited/explored currently.
     */
    private BusinessObjectLight selectedObject;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private CoreActionsRegistry actionRegistry;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * All the object-related views exposed by other modules.
     */
    @Autowired
    private ViewWidgetRegistry viewWidgetRegistry;
    /**
     * All the registered object explorers.
     */
    @Autowired
    private ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;

    public ObjectDashboard() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }
    
    @Override
    public String getPageTitle() {
        return this.selectedObject == null ? ts.getTranslatedString("module.navigation.widgets.object-dashboard.no-object-selected") : 
                String.format(ts.getTranslatedString("module.navigation.widgets.object-dashboard.title"), this.selectedObject);
    }

    @Override
    public void initContent() {
        this.selectedObject = UI.getCurrent().getSession().getAttribute(BusinessObjectLight.class);
        if (this.selectedObject == null)
            add(new Label(ts.getTranslatedString("module.navigation.widgets.object-dashboard.no-object-selected")));
        else {            
            VerticalLayout lytDetails = new VerticalLayout();
            lytDetails.setSizeFull();
            lytDetails.setSpacing(false);
            lytDetails.setPadding(false);
            try {
                ObjectOptionsPanel pnlOptions = new ObjectOptionsPanel(selectedObject, 
                        actionRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry,
                        mem, aem, bem, ts);
                pnlOptions.setSelectionListener((event) -> {
                    switch (event.getActionCommand()) {
                        case ObjectOptionsPanel.EVENT_VIEW_SELECTION:
                            lytDetails.removeAll();
                            try {
                                lytDetails.add(((AbstractObjectRelatedViewWidget)event.getSource()).build(selectedObject));
                            } catch (InventoryException ex) {
                                lytDetails.add(new Label(ex.getLocalizedMessage()));
                            }
                            break;
                        case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                            ModuleActionParameterSet parameters = new ModuleActionParameterSet(new ModuleActionParameter<>("businessObject", selectedObject));
                            Dialog wdwObjectAction = (Dialog)((AbstractVisualInventoryAction)event.getSource()).getVisualComponent(parameters);
                            wdwObjectAction.open();
                            break;
                        case ObjectOptionsPanel.EVENT_EXPLORER_SELECTION:
                            Dialog wdwExplorer = new Dialog(((AbstractExplorerWidget)event.getSource()).build(selectedObject));
                            wdwExplorer.setHeight("90%");
                            wdwExplorer.setMinWidth("70%");
                            wdwExplorer.setResizable(true);
                            wdwExplorer.setDraggable(true);
                            wdwExplorer.setCloseOnEsc(true);
                            wdwExplorer.open();
                            break;
                    }
                });
                pnlOptions.setPropertyListener((property) -> {
                    HashMap<String, String> attributes = new HashMap<>();
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    Object lastValue =  pnlOptions.lastValue(property.getName());
                    attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                    try {
                        bem.updateObject(selectedObject.getClassName(), selectedObject.getId(), attributes);
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), 
                            ts.getTranslatedString("module.general.messages.property-updated-successfully"), 
                                AbstractNotification.NotificationType.INFO, ts).open();
                        // activity log
                        aem.createObjectActivityLogEntry(session.getUser().getUserName(), selectedObject.getClassName(),
                                selectedObject.getId(), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT,
                                property.getName(), lastValue == null ? "" : lastValue.toString(), property.getAsString(), "");
                    } catch (InventoryException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        pnlOptions.UndoLastEdit();
                    }
                });
                // Header
                Label headerObject = new Label(selectedObject.toString());
                headerObject.setClassName("dialog-title");
                Button btnInfo = new Button(this.windowMoreInformation.getDisplayName());
                btnInfo.setWidthFull();
                btnInfo.setMaxWidth("450px");
                btnInfo.addClickListener(event -> {
                    this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("object", selectedObject))).open();
                });
                // Layouts
                HorizontalLayout lytHeader = new HorizontalLayout(headerObject);
                VerticalLayout lytOptions = new VerticalLayout(lytHeader, btnInfo, pnlOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser()));
                lytOptions.setSpacing(false);
                lytOptions.setMinWidth("25%");
                lytOptions.setMaxWidth("350px");
                // Add content
                add(lytOptions);
                add(lytDetails);
            } catch (InventoryException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
}