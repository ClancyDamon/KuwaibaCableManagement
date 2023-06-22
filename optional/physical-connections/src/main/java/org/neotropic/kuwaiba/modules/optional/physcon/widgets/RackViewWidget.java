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
package org.neotropic.kuwaiba.modules.optional.physcon.widgets;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractObjectRelatedViewWidget;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.views.RackView;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.views.util.UtilHtml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import java.util.Collections;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.PortSummaryVisualAction;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.togglemenubutton.ToogleMenuButton;

/**
 * Shows the object view of the given business Object
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class RackViewWidget extends AbstractObjectRelatedViewWidget<VerticalLayout> {

    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the service in charge of creating and manipulating physical
     * connections.
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionsService;

    @Autowired
    private PhysicalPathViewWidget physicalPathViewWidget;
    
    @Autowired
    private PortSummaryVisualAction portSummaryVisualAction;

    private MxGraph simpleMxGraphView;

    private MxGraph detailedMxGraphView;
    
    @Override
    public String appliesTo() {
        return "Rack";
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.visualization.rack-view-name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.visualization.rack-view-description");
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }
    
    @Override
    public String getTitle() {
        return ts.getTranslatedString("module.navigation.widgets.object-dashboard.rack-view");
    }

    @Override
    public VerticalLayout build(BusinessObjectLight businessObject) throws InventoryException {
        try {
            RackView rackView = new RackView(businessObject, false, bem, aem, mem, ts, portSummaryVisualAction);
            RackView detailedRackView = new RackView(businessObject, true, bem, aem, mem, ts, portSummaryVisualAction);
            simpleMxGraphView = rackView.getAsUiElement();
            simpleMxGraphView.setId("simpleMxGraphView");
            detailedMxGraphView = null;
            HorizontalLayout lytRackView = new HorizontalLayout(simpleMxGraphView);

            lytRackView.setMargin(false);
            lytRackView.setPadding(false);
            lytRackView.setSpacing(false);
            lytRackView.setWidthFull();
            HorizontalLayout lytDetailedRackView = new HorizontalLayout();
            lytDetailedRackView.setMargin(false);
            lytDetailedRackView.setPadding(false);
            lytDetailedRackView.setSpacing(false);
            lytDetailedRackView.setVisible(false);
            lytDetailedRackView.setWidthFull();

            Button btnPortDetails = new Button(new Icon(VaadinIcon.CLUSTER), evt -> {
                openDlgPortDetails(businessObject);
            });
            btnPortDetails.setClassName("icon-button");
            btnPortDetails.getElement().setProperty("title", ts.getTranslatedString("module.visualization.rack-view-port-details-title"));
            
            BusinessObject rack = bem.getObject(businessObject.getClassName(), businessObject.getId());
            Button btnShowConnectionsInView = new Button(new Icon(VaadinIcon.CONNECT_O), evt -> {
                showConnectionsInView(rack, detailedRackView.getMapNamedObjects());
            });
            btnShowConnectionsInView.getElement().setProperty("title", ts.getTranslatedString("module.visualization.rack-view-show-wiring-title"));
            btnShowConnectionsInView.setEnabled(false);
            btnShowConnectionsInView.setClassName("icon-button");
            RadioButtonGroup<String> btnRackViewType = new RadioButtonGroup<>();
            btnRackViewType.setItems(ts.getTranslatedString("module.visualization.object-view-show-simple-rack-view"), ts.getTranslatedString("module.visualization.object-view-show-detailed-rack-view"));
            btnRackViewType.setValue(ts.getTranslatedString("module.visualization.object-view-show-simple-rack-view"));
            btnRackViewType.addValueChangeListener((event) -> {
                try {
                    if (btnRackViewType.getValue().equals(ts.getTranslatedString("module.visualization.object-view-show-simple-rack-view"))) { // Simple view
                        lytRackView.setVisible(true);
                        lytDetailedRackView.setVisible(false);
                        btnShowConnectionsInView.setEnabled(false);
                    } else { // Detailed view
                        if (detailedMxGraphView == null) { // If the detailed view hasn't been generated yet, do it, otherwise reuse the existing instance
                            detailedMxGraphView = detailedRackView.getAsUiElement();
                            detailedMxGraphView.setId("detailedMxGraphView");
                            detailedMxGraphView.addEdgeCompleteListener(evt -> {
                                BusinessObjectLight nodeSideA = detailedRackView.getMapNamedObjects().keySet().stream().filter(item -> item.getId().equals(evt.getSourceId())).findAny().get();
                                BusinessObjectLight nodeSideB = detailedRackView.getMapNamedObjects().keySet().stream().filter(item -> item.getId().equals(evt.getTargetId())).findAny().get();
                                if (nodeSideA != null && nodeSideB != null) {
                                    openCreateConnectionDlg(nodeSideA, nodeSideB, detailedRackView.getMapNamedObjects());
                                }
                            });
                            lytDetailedRackView.add(detailedMxGraphView);
                        }
                        btnShowConnectionsInView.setEnabled(true);
                        lytRackView.setVisible(false);
                        lytDetailedRackView.setVisible(true);
                    }
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts)
                            .open();
                    Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            Button btnConnections = new Button(new Icon(VaadinIcon.LINES_LIST), evt -> {
                try {
                    openExploreConnectionsDlg(rack);
                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            btnConnections.setClassName("icon-button");
            btnConnections.getElement().setProperty("title", ts.getTranslatedString("module.visualization.rack-view-show-connections-title"));
            Button btnEditRack = new Button(new Icon(VaadinIcon.EDIT), evt -> {
                try {
                    PropertySheet propertySheet = new PropertySheet(ts);
                    BusinessObject theRack = bem.getObject(businessObject.getClassName(), businessObject.getId());
                    propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(theRack, ts, aem, mem));
                    propertySheet.addPropertyValueChangedListener(new PropertySheet.IPropertyValueChangedListener() {
                        @Override
                        public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
                            try {
                                HashMap<String, String> attributes = new HashMap<>();
                                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                                bem.updateObject(businessObject.getClassName(), businessObject.getId(), attributes);

                                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                                        AbstractNotification.NotificationType.INFO, ts).open();

                            } catch (InventoryException ex) {
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ex.getLocalizedMessage(),
                                        AbstractNotification.NotificationType.INFO, ts).open();
                                propertySheet.undoLastEdit();
                            }
                        }
                    });

                    Dialog dlgPropSheet = new Dialog();
                    dlgPropSheet.setWidth("700px");
                    Button btnClosePropSheet = new Button(ts.getTranslatedString("module.general.messages.close"), evtPropSheet -> {
                        dlgPropSheet.close();
                    });
                    VerticalLayout lytPropSheet = new VerticalLayout(propertySheet, btnClosePropSheet);
                    lytPropSheet.setWidthFull();
                    dlgPropSheet.add(lytPropSheet);
                    dlgPropSheet.open();

                } catch (InventoryException ex) {
                    Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            btnEditRack.setClassName("icon-button");
            btnEditRack.getElement().setProperty("title", ts.getTranslatedString("module.visualization.rack-view-edit-rack-title"));
            // Build info
            BoldLabel lblTitleName = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-name"));
            FormattedObjectDisplayNameSpan lblName = new FormattedObjectDisplayNameSpan(rack, false, false, false, false);
//            Label lblName = new Label(FormattedObjectDisplayNameSpan.getFormattedDisplayName(rack, false));
            BoldLabel lblTitleSerial = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-serial"));
            Label lblSerial = new Label(rack.getAttributes().containsKey("serialNumber") ? (String) rack.getAttributes().get("serialNumber") : ts.getTranslatedString("module.propertysheet.labels.null-value-property"));
            BoldLabel lblTitleVendor = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-vendor"));
            Label lblVendor = new Label(rack.getAttributes().containsKey("vendor") ? (String) rack.getAttributes().get("vendor") : ts.getTranslatedString("module.propertysheet.labels.null-value-property"));
            BoldLabel lblTitleOrdering = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-ordering"));
            Label lblOrdering = new Label(rackView.isOrderDescending() ? ts.getTranslatedString("module.visualization.rack-view-descending")
                    : ts.getTranslatedString("module.visualization.rack-view-ascending"));
            VerticalLayout lytInfo = new VerticalLayout(lblTitleName, lblName,
                    lblTitleSerial, lblSerial, lblTitleVendor, lblVendor, lblTitleOrdering, lblOrdering);
            lytInfo.setPadding(false);
            lytInfo.setMargin(false);

            lytInfo.setSpacing(false);
            lytInfo.setMargin(false);
            lytInfo.setWidth("220px");

            Image imgMoveUp = new Image("images/arrow_up.png", "");
            Image imgMoveDown = new Image("images/arrow_down.png", "");
            Image imgMove = new Image("images/move-unit.png", "");
            Image imgShowPorts = new Image("images/view_port.png", "");
            Image imgShowSlotContent = new Image("images/show-slot.png", "");
            Label moveUpInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-move-up-info"));
            Label moveDownInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-move-down-info"));
            Label moveInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-move-to-position"));
            Label showPortInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-show-ports"));
            Label SlotContentInfo = new Label(ts.getTranslatedString("module.visualization.rack-view-slot-info"));

            VerticalLayout lytIconsInfo = new VerticalLayout(new HorizontalLayout(imgMoveUp, moveUpInfo),
                    new HorizontalLayout(imgMoveDown, moveDownInfo),
                    new HorizontalLayout(imgMove, moveInfo),
                    new HorizontalLayout(imgShowPorts, showPortInfo),
                    new HorizontalLayout(imgShowSlotContent, SlotContentInfo));
            lytIconsInfo.setPadding(false);
            lytIconsInfo.setMargin(false);
            lytIconsInfo.setClassName("lytInfoIcons");
            VerticalLayout lytPanel = new VerticalLayout(lytIconsInfo);
            lytPanel.setId("lytTools");
            lytPanel.setWidth("200px");
            lytPanel.setAlignItems(FlexComponent.Alignment.START);
            lytPanel.setSpacing(false);
            lytPanel.getElement().getStyle().set("display", "none");

            HorizontalLayout lytViews = new HorizontalLayout(lytPanel, lytRackView, lytDetailedRackView);
            lytViews.setSizeFull();
            lytViews.setSpacing(false);
            BoldLabel lblTitle = new BoldLabel(String.format("%s : %s", ts.getTranslatedString("module.visualization.rack-view-name"), rack.getName()));
            Page page = UI.getCurrent().getPage();
            ToogleMenuButton btnSlide = new ToogleMenuButton("Show/Hide Help", "#lytTools",
                    () -> {
                        page.executeJs("$('#simpleMxGraphView  #graphContainer').animate({width: '65vw'}, 400)");
                        page.executeJs("$('#detailedMxGraphView  #graphContainer').animate({width: '65vw'}, 400)");        
                    }, 
                    () -> {
                        page.executeJs("$('#simpleMxGraphView  #graphContainer').animate({width: '50vw'}, 400)");
                        page.executeJs("$('#detailedMxGraphView  #graphContainer').animate({width: '50vw'}, 400)");               
                    });
            btnSlide.setMenuVisible(false);
            HorizontalLayout lytTools = new HorizontalLayout(btnRackViewType, btnPortDetails,
                    btnConnections, btnShowConnectionsInView, btnEditRack);
            lytTools.setAlignItems(FlexComponent.Alignment.END);
            final HorizontalLayout lytTitle = new HorizontalLayout(btnSlide, lytTools);
            lytTitle.setAlignItems(FlexComponent.Alignment.CENTER);
            VerticalLayout lytContent = new VerticalLayout(lytTitle, lytViews);
            lytContent.setSizeFull();
            lytContent.setSpacing(false);
            lytContent.setPadding(false);
            return lytContent;
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            return new VerticalLayout();
        }
    }
    
    /**
     * Builds a dialog with the connections in the given rack object
     *
     * @param rack The rack to which the connections are to be displayed
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws InvalidArgumentException
     */
    private void openExploreConnectionsDlg(BusinessObject rack) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {

        List<BusinessObjectLight> links = bem.getSpecialChildrenOfClassLightRecursive(rack.getId(), rack.getClassName(), Constants.CLASS_GENERICPHYSICALLINK, -1);
        Grid<BusinessObjectLight> tblConnections = new Grid();
        tblConnections.setWidthFull();
        tblConnections.setMaxHeight("450px");
        tblConnections.setItems(links);
        tblConnections.addComponentColumn(item -> {
            VerticalLayout lytName = new VerticalLayout(new BoldLabel(FormattedObjectDisplayNameSpan.getFormattedDisplayName(item, false)));
            lytName.setSpacing(false);
            lytName.setMargin(false);
            lytName.setMargin(false);
            lytName.setPadding(false);
            return lytName;
        }).setHeader(ts.getTranslatedString("module.visualization.rack-view-link-name")).setWidth("80px");

        tblConnections.addColumn(item -> {
            try {
                List<BusinessObjectLight> endpoint = bem.getSpecialAttribute(item.getClassName(), item.getId(), "endpointA");
                if (!endpoint.isEmpty()) {
                    List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(endpoint.get(0).getClassName(), endpoint.get(0).getId(), Constants.CLASS_RACK);
                    if (parents != null && parents.size() > 1) {
                        return parents.get(parents.size() - 2).getName() + " : " + endpoint.get(0).getName();
                    }

                    return ts.getTranslatedString("module.visualization.rack-view-disconnected");
                } else {
                    return ts.getTranslatedString("module.visualization.rack-view-disconnected");
                }
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                return "";
            }
        }).setHeader(ts.getTranslatedString("module.visualization.rack-view-source-device-port"));
        tblConnections.addColumn(item -> {
            try {
                List<BusinessObjectLight> endpoint = bem.getSpecialAttribute(item.getClassName(), item.getId(), "endpointB");
                if (!endpoint.isEmpty()) {
                    List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(endpoint.get(0).getClassName(), endpoint.get(0).getId(), Constants.CLASS_RACK);
                    if (parents != null && parents.size() > 1) {
                        return parents.get(parents.size() - 2).getName() + " : " + endpoint.get(0).getName();
                    }

                    return ts.getTranslatedString("module.visualization.rack-view-disconnected");
                } else {
                    return ts.getTranslatedString("module.visualization.rack-view-disconnected");
                }
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                return "";
            }
        }).setHeader(ts.getTranslatedString("module.visualization.rack-view-target-device-port"));

        EnhancedDialog dlgSummary = new EnhancedDialog();
        dlgSummary.setWidth("90%");
        BoldLabel lblSummary = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-connections-rack"));
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), evt -> {
            dlgSummary.close();
        });
        dlgSummary.setHeader(lblSummary);
        dlgSummary.setContent(tblConnections);
        dlgSummary.setFooter(btnClose);
        dlgSummary.open();
    }
    /**
     * This method adds to the graph the existent connections between the 
     * rendered ports in the graph
     * @param rack The rack to analyze
     * @param shapeObjects The map with the rack content
     */
    private void showConnectionsInView(BusinessObjectLight rack, Map<BusinessObjectLight, MxGraphNode> shapeObjects) {

        try {
            List<BusinessObjectLight> connections = bem.getSpecialChildrenOfClassLightRecursive(rack.getId(), rack.getClassName(), Constants.CLASS_GENERICPHYSICALLINK, -1);

            int linksAdded = 0;
            int linksAlreadyAdded = 0;
            for (int i = 0; i < connections.size(); i += 1) {
                BusinessObjectLight connection = connections.get(i);
                if (detailedMxGraphView.getEdges().stream().filter(item -> item.getUuid().equals(connection.getId())).findAny().isPresent()) {
                    linksAlreadyAdded++;
                    continue;
                }

                List<BusinessObjectLight> endpointA = bem.getSpecialAttribute(connection.getClassName(), connection.getId(), "endpointA");
                List<BusinessObjectLight> endpointB = bem.getSpecialAttribute(connection.getClassName(), connection.getId(), "endpointB");

                if (endpointA == null || endpointA.isEmpty()) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            String.format(ts.getTranslatedString("module.visualization.rack-view-endpointa-removed"), connection.toString()),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    continue;
                }
                if (endpointB == null || endpointB.isEmpty()) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            String.format(ts.getTranslatedString("module.visualization.rack-view-endpointb-removed"), connection.toString()), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                    continue;
                }

                if (!shapeObjects.containsKey(endpointA.get(0))) {
                    continue;
                }

                if (!shapeObjects.containsKey(endpointB.get(0))) {
                    continue;
                }

                MxGraphEdge newMxEdge = new MxGraphEdge();

                newMxEdge.setUuid(connection.getId());
                newMxEdge.setEdgeStyle(MxConstants.EDGESTYLE_ORTHOGONAL);
                newMxEdge.setSource(shapeObjects.get(endpointA.get(0)).getUuid());
                newMxEdge.setTarget(shapeObjects.get(endpointB.get(0)).getUuid());

                try {
                    ClassMetadata theClass = mem.getClass(connection.getClassName());
                    newMxEdge.setStrokeColor(UtilHtml.toHexString(new Color(theClass.getColor())));
                } catch (MetadataObjectNotFoundException ex) {
                    //In case of error, use a default black line
                }
                linksAdded++;
                detailedMxGraphView.addEdge(newMxEdge);

            }
            detailedMxGraphView.refreshGraph();
            if (linksAdded > 0)
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), 
                      String.format(ts.getTranslatedString("module.visualization.rack-view-connections-added"), linksAdded),
                      AbstractNotification.NotificationType.INFO, ts).open();
            else 
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                     ts.getTranslatedString("module.visualization.rack-view-no-connections-to-add"),
                     AbstractNotification.NotificationType.INFO, ts).open();
            if (connections.size() != (linksAdded + linksAlreadyAdded))
                  new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                     ts.getTranslatedString("module.visualization.rack-view-cannot-render-connection"),
                     AbstractNotification.NotificationType.INFO, ts).open();   
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }

    /**
     * Opens a new window that allows the link creation between the selected endpoints in the canvas
     * @param selectedEndPointA The start endpoint
     * @param selectedEndPointB The target endpoint
     * @param shapeObjects The map with the corresponding mxgraph node for each endpoint
     */
    private void openCreateConnectionDlg(BusinessObjectLight selectedEndPointA, BusinessObjectLight selectedEndPointB, Map<BusinessObjectLight, MxGraphNode> shapeObjects) {
        Dialog dlgCreateConnection = new Dialog();

        Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"));
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), evt -> {
            dlgCreateConnection.close();
        });
        dlgCreateConnection.setWidth("70%");
        dlgCreateConnection.setModal(true);
        dlgCreateConnection.setCloseOnOutsideClick(false);

        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setClassName("width300px");

        ComboBox<ConnectionType> cmbConnectionType = new ComboBox<>(ts.getTranslatedString("module.visualization.connection-wizard-connection-type"), Arrays.asList(new ConnectionType(1, "Connect Using a Container"),
                new ConnectionType(2, ts.getTranslatedString("module.visualization.connection-wizard-connect-using-link"))));
        cmbConnectionType.setAllowCustomValue(false);
        cmbConnectionType.setRequiredIndicatorVisible(true);
        cmbConnectionType.setLabel(ts.getTranslatedString("module.visualization.connection-wizard-select-connection-type"));
        cmbConnectionType.setClassName("width300px");

        ComboBox<ClassMetadataLight> cmbConnectionClass = new ComboBox<>(ts.getTranslatedString("module.visualization.connection-wizard-connection-class"));
        cmbConnectionClass.setAllowCustomValue(false);
        cmbConnectionClass.setRequiredIndicatorVisible(true);
        cmbConnectionClass.setLabel(ts.getTranslatedString("module.visualization.connection-wizard-select-connection-class"));
        cmbConnectionClass.setClassName("width300px");

        ComboBox<TemplateObjectLight> cmbTemplates = new ComboBox<>(ts.getTranslatedString("module.visualization.connection-wizard-template"));
        cmbTemplates.setEnabled(false);
        cmbTemplates.setClassName("width300px");
        Checkbox chkHasTemplate = new Checkbox(ts.getTranslatedString("module.visualization.connection-wizard-use-template"));
        chkHasTemplate.addValueChangeListener((newSelection) -> {
            cmbTemplates.setEnabled(chkHasTemplate.getValue());
        });

        cmbConnectionClass.addValueChangeListener((newSelection) -> {
            try {
                if (newSelection.getValue() != null) {
                    cmbTemplates.setItems(this.aem.getTemplatesForClass(newSelection.getValue().getName()));
                }
            } catch (MetadataObjectNotFoundException ex) {
                Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });

        cmbConnectionType.addValueChangeListener((newSelection) -> {
            try {
                if (newSelection.getValue() != null) {
                    if (newSelection.getValue().getType() == 1) {
                        cmbConnectionClass.setItems(this.mem.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false));
                    } else {
                        cmbConnectionClass.setItems(this.mem.getSubClassesLight(Constants.CLASS_GENERICPHYSICALLINK, false, false));
                    }
                }
            } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });

        HorizontalLayout lytTemplate = new HorizontalLayout(chkHasTemplate, cmbTemplates);
        lytTemplate.setAlignItems(FlexComponent.Alignment.BASELINE);

        btnOk.addClickListener((event) -> {
            try {
                if (txtName.getValue().trim().isEmpty() || cmbConnectionType.getValue() == null
                        || cmbConnectionClass.getValue() == null || (chkHasTemplate.getValue() && cmbTemplates.getValue() == null)) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.visualization.connection-wizard-fill-fields"),
                            AbstractNotification.NotificationType.WARNING, ts).open();
                    return;
                }
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                String newConnection = physicalConnectionsService.createPhysicalConnection(selectedEndPointA.getClassName(), selectedEndPointA.getId(), selectedEndPointB.getClassName(),
                        selectedEndPointB.getId(), txtName.getValue(), cmbConnectionClass.getValue().getName(),
                        chkHasTemplate.getValue() ? cmbTemplates.getValue().getId() : "", session.getUser().getUserName());

                MxGraphEdge edge = new MxGraphEdge();
                edge.setSource(selectedEndPointA.getId());
                edge.setTarget(selectedEndPointB.getId());
                edge.setEdgeStyle(MxConstants.EDGESTYLE_ORTHOGONAL);
                shapeObjects.get(selectedEndPointA).setConnectable(false);
                shapeObjects.get(selectedEndPointB).setConnectable(false);

                try {
                    ClassMetadata theClass = mem.getClass(cmbConnectionClass.getValue().getName());
                    edge.setStrokeColor(UtilHtml.toHexString(new Color(theClass.getColor())));
                } catch (MetadataObjectNotFoundException ex) {
                    //In case of error, use a default black line
                }
                detailedMxGraphView.addEdge(edge);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), String.format(ts.getTranslatedString("module.visualization.object-view-connection-created"), txtName.getValue()),
                        AbstractNotification.NotificationType.INFO, ts).open();
                dlgCreateConnection.close();
            } catch (IllegalStateException | OperationNotPermittedException ex) {
                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }

        });

        VerticalLayout lytContent = new VerticalLayout(txtName, cmbConnectionType, cmbConnectionClass,
                lytTemplate, new HorizontalLayout(btnCancel, btnOk));
        lytContent.setSpacing(true);
        lytContent.setSizeFull();

        dlgCreateConnection.add(lytContent);
        dlgCreateConnection.open();
    }
    
    /**
     * Opens a port explorer for all devices in the rack
     * @param rack The rack to explore
     */
    private void openDlgPortDetails(BusinessObjectLight rack) {

        try {
            List<BusinessObjectLight> devicesLight = bem.getObjectChildren(rack.getClassName(), rack.getId(), -1);
            HierarchicalDataProvider dataProvider = buildHierarchicalDataProvider(devicesLight);
            TreeGrid<BusinessObjectLight> tblPorts = new TreeGrid(dataProvider);
            tblPorts.setWidthFull();
            tblPorts.setMaxHeight("450px");
            tblPorts.addComponentHierarchyColumn(item -> {
                try {
                    if (mem.isSubclassOf(Constants.CLASS_GENERICPORT, item.getClassName())) {
                        String txtName = item.getName() == null || item.getName().isEmpty() ? "<Name Not Set>" : item.getName();
                        VerticalLayout lytName = new VerticalLayout(new BoldLabel(txtName));
                        List<BusinessObjectLight> parents = bem.getParents(item.getClassName(), item.getId());
                        String path = "";
                        for (BusinessObjectLight parent : parents) {
                            if (parent.equals(rack) 
                                    || bem.getParent(parent.getClassName(), parent.getId()).equals(rack)) 
                                break;
                            
                            path += parent.getName() + "/";
                        }
                        Label lblPath = new Label(path);
                        lblPath.addClassName("text-secondary-b");
                        lytName.setSpacing(false);
                        lytName.setMargin(false);
                        lytName.setMargin(false);
                        lytName.setPadding(false);
                        lytName.add(lblPath);
                        return lytName;
                    } else {
                          return new FormattedObjectDisplayNameSpan(item, false, false, false, false);
                    }
                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
                }
                return new Label();
            }).setHeader("Device/Port Name");

            tblPorts.addComponentColumn(item -> {

                try {
                    if (mem.isSubclassOf(Constants.CLASS_GENERICPORT, item.getClassName())) {
                        HashMap<String, List<BusinessObjectLight>> uses = bem.getSpecialAttributes(item.getClassName(), item.getId(), "uses");
                        if (!uses.containsKey("uses")) {
                            return new Label("N/A");
                        }
                        VerticalLayout lytUses = new VerticalLayout();
                        for (BusinessObjectLight obj : uses.get("uses")) {
                            lytUses.add(new Label(obj.getName()));
                        }
                        lytUses.setMargin(false);
                        lytUses.setMargin(false);
                        lytUses.setPadding(false);
                        return lytUses;
                    } else {
                        return new Label("");
                    }
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                    return new Label("");
                }
            }).setHeader(ts.getTranslatedString("module.visualization.rack-view-services"));

            tblPorts.addComponentColumn(item -> {
                try {
                    List<BusinessObjectLight> physicalPath = physicalConnectionsService.getPhysicalPath(item.getClassName(), item.getId());
                    if (physicalPath.size() > 0) {
                        BusinessObjectLight endPoint = physicalPath.get(physicalPath.size()-1);
                        BusinessObjectLight parent = bem.getParent(endPoint.getClassName(), endPoint.getId());
                        Label lblEndPoint = new Label(parent.getName() + " : " + endPoint.getName());
                        Button btnPhysicalPath = new Button(new Icon(VaadinIcon.FILE_TREE_SUB), evtPhysicalPath -> {
                            try {
                                EnhancedDialog dlgPhysicalPath = new EnhancedDialog();
                                dlgPhysicalPath.setWidth("95%");

                                Button btnClosePhysicalPath = new Button(ts.getTranslatedString("module.general.messages.close"), evtDlgPhysicalPath -> 
                                        dlgPhysicalPath.close());
                                BoldLabel lblPhysicalPath = new BoldLabel(ts.getTranslatedString("module.visualization.physical-path-view-name") + " : " + item.toString());                                                       

                                dlgPhysicalPath.setHeader(lblPhysicalPath);
                                dlgPhysicalPath.setContent(physicalPathViewWidget.build(item));
                                dlgPhysicalPath.setFooter(btnClosePhysicalPath);
                                dlgPhysicalPath.setThemeVariants(EnhancedDialogVariant.SIZE_LARGE);
                                dlgPhysicalPath.open();
                            } catch (InvalidArgumentException ex) {
                                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                 new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                            } catch (InventoryException ex) {
                                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                            }
                        });
                        btnPhysicalPath.getElement().setProperty("title", ts.getTranslatedString("module.visualization.physical-path-view-show"));     
                        HorizontalLayout lytEndPoint = new HorizontalLayout(lblEndPoint, btnPhysicalPath);
                        lytEndPoint.setFlexGrow(1, lblEndPoint);
                        lytEndPoint.setAlignItems(FlexComponent.Alignment.BASELINE);
                        return lytEndPoint;
                    } else 
                        return new Label(ts.getTranslatedString("module.visualization.rack-view-disconnected"));
                    
                } catch (MetadataObjectNotFoundException | InvalidArgumentException 
                        | IllegalStateException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                    Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                    return new Label("");
                } 
            }).setHeader(ts.getTranslatedString("module.visualization.rack-view-connected-to"));
                                    
            
            EnhancedDialog dlgPorts = new EnhancedDialog();
            dlgPorts.setWidth("80%");
            dlgPorts.setHeight("550px");
            BoldLabel lblSummary = new BoldLabel(ts.getTranslatedString("module.visualization.rack-view-port-summary"));
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), evtBtn -> {
                dlgPorts.close();
            });
            dlgPorts.setHeader(lblSummary);
            dlgPorts.setContent(tblPorts);
            dlgPorts.setFooter(btnClose);
            dlgPorts.open();
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    private HierarchicalDataProvider buildHierarchicalDataProvider(List<BusinessObjectLight> pools) {
        return new AbstractBackEndHierarchicalDataProvider<BusinessObjectLight, Void>() {
            @Override
            protected Stream fetchChildrenFromBackEnd(HierarchicalQuery<BusinessObjectLight, Void> hq) {
                    if (hq.getParent() == null) 
                        return pools.stream();
                    try { 
                        if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, hq.getParent().getClassName())) {

                                 List<BusinessObjectLight> ports = bem.getChildrenOfClassLightRecursive(hq.getParent().getId(), hq.getParent().getClassName(), Constants.CLASS_GENERICPORT, null, -1, -1);                                   
                                 return ports.stream();
                            } 
                         else
                            return Collections.EMPTY_SET.stream(); 
                    }
                    catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                        Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
                        return Collections.EMPTY_SET.stream();
                    }
            }

            @Override
            public int getChildCount(HierarchicalQuery<BusinessObjectLight, Void> hq) {
                try {
                    if (hq.getParent() == null) {
                        return pools.size();
                    }
                    if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, hq.getParent().getClassName())) {
                        return bem.getChildrenOfClassLightRecursive(hq.getParent().getId(), hq.getParent().getClassName(), Constants.CLASS_GENERICPORT, null, -1, -1).size();
                    } else
                        return 0;
                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
                    return 0;
                }
            }

            @Override
            public boolean hasChildren(BusinessObjectLight t) {
                try {
                    return (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, t.getClassName()));
                } catch (MetadataObjectNotFoundException ex) {
                    Logger.getLogger(RackViewWidget.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }

            }
        };
    }

    public class ConnectionType {

        private int type;
        private String displayName;

        public ConnectionType(int type, String displayName) {
            this.type = type;
            this.displayName = displayName;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
