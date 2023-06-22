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
package com.neotropic.kuwaiba.modules.commercial.ospman.providers.ol;

import com.neotropic.flow.component.olmap.AbstractTileLayerSource;
import com.neotropic.flow.component.olmap.Coordinate;
import com.neotropic.flow.component.olmap.Feature;
import com.neotropic.flow.component.olmap.GeometryType;
import com.neotropic.flow.component.olmap.OlMap;
import com.neotropic.flow.component.olmap.VectorLayer;
import com.neotropic.flow.component.olmap.VectorSource;
import com.neotropic.flow.component.olmap.ViewOptions;
import com.neotropic.flow.component.olmap.interaction.Draw;
import com.neotropic.flow.component.olmap.interaction.FeatureContextMenu;
import com.neotropic.flow.component.olmap.interaction.Modify;
import com.neotropic.flow.component.olmap.interaction.Select;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantService;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.Heatmap;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapSelectionManager;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapSelectionManager.EdgeSelectionManager;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapSelectionManager.NodeSelectionManager;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.UnitOfLength;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ViewHeatmap;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.ClickEventDispatcher;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.ModifyEndEventDispatcher;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.RightClickEventDispatcher;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;

/**
 * Map provider implementation to OpenLayers
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>} ToDo:
 * 3, 7, 14 14/18
 */
public abstract class AbstractOlTileLayerProvider implements MapProvider {

    /**
     * Set of zoom changed event listeners.
     */
    private final List<ZoomChangedEvent.ZoomChangedEventListener> zoomChangedEventListeners = new ArrayList();
    /**
     * Set of idle event listeners.
     */
    private final List<IdleEvent.IdleEventListener> idleEventListeners = new ArrayList();
    /**
     * Set of mouse move event listener.
     */
    private final List<MouseMoveEvent.MouseMoveEventListener> mouseMoveEventListeners = new ArrayList();

    private final List<RightClickEvent.RightClickEventListener> rightClickEventListeners = new ArrayList();

    private NodeSelectionManager nodeSelectionManager;

    private EdgeSelectionManager edgeSelectionManager;

    private UnitOfLength unitOfLength;
    private boolean computeEdgesLength;
    private double minZoomForLabels;

    private OlMap olMap;
    private VectorLayer vectorLayer;
    private VectorSource vectorSource;
    private ViewOptions viewOptions;
    private Select select;
    private Modify modify;
    private Draw drawPoint;
    private Registration drawEndRegistration;

    private MetadataEntityManager mem;
    private TranslationService ts;
    private ResourceFactory resourceFactory;

    private String fillColorForNodeLabels;
    private String fillColorForEdgeLabels;
    private String fillColorForSelectedNodeLabels;
    private String fillColorForSelectedEdgeLabels;
    private String colorForLabels;
    private String fontSizeForLabels;

    private OlEdgeHelper edgeHelper;
    private OlPathSelectionHelper pathSelectionHelper;
    private final HashMap<BusinessObjectViewNode, FeatureNode> nodes = new HashMap();
    private final HashMap<BusinessObjectViewEdge, FeatureEdge> edges = new HashMap();

    private AbstractTileLayerSource tileLayerSource;    
    
    public AbstractOlTileLayerProvider() {
    }

    public AbstractOlTileLayerProvider(AbstractTileLayerSource tileLayerSource) {
        this.tileLayerSource = tileLayerSource;
    }

    public AbstractTileLayerSource getTileLayerSource() {
        return tileLayerSource;
    }

    public void setTileLayerSource(AbstractTileLayerSource tileLayerSource) {
        this.tileLayerSource = tileLayerSource;
    }

    @Override
    public void createComponent(ApplicationEntityManager aem, MetadataEntityManager mem, ResourceFactory resourceFactory, TranslationService ts) {
        this.viewOptions = new ViewOptions(new Coordinate(0, 0), 0);
        this.olMap = new OlMap(tileLayerSource, viewOptions);
        vectorLayer = new VectorLayer(olMap);
        vectorSource = new VectorSource();
        vectorLayer.setSource(vectorSource);
        this.olMap.getLayers().add(vectorLayer);
        this.resourceFactory = resourceFactory;
        this.mem = mem;
        this.ts = ts;

        this.olMap.addLoadCompleteListener(event -> {
            viewOptions.addViewChangeResolutionListener(e -> {
                new ArrayList<>(zoomChangedEventListeners).forEach(listener -> {
                    if (zoomChangedEventListeners.contains(listener)) {
                        listener.accept(new ZoomChangedEvent(listener));
                    }
                });
            });
            modify = new Modify(olMap);
            modify.addModifyEndListener(evt -> {
                JsonArray features = evt.getFeatures().getArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JsonObject featureObject = features.getObject(i);
                    String id = featureObject.getString("id");
                    Feature feature = vectorSource.getFeatureById(id);
                    if (feature instanceof ModifyEndEventDispatcher) {
                        ((ModifyEndEventDispatcher) feature).fireModifyEndEvent(featureObject);
                    }
                }
            });
            olMap.addInteraction(modify);

            drawPoint = new Draw(GeometryType.Point, olMap);
            drawPoint.setActive(false);
            olMap.addInteraction(drawPoint);
        });
        this.olMap.addMapMoveendListener(event -> {
            new ArrayList<>(idleEventListeners).forEach(listener -> {
                if (idleEventListeners.contains(listener)) {
                    listener.accept(new IdleEvent(listener));
                }
            });
        });
        this.olMap.addMapPointerMoveListener(event -> {
            new ArrayList<>(mouseMoveEventListeners).forEach(listener -> {
                if (mouseMoveEventListeners.contains(listener)) {
                    listener.accept(new MouseMoveEvent(listener, event.getCoordinate().getY(), event.getCoordinate().getX()));
                }
            });
        });
        this.olMap.addMapViewportContextMenu(event -> {
            new ArrayList<>(rightClickEventListeners).forEach(listener -> {
                if (rightClickEventListeners.contains(listener)) {
                    listener.accept(new RightClickEvent(listener, event.getCoordinate().getY(), event.getCoordinate().getX()));
                }
            });
        });
        select = new Select(olMap);
        select.addSelectListener(event -> {
            event.getFeatureSelectedIds().forEach(featureSelectedId -> {
                Feature selectedFeature = vectorSource.getFeatureById(featureSelectedId);
                if (selectedFeature instanceof ClickEventDispatcher) {
                    ((ClickEventDispatcher) selectedFeature).fireClickEvent();
                }
            });
        });
        FeatureContextMenu featureContextMenu = new FeatureContextMenu(olMap);
        featureContextMenu.addFeatureContextMenuListener(event -> {
            Feature feature = vectorSource.getFeatureById(event.getFeatureId());
            if (feature instanceof RightClickEventDispatcher) {
                ((RightClickEventDispatcher) feature).fireRightClickEvent();
            }
        });
        nodeSelectionManager = new OlNodeSelectionManager();
        edgeSelectionManager = new OlEdgeSelectionManager();

        try {
            setMinZoomForLabels(Double.valueOf(String.valueOf(aem.getConfigurationVariableValue("module.ospman.minZoomForLabels"))));
        } catch (InventoryException ex) {
            setMinZoomForLabels(OutsidePlantService.DEFAULT_MIN_ZOOM_FOR_LABELS);
        }

        try {
            colorForLabels = (String) aem.getConfigurationVariableValue("module.ospman.colorForLabels");
        } catch (InventoryException ex) {
            colorForLabels = OutsidePlantService.DEFAULT_COLOR_FOR_LABELS;
        }

        try {
            fontSizeForLabels = (String) aem.getConfigurationVariableValue("module.ospman.fontSizeForLabels");
        } catch (InventoryException ex) {
            fontSizeForLabels = OutsidePlantService.DEFAULT_FONT_SIZE_FOR_LABELS;
        }

        try {
            fillColorForNodeLabels = (String) aem.getConfigurationVariableValue("module.ospman.fillColorForNodeLabels");
        } catch (InventoryException ex) {
            fillColorForNodeLabels = OutsidePlantService.DEFAULT_FILL_COLOR_FOR_LABELS;
        }

        try {
            fillColorForEdgeLabels = (String) aem.getConfigurationVariableValue("module.ospman.fillColorForEdgeLabels");
        } catch (InventoryException ex) {
            fillColorForEdgeLabels = OutsidePlantService.DEFAULT_FILL_COLOR_FOR_LABELS;
        }

        try {
            fillColorForSelectedNodeLabels = (String) aem.getConfigurationVariableValue("module.ospman.fillColorForSelectedNodeLabels");
        } catch (InventoryException ex) {
            fillColorForSelectedNodeLabels = OutsidePlantService.DEFAULT_FILL_COLOR_FOR_LABELS;
        }

        try {
            fillColorForSelectedEdgeLabels = (String) aem.getConfigurationVariableValue("module.ospman.fillColorForSelectedEdgeLabels");
        } catch (InventoryException ex) {
            fillColorForSelectedEdgeLabels = OutsidePlantService.DEFAULT_FILL_COLOR_FOR_LABELS;
        }

    }

    @Override
    public Component getComponent() {
        return olMap;
    }

    @Override
    public GeoCoordinate getCenter() {
        return new GeoCoordinate(viewOptions.getCenter().getY(), viewOptions.getCenter().getX());
        //13
    }

    @Override
    public void setCenter(GeoCoordinate center) {
        viewOptions.setCenter(new Coordinate(center.getLongitude(), center.getLatitude()));
//        1
    }

    @Override
    public double getZoom() {
        return viewOptions.getZoom();
        //11
    }

    @Override
    public void setZoom(double zoom) {
        viewOptions.setZoom(zoom);
//        2
    }

    @Override
    public double getMinZoomForLabels() {
        //12
        return minZoomForLabels;
    }

    @Override
    public void setMinZoomForLabels(double minZoomForLabels) {
        this.minZoomForLabels = minZoomForLabels;
    }

    @Override
    public String getMapTypeId() {
        return null;
        //14
    }

    @Override
    public void setMapTypeId(String mapTypeId) {
//        3
    }

    @Override
    public List<String> getMapTypeIds() {
        return new ArrayList();
//        7
    }

    @Override
    public void setHandMode() {
        if (drawEndRegistration != null) {
            drawEndRegistration.remove();
            drawEndRegistration = null;
        }
        drawPoint.setActive(false);
        olMap.updateInteraction(drawPoint);

        if (edgeHelper != null) {
            edgeHelper.cancel();
        }
        if (pathSelectionHelper != null) {
            pathSelectionHelper.cancel();
        }
        olMap.setMeasuring(false);
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDrawingMarkerMode(Consumer<GeoCoordinate> drawingMarkerComplete) {
        setHandMode();
        if (drawingMarkerComplete != null) {
            drawPoint.setActive(true);
            olMap.updateInteraction(drawPoint);

            drawEndRegistration = drawPoint.addDrawEndListener(event -> {
                drawPoint.setActive(false);
                olMap.updateInteraction(drawPoint);
                event.unregisterListener();
                drawEndRegistration = null;

                JsonArray coordinates = event.getFeature().getObject("geometry").getArray("coordinates");
                double lon = coordinates.getNumber(0);
                double lat = coordinates.getNumber(1);
                drawingMarkerComplete.accept(new GeoCoordinate(lat, lon));
            });
        }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDrawingEdgeMode(BiConsumer<HashMap<String, Object>, Runnable> callbackEdgeComplete) {
        setHandMode();
        if (edgeHelper == null) {
            edgeHelper = new OlEdgeHelper(callbackEdgeComplete, olMap, modify, select, vectorSource, ts);
        }
        edgeHelper.init();
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPathSelectionMode(BiConsumer<List<BusinessObjectViewEdge>, Runnable> callbackPathSelectionComplete) {
        setHandMode();
        if (pathSelectionHelper == null) {
            pathSelectionHelper = new OlPathSelectionHelper(olMap, modify, select, vectorSource);
        }
        pathSelectionHelper.setConsumerPathSelectionComplete(callbackPathSelectionComplete);
        pathSelectionHelper.init();
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setMeasureMode() {
        setHandMode();
        olMap.setMeasuring(true);
        olMap.getElement().executeJs("this.measure($0)", getUnitOfLength().toJson(3, ts));
    }

    @Override
    public boolean getComputeEdgesLength() {
        return computeEdgesLength;
    }

    @Override
    public void setComputeEdgesLength(boolean computeEdgesLength) {
        this.computeEdgesLength = computeEdgesLength;
//        9
    }

    @Override
    public UnitOfLength getUnitOfLength() {
        return unitOfLength;
    }

    @Override
    public void setUnitOfLength(UnitOfLength unitOfLength) {
        this.unitOfLength = unitOfLength;
        olMap.getElement().executeJs("this.measure($0)", getUnitOfLength().toJson(3, ts));
//        8
    }

    @Override
    public MapSelectionManager.NodeSelectionManager getNodeSelectionManager() {//18
        return nodeSelectionManager;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MapSelectionManager.EdgeSelectionManager getEdgeSelectionManager() {//17
        return edgeSelectionManager;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addIdleEventListener(IdleEvent.IdleEventListener listener) {
        idleEventListeners.add(listener);
//        5
    }

    @Override
    public void removeIdleEventListener(IdleEvent.IdleEventListener listener) {
        idleEventListeners.removeIf(l -> l.equals(listener));
    }

    @Override
    public void removeAllIdleEventListener() {
        idleEventListeners.clear();
    }

    @Override
    public void addZoomChangedEventListener(ZoomChangedEvent.ZoomChangedEventListener listener) {
        zoomChangedEventListeners.add(listener);
//        4
    }

    @Override
    public void removeZoomChangedEventListener(ZoomChangedEvent.ZoomChangedEventListener listener) {
        zoomChangedEventListeners.removeIf(l -> l.equals(listener));
    }

    @Override
    public void removeAllsZoomChangedEventListener() {
        zoomChangedEventListeners.clear();
    }

    @Override
    public void addMouseMoveEventListener(MouseMoveEvent.MouseMoveEventListener listener) {
        mouseMoveEventListeners.add(listener);
//        6
    }

    @Override
    public void removeMouseMoveEventListener(MouseMoveEvent.MouseMoveEventListener listener) {
        mouseMoveEventListeners.removeIf(l -> l.equals(listener));
    }

    @Override
    public void removeAllMouseMoveEventListener() {
        mouseMoveEventListeners.clear();
    }

    @Override
    public void addRightClickEventListener(RightClickEvent.RightClickEventListener listener) {
        rightClickEventListeners.add(listener);
//        10
    }

    @Override
    public void removeRightClickEventListener(RightClickEvent.RightClickEventListener listener) {
        rightClickEventListeners.removeIf(l -> l.equals(listener));
    }

    @Override
    public void removeAllRightClickEventListener() {
        rightClickEventListeners.clear();
    }

    @Override
    public MapNode addNode(BusinessObjectViewNode viewNode) {
        FeatureNode node = new FeatureNode(viewNode, vectorSource, resourceFactory,
                fillColorForNodeLabels, colorForLabels, fillColorForSelectedNodeLabels,
                fontSizeForLabels, getMinZoomForLabels());
        vectorSource.addFeature(node);
        nodes.put(viewNode, node);
        //15
        return node;
    }

    @Override
    public MapEdge addEdge(BusinessObjectViewEdge viewEdge) {
        FeatureEdge edge = new FeatureEdge(viewEdge, this, vectorSource, mem, ts,
                fillColorForEdgeLabels, colorForLabels, fillColorForSelectedEdgeLabels,
                fontSizeForLabels, getMinZoomForLabels());
        vectorSource.addFeature(edge).then(Boolean.class,
                added -> edge.setEdgeLabel(viewEdge.getIdentifier().getName()));
        edges.put(viewEdge, edge);
        // 16
        return edge;
    }

    @Override
    public void removeNode(BusinessObjectViewNode viewNode) {
        FeatureNode node = nodes.remove(viewNode);
        vectorSource.removeFeature(node);
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeEdge(BusinessObjectViewEdge viewEdge) {
        FeatureEdge edge = edges.remove(viewEdge);
        vectorSource.removeFeature(edge);
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Heatmap addHeatmap(ViewHeatmap viewHeatmap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeHeatmap(ViewHeatmap viewHeatmap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
