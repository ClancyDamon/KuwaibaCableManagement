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

package com.neotropic.kuwaiba.modules.commercial.sdh.widgets;

import com.neotropic.flow.component.mxgraph.MxGraphBindedKeyEvent;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import static com.neotropic.kuwaiba.modules.commercial.sdh.SdhModule.RELATIONSHIP_SDHTLENDPOINTA;
import static com.neotropic.kuwaiba.modules.commercial.sdh.SdhModule.RELATIONSHIP_SDHTLENDPOINTB;
import com.neotropic.kuwaiba.modules.commercial.sdh.SdhService;
import com.neotropic.kuwaiba.modules.commercial.sdh.SdhView;
import com.neotropic.kuwaiba.modules.commercial.sdh.actions.DeleteSdhViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.sdh.actions.NewSdhViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.sdh.tools.SdhTools;
import com.neotropic.kuwaiba.modules.commercial.sdh.wizard.NewSDHContainerLinkWizard;
import com.neotropic.kuwaiba.modules.commercial.sdh.wizard.NewSDHTransportLinkWizard;
import com.neotropic.kuwaiba.modules.commercial.sdh.wizard.NewSDHTributaryLinkWizard;
import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
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
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
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
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.StringProperty;
import org.neotropic.util.visual.wizard.Wizard;

/**
 * SDH Main Dashboard.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
public class SdhDashboard extends VerticalLayout {
    
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
     * listener to remove Sdh view action
     */
    private ActionCompletedListener listenerDeleteAction;
    /**
     * listener to add new view Action
     */
    private ActionCompletedListener listenerNewViewAction;
    /**
     * reference of the visual action to remove a Sdh view
     */
    private final DeleteSdhViewVisualAction deleteSdhViewVisualAction;
    /**
     * reference of the visual action to add a Sdh view
     */
    private final NewSdhViewVisualAction newSdhViewVisualAction;
    /**
     * factory to instance object icons
     */
    private final ResourceFactory resourceFactory;
    /**
     * service to persistence actions
     */
    private final SdhService sdhService;
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
     * canvas toolbar
     */
    private SdhTools sdhTools;
    /**
     * Instance of the main canvas view
     */
    private SdhView sdhView;
    /**
     * list of sdh views
     */
    private List<ViewObjectLight> sdhViews;
    /**
     * Reference to the grid that shows the sdh views 
     */
    private Grid<ViewObjectLight> tblViews;
    /**
     * Dialog that lists the whole list of the views
     */
    private Dialog wdwSdhViews;
    /**
     * reference to the current selected object in the canvas
     */
    private BusinessObjectLight selectedObject;
    /**
     *  property sheet instance for canvas objects
     */
    private PropertySheet propSheetObjects;
     /**
     * main property sheet instance for sdh properties
     */
    private PropertySheet propSheetSdh;
    
    private boolean openningView;
    
    
    public final String CONNECTION_TRANSPORTLINK;
    
    public final String CONNECTION_CONTAINERLINK;
    
    public final String CONNECTION_TRIBUTARYLINK;
    


    public ViewObject getCurrentView() {
        return currentView;
    }

    public void setCurrentView(ViewObject currentView) {
        this.currentView = currentView;
        resetDashboard();
    }

    public SdhTools getSdhTools() {
        return sdhTools;
    }

    public void setSdhTools(SdhTools sdhTools) {
        this.sdhTools = sdhTools;
    }  
    
    public SdhDashboard(TranslationService ts, MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, 
            ResourceFactory resourceFactory,SdhService sdhService, DeleteSdhViewVisualAction deleteSDHViewVisualAction, 
            NewSdhViewVisualAction newSDHViewVisualAction) {
        super();
        this.ts = ts;
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.resourceFactory = resourceFactory;
        this.sdhService = sdhService;
        this.newSdhViewVisualAction = newSDHViewVisualAction;
        this.deleteSdhViewVisualAction = deleteSDHViewVisualAction;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        this.openningView = false;
        CONNECTION_TRANSPORTLINK = ts.getTranslatedString("module.sdh.transport-link-label");
        CONNECTION_CONTAINERLINK = ts.getTranslatedString("module.sdh.container-link-label");
        CONNECTION_TRIBUTARYLINK = ts.getTranslatedString("module.sdh.tributary-link-label");
    }        

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent); 
        createContent();
    }
   
    @Override
    public void onDetach(DetachEvent ev) {
        this.deleteSdhViewVisualAction.unregisterListener(listenerDeleteAction);
        this.newSdhViewVisualAction.unregisterListener(listenerNewViewAction);
        sdhView.getMxgraphCanvas().getMxGraph().removeListeners();
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
      
        sdhView = new SdhView(mem, aem, bem, ts, resourceFactory);   
        sdhView.getMxgraphCanvas().setComObjectSelected(() -> {
            
            String objectId = sdhView.getMxgraphCanvas().getSelectedCellId();
            if (MxGraphCell.PROPERTY_VERTEX.equals(sdhView.getMxgraphCanvas().getSelectedCellType())){
                 selectedObject = ((BusinessObjectViewNode) sdhView.getAsViewMap().findNode(objectId)).getIdentifier();
            } else {
                 selectedObject = ((BusinessObjectViewEdge) sdhView.getAsViewMap().findEdge(objectId)).getIdentifier();            
            }
            updatePropertySheetObjects();
            sdhTools.setGeneralToolsEnabled(true);
            sdhTools.setSelectionToolsEnabled(true);
        });
        sdhView.getMxgraphCanvas().setComObjectUnselected(() -> {
            selectedObject = null;
            updatePropertySheetObjects();
            sdhTools.setSelectionToolsEnabled(false);
        });
        sdhView.getMxgraphCanvas().setComObjectDeleted(() -> {
            openConfirmDialogDeleteObject();   
        }); 
        sdhView.getMxgraphCanvas().getMxGraph().addGraphChangedListener(eventListener -> {
            if (!openningView)
                saveCurrentView();
        });
        sdhView.getMxgraphCanvas().getMxGraph().addGraphLoadedListener(eventListener -> {
            sdhView.getMxgraphCanvas().getMxGraph().bindKey(37, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(37 + "")) {
                    MxGraphNode node = sdhView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                    node.setX(node.getX() - ARROWS_KEY_DELTA);
                    saveCurrentView();
                }
            });
            sdhView.getMxgraphCanvas().getMxGraph().bindKey(39, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(39 + "")) {
                    MxGraphNode node = sdhView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                    node.setX(node.getX() + ARROWS_KEY_DELTA);
                    saveCurrentView();
                }
            });
            sdhView.getMxgraphCanvas().getMxGraph().bindKey(38, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(38 + "")) {
                    MxGraphNode node = sdhView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                    node.setY(node.getY() - ARROWS_KEY_DELTA);
                    saveCurrentView();
                }
            });
            sdhView.getMxgraphCanvas().getMxGraph().bindKey(40, (MxGraphBindedKeyEvent t) -> {
                if (selectedObject != null && t.getKey().equals(40 + "")) {
                    MxGraphNode node = sdhView.getMxgraphCanvas().findMxGraphNode(selectedObject);
                    node.setY(node.getY() + ARROWS_KEY_DELTA);
                    saveCurrentView();
                }
            });
        });
       
        sdhTools = new SdhTools(sdhView, bem, ts);
        
        sdhTools.getBtnOpenView().addClickListener(ev -> {
             openListSDHViewDialog();
        });
        sdhTools.getBtnNewView().addClickListener(ev -> {
             this.newSdhViewVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });
        sdhTools.getBtnRemoveView().addClickListener(evt -> {
            if (currentView != null)
                this.deleteSdhViewVisualAction.getVisualComponent(new ModuleActionParameterSet( new ModuleActionParameter("viewId", currentView.getId()))).open();
        });
        
        sdhTools.addNewObjectListener(event -> {   
             BusinessObjectLight tmpObject = event.getObject();
             if (tmpObject == null)
                 return;
             try {
                 
                if(mem.isSubclassOf("GenericSDHTransportLink", tmpObject.getClassName())) {
                    
                   HashMap<String, List<BusinessObjectLight>> devices = bem.getSpecialAttributes(tmpObject.getClassName(), tmpObject.getId(), 
                           RELATIONSHIP_SDHTLENDPOINTA, RELATIONSHIP_SDHTLENDPOINTB);
                    BusinessObject communicationsEquipmentA = null, communicationsEquipmentB = null;
                    if (devices.containsKey(RELATIONSHIP_SDHTLENDPOINTA) && devices.get(RELATIONSHIP_SDHTLENDPOINTA).size() > 0) {
                        BusinessObjectLight sideA = devices.get(RELATIONSHIP_SDHTLENDPOINTA).get(0);
                        communicationsEquipmentA = bem.getFirstParentOfClass(sideA.getClassName(), sideA.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                        if (communicationsEquipmentA == null) {
                            throw new BusinessObjectNotFoundException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", sideA.getClassName(), sideA.getId()));
                        }
                    }
                     if (devices.containsKey(RELATIONSHIP_SDHTLENDPOINTB) && devices.get(RELATIONSHIP_SDHTLENDPOINTB).size() > 0) {
                        BusinessObjectLight sideB = devices.get(RELATIONSHIP_SDHTLENDPOINTB).get(0);

                        communicationsEquipmentB = bem.getFirstParentOfClass(sideB.getClassName(), sideB.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                        if (communicationsEquipmentB == null) {
                            throw new BusinessObjectNotFoundException(String.format("The specified port (%s : %s) doesn't seem to be located in a communications equipment", sideB.getClassName(), sideB.getId()));
                        }
                    }
                    if (communicationsEquipmentA != null && communicationsEquipmentB != null) {
                    addNodeToView(communicationsEquipmentA, 100, 50);
                    addNodeToView(communicationsEquipmentB, 400, 50);
                    addEdgeToView(tmpObject, communicationsEquipmentA, communicationsEquipmentB);
                    }               
                } else
                    addNodeToView(tmpObject, 100, 50);               
             } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        sdhTools.addSelectObjectListener(event -> {   
             BusinessObjectLight tmpObject = event.getObject();
             if (tmpObject == null)
                 return;
             MxGraphCell cell;
             if(tmpObject.getClassName().equals(Constants.CLASS_MPLSLINK))
                 cell = sdhView.getMxgraphCanvas().getEdges().get(tmpObject);
             else
                 cell = sdhView.getMxgraphCanvas().getNodes().get(tmpObject);
             if (cell != null)
                 cell.selectCell();

        });
        
        sdhTools.addNewConnectionListener(event -> {   
            selectedSourceEquipment = null;
            selectedTargetEquipment = null;
            openDlgSelectConnectionType();
        });
        sdhTools.addSaveViewListener(event -> {
            saveCurrentView();
        });
        sdhTools.addDeleteObjectListener(event -> {
           deleteSelectedObject(false); 
        });
        sdhTools.addDeleteObjectPermanentlyObjectListener(event -> {
            openConfirmDialogDeleteObject();
        });

        sdhTools.setGeneralToolsEnabled(false);
              
        initializeActions();
        initializeTblViews();
                   
        VerticalLayout lytDashboard = new VerticalLayout(sdhTools, sdhView.getAsUiElement());
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

                        //special case when the name is updated the label must be refreshed in the sdhView.getMxgraphCanvas()
                        if (property.getName().equals(Constants.PROPERTY_NAME)) {
                            if (MxGraphCell.PROPERTY_VERTEX.equals(sdhView.getMxgraphCanvas().getSelectedCellType())) {
                                sdhView.getMxgraphCanvas().getNodes().get(selectedObject).setLabel((String) property.getValue());
                            } else {
                                sdhView.getMxgraphCanvas().getEdges().get(selectedObject).setLabel((String) property.getValue());
                            }
                            sdhView.getMxgraphCanvas().getMxGraph().refreshGraph();
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
        
        PropertySheet.IPropertyValueChangedListener listenerPropSheetSDH = new PropertySheet.IPropertyValueChangedListener() {
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
        propSheetSdh = new PropertySheet(ts, new ArrayList<>());
        propSheetSdh.addPropertyValueChangedListener(listenerPropSheetSDH);     
        
        Accordion accordion = new Accordion();
        accordion.setWidthFull();
          
        BoldLabel lblViewProperties = new BoldLabel(ts.getTranslatedString("module.mpls.view-properties"));
        lblViewProperties.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryViewProp = new HorizontalLayout(lblViewProperties); 
        lytSummaryViewProp.setWidthFull();       
        AccordionPanel apViewProp = new AccordionPanel(lytSummaryViewProp, propSheetSdh);
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
        sdhView.getMxgraphCanvas().getMxGraph().addEdgeCompleteListener(evt -> {
            selectedSourceEquipment = ((BusinessObjectViewNode) sdhView.getAsViewMap().findNode(evt.getSourceId())).getIdentifier();
            selectedTargetEquipment = ((BusinessObjectViewNode) sdhView.getAsViewMap().findNode(evt.getTargetId())).getIdentifier();          
            openDlgSelectConnectionType();
        });
    }
    
    /**
     * resets the sdh view instance and creates a empty one
     */
    public void resetDashboard() {
        sdhView.clean();
        if (currentView != null)
            sdhView.buildFromSavedView(currentView.getStructure());
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
                        Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
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
     * Save the current view in the canvas.
     */
    private void saveCurrentView() {
        try {
            if (currentView != null) {
                aem.updateGeneralView(currentView.getId(), currentView.getName(), currentView.getDescription(), sdhView.getAsXml(), null);
                currentView.setStructure(sdhView.getAsXml());
            }
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
          
    }
    
    /**
     * Creates a confirm dialog to ask for the remove object action
     */
    private void openConfirmDialogDeleteObject() {
        ConfirmDialog dlgConfirmDelete = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                ts.getTranslatedString("module.sdh.delete-permanently-message"),
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
                if (MxGraphCell.PROPERTY_VERTEX.equals(sdhView.getMxgraphCanvas().getSelectedCellType())) {
                    if (deletePermanently)
                        bem.deleteObject(selectedObject.getClassName(), selectedObject.getId(), false);
                    sdhView.removeNode(selectedObject);                                 
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.sdh.object-deleted"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                } else {
                    if (deletePermanently) 
                        sdhService.deleteSDHTransportLink(selectedObject.getClassName(), selectedObject.getId(), true);       
                    
                    sdhView.removeEdge(selectedObject);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.sdh.sdh-link-deleted"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                }
                if (deletePermanently)
                        saveCurrentView();     
                selectedObject = null;              
                updatePropertySheetObjects();
            } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException | OperationNotPermittedException ex) {
                Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            } catch (InventoryException ex) {
                Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
 
    private void initializeTblViews() {        
        loadViews();
        tblViews = new Grid<>();
        ListDataProvider<ViewObjectLight> dataProvider = new ListDataProvider<>(sdhViews);
        tblViews.setDataProvider(dataProvider);
        tblViews.addColumn(ViewObjectLight::getName).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblViews.addItemClickListener(listener -> {
            openningView = true; // added to ignore change events while we are opening the view
            openSDHView(listener.getItem());
            sdhTools.setView(listener.getItem());
            MxGraphNode dummyNode = new MxGraphNode(); 
            dummyNode.addCellAddedListener(eventListener ->  {
                openningView = false;
                sdhView.getMxgraphCanvas().getMxGraph().removeNode(dummyNode);
            });
            sdhView.getMxgraphCanvas().getMxGraph().addNode(dummyNode);
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
     * Loads the given sdh view into the view.
     * @param item the sdh view to be loaded
     */
    private void openSDHView(ViewObjectLight item) {
        try {
            ViewObject view = aem.getGeneralView(item.getId());
            setCurrentView(view);
            if (wdwSdhViews != null)
                this.wdwSdhViews.close();
            this.sdhTools.setGeneralToolsEnabled(true);
            selectedObject = null;
            updatePropertySheetObjects();
            updatePropertySheetView();
        } catch (ApplicationObjectNotFoundException ex) {
            Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Loads all the sdh Views.
     */
    public void loadViews() {
        try {
            sdhViews = aem.getGeneralViews(SdhService.CLASS_VIEW,-1);             
        } catch (InvalidArgumentException | NotAuthorizedException ex) {
            Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialize the general actions that provides the functionalty to create 
     * and remove sdh views 
     */
    private void initializeActions() {
        listenerDeleteAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.getDataProvider().refreshAll();
            tblViews.setItems(sdhViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            sdhTools.setGeneralToolsEnabled(false);
            sdhTools.setSelectionToolsEnabled(false);
            setCurrentView(null);
            selectedObject = null;
            updatePropertySheetObjects();
            updatePropertySheetView();
        };
        this.deleteSdhViewVisualAction.registerActionCompletedLister(listenerDeleteAction);
        
        listenerNewViewAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.getDataProvider().refreshAll();
            tblViews.setItems(sdhViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            sdhTools.setGeneralToolsEnabled(true);
            if (wdwSdhViews != null)
                wdwSdhViews.close();
            selectedObject = null;
            updatePropertySheetObjects();
            
            ActionResponse response = ev.getActionResponse();
            try {
                ViewObject newView = aem.getGeneralView((long) response.get("viewId"));
                setCurrentView(newView);
                updatePropertySheetView();
                          
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
        this.newSdhViewVisualAction.registerActionCompletedLister(listenerNewViewAction);        
    }

    /**
     * open the dialog that shows the list of available SDH views.
     */
    private void openListSDHViewDialog() {
        if (sdhViews.size() > 0) {
            wdwSdhViews = new Dialog();

            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), ev -> {
                wdwSdhViews.close();
            });      
            VerticalLayout lytContent = new VerticalLayout(tblViews, btnCancel);
            lytContent.setAlignItems(Alignment.CENTER);
            wdwSdhViews.add(lytContent);
            wdwSdhViews.setWidth("600px");
            wdwSdhViews.open();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), ts.getTranslatedString("module.sdh.no-views-created"), 
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
            Logger.getLogger(SdhDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updatePropertySheetView() {
        if (currentView != null) {
            ArrayList<AbstractProperty> viewProperties = new ArrayList<>();
            viewProperties.add(new StringProperty(Constants.PROPERTY_NAME,
                    Constants.PROPERTY_NAME, "", currentView.getName(), ts));
            viewProperties.add(new StringProperty(Constants.PROPERTY_DESCRIPTION,
                    Constants.PROPERTY_DESCRIPTION, "", currentView.getDescription(), ts));
            propSheetSdh.setItems(viewProperties);
        } else
            propSheetSdh.clear();
    }
    /**
     * add a single node to the sdh view
     * @param node the node to be added
     */
    private void addNodeToView(BusinessObjectLight node, int x, int y) {
        if (sdhView.getAsViewMap().findNode(node) == null) {
            String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(node.getClassName())).toString();
            Properties props = new Properties();
            props.put("imageUrl", uri);
            props.put("x", x);
            props.put("y", y);
            sdhView.addNode(node, props);                         
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.sdh.object-already-included"), 
                            AbstractNotification.NotificationType.INFO, ts).open();                         
    }
    
    /**
     * adds and edge with his nodes o the sdh view
     * @param connection The link definition
     */
    private void addEdgeToView(BusinessObjectLight edge, BusinessObjectLight source, BusinessObjectLight target) {
        if (sdhView.getAsViewMap().findEdge(edge) == null) {
            Properties props = new Properties();
            props.put("controlPoints", new ArrayList());
            props.put("sourceLabel", source == null ? "" : source.getName());
            props.put("targetLabel", target == null ? "" : target.getName());
            sdhView.addEdge(edge, source, target, props);                            
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.sdh.edge-already-included"), 
                            AbstractNotification.NotificationType.INFO, ts).open();                   
        
    }
 
    void openDlgSelectConnectionType() {
        ConfirmDialog dlgConnection = new ConfirmDialog(ts, ts.getTranslatedString("Select connection type"), ts.getTranslatedString("module.general.messages.ok"));
        ComboBox<Integer> cbxConnType = new ComboBox<>();
        cbxConnType.setWidth("300px");
        cbxConnType.setItems(Arrays.asList(1, 2, 3));
        cbxConnType.setItemLabelGenerator(item -> {
            switch (item) {
                    case 1:
                        return CONNECTION_TRANSPORTLINK;
                    case 2:
                        return CONNECTION_CONTAINERLINK;
                    default:
                        return CONNECTION_TRIBUTARYLINK;
                }
        });
        dlgConnection.getBtnConfirm().addClickListener(listener -> {
            if (cbxConnType.getValue() != null) {
                Wizard wizard;
                String message, wizardHeader;
                switch (cbxConnType.getValue()) {
                    case 1:
                        wizard = new NewSDHTransportLinkWizard(selectedSourceEquipment,
                        selectedTargetEquipment, bem, mem, resourceFactory, sdhService, ts);
                        message = ts.getTranslatedString("module.sdh.transport-link-created");
                        wizardHeader = String.format("%s %s", ts.getTranslatedString("module.general.labels.create"), CONNECTION_TRANSPORTLINK);
                        break;
                    case 2:
                        wizard = new NewSDHContainerLinkWizard(selectedSourceEquipment,
                        selectedTargetEquipment, mem, resourceFactory, sdhService, ts);
                        message = ts.getTranslatedString("module.sdh.container-link-created");
                        wizardHeader = String.format("%s %s", ts.getTranslatedString("module.general.labels.create"), CONNECTION_CONTAINERLINK);                 
                        break;
                    case 3:
                        wizard = new NewSDHTributaryLinkWizard(selectedSourceEquipment,
                        selectedTargetEquipment, mem, bem, resourceFactory, sdhService, ts);
                        message = ts.getTranslatedString("module.sdh.tributary-link-created");
                        wizardHeader = String.format("%s %s", ts.getTranslatedString("module.general.labels.create"), CONNECTION_TRIBUTARYLINK);                                        
                        break;
                    default: return;
                }
                
                EnhancedDialog dlgWizard = new EnhancedDialog();

                wizard.setSizeFull();
                wizard.addEventListener((wizardEvent) -> {
                    switch (wizardEvent.getType()) {
                        case Wizard.WizardEvent.TYPE_FINAL_STEP:
                            BusinessObjectLight newConnection = (BusinessObjectLight) wizardEvent.getInformation().get("connection");
                            if (cbxConnType.getValue() == 1) {
                                BusinessObjectLight aSide = (BusinessObjectLight) wizardEvent.getInformation().get("equipmentA");
                                BusinessObjectLight bSide = (BusinessObjectLight) wizardEvent.getInformation().get("equipmentB");
                                addEdgeToView(newConnection, aSide, bSide);
                            }
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), String.format(message, newConnection.getName()),
                                    AbstractNotification.NotificationType.INFO, ts).open();
                        case Wizard.WizardEvent.TYPE_CANCEL:
                            dlgWizard.close();
                    }
                });

                dlgWizard.setModal(true);
                dlgWizard.setWidth("70%");
                dlgWizard.setHeader(new BoldLabel(wizardHeader));
                dlgWizard.setContent(wizard.getLytMainContent());
                dlgWizard.setFooter(wizard.getLytButtons());
                dlgConnection.close();
                dlgWizard.open();
            }
        });
        dlgConnection.setContent(cbxConnType);
        dlgConnection.setHeader(ts.getTranslatedString("module.sdh.select-connection-type"));
        dlgConnection.open();
    }
        
}
