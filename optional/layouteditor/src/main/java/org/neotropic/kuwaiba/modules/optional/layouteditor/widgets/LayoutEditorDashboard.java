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
package org.neotropic.kuwaiba.modules.optional.layouteditor.widgets;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraphBindedKeyEvent;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.flow.component.mxgraph.Point;
import com.neotropic.flow.component.mxgraph.Rectangle;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.text.html.StyleSheet;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.layouteditor.actions.DeleteLayoutViewVisualAction;
import org.neotropic.kuwaiba.modules.optional.layouteditor.actions.NewLayoutViewVisualAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.mxgraph.BasicStyleEditor;
import org.neotropic.kuwaiba.visualization.mxgraph.MxGraphGeometryEditor;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.StringProperty;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * Layout editor Main Dashboard.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
public class LayoutEditorDashboard extends VerticalLayout implements PropertySheet.IPropertyValueChangedListener {

    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private BusinessEntityManager bem;

    /**
     * listener to remove layout view action
     */
    private ActionCompletedListener listenerDeleteAction;
    /**
     * listener to add new layout view Action
     */
    private ActionCompletedListener listenerNewViewAction;
    /**
     * reference of the visual action to remove a layout view
     */
    private DeleteLayoutViewVisualAction deleteLayoutViewVisualAction;
    /**
     * reference of the visual action to create a new layout view
     */
    private NewLayoutViewVisualAction newLayoutViewVisualAction;
    /**
     * factory to instance object icons
     */
    private ResourceFactory resourceFactory;
    /**
     * source Equipment in create new connection dialog
     */
    private BusinessObjectLight selectedSourceEquipment;
    /**
     * target Equipment in create new connection dialog
     */
    private BusinessObjectLight selectedTargetEquipment;
    /**
     * current view in the canvas
     */
    private ViewObject currentView;

    /**
     * list of views
     */
    private List<ViewObjectLight> deviceLayouts;
    /**
     * list of views
     */
    private List<ViewObjectLight> lstListTypeItems;
    /**
     * Reference to the grid that shows the layout views
     */
    private Grid<ViewObjectLight> tblViews;
    /**
     * Dialog that lists the whole list of the views
     */
    private ConfirmDialog wdwLayoutViews;
    /**
     * reference to the current selected object in the canvas
     */
    private BusinessObjectLight selectedObject;
    /**
     * Prop Sheet for view properties
     */
    private  PropertySheet propSheetLayoutView;
    
    private Button btnSaveView;
    
    private Button btnRemoveView;
        
    private Button btnRemoveObjectFromView;

    private Button btnCopyShape;
        
    private Button btnAddRectShape;
    
    private Button btnAddEllipseShape;
    
    private Button btnAddLabel; 

    private PhysicalConnectionsService connectionsService;
    
    private VerticalLayout lytObjectProperties;
    
    private BasicStyleEditor styleEditor;
    
    private MxGraphGeometryEditor geometryEditor;
    
    private Accordion accordionProperties;
    
    private MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxGraphCanvas;
    
    private BusinessObjectLight currentListTypeItem;
    
    /**
     * Reference to the grid that shows the custom layouts to be added in a device layout
     */
    private Grid<BusinessObjectLight> tblCustomShapes;
    /**
     * Reference to the grid that opens the custom layouts
     */
    private Grid<BusinessObjectLight> tblEditCustomShapes;
    
    /**
     * list of custom shapes
     */
    private List<BusinessObjectLight> customShapes;
     /*
    map to store the device with their respective layout
     */
    private HashMap<BusinessObjectLight, byte[]> layoutDevices;
    /**
     * reference to layout that shows the view related list type
     */
    private VerticalLayout lytRelatedListType;
    /**
     * reference to label that shows the name of the related list type
     */
    private Label lblListTypeItem;
    
    private Button btnCopyView;
    
    private boolean editingLayoutView;
    
    private TextField txtShapeName;
    
    private Checkbox chkIsSlot;
    
    private HorizontalLayout lytGeneralInfo;
    
    Anchor viewExport;
    
    Button btnExportLayout;
    
    public static String PROPERTY_TYPE = "type"; //NOI18N
    public static String PROPERTY_NAME = "name"; //NOI18N 
    public static String PROPERTY_X = "x"; //NOI18N
    public static String PROPERTY_Y = "y"; //NOI18N
    public static String PROPERTY_WIDTH = "width"; //NOI18N
    public static String PROPERTY_HEIGHT = "height"; //NOI18N
    public static String PROPERTY_COLOR = "color"; //NOI18N
    public static String PROPERTY_BORDER_WIDTH = "borderWidth"; //NOI18N
    public static String PROPERTY_BORDER_COLOR = "borderColor"; //NOI18N
    public static String PROPERTY_IS_EQUIPMENT = "isEquipment"; //NOI18N
    public static String PROPERTY_OPAQUE = "opaque"; //NOI18N
    public static String SHAPE_CUSTOM = "custom";
    public static String SHAPE_RECTANGLE = "rectangle";
    public static String SHAPE_POLYGON = "polygon";
    public static String SHAPE_ELLIPSE = "ellipse";
    public static String SHAPE_LABEL = "label";
    public static final String PROPERTY_IS_SLOT = "isSlot";
    public static String PROPERTY_ELLIPSE_COLOR = "ellipseColor"; //NOI18N
    public static String PROPERTY_OVAL_COLOR = "ovalColor"; //NOI18N
    public static String PROPERTY_NUM_OF_SIDES = "numberOfSides"; //NOI18N
    public static String PROPERTY_OUTLINE_COLOR = "outlineColor"; //NOI18N
    public static String PROPERTY_INTERIOR_COLOR = "interiorColor"; //NOI18N
    public static final int UNIT_WIDTH = 1086 * 3; //NOI18N
    public static final int UNIT_HEIGHT = 100 * 3;//NOI18N
    public static String NODE_GUIDE = "NODE_GUIDE";
    public static String CLASS_CUSTOM = "CustomShape";
    public static String INNER_SHAPE = "innerShape";
    public static double DEFAULT_SHAPE_WIDTH = UNIT_HEIGHT / 3;
    public static double DEFAULT_SHAPE_HEIGHT = UNIT_HEIGHT / 3;
    
    public ViewObject getCurrentView() {
        return currentView;
    }

    public LayoutEditorDashboard(TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem,
            ResourceFactory resourceFactory, DeleteLayoutViewVisualAction deleteLayoutViewVisualAction, NewLayoutViewVisualAction newLayoutViewVisualAction) {
        super(); 
        this.ts = ts;
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.resourceFactory = resourceFactory;
        this.deleteLayoutViewVisualAction = deleteLayoutViewVisualAction;
        this.newLayoutViewVisualAction = newLayoutViewVisualAction;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        mxGraphCanvas = new MxGraphCanvas<>();
        mxGraphCanvas.getMxGraph().setGrid("img/grid.gif");
        mxGraphCanvas.getMxGraph().setRecursiveResize(true);
        layoutDevices = new HashMap<>();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        createContent();
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.deleteLayoutViewVisualAction.unregisterListener(listenerDeleteAction);
        this.newLayoutViewVisualAction.unregisterListener(listenerNewViewAction);
        mxGraphCanvas.getMxGraph().removeListeners();
    }

    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }

    private void createContent() {
        
        buildCanvasSkeleton();

        Button btnNewView = new Button(new Icon(VaadinIcon.FILE_ADD), ev -> {
            createNewLayoutView();
        });
        setButtonTitle(btnNewView, ts.getTranslatedString("module.layout-editor.actions.new-view.name"));
        btnNewView.setClassName("icon-button");
        btnCopyView = new Button(new Icon(VaadinIcon.COPY_O), ev -> {
            copyCurrentView();
        });
        setButtonTitle(btnCopyView, ts.getTranslatedString("module.layout-editor.copy-view"));
        btnCopyView.setClassName("icon-button");
        Button btnOpenView = new Button(new Icon(VaadinIcon.FOLDER_OPEN_O), ev -> {
            openDeviceLayoutsListDialog();
        });
        setButtonTitle(btnOpenView, ts.getTranslatedString("module.layout-editor.open-layout-view"));
        btnOpenView.setClassName("icon-button");
        
         Button btnEditCustomShape = new Button(new Icon(VaadinIcon.COMPILE), ev -> {
            openCustomShapesListDialog();
        });
        setButtonTitle(btnEditCustomShape, ts.getTranslatedString("module.layout-editor.open-custom-shape"));
        btnEditCustomShape.setClassName("icon-button");

        mxGraphCanvas.setComObjectSelected(() -> {
                      
            String objectId = mxGraphCanvas.getSelectedCellId();
            if (MxGraphCell.PROPERTY_VERTEX.equals(mxGraphCanvas.getSelectedCellType()) && !objectId.startsWith("*")) {
                selectedObject =  mxGraphCanvas.getNodes().keySet().stream().filter(item -> item.getId().equals(objectId) ).findAny().get();
                setGeneralToolsEnabled(true);
                setSelectionToolsEnabled(true);
            } else {
                selectedObject = null;
                setSelectionToolsEnabled(false);
            }
                
            updateShapeProperties();          
        });
        mxGraphCanvas.setComObjectUnselected(() -> {
            selectedObject = null;
            updateShapeProperties();
            setSelectionToolsEnabled(false);
        });
        
        mxGraphCanvas.setComObjectDeleted(() -> {
              deleteSelectedObject(); 
        });
        
        mxGraphCanvas.getMxGraph().addGraphLoadedListener(eventListener -> {
            mxGraphCanvas.getMxGraph().bindKey(37, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(37 + "")) {
                    MxGraphNode node = mxGraphCanvas.findMxGraphNode(selectedObject);
                    node.setX(node.getX() - 1);
                    geometryEditor.update(node);
                }
            });
            mxGraphCanvas.getMxGraph().bindKey(39, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(39 + "")) {
                    MxGraphNode node = mxGraphCanvas.findMxGraphNode(selectedObject);
                    node.setX(node.getX() + 1);
                    geometryEditor.update(node);
                }
            });
            mxGraphCanvas.getMxGraph().bindKey(38, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(38 + "")) {
                    MxGraphNode node = mxGraphCanvas.findMxGraphNode(selectedObject);
                    node.setY(node.getY() - 1);
                    geometryEditor.update(node);
                }
            });
            mxGraphCanvas.getMxGraph().bindKey(40, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(40 + "")) {
                    MxGraphNode node = mxGraphCanvas.findMxGraphNode(selectedObject);
                    node.setY(node.getY() + 1);
                    geometryEditor.update(node);
                }
            });
        });
             
        btnSaveView = new Button(new Icon(VaadinIcon.DOWNLOAD), evt -> {
            saveCurrentView();
        });
        btnSaveView.setClassName("icon-button");
        setButtonTitle(btnSaveView, ts.getTranslatedString("module.general.messages.save"));
        btnRemoveView = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE_O), evt -> {
            if (editingLayoutView) {
                deleteLayoutViewVisualAction.getVisualComponent(new ModuleActionParameterSet( new ModuleActionParameter("viewId", currentView.getId()))).open();
            } else {
                ConfirmDialog wdwDeleteClass = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                        ts.getTranslatedString("module.layout-editor.delete-permanently-shape"),
                        ts.getTranslatedString("module.general.messages.ok"));
                wdwDeleteClass.getBtnConfirm().addClickListener((ev) -> {
                    removeCustomShape(currentListTypeItem, currentView);
                    resetDashboard();
                    loadCustomShapes();
                    tblCustomShapes.setItems(customShapes);
                    tblCustomShapes.getDataProvider().refreshAll();
                    tblEditCustomShapes.setItems(customShapes);
                    tblEditCustomShapes.getDataProvider().refreshAll();
                    currentView = null;
                    currentListTypeItem = null;
                    updatePropertySheetView();
                    updateShapeProperties();
                    setSelectionToolsEnabled(false);
                    setGeneralToolsEnabled(false);
                    wdwDeleteClass.close();
                });
                wdwDeleteClass.open();
            }
        });
        btnRemoveView.setClassName("icon-button");
        btnRemoveView.setEnabled(false);
        setButtonTitle(btnRemoveView, ts.getTranslatedString("module.layout-editor.actions.delete-view.name"));
        btnCopyShape = new Button(new Icon(VaadinIcon.COPY_O),
                e -> {
                    copySelectedShape(); 
        });
        btnCopyShape.setClassName("icon-button");
        setButtonTitle(btnCopyShape, ts.getTranslatedString("module.layout-editor.copy-shape"));
        
        btnRemoveObjectFromView = new Button(new Icon(VaadinIcon.FILE_REMOVE),
                e -> {
                    deleteSelectedObject(); 
        });
        btnRemoveObjectFromView.setClassName("icon-button");
        setButtonTitle(btnRemoveObjectFromView, ts.getTranslatedString("module.layout-editor.remove-object-from-view"));
        
        btnAddRectShape = new Button(new Icon(VaadinIcon.THIN_SQUARE),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_RECTANGLE);
        });
        btnAddRectShape.setClassName("icon-button");
        setButtonTitle(btnAddRectShape, ts.getTranslatedString("module.topoman.add-rectangle"));
        btnAddEllipseShape = new Button(new Icon(VaadinIcon.CIRCLE_THIN),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_ELLIPSE);
        }); 
        btnAddEllipseShape.setClassName("icon-button");
        setButtonTitle(btnAddEllipseShape, ts.getTranslatedString("module.topoman.add-ellipse"));
        btnAddLabel = new Button(new Icon(VaadinIcon.TEXT_LABEL),
                e -> {
                    addShapeNodeToView(MxConstants.SHAPE_LABEL);
        }); 
        btnAddLabel.setClassName("icon-button");
        setButtonTitle(btnAddLabel, ts.getTranslatedString("module.topoman.add-label"));
              
        Button btnToogleGrid = new Button(new Icon(VaadinIcon.GRID), evt -> {
             if (mxGraphCanvas.getMxGraph().getGrid() != null &&
                     mxGraphCanvas.getMxGraph().getGrid().isEmpty())
                mxGraphCanvas.getMxGraph().setGrid("images/grid.gif");
             else
                mxGraphCanvas.getMxGraph().setGrid("");
         }); 
        btnToogleGrid.setClassName("icon-button");
        btnToogleGrid.getElement().setProperty("title", ts.getTranslatedString("module.topoman.show-hide-grid"));
        
        viewExport = new Anchor();
        viewExport.setId("anchorDownload");
        viewExport.getElement().setAttribute("download", true);
//        viewExport.setClassName("hidden");

        btnExportLayout = new Button(new Icon(VaadinIcon.EXTERNAL_LINK), evt -> {
            if (currentView != null) {                   
                StreamRegistration regn = VaadinSession.getCurrent().getResourceRegistry().registerResource(createStreamResource(
                        (currentView.getName() == null || currentView.getName().trim().isEmpty() ? "view": currentView.getName()), getAsXML(true)));               
                viewExport.setHref(regn.getResourceUri().getPath());
                UI.getCurrent().getPage().executeJs("$0.click();", viewExport.getElement());
            }
        });
        btnExportLayout.setClassName("icon-button");
        btnExportLayout.getElement().setProperty("title", ts.getTranslatedString("module.layout-editor.export"));
        viewExport.add(btnExportLayout);
        
        Button btnImportLayout = new Button(VaadinIcon.EXTERNAL_BROWSER.create(), evt -> {
            openImportLayoutDlg();
        });
        btnImportLayout.setClassName("icon-button");
        btnImportLayout.getElement().setProperty("title", ts.getTranslatedString("module.layout-editor.import-view"));
        HorizontalLayout lytTools = new HorizontalLayout(btnNewView, btnOpenView, btnEditCustomShape,
                                    btnSaveView, btnCopyView, btnRemoveView, btnRemoveObjectFromView,
                                    btnAddRectShape, btnAddEllipseShape, btnAddLabel, btnCopyShape, viewExport, btnImportLayout,
                                    btnToogleGrid);
        lytTools.setAlignItems(Alignment.CENTER);
        lytTools.setSpacing(false);
        initializeActions();
        initializeTblViews();   

        VerticalLayout lytDashboard = new VerticalLayout(lytTools, mxGraphCanvas.getMxGraph());
        setMarginPaddingLayout(lytDashboard, false);
        lytDashboard.setSpacing(false);
        lytDashboard.setWidth("75%");
                        
        PropertySheet.IPropertyValueChangedListener listenerPropSheetLayoutView = new PropertySheet.IPropertyValueChangedListener() {
            @Override
            public void updatePropertyChanged(AbstractProperty property) {
                if (currentView != null) {
                    
                    if (property.getName().equals(Constants.PROPERTY_NAME))
                        currentView.setName(property.getAsString());
                    if (property.getName().equals(Constants.PROPERTY_DESCRIPTION))
                        currentView.setDescription(property.getAsString());                  
                    if (!saveCurrentView())
                       propSheetLayoutView.undoLastEdit();
                    else {
                        loadLayouts();
                        loadCustomShapes();
                    }
                }
            }
        };
        propSheetLayoutView = new PropertySheet(ts, new ArrayList<>());
        propSheetLayoutView.addPropertyValueChangedListener(listenerPropSheetLayoutView);
        
        accordionProperties = new Accordion();
        accordionProperties.setWidth("300px");
        
        loadCustomShapes();
        initializeTblCustomShapes();
        initializeTblEditCustomShapes();
          
        BoldLabel lblTitleRelatedListTypeItem = new BoldLabel(ts.getTranslatedString("module.layout-editor.related-list-type-item"));
        lblListTypeItem = new Label();
        Button btnSetListTypeItem = new Button(new Icon(VaadinIcon.COMPRESS), (evt) -> {
            openDlgRelateListTypeItem();
        });
        setButtonTitle(btnSetListTypeItem, ts.getTranslatedString("module.layout-editor.set-list-type-item"));
        HorizontalLayout lytListTypeName = new HorizontalLayout(lblListTypeItem, btnSetListTypeItem);
        lytListTypeName.setAlignItems(Alignment.CENTER);
        lytListTypeName.setWidthFull();
        lytListTypeName.setFlexGrow(1, lblListTypeItem);
        lytRelatedListType = new VerticalLayout(lblTitleRelatedListTypeItem, lytListTypeName);
        lytRelatedListType.setMargin(false);
        lytRelatedListType.setPadding(false);
        lytRelatedListType.setVisible(false);
        BoldLabel lblViewProperties = new BoldLabel(ts.getTranslatedString("module.topoman.view-properties"));
        lblViewProperties.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryViewProp = new HorizontalLayout(lblViewProperties); 
        lytSummaryViewProp.setWidthFull();       
        AccordionPanel apViewProp = new AccordionPanel(lytSummaryViewProp, new VerticalLayout(propSheetLayoutView, lytRelatedListType));
        accordionProperties.add(apViewProp);
        
        Button btnNewCustomShape = new Button(ts.getTranslatedString("module.layout-editor.new-custom-shape"), new Icon(VaadinIcon.PLUS), evt -> {
            openCreateNewCustomShapeDlg();
        });
        VerticalLayout lytcustomShapes = new VerticalLayout(btnNewCustomShape, tblCustomShapes);
        lytcustomShapes.setSpacing(false);
        lytcustomShapes.setPadding(false);
        BoldLabel lblCustomShapes = new BoldLabel(ts.getTranslatedString("module.layout-editor.custom-shapes"));
        lblCustomShapes.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryCustomShapes = new HorizontalLayout(lblCustomShapes); 
        lytSummaryCustomShapes.setWidthFull();       
        AccordionPanel apCustomShapes = new AccordionPanel(lytSummaryCustomShapes, lytcustomShapes);
        accordionProperties.add(apCustomShapes);
  
        txtShapeName = new TextField(ts.getTranslatedString("module.general.labels.name"));
        txtShapeName.setValueChangeMode(ValueChangeMode.EAGER);
        txtShapeName.addValueChangeListener(listener -> {
            if (selectedObject != null && !selectedObject.getId().startsWith("*")) {
                MxGraphNode node = mxGraphCanvas.findMxGraphNode(selectedObject);
                node.getProperties().put(PROPERTY_NAME, listener.getValue());
            }
        });
        chkIsSlot = new Checkbox("Is Slot");
        chkIsSlot.addValueChangeListener(listener -> {
           if (selectedObject != null && !selectedObject.getId().startsWith("*")) {
                MxGraphNode node = mxGraphCanvas.findMxGraphNode(selectedObject);
                node.getProperties().put(PROPERTY_IS_SLOT, listener.getValue());
            }
        });
        lytGeneralInfo = new HorizontalLayout(txtShapeName, chkIsSlot);
        lytGeneralInfo.setFlexGrow(1, txtShapeName);
        lytGeneralInfo.setPadding(false);
        lytGeneralInfo.setAlignItems(Alignment.BASELINE);
        lytGeneralInfo.setVisible(false);
        styleEditor = new BasicStyleEditor(ts, new ArrayList(Arrays.asList(
                          MxConstants.STYLE_STROKECOLOR, MxConstants.STYLE_FILLCOLOR,
                          MxConstants.STYLE_FONTSIZE, MxConstants.STYLE_FONTCOLOR)));
        styleEditor.updateControlsVisibility(null);
        geometryEditor = new MxGraphGeometryEditor(ts);
        geometryEditor.updateControlsVisibility(null);
        VerticalLayout lytProperties = new VerticalLayout(lytGeneralInfo, styleEditor, geometryEditor);
        lytProperties.setPadding(false);
        lytProperties.setMargin(false);
        lytProperties.setSpacing(false);
                
        BoldLabel lblStyleEditor = new BoldLabel(ts.getTranslatedString("module.topoman.shape-properties"));
        lblStyleEditor.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryStyleEditor = new HorizontalLayout(lblStyleEditor); 
        lytSummaryStyleEditor.setWidthFull();       
        AccordionPanel apStyleEditor = new AccordionPanel(lytSummaryStyleEditor, lytProperties);
        accordionProperties.add(apStyleEditor);
        
        Label lblHelp = new Label(ts.getTranslatedString("module.layout-editor.help"));
        lblHelp.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryHelp = new HorizontalLayout(lblHelp); 
        lytSummaryHelp.setWidthFull();           
        AccordionPanel apHelp = new AccordionPanel(lytSummaryHelp, new Label());
        accordionProperties.add(apHelp);

        HorizontalLayout lytMain = new HorizontalLayout(accordionProperties, lytDashboard);
        lytMain.setSizeFull();
        setMarginPaddingLayout(lytMain, false);
        setSpacing(false);
        setMargin(false);
        addAndExpand(lytMain);
        setSizeFull();
        
        setGeneralToolsEnabled(false);
        setSelectionToolsEnabled(false);
    }

    private void updateShapeProperties() {
        if (selectedObject != null) {
            if (!selectedObject.getId().startsWith("*")) {
                MxGraphNode node = mxGraphCanvas.findMxGraphNode(selectedObject);
                if (node != null) {
                    styleEditor.update(node);
                    geometryEditor.update(node);
                    if (node.getShape() != null && node.getShape().equals(MxConstants.SHAPE_RECTANGLE)) {
                        chkIsSlot.setVisible(true);
                        if (node.getProperties().containsKey(PROPERTY_IS_SLOT))
                            chkIsSlot.setValue( (Boolean) node.getProperties().get(PROPERTY_IS_SLOT));
                        else 
                           chkIsSlot.setValue(false);
                    }
                    else 
                        chkIsSlot.setVisible(false);
                    
                    lytGeneralInfo.setVisible(true);
                    if (node.getProperties().containsKey(PROPERTY_NAME))
                        txtShapeName.setValue((String) node.getProperties().get(PROPERTY_NAME));
                    else
                        txtShapeName.setValue("");
                    
                    accordionProperties.open(2);
                }
            } else {
                styleEditor.update(null); 
                geometryEditor.update(null);
                lytGeneralInfo.setVisible(false);
            }
        } else {
            styleEditor.update(null);
            geometryEditor.update(null); 
            lytGeneralInfo.setVisible(false);
        }      
    }

    private void setMarginPaddingLayout(ThemableLayout lyt, boolean enable) {
        lyt.setMargin(enable);
        lyt.setPadding(enable);
    }

    /**
     * resets the layout view instance and creates a empty one
     */
    public void resetDashboard() {
        List<BusinessObjectLight> objectsToRemove = mxGraphCanvas.getNodes().keySet().stream().filter(node -> !NODE_GUIDE.equals(node.getClassName())).collect(Collectors.toList());
        for (BusinessObjectLight obj : objectsToRemove)
            mxGraphCanvas.removeNode(obj);
    }

    /**
     * Save the current view in the canvas
     */
    private boolean saveCurrentView() {
        try {
            if (currentView != null) {
                byte [] structure = getAsXML(false);

                aem.updateLayout(currentView.getId(), currentView.getName(), currentView.getDescription(), structure, null);
                currentView.setStructure(structure);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.layout-editor.view-saved"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                return true;
            }
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return false;
    }

    /**
     * Removes the selected object in the view.
     */
    private void deleteSelectedObject() {
        if (selectedObject != null) {
            if (MxGraphCell.PROPERTY_VERTEX.equals(mxGraphCanvas.getSelectedCellType())) {
                mxGraphCanvas.removeNode(selectedObject);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.layout-editor.object-deleted"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            } 
            selectedObject = null;
            setSelectionToolsEnabled(false);
        }
    }

    private void initializeTblViews() {
        loadLayouts();
        tblViews = new Grid<>();
        ListDataProvider<ViewObjectLight> dataProvider = new ListDataProvider<>(deviceLayouts);
        tblViews.setDataProvider(dataProvider);
        tblViews.addColumn(item -> {
            return item.getName() == null || item.getName().isEmpty() 
                    ? ts.getTranslatedString("module.layout-editor.name-not-set") : item.getName();
        }).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
         tblViews.addColumn(item -> {
            try {
                BusinessObjectLight lti = aem.getListTypeItemForLayout(item.getId());
                return (lti == null ? ts.getTranslatedString("module.layout-editor.not-related-list-type-item") 
                        : lti.getName());
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                return "";
            }
        }).setFlexGrow(3).setKey(ts.getTranslatedString("module.layout-editor.list-type-item-related"));
        tblViews.addItemClickListener(listener -> {
            openDeviceLayout(listener.getItem());
        });
        HeaderRow filterRow = tblViews.appendHeaderRow();

        TextField txtViewNameFilter = new TextField();
        txtViewNameFilter.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtViewNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtViewNameFilter.setWidthFull();
        txtViewNameFilter.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtViewNameFilter.getValue())));

        filterRow.getCell(tblViews.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtViewNameFilter);

    }
    
    private void initializeTblCustomShapes() {
        tblCustomShapes = new Grid<>();
        ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(customShapes);
        tblCustomShapes.setDataProvider(dataProvider);
        tblCustomShapes.addColumn(BusinessObjectLight::getName).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblCustomShapes.addComponentColumn(item -> createActionsColumnTblCustomLayouts(item)).setKey("component-column");
        HeaderRow filterRow = tblCustomShapes.appendHeaderRow();

        TextField txtViewNameFilter = new TextField(ts.getTranslatedString("module.general.messages.search"), ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtViewNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtViewNameFilter.setWidthFull();
        txtViewNameFilter.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtViewNameFilter.getValue())));

        filterRow.getCell(tblCustomShapes.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtViewNameFilter);
    }
    
    private void initializeTblEditCustomShapes() {
        tblEditCustomShapes = new Grid<>();
        ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(customShapes);
        tblEditCustomShapes.setDataProvider(dataProvider);
        tblEditCustomShapes.addColumn(BusinessObjectLight::toString).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblEditCustomShapes.addItemClickListener(listener -> openCustomShape(listener.getItem()));
        HeaderRow filterRow = tblEditCustomShapes.appendHeaderRow();

        TextField txtViewNameFilter = new TextField(ts.getTranslatedString("module.general.messages.search"), ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtViewNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtViewNameFilter.setWidthFull();
        txtViewNameFilter.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtViewNameFilter.getValue())));

        filterRow.getCell(tblEditCustomShapes.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtViewNameFilter);
    }
    
    private HorizontalLayout createActionsColumnTblCustomLayouts(BusinessObjectLight item) {
        
    HorizontalLayout lytActions = new HorizontalLayout();
    Button btnAdd = new Button(new Icon(VaadinIcon.PLUS_CIRCLE_O), evt -> {
        addCustomShapeToView(item, null);
    });
    btnAdd.setClassName("icon-button");

    lytActions.add(btnAdd);
    return lytActions;
}

    /**
     * loads the given layout view into the view
     * @param item the layout view to be loaded
     */
    private void openDeviceLayout(ViewObjectLight item) {
        try {
            if (item != null) {
                currentView = aem.getLayout(item.getId());               
            } else 
                return;
            resetDashboard();
            btnRemoveView.setEnabled(false);
            currentListTypeItem = aem.getListTypeItemForLayout(item.getId());
            byte[] deviceStructure = currentView.getStructure();
//           <editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">           
//            try {
////                FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/LAYOUT_OPEN" + ".xml");
////                fos.write(deviceStructure);
////                fos.close();
//            } catch (IOException e) {
//            }
//          </editor-fold>
            renderShape(deviceStructure, UNIT_WIDTH, UNIT_HEIGHT, mxGraphCanvas.getNodes().get(new BusinessObjectLight("", "*main", "")), false);
            
            if (wdwLayoutViews != null) {
                this.wdwLayoutViews.close();
            }
            selectedObject = null;
            updatePropertySheetView();
            updateShapeProperties();
            udpateRelatedListType();
            accordionProperties.open(0);
            setGeneralToolsEnabled(true);
            tblCustomShapes.setEnabled(true);
            editingLayoutView = true;
            
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * loads the given custom shape into the view
     * @param item the layout view to be loaded
     */
    private void openCustomShape(BusinessObjectLight item) {
        try {
            currentListTypeItem =  item;
            List<ViewObjectLight> views = aem.getListTypeItemRelatedLayout(currentListTypeItem.getId(), currentListTypeItem.getClassName(), 1);
            
            if (!views.isEmpty()) {
                currentView = aem.getListTypeItemRelatedLayout(currentListTypeItem.getId(), currentListTypeItem.getClassName(), views.get(0).getId());
            } else {
                return;
            }
            btnRemoveView.setEnabled(true);
            resetDashboard();
            byte[] deviceStructure = currentView.getStructure();
           
            renderShape(deviceStructure, UNIT_WIDTH, UNIT_HEIGHT, mxGraphCanvas.getNodes().get(new BusinessObjectLight("", "*main", "")), false);
            
            if (wdwLayoutViews != null) {
                this.wdwLayoutViews.close();
            }
            editingLayoutView = false;
            selectedObject = null;
            updateShapeProperties();
            lytRelatedListType.setVisible(false);
            propSheetLayoutView.setVisible(false);
            accordionProperties.open(0);
            setGeneralToolsEnabled(true);
            tblCustomShapes.setEnabled(false);   
            
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * loads the given layout view into the view
     * @param item the layout view to be loaded
     */
    private void addCustomShapeToView(BusinessObjectLight item, MxGraphNode sourceCopyNode) {
        try {
            String businessObjectId;
            if (sourceCopyNode == null) 
                businessObjectId = item.getId();
            else
                businessObjectId = item.getName();
            List<ViewObjectLight> views = aem.getListTypeItemRelatedLayout(businessObjectId, CLASS_CUSTOM, 1);
            ViewObject shapeView;
            if (!views.isEmpty()) {
                shapeView = aem.getListTypeItemRelatedLayout(businessObjectId, CLASS_CUSTOM, views.get(0).getId());
            } else 
                return;
            byte[] deviceStructure = shapeView.getStructure();
//           <editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">           
//            try {
//                FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/CUSTOM_SHAPE_ADD" + currentView.getId() + ".xml");
//                fos.write(deviceStructure);
//                fos.close();
//            } catch (IOException e) {
//            }
//          </editor-fold>           
            if (deviceStructure == null || deviceStructure.length == 0) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), "The custom shape view its empty", 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                return;
            }
                
            
            ByteArrayInputStream bais = new ByteArrayInputStream(deviceStructure);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            
            QName tagLayout = new QName("layout"); //NOI18N
            String attrValue;

            int layoutWidth = 0, layoutHeight = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagLayout)) {

                        attrValue = reader.getAttributeValue(null, "width"); //NOI18N
                        if (attrValue != null) 
                            layoutWidth = Integer.valueOf(attrValue);
                        
                        attrValue = reader.getAttributeValue(null, "height"); //NOI18N
                        if (attrValue != null) 
                            layoutHeight = Integer.valueOf(attrValue);
                          
                    }
                }
            }
            MxGraphNode nodeShape;
            if (sourceCopyNode == null) {  // creating a new shape
                nodeShape = new MxGraphNode();
                nodeShape.setShape(SHAPE_CUSTOM);
                String nodeUuid = UUID.randomUUID().toString();
                nodeShape.setUuid(nodeUuid);
                nodeShape.setCellParent("*main");
                nodeShape.setGeometry(50, 50, layoutWidth, layoutHeight);
                nodeShape.getProperties().put(PROPERTY_NAME, item.getName());
                mxGraphCanvas.addNode(new BusinessObjectLight((SHAPE_CUSTOM), nodeUuid, businessObjectId), nodeShape);                                 
            } else {  // copying a shape
                nodeShape = sourceCopyNode;
                mxGraphCanvas.addNode(new BusinessObjectLight((SHAPE_CUSTOM), nodeShape.getUuid(), businessObjectId), nodeShape);
            }                     
            renderShape(deviceStructure, UNIT_WIDTH, UNIT_HEIGHT, nodeShape, true);

        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException | XMLStreamException ex) {
             new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();   
        }
    }
    
    public void loadLayouts() {
        try {
            deviceLayouts = aem.getLayouts(-1);
        } catch (InvalidArgumentException | NotAuthorizedException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     public void loadCustomShapes() {
        try {
            customShapes = aem.getListTypeItems(CLASS_CUSTOM);
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
     
    /**
     * Initialize the general actions that provides the functionalty to create 
     * and remove device layout
     */
    private void initializeActions() {
        listenerDeleteAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            refreshLayoutViews();
            showActionCompledMessages(ev);
            currentView = null;
            currentListTypeItem = null;
            resetDashboard();
            selectedObject = null;
            updatePropertySheetView();
            updateShapeProperties();
            lytRelatedListType.setVisible(true);
            setGeneralToolsEnabled(false);
            setSelectionToolsEnabled(false);
        };
        this.deleteLayoutViewVisualAction.registerActionCompletedLister(listenerDeleteAction);          
        listenerNewViewAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            refreshLayoutViews();
            showActionCompledMessages(ev);
            try {
                currentView = aem.getLayout((long) ev.getActionResponse().get("viewId"));
            
                resetDashboard();
                selectedObject = null;
                updatePropertySheetView();
                updateShapeProperties();
                currentListTypeItem = aem.getListTypeItemForLayout(currentView.getId());
                udpateRelatedListType();
                setGeneralToolsEnabled(true);
                setSelectionToolsEnabled(false);
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            } catch (InvalidArgumentException ex) {
                Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        this.newLayoutViewVisualAction.registerActionCompletedLister(listenerNewViewAction);          
    }

    private void refreshLayoutViews() {
        loadLayouts();
        tblViews.getDataProvider().refreshAll();
        tblViews.setItems(deviceLayouts);
        tblViews.getDataProvider().refreshAll();
    }
    /**
     * open the dialog that shows the list of available views.
     */
    private void openDeviceLayoutsListDialog() {
        wdwLayoutViews = new ConfirmDialog(ts, ts.getTranslatedString("module.layout-editor.open-layout-view"), 
                                ts.getTranslatedString("module.general.messages.ok"));

        wdwLayoutViews.setContent(tblViews);
        wdwLayoutViews.getBtnConfirm().setVisible(false);
        wdwLayoutViews.setWidth("600px");
        wdwLayoutViews.open();
    }
    
     /**
     * open the dialog that shows the list of available views.
     */
    private void openCustomShapesListDialog() {
        wdwLayoutViews = new ConfirmDialog(ts, ts.getTranslatedString("module.layout-editor.open-custom-shape"), 
                                ts.getTranslatedString("module.general.messages.ok"));

        wdwLayoutViews.setContent(tblEditCustomShapes);
        wdwLayoutViews.getBtnConfirm().setVisible(false);
        wdwLayoutViews.setWidth("600px");
        wdwLayoutViews.open();
    }
    
    private void updatePropertySheetView() {
        if (currentView != null) {
            propSheetLayoutView.setVisible(true);
            ArrayList<AbstractProperty> viewProperties = new ArrayList<>();
            viewProperties.add(new StringProperty(Constants.PROPERTY_NAME,
                    Constants.PROPERTY_NAME, "", currentView.getName(), ts));
            viewProperties.add(new StringProperty(Constants.PROPERTY_DESCRIPTION,
                    Constants.PROPERTY_DESCRIPTION, "", currentView.getDescription(), ts));
            
            propSheetLayoutView.setItems(viewProperties);
        } else
            propSheetLayoutView.clear();
        propSheetLayoutView.getDataProvider().refreshAll();
    }

    @Override
    public void updatePropertyChanged(AbstractProperty property) {
        try {
            if (selectedObject != null) {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));

                bem.updateObject(selectedObject.getClassName(), selectedObject.getId(), attributes);
//                updatePropertySheet();
                saveCurrentView();

                //special case when the name is updated the label must be refreshed in the canvas
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    if (MxGraphCell.PROPERTY_VERTEX.equals(mxGraphCanvas.getSelectedCellType())) {
                        mxGraphCanvas.getNodes().get(selectedObject).setLabel((String) property.getValue());
                    } else {
                        mxGraphCanvas.getEdges().get(selectedObject).setLabel((String) property.getValue());
                    }
                    mxGraphCanvas.getMxGraph().refreshGraph();
                }

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
      
     /**
     * add a single icon node to the view
     *
     * @param node the node to be added
     */
    private void addShapeNodeToView(String shape) {
        BusinessObjectLight obj = new BusinessObjectLight(shape, UUID.randomUUID().toString(), shape);
        Properties props = new Properties();
        props.put("x", 50);
        props.put("y", 50);
        props.put("shape", shape);
        if (shape.equals(MxConstants.SHAPE_LABEL)) 
            props.put("label", "New Label");        
        addNode(obj, props);
    }
 
     public void addNode(BusinessObjectLight businessObject, Properties properties) {
   
                int x = (int) properties.get("x");
                int y = (int) properties.get("y");
                Integer width = (Integer) properties.get("w");
                Integer height = (Integer) properties.get("h");
                String urlImage = (String) properties.get("imageUrl");
                String shape = (String) properties.get("shape");
                String label = (String) properties.get("label");

                MxGraphNode newMxNode = new MxGraphNode();
                newMxNode.setUuid(businessObject.getId());
                newMxNode.setLabel(label);
                newMxNode.setWidth(width == null ? ((int) DEFAULT_SHAPE_WIDTH) : width);
                newMxNode.setHeight(height == null ? ((int) DEFAULT_SHAPE_HEIGHT) : height);
                newMxNode.setX(x);
                newMxNode.setY(y);
                if (urlImage == null) {// is a Free shape 
                    newMxNode.setStrokeColor("#000000");
                    newMxNode.setFillColor(MxConstants.NONE);
                    newMxNode.setShape(shape);
                    LinkedHashMap<String, String> mapStyle = new LinkedHashMap();
                    for (String style : BasicStyleEditor.supportedNodeStyles) {
                        String prop = (String) properties.get(style);
                        if ( prop != null && !prop.isEmpty() )
                           mapStyle.put(style, prop);
                    }
                    newMxNode.setRawStyle(mapStyle);
                    if (shape.equals(SHAPE_LABEL)) {
                        newMxNode.setAutosize(Boolean.TRUE);
                        newMxNode.setIsResizable(false);
                    }
                    newMxNode.addCellAddedListener(eventListener -> {
//                       newMxNode.setSelfPosition(0);
                       mxGraphCanvas.getMxGraph().refreshGraph();
                    });
                } else {
                    newMxNode.setShape(MxConstants.SHAPE_IMAGE);
                    newMxNode.setImage(urlImage);
                    newMxNode.setIsResizable(false);
                }

             mxGraphCanvas.addNode(businessObject, newMxNode);
           
       
    }   
    
    /**
     * Set the title/tool tip for the given button
     * @param button the button to be set
     * @param title the title to be added
     */
    public static void setButtonTitle(Button button, String title) {
        button.getElement().setProperty("title", title);     
    }

    private void setSelectionToolsEnabled(boolean b) {
        btnRemoveObjectFromView.setEnabled(b);
        btnCopyShape.setEnabled(b);
    }

    private void setGeneralToolsEnabled(boolean b) {
        btnSaveView.setEnabled(b);
        btnAddRectShape.setEnabled(b);
        btnAddRectShape.setEnabled(b);
        btnAddEllipseShape.setEnabled(b);
        btnAddLabel.setEnabled(b);
        btnCopyView.setEnabled(b && editingLayoutView);
        btnRemoveView.setEnabled(b);
        btnExportLayout.setEnabled(b);
    }

    private void buildCanvasSkeleton() { 

        int unitHeight = UNIT_HEIGHT, unitWidth = UNIT_WIDTH, deviceRackUnits, deviceRackPosition, currentRackUnitPosition = 0, currentRackUnitSize = 0;
        int rackUnits = 14;
        MxGraphNode rackUnit = new MxGraphNode();
        MxGraphNode deviceNode = null;
        MxGraphNode rackNode = new MxGraphNode();
        MxGraphNode mainBox = new MxGraphNode();
        mainBox.setUuid("*main");
        mainBox.setLabel("");
        mainBox.setGeometry(0, 0, unitWidth, unitHeight * rackUnits);
        mainBox.setIsResizable(false);
        mainBox.setIsMovable(false);
        mainBox.setFillColor(MxConstants.NONE);
 
        rackNode.setUuid("*rackNode");
        rackNode.setCellParent(mainBox.getUuid());
        rackNode.setLabel("");
        rackNode.setGeometry(0, 0, unitWidth, unitHeight * rackUnits);  
        rackNode.setIsResizable(false);
        rackNode.setIsMovable(false);
        rackNode.setFillColor(MxConstants.NONE);
        // added "*" to differentiate the nodes of the device layout with the nodes of the skeleton-guide

        mxGraphCanvas.addNode(new BusinessObjectLight(NODE_GUIDE, "*main", ""), mainBox);
        
        MxGraphNode nodeNumber;
        MxGraphNode nodeUnitNumbers = new MxGraphNode();
        nodeUnitNumbers.setUuid("*nodeNumbers");
        nodeUnitNumbers.setGeometry(50, 50, 50, unitHeight * rackUnits);
        nodeUnitNumbers.setCellParent(mainBox.getUuid());
//        mxGraphCanvas.addNode(new BusinessObjectLight("", UUID.randomUUID().toString(), ""), nodeUnitNumbers);
        mxGraphCanvas.addNode(new BusinessObjectLight(NODE_GUIDE, "*rackNode" , ""), rackNode);

        for (int i = 0; i < rackUnits; i++) {                    
            nodeNumber = new MxGraphNode();
            nodeNumber.setUuid("*nodeNumber" + i);
            nodeNumber.setLabel((i+1) + "");
            nodeNumber.setGeometry(0, i * unitHeight, 50, unitHeight);
            nodeNumber.setCellParent("nodeNumbers");
            nodeNumber.setVerticalLabelPosition(MxConstants.ALIGN_CENTER);
//            mxGraphCanvas.addNode(new BusinessObjectLight(NODE_GUIDE, UUID.randomUUID().toString(), ""), nodeNumber);
            
            rackUnit = new MxGraphNode();
            rackUnit.setUuid("*rackUnit" + i);
            rackUnit.setLabel("");
            rackUnit.setGeometry(0, i * unitHeight, unitWidth, unitHeight);
            rackUnit.setCellParent(rackNode.getUuid());
            rackUnit.setIsResizable(false);
            rackUnit.setIsMovable(false);
            rackUnit.setFillColor(MxConstants.NONE);
            mxGraphCanvas.addNode(new BusinessObjectLight(NODE_GUIDE, "*rackUnit" + i, ""), rackUnit);

            if (i == (rackUnits - 1)) {
                rackUnit.addCellAddedListener(eventListener -> {
//                    mxGraphCanvas.getMxGraph().executeStackLayout(nodeUnitNumbers.getUuid(), false, 0);
                    mxGraphCanvas.getMxGraph().executeStackLayout(rackNode.getUuid(), false, 15, 0, true);
                    mxGraphCanvas.getMxGraph().executeStackLayout(mainBox.getUuid(), true, 5);
                });
            }
        }                     
    }
    
    public byte[] getAsXML(boolean export) {
        try {
            double propY = 1, propX = 1,
                    propSize = 1;
            Map<BusinessObjectLight, ViewObject> mapCustomShapes = new HashMap<>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();

            QName tagView = new QName("view"); //NOI18N
            xmlew.add(xmlef.createStartElement(tagView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), "2.1")); 


            QName tagLayout = new QName("layout"); //NOI18N
            xmlew.add(xmlef.createStartElement(tagLayout, null, null));
            if (editingLayoutView) 
                xmlew.add(xmlef.createAttribute(new QName("name"), 
                    currentView.getName() == null || currentView.getName().trim().isEmpty() ? "" : currentView.getName())); 
            else
                xmlew.add(xmlef.createAttribute(new QName("name"), 
                    currentListTypeItem.getName() == null || currentListTypeItem.getName().trim().isEmpty() ? "" : currentListTypeItem.getName())); 
            List<MxGraphNode> deviceLayoutNodes = mxGraphCanvas.getNodes().entrySet().stream()
                    .filter(node -> !node.getKey().getClassName().equals(INNER_SHAPE)
                    && !node.getKey().getClassName().equals(NODE_GUIDE)).map(item -> item.getValue())
                    .collect(Collectors.toList());
            Rectangle layoutBounds = getLayoutBounds(deviceLayoutNodes);

            xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString((int) (layoutBounds.getX() / propX)))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString((int) (layoutBounds.getY() / propY)))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("width"), Integer.toString((int) (layoutBounds.getWidth() / propSize)))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("height"), Integer.toString((int) (layoutBounds.getHeight() / propSize)))); //NOI18N
            if (editingLayoutView)
                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_TYPE), "layout"));
            else
                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_TYPE), CLASS_CUSTOM));
            mxGraphCanvas.getNodes().forEach((objectNode, MxNode) -> {

                if (objectNode.getClassName().equals(NODE_GUIDE) || objectNode.getClassName().equals(INNER_SHAPE)) {
                    return;
                }
                try {
                    QName tagShape = new QName("shape"); //NOI18N
                    xmlew.add(xmlef.createStartElement(tagShape, null, null));

                    String shapeType = objectNode.getClassName();
                    HashMap mapStyle = MxNode.getRawStyleAsMap();
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_TYPE), shapeType));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_X), Integer.toString((int) ((MxNode.getX() - layoutBounds.getX()) / propX))));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_Y), Integer.toString((int) ((MxNode.getY() - layoutBounds.getY()) / propY))));
                    if (SHAPE_LABEL.equals(shapeType)) { // special case for fontsize in desktop app, its calculated by height and width
                        double fontSize = new Double((String) mapStyle.get(MxConstants.STYLE_FONTSIZE));
                        xmlew.add(xmlef.createAttribute(new QName(PROPERTY_HEIGHT), Integer.toString((int) (fontSize / 0.65))));
                    } else {
                        xmlew.add(xmlef.createAttribute(new QName(PROPERTY_HEIGHT), Integer.toString((int) (MxNode.getHeight() / propSize))));
                    }

                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_WIDTH), Integer.toString((int) (MxNode.getWidth() / propSize))));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_OPAQUE), shapeType.equals(MxConstants.SHAPE_RECTANGLE) ? "true" : "false"));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_IS_EQUIPMENT), "false"));
                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_NAME), (MxNode.getProperties().containsKey(PROPERTY_NAME) ? (String) MxNode.getProperties().get(PROPERTY_NAME) : "")));
                    
                    StyleSheet styleSheet = new StyleSheet();
                    if (SHAPE_CUSTOM.equals(shapeType)) {
                        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), objectNode.getName()));
                        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_CLASSNAME), CLASS_CUSTOM));
                    } else {
                        styleSheet = new StyleSheet();
                        String fillColor = (String) mapStyle.get(MxConstants.STYLE_FILLCOLOR);
                        Color clrFillColor = styleSheet.stringToColor(fillColor.equals(MxConstants.NONE) ? "#ffffff00" : fillColor);
                        xmlew.add(xmlef.createAttribute(new QName(PROPERTY_COLOR), clrFillColor.getRGB() + ""));
                        String strokeColor = (String) mapStyle.get(MxConstants.STYLE_STROKECOLOR);
                        Color clrStrokeColor;// = Color.decode(strokeColor.equals(MxConstants.NONE) ? "#000000" : fillColor) ;
                        clrStrokeColor = styleSheet.stringToColor(strokeColor.equals(MxConstants.NONE) ? "#000000" : strokeColor);
                        xmlew.add(xmlef.createAttribute(new QName(PROPERTY_BORDER_COLOR), clrStrokeColor.getRGB() + ""));
//                    xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_IS_EQUIPMENT), Boolean.toString(shape.isEquipment())));

                        if (SHAPE_RECTANGLE.equals(shapeType)) {
                            xmlew.add(xmlef.createAttribute(new QName(PROPERTY_IS_SLOT), (MxNode.getProperties().containsKey(PROPERTY_IS_SLOT) ? ((Boolean) MxNode.getProperties().get(PROPERTY_IS_SLOT)).toString() : "false")));
                        } else if (SHAPE_LABEL.equals(shapeType)) {
                            xmlew.add(xmlef.createAttribute(new QName("label"), MxNode.getLabel())); //NOI18N
                            Color fontColor = styleSheet.stringToColor((String) mapStyle.get(MxConstants.STYLE_FONTCOLOR));
                            xmlew.add(xmlef.createAttribute(new QName("textColor"), fontColor.getRGB() + "")); //NOI18N
                            xmlew.add(xmlef.createAttribute(new QName("fontSize"), Math.round(new Double((String) mapStyle.get(MxConstants.STYLE_FONTSIZE))) + "")); //NOI18N                          
                        } else if (SHAPE_ELLIPSE.equals(shapeType)) {
                            xmlew.add(xmlef.createAttribute(
                                    new QName(PROPERTY_ELLIPSE_COLOR), clrFillColor.getRGB() + ""));
                            xmlew.add(xmlef.createAttribute(
                                    new QName(PROPERTY_OVAL_COLOR), clrStrokeColor.getRGB() + ""));
                        } else if (SHAPE_POLYGON.equals(shapeType)) {
                            xmlew.add(xmlef.createAttribute(
                                    new QName(PROPERTY_INTERIOR_COLOR), clrFillColor.getRGB() + ""));
                            xmlew.add(xmlef.createAttribute(
                                    new QName(PROPERTY_OUTLINE_COLOR), clrFillColor.getRGB() + ""));
                        }
                    }
                     if (export && SHAPE_CUSTOM.equals(shapeType)) {
                         Optional<BusinessObjectLight> ocs = customShapes.stream().filter(item -> item.getId().equals(objectNode.getName())).findAny();
                         if (ocs.isPresent()) {
                            BusinessObjectLight cs = ocs.get();
                            ViewObject theView = null;
                            if (!mapCustomShapes.containsKey(cs)) {
                                List<ViewObjectLight> views;                           
                                try {
                                    views = aem.getListTypeItemRelatedLayout(cs.getId(), cs.getClassName(), 1); 
                                    if (!views.isEmpty()) { 
                                        theView = aem.getListTypeItemRelatedLayout(cs.getId(), cs.getClassName(), views.get(0).getId());
                                        mapCustomShapes.put(cs, theView);
                                    }
                                } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                                    Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                                        AbstractNotification.NotificationType.ERROR, ts).open();
                                }                                       
                            } else {
                                theView = mapCustomShapes.get(cs);
                            }
                            if (theView != null)
                                xmlew.add(xmlef.createCharacters(Base64.getEncoder().encodeToString(theView.getStructure())));
                         }
                    }
                    xmlew.add(xmlef.createEndElement(tagShape, null));
                } catch (XMLStreamException ex) {
                     new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();   
                    Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            xmlew.add(xmlef.createEndElement(tagLayout, null));

            xmlew.add(xmlef.createEndElement(tagView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();   
            return null;
        }
    }
    
    public Rectangle getLayoutBounds(List<MxGraphNode> children) {
        double xmin = Integer.MAX_VALUE;
        double ymin = Integer.MAX_VALUE;
        double xmax = Integer.MIN_VALUE;
        double ymax = Integer.MIN_VALUE;
                
        for (MxGraphNode child : children) {
            Point childPoint = new Point(child.getX(), child.getY());
           
            double childW = child.getWidth();
            double childH = child.getHeight();
            /*
                0-----1
                |     |
                |     |
                3-----2
            */
            Point [] points = new Point[4];
            points[0] = new Point(childPoint.getX(), childPoint.getY());
            points[1] = new Point(childPoint.getX() + childW, childPoint.getY());
            points[2] = new Point(childPoint.getX() + childW, childPoint.getY() + childH);
            points[3] = new Point(childPoint.getX(), childPoint.getY() + childH);
                        
            for (Point point : points) {
                if (xmin > point.getX()) {xmin = point.getX();}

                if (ymin > point.getY()) {ymin = point.getY();}

                if (xmax < point.getX()) {xmax = point.getX();}

                if (ymax < point.getY()) {ymax = point.getY();}
            }
        }
        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }
    
     private void renderShape(byte[] structure, int unitWidth, int unitHeight, MxGraphNode deviceNode, boolean renderCustomShape) throws FactoryConfigurationError, NumberFormatException {
        try {

//          <editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//            try {
//                FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/device_structure " + structure.length + ".xml");
//                fos.write(structure);
//                fos.close();
//            } catch (IOException e) {
//            }
//                     </editor-fold>

            if (structure == null || structure.length == 0)
                return;
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            
            QName tagLayout = new QName("layout"); //NOI18N
            QName tagCloseLayout = new QName("/layout"); //NOI18N
            QName tagShape = new QName("shape"); //NOI18N
            String attrValue;
            double percentWidth = 1, percentHeight = 1, percentX = 1, percentY = 1;
            double propY =  1, propX = renderCustomShape ? 1 : 1,
            propSize = 1; // Fix coordinates
            double layoutX = 0, layoutY = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagLayout)) {
                        attrValue = reader.getAttributeValue(null, "x"); //NOI18N
                        if (attrValue != null) {
                            layoutX = Integer.valueOf(attrValue) * percentWidth;
                        }
                        attrValue = reader.getAttributeValue(null, "y"); //NOI18N
                        if (attrValue != null) {
                            layoutY = Integer.valueOf(attrValue) * percentHeight;
                        }
                        attrValue = reader.getAttributeValue(null, "width"); //NOI18N
                        int width = 0;
                        if (attrValue != null) {
                            width = Integer.valueOf(attrValue);
                        }
                        int height = 0;
                        attrValue = reader.getAttributeValue(null, "height"); //NOI18N
                        if (attrValue != null) {
                            height = Integer.valueOf(attrValue);
                        }                        
                        if (renderCustomShape) {
                            percentWidth = deviceNode.getWidth() / (width * propSize );
                            percentHeight = deviceNode.getHeight() / (height * propSize);
                        }
                    }
                    if (reader.getName().equals(tagShape)) {
                        String shapeType = reader.getAttributeValue(null, "type");
                        
                        int color, borderColor, textColor;
                        double width = 50, height = 50, x = 0, y = 0;
                        String name, label;
                        
                        if (shapeType != null) {
                            
                            MxGraphNode nodeShape = new MxGraphNode();
                            String nodeUuid = UUID.randomUUID().toString();
                            nodeShape.setUuid(nodeUuid);
                            nodeShape.setCellParent(deviceNode.getUuid());
                                                          
                            attrValue = reader.getAttributeValue(null, PROPERTY_X);
                            if (attrValue != null) {
                                x = Double.valueOf(attrValue) * percentWidth + (!renderCustomShape ? layoutX : 0);
                            }
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_Y);
                            if (attrValue != null) {
                                y = Double.valueOf(attrValue) * percentHeight + (!renderCustomShape ? layoutY : 0);
                            }
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_WIDTH);
                            if (attrValue != null) {
                                width = Double.valueOf(attrValue) * percentWidth;
                            }
                            
                            attrValue = reader.getAttributeValue(null, PROPERTY_HEIGHT);
                            if (attrValue != null) {
                                height = Double.valueOf(attrValue) * percentHeight;
                            }                            
                            
                            double widthAdjust;
                            width = (width * propSize);
                            height = height * propSize;
                            if (width > unitWidth) {
                                width = unitWidth;
                            }

                            x = x * propX;
                            y = y * propY;

                            
                            nodeShape.setGeometry((int) x, (int) y, (int) width, (int) height);
                            nodeShape.setIsEditable(false);
                            nodeShape.setStrokeColor(MxConstants.NONE);
                            if (renderCustomShape) {
                                nodeShape.setIsSelectable(false);
                                nodeShape.setIsConstituent(true);
                            }
                            name = reader.getAttributeValue(null, PROPERTY_NAME);
                            if (name != null) 
                                nodeShape.getProperties().put(PROPERTY_NAME, name);
                            if (SHAPE_CUSTOM.equals(shapeType)) {
                                nodeShape.setShape(MxConstants.SHAPE_RECTANGLE);   
                                String id = reader.getAttributeValue(null, Constants.PROPERTY_ID);
                                String className = reader.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);   
                                Optional<BusinessObjectLight> optObj = customShapes.stream().filter(item -> item.getId().equals(id)).findFirst();                             
                                if (!optObj.isPresent()) {
                                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), String.format(ts.getTranslatedString("module.layout-editor.custom-shape-not-found"), id), 
                                         AbstractNotification.NotificationType.INFO, ts).open();
                                    continue;
                                }
                                BusinessObjectLight lstTypeObject = optObj.get();
                                if (name == null)
                                    nodeShape.getProperties().put(PROPERTY_NAME, lstTypeObject.getName());
                                
                                byte[] customShapeStructure = null;
                                if (layoutDevices.containsKey(lstTypeObject)) {
                                    customShapeStructure = layoutDevices.get(lstTypeObject);
                                } else {
                                    try {
                                        List<ViewObjectLight> views = aem.getListTypeItemRelatedLayout(id, className, 1);
                                        if (!views.isEmpty()) {
                                            ViewObject view = aem.getListTypeItemRelatedLayout(id, className, views.get(0).getId());
                                            customShapeStructure = view.getStructure();
                                            layoutDevices.put(lstTypeObject, customShapeStructure);
                                        }
                                    } catch(ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                                             AbstractNotification.NotificationType.ERROR, ts).open();
                                    }
                                }
                                if (customShapeStructure != null) {
                                    mxGraphCanvas.addNode(new BusinessObjectLight((renderCustomShape ? INNER_SHAPE : SHAPE_CUSTOM), nodeUuid, id), nodeShape);                                 
                                    renderShape(customShapeStructure, unitWidth, unitHeight, nodeShape, true);                                    
                                }
                            } else {
                                attrValue = reader.getAttributeValue(null, PROPERTY_COLOR);
                                if (attrValue != null) {
                                    color = (Integer.valueOf(attrValue));
                                    nodeShape.setFillColor(UtilHtml.toHexString(new Color(color)));
                                }
                                
                                attrValue = reader.getAttributeValue(null, PROPERTY_BORDER_COLOR);
                                if (attrValue != null) {
                                    borderColor = (Integer.valueOf(attrValue));
                                    nodeShape.setStrokeColor(UtilHtml.toHexString(new Color(borderColor)));
                                }
                                
                                if (SHAPE_RECTANGLE.equals(shapeType)) {
                                    nodeShape.setShape(MxConstants.SHAPE_RECTANGLE);                                       
                                     attrValue = reader.getAttributeValue(null, PROPERTY_IS_SLOT);
                                     if (attrValue != null && !attrValue.isEmpty()) 
                                        nodeShape.getProperties().put(PROPERTY_IS_SLOT, Boolean.valueOf(attrValue));
                                     
                                } else if (SHAPE_LABEL.equals(shapeType)) {
                                    
                                    nodeShape.setShape(MxConstants.SHAPE_LABEL);
                                    if (!renderCustomShape)
                                        nodeShape.setIsResizable(false);
                                    nodeShape.setAutosize(true);
                                    label = reader.getAttributeValue(null, "label"); //NOI18N                                    
                                    nodeShape.setLabel(label);
                                    if (!renderCustomShape)
                                        nodeShape.setIsEditable(true);
                                    
                                    attrValue = reader.getAttributeValue(null, "textColor"); //NOI18N
                                    if (attrValue != null) {
                                        textColor = (Integer.valueOf(attrValue));
                                        nodeShape.setFontColor(UtilHtml.toHexString(new Color(textColor)));
                                    }
                                    
                                    nodeShape.setFontSize(height * 0.65);
                                    
                                } else if (SHAPE_ELLIPSE.equals(shapeType)) {
                                    nodeShape.setShape(MxConstants.SHAPE_ELLIPSE);
                                    attrValue = reader.getAttributeValue(null, PROPERTY_ELLIPSE_COLOR);
                                    if (attrValue != null) {
                                        color = (Integer.valueOf(attrValue));
                                        nodeShape.setFillColor(UtilHtml.toHexString(new Color(color)));
                                    }
                                    
                                    attrValue = reader.getAttributeValue(null, PROPERTY_OVAL_COLOR);
                                    if (attrValue != null) {
                                        color = (Integer.valueOf(attrValue));
                                        nodeShape.setStrokeColor(UtilHtml.toHexString(new Color(color)));
                                    }
                                } else if (SHAPE_POLYGON.equals(shapeType)) {
                                    
                                    nodeShape.setShape(MxConstants.SHAPE_TRIANGLE);
                                    attrValue = reader.getAttributeValue(null, PROPERTY_INTERIOR_COLOR);
                                    if (attrValue != null) {
                                        color = (Integer.valueOf(attrValue));
                                        nodeShape.setFillColor(UtilHtml.toHexString(new Color(color)));
                                    }
                                    
                                    attrValue = reader.getAttributeValue(null, PROPERTY_OUTLINE_COLOR);
                                    if (attrValue != null) {
                                        color = (Integer.valueOf(attrValue));
                                        nodeShape.setStrokeColor(UtilHtml.toHexString(new Color(color)));
                                    }
                                }
                                mxGraphCanvas.addNode(new BusinessObjectLight(renderCustomShape ? INNER_SHAPE : shapeType, nodeUuid, ""), nodeShape);
                            }
                        }
                    } 
                    if (reader.getName().equals(tagCloseLayout)) {
                        if (renderCustomShape) {
                            
                        }
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();           
        } 
    }

    private void openCreateNewCustomShapeDlg() {
         ConfirmDialog dlgCreateCustomShape = new ConfirmDialog(ts, ts.getTranslatedString("module.layout-editor.new-custom-shape"), ts.getTranslatedString("module.general.labels.create"));
         TextField txtNewShapeName = new TextField(ts.getTranslatedString("module.layout-editor.shape-name"));
         dlgCreateCustomShape.getBtnConfirm().addClickListener((e) -> {        
                if (txtNewShapeName.getValue() == null || txtNewShapeName.getValue().isEmpty()) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.general.messages.must-fill-all-fields"), AbstractNotification.NotificationType.WARNING, ts).open();
                } else {
                    try {
                        createNewCustomShape(txtNewShapeName.getValue(), null);
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("Custom shape Craeted"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                        dlgCreateCustomShape.close();
                        
                    } catch (MetadataObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException ex) {
                        Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    }                    
                }         
        });
        
        txtNewShapeName.addValueChangeListener((e) -> {
            dlgCreateCustomShape.getBtnConfirm().setEnabled(!txtNewShapeName.isEmpty());
        });
        txtNewShapeName.setWidth("350px");
        dlgCreateCustomShape.add(txtNewShapeName);
        dlgCreateCustomShape.open();
    }
    
    private void removeCustomShape(BusinessObjectLight listTypeItem, ViewObjectLight view) {
        if (currentView != null) {
            
            try {
                aem.deleteListTypeItemRelatedLayout(listTypeItem.getId(), listTypeItem.getClassName(), view.getId());
                aem.deleteListTypeItem(listTypeItem.getClassName(), listTypeItem.getId(), true);
  
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.layout-editor.custom-shape-deleted"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            } catch (MetadataObjectNotFoundException | InvalidArgumentException
                    | ApplicationObjectNotFoundException | BusinessObjectNotFoundException
                    | OperationNotPermittedException | NotAuthorizedException ex) {
                Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }

    private void createNewLayoutView() {
         this.newLayoutViewVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
    }
    
    private void openDlgRelateListTypeItem() {
        try {
            List<ClassMetadataLight> listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
            ComboBox<ClassMetadataLight> cmbLstTypes = new ComboBox(ts.getTranslatedString("module.layout-editor.list-type"));
            cmbLstTypes.setItems(listTypes);
            
            ComboBox<BusinessObjectLight> cmbLstTypeItems = new ComboBox(ts.getTranslatedString("module.layout-editor.list-type-items"));
            cmbLstTypes.addValueChangeListener(listener -> {
               if (listener.getValue() != null) {
                    try {
                        cmbLstTypeItems.setItems(aem.getListTypeItems(listener.getValue().getName()));
                    } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                        Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                } else 
                   cmbLstTypeItems.setItems();
            });
            
            ConfirmDialog dlgConfirm = new ConfirmDialog(ts, new VerticalLayout(cmbLstTypes, cmbLstTypeItems), 
                    ts.getTranslatedString("module.general.messages.ok"), () -> {
                        if (cmbLstTypeItems.getValue() != null) {
                            try {
                                aem.setListTypeItemRelatedLayout(cmbLstTypeItems.getValue().getId(), cmbLstTypeItems.getValue().getClassName(),
                                        currentView.getId());
                                refreshLayoutViews();
                                currentListTypeItem = cmbLstTypeItems.getValue();
                                udpateRelatedListType();
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.layout-editor.list-type-item-related-successfully"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                            } catch (InvalidArgumentException | MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                                Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                            }
                        }
                    });
            dlgConfirm.open();
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    private void udpateRelatedListType() {
        lytRelatedListType.setVisible(true);
        if (currentListTypeItem == null)
            lblListTypeItem.setText(ts.getTranslatedString("module.layout-editor.not-related-list-type-item"));
        else 
            lblListTypeItem.setText(currentListTypeItem.toString());     
    }

    private void copyCurrentView() {
        if (currentView != null) {

            TextField txtName = new TextField(ts.getTranslatedString("module.layout-editor.copy-name"));
            ConfirmDialog dlgConfirm = new ConfirmDialog(ts, new VerticalLayout(txtName),
                    ts.getTranslatedString("module.general.messages.ok"), () -> {
                if (txtName.getValue() != null) {
                    try {
                        if (editingLayoutView) {
                            long newViewId = aem.createLayout("", txtName.getValue() , currentView.getName(), currentView.getStructure(), null);
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), 
                                    ts.getTranslatedString("module.layout-editor.copy-view-created"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                        } else {
                            String id = aem.createListTypeItem(CLASS_CUSTOM, txtName.getValue(), txtName.getValue());
                            aem.createListTypeItemRelatedLayout(id, CLASS_CUSTOM, "DeviceLayoutView", " ", null, currentView.getStructure(), null);
                            loadCustomShapes();
                            tblCustomShapes.setItems(customShapes);
                            tblCustomShapes.getDataProvider().refreshAll();
                            tblEditCustomShapes.setItems(customShapes);
                            tblEditCustomShapes.getDataProvider().refreshAll();
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), 
                                    ts.getTranslatedString("Custom Shape Copy Created"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                        }

                    } catch (InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    }                  
                }

            });
            dlgConfirm.setHeader(ts.getTranslatedString("module.layout-editor.copy-view"));
            dlgConfirm.open();

        }
    }

    private void copySelectedShape() {
        if (selectedObject != null) {
            if (MxGraphCell.PROPERTY_VERTEX.equals(mxGraphCanvas.getSelectedCellType())) {
                MxGraphNode selectedMxNode  =  mxGraphCanvas.getNodes().values().stream().filter(item -> item.getUuid().equals(selectedObject.getId())).findAny().get();
                MxGraphNode newNode = new MxGraphNode();
                newNode.setWidth(selectedMxNode.getWidth());
                newNode.setHeight(selectedMxNode.getHeight());
                newNode.setX(selectedMxNode.getX()+ selectedMxNode.getWidth() + 20);
                newNode.setY(selectedMxNode.getY());
                newNode.setUuid(UUID.randomUUID().toString());
                newNode.setShape(selectedMxNode.getShape());
                
                if (selectedMxNode.getShape().equals(SHAPE_CUSTOM)) {
                    addCustomShapeToView(selectedObject, newNode);
                    return;
                }
                newNode.setFillColor(selectedMxNode.getFillColor());
                newNode.setStrokeColor(selectedMxNode.getStrokeColor());
                if (selectedMxNode.getShape().equals(MxConstants.SHAPE_LABEL)) {
                    newNode.setLabel(selectedMxNode.getLabel());
                    newNode.setFontColor(selectedMxNode.getFontColor());
                    newNode.setFontSize(selectedMxNode.getFontSize());
                }
                mxGraphCanvas.addNode(new BusinessObjectLight(newNode.getShape(), newNode.getUuid(), ""), newNode);
            } 
        }
    }

    private void openImportLayoutDlg() {

        MemoryBuffer bufferIcon = new MemoryBuffer();
        Upload uploadView = new Upload(bufferIcon);
        uploadView.setWidth("400px");
        ConfirmDialog dlg = new ConfirmDialog(ts, ts.getTranslatedString("module.layout-editor.import-view"), ts.getTranslatedString("module.general.messages.ok"));
        dlg.setContent(uploadView);
        uploadView.setMaxFiles(1);
        uploadView.setDropLabel(new Label(ts.getTranslatedString("module.datamodelman.dropmessage")));
        uploadView.addSucceededListener(evt -> {
            try {
                byte[] data = IOUtils.toByteArray(bufferIcon.getInputStream());
                if (data.length > 0) {

                    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                    QName qView = new QName("view"); //NOI18N
                    QName tagShape = new QName("shape"); //NOI18N
                    QName tagLayout = new QName("layout"); //NOI18N

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
                    XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
                    XMLEventFactory xmlef = XMLEventFactory.newInstance();
                    QName tagView = new QName("view"); //NOI18N
                    xmlew.add(xmlef.createStartElement(tagView, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("version"), "2.1"));

                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
                    String viewName = "";
                    String viewType = "";
                    while (reader.hasNext()) {
                        int event = reader.next();

                        if (event == XMLStreamConstants.START_ELEMENT) {
                           if (reader.getName().equals(tagView)) {
                               String version = reader.getAttributeValue(null, "version");
                               if (version == null || !version.equals("2.1")) {
                                   xmlew.close();
                                   reader.close();
                                   new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                          "module.layout-editor.error-view-version", AbstractNotification.NotificationType.ERROR, ts).open();
                                   return;
                               }
                           }
                           if (reader.getName().equals(tagLayout)) {
                                xmlew.add(xmlef.createStartElement(tagLayout, null, null));
                                viewName = reader.getAttributeValue(null, "name");
                                xmlew.add(xmlef.createAttribute(new QName("name"), viewName == null ? "" : viewName));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_X), reader.getAttributeValue(null, PROPERTY_X)));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_Y), reader.getAttributeValue(null, PROPERTY_Y)));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_HEIGHT), reader.getAttributeValue(null, PROPERTY_HEIGHT)));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_WIDTH), reader.getAttributeValue(null, PROPERTY_WIDTH)));
                                viewType = reader.getAttributeValue(null, "type");
                            }
                            if (reader.getName().equals(qView)) {
                                viewName = reader.getAttributeValue(null, "name");
//                        String out = getOuterXml(reader);
                            } else if (reader.getName().equals(tagShape)) {
                                String shapeType = reader.getAttributeValue(null, "type");
                                xmlew.add(xmlef.createStartElement(tagShape, null, null));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_TYPE), shapeType));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_X), reader.getAttributeValue(null, PROPERTY_X)));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_Y), reader.getAttributeValue(null, PROPERTY_Y)));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_HEIGHT), reader.getAttributeValue(null, PROPERTY_HEIGHT)));

                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_WIDTH), reader.getAttributeValue(null, PROPERTY_WIDTH)));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_OPAQUE), reader.getAttributeValue(null, PROPERTY_OPAQUE)));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_IS_EQUIPMENT), "false"));
                                xmlew.add(xmlef.createAttribute(new QName(PROPERTY_NAME), reader.getAttributeValue(null, PROPERTY_NAME)));

                                if (SHAPE_CUSTOM.equals(shapeType)) {
                                    String csName = reader.getAttributeValue(null, "name");
                                    byte [] csView = Base64.getDecoder().decode(reader.getElementText());
                                    
                                    Optional<BusinessObjectLight> op = customShapes.stream().filter(item -> item.getName().equals(csName)).findAny();
                                    if (op.isPresent()) 
                                        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), op.get().getId()));
                                     else {
                                        String newId = createNewCustomShape(csName, csView);
                                        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), newId));
                                    }
                                    xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_CLASSNAME), CLASS_CUSTOM));
                                } else {

                                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_COLOR), reader.getAttributeValue(null, PROPERTY_COLOR)));
                                    xmlew.add(xmlef.createAttribute(new QName(PROPERTY_BORDER_COLOR), reader.getAttributeValue(null, PROPERTY_BORDER_COLOR)));

                                    if (SHAPE_RECTANGLE.equals(shapeType)) {
                                        xmlew.add(xmlef.createAttribute(new QName(PROPERTY_IS_SLOT), reader.getAttributeValue(null, PROPERTY_IS_SLOT)));
                                    } else if (SHAPE_LABEL.equals(shapeType)) {
                                        xmlew.add(xmlef.createAttribute(new QName("label"), reader.getAttributeValue(null, "label")));
                                        xmlew.add(xmlef.createAttribute(new QName("textColor"), reader.getAttributeValue(null, "textColor")));
                                        xmlew.add(xmlef.createAttribute(new QName("fontSize"), reader.getAttributeValue(null, "fontSize")));
                                    } else if (SHAPE_ELLIPSE.equals(shapeType)) {
                                        xmlew.add(xmlef.createAttribute(
                                                new QName(PROPERTY_ELLIPSE_COLOR), reader.getAttributeValue(null, PROPERTY_ELLIPSE_COLOR)));
                                        xmlew.add(xmlef.createAttribute(
                                                new QName(PROPERTY_OVAL_COLOR), reader.getAttributeValue(null, PROPERTY_OVAL_COLOR)));
                                    } else if (SHAPE_POLYGON.equals(shapeType)) {
                                        xmlew.add(xmlef.createAttribute(
                                                new QName(PROPERTY_INTERIOR_COLOR), reader.getAttributeValue(null, PROPERTY_INTERIOR_COLOR)));
                                        xmlew.add(xmlef.createAttribute(
                                                new QName(PROPERTY_OUTLINE_COLOR), reader.getAttributeValue(null, PROPERTY_OUTLINE_COLOR)));
                                    }
                                }
                                xmlew.add(xmlef.createEndElement(tagShape, null));
                            }
                        }
                    }
                    reader.close();
                    xmlew.add(xmlef.createEndElement(tagLayout, null));

                    xmlew.add(xmlef.createEndElement(tagView, null));
                    xmlew.close();
                    if (!viewType.isEmpty()) {
                        if (viewType.equals(CLASS_CUSTOM)) {
                            String newId = createNewCustomShape(viewName, baos.toByteArray());  
                            loadCustomShapes();
                            tblCustomShapes.setItems(customShapes);
                            tblCustomShapes.getDataProvider().refreshAll();
                            tblEditCustomShapes.setItems(customShapes);
                            tblEditCustomShapes.getDataProvider().refreshAll();
                        } else {
                            long newViewId = aem.createLayout("", viewName, "", baos.toByteArray(), null);
                            loadLayouts();
                            tblViews.getDataProvider().refreshAll();
                            tblViews.setItems(deviceLayouts);
                            tblViews.getDataProvider().refreshAll();
                        }
                        dlg.close();
                    } else {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                                "module.layout-editor.error-view-type", AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("View Imported Succesfully"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                    
                } else {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ts.getTranslatedString("module.layout-editor.corrupted-view"), AbstractNotification.NotificationType.ERROR, ts).open();
                }
                dlg.close();
            } catch (XMLStreamException | IOException ex) {
                dlg.close();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ts.getTranslatedString("module.layout-editor.corrupted-view"), AbstractNotification.NotificationType.ERROR, ts).open();
            } catch (MetadataObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | TransformerFactoryConfigurationError ex) {
                Logger.getLogger(LayoutEditorDashboard.class.getName()).log(Level.SEVERE, null, ex);
                dlg.close();
            }
        });

        uploadView.addFileRejectedListener(listener -> {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    listener.getErrorMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
        }
        );
        
        dlg.open();
    }
 
    private StreamResource createStreamResource(String name, byte[] ba) {
        return new StreamResource(name, () -> new ByteArrayInputStream(ba));                                
    }  

    private String createNewCustomShape(String txtShapeName, byte [] structure) throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {
            String id = aem.createListTypeItem(CLASS_CUSTOM, txtShapeName, txtShapeName);
            aem.createListTypeItemRelatedLayout(id, CLASS_CUSTOM, "DeviceLayoutView", " ", null, structure, null);
            loadCustomShapes();
            tblCustomShapes.setItems(customShapes);
            tblCustomShapes.getDataProvider().refreshAll();
            tblEditCustomShapes.setItems(customShapes);
            tblEditCustomShapes.getDataProvider().refreshAll();
            return id;
    }

}
