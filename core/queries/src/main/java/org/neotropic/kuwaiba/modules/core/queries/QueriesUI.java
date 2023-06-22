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

package org.neotropic.kuwaiba.modules.core.queries;

import com.neotropic.flow.component.aceeditor.AceEditor;
import com.neotropic.flow.component.aceeditor.AceMode;
import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.CompactQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ExtendedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultRecord;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueriesPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ExecutionException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectDashboard;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DefaultDeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.queries.actions.CreateQueryVisualAction;
import org.neotropic.kuwaiba.modules.core.queries.actions.DeleteScriptVisualAction;
import org.neotropic.kuwaiba.modules.core.queries.actions.DeleteScriptedQueryPoolVisualAction;
import org.neotropic.kuwaiba.modules.core.queries.actions.NewScriptedQueryPoolVisualAction;
import org.neotropic.kuwaiba.modules.core.queries.actions.NewScriptedQueryVisualAction;
import org.neotropic.kuwaiba.modules.core.queries.filters.AbstractFilter;
import org.neotropic.kuwaiba.modules.core.queries.filters.AbstractFilter.Criteria;
import org.neotropic.kuwaiba.modules.core.queries.filters.BooleanFilter;
import org.neotropic.kuwaiba.modules.core.queries.filters.DateFilter;
import org.neotropic.kuwaiba.modules.core.queries.filters.ListTypeFilter;
import org.neotropic.kuwaiba.modules.core.queries.filters.NumericFilter;
import org.neotropic.kuwaiba.modules.core.queries.filters.ParentFilter;
import org.neotropic.kuwaiba.modules.core.queries.filters.ResultRecordParser;
import org.neotropic.kuwaiba.modules.core.queries.filters.ResultScriptedQueryParser;
import org.neotropic.kuwaiba.modules.core.queries.filters.StringFilter;
import org.neotropic.kuwaiba.modules.core.queries.nodes.ObjectMxNode;
import org.neotropic.kuwaiba.modules.core.queries.nodes.ObjectMxNodeMap;
import org.neotropic.kuwaiba.modules.core.queries.nodes.PoolNode;
import org.neotropic.kuwaiba.modules.core.queries.nodes.ScriptNode;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.exporters.CSVFormatter;
import org.neotropic.util.visual.exporters.XMLExporter;
import org.neotropic.util.visual.exporters.grid.GridExporter;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.StringProperty;
import org.neotropic.util.visual.tree.nodes.AbstractNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

/**
 * Main for Queries. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "queries", layout = QueriesLayout.class)
public class QueriesUI extends VerticalLayout implements HasDynamicTitle, AbstractUI {
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * reference of the visual action to add a topology view
     */
    @Autowired
    private DefaultDeleteBusinessObjectVisualAction deleteBusinessObjectVisualAction;
    
    @Autowired
    private NewScriptedQueryPoolVisualAction newScriptedQueryPoolVisualAction;
    
    @Autowired
    private NewScriptedQueryVisualAction newScriptedQueryVisualAction;
    
    @Autowired
    private DeleteScriptVisualAction deleteScriptVisualAction;
     
    @Autowired
    private DeleteScriptedQueryPoolVisualAction deleteScriptedQueryPoolVisualAction;

    @Autowired
    private CreateQueryVisualAction createQueryVisualAction;
     /**
     * listener to remove object action
     */
    private ActionCompletedListener listenerDeleteBusinessObjectAction;
    
    private ActionCompletedListener listenerNewPoolAction;
    
    private ActionCompletedListener listenerNewScriptAction;
    
    private ActionCompletedListener listenerDeleteScriptAction;
    
    private ActionCompletedListener listenerDeletePoolAction;
    
    private ActionCompletedListener listenerCreateQueryAction;
    /**
     * combo filter for inventory tree
     */   
    private ComboBox<ClassMetadataLight> cbxFilterClassQuery;

    private MxGraphCanvas<ObjectMxNode, String> mxGraphCanvas;
    
    private ClassMetadataLight selectedClass;
    
    private ScriptedQueriesPool selectedPool;

    private ScriptedQuery selectedScript;
    
    private ExtendedQuery query;
    
    private NumberField txtLimit;
    
    private RadioButtonGroup rdioConnector;

    private Button btnExecQuery;
    
    int attributeHeight = 22;
    
    private ObjectMxNodeMap objectMxNodeMap;
    
    private TreeGrid<AbstractNode> treeScriptedQueries;
    /**
     * Button instance to execute the save script action
     */
    private Button btnSave;
    /**
     * Button instance to execute the run script action
     */
    private Button btnRunscript;
    /**
     * Button instance to execute the delete script action
     */
    private Button btnDeleteScript;
     /**
     * Button instance to execute the delete script action
     */
    private Button btnDeletePool;
    
    private H4 lblScriptName;
    
    private VerticalLayout lytScriptContent;
    
    public static final String FORMAT_VERSION = "1.0";
    
    /**
     * Dialog that lists the whole list of the queries
     */
    private Dialog dlgSavedQueries;
    
    /**
     * AceEditor instance to edit the script
     */
    private AceEditor editorScript;
    
    /**
     * Prop Sheet for pool properties
     */
    private PropertySheet propSheetPools;
    /**
     * Dialog for pool PS
     */
    private ConfirmDialog dlgEditPoolProperties;
    /**
     * Prop Sheet for script properties
     */
    PropertySheet propSheetScripts;
    /**
     * Dialog for script PS
     */
    private ConfirmDialog dlgEditScriptProperties;
    /**
     * User Queries
     */
    private List<CompactQuery> lstQueries;
    /**
     * Grid for queries
     */
    private Grid<CompactQuery> tblQueries;
    /**
     * current reference to the saved query
     */
    private CompactQuery savedQuery;
    
    public QueriesUI() {
        super();
        setSizeFull();
        query = new ExtendedQuery();
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.deleteBusinessObjectVisualAction.unregisterListener(listenerDeleteBusinessObjectAction);
        this.newScriptedQueryPoolVisualAction.unregisterListener(listenerNewPoolAction);
        this.newScriptedQueryVisualAction.unregisterListener(listenerNewScriptAction);
        this.deleteScriptVisualAction.unregisterListener(listenerDeleteScriptAction);
        this.deleteScriptedQueryPoolVisualAction.unregisterListener(listenerDeletePoolAction);
        this.createQueryVisualAction.unregisterListener(listenerCreateQueryAction);
    }
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();                                          
            } catch (Exception ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
 
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.queries.title");
    }

    @Override
    public void initContent() {
        setSizeFull();   
        
        PropertySheet.IPropertyValueChangedListener listenerPropSheetPools = new PropertySheet.IPropertyValueChangedListener() {
            @Override
            public void updatePropertyChanged(AbstractProperty property) {
                if (selectedPool != null) {
                    if (property.getName().equals(Constants.PROPERTY_NAME))
                        selectedPool.setName(property.getAsString());
                    if (property.getName().equals(Constants.PROPERTY_DESCRIPTION))
                        selectedPool.setDescription(property.getAsString()); 
                    
                    try {
                        aem.updateScriptedQueriesPool(selectedPool.getId(), selectedPool.getName(), selectedPool.getDescription());
                        treeScriptedQueries.getDataProvider().refreshAll();
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                    } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                        propSheetPools.undoLastEdit();
                    }                            
                }
            }
        };
        propSheetPools = new PropertySheet(ts, new ArrayList<>());
        propSheetPools.addPropertyValueChangedListener(listenerPropSheetPools);
        dlgEditPoolProperties = new ConfirmDialog(ts, ts.getTranslatedString("module.taskman.task.actions.edit-properties.name"), "");
        dlgEditPoolProperties.getBtnCancel().setText(ts.getTranslatedString("module.general.messages.close"));
        dlgEditPoolProperties.getBtnConfirm().setVisible(false);
        dlgEditPoolProperties.setContent(propSheetPools);
        dlgEditPoolProperties.setWidth("450px");
        dlgEditPoolProperties.setHeight("250px");
        
        PropertySheet.IPropertyValueChangedListener listenerPropSheetScripts = new PropertySheet.IPropertyValueChangedListener() {
            @Override
            public void updatePropertyChanged(AbstractProperty property) {
                if (selectedScript != null) {
                    
                    if (property.getName().equals(Constants.PROPERTY_NAME)) {
                        selectedScript.setName(property.getAsString());
                        lblScriptName.setText(selectedScript.getName());
                    }
                    if (property.getName().equals(Constants.PROPERTY_DESCRIPTION))
                        selectedScript.setDescription(property.getAsString()); 
                    
                    try {
                        aem.updateScriptedQuery(selectedScript.getId(), selectedScript.getName(), 
                                selectedScript.getDescription() ,selectedScript.getScript(), true);
                        treeScriptedQueries.getDataProvider().refreshAll();
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                    } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                        propSheetScripts.undoLastEdit();
                    }                            
                }
            }
        };
        propSheetScripts = new PropertySheet(ts, new ArrayList<>());
        propSheetScripts.addPropertyValueChangedListener(listenerPropSheetScripts);
        dlgEditScriptProperties = new ConfirmDialog(ts, ts.getTranslatedString("module.taskman.task.actions.edit-properties.name"), "");
        dlgEditScriptProperties.setWidth("450px");
        dlgEditScriptProperties.setHeight("250px");
        dlgEditScriptProperties.setContent(propSheetScripts);
        dlgEditScriptProperties.getBtnConfirm().setVisible(false);
      
        List<ClassMetadataLight> inventoryObjectClasses = new ArrayList<>();
        try {
            inventoryObjectClasses = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
        } catch (MetadataObjectNotFoundException  ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        cbxFilterClassQuery= new ComboBox<>(ts.getTranslatedString("module.general.labels.class-name"));
        this.cbxFilterClassQuery.setWidth("300px");
        this.cbxFilterClassQuery.setItems(inventoryObjectClasses);
        this.cbxFilterClassQuery.setClearButtonVisible(true);
        this.cbxFilterClassQuery.addValueChangeListener(listener -> {
           clearCanvas();
           if (listener.getValue() != null) {
               selectedClass = listener.getValue();
               savedQuery = null;
               query = new ExtendedQuery();
               query.setClassName(selectedClass.getName());
               query.setLimit(10);
               query.setLogicalConnector(ExtendedQuery.CONNECTOR_OR);
               query.setJoin(false);
               objectMxNodeMap = new ObjectMxNodeMap();
               renderClass(null, selectedClass, query, objectMxNodeMap, null);
               btnExecQuery.setEnabled(true);
           } else
               btnExecQuery.setEnabled(false);
        });
        
        mxGraphCanvas = new MxGraphCanvas<>();
//        mxGraphCanvas.getMxGraph().setGrid("img/grid.gif");
        mxGraphCanvas.getMxGraph().setRecursiveResize(true);
        
        btnExecQuery = new Button(ts.getTranslatedString("module.queries.execute-query"), new Icon(VaadinIcon.PLAY), evt -> {
            if (listenerDeleteBusinessObjectAction != null)
                this.deleteBusinessObjectVisualAction.unregisterListener(listenerDeleteBusinessObjectAction);
            executeQuery();
        });
        btnExecQuery.setEnabled(false);
        rdioConnector = new RadioButtonGroup();
        rdioConnector.setItems("OR", "AND");
        rdioConnector.setValue("OR");
        rdioConnector.getElement().setProperty("title", ts.getTranslatedString("module.queries.logical-connector"));
        
        txtLimit = new NumberField(ts.getTranslatedString("module.queries.limit"));
        txtLimit.setValue(10d);
        txtLimit.setMin(1);
        txtLimit.setStep(1);
        
        listenerCreateQueryAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            try {
                long id = (long) ev.getActionResponse().get("id");
                savedQuery = aem.getQuery(id);
                showActionCompledMessages(ev);
            } catch (ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        };
        this.createQueryVisualAction.registerActionCompletedLister(listenerCreateQueryAction);
        Button btnSaveQuery = new Button(VaadinIcon.DOWNLOAD_ALT.create(), evt -> {
           if (mxGraphCanvas.getNodes().isEmpty()) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.queries.query-empty"),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                return;
           }
           if (savedQuery == null)
              createUserQuery(); 
           else 
              saveCurrentQuery();
        });
        Button btnOpenQuery = new Button(VaadinIcon.FOLDER_OPEN_O.create(), evt -> {
            openQueriesListDialog();
        });
        loadQueries();
        initializeTblQueries();
        
        HorizontalLayout lytTools = new HorizontalLayout(btnOpenQuery, btnSaveQuery, 
                rdioConnector, txtLimit, btnExecQuery);
        lytTools.setWidthFull();
        lytTools.setSpacing(true);
        lytTools.setAlignItems(Alignment.END);
        HorizontalLayout lytToolBar = new HorizontalLayout(cbxFilterClassQuery, lytTools);
        HorizontalLayout lytContent = new HorizontalLayout(mxGraphCanvas.getMxGraph());
        lytContent.setSizeFull();
        
        Tab tabPs = new Tab(ts.getTranslatedString("module.queries.query-builder"));
        Div page1 = new Div();
        page1.setSizeFull();
        page1.add(lytToolBar, lytContent);

        Tab tabFiles = new Tab(ts.getTranslatedString("module.queries.scripted-queries"));
        Div page2 = new Div();
        page2.add(createScriptedQueriesTab());
        page2.setVisible(false);

        Map<Tab, com.vaadin.flow.component.Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tabPs, page1);
        tabsToPages.put(tabFiles, page2);
        Tabs tabs = new Tabs(tabPs, tabFiles);
        Div pages = new Div(page1, page2);
        pages.setWidthFull();
        Set<com.vaadin.flow.component.Component> pagesShown = Stream.of(page1)
                .collect(Collectors.toSet());

        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            com.vaadin.flow.component.Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
        });    
        setSpacing(false);
        setPadding(false);
        add(tabs, pages);
        
    }

    private void clearCanvas() {
        mxGraphCanvas.removeAllCells(); 
    }

    /**
     * Main method for rendering classes. Use this procedure to render a class in the 
     * default position or relative to any attribute.
     * @param attObject The source attribute to paint the class edge, a null value means the default position.
     * @param theClass the class to render
     * @param theQuery the query that represents the new class
     * @param objectMxNodeMap map that stores all the nodes inside a class. It is 
     * used in case of deleting a filter, and here the references to all
     * objects are saved (visible attribute nodes, class node, node that shows the attribute text) 
     * @param classNode the mxGraphNode object that represents the class
     */
    private MxGraphNode renderClass(ObjectMxNode attObject, ClassMetadataLight theClass, ExtendedQuery theQuery, ObjectMxNodeMap objectMxNodeMap, MxGraphNode classNode) {
        if (theClass != null) {
            try {
                int row, column = 0;
                if (classNode == null)
                    row = 40;
                else {
                    MxGraphNode sourceNode = mxGraphCanvas.getNodes().get(attObject);
                    int index = (int) sourceNode.getProperties().get("index");
                    column = (int) classNode.getX() + 350;
                    row = (int) classNode.getY() + index * attributeHeight + 10;
                }
                MxGraphNode mainNode = new MxGraphNode();
                mainNode.setGeometry(50 + column, row, 120, 10);
                String classID = UUID.randomUUID().toString();
                mainNode.setUuid(classID);
                mainNode.setIsEditable(false);
                mainNode.setFillColor("#f5f2ce");
                mainNode.setIsResizable(false);
                mxGraphCanvas.addNode(new ObjectMxNode(classID), mainNode);
                objectMxNodeMap.setObject(new ObjectMxNode(classID));
                ClassMetadata classMetadata = mem.getClass(theClass.getName());
                classMetadata.getAttributes().sort(Comparator.comparing(item -> (item.getOrder())));
                if (attObject == null) { // Only add parent filter for main class
                    AttributeMetadata attrParent = new AttributeMetadata(new Random().nextLong(), "parent", "Parent");
                    attrParent.setType("parent");
                    classMetadata.getAttributes().add(0, attrParent);
                }
                AttributeMetadata attId = new AttributeMetadata(new Random().nextLong(), "id", "ID");
                attId.setType(Constants.DATA_TYPE_STRING);
                classMetadata.getAttributes().add(1, attId);
                for (int i = 0; i < classMetadata.getAttributes().size(); i++) {
                    AttributeMetadata att = classMetadata.getAttributes().get(i);
                    MxGraphNode containerNode = new MxGraphNode();
                    String containerId = "container_" + att.getId();
                    containerNode.setUuid(containerId);
                    containerNode.setCellParent(classID);
                    containerNode.setHeight(attributeHeight);
                    containerNode.setFillColor(MxConstants.NONE);
                    containerNode.setStrokeColor(MxConstants.NONE);
                    containerNode.setIsSelectable(false);
                    containerNode.setIsResizable(false);
                    containerNode.setIsEditable(false);
                    containerNode.setIsConstituent(true);

                    MxGraphNode visibleAttributeNode = new MxGraphNode();
                    String visibleAttrNodeId = "vattr_" + att.getId();
                    visibleAttributeNode.setUuid(visibleAttrNodeId);
                    visibleAttributeNode.setWidth(50);
                    visibleAttributeNode.setFillColor("#a2a2a3");
                    visibleAttributeNode.setFontColor("#FFFFFF");
                    visibleAttributeNode.setStrokeColor("#FFFFFF");
                    visibleAttributeNode.setLabelBackgroundColor(MxConstants.NONE);
                    visibleAttributeNode.setLabel("Visible");
                    visibleAttributeNode.setIsCurved(Boolean.TRUE);
                    visibleAttributeNode.setRawStyle(MxConstants.STYLE_ROUNDED + "=" + 1 + ";locked=1;");
                    visibleAttributeNode.setHeight(attributeHeight);
                    visibleAttributeNode.setFontSize(13);
                    visibleAttributeNode.setIsConstituent(true);
                    visibleAttributeNode.setIsResizable(false);
                    visibleAttributeNode.setLabelPosition(MxConstants.ALIGN_CENTER);
                    visibleAttributeNode.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
                    visibleAttributeNode.setCellParent(containerId);
                    visibleAttributeNode.getProperties().put("visible", false);
                    visibleAttributeNode.addClickCellListener(clickListener -> {
                        Boolean visible = (Boolean) visibleAttributeNode.getProperties().get("visible");
                        visibleAttributeNode.getProperties().put("visible", !visible);
                        if (visible) {
                            visibleAttributeNode.setFillColor("#a2a2a3");
                            visibleAttributeNode.setRawStyle(MxConstants.STYLE_FILLCOLOR + "=#a2a2a3;" + MxConstants.STYLE_FONTSTYLE + "=0;");
                            visibleAttributeNode.addRawStyleToCurrent();
                            theQuery.getVisibleAttributeNames().remove(att.getName());
                        } else {
                            visibleAttributeNode.setRawStyle(MxConstants.STYLE_FILLCOLOR + "=#1df525;" + MxConstants.STYLE_FONTSTYLE + "=1;");
                            visibleAttributeNode.addRawStyleToCurrent();
                            visibleAttributeNode.setFillColor("#1df525");
                            theQuery.addVisibleAttributeName(att.getName());
                        }
                    });

                    MxGraphNode attributeNode = new MxGraphNode();
                    attributeNode.setUuid(att.getId() + "");
                    attributeNode.setWidth(115);
                    attributeNode.setHeight(attributeHeight);
                    attributeNode.setLabel(createAttributeLabel(att.getName()));
                    attributeNode.setCellParent(containerId);
                    attributeNode.setIsEditable(false);
                    attributeNode.setIsSelectable(false);
                    attributeNode.setStrokeColor(MxConstants.NONE);
                    attributeNode.setLabelBackgroundColor(MxConstants.NONE);
                    attributeNode.setLabelPosition(MxConstants.ALIGN_CENTER);
                    attributeNode.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
                    attributeNode.setIsResizable(false);
                    attributeNode.setIsDashed(true);
                    attributeNode.setFontSize(12);
                    attributeNode.setIsConstituent(true);
                    attributeNode.setFillColor(MxConstants.NONE);
                    attributeNode.setRawStyle(MxConstants.STYLE_ALIGN + "=right");
                    attributeNode.getProperties().put("index", i);

                    ObjectMxNode attrObjectNode = new ObjectMxNode(att.getId() + "", att);
                    attributeNode.addCellAddedListener(eventListener -> {
                        attributeNode.addOverlayButton("btn_" + att.getId(), "", "images/plus.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_BOTTOM, 15, (int) (-attributeHeight / 2));
                        mxGraphCanvas.getMxGraph().executeStackLayout(containerId, true, 0);
                    });
                    attributeNode.addClickOverlayButtonListener(eventListener -> {
                        AbstractFilter filter = getAttributeFilter(theClass, att);
                        openFilterDialog(attrObjectNode, filter, theQuery, objectMxNodeMap, mainNode, false);
                    });
                    mxGraphCanvas.addNode(new ObjectMxNode(containerId), containerNode);
                    mxGraphCanvas.addNode(new ObjectMxNode(visibleAttrNodeId), visibleAttributeNode);
                    mxGraphCanvas.addNode(attrObjectNode, attributeNode);

                    objectMxNodeMap.getChildrens().add(new ObjectMxNode(containerId));
                    objectMxNodeMap.getChildrens().add(new ObjectMxNode(visibleAttrNodeId));
                    objectMxNodeMap.getChildrens().add(attrObjectNode);
                }
                if (attObject != null) { // Advanced filter for list types
                    MxGraphEdge edgeFilter = new MxGraphEdge();
                    edgeFilter.setEdgeStyle(MxConstants.EDGESTYLE_SEGMENT);
                    edgeFilter.setSource(attObject.getId() + "");
                    edgeFilter.setTarget(classID);
                    mxGraphCanvas.addEdge("edge_" + classID, attObject, new ObjectMxNode(classID), edgeFilter);
                    MxGraphNode sourceNode = mxGraphCanvas.getNodes().get(attObject);
                    sourceNode.removeOverlayButtons();
                    mainNode.addCellAddedListener(eventListener -> {
                        mainNode.addOverlayButton("btn_delete_" + classID, "", "images/delete.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_TOP, 15, -4);
                    });
                    mainNode.addClickOverlayButtonListener(eventListener -> { // when the class filter is removed, then add the plus icon again
                        theQuery.getQueryJoin().removeAttribute(attObject.getObject().getName());
                        removeMxNodes(objectMxNodeMap);
                        sourceNode.addOverlayButton("btn_" + attObject.getId(), "", "images/plus.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_BOTTOM, 15, (int) (-attributeHeight / 2));
                        mxGraphCanvas.getMxGraph().refreshGraph();
                    });
                }

                MxGraphNode lastNode = new MxGraphNode();
                String rndUUID = UUID.randomUUID().toString();
                lastNode.setGeometry(0, 0, 0, 0);
                lastNode.setUuid(rndUUID);
                mxGraphCanvas.addNode(new ObjectMxNode(rndUUID), lastNode);
                mxGraphCanvas.getMxGraph().setIsCellResizable(false);
                lastNode.addCellAddedListener(eventListener -> {
                    mxGraphCanvas.getMxGraph().executeStackLayout(classID, false, 0, 10, 30, 10, 10, true);
                    mxGraphCanvas.getMxGraph().setIsCellResizable(false);
//            mxGraphCanvas.getNodes().values().stream().forEach( item -> {
////                 if (!item.getUuid().startsWith("filter") && !item.getUuid().equals(classID))
////                     item.setMovable(false);
//            });
                });
                return mainNode;

            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
        return null;
    }

    private AbstractFilter getAttributeFilter(ClassMetadataLight theClass, AttributeMetadata att) {
        AbstractFilter filter;
        switch (att.getType()) {
            case Constants.DATA_TYPE_STRING:
                filter = new StringFilter(ts);
                break;
            case Constants.DATA_TYPE_BOOLEAN:
                filter = new BooleanFilter(ts);
                break;
            case Constants.DATA_TYPE_DATE:
            case Constants.DATA_TYPE_TIME_STAMP:
                filter = new DateFilter(ts);
                break;
            case Constants.DATA_TYPE_DOUBLE:
            case Constants.DATA_TYPE_FLOAT:
            case Constants.DATA_TYPE_LONG:
            case Constants.DATA_TYPE_INTEGER: {
                filter = new NumericFilter(ts);
                break;
            }
            case "parent": {
                try {
                    List<ClassMetadataLight> possibleParents = mem.getUpstreamContainmentHierarchy(theClass.getName(), true);
                    if (possibleParents.isEmpty()) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.queries.cannot-be-parent"),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                        return null;
                    }
                    filter = new ParentFilter(ts, possibleParents);
                    break;
                } catch (MetadataObjectNotFoundException ex) {
                    filter = new ListTypeFilter(ts, new ArrayList<>());
                    break;
                }
            }
            default:
                List<BusinessObjectLight> items;
                try {
                    items = aem.getListTypeItems(att.getType());
                    filter = new ListTypeFilter(ts, items);
                } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    filter = new ListTypeFilter(ts, new ArrayList<>());
                    break;
                }
        }
        return filter;
    }

    /**
     * Allows render filter component and set his value depending of attribute date type.
     * @param attObject The clicked attribute
     * @param filter The filter based in the attribute type
     * @param theQuery The current query (class)
     * @param objectMxNodeMap Helper map that stores the whole query structure
     * @param classNode The mxgraphNode object that represents the class
     * @param editing boolean that specifies if we are editing.
     */
    private void openFilterDialog(ObjectMxNode attObject, AbstractFilter filter, ExtendedQuery theQuery, ObjectMxNodeMap objectMxNodeMap, MxGraphNode classNode, boolean editing) {
        ConfirmDialog dlgFilter = new ConfirmDialog(ts, "", ts.getTranslatedString("module.general.messages.ok"));
        dlgFilter.setWidth("400px");
        dlgFilter.setContent(filter.getComponent());
        dlgFilter.getBtnConfirm().addClickListener(listener -> {
            if ((filter instanceof ListTypeFilter && ((ListTypeFilter) filter).isUseAdvancedSearch()) 
              || filter instanceof ParentFilter) {              
                    try {
                        ClassMetadataLight relatedClass;
                        if (filter instanceof ParentFilter) {
                            if (!filter.isValid()) {
                               new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.queries.all-field-filled"),
                                     AbstractNotification.NotificationType.WARNING, ts).open();
                               return;
                            }                    
                            relatedClass = mem.getClass(((ParentFilter) filter).getClassName());
                        } else 
                            relatedClass = mem.getClass(attObject.getObject().getType());
                        theQuery.addAttributeValue(null);
                        theQuery.addConditions(null);
                        theQuery.addAttributeName(attObject.getObject().getName());
                        
                        ExtendedQuery joinQuery = new ExtendedQuery();
                        joinQuery.setJoin(true);
                        joinQuery.setClassName(relatedClass.getName());
                        joinQuery.setQueryJoin(query);
                        theQuery.getJoins().add(joinQuery);
                        
                        // Create a new map, from this point in case of removing this filter class
                        // the next filters can be removed too
                        ObjectMxNodeMap newObjectMxNodeMap = new ObjectMxNodeMap();
                        newObjectMxNodeMap.setParent(objectMxNodeMap);
                        objectMxNodeMap.getJoins().add(newObjectMxNodeMap);
                        
                        renderClass(attObject, relatedClass, joinQuery, newObjectMxNodeMap, classNode);
                        dlgFilter.close();
                        return;
                    } catch (MetadataObjectNotFoundException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }                 
            }
            if (filter.isValid()) {
                MxGraphNode nodeFilter;
                if (editing) {
                    nodeFilter = mxGraphCanvas.getNodes().get(new ObjectMxNode("filter_" + attObject.getId()));
                    if (filter instanceof ListTypeFilter) {
                       int joinIndex = theQuery.getAttributeNames().indexOf(attObject.getObject().getName());
                       ExtendedQuery joinQuery = theQuery.getJoins().get(joinIndex);
                       joinQuery.editAttribute(Constants.PROPERTY_ID, filter.getValue(), ExtendedQuery.EQUAL);
                    } else
                       theQuery.editAttribute(attObject.getObject().getName(), filter.getValue(), filter.getCriteria().id());  
                    nodeFilter.setLabel(filter.getValueAsString());
                    nodeFilter.setWidth(filter.getValueAsString().length() * 8);
                } else {
                    addFilter(attObject, filter, theQuery, classNode, false);
                }
                dlgFilter.close();
                mxGraphCanvas.getMxGraph().refreshGraph();
            } else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("All the field must be filled"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
        });
        dlgFilter.open();
    }

    private void addFilter(ObjectMxNode attObject, AbstractFilter filter, ExtendedQuery theQuery,
        MxGraphNode classNode, boolean openingQuery) {
        MxGraphNode nodeFilter;
        MxGraphNode attNode = mxGraphCanvas.getNodes().get(attObject);
        MxGraphNode sourceNode = mxGraphCanvas.getNodes().get(attObject);
        int index = (int) sourceNode.getProperties().get("index");
        int x = (int) classNode.getX();
        int y = (int) classNode.getY() + (index * attributeHeight) + 10;
        nodeFilter = new MxGraphNode();
        nodeFilter.setUuid("filter_" + attObject.getId());
        nodeFilter.setLabel(filter.getValueAsString());
        nodeFilter.setGeometry(350 + x, y, filter.getValueAsString().length() * 8 , attributeHeight);
        nodeFilter.setIsEditable(false);
        nodeFilter.setFontSize(13);
        nodeFilter.setFillColor("#4287f5");
        nodeFilter.setFontColor("white");
        nodeFilter.setStrokeColor(MxConstants.NONE);
        nodeFilter.setLabelBackgroundColor(MxConstants.NONE);
        nodeFilter.setRawStyle(MxConstants.STYLE_ROUNDED + "=1;"+ MxConstants.STYLE_ALIGN + "=center;" +
                "labelPadding=5");
        nodeFilter.setLabelPosition(MxConstants.ALIGN_CENTER);
        nodeFilter.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
        nodeFilter.addCellAddedListener(eventListener -> {
            nodeFilter.addOverlayButton("btn_edit_" + attObject.getId(), "", "images/edit.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_TOP, 10, 0);
            nodeFilter.addOverlayButton("btn_delete_" + attObject.getId(), "", "images/delete.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_TOP, 28, 0);
        });
        attNode.removeOverlayButtons();
        nodeFilter.addClickOverlayButtonListener(eventListener -> {
            if (eventListener.getButtonId().equals("btn_edit_" + attObject.getId())) {
                openFilterDialog(attObject, filter, theQuery, objectMxNodeMap, classNode, true);
            } else { // remove filter
                theQuery.removeAttribute(attObject.getObject().getName());
                mxGraphCanvas.removeNode(new ObjectMxNode("filter_" + attObject.getId()));
                attNode.addOverlayButton("btn_" + attObject.getId(), "", "images/plus.png", MxConstants.ALIGN_RIGHT, MxConstants.ALIGN_BOTTOM, 15, (int)(-attributeHeight / 2));
                mxGraphCanvas.getMxGraph().refreshGraph();
            }
        });
        if (!openingQuery) {
            if (filter instanceof ListTypeFilter) {
                theQuery.addAttributeValue(null);
                theQuery.addConditions(null);
                ExtendedQuery joinQuery = new ExtendedQuery();
                joinQuery.setJoin(true);
                joinQuery.addJoin(null);
                joinQuery.addAttributeName(Constants.PROPERTY_ID);
                joinQuery.addAttributeValue(filter.getValue());
                joinQuery.addConditions(ExtendedQuery.EQUAL);
                joinQuery.setClassName(((ListTypeFilter) filter).getClassName());
                theQuery.addJoin(joinQuery);
            } else if (filter instanceof ParentFilter) {
                theQuery.addAttributeValue(null);
                theQuery.addConditions(null);
                ExtendedQuery joinQuery = new ExtendedQuery();
                joinQuery.setJoin(true);
                joinQuery.addJoin(null);
                joinQuery.setClassName(((ParentFilter) filter).getClassName());
                theQuery.addJoin(joinQuery);
            } else {
                theQuery.addJoin(null);
                theQuery.addAttributeValue(filter.getValue());
                theQuery.addConditions(filter.getCriteria().id());
            }
            theQuery.addAttributeName(attObject.getObject().getName());
        }
        ObjectMxNode filterObjectNode = new ObjectMxNode("filter_" + attObject.getId());
        mxGraphCanvas.addNode(filterObjectNode, nodeFilter);
        objectMxNodeMap.getChildrens().add(filterObjectNode);
        MxGraphEdge edgeFilter = new MxGraphEdge();
        edgeFilter.setEdgeStyle(MxConstants.EDGESTYLE_ORTHOGONAL);
        edgeFilter.setSource(attObject.getId() + "");
        edgeFilter.setTarget("filter_" + attObject.getId());
        mxGraphCanvas.addEdge("edge_" + attObject.getId(), attObject, filterObjectNode, edgeFilter);
    }

    private void executeQuery() {
        try {
            query.setLimit(txtLimit.getValue() == null ? 10 : txtLimit.getValue().intValue());
            query.setLogicalConnector(rdioConnector.getValue().equals("OR") ? ExtendedQuery.CONNECTOR_OR : ExtendedQuery.CONNECTOR_AND);
            List<ResultRecord> result = aem.executeQuery(query);
            if (result.size() > 1) {
                VerticalLayout lytContent = new VerticalLayout();
                lytContent.setSpacing(false);
                lytContent.setPadding(false);
                lytContent.setMargin(false);
                PaginatedGrid<ResultRecord> grdResult = new PaginatedGrid<>();
                grdResult.setItems(result.subList(1, result.size()));
                grdResult.addComponentColumn(item -> {
                    return new Label(FormattedObjectDisplayNameSpan.getFormattedDisplayName(item, true));
                        }).setHeader(ts.getTranslatedString("module.queries.default-column"));
                grdResult.setPageSize(10);
                GridExporter gridExporter = new GridExporter(ts, (List<Object>) (Object) result, new ResultRecordParser(), new CSVFormatter(ts), new XMLExporter(ts));
                Button btnExport = new Button(ts.getTranslatedString("module.queries.export"), new Icon(VaadinIcon.FILE_TEXT_O), evt -> {
                    gridExporter.open();
                });
                // Sets how many pages should be visible on the pagination before and/or after the current selected page
                grdResult.setPaginatorSize(5);
                for (int i = 0; i < result.get(0).getExtraColumns().size(); i++) {
                    int col = i;
                    grdResult.addComponentColumn(item -> {
                        return new Label(item.getExtraColumns().get(col));
                    }).setHeader(result.get(0).getExtraColumns().get(col));
                }

                ConfirmDialog dlgResult = new ConfirmDialog(ts, ts.getTranslatedString("module.queries.query-result"), ts.getTranslatedString("module.general.messages.ok"));
                dlgResult.setWidth("90%");
                dlgResult.getBtnConfirm().addClickListener(listener -> {
                    dlgResult.close();
                });

                grdResult.addComponentColumn(item -> createActionsColumn(item, dlgResult));

                listenerDeleteBusinessObjectAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
                    try {
                        List<ResultRecord> newResult = aem.executeQuery(query);
                        grdResult.setItems(newResult.subList(1, result.size()));
                        grdResult.getDataProvider().refreshAll();
                        gridExporter.setDataSource(newResult);
                        showActionCompledMessages(ev);
                    } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                };
                this.deleteBusinessObjectVisualAction.registerActionCompletedLister(listenerDeleteBusinessObjectAction);

                lytContent.add(btnExport);
                lytContent.setAlignSelf(Alignment.END, btnExport);
                lytContent.add(grdResult);
                dlgResult.setContent(lytContent);
                dlgResult.open();
            } else 
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.general.messages.no-search-results"),
                        AbstractNotification.NotificationType.INFO, ts).open();
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
   
    private HorizontalLayout createActionsColumn(ResultRecord selectedObject, Dialog dlgResult) {
        HorizontalLayout lyt; 
        
       Button btnGoToDashboard = new Button(ts.getTranslatedString("module.queries.explore-object"),
                          new Icon(VaadinIcon.SIGN_IN), ev -> {
                     getUI().ifPresent(ui -> {
                            ui.getSession().setAttribute(BusinessObjectLight.class, selectedObject);
                            ui.getPage().open(new RouterLink("", ObjectDashboard.class).getHref(), "_blank");
                        });
                });             
                        
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), ev -> {
            this.deleteBusinessObjectVisualAction.getVisualComponent(
                new ModuleActionParameterSet( new ModuleActionParameter(DefaultDeleteBusinessObjectVisualAction.PARAM_BUSINESS_OBJECT, selectedObject))).open();
        });
        lyt = new HorizontalLayout(btnGoToDashboard, btnDelete);
        lyt.setWidthFull();
        lyt.setSpacing(true);
        
        return lyt;
    }

    /**
     * recursively remove nodes in the given map. 
     * @param objectMxNodeMap the map with the nodes
     */
    private void removeMxNodes(ObjectMxNodeMap objectMxNodeMap) {
        
        objectMxNodeMap.getChildrens().forEach(item -> {
              mxGraphCanvas.removeNode(item);          
        });
        mxGraphCanvas.removeNode(objectMxNodeMap.getObject());
        objectMxNodeMap.getJoins().forEach(item -> {
              removeMxNodes(item);          
        });
    }

    private String createAttributeLabel(String name) {
        if (name == null || name.length() < 15)
            return name;
        return name.substring(0, 15) + "...";
    }

    private Component createScriptedQueriesTab() {
        
        listenerNewPoolAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadPools();
            treeScriptedQueries.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
        };
        this.newScriptedQueryPoolVisualAction.registerActionCompletedLister(listenerNewPoolAction);
        listenerNewScriptAction = (ActionCompletedListener.ActionCompletedEvent ev) -> { 
            treeScriptedQueries.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
        };
        this.newScriptedQueryVisualAction.registerActionCompletedLister(listenerNewScriptAction);
        listenerDeleteScriptAction = (ActionCompletedListener.ActionCompletedEvent ev) -> { 
            lytScriptContent.setVisible(false);
            selectedScript = null;
            treeScriptedQueries.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
        };
        this.deleteScriptVisualAction.registerActionCompletedLister(listenerDeleteScriptAction);
        listenerDeletePoolAction = (ActionCompletedListener.ActionCompletedEvent ev) -> { 
            loadPools();
            lytScriptContent.setVisible(false);
            selectedPool = null;
            btnDeletePool.setEnabled(false);
            treeScriptedQueries.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
        };
        this.deleteScriptedQueryPoolVisualAction.registerActionCompletedLister(listenerDeletePoolAction);
        
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(30); 
        initializeScriptedQueriesTree();
        
        Button btnNewPool = new Button(ts.getTranslatedString("module.queries.new-pool-short"), new Icon(VaadinIcon.PLUS), evt -> {
            this.newScriptedQueryPoolVisualAction.getVisualComponent(
                new ModuleActionParameterSet()).open();   
        });
        btnDeletePool = new Button(ts.getTranslatedString("module.queries.delete-pool-short"), new Icon(VaadinIcon.TRASH), evt -> {
            this.deleteScriptedQueryPoolVisualAction.getVisualComponent(
                new ModuleActionParameterSet(new ModuleActionParameter("pool", selectedPool))).open();   
        });
        btnDeletePool.setEnabled(false);
        
        HorizontalLayout lytPoolActions = new HorizontalLayout(btnNewPool, btnDeletePool);
        lytPoolActions.setPadding(false);
        VerticalLayout lytPrimary = new VerticalLayout(lytPoolActions, treeScriptedQueries);
        splitLayout.addToPrimary(lytPrimary);
        splitLayout.setSizeFull();
        
        btnSave = new Button(ts.getTranslatedString("module.general.messages.save"), new Icon(VaadinIcon.DOWNLOAD),  evt -> {
            saveCurrentScript();
        });
        btnDeleteScript = new Button(this.deleteScriptVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                     this.deleteScriptVisualAction.getVisualComponent(new ModuleActionParameterSet(
                      new ModuleActionParameter("script", selectedScript))).open();
        });
        btnRunscript = new Button(ts.getTranslatedString("module.reporting.execute"), new Icon(VaadinIcon.PLAY), evt -> {
            saveCurrentScript();
            runScript();
        });
        Button btnEditProperties = new Button(ts.getTranslatedString("module.taskman.task.actions.edit-properties.name"), 
                new Icon(VaadinIcon.EDIT), evt -> {
            updatePropertySheetScripts();
            dlgEditScriptProperties.open();
        });
        
        lblScriptName = new H4();
        lblScriptName.getElement().getStyle().set("margin-top", "0");
        HorizontalLayout lytReportActions = new HorizontalLayout();
        lytReportActions.addAndExpand(lblScriptName);
        lytReportActions.add(btnEditProperties, btnSave, btnDeleteScript, btnRunscript);
        lytReportActions.setAlignItems(Alignment.BASELINE);
        lytReportActions.setWidthFull();
        lytReportActions.setPadding(false);
        editorScript = new AceEditor();
        editorScript.setMode(AceMode.groovy);
        editorScript.setHeight("72vh");
        lytScriptContent = new VerticalLayout(lytReportActions, editorScript);       
        lytScriptContent.setVisible(false);
        lytScriptContent.setPadding(false);
        lytScriptContent.setSpacing(false);
        lytScriptContent.getElement().getStyle().set("margin-top", "0");
        HorizontalLayout lytSecContent = new HorizontalLayout();
        lytSecContent.addAndExpand(lytScriptContent);
        lytSecContent.setPadding(false);
        lytSecContent.setSpacing(false);
        splitLayout.addToSecondary(lytSecContent);
        return splitLayout;
    }

    private void loadPools() {
        List<ScriptedQueriesPool> pools = aem.getScriptedQueriesPools("", 0, -1);
        HierarchicalDataProvider dataProvider = buildHierarchicalDataProvider(pools);
        treeScriptedQueries.setDataProvider(dataProvider);
    }
    
    /**
     * Creates a valid XML document describing this object in the format exposed at the <a href="http://is.gd/kcl1a">project's wiki</a>
     * @return a byte array with the querty structure
     */
    public byte[] getCurrentQueryAsXML() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        
            XMLEventWriter xmlew;
            xmlew = xmlof.createXMLEventWriter(baos);
            
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameQuery = new QName("query");

            xmlew.add(xmlef.createStartElement(qnameQuery, null, null));
            // query attributes
            xmlew.add(xmlef.createAttribute(new QName("version"), FORMAT_VERSION));
            xmlew.add(xmlef.createAttribute(new QName("logicalconnector"), "" +
                    (rdioConnector.getValue().equals("OR") ? ExtendedQuery.CONNECTOR_OR : ExtendedQuery.CONNECTOR_AND)));
            xmlew.add(xmlef.createAttribute(new QName("limit"), (txtLimit.getValue() == null ? (10 + "")
                    : txtLimit.getValue().intValue() + "")));

            bulidClassNode(xmlew, xmlef, query);

            xmlew.add(xmlef.createEndElement(qnameQuery, null));

            xmlew.close();
            
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return null;
    }
    
    private void bulidClassNode(XMLEventWriter xmlew, XMLEventFactory xmlef, ExtendedQuery currentJoin) throws XMLStreamException {
        QName qnameClass = new QName("class");
        
        xmlew.add(xmlef.createStartElement(qnameClass, null, null));
        xmlew.add(xmlef.createAttribute(new QName("name"), currentJoin.getClassName()));
        // Visible attributes                
        QName qnameVisibleattrs = new QName("visibleattributes");
        
        xmlew.add(xmlef.createStartElement(qnameVisibleattrs, null, null));
                
        for (String visibleAttrName : currentJoin.getVisibleAttributeNames()) {
            QName qnameAttr = new QName("attribute");
            
            xmlew.add(xmlef.createStartElement(qnameAttr, null, null));
            xmlew.add(xmlef.createAttribute(new QName("name"), visibleAttrName));
            xmlew.add(xmlef.createEndElement(qnameAttr, null));
        }
        xmlew.add(xmlef.createEndElement(qnameVisibleattrs, null));
        // Filters                        
        QName qnameFilters = new QName("filters");
        
        xmlew.add(xmlef.createStartElement(qnameFilters, null, null));
                        
        for (int i = 0; i < currentJoin.getAttributeNames().size(); i += 1) {
            QName qnameFilter = new QName("filter");
            
            xmlew.add(xmlef.createStartElement(qnameFilter, null, null));
            
            xmlew.add(xmlef.createAttribute(new QName("attribute"), currentJoin.getAttributeNames().get(i)));
            xmlew.add(xmlef.createAttribute(new QName("condition"), 
                    currentJoin.getConditions().get(i) == null ? 
                            "0" : Integer.toString(currentJoin.getConditions().get(i))));
            
            if (currentJoin.getJoins().get(i) != null) 
                bulidClassNode(xmlew, xmlef, currentJoin.getJoins().get(i));
            xmlew.add(xmlef.createEndElement(qnameFilter, null));
        }
        xmlew.add(xmlef.createEndElement(qnameFilters, null));
                        
        xmlew.add(xmlef.createEndElement(qnameClass, null));
    }
    
     /**
     * Initialize the scripted queries tree
     */
    private void initializeScriptedQueriesTree() {
        try {
            List<ScriptedQueriesPool> pools = aem.getScriptedQueriesPools("", 0, -1);
            HierarchicalDataProvider dataProvider = buildHierarchicalDataProvider(pools);

            treeScriptedQueries = new TreeGrid(dataProvider);
            treeScriptedQueries.setId("treeScriptedQueries");            
            treeScriptedQueries.addComponentHierarchyColumn(item -> {
                if (item instanceof PoolNode) {
                    ActionButton btnNewScrip = new ActionButton(new ActionIcon(VaadinIcon.PLUS_SQUARE_O), ts.getTranslatedString("module.queries.new-script-short"));
                    btnNewScrip.addClickListener(event -> this.newScriptedQueryVisualAction.getVisualComponent(new ModuleActionParameterSet(new ModuleActionParameter("pool", item.getObject()))).open());
                    btnNewScrip.setHeight("32px");
                    
                    ActionButton btnEditProperties = new ActionButton(new ActionIcon(VaadinIcon.EDIT), ts.getTranslatedString("module.pools.edit-properties"));
                    btnEditProperties.addClickListener(evt -> {
                        selectedPool = (ScriptedQueriesPool) item.getObject();
                        updatePropertySheetPools();
                        dlgEditPoolProperties.open();
                    });
                    btnEditProperties.setHeight("32px");
                    
                    Label lblName = new Label(((ScriptedQueriesPool) item.getObject()).getName());
                    HorizontalLayout lytActions = new HorizontalLayout(lblName, btnNewScrip, btnEditProperties);
                    lytActions.setSpacing(false);
                    lytActions.setFlexGrow(1, lblName);
                    lytActions.setAlignItems(Alignment.CENTER);
                    lytActions.setPadding(false);
                    lytActions.setMinWidth("350px");
                    return lytActions;
                } else 
                    return new Label(((ScriptedQuery) item.getObject()).getName());
            }).setWidth("100%");

            treeScriptedQueries.addItemClickListener(evt -> {
                if (evt.getItem() instanceof PoolNode) {
                    selectedPool = (ScriptedQueriesPool) evt.getItem().getObject();
                    btnDeletePool.setEnabled(true);
                } else {
                    selectedScript = (ScriptedQuery) evt.getItem().getObject();
                    lytScriptContent.setVisible(true);
                    updateScriptContent();
                }
            });
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }   
    }
    
        private HierarchicalDataProvider buildHierarchicalDataProvider(List<ScriptedQueriesPool> pools) {
        return new AbstractBackEndHierarchicalDataProvider() {
            @Override
            protected Stream fetchChildrenFromBackEnd(HierarchicalQuery hq) {
                if (hq.getParent() == null) {
                    List<PoolNode> poolNodes = new ArrayList();
                    pools.forEach(pool -> poolNodes.add(new PoolNode(pool)));
                    return poolNodes.stream();
                }
                if (hq.getParent() instanceof PoolNode) {
                    try {
                        PoolNode poolNode = (PoolNode) hq.getParent();
                        List<ScriptedQuery> scriptedQueriesPools = aem.getScriptedQueriesByPoolId(poolNode.getObject().getId(),"", true, 0, -1);
                        List<ScriptNode> scriptNodes = new ArrayList();
                        scriptedQueriesPools.forEach(script -> scriptNodes.add(new ScriptNode(script)));
                        return scriptNodes.stream();
                    } catch (Exception ex) {
                        return Collections.EMPTY_SET.stream();
                    }
                } else
                    return Collections.EMPTY_SET.stream();
            }

            @Override
            public int getChildCount(HierarchicalQuery hq) {
                if (hq.getParent() == null) {
                    return pools.size();
                }
                if (hq.getParent() instanceof PoolNode) {                 
                    PoolNode classNode = (PoolNode) hq.getParent();
                    return aem.getScriptedQueryCountByPoolId(classNode.getObject().getId(), "", true);                   
                } else 
                    return 0;
            }

            @Override
            public boolean hasChildren(Object t) {
                return t instanceof PoolNode;
            }
        };
    }

    private void updateScriptContent() {
        if (selectedScript != null) { 
            editorScript.setValue(selectedScript.getScript());
            lblScriptName.setText(selectedScript.getName());           
        }
    }

    private void saveCurrentScript() {
        if (selectedScript != null) {
            try {
                selectedScript.setScript(editorScript.getValue());
                aem.updateScriptedQuery(selectedScript.getId(), selectedScript.getName(), selectedScript.getDescription(), selectedScript.getScript(), true);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.queries.script-saved"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }

    private void runScript() {
        if (selectedScript != null) {
            ConfirmDialog dlgRunScript = new ConfirmDialog(ts, ts.getTranslatedString("module.queries.execute-script"), ts.getTranslatedString("module.queries.execute-script"));
            
            List<ScriptedQueryParameter>  lstParameters = new ArrayList<>();
            Grid<ScriptedQueryParameter> grdParamters = new Grid<>();
            TextField txtParamName = new TextField(ts.getTranslatedString("module.queries.parameter-name"));
            TextField txtParamValue = new TextField(ts.getTranslatedString("module.general.labels.value"));
            Button btnAddParameter = new Button(ts.getTranslatedString("module.queries.add-parameter"), new Icon(VaadinIcon.PLUS_CIRCLE_O), evt -> {
                if (txtParamName.getValue() != null && !txtParamName.getValue().isEmpty()) {
                    lstParameters.add(new ScriptedQueryParameter(txtParamName.getValue(), txtParamValue.getValue()));
                    grdParamters.setItems(lstParameters);
                    grdParamters.getDataProvider().refreshAll();
                    txtParamName.setValue("");
                    txtParamValue.setValue("");
                } else
                     new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.queries.param-name-cannot-be-null"), 
                    AbstractNotification.NotificationType.WARNING, ts).open();
            });
            grdParamters.addColumn(ScriptedQueryParameter::getName).setHeader(ts.getTranslatedString("module.general.labels.name"));
            grdParamters.addColumn(ScriptedQueryParameter::getValue).setHeader(ts.getTranslatedString("module.general.labels.value"));
            grdParamters.addComponentColumn(item -> {
               return new Button(new Icon(VaadinIcon.TRASH), evt -> {
                   lstParameters.remove(item);
                   grdParamters.getDataProvider().refreshAll();
               }); 
            });
            HorizontalLayout lytAddParam = new HorizontalLayout(txtParamName, txtParamValue, btnAddParameter);
            VerticalLayout lytContent = new VerticalLayout(lytAddParam, grdParamters);
            dlgRunScript.setContent(lytContent);
            dlgRunScript.getBtnConfirm().addClickListener(listener -> {
                try {
                    ScriptedQueryResult result = aem.executeScriptedQuery(selectedScript.getId(), lstParameters.toArray(new ScriptedQueryParameter[0]));
                    if (!result.getRows().isEmpty()) {
                        VerticalLayout lytResult = new VerticalLayout();
                        lytResult.setSpacing(false);
                        lytResult.setPadding(false);
                        lytResult.setMargin(false);
                        PaginatedGrid<List<Object>> grdResult = new PaginatedGrid<>();
                        grdResult.setItems(result.getRows());
                        grdResult.setPageSize(10);
                        GridExporter gridExporter = new GridExporter(ts,  result, new ResultScriptedQueryParser(), new CSVFormatter(ts), new XMLExporter(ts));
                        Button btnExport = new Button(ts.getTranslatedString("module.queries.export"), new Icon(VaadinIcon.FILE_TEXT_O), evt -> {
                            gridExporter.open();
                        });
                        // Sets how many pages should be visible on the pagination before and/or after the current selected page
                        grdResult.setPaginatorSize(5);
                        for (int i = 0; i < result.getColumnsSize(); i++) {
                            int col = i;
                            Column newCol = grdResult.addComponentColumn(item -> {                    
                                return new Label((String) item.get(col));
                            });
                            if (result.getColumnLabels() != null && result.getColumnLabels().size() == result.getColumnsSize())
                                newCol.setHeader(result.getColumnLabels().get(i));
                        }

                        ConfirmDialog dlgResult = new ConfirmDialog(ts, ts.getTranslatedString("module.queries.query-result"), ts.getTranslatedString("module.general.messages.ok"));
                        dlgResult.setWidth("90%");
                        dlgResult.getBtnConfirm().addClickListener(evt -> {
                            dlgResult.close();
                        });

                        listenerDeleteBusinessObjectAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
                            try {
                                ScriptedQueryResult newResult = aem.executeScriptedQuery(selectedScript.getId(), lstParameters.toArray(new ScriptedQueryParameter[0]));                  
                                grdResult.setItems(newResult.getRows());
                                grdResult.getDataProvider().refreshAll();
                                gridExporter.setDataSource(newResult);
                                showActionCompledMessages(ev);
                            } catch (ApplicationObjectNotFoundException | ExecutionException | InvalidArgumentException ex) {
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                        AbstractNotification.NotificationType.ERROR, ts).open();
                            }
                        };
                        this.deleteBusinessObjectVisualAction.registerActionCompletedLister(listenerDeleteBusinessObjectAction);

                        lytResult.add(btnExport);
                        lytResult.setAlignSelf(Alignment.END, btnExport);
                        lytResult.add(grdResult);
                        dlgResult.setContent(lytResult);
                        dlgRunScript.close();
                        dlgResult.open();
                    } else {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.no-search-results"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                    }
                } catch (InvalidArgumentException | ApplicationObjectNotFoundException | ExecutionException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            dlgRunScript.open();
        }
    }
    
    private void updatePropertySheetPools() {
        if (selectedPool != null) {
            ArrayList<AbstractProperty> viewProperties = new ArrayList<>();
            viewProperties.add(new StringProperty(Constants.PROPERTY_NAME,
                    Constants.PROPERTY_NAME, "", selectedPool.getName(), ts));
            viewProperties.add(new StringProperty(Constants.PROPERTY_DESCRIPTION,
                    Constants.PROPERTY_DESCRIPTION, "", selectedPool.getDescription(), ts));
            propSheetPools.setItems(viewProperties);
        } else
            propSheetPools.clear();
    }
    
    private void updatePropertySheetScripts() {
        if (selectedScript != null) {
            ArrayList<AbstractProperty> viewProperties = new ArrayList<>();
            viewProperties.add(new StringProperty(Constants.PROPERTY_NAME,
                    Constants.PROPERTY_NAME, "", selectedScript.getName(), ts));
            viewProperties.add(new StringProperty(Constants.PROPERTY_DESCRIPTION,
                    Constants.PROPERTY_DESCRIPTION, "", selectedScript.getDescription(), ts));
            propSheetScripts.setItems(viewProperties);
        } else
            propSheetScripts.clear();
    }

    private void createUserQuery() {
        this.createQueryVisualAction.getVisualComponent(
                new ModuleActionParameterSet( new ModuleActionParameter("structure", getCurrentQueryAsXML()))).open();      
    }
    
    public void loadQueries() {
        try {
            lstQueries = aem.getQueries(true);
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private void initializeTblQueries() {
        loadQueries();
        tblQueries = new Grid<>();
        tblQueries.setId("tblQueries");
        tblQueries.setItems(lstQueries);
        tblQueries.addColumn(CompactQuery::getName).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblQueries.addItemClickListener(listener -> {          
            openQuery(listener.getItem());  
            dlgSavedQueries.close();
        });
    }

    private void saveCurrentQuery() {
        if (savedQuery != null) {
            try {
                aem.saveQuery(savedQuery.getId(), savedQuery.getName(),
                        savedQuery.getOwnerId(), getCurrentQueryAsXML(), savedQuery.getDescription());
                loadQueries();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.queries.query-saved"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            } catch (ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
    
     /**
     * open the dialog that shows the list of available queries.
     */
    private void openQueriesListDialog() {
            if (dlgSavedQueries == null) {
                dlgSavedQueries = new Dialog();

                Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> {
                    dlgSavedQueries.close();
                });
                VerticalLayout lytContent = new VerticalLayout(tblQueries, btnCancel);
                lytContent.setAlignItems(Alignment.CENTER);
                dlgSavedQueries.add(lytContent);
                dlgSavedQueries.setWidth("600px");
            }
            dlgSavedQueries.open();        
    }

    private void openQuery(CompactQuery item) {
        try {
            clearCanvas();
            savedQuery = item;
            if (savedQuery.getOwnerId() == null)
                savedQuery.setOwnerId(-1l);
            loadQuery(savedQuery.getContent());
            ClassMetadataLight mainClass = mem.getClass(query.getClassName());
            selectedClass = mainClass;
            objectMxNodeMap = new ObjectMxNodeMap();
            MxGraphNode classNode = renderClass(null, mainClass, query, objectMxNodeMap, null);
            loadExtendedQuery(query, objectMxNodeMap, classNode);
            btnExecQuery.setEnabled(true);
            
        } catch (XMLStreamException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    /**
     * Loads the given query to the canvas.
     */
    private void loadExtendedQuery(ExtendedQuery query, ObjectMxNodeMap nodeMap, MxGraphNode classNode) {
        try {
            for (int i = 0; i < query.getAttributeNames().size(); i++) {

                String attr = query.getAttributeNames().get(i);
                int cond = query.getConditions().get(i);
                ExtendedQuery join = query.getJoins().get(i);
                if (join != null) // list type filters simple/advanced
                    join.setQueryJoin(query);
                
                ClassMetadata theClass = mem.getClass(query.getClassName());

                AttributeMetadata attrObject = theClass.getAttribute(attr);
                ObjectMxNode mxNodeAttr = new ObjectMxNode(attrObject.getId() + "", attrObject);

                if (join != null && !(join.getAttributeNames() != null && !join.getAttributeNames().isEmpty() &&
                        join.getAttributeNames().get(0).equals("id"))) { // Advanced filter
                    ObjectMxNodeMap newObjectMxNodeMap = new ObjectMxNodeMap();
                   
                    newObjectMxNodeMap.setParent(nodeMap);
                    nodeMap.getJoins().add(newObjectMxNodeMap);
                    ClassMetadata joinClass = mem.getClass(join.getClassName());

                    MxGraphNode newClassNode = renderClass(mxNodeAttr, joinClass, join, newObjectMxNodeMap, classNode);
                    loadExtendedQuery(join, newObjectMxNodeMap, newClassNode);
                } else {
                    AbstractFilter filter = getAttributeFilter(theClass, attrObject);
                    filter.setCriteria(Criteria.fromId(cond));
                    addFilter(mxNodeAttr, filter, query, classNode, true);
                }
            }
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }

    }

    private void loadQuery(byte[] structure) throws XMLStreamException {
        
         
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        QName qQuery = new QName("query"); //NOI18N
        QName qClass = new QName("class"); //NOI18N

        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        while (reader.hasNext()){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(qQuery)){
                    query.setLogicalConnector(Double.valueOf(reader.getAttributeValue(null, "logicalconnector")).intValue());
                    query.setLimit(Integer.valueOf(reader.getAttributeValue(null, "limit"))); //NOI18N
                }else{
                    if (reader.getName().equals(qClass)){
                        ExtendedQuery me = processClassTag(reader);
                        query.setAttributeNames(me.getAttributeNames());
                        query.setAttributeValues(me.getAttributeValues());
                        query.setConditions(me.getConditions());
                        query.setJoin(false);
                        query.setVisibleAttributeNames(me.getVisibleAttributeNames());
                        query.setJoins(me.getJoins());
                        query.setClassName(me.getClassName());
                    }
                }
            }
        }
        reader.close();
    }

    private ExtendedQuery processClassTag(XMLStreamReader reader) throws XMLStreamException {
        ExtendedQuery newJoin = new ExtendedQuery(reader.getAttributeValue(null,"name"), 
                                                                query.getLogicalConnector(), true, 
                                                                query.getLimit(), 0);
        
        newJoin.setVisibleAttributeNames(new ArrayList<>());
        newJoin.setAttributeValues(new ArrayList<>());
        newJoin.setAttributeNames(new ArrayList<>());
        newJoin.setJoins(new ArrayList<>());
        
        QName qVisibleAttributes = new QName("visibleattributes"); //NOI18N
        QName qAttribute = new QName("attribute"); //NOI18N
        QName qFilters = new QName("filters"); //NOI18N
        QName qFilter = new QName("filter"); //NOI18N
        QName qClass = new QName("class"); //NOI18N

        while (true){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(qVisibleAttributes)){
                    while (true){
                        int localEvent = reader.next();
                        if (localEvent == XMLStreamConstants.END_ELEMENT){
                            if (reader.getName().equals(qVisibleAttributes))
                                break;
                        }else{
                            if (localEvent == XMLStreamConstants.START_ELEMENT){
                                if (reader.getName().equals(qAttribute)) 
                                    newJoin.getVisibleAttributeNames().add(reader.getAttributeValue(null, "name"));                                    
                            }
                        }
                        
                    }
                }else{
                    if (reader.getName().equals(qFilters)){
                        while (true){
                            int localEvent = reader.next();
                            if (localEvent == XMLStreamConstants.END_ELEMENT){
                                if (reader.getName().equals(qFilters))
                                    break;
                            }else{
                                if (localEvent == XMLStreamConstants.START_ELEMENT){
                                    if (reader.getName().equals(qFilter)){
                                        newJoin.getAttributeNames().add(reader.getAttributeValue(null, "attribute"));     //NOI18N
                                        newJoin.getAttributeValues().add(null);     //NOI18N
                                        newJoin.getConditions().add(Integer.valueOf(reader.getAttributeValue(null, "condition")));     //NOI18N
                                        if (reader.nextTag() != XMLStreamConstants.END_ELEMENT){ //There's a nested subquery
                                            newJoin.getJoins().add(processClassTag(reader));
                                        }else newJoin.getJoins().add(null); //padding
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                if (event == XMLStreamConstants.END_ELEMENT){
                    if (reader.getName().equals(qClass))
                        break;
                }
            }
        }
        return newJoin;
    }   
}