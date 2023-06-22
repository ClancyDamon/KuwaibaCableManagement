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
package org.neotropic.kuwaiba.modules.core.audittrail;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.audittrail.tools.AuditTrailType;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.icon.ActionIcon;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Audit Trail module. This class manages how the pages corresponding
 * to different functionalities are present in a single place. 
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "audit-trail", layout = AuditTrailLayout.class)
public class AuditTrailUI extends VerticalLayout implements ActionCompletedListener, 
        HasDynamicTitle, AbstractUI {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * The grid with the list audit trail
     */
    private final Grid<ActivityLogEntry> gridAuditTrail; 
    /**
     * Save activity type to display later in readable format
     */
    private final HashMap<Integer, String> types;
    private List<AuditTrailType> listType;
    /**
     * Save filter values
     */
    private final HashMap<String, Object> filters;
    private ComboBox<AuditTrailType> cmbType;
    private ComboBox<UserProfile> cmbUser;
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) { }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.audit-trail.title");
    }
    
    public AuditTrailUI() {
        super();
        setSizeFull();
        gridAuditTrail = new Grid<>();
        types = new HashMap<>();
        filters = new HashMap<>();
    }
    
    @Override
    public void onDetach(DetachEvent ev) { }

    private void buildAuditTrailGrid() {
        loadDataProvider();
        gridAuditTrail.setHeightFull();
        gridAuditTrail.addColumn(item -> new Date(item.getTimestamp()))
                .setHeader(ts.getTranslatedString("module.audit-trail.activity-timestamp")).setResizable(true);
        gridAuditTrail.addColumn(item -> ts.getTranslatedString(types.get(item.getType())))
                .setHeader(ts.getTranslatedString("module.audit-trail.activity-type"))
                .setKey(ts.getTranslatedString("module.audit-trail.activity-type")).setResizable(true);
        gridAuditTrail.addColumn(ActivityLogEntry::getUserName)
                .setHeader(ts.getTranslatedString("module.audit-trail.activity-user"))
                .setKey(ts.getTranslatedString("module.audit-trail.activity-user")).setResizable(true);
        gridAuditTrail.addColumn(ActivityLogEntry::getAffectedProperty)
                .setHeader(ts.getTranslatedString("module.audit-trail.activity-property")).setResizable(true);
        gridAuditTrail.addColumn(ActivityLogEntry::getOldValue)
                .setHeader(ts.getTranslatedString("module.audit-trail.activity-oldValue")).setResizable(true);
        gridAuditTrail.addColumn(ActivityLogEntry::getNewValue)
                .setHeader(ts.getTranslatedString("module.audit-trail.activity-newValue")).setResizable(true);
        gridAuditTrail.addColumn(ActivityLogEntry::getNotes)
                .setHeader(ts.getTranslatedString("module.audit-trail.activity-notes")).setResizable(true);
        gridAuditTrail.setClassNameGenerator(item -> item.getNotes() != null && !item.getNotes().isEmpty() ? "text" : "");
        getfilters();
    }
    
    /**
     * Load the data provider for audit trail
     * @param query.getOffset The index of the first item to load 
     * @param query.getLimit The number of items to load
     * @param filters add filters to data provider
     */
    private void loadDataProvider() {
        DataProvider<ActivityLogEntry, Void> dataProvider = DataProvider.fromFilteringCallbacks(
            query -> aem.getGeneralActivityAuditTrail(query.getOffset(), query.getLimit(), filters).stream(),
            query -> (int) aem.getGeneralActivityAuditTrailCount(query.getOffset(), query.getLimit(), filters)
        );
        gridAuditTrail.setDataProvider(dataProvider);
    }
    
    /**
     * Load the data provider for audit trail without filters     
     * @param query.getOffset The index of the first item to load 
     * @param query.getLimit The number of items to load
     */
    private void refreshAuditTrail() {
        DataProvider<ActivityLogEntry, Void> dataProvider = DataProvider.fromFilteringCallbacks(
            query -> aem.getGeneralActivityAuditTrail(query.getOffset(), query.getLimit(), null).stream(),
            query -> (int) aem.getGeneralActivityAuditTrailCount(query.getOffset(), query.getLimit(), null)
        );
        gridAuditTrail.setDataProvider(dataProvider);
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
    
    private void getfilters() {
        HeaderRow headerRow = gridAuditTrail.prependHeaderRow();        
        //type filter
        cmbType = new ComboBox();
        cmbType.setSizeFull();
        cmbType.setItems(listType);
        cmbType.setItemLabelGenerator(type -> ts.getTranslatedString(type.getDisplayName()));
        cmbType.setAllowCustomValue(false);
        cmbType.setClearButtonVisible(true);
        cmbType.setPlaceholder(ts.getTranslatedString("module.audit-trail.activity-type"));
        cmbType.addValueChangeListener(event -> {
           if (event.getValue() != null)
               filters.put("type", event.getValue().getType());
           else 
               filters.remove("type");
           loadDataProvider();
        });
        headerRow.getCell(gridAuditTrail.getColumnByKey(ts.getTranslatedString("module.audit-trail.activity-type"))).setComponent(cmbType);
        //user filter
        List<UserProfile> listUsers = aem.getUsers();
        cmbUser = new ComboBox();
        cmbUser.setSizeFull();
        cmbUser.setItems(listUsers);
        cmbUser.setItemLabelGenerator(user -> user.getUserName());
        cmbUser.setAllowCustomValue(false);
        cmbUser.setClearButtonVisible(true);
        cmbUser.setPlaceholder(ts.getTranslatedString("module.audit-trail.activity-user"));
        cmbUser.addValueChangeListener(event -> {
           if (event.getValue() != null)
               filters.put("user", event.getValue().getUserName());
           else 
               filters.remove("user");
           loadDataProvider();
        });
        headerRow.getCell(gridAuditTrail.getColumnByKey(ts.getTranslatedString("module.audit-trail.activity-user"))).setComponent(cmbUser);
    }
    
    private void cleanFilters() {
        if (cmbType.getValue() != null) {
            cmbType.clear();
            filters.remove("type");
        }
        if (cmbUser.getValue() != null) {
            cmbUser.clear();
            filters.remove("user");
        }
    }

    @Override
    public void initContent() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();

        ActionButton btnRefresh = new ActionButton(new ActionIcon(VaadinIcon.REFRESH), ts.getTranslatedString("module.audit-trail.actions.refresh"));
        btnRefresh.addClickListener(event -> {
           cleanFilters();
           refreshAuditTrail();
        });
        btnRefresh.setHeight("32px");
        
        getActivityType();
        loadActivityType();
        buildAuditTrailGrid();
        Label header = new Label(ts.getTranslatedString("module.audit-trail.header"));
        header.setClassName("audit-trail-main-header");
        HorizontalLayout headers = new HorizontalLayout(header, btnRefresh);
        VerticalLayout lytAuditTrail = new VerticalLayout(headers, gridAuditTrail);
        
        lytMainContent.add(lytAuditTrail);
        add(lytMainContent);
    }
     
    private void loadActivityType() {
        listType = new ArrayList<>();
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, "module.audit-trail.activity-type.create-application-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, "module.audit-trail.activity-type.delete-application-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, "module.audit-trail.activity-type.update-aplication-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, "module.audit-trail.activity-type.create-inventory-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, "module.audit-trail.activity-type.delete-inventory-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, "module.audit-trail.activity-type.update-inventory-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, "module.audit-trail.activity-type.create-metadata-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, "module.audit-trail.activity-type.delete-metadata-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, "module.audit-trail.activity-type.update-metadata-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, "module.audit-trail.activity-type.move-object"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_MASSIVE_DELETE_APPLICATION_OBJECT, "module.audit-trail.activity-type.massive-delete"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, "module.audit-trail.activity-type.view-update"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_OPEN_SESSION, "module.audit-trail.activity-type.session-created"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_CLOSE_SESSION, "module.audit-trail.activity-type.session-closed"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_CREATE_USER, "module.audit-trail.activity-type.new-user"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_MASSIVE_UPDATE_APPLICATION_OBJECT, "module.audit-trail.activity-type.massive-application-object-update"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, "module.audit-trail.activity-type.create-inventory-object-relationship"));
        listType.add(new AuditTrailType(ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, "module.audit-trail.activity-type.release-inventory-object-relationship"));
    }
}