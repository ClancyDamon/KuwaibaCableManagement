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

package com.neotropic.kuwaiba.modules.commercial.mpls.widgets;

import com.neotropic.flow.component.mxgraph.MxGraphBindedKeyEvent;
import com.neotropic.kuwaiba.modules.commercial.mpls.MplsManagerUI;
import com.neotropic.kuwaiba.modules.commercial.mpls.MplsView;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.DeleteMplsViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.NewMplsViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.model.MplsConnectionDefinition;
import com.neotropic.kuwaiba.modules.commercial.mpls.MplsService;
import com.neotropic.kuwaiba.modules.commercial.mpls.tools.MplsTools;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.AnnotatedBusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavigationTree;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.icons.BasicTreeNodeIconGenerator;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.StringProperty;

/**
 * MPLS Main Dashboard.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
public class MplsDashboard extends VerticalLayout {
    
    private final TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private final BusinessEntityManager bem;
    /**
     * listener to remove mpls view action
     */
    private ActionCompletedListener listenerDeleteAction;
    /**
     * listener to add new view Action
     */
    private ActionCompletedListener listenerNewViewAction;
    /**
     * reference of the visual action to remove a mpls view
     */
    private final DeleteMplsViewVisualAction deleteMPLSViewVisualAction;
    /**
     * reference of the visual action to add a mpls view
     */
    private final NewMplsViewVisualAction newMPLSViewVisualAction;
    /**
     * factory to instance object icons
     */
    private final ResourceFactory resourceFactory;
    /**
     * service to persistence actions
     */
    private final MplsService mplsService;
     /**
     * source Equipment in create new connection dialog
     */   
    private BusinessObjectLight selectedSourceEquipment;
    /**
     * target Equipment in create new connection dialog
     */
    private BusinessObjectLight selectedTargetEquipment;
    /**
     * source end point in create new connection dialog
     */
    private BusinessObjectLight selectedEndPointA;
    /**
     * target End point in create new connection dialog
     */
    private BusinessObjectLight selectedEndPointB;  
    /**
     * current view in the canvas
     */
    private ViewObject currentView;
    /**
     * canvas toolbar
     */
    private MplsTools mplsTools;
    /**
     * Instance of the main canvas view
     */
    private MplsView mplsView;
    /**
     * list of mpls views
     */
    private List<ViewObjectLight> mplsViews;
    /**
     * Reference to the grid that shows the mpls views 
     */
    private Grid<ViewObjectLight> tblViews;
    /**
     * Dialog that lists the whole list of the views
     */
    private Dialog wdwMPLSViews;
    /**
     * reference to the current selected object in the canvas
     */
    private BusinessObjectLight selectedObject;
    /**
     *  property sheet instance for canvas objects
     */
    private PropertySheet propSheetObjects;
     /**
     * main property sheet instance for mpls properties
     */
    private PropertySheet propSheetMPLS;
    
    private boolean openningView;

    public ViewObject getCurrentView() {
        return currentView;
    }

    public void setCurrentView(ViewObject currentView) {
        this.currentView = currentView;
        resetDashboard();
    }

    public MplsTools getMplsTools() {
        return mplsTools;
    }

    public void setMplsTools(MplsTools mplsTools) {
        this.mplsTools = mplsTools;
    }  
    
    public MplsDashboard(TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, 
            ResourceFactory resourceFactory,MplsService mplsService, DeleteMplsViewVisualAction deleteMPLSViewVisualAction, 
            NewMplsViewVisualAction newMPLSViewVisualAction) {
        super();
        this.ts = ts;
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.resourceFactory = resourceFactory;
        this.mplsService = mplsService;
        this.newMPLSViewVisualAction = newMPLSViewVisualAction;
        this.deleteMPLSViewVisualAction = deleteMPLSViewVisualAction;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        this.openningView = false;
    }        

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent); 
        createContent();
    }
   
    @Override
    public void onDetach(DetachEvent ev) {
        this.deleteMPLSViewVisualAction.unregisterListener(listenerDeleteAction);
        this.newMPLSViewVisualAction.unregisterListener(listenerNewViewAction);
        mplsView.getMxgraphCanvas().getMxGraph().removeListeners();
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
        
        mplsView = new MplsView(mem, aem, bem, ts, resourceFactory); 
        mplsView.getMxgraphCanvas().setComObjectSelected(() -> {
            
            String objectId = mplsView.getMxgraphCanvas().getSelectedCellId();
            if (MxGraphCell.PROPERTY_VERTEX.equals(mplsView.getMxgraphCanvas().getSelectedCellType())){
                 selectedObject = ((BusinessObjectViewNode) mplsView.getAsViewMap().findNode(objectId)).getIdentifier();
            } else {
                 selectedObject = ((BusinessObjectViewEdge) mplsView.getAsViewMap().findEdge(objectId)).getIdentifier();            
            }
            updatePropertySheetObjects();
            mplsTools.setGeneralToolsEnabled(true);
            mplsTools.setSelectionToolsEnabled(true);
        });
        mplsView.getMxgraphCanvas().setComObjectUnselected(() -> {
            selectedObject = null;
            updatePropertySheetObjects();
            mplsTools.setSelectionToolsEnabled(false);
        });
        mplsView.getMxgraphCanvas().setComObjectDeleted(() -> {
            openConfirmDialogDeleteObject();   
        }); 
        mplsView.getMxgraphCanvas().getMxGraph().addGraphChangedListener(eventListener -> {
            if (!openningView)
                saveCurrentView();
        });this.
        mplsView.getMxgraphCanvas().getMxGraph().addGraphLoadedListener(eventListener -> {
            mplsView.getMxgraphCanvas().getMxGraph().bindKey(37, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(37 + "")) {
                    MxGraphNode node = mplsView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                    node.setX(node.getX() - ARROWS_KEY_DELTA);
                    saveCurrentView();
                }
            });
            mplsView.getMxgraphCanvas().getMxGraph().bindKey(39, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(39 + "")) {
                    MxGraphNode node = mplsView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                    node.setX(node.getX() + ARROWS_KEY_DELTA);
                    saveCurrentView();
                }
            });
            mplsView.getMxgraphCanvas().getMxGraph().bindKey(38, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(38 + "")) {
                    MxGraphNode node = mplsView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                    node.setY(node.getY() - ARROWS_KEY_DELTA);
                    saveCurrentView();
                }
            });
            mplsView.getMxgraphCanvas().getMxGraph().bindKey(40, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(40 + "")) {
                    MxGraphNode node = mplsView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                    node.setY(node.getY() + ARROWS_KEY_DELTA);
                    saveCurrentView();
                }
            });
        });
       
        mplsTools = new MplsTools(mplsView, bem, ts);
        
        mplsTools.getBtnOpenView().addClickListener(ev -> {
             openListMplsViewDialog();
        });
        mplsTools.getBtnNewView().addClickListener(ev -> {
             this.newMPLSViewVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });
        mplsTools.getBtnRemoveView().addClickListener(evt -> {
            if (currentView != null)
                this.deleteMPLSViewVisualAction.getVisualComponent(new ModuleActionParameterSet( new ModuleActionParameter("viewId", currentView.getId()))).open();
        });
        
        mplsTools.addNewObjectListener(event -> {   
             BusinessObjectLight tmpObject = event.getObject();
             if (tmpObject == null)
                 return;
             try {
                 
                if(tmpObject.getClassName().equals(Constants.CLASS_MPLSLINK)) {                  
                   MplsConnectionDefinition connectionDetails = mplsService.getMPLSLinkDetails(tmpObject.getId());                    
                   if (connectionDetails.getDeviceA() != null && connectionDetails.getDeviceB() != null) {
                       addNodeToView(connectionDetails.getDeviceA(), 100, 50);
                       addNodeToView(connectionDetails.getDeviceB(), 400, 50);
                       addEdgeToView(connectionDetails);
                   } else 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.null-endpoint"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                }  else 
                        addNodeToView(tmpObject, 100, 50);               
                
//                saveCurrentView();
             } catch (InvalidArgumentException ex) {
                 Notification.show(ex.getMessage());
             }
        });
        mplsTools.addSelectObjectListener(event -> {   
             BusinessObjectLight tmpObject = event.getObject();
             if (tmpObject == null)
                 return;
             MxGraphCell cell;
             if(tmpObject.getClassName().equals(Constants.CLASS_MPLSLINK))
                 cell = mplsView.getMxgraphCanvas().getEdges().get(tmpObject);
             else
                 cell = mplsView.getMxgraphCanvas().getNodes().get(tmpObject);
             if (cell != null)
                 cell.selectCell();

        });
        
        mplsTools.addNewConnectionListener(event -> {   
            selectedSourceEquipment = null;
            selectedTargetEquipment = null;
            openNewConnectionDialog();           
        });
        mplsTools.addSaveViewListener(event -> {
            saveCurrentView();
        });
        mplsTools.addDeleteObjectListener(event -> {
           deleteSelectedObject(false); 
//           saveCurrentView();
        });
        mplsTools.addDeleteObjectPermanentlyObjectListener(event -> {
            openConfirmDialogDeleteObject();
        });
        mplsTools.AddDetectConnectionsListener(event -> {
            detectRelationships();
//            saveCurrentView();
        });
        mplsTools.setGeneralToolsEnabled(false);
        
        initializeActions();
        initializeTblViews();
                   
        VerticalLayout lytDashboard = new VerticalLayout(mplsTools, mplsView.getAsUiElement());
        lytDashboard.setWidth("70%");
        //prop sheet section
        PropertySheet.IPropertyValueChangedListener listenerPropSheetObjects = new PropertySheet.IPropertyValueChangedListener() {
            @Override
            public void updatePropertyChanged(AbstractProperty property) {
                try {
                    if (selectedObject != null) {
                        HashMap<String, String> attributes = new HashMap<>();
                        attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));

                        bem.updateObject(selectedObject.getClassName(), selectedObject.getId(), attributes);
                        updatePropertySheetObjects();
                        saveCurrentView();

                        //special case when the name is updated the label must be refreshed in the canvas
                        if (property.getName().equals(Constants.PROPERTY_NAME)) {
                            if (MxGraphCell.PROPERTY_VERTEX.equals(mplsView.getMxgraphCanvas().getSelectedCellType())) {
                                mplsView.getMxgraphCanvas().getNodes().get(selectedObject).setLabel((String) property.getValue());
                            } else {
                                mplsView.getMxgraphCanvas().getEdges().get(selectedObject).setLabel((String) property.getValue());
                            }
                            mplsView.getMxgraphCanvas().getMxGraph().refreshGraph();
                        }

                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        };
        propSheetObjects = new PropertySheet(ts, new ArrayList<>());
        propSheetObjects.addPropertyValueChangedListener(listenerPropSheetObjects);
        
        PropertySheet.IPropertyValueChangedListener listenerPropSheetMPLS = new PropertySheet.IPropertyValueChangedListener() {
            @Override
            public void updatePropertyChanged(AbstractProperty property) {
                if (currentView != null) {
                    if (property.getName().equals(Constants.PROPERTY_NAME))
                        currentView.setName(property.getAsString());
                    if (property.getName().equals(Constants.PROPERTY_DESCRIPTION))
                        currentView.setDescription(property.getAsString());
                    
                    saveCurrentView();
                    loadViews();
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                }
            }
        };
        propSheetMPLS = new PropertySheet(ts, new ArrayList<>());
        propSheetMPLS.addPropertyValueChangedListener(listenerPropSheetMPLS);     
        
        Accordion accordion = new Accordion();
        accordion.setWidthFull();
          
        BoldLabel lblViewProperties = new BoldLabel(ts.getTranslatedString("module.mpls.view-properties"));
        lblViewProperties.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryViewProp = new HorizontalLayout(lblViewProperties); 
        lytSummaryViewProp.setWidthFull();       
        AccordionPanel apViewProp = new AccordionPanel(lytSummaryViewProp, propSheetMPLS);
        accordion.add(apViewProp);
        
        BoldLabel lblObjectProperties = new BoldLabel(ts.getTranslatedString("module.mpls.object-properties"));
        lblObjectProperties.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryObjectProp = new HorizontalLayout(lblObjectProperties); 
        lytSummaryObjectProp.setWidthFull();       
        AccordionPanel apObjectProp = new AccordionPanel(lytSummaryObjectProp, propSheetObjects);
        accordion.add(apObjectProp);
        
        Label lblHelp = new Label(ts.getTranslatedString("module.mpls.help"));
        lblHelp.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryHelp = new HorizontalLayout(lblHelp); 
        lytSummaryHelp.setWidthFull();
        Label lblHintControlPoints = new Label(ts.getTranslatedString("module.mpls.hint-create-delete-control-point"));
        lblHintControlPoints.setClassName("hintMplsView");           
        VerticalLayout lytFooterView = new VerticalLayout(lblHintControlPoints);
        setMarginPaddingLayout(lytFooterView, false);
        lytFooterView.setSpacing(false);
        AccordionPanel apHelp = new AccordionPanel(lytSummaryHelp, lytFooterView);
        accordion.add(apHelp);
        
        Label lblContext = new Label(ts.getTranslatedString("module.mpls.context"));
        lblContext.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryContext = new HorizontalLayout(lblContext); 
        lytSummaryContext.setWidthFull();      
        VerticalLayout lytContext = new VerticalLayout();
        setMarginPaddingLayout(lytContext, false);
        lytContext.setSpacing(false);
        AccordionPanel apContext = new AccordionPanel(lytSummaryContext, lytContext);
        accordion.add(apContext);
        accordion.close();
        accordion.open(apObjectProp);
        
        VerticalLayout lytSheet = new VerticalLayout(accordion);
        lytSheet.setSpacing(false);
        setMarginPaddingLayout(lytSheet, false);
        lytSheet.setWidth("30%");
        lytSheet.addClassName("overflow-y-scroll");

        HorizontalLayout lytMain = new HorizontalLayout(lytSheet, lytDashboard);
        lytMain.setSizeFull();
        setMarginPaddingLayout(lytMain, false);
                     
        addAndExpand(lytMain);
        setSizeFull();
        configureEdgeCreation();
    }
    private static final int ARROWS_KEY_DELTA = 1;

    private void setMarginPaddingLayout(ThemableLayout lytViewInfo, boolean enable) {
        lytViewInfo.setMargin(enable);
        lytViewInfo.setPadding(enable);
    }
    
    private void configureEdgeCreation() {
        mplsView.getMxgraphCanvas().getMxGraph().addEdgeCompleteListener(evt -> {
            selectedSourceEquipment = ((BusinessObjectViewNode) mplsView.getAsViewMap().findNode(evt.getSourceId())).getIdentifier();
            selectedTargetEquipment = ((BusinessObjectViewNode) mplsView.getAsViewMap().findNode(evt.getTargetId())).getIdentifier();          
            openNewConnectionDialog();
        });
    }
    
    /**
     * resets the mpls view instance and creates a empty one
     */
    public void resetDashboard() {
        mplsView.clean();
        if (currentView != null)
            mplsView.buildFromSavedView(currentView.getStructure());
    }
    
    /**
     * Create and open the dialog form to create a new Connection
     */
    private void openNewConnectionDialog() {

        selectedEndPointA = null;
        selectedEndPointB = null;
        ConfirmDialog dlgConnection = new ConfirmDialog(ts, ts.getTranslatedString("module.visualization.object-view-new-connection"), ts.getTranslatedString("module.mpls.create-connection"));
        TextField txtConnectionName = new TextField(ts.getTranslatedString("module.mpls.connection-name"));
        txtConnectionName.setWidthFull();
        ComboBox<BusinessObjectLight> cbxSourceObject = new ComboBox<>(ts.getTranslatedString("module.mpls.source-equipment"));                                 
        ComboBox<BusinessObjectLight>  cbxTargetObject = new ComboBox<>(ts.getTranslatedString("module.mpls.target-equipment"));
        cbxSourceObject.setWidthFull();
    
        cbxSourceObject.setAllowCustomValue(false);
        cbxSourceObject.setClearButtonVisible(true);
        cbxSourceObject.setItems(mplsView.getMxgraphCanvas().getNodes().keySet());
        cbxSourceObject.setValue(selectedSourceEquipment);
        
        cbxTargetObject.setWidthFull();    
        cbxTargetObject.setAllowCustomValue(false);
        cbxTargetObject.setClearButtonVisible(true);
        cbxTargetObject.setItems(mplsView.getMxgraphCanvas().getNodes().keySet());
        cbxTargetObject.setValue(selectedTargetEquipment);
        
        HierarchicalDataProvider dataProviderSourceTree = buildHierarchicalDataProvider(new BusinessObjectLight("", "", ""));
        HierarchicalDataProvider dataProviderTargetTree = buildHierarchicalDataProvider(new BusinessObjectLight("", "", ""));
        TreeGrid<InventoryObjectNode> sourceTree = new NavigationTree(dataProviderSourceTree , new BasicTreeNodeIconGenerator(resourceFactory));
        TreeGrid<InventoryObjectNode> targetTree = new NavigationTree(dataProviderTargetTree , new BasicTreeNodeIconGenerator(resourceFactory));
        targetTree.setHeightByRows(true);
        sourceTree.setVisible(false);
        targetTree.setVisible(false);
        if (selectedSourceEquipment != null) {
            sourceTree.setVisible(true);
            sourceTree.setDataProvider(buildHierarchicalDataProvider(selectedSourceEquipment));
        }
        if (selectedTargetEquipment != null) {
            targetTree.setVisible(true);
            targetTree.setDataProvider(buildHierarchicalDataProvider(selectedTargetEquipment));
        }
        
        cbxSourceObject.addValueChangeListener(listener -> {
            if (listener.getValue() != null && listener.getValue().equals(selectedTargetEquipment)) {
                cbxSourceObject.setValue(null);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-different-devices"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                return;
            }
            selectedSourceEquipment = listener.getValue();
            selectedEndPointA = null;
            sourceTree.setVisible(true);
            sourceTree.setDataProvider(buildHierarchicalDataProvider(listener.getValue() == null ? new BusinessObjectLight("", "", "") : listener.getValue()));
        });
        cbxTargetObject.addValueChangeListener(listener -> {
            if (listener.getValue() != null && listener.getValue().equals(selectedSourceEquipment)) {
                cbxTargetObject.setValue(null);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-different-devices"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                return;
            }
            selectedTargetEquipment = listener.getValue();
            selectedEndPointB = null;
            targetTree.setVisible(true);
            targetTree.setDataProvider(buildHierarchicalDataProvider(listener.getValue() == null ? new BusinessObjectLight("", "", "") : listener.getValue()));
        });

        sourceTree.addItemClickListener( item -> {
             selectedEndPointA = item.getItem().getObject();
        });
        targetTree.addItemClickListener( item -> {
             selectedEndPointB = item.getItem().getObject();
        });
        
        HorizontalLayout lytTrees = new HorizontalLayout(new VerticalLayout(new H5(ts.getTranslatedString("module.mpls.source-end-point")), sourceTree) 
                                 , new VerticalLayout(new H5(ts.getTranslatedString("module.mpls.target-end-point")),targetTree));
        lytTrees.setSizeFull();
              
        dlgConnection.getBtnConfirm().addClickListener(evt -> {
            
            if (selectedEndPointA == null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-end-point-a"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                return;
            }
            if (selectedEndPointB == null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-select-end-point-b"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                return;
            }
            try {
                if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointA.getClassName())) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-be-genericport-endpointA"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                    return;
                }

                if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, selectedEndPointB.getClassName())) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.mpls.must-be-genericport-endpointB"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                    return;
                }

                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(Constants.PROPERTY_NAME, txtConnectionName.getValue());
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                String newTransportLink = mplsService.createMPLSLink(selectedEndPointA.getClassName(), selectedEndPointA.getId(),
                        selectedEndPointB.getClassName(), selectedEndPointB.getId(), attributes, session.getUser().getUserName());
                if (newTransportLink == null) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.mpls.actions.mpls-link-error-creating"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                } else {
                    BusinessObjectLight mplsLink = bem.getObject(Constants.CLASS_MPLSLINK, newTransportLink);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.actions.mpls-link-created"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                    mplsView.getMxgraphCanvas().addEdge(mplsLink, mplsLink.getId(), selectedSourceEquipment, selectedTargetEquipment, null, selectedEndPointA.getName(), selectedEndPointB.getName());
                    mplsView.syncViewMap();
                    dlgConnection.close();
                }
            } catch (InvalidArgumentException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            } 
            
        });
        
        VerticalLayout lytForm = new VerticalLayout(txtConnectionName, cbxSourceObject, cbxTargetObject);
        //lytForm.setWidth("500px");
        VerticalLayout lytContent = new VerticalLayout(lytForm, lytTrees);
        lytContent.setWidthFull();
        lytContent.setSpacing(false);
        lytContent.setPadding(false);
        
        dlgConnection.setContent(lytContent);
        dlgConnection.setMinWidth("80%");
        dlgConnection.open();
    }  
  
    /**
     * Function that creates a new HierarchicalDataProvider for a tree grid.
     * @param root the main toot of the tree
     * @return the new data Provider the the given root
     */
    public HierarchicalDataProvider buildHierarchicalDataProvider(BusinessObjectLight root) {
        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {
            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    if (parent.getObject().getClassName() == null || parent.getObject().getClassName().isEmpty()) 
                        return new ArrayList().stream();
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(object.getClassName(), object.getId(), -1);
                        List<InventoryObjectNode> theChildren = new ArrayList();
                        children.forEach(child -> {
                            theChildren.add(new InventoryObjectNode(child));
                        });
                        return theChildren.stream();
                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        Notification.show(ex.getMessage());
                        return new ArrayList().stream();
                    } 
                } else {   
                       
                        return Arrays.asList(new InventoryObjectNode(root)).stream();                   
                }              
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    try {
                        BusinessObjectLight object = parent.getObject();
                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId(), null);
                    } catch (InvalidArgumentException ex) {
                        Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
                        return 0;
                    }
                    
                } else
                    return 1;
            }

            @Override
            public boolean hasChildren(InventoryObjectNode node) {
                return true;
            }
        };
    }
      
    /**
     * Save the current view in the canvas
     */
    private void saveCurrentView() {
        try {
            if (currentView != null) {
                aem.updateGeneralView(currentView.getId(), currentView.getName(), currentView.getDescription(), mplsView.getAsXml(), null);
                currentView.setStructure(mplsView.getAsXml());
            }
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
          
    }
    
    /**
     * Creates a confirm dialog to ask for the remove object action
     */
    private void openConfirmDialogDeleteObject() {
        ConfirmDialog dlgConfirmDelete = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                ts.getTranslatedString("module.mpls.delete-permanently-message"),
                ts.getTranslatedString("module.general.messages.ok"));
        dlgConfirmDelete.open();
        dlgConfirmDelete.getBtnConfirm().addClickListener(evt -> {
            deleteSelectedObject(true);
//            saveCurrentView();
            dlgConfirmDelete.close();           
        });
    }

    /**
     * removes the selected object in the view
     * @param deletePermanently Boolean that if true specifies that the object
     * is permanently deleted or false if it was only deleted from the view. 
     */
    private void deleteSelectedObject(boolean deletePermanently) {            
        if( selectedObject != null) {
            try {
                if (MxGraphCell.PROPERTY_VERTEX.equals(mplsView.getMxgraphCanvas().getSelectedCellType())) {
                    if (deletePermanently)
                        bem.deleteObject(selectedObject.getClassName(), selectedObject.getId(), false);
                    mplsView.removeNode(selectedObject);                                 
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.object-deleted"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                } else {
                    if (deletePermanently) {
                        Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                        mplsService.deleteMPLSLink(selectedObject.getId(), true, session.getUser().getUserName());       
                    }
                    mplsView.removeEdge(selectedObject);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.mpls-link-deleted"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                }
                if (deletePermanently)
                        saveCurrentView();     
                selectedObject = null;              
                updatePropertySheetObjects();
            } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }

    /**
     * detects the relationships of the nodes that are currently in the view, 
     * if they have MPLS Links that are not in the view then they are added 
     * with the corresponding end points
     */
    private void detectRelationships() {
            List<BusinessObjectLight> mplsLinksAdded = new ArrayList<>();
            List<BusinessObjectLight> nodesAdded = new ArrayList<>();
        try {
                            
            Properties props;
            List<BusinessObjectLight> viewNodes = new ArrayList(mplsView.getMxgraphCanvas().getNodes().keySet());
            for (BusinessObjectLight node : viewNodes) {
                
                List<AnnotatedBusinessObjectLight> objectMplsLinks = bem.getAnnotatedSpecialAttribute(node.getClassName(), node.getId(), MplsService.RELATIONSHIP_MPLSLINK);
                
                for (AnnotatedBusinessObjectLight link : objectMplsLinks) {
                    if (mplsView.getMxgraphCanvas().getEdges().containsKey(link.getObject()))
                        continue;
                    MplsConnectionDefinition connectionDetails = mplsService.getMPLSLinkDetails(link.getObject().getId());                
                                    
                    BusinessObjectLight theOtherEndPoint;
                    
                    if (node.equals(connectionDetails.getDeviceA()))
                        theOtherEndPoint = connectionDetails.getDeviceB();
                    else
                        theOtherEndPoint = connectionDetails.getDeviceA();
                    
                    if (theOtherEndPoint == null) // if the other object is null then omite the edge
                        continue;
                    
                    if (!mplsView.getMxgraphCanvas().getNodes().containsKey(theOtherEndPoint)) {                        
                        addNodeToView(theOtherEndPoint, 100 , 50);
                        nodesAdded.add(theOtherEndPoint);                              
                    }
                
                    addEdgeToView(connectionDetails);                        
                    mplsLinksAdded.add(link.getObject());
               }
            }
            if (nodesAdded.size() > 0)    
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), nodesAdded.size() == 1 ?
                            String.format("%s %s", mplsLinksAdded.size(),ts.getTranslatedString("module.mpls.mpls-object-added"))
                            : String.format("%s %s", mplsLinksAdded.size(),ts.getTranslatedString("module.mpls.mpls-objects-added")), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            if (mplsLinksAdded.size() > 0)    
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), 
                          mplsLinksAdded.size() == 1  ? String.format("%s %s", mplsLinksAdded.size(),ts.getTranslatedString("module.mpls.mpls-link-added"))
                            : String.format("%s %s", mplsLinksAdded.size(),ts.getTranslatedString("module.mpls.mpls-links-added")), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            else 
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.no-link-found"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
        }
        catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initializeTblViews() {        
        loadViews();
        tblViews = new Grid<>();
        ListDataProvider<ViewObjectLight> dataProvider = new ListDataProvider<>(mplsViews);
        tblViews.setDataProvider(dataProvider);
        tblViews.addColumn(ViewObjectLight::getName).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblViews.addItemClickListener(listener -> {
            openningView = true; // added to ignore change events while we are opening the view
            openMplsView(listener.getItem());
            mplsTools.setView(listener.getItem());
            MxGraphNode dummyNode = new MxGraphNode(); 
            dummyNode.addCellAddedListener(eventListener ->  {
                openningView = false;
                mplsView.getMxgraphCanvas().getMxGraph().removeNode(dummyNode);
            });
            mplsView.getMxgraphCanvas().getMxGraph().addNode(dummyNode);
        });
        HeaderRow filterRow = tblViews.appendHeaderRow();
        
        TextField txtViewNameFilter = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtViewNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtViewNameFilter.setWidthFull();
        txtViewNameFilter.addValueChangeListener(event -> dataProvider.addFilter(
        project -> StringUtils.containsIgnoreCase(project.getName(),
                txtViewNameFilter.getValue())));
        
        filterRow.getCell(tblViews.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtViewNameFilter);
        
    }

    /**
     * loads the given mpls view into the view
     * @param item the mpls view to be loaded
     */
    private void openMplsView(ViewObjectLight item) {
        try {
            ViewObject view = aem.getGeneralView(item.getId());
            setCurrentView(view);
            if (wdwMPLSViews != null)
                this.wdwMPLSViews.close();
            this.mplsTools.setGeneralToolsEnabled(true);
            selectedObject = null;
            updatePropertySheetObjects();
            updatePropertySheetView();
        } catch (ApplicationObjectNotFoundException ex) {
            Logger.getLogger(MplsManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadViews() {
        try {
            mplsViews = aem.getGeneralViews(MplsService.VIEW_CLASS,-1);             
        } catch (InvalidArgumentException | NotAuthorizedException ex) {
            Logger.getLogger(MplsManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialize the general actions that provides the functionalty to create 
     * and remove mpls views 
     */
    private void initializeActions() {
        listenerDeleteAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.getDataProvider().refreshAll();
            tblViews.setItems(mplsViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            mplsTools.setGeneralToolsEnabled(false);
            mplsTools.setSelectionToolsEnabled(false);         
            setCurrentView(null);
            selectedObject = null;
            updatePropertySheetObjects();
            updatePropertySheetView();
        };
        this.deleteMPLSViewVisualAction.registerActionCompletedLister(listenerDeleteAction);
        
        listenerNewViewAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.getDataProvider().refreshAll();
            tblViews.setItems(mplsViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            mplsTools.setGeneralToolsEnabled(true);
            if (wdwMPLSViews != null)
                wdwMPLSViews.close();
            selectedObject = null;
            updatePropertySheetObjects();
            
            ActionResponse response = ev.getActionResponse();
            try {
                ViewObject newView = aem.getGeneralView((long) response.get("viewId"));
                setCurrentView(newView);
                updatePropertySheetView();
                          
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(MplsManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
        this.newMPLSViewVisualAction.registerActionCompletedLister(listenerNewViewAction);        
    }

    /**
     * open the dialog that shows the list of available MPLS views.
     */
    private void openListMplsViewDialog() {
        if (mplsViews.size() > 0) {
            wdwMPLSViews = new Dialog();

            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> {
                wdwMPLSViews.close();
            });      
            VerticalLayout lytContent = new VerticalLayout(tblViews, btnCancel);
            lytContent.setAlignItems(Alignment.CENTER);
            wdwMPLSViews.add(lytContent);
            wdwMPLSViews.setWidth("600px");
            wdwMPLSViews.open();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), ts.getTranslatedString("module.mpls.no-views-created"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
    }
  
    private void updatePropertySheetObjects() {
        try {        
            if (selectedObject != null) {
                BusinessObject aWholeListTypeItem = bem.getObject(selectedObject.getClassName(), selectedObject.getId());
                propSheetObjects.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeListTypeItem, ts, aem, mem));
            } else 
                propSheetObjects.clear();
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updatePropertySheetView() {
        if (currentView != null) {
            ArrayList<AbstractProperty> viewProperties = new ArrayList<>();
            viewProperties.add(new StringProperty(Constants.PROPERTY_NAME,
                    Constants.PROPERTY_NAME, "", currentView.getName(), ts));
            viewProperties.add(new StringProperty(Constants.PROPERTY_DESCRIPTION,
                    Constants.PROPERTY_DESCRIPTION, "", currentView.getDescription(), ts));
            propSheetMPLS.setItems(viewProperties);
        } else
            propSheetMPLS.clear();
    }
    /**
     * add a single node to the mpls view
     * @param node the node to be added
     */
    private void addNodeToView(BusinessObjectLight node, int x, int y) {
        if (mplsView.getAsViewMap().findNode(node) == null) {
            String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(node.getClassName())).toString();
            Properties props = new Properties();
            props.put("imageUrl", uri);
            props.put("x", x);
            props.put("y", y);
            mplsView.addNode(node, props);                         
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.object-already-included"), 
                            AbstractNotification.NotificationType.INFO, ts).open();                         
    }
    
    /**
     * adds and edge with his nodes o the mpls view
     * @param connection The link definition
     */
    private void addEdgeToView(MplsConnectionDefinition connection) {
        if (mplsView.getAsViewMap().findEdge(connection.getConnectionObject()) == null) {
            Properties props = new Properties();
            props.put("controlPoints", new ArrayList());
            props.put("sourceLabel", connection.getEndpointA() == null ? "" : connection.getEndpointA().getName());
            props.put("targetLabel", connection.getEndpointB() == null ? "" : connection.getEndpointB().getName());
            mplsView.addEdge(connection.getConnectionObject(), connection.getDeviceA(), connection.getDeviceB(), props);                            
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.mpls.edge-already-included"), 
                            AbstractNotification.NotificationType.INFO, ts).open();                   
        
    }
        
}
