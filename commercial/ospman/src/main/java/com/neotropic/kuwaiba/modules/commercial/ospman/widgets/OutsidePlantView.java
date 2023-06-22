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
package com.neotropic.kuwaiba.modules.commercial.ospman.widgets;

import com.neotropic.kuwaiba.modules.commercial.osp.external.services.OutsidePlantExternalServicesProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogOspViews;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowContainers;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantService;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowDeleteOspView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.SimpleNotification;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.UnitOfLength;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogOspViewSearch;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowAddContainers;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowAddNodes;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowFilters;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowMap;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNewContainer;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowNewNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.providers.google.GoogleMapsMapProvider;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractImageExporter;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ConfirmDialogEditConnections;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.EditConnectionsVisualAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;

/**
 * Graphically displays Outside Plant elements on a map.
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantView extends AbstractView<Component> {

    /**
     * Set of Outside Plant View XML tags
     */
    private final String TAG_VIEW = "view"; //NOI18N
    private final String TAG_CLASS = "class"; //NOI18N
    private final String TAG_CENTER = "center"; //NOI18N
    private final String TAG_ZOOM = "zoom"; //NOI18N
    private final String TAG_NODES = "nodes"; //NOI18N
    private final String TAG_NODE = "node"; //NOI18N
    private final String TAG_EDGES = "edges"; //NOI18N
    private final String TAG_EDGE = "edge"; //NOI18N
    private final String TAG_CONTROL_POINT = "controlpoint"; //NOI18N
    private final String TAG_MAP_TYPE_ID = "mapTypeId"; //NOI18N
    private final String TAG_SYNC_GEO_POSITION = "syncGeoPosition"; //NOI18N
    private final String TAG_DEFAULT_PARENT = "defaultParent"; //NOI18N
    private final String TAG_UNIT_OF_LENGTH = "unitOfLength"; //NOI18N
    private final String TAG_COMPUTE_EDGES_LENGTH = "computeEdgesLength"; //NOI18N
    /**
     * Set of Outside Plant View XML attributes
     */
    private final String ATTR_ID = "id"; //NOI18N
    private final String ATTR_CLASS = "class"; //NOI18N
    private final String ATTR_A_SIDE_ID = "asideid"; //NOI18N
    private final String ATTR_A_SIDE_CLASS = "asideclass"; //NOI18N
    private final String ATTR_B_SIDE_ID = "bsideid"; //NOI18N
    private final String ATTR_B_SIDE_CLASS = "bsideclass"; //NOI18N
    private final String ATTR_VERSION = "version"; ///NOI18N

    @Override
    public void nodeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void edgeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Set of Outside Plant View properties
     */
    private class PropertyNames {

        public static final String CENTER = "center"; //NOI18N
        public static final String ZOOM = "zoom"; //NOI18N
        public static final String MAP_TYPE_ID = "mapTypeId"; //NOI18N
        public static final String SYNC_GEO_POSITION = "syncGeoPosition"; //NOI18N
    }
    /**
     * Map Provider to the Outside Plant View
     */
    private MapProvider mapProvider;
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the Resource Factory
     */
    private final ResourceFactory resourceFactory;
    /**
     * Reference to the Physical Connections Service
     */
    private PhysicalConnectionsService physicalConnectionsService;
    /**
     * Reference to the New Business Object Visual Action
     */
    private NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * Reference to the New Business Object from Template Visual Action.
     */
    private NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    /**
     * Reference to the New Multiple Business Objects Visual Action.
     */
    private NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    /**
     * Reference to action to manage port mirroring.
     */
    private ManagePortMirroringVisualAction managePortMirroringVisualAction;
    /**
     * Reference to the action registry.
     */
    private CoreActionsRegistry coreActionsRegistry;
    /**
     * Reference to the module actions registry.
     */
    private AdvancedActionsRegistry advancedActionsRegistry;
    /**
     * Reference to the view widget registry.
     */
    private ViewWidgetRegistry viewWidgetRegistry;
    /**
     * Reference to ht explorer widget registry.
     */
    private ExplorerWidgetRegistry explorerWidgetRegistry;
    /**
     * Reference to the Outside Plant External Services Provider.
     */
    private OutsidePlantExternalServicesProvider ospExternalServicesProvider;

    private EditConnectionsVisualAction editConnectionEndPointsWidget;

    private final List<TemplateObjectLight> dragTemplate = new ArrayList();
    /**
     * The Outside Plant View Component
     */
    private Div component;
    private final boolean viewTools;

    private final HashMap<BusinessObjectViewNode, MapNode> nodes = new HashMap();
    private final HashMap<BusinessObjectViewEdge, MapEdge> edges = new HashMap();
    private boolean minZoomForLabels = false;
    private OutsidePlantAccordion ospAccordion;
    private WindowFilters wdwFilters;
    private WindowAddNodes wdwAddNodes;
    private WindowAddContainers wdwAddContainers;
    private OutsidePlantManagerDashboard ospmanDashboard;
    private final List<ShortcutRegistration> shortcutRegistrations = new ArrayList();

    private Consumer<BusinessObjectLight> consumerLocateNode;

    private boolean toolAddEdge = false;

    public OutsidePlantView(
            OutsidePlantManagerDashboard ospmanDashboard,
            ApplicationEntityManager aem,
            BusinessEntityManager bem,
            MetadataEntityManager mem,
            TranslationService ts,
            ResourceFactory resourceFactory,
            PhysicalConnectionsService physicalConnectionsService,
            NewBusinessObjectVisualAction newBusinessObjectVisualAction,
            NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction,
            NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction,
            ManagePortMirroringVisualAction managePortMirroringVisualAction,
            CoreActionsRegistry coreActionsRegistry,
            AdvancedActionsRegistry advancedActionsRegistry,
            ViewWidgetRegistry viewWidgetRegistry,
            ExplorerWidgetRegistry explorerWidgetRegistry,
            OutsidePlantAccordion ospAccordion,
            OutsidePlantExternalServicesProvider ospExternalServicesProvider,
            EditConnectionsVisualAction editConnectionEndPointsWidget) {

        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.physicalConnectionsService = physicalConnectionsService;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.newBusinessObjectFromTemplateVisualAction = newBusinessObjectFromTemplateVisualAction;
        this.newMultipleBusinessObjectsVisualAction = newMultipleBusinessObjectsVisualAction;
        this.managePortMirroringVisualAction = managePortMirroringVisualAction;
        this.coreActionsRegistry = coreActionsRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.viewTools = true;
        this.ospAccordion = ospAccordion;
        this.viewMap = new ViewMap();
        this.ospmanDashboard = ospmanDashboard;
        this.ospExternalServicesProvider = ospExternalServicesProvider;
        this.editConnectionEndPointsWidget = editConnectionEndPointsWidget;

        ospAccordion.getViewPropertySheet().addPropertyValueChangedListener(property -> {
            try {
                if (Constants.PROPERTY_NAME.equals(property.getName())) {
                    getProperties().put(Constants.PROPERTY_NAME, property.getValue() != null ? property.getValue() : "");
                } else if (Constants.PROPERTY_DESCRIPTION.equals(property.getName())) {
                    getProperties().put(Constants.PROPERTY_DESCRIPTION, property.getValue() != null ? property.getValue() : "");
                } else if (OspConstants.MAP_PROPERTY_SYNC_GEO_POSITION.equals(property.getName())) {
                    viewMap.getProperties().put(PropertyNames.SYNC_GEO_POSITION, property.getValue());
                    saveOspView(
                            getProperties().getProperty(Constants.PROPERTY_NAME),
                            getProperties().getProperty(Constants.PROPERTY_DESCRIPTION),
                            false
                    );
                } else if (OspConstants.MAP_PROPERTY_DEFAULT_PARENT.equals(property.getName())) {
                    ospAccordion.getViewPropertySheet().setDefaultParent((BusinessObjectLight) property.getValue());
                } else {
                    return;
                }

                ViewObject viewObject = aem.getOSPView(
                        (long) getProperties().get(Constants.PROPERTY_ID)
                );
                aem.updateOSPView(
                        viewObject.getId(),
                        (String) getProperties().get(Constants.PROPERTY_NAME),
                        (String) getProperties().get(Constants.PROPERTY_DESCRIPTION),
                        viewObject.getStructure()
                );
                ospAccordion.getViewPropertySheet().getDataProvider().refreshItem(property);
            } catch (InventoryException ex) {
                new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        });
        ospAccordion.getMapPropertySheet().addPropertyValueChangedListener(property -> {
            if (null != property.getName()) {
                switch (property.getName()) {
                    case OspConstants.MAP_PROPERTY_TYPE_ID:
                        if (property.getValue() != null) {
                            mapProvider.setMapTypeId((String) property.getValue());
                        }
                        break;
                    case OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH:
                        UnitOfLength unitOfLength = property.getValue() != null ? (UnitOfLength) property.getValue() : UnitOfLength.M;
                        mapProvider.setUnitOfLength(unitOfLength);
                        viewMap.getProperties().put(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH, unitOfLength);
                        if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels()) {
                            edges.forEach((viewEdge, mapNode)
                                    -> mapNode.setEdgeLabel(viewEdge.getIdentifier().getName())
                            );
                        }
                        break;
                    case OspConstants.MAP_PROPERTY_COMPUTE_EDGES_LENGTH:
                        Boolean computeEdgeLength = property.getValue() != null ? (Boolean) property.getValue() : false;
                        mapProvider.setComputeEdgesLength(computeEdgeLength);
                        viewMap.getProperties().put(OspConstants.MAP_PROPERTY_COMPUTE_EDGES_LENGTH, computeEdgeLength);
                        if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels()) {
                            edges.forEach((viewEdge, mapNode)
                                    -> mapNode.setEdgeLabel(viewEdge.getIdentifier().getName())
                            );
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        ospAccordion.getBusinessObjectPropertySheet().addPropertyValueChangedListener(property -> {
            try {
                BusinessObjectLight businessObject = ospAccordion.getBusinessObjectPropertySheet().getBusinessObject();
                HashMap<String, String> attrs = new HashMap();
                attrs.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));

                bem.updateObject(businessObject.getClassName(), businessObject.getId(), attrs);
                ospAccordion.getBusinessObjectPropertySheet().getDataProvider().refreshItem(property);

                if (Constants.PROPERTY_NAME.equals(property.getName())) {
                    BusinessObjectViewNode viewNode = (BusinessObjectViewNode) viewMap.findNode(businessObject);

                    if (viewNode != null) {
                        viewNode.getIdentifier().setName((String) property.getValue());

                        MapNode mapNode = nodes.get(viewNode);
                        mapNode.setNodeTitle((String) property.getValue());
                        if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels()) {
                            mapNode.setNodeLabel((String) property.getValue());
                        }
                    } else {
                        BusinessObjectViewEdge viewEdge = (BusinessObjectViewEdge) viewMap.findEdge(businessObject);

                        if (viewEdge != null) {
                            viewEdge.getIdentifier().setName((String) property.getValue());

                            if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels()) {
                                MapEdge mapEdge = edges.get(viewEdge);
                                mapEdge.setEdgeLabel((String) property.getValue());
                            }
                        }
                    }
                }
            } catch (InventoryException ex) {
                new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        });
        consumerLocateNode = businessObject -> {
            nodes.values().forEach(mapNode -> mapNode.setPlayAnimation(false));
            edges.values().forEach(mapEdge -> mapEdge.setPlayAnimation(false));
            BusinessObjectViewNode viewNode = (BusinessObjectViewNode) viewMap.findNode(businessObject);
            if (nodes.containsKey(viewNode)) {
                nodes.get(viewNode).setPlayAnimation(true);
                mapProvider.setCenter(new GeoCoordinate(
                        (double) viewNode.getProperties().get(OspConstants.ATTR_LAT),
                        (double) viewNode.getProperties().get(OspConstants.ATTR_LON)
                ));
            } else {
                new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"),
                        String.format(
                                ts.getTranslatedString("module.ospman.wdw.select-physical-node.view-node-not-found"),
                                businessObject.getName()
                        ),
                        AbstractNotification.NotificationType.INFO,
                        ts
                ).open();
            }
        };
    }

    public OutsidePlantView(
            ApplicationEntityManager aem,
            BusinessEntityManager bem,
            MetadataEntityManager mem,
            TranslationService ts,
            ResourceFactory resourceFactory) {

        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(resourceFactory);

        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        this.viewTools = false;
        this.viewMap = new ViewMap();
    }

    /**
     * <pre>{@code
     * <view version="">
     *  <class>OSPView</class>
     *  <center lon="" lat=""></center>
     *  <zoom>0</zoom>
     *  <nodes>
     *   <node lon="" lat="" class="businessObjectClass">businessObjectId</node>
     *  </nodes>
     *  <edge>
     *   <edge id="" class="" asideid="" asideclass="" bsideid="" bsideclass="">
     *    <controlpoint lon="" lat=""></controlpoint>
     *   </edge>
     *  </edge>
     * </view>
     * }</pre>
     */
    @Override
    public byte[] getAsXml() {
        final QName tagView = new QName(TAG_VIEW);
        final QName tagClass = new QName(TAG_CLASS);
        final QName tagCenter = new QName(TAG_CENTER);
        final QName tagZoom = new QName(TAG_ZOOM);
        final QName tagNodes = new QName(TAG_NODES);
        final QName tagNode = new QName(TAG_NODE);
        final QName tagEdges = new QName(TAG_EDGES);
        final QName tagEdge = new QName(TAG_EDGE);
        final QName tagControlpoint = new QName(TAG_CONTROL_POINT);
        final QName tagMapTypeId = new QName(TAG_MAP_TYPE_ID);
        final QName tagSyncGeoPosition = new QName(TAG_SYNC_GEO_POSITION);
        final QName tagDefaultParent = new QName(TAG_DEFAULT_PARENT);
        final QName tagUnitOfLength = new QName(TAG_UNIT_OF_LENGTH);
        final QName tagComputeEdgesLength = new QName(TAG_COMPUTE_EDGES_LENGTH);

        final QName attrLon = new QName(OspConstants.ATTR_LON);
        final QName attrLat = new QName(OspConstants.ATTR_LAT);
        final QName attrClass = new QName(ATTR_CLASS);
        final QName attrAsideId = new QName(ATTR_A_SIDE_ID);
        final QName attrAsideClass = new QName(ATTR_A_SIDE_CLASS);
        final QName attrBsideId = new QName(ATTR_B_SIDE_ID);
        final QName attrBsideClass = new QName(ATTR_B_SIDE_CLASS);
        final QName attrVersion = new QName(ATTR_VERSION);

        viewMap.getProperties().put(PropertyNames.CENTER, mapProvider.getCenter());
        viewMap.getProperties().put(PropertyNames.ZOOM, mapProvider.getZoom());
        viewMap.getProperties().put(PropertyNames.MAP_TYPE_ID, mapProvider.getMapTypeId() != null ? mapProvider.getMapTypeId() : "");
        viewMap.getProperties().put(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH, mapProvider.getUnitOfLength());

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();

            xmlew.add(xmlef.createStartElement(tagView, null, null));
            xmlew.add(xmlef.createAttribute(attrVersion, OutsidePlantService.VIEW_VERSION));

            xmlew.add(xmlef.createStartElement(tagClass, null, null));
            xmlew.add(xmlef.createCharacters("OSPView")); //NOI18N
            xmlew.add(xmlef.createEndElement(tagClass, null));

            xmlew.add(xmlef.createStartElement(tagCenter, null, null));
            xmlew.add(xmlef.createAttribute(attrLat, Double.toString(((GeoCoordinate) viewMap.getProperties().get(PropertyNames.CENTER)).getLatitude())));
            xmlew.add(xmlef.createAttribute(attrLon, Double.toString(((GeoCoordinate) viewMap.getProperties().get(PropertyNames.CENTER)).getLongitude())));
            xmlew.add(xmlef.createEndElement(tagCenter, null));

            xmlew.add(xmlef.createStartElement(tagMapTypeId, null, null));
            xmlew.add(xmlef.createCharacters(String.valueOf(viewMap.getProperties().get(PropertyNames.MAP_TYPE_ID))));
            xmlew.add(xmlef.createEndElement(tagMapTypeId, null));

            xmlew.add(xmlef.createStartElement(tagSyncGeoPosition, null, null));
            xmlew.add(xmlef.createCharacters(String.valueOf(viewMap.getProperties().get(PropertyNames.SYNC_GEO_POSITION))));
            xmlew.add(xmlef.createEndElement(tagSyncGeoPosition, null));

            BusinessObjectLight defaultParent = ospAccordion.getViewPropertySheet().getDefaultParent();
            if (defaultParent != null) {
                xmlew.add(xmlef.createStartElement(tagDefaultParent, null, null));
                xmlew.add(xmlef.createAttribute(ATTR_ID, defaultParent.getId()));
                xmlew.add(xmlef.createAttribute(ATTR_CLASS, defaultParent.getClassName()));
                xmlew.add(xmlef.createEndElement(tagDefaultParent, null));
            }
            UnitOfLength unitOfLength = (UnitOfLength) viewMap.getProperties().get(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH);
            if (unitOfLength != null) {
                xmlew.add(xmlef.createStartElement(tagUnitOfLength, null, null));
                xmlew.add(xmlef.createCharacters(unitOfLength.toString()));
                xmlew.add(xmlef.createEndElement(tagUnitOfLength, null));
            }
            Boolean computeEdgesLength = (Boolean) viewMap.getProperties().get(OspConstants.MAP_PROPERTY_COMPUTE_EDGES_LENGTH);
            if (computeEdgesLength != null) {
                xmlew.add(xmlef.createStartElement(tagComputeEdgesLength, null, null));
                xmlew.add(xmlef.createCharacters(computeEdgesLength.toString()));
                xmlew.add(xmlef.createEndElement(tagComputeEdgesLength, null));
            }
            xmlew.add(xmlef.createStartElement(tagZoom, null, null));
            xmlew.add(xmlef.createCharacters(String.valueOf(viewMap.getProperties().get(PropertyNames.ZOOM))));
            xmlew.add(xmlef.createEndElement(tagZoom, null));

            xmlew.add(xmlef.createStartElement(tagNodes, null, null));
            for (AbstractViewNode node : viewMap.getNodes()) {
                xmlew.add(xmlef.createStartElement(tagNode, null, null));
                xmlew.add(xmlef.createAttribute(attrLat, String.valueOf(node.getProperties().get(OspConstants.ATTR_LAT))));
                xmlew.add(xmlef.createAttribute(attrLon, String.valueOf(node.getProperties().get(OspConstants.ATTR_LON))));
                xmlew.add(xmlef.createAttribute(attrClass, ((BusinessObjectLight) node.getIdentifier()).getClassName()));
                xmlew.add(xmlef.createCharacters(((BusinessObjectLight) node.getIdentifier()).getId()));
                xmlew.add(xmlef.createEndElement(tagNode, null));
            }
            xmlew.add(xmlef.createEndElement(tagNodes, null));

            xmlew.add(xmlef.createStartElement(tagEdges, null, null));
            for (AbstractViewEdge edge : viewMap.getEdges()) {
                BusinessObjectLight businessObject = (BusinessObjectLight) edge.getIdentifier();

                xmlew.add(xmlef.createStartElement(tagEdge, null, null));
                xmlew.add(xmlef.createAttribute(ATTR_ID, businessObject.getId()));
                xmlew.add(xmlef.createAttribute(ATTR_CLASS, businessObject.getClassName()));
                BusinessObjectLight source = (BusinessObjectLight) viewMap.getEdgeSource(edge).getIdentifier();
                BusinessObjectLight target = (BusinessObjectLight) viewMap.getEdgeTarget(edge).getIdentifier();
                xmlew.add(xmlef.createAttribute(attrAsideId, source.getId()));
                xmlew.add(xmlef.createAttribute(attrAsideClass, source.getClassName()));
                xmlew.add(xmlef.createAttribute(attrBsideId, target.getId()));
                xmlew.add(xmlef.createAttribute(attrBsideClass, target.getClassName()));

                for (GeoCoordinate controlPoint : (List<GeoCoordinate>) edge.getProperties().get(OspConstants.PROPERTY_CONTROL_POINTS)) {
                    xmlew.add(xmlef.createStartElement(tagControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(attrLat, String.valueOf(controlPoint.getLatitude())));
                    xmlew.add(xmlef.createAttribute(attrLon, String.valueOf(controlPoint.getLongitude())));
                    xmlew.add(xmlef.createEndElement(tagControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(tagEdge, null));
            }
            xmlew.add(xmlef.createEndElement(tagEdges, null));

            xmlew.add(xmlef.createEndElement(tagView, null));
            xmlew.close();

            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            return new byte[0];
        }
    }

    @Override
    public byte[] getAsImage(AbstractImageExporter exporter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void addNode(GeoCoordinate geoCoordinate) {
        if (wdwAddNodes != null && wdwAddNodes.isOpened()) {
            try {
                BusinessObjectLight businessObjectLight = wdwAddNodes.getNode();
                if (businessObjectLight == null) {
                    mapProvider.setDrawingMarkerMode(coordinate -> addNode(coordinate));
                    new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.information"),
                            ts.getTranslatedString("module.ospman.dialog.add-nodes.no-selected-node"),
                            AbstractNotification.NotificationType.INFO, ts
                    ).open();
                    return;
                }
                ClassMetadata businessObjectClass = mem.getClass(businessObjectLight.getClassName());
                BusinessObject businessObject = bem.getObject(businessObjectLight.getClassName(), businessObjectLight.getId());

                Runnable runnableDrawingMarkerMode = () -> mapProvider.setDrawingMarkerMode(latLng -> addNode(latLng));

                Consumer<GeoCoordinate> consumerAddNode = coordinate -> {
                    BusinessObjectViewNode newViewNode = new BusinessObjectViewNode(businessObjectLight);
                    newViewNode.getProperties().put(OspConstants.ATTR_LAT, coordinate.getLatitude());
                    newViewNode.getProperties().put(OspConstants.ATTR_LON, coordinate.getLongitude());
                    viewMap.addNode(newViewNode);
                    addNode(businessObjectLight, newViewNode.getProperties());
                    wdwAddNodes.notifyAddedNode(businessObjectLight);

                    if (viewMap.getProperties().containsKey(PropertyNames.SYNC_GEO_POSITION)
                            && (boolean) viewMap.getProperties().get(PropertyNames.SYNC_GEO_POSITION)) {
                        try {
                            HashMap<String, String> attributes = new HashMap();
                            attributes.put(OspConstants.ATTR_LATITUDE, String.valueOf(coordinate.getLatitude()));
                            attributes.put(OspConstants.ATTR_LONGITUDE, String.valueOf(coordinate.getLongitude()));
                            bem.updateObject(businessObjectLight.getClassName(), businessObjectLight.getId(), attributes);
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"),
                                    ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    }
                    runnableDrawingMarkerMode.run();
                };
                if (businessObjectClass.hasAttribute(OspConstants.ATTR_LATITUDE)
                        && businessObjectClass.hasAttribute(OspConstants.ATTR_LONGITUDE)
                        && (Constants.DATA_TYPE_FLOAT.equals(businessObjectClass.getAttribute(OspConstants.ATTR_LATITUDE).getType())
                        || Constants.DATA_TYPE_DOUBLE.equals(businessObjectClass.getAttribute(OspConstants.ATTR_LATITUDE).getType()))
                        && (Constants.DATA_TYPE_FLOAT.equals(businessObjectClass.getAttribute(OspConstants.ATTR_LONGITUDE).getType())
                        || Constants.DATA_TYPE_DOUBLE.equals(businessObjectClass.getAttribute(OspConstants.ATTR_LONGITUDE).getType()))
                        && businessObject.getAttributes().containsKey(OspConstants.ATTR_LATITUDE)
                        && businessObject.getAttributes().containsKey(OspConstants.ATTR_LONGITUDE)) {

                    Label lblSetPosition = new Label(ts.getTranslatedString("module.ospman.view.node.set-position"));
                    VerticalLayout lytContent = new VerticalLayout(lblSetPosition);
                    lytContent.setSizeFull();
                    lytContent.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lblSetPosition);

                    Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"));
                    Button btnNo = new Button(ts.getTranslatedString("module.ospman.view.node.set-position.no"));
                    Button btnYes = new Button(ts.getTranslatedString("module.ospman.view.node.set-position.yes"));
                    HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnNo, btnYes);
                    lytFooter.setMargin(false);
                    lytFooter.setPadding(false);
                    lytFooter.setFlexGrow(1, btnCancel, btnNo, btnYes);

                    ConfirmDialog wdwNodePosition = new ConfirmDialog();
                    wdwNodePosition.setCloseOnEsc(false);
                    wdwNodePosition.setCloseOnOutsideClick(false);
                    wdwNodePosition.setHeader(ts.getTranslatedString("module.general.labels.confirmation"));
                    wdwNodePosition.setContent(lytContent);
                    wdwNodePosition.setFooter(lytFooter);
                    wdwNodePosition.open();

                    btnCancel.addClickListener(clickEvent -> {
                        runnableDrawingMarkerMode.run();
                        wdwNodePosition.close();
                    });
                    btnNo.addClickListener(clickEvent -> {
                        wdwNodePosition.close();
                        try {
                            HashMap<String, String> attributes = new HashMap();
                            attributes.put(OspConstants.ATTR_LATITUDE, String.valueOf(geoCoordinate.getLatitude()));
                            attributes.put(OspConstants.ATTR_LONGITUDE, String.valueOf(geoCoordinate.getLongitude()));
                            bem.updateObject(businessObject.getClassName(), businessObject.getId(), attributes);

                            consumerAddNode.accept(geoCoordinate);
                        } catch (InventoryException ex) {
                            runnableDrawingMarkerMode.run();
                            new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"),
                                    ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    });
                    btnYes.addClickListener(clickEvent -> {
                        wdwNodePosition.close();
                        double latitude = Double.valueOf(String.valueOf(businessObject.getAttributes().get(OspConstants.ATTR_LATITUDE)));
                        double longitude = Double.valueOf(String.valueOf(businessObject.getAttributes().get(OspConstants.ATTR_LONGITUDE)));
                        consumerAddNode.accept(new GeoCoordinate(latitude, longitude));
                    });
                    btnYes.addClickShortcut(Key.ENTER);
                } else {
                    consumerAddNode.accept(geoCoordinate);
                }

            } catch (InventoryException ex) {
                new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        }
    }

    private void setDrawingPolylineMode() {
        if (mapProvider != null) {
            mapProvider.setDrawingEdgeMode((parameters, callbackEdgeHelperCancel) -> {
                BusinessObjectLight source = (BusinessObjectLight) parameters.get(OspConstants.BUSINESS_OBJECT_SOURCE);
                BusinessObjectLight target = (BusinessObjectLight) parameters.get(OspConstants.BUSINESS_OBJECT_TARGET);
                List<GeoCoordinate> controlPoints = (List) parameters.get(OspConstants.PROPERTY_CONTROL_POINTS);

                WindowNewContainer dialogNewContainer = new WindowNewContainer(
                        source, target, ts, aem, bem, mem, physicalConnectionsService,
                        container -> {
                            if (controlPoints.size() >= 2) {
                                BusinessObjectViewEdge viewEdge = new BusinessObjectViewEdge(container);
                                viewEdge.getProperties().put(OspConstants.PROPERTY_CONTROL_POINTS, controlPoints);
                                viewMap.addEdge(viewEdge);
                                viewMap.attachSourceNode(viewEdge, viewMap.findNode(source));
                                viewMap.attachTargetNode(viewEdge, viewMap.findNode(target));
                                addEdge(container, source, target, viewEdge.getProperties());

                                callbackEdgeHelperCancel.run();
                                ConfirmDialogEditConnections wdwEditConn = new ConfirmDialogEditConnections(
                                        viewEdge.getIdentifier(), editConnectionEndPointsWidget, ts
                                );
                                wdwEditConn.open();
                            }
                        },
                        callbackEdgeHelperCancel
                );
                dialogNewContainer.open();
            });
        }
    }

    public void newOspView() {
        try {
            clean();
            getAsUiElement();
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }

    @Override
    public Component getAsUiElement() throws InvalidArgumentException {
        if (mapProvider == null) {
            String generalMapsProvider = null;
            try {
                generalMapsProvider = (String) aem.getConfigurationVariableValue("general.maps.provider");
                Class mapClass = Class.forName(generalMapsProvider);
                if (MapProvider.class.isAssignableFrom(mapClass)) {
                    mapProvider = (MapProvider) mapClass.getDeclaredConstructor().newInstance();
                    mapProvider.createComponent(aem, mem, resourceFactory, ts);
                    if (mapProvider.getComponent() != null) {
                        if (viewMap.getProperties().containsKey(PropertyNames.CENTER)) {
                            mapProvider.setCenter((GeoCoordinate) viewMap.getProperties().get(PropertyNames.CENTER));
                        }
                        if (viewMap.getProperties().containsKey(PropertyNames.ZOOM)) {
                            mapProvider.setZoom(Double.valueOf(String.valueOf(viewMap.getProperties().get(PropertyNames.ZOOM))));
                        }
                        if (viewMap.getProperties().containsKey(PropertyNames.MAP_TYPE_ID)) {
                            mapProvider.setMapTypeId(String.valueOf(viewMap.getProperties().get(PropertyNames.MAP_TYPE_ID)));
                        }
                        if (component == null) {
                            component = new Div();
                            component.setClassName("ospman-div"); //NOI18N                     
                        }
                        if (viewTools) {
                            final String width = "18px";
                            final String height = "18px";

                            Button btnNewOspView = new Button(VaadinIcon.FILE_ADD.create());
                                    btnNewOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.new")); //NOI18N

                            Button btnOpenOspView = new Button(VaadinIcon.FOLDER_OPEN_O.create());
                            btnOpenOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.open")); //NOI18N

                            Button btnSaveOspView = new Button(VaadinIcon.DOWNLOAD.create());
                            btnSaveOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.save")); //NOI18N

                            Button btnDeleteOspView = new Button(VaadinIcon.CLOSE_CIRCLE_O.create());
                            btnDeleteOspView.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.delete")); //NOI18N

                            Button btnHand = new Button(VaadinIcon.HAND.create());
                            btnHand.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.hand")); //NOI18N

                            Image imgMapMarkerAdd = new Image("map-marker-add.svg", "map-marker-add"); //NOI18N
                            imgMapMarkerAdd.setWidth(width);
                            imgMapMarkerAdd.setHeight(height);
                            Button btnMarker = new Button(imgMapMarkerAdd);
                            btnMarker.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.marker")); //NOI18N

                            Button btnPolyline = new Button(VaadinIcon.PLUG.create());
                            btnPolyline.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.polyline")); //NOI18N

                            Button btnSearch = new Button(VaadinIcon.SEARCH.create());
                            btnSearch.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.search")); //NOI18N

                            Image imgWireAdd = new Image("container-add.svg", "container-add");
                            imgWireAdd.setWidth(width);
                            imgWireAdd.setHeight(height);
                            Button btnWire = new Button(imgWireAdd);
                            btnWire.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.wire")); //NOI18N

                            Image imgLinkAdd = new Image("link-add.svg", "link-add"); //NOI18N
                            imgLinkAdd.setWidth(width);
                            imgLinkAdd.setHeight(height);
                            Button btnAddLink = new Button(imgLinkAdd);
                            btnAddLink.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.links.title")); //NOI18N

                            Button btnFilter = new Button(VaadinIcon.FILTER.create());
                            btnFilter.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.filter.title")); //NOI18N

                            Image imgAddContainers = new Image("marker-cxn.svg", "node-cxn"); //NOI18N
                            imgAddContainers.setWidth(width);
                            imgAddContainers.setHeight(height);
                            Button btnAddContainers = new Button(imgAddContainers);
                            btnAddContainers.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.node-cxn.title")); //NOI18N
                            
                            Image imgMeasureDistance = new Image("ruler.png", "measure"); //NOI18N
                            imgMeasureDistance.setWidth(width);
                            imgMeasureDistance.setHeight(height);
                            Button btnMeasureDistance = new Button(imgMeasureDistance);
                            btnMeasureDistance.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.measure-distance")); //NOI18N
                            
                            List<Button> buttons = Arrays.asList(btnOpenOspView,
                                    btnNewOspView,
                                    btnDeleteOspView,
                                    btnSaveOspView,
                                    btnHand,
                                    btnMarker,
                                    btnAddContainers,
                                    btnPolyline,
                                    btnWire,
                                    btnAddLink,
                                    btnSearch,
                                    btnFilter,
                                    btnMeasureDistance
                            );
                            ComponentEventListener<ClickEvent<Button>> generalClickEventListener = clickEvent -> {
                                buttons.forEach(button -> button.removeClassNames("nav-button")); //NOI18N
                                if (clickEvent.getSource().equals(btnOpenOspView)
                                        || clickEvent.getSource().equals(btnHand)
                                        || clickEvent.getSource().equals(btnMarker)
                                        || clickEvent.getSource().equals(btnPolyline)
                                        || clickEvent.getSource().equals(btnWire)
                                        || clickEvent.getSource().equals(btnAddLink)
                                        || clickEvent.getSource().equals(btnAddContainers)) {
                                    clickEvent.getSource().addClassName("nav-button"); //NOI18N
                                }
                                if (!clickEvent.getSource().equals(btnMarker) && wdwAddNodes != null) {
                                    if (wdwAddNodes.isOpened()) {
                                        wdwAddNodes.close();
                                    }
                                    wdwAddNodes = null;
                                }
                                if (wdwAddContainers != null && wdwAddContainers.isOpened()) {
                                    btnAddContainers.addClassName("nav-button"); //NOI18N
                                }
                                toolAddEdge = false;
                            };
                            buttons.forEach(button -> {
                                button.setWidth("32px");
                                button.setHeight("32px");
                                button.getStyle().set("margin", "0px 1px 0px 0px"); //NOI18N
                                button.setClassName("icon-button"); //NOI18N
                                button.addClickListener(generalClickEventListener);
                            });
                            btnHand.click();

                            shortcutRegistrations.addAll(Arrays.asList(
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnNewOspView.click(), Key.KEY_N, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnOpenOspView.click(), Key.KEY_O, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnSaveOspView.click(), Key.KEY_U, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnDeleteOspView.click(), Key.KEY_D, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnHand.click(), Key.KEY_H, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnMarker.click(), Key.KEY_M, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnPolyline.click(), Key.KEY_E, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnWire.click(), Key.KEY_C, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnAddLink.click(), Key.KEY_L, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnSearch.click(), Key.KEY_S, KeyModifier.ALT),
                                    Shortcuts.addShortcutListener(ospmanDashboard, () -> btnFilter.click(), Key.KEY_F, KeyModifier.ALT)
                            ));
                            FlexLayout lytTools = new FlexLayout();

                            Button btnAccordion = new Button(VaadinIcon.CHEVRON_LEFT_SMALL.create(), clickEvent -> {
                                ospAccordion.setVisible(!ospAccordion.isVisible());
                                if (ospAccordion.isVisible()) {
                                    component.setWidth("75%");
                                    clickEvent.getSource().setIcon(VaadinIcon.CHEVRON_LEFT_SMALL.create());
                                    clickEvent.getSource().addClassName("nav-button");
                                    lytTools.removeClassName("ospman-map-only-tools");
                                    lytTools.addClassNames("ospman-map-tools"); //NOI18N

                                } else {
                                    component.setWidthFull();
                                    clickEvent.getSource().setIcon(VaadinIcon.CHEVRON_RIGHT_SMALL.create());
                                    clickEvent.getSource().removeClassName("nav-button");
                                    lytTools.removeClassName("ospman-map-tools");
                                    lytTools.addClassNames("ospman-map-only-tools"); //NOI18N
                                }
                            });
                            btnAccordion.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.tools.osp-view.properties-panel")); //NOI18N

                            btnAccordion.setWidth("32px");
                            btnAccordion.setHeight("32px");
                            btnAccordion.getStyle().set("margin", "0px 1px 0px 0px"); //NOI18N
                            btnAccordion.setClassName("icon-button"); //NOI18N
                            if (ospAccordion.isVisible()) {
                                btnAccordion.addClassName("nav-button"); //NOI18N
                                lytTools.addClassNames("ospman-map-tools"); //NOI18N
                            } else {
                                lytTools.addClassNames("ospman-map-only-tools"); //NOI18N
                            }
                            lytTools.add(btnAccordion);
                            lytTools.add(buttons.toArray(new Component[0]));
                            component.add(lytTools);
                            /*
                            Div div = new Div();
                            div.addClassName("ospman-tabs"); //NOI18N
                            
                            MenuBar menuBar = new MenuBar();
                            Icon icon = VaadinIcon.PENCIL.create(); 
                            MenuItem menuItem = menuBar.addItem(icon);
                            menuItem.getElement().setAttribute("title", "Pencil");
                                                        
                            PaperDialog paperDialog = new PaperDialog();
                            paperDialog.positionTarget(menuItem);
                            paperDialog.setNoOverlap(true);
                            paperDialog.setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
                            paperDialog.setVerticalAlign(PaperDialog.VerticalAlign.TOP);
                            paperDialog.setMargin(false);
                            paperDialog.add(VaadinIcon.TRASH.create());
                            div.add(paperDialog);

                            menuItem.addClickListener(clickEvent -> {
                                icon.setColor("yellow");
                                paperDialog.open();
                            });
                            div.add(menuBar);
                            component.add(div);
                             */
                            btnNewOspView.addClickListener(clickEvent -> {
                                newOspView();
                                saveOspView();
                            });
                            btnOpenOspView.addClickListener(clickEvent -> {
                                DialogOspViews ospViewDialog = new DialogOspViews(btnOpenOspView, aem, ts, viewObject -> {
                                    clean();
                                    getProperties().put(Constants.PROPERTY_ID, viewObject.getId());
                                    getProperties().put(Constants.PROPERTY_NAME, viewObject.getName());
                                    getProperties().put(Constants.PROPERTY_DESCRIPTION, viewObject.getDescription());
                                    buildFromSavedView(viewObject.getStructure());

                                    try {
                                        getAsUiElement();
                                    } catch (InvalidArgumentException ex) {
                                        new SimpleNotification(
                                                ts.getTranslatedString("module.general.messages.error"),
                                                ts.getTranslatedString("module.general.messages.unexpected-error"),
                                                AbstractNotification.NotificationType.ERROR, ts
                                        ).open();
                                        return;
                                    }
                                    ospAccordion.getViewPropertySheet().setView(
                                            viewObject,
                                            (boolean) this.viewMap.getProperties().get(PropertyNames.SYNC_GEO_POSITION)
                                    );
                                });
                                lytTools.add(ospViewDialog);
                                ospViewDialog.open();
                            });
                            btnSaveOspView.addClickListener(clickEvent -> {
                                saveOspView(
                                        getProperties().getProperty(Constants.PROPERTY_NAME),
                                        getProperties().getProperty(Constants.PROPERTY_DESCRIPTION),
                                        true
                                );
                            });
                            btnDeleteOspView.addClickListener(clickEvent -> {
                                deleteOspView();
                            });
                            btnHand.addClickListener(clickEvent -> {
                                mapProvider.setHandMode();
                            });
                            btnMarker.addClickListener(clickEvent -> {
                                if (wdwAddNodes == null) {
                                    wdwAddNodes = new WindowAddNodes(viewMap.getNodes(), bem, ts, drawingNode -> {
                                        if (drawingNode) {
                                            mapProvider.setDrawingMarkerMode(coordinate -> addNode(coordinate));
                                        } else {
                                            mapProvider.setHandMode();
                                            clickEvent.getSource().removeClassName("nav-button"); //NOI18N
                                            wdwAddNodes = null;
                                        }
                                    });
                                    wdwAddNodes.open();
                                }
                            });
                            btnAddContainers.addClickListener(clickEvent -> {
                                if (wdwAddContainers == null) {
                                    wdwAddContainers = new WindowAddContainers(viewMap.getNodes(), viewMap.getEdges(), bem, mem, ts, wdw -> {
                                        BusinessObjectLight container = wdw.getContainer();
                                        BusinessObjectLight source = wdw.getSource();
                                        BusinessObjectLight target = wdw.getTarget();

                                        if (source != null && target != null && container != null) {
                                            AbstractViewNode sourceNode = viewMap.findNode(source);
                                            AbstractViewNode targetNode = viewMap.findNode(target);
                                            GeoCoordinate sourceCoordinate = new GeoCoordinate(
                                                    (double) sourceNode.getProperties().get(OspConstants.ATTR_LAT),
                                                    (double) sourceNode.getProperties().get(OspConstants.ATTR_LON)
                                            );
                                            GeoCoordinate targetCoordinate = new GeoCoordinate(
                                                    (double) targetNode.getProperties().get(OspConstants.ATTR_LAT),
                                                    (double) targetNode.getProperties().get(OspConstants.ATTR_LON)
                                            );
                                            List<GeoCoordinate> controlPoints = new ArrayList();
                                            controlPoints.add(sourceCoordinate);
                                            controlPoints.add(targetCoordinate);

                                            BusinessObjectViewEdge viewEdge = new BusinessObjectViewEdge(container);
                                            viewEdge.getProperties().put(OspConstants.PROPERTY_CONTROL_POINTS, controlPoints);
                                            viewMap.addEdge(viewEdge);
                                            viewMap.attachSourceNode(viewEdge, sourceNode);
                                            viewMap.attachTargetNode(viewEdge, targetNode);
                                            addEdge(container, source, target, viewEdge.getProperties());
                                            new SimpleNotification(
                                                    ts.getTranslatedString("module.general.messages.information"),
                                                    String.format(ts.getTranslatedString("module.ospman.tools.add-containers.container-added"), container.getName()),
                                                    AbstractNotification.NotificationType.INFO, ts
                                            ).open();
                                            ConfirmDialogEditConnections wdwEditConn = new ConfirmDialogEditConnections(
                                                    viewEdge.getIdentifier(), editConnectionEndPointsWidget, ts
                                            );
                                            wdwEditConn.open();
                                        }
                                    });
                                    wdwAddContainers.addOpenedChangeListener(openedChangeEvent -> {
                                        if (!openedChangeEvent.isOpened()) {
                                            btnAddContainers.removeClassName("nav-button"); //NOI18N
                                        }
                                    });
                                }
                                wdwAddContainers.open();
                            });
                            btnPolyline.addClickListener(clickEvent -> {
                                toolAddEdge = true;
                                setDrawingPolylineMode();
                            });
                            btnWire.addClickListener(clickEvent -> {
                                mapProvider.setPathSelectionMode((viewEdges, callbackPathSelectionCancel) -> {
                                    WindowContainers wdwContainer = new WindowContainers(viewEdges, aem, bem, mem, ts, callbackPathSelectionCancel, true, editConnectionEndPointsWidget);
                                    wdwContainer.open();
                                });
                            });
                            btnSearch.addClickListener(clickEvent -> {
                                DialogOspViewSearch dialogOspViewSearch = new DialogOspViewSearch(
                                        btnSearch, ts, viewMap.getNodes(), viewMap.getEdges(),
                                        item -> {
                                            nodes.values().forEach(mapNode -> mapNode.setPlayAnimation(false));
                                            edges.values().forEach(mapEdge -> mapEdge.setPlayAnimation(false));

                                            BusinessObjectViewNode viewNode = (BusinessObjectViewNode) viewMap.findNode(item);
                                            BusinessObjectViewEdge viewEdge = (BusinessObjectViewEdge) viewMap.findEdge(item);

                                            if (nodes.containsKey(viewNode)) {
                                                nodes.get(viewNode).setPlayAnimation(true);
                                                mapProvider.setCenter(new GeoCoordinate(
                                                        (double) viewNode.getProperties().get(OspConstants.ATTR_LAT),
                                                        (double) viewNode.getProperties().get(OspConstants.ATTR_LON)
                                                ));
                                            } else if (edges.containsKey(viewEdge)) {
                                                MapEdge mapEdge = edges.get(viewEdge);
                                                mapEdge.getEdgeLabelPosition(
                                                        position -> mapProvider.setCenter(position)
                                                );
                                                mapEdge.setPlayAnimation(true);
                                            }
                                        }
                                );
                                lytTools.add(dialogOspViewSearch);
                                dialogOspViewSearch.open();
                            });
                            btnAddLink.addClickListener(clickEvent -> {
                                mapProvider.setPathSelectionMode((viewEdges, callbackPathSelectionCancel) -> {
                                    WindowContainers wdwContainer = new WindowContainers(viewEdges, aem, bem, mem, ts, callbackPathSelectionCancel, false, editConnectionEndPointsWidget);
                                    wdwContainer.open();
                                });
                            });
                            btnFilter.addClickListener(clickEvent -> {
                                wdwFilters = new WindowFilters(nodes, edges, mem, ts);
                                wdwFilters.open();
                            });
                            btnMeasureDistance.addClickListener(clickEvent -> mapProvider.setMeasureMode());
                            if (mapProvider instanceof GoogleMapsMapProvider)
                                btnMeasureDistance.setVisible(false);
                        }
                        mapProvider.addZoomChangedEventListener(event -> {
                            boolean newMinZoomForLabels = mapProvider.getZoom() >= mapProvider.getMinZoomForLabels();
                            if (minZoomForLabels != newMinZoomForLabels) {
                                minZoomForLabels = newMinZoomForLabels;
                                if (minZoomForLabels) {
                                    nodes.forEach((viewNode, mapNode)
                                            -> mapNode.setNodeLabel(viewNode.getIdentifier().getName())
                                    );
                                    edges.forEach((viewEdge, mapEdge)
                                            -> mapEdge.setEdgeLabel(viewEdge.getIdentifier().getName())
                                    );
                                } else {
                                    nodes.values().forEach(node -> node.setNodeLabel(null));
                                    edges.values().forEach(edge -> edge.setEdgeLabel(null));
                                }
                            }
                        });
                        mapProvider.addIdleEventListener(event -> {
                            mapProvider.removeIdleEventListener(event.getListener());

                            viewMap.getNodes().forEach(viewNode
                                    -> addNode((BusinessObjectLight) viewNode.getIdentifier(), viewNode.getProperties())
                            );
                            viewMap.getEdges().forEach(viewEdge
                                    -> addEdge(
                                            (BusinessObjectLight) viewEdge.getIdentifier(),
                                            (BusinessObjectLight) viewMap.getEdgeSource(viewEdge).getIdentifier(),
                                            (BusinessObjectLight) viewMap.getEdgeTarget(viewEdge).getIdentifier(),
                                            viewEdge.getProperties()
                                    )
                            );
                            nodes.values().forEach(node
                                    -> node.setNodeTitle(node.getViewNode().getIdentifier().getName())
                            );
                        });
                        mapProvider.addIdleEventListener(event -> {
                            if (ospAccordion != null) {
                                ospAccordion.getMapPropertySheet().setPropertyCenterLatitude(mapProvider.getCenter().getLatitude());
                                ospAccordion.getMapPropertySheet().setPropertyCenterLongitude(mapProvider.getCenter().getLongitude());
                                ospAccordion.getMapPropertySheet().setPropertyZoom(mapProvider.getZoom());
                                ospAccordion.getMapPropertySheet().setPropertyMapTypeId(mapProvider.getMapTypeId());
                            }
                        });
                        mapProvider.addMouseMoveEventListener(event -> {
                            double newNodeLat = event.getLat();
                            double newNodeLng = event.getLng();
                            dragTemplate.forEach(template -> {
                                WindowNewNode wdwNewNode = new WindowNewNode(
                                        ospAccordion.getViewPropertySheet().getDefaultParent(),
                                        template,
                                        aem, bem, mem, ts, resourceFactory,
                                        coreActionsRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry,
                                        newNode -> {
                                            BusinessObjectViewNode newViewNode = new BusinessObjectViewNode(newNode);
                                            newViewNode.getProperties().put(OspConstants.ATTR_LAT, newNodeLat);
                                            newViewNode.getProperties().put(OspConstants.ATTR_LON, newNodeLng);
                                            viewMap.addNode(newViewNode);
                                            addNode(newNode, newViewNode.getProperties());

                                            if (viewMap.getProperties().containsKey(PropertyNames.SYNC_GEO_POSITION)
                                            && (boolean) viewMap.getProperties().get(PropertyNames.SYNC_GEO_POSITION)) {
                                                try {
                                                    HashMap<String, String> attributes = new HashMap();
                                                    attributes.put(OspConstants.ATTR_LATITUDE, String.valueOf(newNodeLat));
                                                    attributes.put(OspConstants.ATTR_LONGITUDE, String.valueOf(newNodeLng));
                                                    bem.updateObject(newNode.getClassName(), newNode.getId(), attributes);
                                                } catch (InventoryException ex) {
                                                    new SimpleNotification(
                                                            ts.getTranslatedString("module.general.messages.error"),
                                                            ex.getLocalizedMessage(),
                                                            AbstractNotification.NotificationType.ERROR, ts
                                                    ).open();
                                                }
                                            }
                                        },
                                        newNode -> {
                                            if (newNode != null) {
                                                try {
                                                    BusinessObjectLight businessObject = bem.getObjectLight(newNode.getClassName(), newNode.getId());
                                                    BusinessObjectViewNode viewNode = (BusinessObjectViewNode) viewMap.findNode(businessObject);

                                                    if (viewNode != null) {
                                                        viewNode.getIdentifier().setName(businessObject.getName());

                                                        MapNode mapNode = nodes.get(viewNode);
                                                        mapNode.setNodeTitle(businessObject.getName());
                                                        if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels()) {
                                                            mapNode.setNodeLabel(businessObject.getName());
                                                        }
                                                    }
                                                } catch (InventoryException ex) {
                                                    new SimpleNotification(
                                                            ts.getTranslatedString("module.general.messages.error"),
                                                            ex.getLocalizedMessage(),
                                                            AbstractNotification.NotificationType.ERROR,
                                                            ts
                                                    ).open();
                                                }
                                            }
                                        }
                                );
                                wdwNewNode.open();
                            });
                            dragTemplate.clear();
                        });
                        if (ospAccordion != null) {
                            ospAccordion.getMapPropertySheet().setPropertyMapTypeIds(mapProvider.getMapTypeIds());

                            UnitOfLength unitOfLength = (UnitOfLength) viewMap.getProperties().get(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH);
                            if (unitOfLength == null) {
                                unitOfLength = UnitOfLength.M;
                                viewMap.getProperties().put(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH, unitOfLength);
                            }
                            mapProvider.setUnitOfLength(unitOfLength);
                            ospAccordion.getMapPropertySheet().setPropertyUnitOfLength(unitOfLength);

                            Boolean computeEdgesLength = (Boolean) viewMap.getProperties().get(OspConstants.MAP_PROPERTY_COMPUTE_EDGES_LENGTH);
                            if (computeEdgesLength == null) {
                                computeEdgesLength = true;
                                viewMap.getProperties().put(OspConstants.MAP_PROPERTY_COMPUTE_EDGES_LENGTH, computeEdgesLength);
                            }
                            mapProvider.setComputeEdgesLength(computeEdgesLength);
                            ospAccordion.getMapPropertySheet().setPropertyComputeEdgesLength(computeEdgesLength);

                            mapProvider.addRightClickEventListener(event -> {
                                if (!toolAddEdge) {
                                    new WindowMap(
                                            (UnitOfLength) viewMap.getProperties().get(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH),
                                            event.getLat(), event.getLng(), getAsViewMap().getNodes(),
                                            coreActionsRegistry, advancedActionsRegistry, viewWidgetRegistry, explorerWidgetRegistry,
                                            aem, bem, mem, physicalConnectionsService, ts, consumerLocateNode, mapProvider
                                    ).open();
                                }
                            });
                        }

                        Component mapProviderComponent = mapProvider.getComponent();

                        DropTarget<Component> dropTarget = DropTarget.create(mapProviderComponent);
                        dropTarget.addDropListener(dropEvent -> {
                            dragTemplate.clear();
                            Object dragData = dropEvent.getDragData().get();
                            if (dragData instanceof List) {
                                List list = (List) dragData;
                                list.forEach(item -> {
                                    if (item instanceof TemplateObjectLight) {
                                        dragTemplate.add((TemplateObjectLight) item);
                                    }
                                });
                            }
                        });
                        component.add(mapProviderComponent);
                    }
                } else {
                    new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            String.format(ts.getTranslatedString("module.ospman.not-valid-map-provider"), mapClass.getCanonicalName()),
                            AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }

            } catch (ClassNotFoundException ex) {
                new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        String.format(ts.getTranslatedString("module.ospman.not-valid-map-provider"), generalMapsProvider),
                        AbstractNotification.NotificationType.ERROR, ts
                ).open();
            } catch (InventoryException ex) {
                new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts
                ).open();
            } catch (IllegalAccessException | IllegalArgumentException
                    | InstantiationException | NoSuchMethodException
                    | SecurityException | InvocationTargetException ex) {

                Logger.getLogger(OutsidePlantView.class.toString()).log(Level.SEVERE, ex.getLocalizedMessage());
                new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ts.getTranslatedString("module.general.messages.unexpected-error"),
                        AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        }
        return component;
    }

    /**
     * <pre>{@code
     * <view version="">
     *  <class>OSPView</class>
     *  <center lon="" lat=""></center>
     *  <zoom>0</zoom>
     *  <nodes>
     *   <node lon="" lat="" class="businessObjectClass" overlayid="">businessObjectId</node>
     *  </nodes>
     *  <edge>
     *   <edge id="" class="" asideid="" asideclass="" bsideid="" bsideclass="" overlayid="">
     *    <controlpoint lon="" lat=""></controlpoint>
     *   </edge>
     *  </edge>
     * </view>
     * }</pre>
     */
    @Override
    public void buildFromSavedView(byte[] view) {
        try {
            QName tagView = new QName(TAG_VIEW);
            QName tagCenter = new QName(TAG_CENTER);
            QName tagZoom = new QName(TAG_ZOOM);
            QName tagNode = new QName(TAG_NODE);
            QName tagEdge = new QName(TAG_EDGE);
            QName tagControlPoint = new QName(TAG_CONTROL_POINT);
            QName tagMapTypeId = new QName(TAG_MAP_TYPE_ID);
            QName tagSyncGeoPosition = new QName(TAG_SYNC_GEO_POSITION);
            QName tagDefaultParent = new QName(TAG_DEFAULT_PARENT);
            QName tagUnitOfLength = new QName(TAG_UNIT_OF_LENGTH);
            QName tagComputeEdgesLength = new QName(TAG_COMPUTE_EDGES_LENGTH);

            QName tmpTagControlPoint = new QName("controlPoint");
            boolean tmpBadNames = false;

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(view);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (tagView.equals(reader.getName())) {
                        String version = reader.getAttributeValue(null, ATTR_VERSION);
                        if ("2.1".equals(version)) //TODO: remove temporals and lng, 2.1 hard code references
                        {
                            tmpBadNames = true;
                        }
                    }
                    if (tagCenter.equals(reader.getName())) {
                        double lat = Double.valueOf(reader.getAttributeValue(null, OspConstants.ATTR_LAT));
                        double lon = Double.valueOf(reader.getAttributeValue(null, tmpBadNames ? "lng" : OspConstants.ATTR_LON));
                        GeoCoordinate mapCenter = new GeoCoordinate(lat, lon);
                        viewMap.getProperties().put(PropertyNames.CENTER, mapCenter);
                    } else if (tagMapTypeId.equals(reader.getName())) {
                        String mapTypeId = String.valueOf(reader.getElementText());
                        viewMap.getProperties().put(PropertyNames.MAP_TYPE_ID, mapTypeId);
                    } else if (tagSyncGeoPosition.equals(reader.getName())) {
                        boolean syncGeoPosition = Boolean.valueOf(String.valueOf(reader.getElementText()));
                        viewMap.getProperties().put(PropertyNames.SYNC_GEO_POSITION, syncGeoPosition);
                    } else if (tagDefaultParent.equals(reader.getName())) {
                        String defaultParentId = String.valueOf(reader.getAttributeValue(null, ATTR_ID));
                        String defaultParentClass = String.valueOf(reader.getAttributeValue(null, ATTR_CLASS));
                        try {
                            ospAccordion.getViewPropertySheet().setDefaultParent(
                                    bem.getObjectLight(defaultParentClass, defaultParentId)
                            );
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"),
                                    ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    } else if (tagUnitOfLength.equals(reader.getName())) {
                        UnitOfLength unitOfLength = UnitOfLength.getUnitOfLength(reader.getElementText());
                        if (unitOfLength != null) {
                            viewMap.getProperties().put(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH, unitOfLength);
                        }
                    } else if (tagComputeEdgesLength.equals(reader.getName())) {
                        String elementText = reader.getElementText();
                        if (elementText != null) {
                            Boolean computeEdgesLength = Boolean.valueOf(elementText);
                            viewMap.getProperties().put(OspConstants.MAP_PROPERTY_COMPUTE_EDGES_LENGTH, computeEdgesLength);
                        }
                    } else if (tagZoom.equals(reader.getName())) {
                        double zoom = Double.valueOf(reader.getElementText());
                        viewMap.getProperties().put(PropertyNames.ZOOM, zoom);
                    } else if (tagNode.equals(reader.getName())) {
                        try {
                            String objectClass = reader.getAttributeValue(null, ATTR_CLASS);
                            double lat = Double.valueOf(reader.getAttributeValue(null, OspConstants.ATTR_LAT));
                            double lon = Double.valueOf(reader.getAttributeValue(null, tmpBadNames ? "lng" : OspConstants.ATTR_LON));
                            String objectId = reader.getElementText();

                            BusinessObjectViewNode viewNode = new BusinessObjectViewNode(bem.getObjectLight(objectClass, objectId));
                            viewNode.getProperties().put(OspConstants.ATTR_LAT, lat);
                            viewNode.getProperties().put(OspConstants.ATTR_LON, lon);
                            getAsViewMap().addNode(viewNode);
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"),
                                    ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    } else if (tagEdge.equals(reader.getName())) {
                        try {
                            String objectId = reader.getAttributeValue(null, ATTR_ID);
                            String objectClass = reader.getAttributeValue(null, ATTR_CLASS);
                            String aSideId = reader.getAttributeValue(null, ATTR_A_SIDE_ID);
                            String aSideClass = reader.getAttributeValue(null, ATTR_A_SIDE_CLASS);
                            String bSideId = reader.getAttributeValue(null, ATTR_B_SIDE_ID);
                            String bSideClass = reader.getAttributeValue(null, ATTR_B_SIDE_CLASS);

                            List<GeoCoordinate> controlPoints = new ArrayList();
                            while (true) {
                                reader.nextTag();
                                if (tmpBadNames ? tmpTagControlPoint.equals(reader.getName()) : tagControlPoint.equals(reader.getName())) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        controlPoints.add(new GeoCoordinate(
                                                Double.valueOf(reader.getAttributeValue(null, OspConstants.ATTR_LAT)),
                                                Double.valueOf(reader.getAttributeValue(null, tmpBadNames ? "lng" : OspConstants.ATTR_LON))
                                        ));
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (controlPoints.size() >= 2) {
                                BusinessObjectViewEdge viewEdge = new BusinessObjectViewEdge(bem.getObjectLight(objectClass, objectId));
                                viewEdge.getProperties().put(OspConstants.PROPERTY_CONTROL_POINTS, controlPoints);
                                viewMap.addEdge(viewEdge);
                                viewMap.attachSourceNode(viewEdge, viewMap.findNode(bem.getObjectLight(aSideClass, aSideId)));
                                viewMap.attachTargetNode(viewEdge, viewMap.findNode(bem.getObjectLight(bSideClass, bSideId)));
                            }
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"),
                                    ex.getLocalizedMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            Logger.getLogger(OutsidePlantView.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.general.messages.unexpected-error"),
                    AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }

    @Override
    public void clean() {
        shortcutRegistrations.forEach(shortcutRegistration -> shortcutRegistration.remove());
        shortcutRegistrations.clear();

        if (wdwFilters != null && wdwFilters.isOpened()) {
            wdwFilters.close();
        }
        wdwFilters = null;

        if (wdwAddNodes != null && wdwAddNodes.isOpened()) {
            wdwAddNodes.close();
        }
        wdwAddNodes = null;

        if (wdwAddContainers != null && wdwAddContainers.isOpened()) {
            wdwAddContainers.close();
        }
        wdwAddContainers = null;

        this.viewMap.clear();

        mapProvider = null;

        if (component != null) {
            component.removeAll();
        }

        nodes.clear();
        edges.clear();

        this.getProperties().put(Constants.PROPERTY_ID, -1);
        this.getProperties().put(Constants.PROPERTY_NAME, "");
        this.getProperties().put(Constants.PROPERTY_DESCRIPTION, "");

        Double mapCenterLatitude = OutsidePlantService.DEFAULT_CENTER_LATITUDE;
        Double mapCenterLongitude = OutsidePlantService.DEFAULT_CENTER_LONGITUDE;

        try {
            mapCenterLatitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLatitude"); //NOI18N
            mapCenterLongitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLongitude"); //NOI18N
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            //Nothing to do
        }
        this.viewMap.getProperties().put(PropertyNames.CENTER, new GeoCoordinate(mapCenterLatitude, mapCenterLongitude));
        try {
            this.viewMap.getProperties().put(PropertyNames.ZOOM, aem.getConfigurationVariableValue("widgets.simplemap.zoom")); //NOI18N
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            this.viewMap.getProperties().put(PropertyNames.ZOOM, OutsidePlantService.DEFAULT_ZOOM); //NOI18N
        }
        this.viewMap.getProperties().put(PropertyNames.MAP_TYPE_ID, com.neotropic.flow.component.googlemap.Constants.MapTypeId.ROADMAP); //NOI18N
        this.viewMap.getProperties().put(PropertyNames.SYNC_GEO_POSITION, true);
        this.viewMap.getProperties().put(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH, UnitOfLength.M);
        this.viewMap.getProperties().put(OspConstants.MAP_PROPERTY_COMPUTE_EDGES_LENGTH, true);

        if (ospAccordion != null) {
            ospAccordion.getViewPropertySheet().clear();
            ospAccordion.getBusinessObjectPropertySheet().clear();
            ospAccordion.getNewNodePanelContent().clear();
        }
    }

    @Override
    public AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties) {
        BusinessObjectViewNode viewNode = (BusinessObjectViewNode) viewMap.findNode(businessObject.getId());
        MapNode mapNode = mapProvider.addNode(viewNode);
        nodes.put(viewNode, mapNode);
        Command cmdSelectViewNode = () -> {
            mapProvider.getEdgeSelectionManager().deselectAll();
            mapProvider.getNodeSelectionManager().deselectAll();
            mapProvider.getNodeSelectionManager().select(viewNode);
        };
        mapNode.addClickEventListener(event -> cmdSelectViewNode.execute());
        mapNode.addRightClickEventListener(event -> cmdSelectViewNode.execute());

        if (viewTools) {
            mapNode.setDraggableNode(true);
            Command cmdViewNodeClick = () -> {
                if (ospAccordion != null) {
                    try {
                        ospAccordion.getBusinessObjectPropertySheet().setBusinessObject(businessObject);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                }
            };
            mapNode.addClickEventListener(event -> cmdViewNodeClick.execute());
            mapNode.addRightClickEventListener(event -> cmdViewNodeClick.execute());

            mapNode.addPositionChangedEventListener(event -> {
                GeoCoordinate geoCoordinate = new GeoCoordinate(event.getLat(), event.getLng());
                updatePosition(viewNode, geoCoordinate);
                if (viewMap.getProperties().containsKey(PropertyNames.SYNC_GEO_POSITION)
                        && (boolean) viewMap.getProperties().get(PropertyNames.SYNC_GEO_POSITION)) {

                    try {
                        ClassMetadata businessObjectClass = mem.getClass(businessObject.getClassName());

                        if (businessObjectClass.hasAttribute(OspConstants.ATTR_LATITUDE)
                                && businessObjectClass.hasAttribute(OspConstants.ATTR_LONGITUDE)
                                && (Constants.DATA_TYPE_FLOAT.equals(businessObjectClass.getAttribute(OspConstants.ATTR_LATITUDE).getType())
                                || Constants.DATA_TYPE_DOUBLE.equals(businessObjectClass.getAttribute(OspConstants.ATTR_LATITUDE).getType()))
                                && (Constants.DATA_TYPE_FLOAT.equals(businessObjectClass.getAttribute(OspConstants.ATTR_LONGITUDE).getType())
                                || Constants.DATA_TYPE_DOUBLE.equals(businessObjectClass.getAttribute(OspConstants.ATTR_LONGITUDE).getType()))) {

                            HashMap<String, String> attributes = new HashMap();
                            attributes.put(OspConstants.ATTR_LATITUDE, String.valueOf(geoCoordinate.getLatitude()));
                            attributes.put(OspConstants.ATTR_LONGITUDE, String.valueOf(geoCoordinate.getLongitude()));
                            bem.updateObject(businessObject.getClassName(), businessObject.getId(), attributes);
                        }
                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                }
            });
            mapNode.addRightClickEventListener(event -> openWindowNode(viewNode));
        }
        if (viewTools) {
            saveOspView(
                    getProperties().getProperty(Constants.PROPERTY_NAME),
                    getProperties().getProperty(Constants.PROPERTY_DESCRIPTION),
                    false
            );
        }
        return viewNode;
    }

    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
        AbstractViewNode sourceNode = this.viewMap.findNode(sourceBusinessObject.getId());
        if (sourceNode == null) {
            return null;
        }
        AbstractViewNode targetNode = this.viewMap.findNode(targetBusinessObject.getId());
        if (targetNode == null) {
            return null;
        }
        BusinessObjectViewEdge viewEdge = (BusinessObjectViewEdge) viewMap.findEdge(businessObject.getId());
        MapEdge mapEdge = mapProvider.addEdge(viewEdge);
        edges.put(viewEdge, mapEdge);
        Command cmdSelectViewEdge = () -> {
            mapProvider.getNodeSelectionManager().deselectAll();
            mapProvider.getEdgeSelectionManager().deselectAll();
            mapProvider.getEdgeSelectionManager().select(viewEdge);
        };
        mapEdge.addClickEventListener(event -> cmdSelectViewEdge.execute());
        mapEdge.addRightClickEventListener(event -> cmdSelectViewEdge.execute());
        if (viewTools) {
            mapEdge.addPathChangedEventListener(event -> {
                mapEdge.computeLength(event.getControlPoints(), length -> {
                    mapEdge.setLength(length);

                    if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels()) {
                        mapEdge.setEdgeLabel(businessObject.getName());
                    }
                });
                viewEdge.getProperties().put(OspConstants.PROPERTY_CONTROL_POINTS, event.getControlPoints());
                
                GeoCoordinate sourcePosition = event.getControlPoints().get(0);
                GeoCoordinate targetPosition = event.getControlPoints().get(event.getControlPoints().size() - 1);
                
                MapNode sourceMapNode = nodes.get((BusinessObjectViewNode) sourceNode);
                sourceMapNode.setPosition(sourcePosition);
                MapNode targetMapNode = nodes.get((BusinessObjectViewNode) targetNode);
                targetMapNode.setPosition(targetPosition);
                
                updatePosition(sourceNode, sourcePosition);
                updatePosition(targetNode, targetPosition);
            });
            Command cmdViewEdgeClick = () -> {
                edges.values().forEach(edge -> {
                    if (!edge.equals(mapEdge)) {
                        edge.setEditableEdge(false);
                    }
                });
                mapEdge.setEditableEdge(!mapEdge.getEditableEdge());
                if (ospAccordion != null) {
                    try {
                        ospAccordion.getBusinessObjectPropertySheet().setBusinessObject(businessObject);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                }
            };
            mapEdge.addClickEventListener(event -> cmdViewEdgeClick.execute());
            mapEdge.addRightClickEventListener(event -> cmdViewEdgeClick.execute());

            mapEdge.addRightClickEventListener(event -> openWindowEdge(viewEdge));
        }
        if (viewTools) {
            saveOspView(
                    getProperties().getProperty(Constants.PROPERTY_NAME),
                    getProperties().getProperty(Constants.PROPERTY_DESCRIPTION),
                    false
            );
        }
        return viewEdge;
    }

    @Override
    public void removeNode(BusinessObjectLight businessObject) {
        AbstractViewNode viewNode = viewMap.findNode(businessObject.getId());
        if (viewNode instanceof BusinessObjectViewNode) {
            List<BusinessObjectViewEdge> viewEdgesToRemove = new ArrayList();
            viewMap.getEdges().forEach(viewEdge -> {
                if (viewNode.equals(viewMap.getEdgeSource(viewEdge)) || viewNode.equals(viewMap.getEdgeTarget(viewEdge))) {
                    viewEdgesToRemove.add((BusinessObjectViewEdge) viewEdge);
                }
            });
            viewEdgesToRemove.forEach(viewEdge -> removeEdge(viewEdge.getIdentifier()));
            nodes.remove((BusinessObjectViewNode) viewNode);
            mapProvider.removeNode((BusinessObjectViewNode) viewNode);
            viewMap.getNodes().remove(viewNode);

            if (viewTools) {
                saveOspView(
                        getProperties().getProperty(Constants.PROPERTY_NAME),
                        getProperties().getProperty(Constants.PROPERTY_DESCRIPTION),
                        false
                );
            }
        }
    }

    @Override
    public void removeEdge(BusinessObjectLight businessObject) {
        AbstractViewEdge viewEdge = viewMap.findEdge(businessObject.getId());
        if (viewEdge instanceof BusinessObjectViewEdge) {
            edges.remove((BusinessObjectViewEdge) viewEdge);
            mapProvider.removeEdge((BusinessObjectViewEdge) viewEdge);
            viewMap.getEdges().remove(viewEdge);

            if (viewTools) {
                saveOspView(
                        getProperties().getProperty(Constants.PROPERTY_NAME),
                        getProperties().getProperty(Constants.PROPERTY_DESCRIPTION),
                        false
                );
            }
            if (wdwAddContainers != null) {
                wdwAddContainers.updateContainers();
            }
        }
    }

    private void openWindowNode(BusinessObjectViewNode viewNode) {
        if (viewNode != null) {
            UnitOfLength unitOfLength = (UnitOfLength) viewMap.getProperties().get(OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH);

            WindowNode wdwNode = new WindowNode(unitOfLength, viewNode, getAsViewMap().getNodes(),
                    coreActionsRegistry,
                    advancedActionsRegistry,
                    viewWidgetRegistry,
                    explorerWidgetRegistry,
                    aem, bem, mem, ts,
                    physicalConnectionsService,
                    newBusinessObjectVisualAction,
                    newBusinessObjectFromTemplateVisualAction,
                    newMultipleBusinessObjectsVisualAction,
                    managePortMirroringVisualAction,
                    () -> {
                        ConfirmDialog confirmDialog = new ConfirmDialog(ts,
                                ts.getTranslatedString("module.general.labels.confirmation"),
                                new Label(String.format(ts.getTranslatedString("module.ospman.view-node.tool.remove.confirm"), viewNode.getIdentifier().getName())),
                                ts.getTranslatedString("module.general.messages.ok"),
                                () -> removeNode(viewNode.getIdentifier())
                        );
                        confirmDialog.setDraggable(true);
                        confirmDialog.open();
                    },
                    consumerLocateNode,
                    mapProvider,
                    ospExternalServicesProvider
            );
            wdwNode.open();
        }
    }

    private void openWindowEdge(BusinessObjectViewEdge viewEdge) {
        if (viewEdge != null) {
            WindowEdge wdwEdge = new WindowEdge(viewEdge, ts,
                    () -> {
                        ConfirmDialog confirmDialog = new ConfirmDialog(ts,
                                ts.getTranslatedString("module.general.labels.confirmation"),
                                new Label(String.format(ts.getTranslatedString("module.ospman.view-edge.tool.remove.confirm"), viewEdge.getIdentifier().getName())),
                                ts.getTranslatedString("module.general.messages.ok"),
                                () -> removeEdge(viewEdge.getIdentifier())
                        );
                        confirmDialog.setDraggable(true);
                        confirmDialog.open();
                    },
                    editConnectionEndPointsWidget
            );
            wdwEdge.open();
        }
    }

    private void saveOspView() {
        FormLayout lytForm = new FormLayout();
        TextField txtName = new TextField();
        txtName.setWidth("98%");
        txtName.setRequiredIndicatorVisible(true);
        txtName.setValue(this.getProperties().getProperty(Constants.PROPERTY_NAME) == null
                ? "" : this.getProperties().getProperty(Constants.PROPERTY_NAME));
        TextField txtDescription = new TextField();
        txtDescription.setWidth("98%");
        txtDescription.setValue(this.getProperties().getProperty(Constants.PROPERTY_DESCRIPTION) == null
                ? "" : this.getProperties().getProperty(Constants.PROPERTY_DESCRIPTION));
        lytForm.addFormItem(txtName, ts.getTranslatedString("module.general.labels.name"));
        lytForm.addFormItem(txtDescription, ts.getTranslatedString("module.general.labels.description"));

        ConfirmDialog confirmDialog = new ConfirmDialog(ts,
                ts.getTranslatedString("module.ospman.save-view"), lytForm,
                ts.getTranslatedString("module.general.messages.ok"),
                () -> saveOspView(txtName.getValue(), txtDescription.getValue(), true)
        );
        confirmDialog.setDraggable(true);
        confirmDialog.open();
    }

    private void saveOspView(String viewName, String viewDescription, boolean showInfo) {
        viewName = viewName != null ? viewName : "";
        viewDescription = viewDescription != null ? viewDescription : "";
        if (viewName.isEmpty()) {
            saveOspView();
            return;
        }
        if (!showInfo && viewName.isEmpty()) {
            saveOspView();
            return;
        }
        try {
            boolean created = false;
            if (this.properties.get(Constants.PROPERTY_ID).equals(-1)) {
                long newOSPViewId = aem.createOSPView(viewName, viewDescription, getAsXml());
                this.getProperties().put(Constants.PROPERTY_ID, newOSPViewId);
                created = true;
            } else {
                aem.updateOSPView((long) this.getProperties().get(Constants.PROPERTY_ID),
                        viewName, viewDescription, getAsXml());
            }
            this.getProperties().put(Constants.PROPERTY_NAME, viewName);
            this.getProperties().put(Constants.PROPERTY_DESCRIPTION, viewDescription);

            ospAccordion.getViewPropertySheet().setView(
                    aem.getOSPView((long) this.getProperties().get(Constants.PROPERTY_ID)),
                    (boolean) this.viewMap.getProperties().get(PropertyNames.SYNC_GEO_POSITION)
            );
            if (showInfo) {
                new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.success"),
                        created ? ts.getTranslatedString("module.ospman.view-created") : ts.getTranslatedString("module.ospman.view-saved"),
                        AbstractNotification.NotificationType.INFO, ts
                ).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }

    private void deleteOspView() {
        if (this.getProperties().get(Constants.PROPERTY_ID) instanceof Integer && (int) this.getProperties().get(Constants.PROPERTY_ID) == -1) {
            new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"),
                    ts.getTranslatedString("module.ospman.tools.osp-view.delete.info"),
                    AbstractNotification.NotificationType.INFO,
                    ts
            ).open();
            return;
        }
        WindowDeleteOspView confirmDialog = new WindowDeleteOspView((long) this.getProperties().get(Constants.PROPERTY_ID), ts, aem, this);
        confirmDialog.open();
    }

    private void updatePosition(AbstractViewNode viewNode, GeoCoordinate geoCoordinate) {
        MapNode mapNode = nodes.get((BusinessObjectViewNode) viewNode);
        if (mapNode != null) {
            viewNode.getProperties().put(OspConstants.ATTR_LAT, geoCoordinate.getLatitude());
            viewNode.getProperties().put(OspConstants.ATTR_LON, geoCoordinate.getLongitude());

            viewMap.getEdges().forEach(edge -> {
                List<GeoCoordinate> controlPoints = (List) edge.getProperties().get(OspConstants.PROPERTY_CONTROL_POINTS);

                if (viewNode.equals(viewMap.getEdgeSource(edge))) {
                    controlPoints.remove(0);
                    controlPoints.add(0, geoCoordinate);

                    edges.get((BusinessObjectViewEdge) edge).setControlPoints(controlPoints);
                } else if (viewNode.equals(viewMap.getEdgeTarget(edge))) {
                    controlPoints.remove(controlPoints.size() - 1);
                    controlPoints.add(geoCoordinate);

                    edges.get((BusinessObjectViewEdge) edge).setControlPoints(controlPoints);
                }
            });
        }
    }
}
