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
package org.neotropic.kuwaiba.modules.core.audittrail.widgets;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * Retrieves the activity log related to an object
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class ObjectAuditTrailWidget extends AbstractDashboardWidget {
    /**
     * The main business object
     */
    private final BusinessObjectLight businessObject;
    /**
     * Save time stamp activity to display later in readable format
     */
    private Date timeStamp;
    /**
     * Save activity type to display later in readable format
     */
    private final HashMap<Integer, String> types;
    /**
     * The grid with the audit trail of an object
     */
    private final Grid<ActivityLogEntry> tblObjectAuditTrail;

    public ObjectAuditTrailWidget(BusinessObjectLight businessObject, MetadataEntityManager mem, ApplicationEntityManager aem,
            BusinessEntityManager bem, TranslationService ts) {
        super(mem, aem, bem, ts);
        this.businessObject = businessObject;
        types = new HashMap<>();
        tblObjectAuditTrail = new Grid<>();
        super.setTitle(ts.getTranslatedString("module.audit-trail.header"));
        super.createCover();
        coverComponent.addClassName("widgets-colors-magenta");
    }

    @Override
    public void createContent() {
        getActivityType();
        buildAuditTrailGrid();
        BoldLabel lblTitle = new BoldLabel(String.format(ts.getTranslatedString("module.audit-trail.activity-object.header"), businessObject.getName()));
        VerticalLayout lytContent = new VerticalLayout(lblTitle, tblObjectAuditTrail);
        lytContent.setSizeFull();
        lytContent.setWidth("1000px");
        contentComponent = lytContent;
    }

    private void buildAuditTrailGrid() {
        try {
            List<ActivityLogEntry> objectEntries = aem.getBusinessObjectAuditTrail(businessObject.getClassName(), businessObject.getId(), -1);
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