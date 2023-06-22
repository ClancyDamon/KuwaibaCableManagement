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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.kuwaiba.modules.core.navigation.tools.RelationshipType;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of the action of release an object from another object.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseFromVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Business Object Parameter.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Parameter object class name.
     */
    public static String PARAM_OBJECT_CLASS = "objectClass"; //NOI18
    /**
     * Parameter by object id.
     */
    public static String PARAM_OBJECT_ID = "objectId"; //NOI18
    /**
     * Parameter by other target id.
     */
    public static String PARAM_OTHER_OBJECT_ID = "otherObjectId"; //NOI18N
    /**
     * Parameter by relationship name.
     */
    public static String PARAM_RELATIONSHIP_NAME = "relationshipName";
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * The window to show more information about an object.
     */
    @Autowired
    private ShowMoreInformationAction windowMoreInformation;
    /**
     * Reference to the Release From Action.
     */
    @Autowired
    private ReleaseFromAction releaseFromAction;
    /**
     * Relationship contact to object.
     */
    private static final String RELATIONSHIP_HASCONTACT = "hasContact";
    /**
     * Relationship contract to object.
     */
    private static final String RELATIONSHIP_CONTRACTHAS = "contractHas";
    /**
     * Relationship project to object.
     */
    private static final String RELATIONSHIP_PROJECTSPROJECTUSES = "projectsProjectUses";
    /**
     * Relationship proxy to object.
     */
    private static final String RELATIONSHIP_HASPROXY = "hasProxy";
    /**
     * Relationship service to object.
     */
    private static final String RELATIONSHIP_USES = "uses";
    /**
     * Layout of business objects associated with an another business object.
     */
    private VerticalLayout lytObjects;
    /**
     * List of business objects associated to be released.
     */
    private List<BusinessObjectLight> selectedObjectList;
    /**
     * Dialog to release an inventory object from others inventory object.
     */
    private ConfirmDialog windowRelease;
    /**
     * Action response for the navigation module.
     */
    private ActionResponse actionResponse;
    
    public ReleaseFromVisualAction() {
        super(NavigationModule.MODULE_ID);
    }

    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public com.vaadin.flow.component.Component getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        if (businessObject != null) {
            windowRelease = new ConfirmDialog(ts,
                    String.format(ts.getTranslatedString("module.navigation.actions.release-from.header"), businessObject.getName()),
                    ts.getTranslatedString("module.general.messages.release"));
            windowRelease.getBtnConfirm().setEnabled(false);
            windowRelease.setDraggable(true);
            windowRelease.setResizable(true);
            windowRelease.setModal(false);
            windowRelease.setWidth("60%");
            VerticalLayout lytContent = new VerticalLayout();
            lytContent.setSpacing(true);
            lytContent.setPadding(false);
            lytContent.setMargin(false);
            
            List<RelationshipType> relationshipTypeList = this.createRelationshipList(businessObject);
            if (!relationshipTypeList.isEmpty()) {
                ComboBox<RelationshipType> cmbRelationshipType = new ComboBox<>(
                        ts.getTranslatedString("module.general.labels.relationship"),
                        relationshipTypeList
                );
                cmbRelationshipType.setWidthFull();

                cmbRelationshipType.addValueChangeListener(event -> {
                    lytObjects.removeAll();
                    if (event.getValue() != null) {
                        lytObjects.setVisible(true);
                        buildObjectsGrid(businessObject, event.getValue().getName());
                    } else 
                        lytObjects.setVisible(false);
                });

                //objects layout
                lytObjects = new VerticalLayout();
                lytObjects.setClassName("grig-pool-container");
                lytObjects.setSizeFull();
                lytObjects.setMinHeight("400px");
                lytObjects.setMargin(false);
                lytObjects.setSpacing(true);
                lytObjects.setVisible(false);

                lytContent.add(cmbRelationshipType, lytObjects);
                windowRelease.getBtnConfirm().addClickListener(event -> {
                    selectedObjectList.forEach(object -> {
                        try {
                            actionResponse = releaseFromAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>(PARAM_OBJECT_CLASS, businessObject.getClassName()),
                                    new ModuleActionParameter<>(PARAM_OBJECT_ID, businessObject.getId()),
                                    new ModuleActionParameter<>(PARAM_OTHER_OBJECT_ID, object.getId()),
                                    new ModuleActionParameter<>(PARAM_RELATIONSHIP_NAME, cmbRelationshipType.getValue().getName())
                            ));
                        } catch (ModuleActionException ex) {
                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                    ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                    ex.getLocalizedMessage(),
                                    ModuleActionException.class
                            ));
                        }
                    });

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString(
                                    selectedObjectList.size() > 1
                                    ? String.format(ts.getTranslatedString("module.navigation.actions.release-from-multiple.success"), businessObject.getName())
                                    : String.format(ts.getTranslatedString("module.navigation.actions.release-from.success"), businessObject.getName(), selectedObjectList.get(0).getName())),
                            ReleaseFromAction.class,
                            actionResponse
                    ));
                    windowRelease.close();
                });
            } else {
                Label lblNoRelations = new Label(String.format(ts.getTranslatedString("module.general.labels.no-relationship"), businessObject.getName()));
                lytContent.add(lblNoRelations);

                windowRelease.getBtnConfirm().setVisible(false);
                windowRelease.setMinHeight("150px");
                windowRelease.setWidth("40%");
            }
            windowRelease.setContent(lytContent);
            return windowRelease;
        }
        return null;
    }
    
    /**
     * Evaluate if the business object is related to contact, contract, project, proxy and service.
     * If the class of the object is a subclass of one of the following GenericContact, GenericContract, GenericProject, GenericProxy,  GenericService options,
     * the relation that indicates its resources is added.
     * @param businessObject The selected business object.
     * @return The relationship type list.
     */
    private List<RelationshipType> createRelationshipList(BusinessObjectLight businessObject) {
        List<RelationshipType> relationshipTypeList = new ArrayList<>();
        try {
            // Has contact?
            if (bem.hasSpecialAttribute(businessObject.getClassName(), businessObject.getId(), RELATIONSHIP_HASCONTACT))
                relationshipTypeList.add(
                        mem.isSubclassOf(Constants.CLASS_GENERICCONTACT, businessObject.getClassName())
                                ? new RelationshipType(RELATIONSHIP_HASCONTACT, String.format(ts.getTranslatedString("module.general.labels.relationship.other-resources"),
                                        ts.getTranslatedString("module.general.labels.relationship.contact")))
                                : new RelationshipType(RELATIONSHIP_HASCONTACT, ts.getTranslatedString("module.general.labels.relationship.contact"))
                );
            // Has contract?
            if (bem.hasSpecialAttribute(businessObject.getClassName(), businessObject.getId(), RELATIONSHIP_CONTRACTHAS))
                relationshipTypeList.add(
                        mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, businessObject.getClassName())
                                ? new RelationshipType(RELATIONSHIP_CONTRACTHAS, String.format(ts.getTranslatedString("module.general.labels.relationship.other-resources"),
                                        ts.getTranslatedString("module.general.labels.relationship.contract")))
                                : new RelationshipType(RELATIONSHIP_CONTRACTHAS, ts.getTranslatedString("module.general.labels.relationship.contract"))
                );
            // Has project?
            if (bem.hasSpecialAttribute(businessObject.getClassName(), businessObject.getId(), RELATIONSHIP_PROJECTSPROJECTUSES))
                relationshipTypeList.add(
                        mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, businessObject.getClassName())
                                ? new RelationshipType(RELATIONSHIP_PROJECTSPROJECTUSES, String.format(ts.getTranslatedString("module.general.labels.relationship.other-resources"),
                                        ts.getTranslatedString("module.general.labels.relationship.project")))
                                : new RelationshipType(RELATIONSHIP_PROJECTSPROJECTUSES, ts.getTranslatedString("module.general.labels.relationship.project"))
                );
            // Has proxy?
            if (bem.hasSpecialAttribute(businessObject.getClassName(), businessObject.getId(), RELATIONSHIP_HASPROXY))
                relationshipTypeList.add(
                        mem.isSubclassOf(Constants.CLASS_GENERICPROXY, businessObject.getClassName())
                                ? new RelationshipType(RELATIONSHIP_HASPROXY, String.format(ts.getTranslatedString("module.general.labels.relationship.other-resources"),
                                        ts.getTranslatedString("module.general.labels.relationship.proxy")))
                                : new RelationshipType(RELATIONSHIP_HASPROXY, ts.getTranslatedString("module.general.labels.relationship.proxy"))
                );
            // Has service?
            if (bem.hasSpecialAttribute(businessObject.getClassName(), businessObject.getId(), RELATIONSHIP_USES))
                relationshipTypeList.add(
                        mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, businessObject.getClassName())
                                ? new RelationshipType(RELATIONSHIP_USES, ts.getTranslatedString("module.general.labels.relationship.network-resources"))
                                : new RelationshipType(RELATIONSHIP_USES, ts.getTranslatedString("module.general.labels.relationship.service"))
                );
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return relationshipTypeList;
    }
    
    /**
     * Creates the grid with the inventory objects related to the selected business object.
     * @param businessObject The selected business object.
     * @param relationshipName The selected relationship name.
     */
    private void buildObjectsGrid(BusinessObjectLight businessObject, String relationshipName) {
        try {
            List<BusinessObjectLight> ObjectList = bem.getSpecialAttribute(
                    businessObject.getClassName(),
                    businessObject.getId(),
                    relationshipName
            );
            
            lytObjects.removeAll();
            if (ObjectList != null && !ObjectList.isEmpty()) {
                ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(ObjectList);
                // The grid with the list objects
                Grid<BusinessObjectLight> gridObjects = new Grid<>();
                gridObjects.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                        GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
                gridObjects.setDataProvider(dataProvider);
                gridObjects.setSelectionMode(Grid.SelectionMode.MULTI);
                gridObjects.setWidthFull();
                gridObjects.setHeightFull();
                
                Grid.Column<BusinessObjectLight> column = gridObjects.addComponentColumn(anObject -> {
                    ActionButton btnShowInfo = new ActionButton(new ActionIcon(VaadinIcon.INFO_CIRCLE),
                             ts.getTranslatedString("module.navigation.actions.show-more-information-button-name"));
                    btnShowInfo.setWidth("10px");
                    
                    btnShowInfo.addClickListener(event -> {
                        this.windowMoreInformation.getVisualComponent(new ModuleActionParameterSet(
                                new ModuleActionParameter("object", anObject))).open();
                    });
                    
                    Label lblObjectName = new Label(anObject.getName());
                    lblObjectName.setWidthFull();
                    
                    HorizontalLayout lytRow = new HorizontalLayout(lblObjectName, btnShowInfo);
                    lytRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
                    return lytRow;
                });
                
                gridObjects.asMultiSelect().addSelectionListener(event -> {
                    selectedObjectList = new ArrayList<>();
                    windowRelease.getBtnConfirm().setEnabled((event.getValue() != null && !event.getValue().isEmpty()));
                    Collection<BusinessObjectLight> newValues = event.getValue();
                    newValues.forEach(value -> selectedObjectList.add(value));
                });
                
                // Filter business object by name
                HeaderRow filterRow = gridObjects.appendHeaderRow();
                TextField txtClassName = createTxtFieldClassName(dataProvider);
                filterRow.getCell(column).setComponent(txtClassName);
                
                lytObjects.add(gridObjects);
                windowRelease.setMinHeight("400px");
            }
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Create a new input field to classes in the header row.
     * @param dataProvider Data provider to filter.
     * @return The new input field filter.
     */
    private TextField createTxtFieldClassName(ListDataProvider<BusinessObjectLight> dataProvider) {
        Icon icon = VaadinIcon.SEARCH.create();
        icon.setClassName("icon-filter");

        TextField txtClassName = new TextField();
        txtClassName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        
        txtClassName.setValueChangeMode(ValueChangeMode.EAGER);
        txtClassName.setWidthFull();
        txtClassName.setSuffixComponent(icon);
        txtClassName.addValueChangeListener(e -> dataProvider.addFilter(
                classes -> StringUtils.containsIgnoreCase(classes.getName(), txtClassName.getValue())
        ));
        return txtClassName;
    }

    @Override
    public AbstractAction getModuleAction() {
        return releaseFromAction;
    }
    
    @Override
    public String appliesTo() {
        return Constants.CLASS_INVENTORYOBJECT;
    }
}