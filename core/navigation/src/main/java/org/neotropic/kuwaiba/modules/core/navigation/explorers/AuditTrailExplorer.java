/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.navigation.explorers;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.ShowMoreInformationAction;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Shows audit trail associated to a given object.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class AuditTrailExplorer extends AbstractExplorerWidget<VerticalLayout> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * The grid with the audit trail of an object
     */
    private Grid<ActivityLogEntry> tblObjectAuditTrail;
    /**
     * Save time stamp activity to display later in readable format
     */
    private Date timeStamp;
    /**
     * Save activity type to display later in readable format
     */
    private HashMap<Integer, String> types;
    /**
     * Main layout
     */
    private VerticalLayout lytMain;

    @Override
    public String getName() {
        return ts.getTranslatedString("module.navigation.explorers.audit-trail.title");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.navigation.explorers.audit-trail.description");
    }

    @Override
    public String appliesTo() {
        return "InventoryObject";
    }

    @Override
    public VerticalLayout build(BusinessObjectLight selectedObject) {
        // Main Layout
        lytMain = new VerticalLayout();
        // Actions
        Button btnInfo = new Button(this.windowMoreInformation.getDisplayName());
        btnInfo.addClickListener(event -> {
            this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("object", selectedObject))).open();
        });
        // Header
        Label lblTitle = new Label(String.format(ts.getTranslatedString("module.navigation.explorers.audit-trail.header"),
                 selectedObject.toString()));
        lblTitle.setClassName("dialog-title");
        HorizontalLayout lytTitleHeader = new HorizontalLayout(lblTitle);
        lytTitleHeader.setPadding(false);
        lytTitleHeader.setMargin(false);
        // Add info to layout
        Label lblInfo = new Label(ts.getTranslatedString("module.navigation.explorers.help"));
        lblInfo.setClassName("info-label");
        HorizontalLayout lytInfo = new HorizontalLayout(lblInfo);
        lytInfo.setMargin(false);
        lytInfo.setPadding(false);
        // Content to main layout
        lytMain.add(lytTitleHeader, btnInfo, lytInfo);
        lytMain.setHeightFull();
        lytMain.setWidthFull();
        lytMain.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytTitleHeader);
        lytMain.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btnInfo);
        types = new HashMap<>();
        getActivityType();
        buildAuditTrailGrid(selectedObject);
        return lytMain;
    }
    
    private void buildAuditTrailGrid(BusinessObjectLight selectedObject) {
        try {
            List<ActivityLogEntry> objectEntries = aem.getBusinessObjectAuditTrail(selectedObject.getClassName(), selectedObject.getId(), -1);
            if (objectEntries.isEmpty()) {
                Label lblNoAuditTrail = new Label(ts.getTranslatedString("module.navigation.explorers.audit-trail.ui.no-audit-trail"));
                lytMain.add(lblNoAuditTrail);
            } else {
                tblObjectAuditTrail = new Grid<>();
                tblObjectAuditTrail.setItems(objectEntries);
                tblObjectAuditTrail.setHeightFull();
                tblObjectAuditTrail.addColumn(item -> timeStamp = new Date(item.getTimestamp()))
                        .setHeader(ts.getTranslatedString("module.audit-trail.activity-timestamp"));
                tblObjectAuditTrail.addColumn(item -> ts.getTranslatedString(types.get(item.getType())))
                        .setHeader(ts.getTranslatedString("module.audit-trail.activity-type"));
                tblObjectAuditTrail.addColumn(ActivityLogEntry::getUserName)
                        .setHeader(ts.getTranslatedString("module.audit-trail.activity-user"));
                tblObjectAuditTrail.addColumn(ActivityLogEntry::getAffectedProperty)
                        .setHeader(ts.getTranslatedString("module.audit-trail.activity-property"));
                tblObjectAuditTrail.addColumn(ActivityLogEntry::getOldValue)
                        .setHeader(ts.getTranslatedString("module.audit-trail.activity-oldValue"));
                tblObjectAuditTrail.addColumn(ActivityLogEntry::getNewValue)
                        .setHeader(ts.getTranslatedString("module.audit-trail.activity-newValue"));
                tblObjectAuditTrail.setClassNameGenerator(item -> item.getNotes() != null && !item.getNotes().isEmpty() ? "text" : "");
                // Audit trail
                VerticalLayout lytAuditTrail = new VerticalLayout(tblObjectAuditTrail);
                lytAuditTrail.setHeightFull();
                lytAuditTrail.setWidthFull();
                lytAuditTrail.setSpacing(false);
                lytAuditTrail.setMargin(false);
                lytAuditTrail.setPadding(false);
                // Add to main layout
                lytMain.add(lytAuditTrail);
            }
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void getActivityType() {
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, "module.audit-trail.activity-type.create-application-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, "module.audit-trail.activity-type.delete-application-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, "module.audit-trail.activity-type.update-aplication-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, "module.audit-trail.activity-type.create-inventory-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, "module.audit-trail.activity-type.delete-inventory-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, "module.audit-trail.activity-type.update-inventory-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, "module.audit-trail.activity-type.create-metadata-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, "module.audit-trail.activity-type.delete-metadata-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, "module.audit-trail.activity-type.update-metadata-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, "module.audit-trail.activity-type.move-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_MASSIVE_DELETE_APPLICATION_OBJECT, "module.audit-trail.activity-type.massive-delete");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, "module.audit-trail.activity-type.view-update");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_OPEN_SESSION, "module.audit-trail.activity-type.session-created");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CLOSE_SESSION, "module.audit-trail.activity-type.session-closed");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_USER, "module.audit-trail.activity-type.new-user");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_MASSIVE_UPDATE_APPLICATION_OBJECT, "module.audit-trail.activity-type.massive-application-object-update");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, "module.audit-trail.activity-type.create-inventory-object-relationship");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, "module.audit-trail.activity-type.release-inventory-object-relationship");
    }    
}