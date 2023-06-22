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
package org.neotropic.kuwaiba.modules.core.datamodelman;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import elemental.json.Json;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.io.IOUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.modules.core.datamodelman.nodes.DataModelNode;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.datamodelman.actions.DeleteAttributeVisualAction;
import org.neotropic.kuwaiba.modules.core.datamodelman.actions.DeleteClassVisualAction;
import org.neotropic.kuwaiba.modules.core.datamodelman.actions.NewAttributeVisualAction;
import org.neotropic.kuwaiba.modules.core.datamodelman.actions.NewClassVisualAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.NavTreeGrid;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Data Model manager module. This class manages how the pages
 * corresponding to different functionalities are presented in a single place.
 *
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "dmman", layout = DataModelManagerLayout.class)
public class DataModelManagerUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle, AbstractUI {

    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * factory to build resources from data source
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * the visual action to create a new class
     */
    @Autowired
    private NewClassVisualAction newClassVisualAction;
    /**
     * the visual action to delete a class
     */
    @Autowired
    private DeleteClassVisualAction deleteClassVisualAction;
    /**
     * the visual action to create a new attribute
     */
    @Autowired
    private NewAttributeVisualAction newAttributeVisualAction;
    /**
     * the visual action to delete attribute
     */
    @Autowired
    private DeleteAttributeVisualAction deleteAttributeVisualAction;
    /**
     * sheet for general Attributes
     */
    private PropertySheet propsheetGeneralAttributes;
    /**
     * sheet for class Attributes properties
     */
    private PropertySheet propsheetClassAttributes;
    /**
     * current selected class
     */
    private ClassMetadataLight selectedClass;
    /**
     * grid to list class attributes
     */
    private Grid<AttributeMetadata> tblClassAttributes;
    /**
     * current selected class attribute
     */
    private AttributeMetadata selectedAttribute;
    /**
     * upload control to class icon
     */
    private Upload uploadIcon;
    /**
     * upload control to small class icon
     */
    private Upload uploadSmallIcon;
    /**
     * icon class image
     */
    private Image iconImage;
    /**
     * upload control to small class icon
     */
    private Image smallIconImage;
    /**
     * layout to show class attributes property sheet
     */
    private VerticalLayout lytPropSheetClassAttributes;
    /**
     * contains class icons
     */
    private VerticalLayout lytIcons;
    /**
     * Label class name
     */
    private BoldLabel lblClassName;
    private HorizontalLayout lytBreadCumb;
    /**
     * Classes grid
     */
    private NavTreeGrid<DataModelNode> gridClasses;
    /**
     * List types grid
     */
    private NavTreeGrid<DataModelNode> gridListTypes;
    /**
     * An icon generator for create icons
     */
    private ClassNameIconGenerator iconGenerator;
    /**
     * Filters for class grids and list type
     */
    private ComboBox<ClassMetadataLight> cmbClassFilter;
    private ComboBox<ClassMetadataLight> cmbListTypeFilter;
    /**
     * Main layouts
     */
    private VerticalLayout lytTabs;
    private VerticalLayout lytNavTree;
    private HorizontalLayout lytClassActions;
    private VerticalLayout lytSecondary;
    private VerticalLayout lytAttributes;
    private VerticalLayout lytClasses;
    private VerticalLayout lytListTypes;
    /**
     * Buttons for class actions
     */
    private ActionButton btnAddClass;
    private ActionButton btnDeleteClass;
    /**
     * Buttons for attribute actions
     */
    private ActionButton btnAddAttribute;
    private ActionButton btnDeleteAttribute;
    /**
     * Main tabs
     */
    private Tabs tabsRoot;
    private Tab tabClasses;
    private Tab tabListTypes;
    /**
     * Split layout
     */
    private SplitLayout splitLayout;

    public DataModelManagerUI() {
        super();
        setSizeFull();
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newClassVisualAction.unregisterListener(this);
        this.deleteClassVisualAction.unregisterListener(this);
        this.newAttributeVisualAction.unregisterListener(this);
        this.deleteAttributeVisualAction.unregisterListener(this);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {

            if (tabsRoot.getSelectedTab().equals(tabClasses)) {//If it's a class
                if (gridClasses != null) {
                    if (ev.getActionResponse() != null
                            && ev.getActionResponse().containsKey(NewAttributeVisualAction.PARAM_CLASS)
                            && ev.getActionResponse().get(NewAttributeVisualAction.PARAM_CLASS) != null) {

                        if (ev.getActionResponse().containsKey(NewAttributeVisualAction.PARAM_ATTRIBUTE)
                                && ev.getActionResponse().get(NewAttributeVisualAction.PARAM_ATTRIBUTE) != null) {

                            updateElements();
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                                    AbstractNotification.NotificationType.INFO, ts).open();
                        } else {
                            try {
                                DataModelNode affectedNode = gridClasses.findNodeById(String.valueOf(((ClassMetadataLight) ev.getActionResponse().get(NewClassVisualAction.PARAM_CLASS)).getId())).get();

                                if (ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD))
                                    gridClasses.update(affectedNode);
                                else if (ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)) {
                                    gridClasses.remove(affectedNode);
                                    clearElements();
                                }
                                
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                                AbstractNotification.NotificationType.INFO, ts).open();
                            } catch (NoSuchElementException ex) {
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                        String.format(ts.getTranslatedString("apis.persistence.mem.messages.class-not-found"),
                                                ((ClassMetadataLight) ev.getActionResponse().get(NewClassVisualAction.PARAM_CLASS)).getName()),
                                        AbstractNotification.NotificationType.ERROR, ts).open();
                            }
                        }
                    }
                }
            } else if (tabsRoot.getSelectedTab().equals(tabListTypes)) {//If it's a list type
                if (gridListTypes != null) {
                    if (ev.getActionResponse() != null
                            && ev.getActionResponse().containsKey(NewAttributeVisualAction.PARAM_CLASS)
                            && ev.getActionResponse().get(NewAttributeVisualAction.PARAM_CLASS) != null) {

                        if (ev.getActionResponse().containsKey(NewAttributeVisualAction.PARAM_ATTRIBUTE)
                                && ev.getActionResponse().get(NewAttributeVisualAction.PARAM_ATTRIBUTE) != null) {
                            
                            updateElements();
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                                AbstractNotification.NotificationType.INFO, ts).open();
                        } else {
                            try {
                                DataModelNode affectedNode = gridListTypes.findNodeById(String.valueOf(((ClassMetadataLight) ev.getActionResponse().get(NewClassVisualAction.PARAM_CLASS)).getId())).get();
                                if (ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD))
                                    gridListTypes.update(affectedNode);
                                else if (ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)) {
                                    gridListTypes.remove(affectedNode);
                                    clearElements();
                                }
                                
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(),
                                AbstractNotification.NotificationType.INFO, ts).open();
                            } catch (NoSuchElementException ex) {
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                        String.format(ts.getTranslatedString("apis.persistence.mem.messages.class-not-found"),
                                                ((ClassMetadataLight) ev.getActionResponse().get(NewClassVisualAction.PARAM_CLASS)).getName()),
                                        AbstractNotification.NotificationType.ERROR, ts).open();
                            }
                        }
                    }
                }
            }
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
    }

    @Override
    public void initContent() {
        setSizeFull();
        lblClassName = new BoldLabel();
        lytBreadCumb = new HorizontalLayout();

        // in case we are updating the page
        this.newClassVisualAction.unregisterListener(this);
        this.deleteClassVisualAction.unregisterListener(this);
        this.newAttributeVisualAction.unregisterListener(this);
        this.deleteAttributeVisualAction.unregisterListener(this);
        
        // register action completed
        this.newClassVisualAction.registerActionCompletedLister(this);
        this.deleteClassVisualAction.registerActionCompletedLister(this);
        this.newAttributeVisualAction.registerActionCompletedLister(this);
        this.deleteAttributeVisualAction.registerActionCompletedLister(this);

        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);

        iconGenerator = new ClassNameIconGenerator(resourceFactory);
        initializePropSheetGenericAttributes();
        initializeGridClassAttributes();
        initializePropSheetClassAttributes();
        initializeIconUploaders();

        setupLayouts();
        createActions();

        createClassTabs();
        splitLayout.addToPrimary(lytTabs);

        setPadding(false);
        add(splitLayout);
    }

    private void setupLayouts() {
        if (lytTabs == null) {
            lytTabs = new VerticalLayout();
            lytTabs.setId("lytTabs");
            lytTabs.setHeightFull();
            lytTabs.setSpacing(true);
            lytTabs.setMargin(false);
        } 
        if (lytNavTree == null) {
            lytNavTree = new VerticalLayout();
            lytNavTree.setId("lytNavTree");
            lytNavTree.setPadding(false);
            lytNavTree.setSpacing(false);
            lytNavTree.setMargin(false);
            lytNavTree.setSizeFull();
        }
        if (lytClassActions == null) {
            lytClassActions = new HorizontalLayout();
            lytClassActions.setId("lytClassActions");
            lytClassActions.setSpacing(false);
            lytClassActions.setWidthFull();
            lytClassActions.setJustifyContentMode(JustifyContentMode.END);
        }
        if (lytSecondary == null) {
            lytSecondary = new VerticalLayout();
            lytSecondary.setId("lytSecondary");
            lytSecondary.setSizeFull();
        }
        if (lytAttributes == null) {
            lytAttributes = new VerticalLayout();
            lytAttributes.setId("lytAttributes");
            lytAttributes.setSpacing(false);
            lytAttributes.setPadding(false);
            lytAttributes.setSizeFull();
        }
    }

    private void createActions() {
        btnAddClass = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                this.newClassVisualAction.getModuleAction().getDisplayName());
        btnAddClass.addClickListener(event -> {
            if (selectedClass != null) {
                this.newClassVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(NewClassVisualAction.PARAM_CLASS, selectedClass))).open();
            } else {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                        ts.getTranslatedString("module.datamodelman.messages.class-unselected"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
            }
        });

        btnDeleteClass = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteClassVisualAction.getModuleAction().getDisplayName());
        btnDeleteClass.addClickListener(event -> {
            if (selectedClass != null) {
                this.deleteClassVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("class", selectedClass))).open();
            } else {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.datamodelman.messages.class-unselected"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
            }
        });
        btnDeleteClass.setEnabled(false);

        lytClassActions.add(btnAddClass, btnDeleteClass);

        // now create the tabs for class attributes
        createAttributeTabs();
    }

    private void createClassTabs() {
        try {
            tabClasses = new Tab(ts.getTranslatedString("module.datamodelman.inventory-classes"));
            tabListTypes = new Tab(ts.getTranslatedString("module.datamodelman.list-types"));

            Div pagClasses = new Div();
            Map<Tab, Component> tabsToPages = new HashMap<>();
            tabsToPages.put(tabClasses, pagClasses);
            tabsToPages.put(tabListTypes, pagClasses);

            tabsRoot = new Tabs(tabClasses, tabListTypes);
            tabsRoot.setFlexGrowForEnclosedTabs(1);
            tabsRoot.setSelectedTab(tabClasses);
            tabsRoot.addSelectedChangeListener(event -> {
                if (event.getSelectedTab().equals(tabClasses)) {
                    lytListTypes.setVisible(false);
                    lytClasses.setVisible(true);
                } else if (event.getSelectedTab().equals(tabListTypes)) {
                    try {
                        if (cmbListTypeFilter == null) {
                            ClassMetadata rootClass = mem.getClass(Constants.CLASS_GENERICOBJECTLIST);
                            List<ClassMetadataLight> listTypeClasses = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, true, true);
                            createListTypeFilter(listTypeClasses, rootClass);
                        }
                        lytClasses.setVisible(false);
                        lytListTypes.setVisible(true);
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                String.format(ts.getTranslatedString("apis.persistence.mem.messages.class-not-found"),
                                         Constants.CLASS_GENERICOBJECTLIST),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                }
            });

            // First load class inventory object
            if (cmbClassFilter == null) {
                ClassMetadata rootClass = mem.getClass(Constants.CLASS_INVENTORYOBJECT);
                List<ClassMetadataLight> inventoryObjectClasses = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
                createClassFilter(inventoryObjectClasses, rootClass);
            }    
            
            pagClasses.add(lytNavTree);
            pagClasses.setSizeFull();
            pagClasses.setWidthFull();

            lytTabs.add(tabsRoot, pagClasses);
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    String.format(ts.getTranslatedString("apis.persistence.mem.messages.class-not-found"), Constants.CLASS_INVENTORYOBJECT),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void createClassFilter(List<ClassMetadataLight> classes, ClassMetadataLight rootClass) {
        cmbClassFilter = new ComboBox<>(ts.getTranslatedString("module.general.labels.filter"));
        cmbClassFilter.setItemLabelGenerator(ClassMetadataLight::getName);
        cmbClassFilter.setClearButtonVisible(true);
        cmbClassFilter.setAllowCustomValue(false);
        cmbClassFilter.setItems(classes);
        cmbClassFilter.setWidthFull();

        cmbClassFilter.addValueChangeListener(ev -> {
            if (ev.getValue() != null)
                buildClassesGrid(ev.getValue());
            else
                buildClassesGrid(rootClass);
            clearElements();
        });
        cmbClassFilter.setValue(rootClass);
    }

    private void createListTypeFilter(List<ClassMetadataLight> classes, ClassMetadataLight rootClass) {
        cmbListTypeFilter = new ComboBox<>(ts.getTranslatedString("module.general.labels.filter"));
        cmbListTypeFilter.setItemLabelGenerator(ClassMetadataLight::getName);
        cmbListTypeFilter.setClearButtonVisible(true);
        cmbListTypeFilter.setAllowCustomValue(false);
        cmbListTypeFilter.setItems(classes);
        cmbListTypeFilter.setWidthFull();

        cmbListTypeFilter.addValueChangeListener(ev -> {
            if (ev.getValue() != null)
                buildListTypesGrid(ev.getValue());
            else
                buildListTypesGrid(rootClass);                
            clearElements();
        });
        cmbListTypeFilter.setValue(rootClass);
    }

    private void buildClassesGrid(ClassMetadataLight rootClass) {        
        if (lytClasses == null) {
            lytClasses = new VerticalLayout();
            lytClasses.setId("lytClasses");
            lytClasses.setPadding(false);
            lytClasses.setSpacing(false);
            lytClasses.setMargin(false);
            lytClasses.setSizeFull();            
            lytClasses.add(cmbClassFilter);
            lytNavTree.add(lytClasses);
        } else
            lytClasses.remove(gridClasses);
        
        try {
            gridClasses = new NavTreeGrid<DataModelNode>() {
                @Override
                public List<DataModelNode> fetchData(DataModelNode node) {
                    List<DataModelNode> childrenNodes = new ArrayList<>();
                    try {
                        if (node.getName().equals(rootClass.getParentClassName()))
                            childrenNodes.add(new DataModelNode(rootClass));
                        else {
                            List<ClassMetadataLight> children = mem.getSubClassesLightNoRecursive(node.getName(), true, false);
                            children.forEach(child -> childrenNodes.add(new DataModelNode(child)));
                        }
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                    return childrenNodes;
                }
            };
            
            ClassMetadataLight parentClass = mem.getClass(rootClass.getParentClassName());
            gridClasses.createDataProvider(new DataModelNode((ClassMetadataLight) parentClass));
            gridClasses.addComponentHierarchyColumn(item -> {
                IconNameCellGrid node = new IconNameCellGrid(item.getObject().getName(), item.getObject().getName(), iconGenerator);
                HorizontalLayout lytValue = new HorizontalLayout(node);
                return lytValue;
            });

            gridClasses.addItemClickListener(e -> {
                selectedClass = e.getItem().getObject();
                loadAttributes();
            });

            gridClasses.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridClasses.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridClasses.setHeightByRows(true);
            lytClasses.add(gridClasses);
        } catch (IllegalArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void buildListTypesGrid(ClassMetadataLight rootClass) {
        if (lytListTypes == null) {
            lytListTypes = new VerticalLayout();
            lytListTypes.setId("lytListTypes");
            lytListTypes.setPadding(false);
            lytListTypes.setSpacing(false);
            lytListTypes.setMargin(false);
            lytListTypes.setSizeFull();
            lytListTypes.add(cmbListTypeFilter);
            lytNavTree.add(lytListTypes);
        } else
            lytListTypes.remove(gridListTypes);

        try {
            gridListTypes = new NavTreeGrid<DataModelNode>() {
                @Override
                public List<DataModelNode> fetchData(DataModelNode node) {
                    List<DataModelNode> childrenNodes = new ArrayList<>();
                    try {
                        if (node.getName().equals(rootClass.getParentClassName()))
                            childrenNodes.add(new DataModelNode(rootClass));
                        else {
                            List<ClassMetadataLight> children = mem.getSubClassesLightNoRecursive(node.getName(), true, false);
                            children.forEach(child -> childrenNodes.add(new DataModelNode(child)));
                        }
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                    return childrenNodes;
                }
            };

            ClassMetadataLight parentClass = mem.getClass(rootClass.getParentClassName());
            gridListTypes.createDataProvider(new DataModelNode((ClassMetadataLight) parentClass));
            gridListTypes.addComponentHierarchyColumn(item -> {
                IconNameCellGrid node = new IconNameCellGrid(item.getObject().getName(), item.getObject().getName(), iconGenerator);
                HorizontalLayout lytValue = new HorizontalLayout(node);
                return lytValue;
            });

            gridListTypes.addItemClickListener(item -> {
                selectedClass = item.getItem().getObject();
                loadAttributes();
            });

            gridListTypes.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
            gridListTypes.setSelectionMode(Grid.SelectionMode.SINGLE);
            gridListTypes.setHeightByRows(true);
            lytListTypes.add(gridListTypes);
        } catch (IllegalArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void updateItemName(long id, String newName) {
        if (tabsRoot.getSelectedTab().equals(tabClasses)) {
            if (gridClasses != null) {
                for (DataModelNode node : gridClasses.getAllNodesAsList()) {
                    if (node.getObject().getId() == id && gridClasses.getTreeData().contains(node)) {
                        node.getObject().setName(newName);
                        node.setName(newName);
                        gridClasses.getDataProvider().refreshItem(node);
                        break;
                    }
                }
            }
        } else if (tabsRoot.getSelectedTab().equals(tabListTypes)) {
            if (gridListTypes != null) {
                for (DataModelNode node : gridListTypes.getAllNodesAsList()) {
                    if (node.getObject().getId() == id && gridListTypes.getTreeData().contains(node)) {
                        node.getObject().setName(newName);
                        node.setName(newName);
                        gridListTypes.getDataProvider().refreshItem(node);
                        break;
                    }
                }
            }
        }
    }
    
    private void updateItemIcon(long id, byte[] imageData) {
         if (tabsRoot.getSelectedTab().equals(tabClasses)) {
            if (gridClasses != null) {
                for (DataModelNode node : gridClasses.getAllNodesAsList()) {
                    if (node.getObject().getId() == id && gridClasses.getTreeData().contains(node)) {
                        node.getObject().setSmallIcon(imageData);
                        gridClasses.getDataProvider().refreshItem(node);
                        break;
                    }
                }
            }
        } else if (tabsRoot.getSelectedTab().equals(tabListTypes)) {
            if (gridListTypes != null) {
                for (DataModelNode node : gridListTypes.getAllNodesAsList()) {
                    if (node.getObject().getId() == id && gridListTypes.getTreeData().contains(node)) {
                        node.getObject().setSmallIcon(imageData);
                        gridListTypes.getDataProvider().refreshItem(node);
                        break;
                    }
                }
            }
        }
    }
    
    private void updateItemColor(long id, int color) {
        if (tabsRoot.getSelectedTab().equals(tabClasses)) {
            if (gridClasses != null) {
                for (DataModelNode node : gridClasses.getAllNodesAsList()) {
                    if (node.getObject().getId() == id && gridClasses.getTreeData().contains(node)) {
                        node.getObject().setColor(color);
                        gridClasses.getDataProvider().refreshItem(node);
                        break;
                    }
                }
            }
        } else if (tabsRoot.getSelectedTab().equals(tabListTypes)) {
            if (gridListTypes != null) {
                for (DataModelNode node : gridListTypes.getAllNodesAsList()) {
                    if (node.getObject().getId() == id && gridListTypes.getTreeData().contains(node)) {
                        node.getObject().setColor(color);
                        gridListTypes.getDataProvider().refreshItem(node);
                        break;
                    }
                }
            }
        }
    }

    private void createAttributeTabs() {
        Tab tabProperties = new Tab(ts.getTranslatedString("module.datamodelman.properties"));
        Tab tabAttributes = new Tab(ts.getTranslatedString("module.datamodelman.class-attributes"));

        Div pageAttribute = new Div();
        Map<Tab, Component> tabsToAttribute = new HashMap<>();
        tabsToAttribute.put(tabProperties, pageAttribute);
        tabsToAttribute.put(tabAttributes, pageAttribute);

        Tabs tabsAttribute = new Tabs(tabProperties, tabAttributes);
        tabsAttribute.setFlexGrowForEnclosedTabs(1);
        tabsAttribute.setSelectedTab(tabProperties);
        tabsAttribute.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(tabProperties))
                createClassProperties();
            else
                createClassAttributes();
        });

        HorizontalLayout lytHeaderContent = new HorizontalLayout(lblClassName, lytBreadCumb, lytClassActions);
        lytHeaderContent.setAlignItems(Alignment.CENTER);
        lytHeaderContent.setWidthFull();

        VerticalLayout lytContent = new VerticalLayout(lytHeaderContent, tabsAttribute);
        lytContent.setSpacing(false);
        lytSecondary.add(lytContent);

        createClassProperties();
        lytSecondary.add(lytAttributes);

        splitLayout.addToSecondary(lytSecondary);
    }

    private void createClassAttributes() {
        lytAttributes.removeAll();

        btnAddAttribute = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O),
                 this.newAttributeVisualAction.getModuleAction().getDisplayName());
        btnAddAttribute.addClickListener(event -> {
            if (selectedClass != null) {
                this.newAttributeVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("class", selectedClass))).open();
            } else {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.datamodelman.messages.class-unselected"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
            }
        });

        btnDeleteAttribute = new ActionButton(new ActionIcon(VaadinIcon.TRASH),
                this.deleteAttributeVisualAction.getModuleAction().getDisplayName());
        btnDeleteAttribute.addClickListener(event -> {
            if (selectedClass != null && selectedAttribute != null) {
                this.deleteAttributeVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("class", selectedClass),
                        new ModuleActionParameter("attribute", selectedAttribute))).open();
            } else {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"),
                         ts.getTranslatedString("module.datamodelman.messages.class-unselected"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
            }
        });
        btnDeleteAttribute.setEnabled(false);

        HorizontalLayout lytActions = new HorizontalLayout(btnAddAttribute, btnDeleteAttribute);
        lytActions.setSpacing(false);
        
        VerticalLayout lytListClassAttributes = new VerticalLayout(lytActions, tblClassAttributes);

        lytPropSheetClassAttributes = new VerticalLayout(new H4(ts.getTranslatedString("module.datamodelman.attributes")), propsheetClassAttributes);
        lytPropSheetClassAttributes.setSpacing(false);
        lytPropSheetClassAttributes.setVisible(false);

        HorizontalLayout lytClassAttributes = new HorizontalLayout(lytListClassAttributes, lytPropSheetClassAttributes);
        lytClassAttributes.setSizeFull();

        lytAttributes.add(lytClassAttributes);
    }

    private void createClassProperties() {
        lytAttributes.removeAll();

        BoldLabel lblIcon = new BoldLabel(ts.getTranslatedString("module.datamodelman.icon"));
        lblIcon.setClassName("lbl-icon-dmman");

        Div divIcon = new Div(iconImage);

        ActionButton btnRemoveIcon = new ActionButton(new ActionIcon(VaadinIcon.CLOSE_CIRCLE_O, ts.getTranslatedString("module.datamodelman.remove-icon")));
        btnRemoveIcon.addClickListener(event -> {
            byte[] imageData = new byte[0];
            if (selectedClass != null) {
                try {
                    HashMap<String, Object> newSmallIcon = new HashMap<>();
                    newSmallIcon.put(Constants.PROPERTY_ICON, imageData);
                    mem.setClassProperties(selectedClass.getId(), newSmallIcon);
                    updateIconImages();

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        });

        ActionButton btnRemoveSmallIcon = new ActionButton(new ActionIcon(VaadinIcon.CLOSE_CIRCLE_O), ts.getTranslatedString("module.datamodelman.remove-icon"));
        btnRemoveSmallIcon.addClickListener(event -> {
            byte[] imageData = new byte[0];
            if (selectedClass != null) {
                try {
                    HashMap<String, Object> newSmallIcon = new HashMap<>();
                    newSmallIcon.put(Constants.PROPERTY_SMALL_ICON, imageData);
                    mem.setClassProperties(selectedClass.getId(), newSmallIcon);
                    updateIconImages();
                    updateItemIcon(selectedClass.getId(), imageData);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        });

        HorizontalLayout lytClassIcon = new HorizontalLayout(lblIcon, divIcon, btnRemoveIcon, uploadIcon);
        lytClassIcon.setSpacing(true);
        lytClassIcon.setAlignItems(Alignment.CENTER);

        // Small Icon
        BoldLabel lblSmallIcon = new BoldLabel(ts.getTranslatedString("module.datamodelman.smallicon"));
        lblSmallIcon.setClassName("lbl-icon-dmman");

        Div divSmallIcon = new Div(smallIconImage);
        divSmallIcon.setClassName("div-icon-dmman");

        HorizontalLayout lytSmallClassIcon = new HorizontalLayout(lblSmallIcon, divSmallIcon, btnRemoveSmallIcon, uploadSmallIcon);
        lytSmallClassIcon.setSpacing(true);
        lytSmallClassIcon.setAlignItems(Alignment.CENTER);

        BoldLabel lblInfoFile = new BoldLabel(String.format("%s.     %s: %s bytes",
                ts.getTranslatedString("module.datamodelman.accepted-icon-file-types"),
                ts.getTranslatedString("module.datamodelman.max-size"),
                Constants.MAX_ICON_SIZE_IN_BYTES));
        lblInfoFile.setClassName("text-secondary");

        lytIcons = new VerticalLayout(new H4(ts.getTranslatedString("module.datamodelman.icons")),
                lblInfoFile, lytClassIcon, lytSmallClassIcon);
        lytIcons.setVisible(true);
        lytIcons.setSpacing(true);
        lytIcons.setPadding(false);

        VerticalLayout lytGeneralAttributes = new VerticalLayout(propsheetGeneralAttributes);
        lytGeneralAttributes.setSpacing(false);
        lytGeneralAttributes.setPadding(false);

        lytAttributes.add(lytGeneralAttributes, lytIcons);
    }

    private void loadAttributes() {
        try {
            updatePropertySheetGeneralAttributes();
            updateGridClassAttributes(selectedClass);
            updateIconImages();

            propsheetClassAttributes.clear();

            if (lytPropSheetClassAttributes != null)
                lytPropSheetClassAttributes.setVisible(false);
            if (lytIcons != null)
                lytIcons.setVisible(true);
            if (btnDeleteAttribute != null)
                btnDeleteAttribute.setEnabled(false);
            if (btnDeleteClass != null)
                btnDeleteClass.setEnabled(true);

            List<ClassMetadataLight> parents = mem.getUpstreamClassHierarchy(selectedClass.getName(), false);
            lblClassName.setText(selectedClass.getName());

            lytBreadCumb.removeAll();
            lytBreadCumb.add(createParentBreadCrumbs(parents, 2));
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * Creates/updates the localization path, that shows the whole list of the
     * parents of the selected object in the tree
     *
     * @param selectedItemParents the selected object in the location tree
     * @param kind the kind of bread crumbs if is location or device
     */
    private Div createParentBreadCrumbs(List<ClassMetadataLight> selectedItemParents, int kind) {
        Div divPowerline = new Div();
        divPowerline.setWidthFull();
        divPowerline.setHeight("20px");
        divPowerline.setClassName("parents-breadcrumbs");

        List<ClassMetadataLight> parents = new ArrayList(selectedItemParents);
        Collections.reverse(parents);
        selectedItemParents.forEach(parent -> {
            Span span = new Span(new Label(parent.getName().equals(Constants.DUMMY_ROOT) ? "/" : parent.getName()));
            span.setSizeUndefined();
            span.setTitle(String.format("[%s]", parent.getName()));
            span.addClassNames("parent", kind == 1 ? "location-parent-color" : "device-parent-color");
            divPowerline.add(span);
        });

        return divPowerline;
    }

    private void updatePropertySheetGeneralAttributes() {
        try {
            ClassMetadata classMetadata = mem.getClass(selectedClass.getName());
            propsheetGeneralAttributes.setItems(PropertyFactory.generalPropertiesFromClass(classMetadata, ts));
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void updatePropertySheetClassAttributes() {
        try {
            propsheetClassAttributes.setItems(PropertyFactory.generalPropertiesFromAttribute(selectedAttribute, mem, ts));
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.general.messages.unexpected-error"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void initializePropSheetGenericAttributes() {
        propsheetGeneralAttributes = new PropertySheet(ts, new ArrayList<>());
        propsheetGeneralAttributes.addPropertyValueChangedListener((AbstractProperty<? extends Object> property) -> {
            try {
                if (selectedClass != null) {
                    boolean changeColor = false;
                    HashMap<String, Object> newProperties = new HashMap<>();
                    switch (property.getName()) {
                        case Constants.PROPERTY_NAME:
                            updateItemName(selectedClass.getId(), property.getValue().toString());
                            newProperties.put(Constants.PROPERTY_NAME, property.getValue());
                            selectedClass.setName(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_DISPLAY_NAME:
                            newProperties.put(Constants.PROPERTY_DISPLAY_NAME, property.getValue());
                            selectedClass.setDisplayName(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_DESCRIPTION:
                            newProperties.put(Constants.PROPERTY_DESCRIPTION, property.getValue());
                            break;
                        case Constants.PROPERTY_ABSTRACT:
                            newProperties.put(Constants.PROPERTY_ABSTRACT, property.getValue());
                            selectedClass.setAbstract((Boolean) property.getValue());
                            break;
                        case Constants.PROPERTY_IN_DESIGN:
                            newProperties.put(Constants.PROPERTY_IN_DESIGN, property.getValue());
                            selectedClass.setInDesign((Boolean) property.getValue());
                            break;
                        case Constants.PROPERTY_COUNTABLE:
                            newProperties.put(Constants.PROPERTY_COUNTABLE, property.getValue());
                            break;
                        case Constants.PROPERTY_COLOR:
                            int color = Color.decode((String) property.getValue()).getRGB();
                            newProperties.put(Constants.PROPERTY_COLOR, color);
                            selectedClass.setColor(color);
                            changeColor = true;
                            break;
                    }
                    mem.setClassProperties(selectedClass.getId(), newProperties);
                    updatePropertySheetGeneralAttributes();
                    
                    if (changeColor)
                        updateItemColor(selectedClass.getId(), selectedClass.getColor());
                    
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                }
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                    | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                propsheetGeneralAttributes.undoLastEdit();
            }
        });
    }

    private void initializeGridClassAttributes() {
        tblClassAttributes = new Grid();
        tblClassAttributes.addThemeVariants(GridVariant.LUMO_COMPACT);
        tblClassAttributes.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
        tblClassAttributes.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        tblClassAttributes.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        tblClassAttributes.addColumn(item -> {
            String value = item.isMandatory() ? (item.getName() + " *") : item.getName();
            value += item.getDisplayName() != null && !item.getDisplayName().isEmpty()
                    ? " (" + item.getDisplayName() + ")"
                    : " (" + ts.getTranslatedString("module.datamodelman.display-name-not-set") + ")";
            return value;
        })
                .setHeader(ts.getTranslatedString("module.general.labels.attribute-name"))
                .setKey(ts.getTranslatedString("module.general.labels.name"));

        tblClassAttributes.addItemClickListener(ev -> {
            try {
                selectedAttribute = ev.getItem();
                updatePropertySheetClassAttributes();
                lytPropSheetClassAttributes.setVisible(true);
                btnDeleteAttribute.setEnabled(true);
            } catch (Exception ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
    }

    private void updateGridClassAttributes(ClassMetadataLight object) {
        try {
            if (object != null) {
                ClassMetadata classMetadata = mem.getClass(object.getName());
                tblClassAttributes.setItems(classMetadata.getAttributes());
                tblClassAttributes.getDataProvider().refreshAll();
            } else {
                tblClassAttributes.setItems(new ArrayList<>());
            }
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void initializePropSheetClassAttributes() {
        propsheetClassAttributes = new PropertySheet(ts, new ArrayList<>());
        propsheetClassAttributes.addPropertyValueChangedListener((AbstractProperty<? extends Object> property) -> {
            try {
                if (selectedAttribute != null && selectedClass != null) {
                    HashMap<String, Object> newProperties = new HashMap<>();

                    switch (property.getName()) {
                        case Constants.PROPERTY_NAME:
                            newProperties.put(Constants.PROPERTY_NAME, property.getValue().toString());
                            break;
                        case Constants.PROPERTY_DISPLAY_NAME:
                            newProperties.put(Constants.PROPERTY_DISPLAY_NAME, property.getValue());
                            break;
                        case Constants.PROPERTY_DESCRIPTION:
                            newProperties.put(Constants.PROPERTY_DESCRIPTION, property.getValue());
                            break;
                        case Constants.PROPERTY_TYPE:
                            if (property.getValue() != null)
                                newProperties.put(Constants.PROPERTY_TYPE, property.getValue());
                            break;
                        case Constants.PROPERTY_MANDATORY:
                            newProperties.put(Constants.PROPERTY_MANDATORY, property.getValue());
                            break;
                        case Constants.PROPERTY_UNIQUE:
                            newProperties.put(Constants.PROPERTY_UNIQUE, property.getValue());
                            break;
                        case Constants.PROPERTY_MULTIPLE:
                            newProperties.put(Constants.PROPERTY_MULTIPLE, property.getValue());
                            break;
                        case Constants.PROPERTY_VISIBLE:
                            newProperties.put(Constants.PROPERTY_VISIBLE, property.getValue());
                            break;
                        case Constants.PROPERTY_ADMINISTRATIVE:
                            newProperties.put(Constants.PROPERTY_ADMINISTRATIVE, property.getValue());
                            break;
                        case Constants.PROPERTY_NO_COPY:
                            newProperties.put(Constants.PROPERTY_NO_COPY, property.getValue());
                            break;
                        case Constants.PROPERTY_ORDER:
                            newProperties.put(Constants.PROPERTY_ORDER, property.getValue());
                            break;
                    }

                    mem.setAttributeProperties(selectedClass.getId(), selectedAttribute.getId(), newProperties);
                    // Refresh Objects
                    selectedAttribute = mem.getAttribute(selectedClass.getId(), selectedAttribute.getId());
                    updateGridClassAttributes(selectedClass);
                    // Update Property Sheet
                    updatePropertySheetClassAttributes();
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                }
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                    | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                propsheetClassAttributes.undoLastEdit();
            }
        });
    }

    private void initializeIconUploaders() {
        iconImage = new Image();
        iconImage.setWidth(Constants.DEFAULT_ICON_SIZE);
        iconImage.setHeight(Constants.DEFAULT_ICON_SIZE);

        smallIconImage = new Image();
        smallIconImage.setWidth(Constants.DEFAULT_SMALL_ICON_SIZE);
        smallIconImage.setHeight(Constants.DEFAULT_SMALL_ICON_SIZE);

        MemoryBuffer bufferIcon = new MemoryBuffer();
        uploadIcon = new Upload(bufferIcon);
        uploadIcon.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        uploadIcon.setMaxFiles(1);
        uploadIcon.setDropLabel(new Label(ts.getTranslatedString("module.datamodelman.dropmessage")));
        uploadIcon.setMaxFileSize(Constants.MAX_ICON_SIZE_IN_BYTES);
        uploadIcon.addSucceededListener(event -> {
            try {
                byte[] imageData = IOUtils.toByteArray(bufferIcon.getInputStream());
                if (selectedClass != null) {
                    HashMap<String, Object> newIcon = new HashMap<>();
                    newIcon.put(Constants.PROPERTY_ICON, imageData);
                    mem.setClassProperties(selectedClass.getId(), newIcon);
                    
                    StreamResource resource = new StreamResource("icon", () -> bufferIcon.getInputStream());
                    iconImage.setSrc(resource);
                    uploadIcon.getElement().setPropertyJson("files", Json.createArray());
                    
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                }
            } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            } catch (IOException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ts.getTranslatedString("module.general.messages.unexpected-error"),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });

        uploadIcon.addFileRejectedListener(listener -> {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    listener.getErrorMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        });

        //small icon
        MemoryBuffer bufferSmallIcon = new MemoryBuffer();
        uploadSmallIcon = new Upload(bufferSmallIcon);
        uploadSmallIcon.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        uploadSmallIcon.setMaxFiles(1);
        uploadSmallIcon.setDropLabel(new Label(ts.getTranslatedString("module.datamodelman.dropmessage")));
        uploadSmallIcon.setMaxFileSize(Constants.MAX_ICON_SIZE_IN_BYTES);

        uploadSmallIcon.addSucceededListener(event -> {
            try {
                byte[] imageData = IOUtils.toByteArray(bufferSmallIcon.getInputStream());
                if (selectedClass != null) {
                    HashMap<String, Object> newSmallIcon = new HashMap<>();
                    newSmallIcon.put(Constants.PROPERTY_SMALL_ICON, imageData);
                    mem.setClassProperties(selectedClass.getId(), newSmallIcon);
                    updateItemIcon(selectedClass.getId(), imageData);
                    
                    StreamResource resource = new StreamResource("icon", () -> bufferSmallIcon.getInputStream());
                    smallIconImage.setSrc(resource);
                    uploadSmallIcon.getElement().setPropertyJson("files", Json.createArray());
                    
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                }
            } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
            } catch (IOException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ts.getTranslatedString("module.general.messages.unexpected-error"),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });

        uploadSmallIcon.addFileRejectedListener(listener
                -> new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        listener.getErrorMessage(), AbstractNotification.NotificationType.ERROR, ts).open());
    }

    private void updateIconImages() {
        try {
            ClassMetadata classMetadata = mem.getClass(selectedClass.getName());
            byte[] iconBytes = classMetadata.getIcon();
            if (iconBytes.length > 0) {
                StreamResource resource = new StreamResource("icon.jpg", () -> new ByteArrayInputStream(iconBytes));
                iconImage.setSrc(resource); // Icon 32X32
            } else {
                iconImage.setSrc("img/no_image.png");
            }
            //small icon
            byte[] smallIconBytes = classMetadata.getSmallIcon();
            if (smallIconBytes.length > 0) {
                StreamResource resource = new StreamResource("Small icon.jpg", () -> new ByteArrayInputStream(smallIconBytes));
                smallIconImage.setSrc(resource); // "Small Icon 16X16");
            } else {
                smallIconImage.setSrc("img/no_image.png");
            }
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void updateElements() {
        updateGridClassAttributes(selectedClass);
        selectedAttribute = null;
        propsheetClassAttributes.clear();
        btnDeleteAttribute.setEnabled(false);
    }

    private void clearElements() {
        //clear general attributes section
        propsheetGeneralAttributes.clear();
        lytIcons.setVisible(false);
        btnDeleteClass.setEnabled(false);

        selectedClass = null;
        selectedAttribute = null;

        //clear class attributes section
        updateGridClassAttributes(selectedClass);
        propsheetClassAttributes.clear();
        lblClassName.setText("");
        lytBreadCumb.removeAll();

        if (btnDeleteAttribute != null)
            btnDeleteAttribute.setEnabled(false);
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.datamodelman.title");
    }
}
