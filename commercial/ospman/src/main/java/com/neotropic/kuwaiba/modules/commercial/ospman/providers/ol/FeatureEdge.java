/**
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.providers.ol;

import com.neotropic.flow.component.olmap.Feature;
import com.neotropic.flow.component.olmap.LineString;
import com.neotropic.flow.component.olmap.LineStringCoordinates;
import com.neotropic.flow.component.olmap.PointCoordinates;
import com.neotropic.flow.component.olmap.Properties;
import com.neotropic.flow.component.olmap.VectorSource;
import com.neotropic.flow.component.olmap.style.Fill;
import com.neotropic.flow.component.olmap.style.Stroke;
import com.neotropic.flow.component.olmap.style.Style;
import com.neotropic.flow.component.olmap.style.Text;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ClickEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.PathChangedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.RightClickEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.UnitOfLength;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.ClickEventDispatcher;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.ModifyEndEventDispatcher;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.RightClickEventDispatcher;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * A edge wrapper to features with geometry of type LineString
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FeatureEdge extends Feature implements MapEdge, ClickEventDispatcher, RightClickEventDispatcher,
        ModifyEndEventDispatcher {

    private final BusinessObjectViewEdge viewEdge;
    private final MapProvider mapProvider;
    private final VectorSource vectorSource;
    private final TranslationService ts;
    private boolean clickable = true;

    private final List<ClickEvent.ClickEventListener> clickEventListeners = new ArrayList();
    private final List<RightClickEvent.RightClickEventListener> rightClickEventListeners = new ArrayList();
    private final List<PathChangedEvent.PathChangedEventListener> pathChangedEventListeners = new ArrayList();
    private Text text;
    private Stroke stroke;
    private String color;
    private Double length;
    private boolean visible = true;

    public FeatureEdge(BusinessObjectViewEdge viewEdge, MapProvider mapProvider, VectorSource vectorSource, MetadataEntityManager mem, TranslationService ts,
            String textBackgroundFillColor,
            String styleTextFillColor,
            String selectedStyleTextBackgroundFillColor,
            String styleTextFontSize,
            double styleTextMinZoom) {
        this.viewEdge = viewEdge;
        this.mapProvider = mapProvider;
        this.vectorSource = vectorSource;
        this.ts = ts;

        setId(viewEdge.getIdentifier().getId());

        List<PointCoordinates> coordinates = new ArrayList();
        Properties properties = new Properties() {
            @Override
            public JsonObject toJsonValue() {
                JsonObject properties = Json.createObject();
                Style style = new Style();

                Fill backgroundFill = new Fill();
                backgroundFill.setColor(textBackgroundFillColor);
                if (text == null) {
                    text = new Text();
                    text.setFont(String.format("%s sans-serif", styleTextFontSize));
                    text.setMinZoom(styleTextMinZoom);
                    text.setText(getFormattedText());

                    Fill fill = new Fill();
                    fill.setColor(styleTextFillColor);
                    text.setFill(fill);
                }
                text.setBackgroundFill(backgroundFill);
                style.setText(text);
                try {
                    if (stroke == null) {
                        color = UtilHtml.toHexString(new Color(mem.getClass(viewEdge.getIdentifier().getClassName()).getColor()));
                        stroke = new Stroke();
                        stroke.setColor(color);
                        stroke.setWidth(OspConstants.EDGE_STROKE_WEIGHT);
                    }
                    style.setStroke(stroke);
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ts.getTranslatedString("module.general.messages.unexpected-error"),
                            AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
                properties.put("style", style.toJsonValue());

                backgroundFill.setColor(selectedStyleTextBackgroundFillColor);
                properties.put("selectedStyle", style.toJsonValue());
                return properties;
            }
        };
        setProperties(properties);
        List<GeoCoordinate> controlPoints = (List) viewEdge.getProperties().get(OspConstants.PROPERTY_CONTROL_POINTS);
        controlPoints.forEach(controlPoint -> coordinates.add(
                new PointCoordinates(controlPoint.getLongitude(), controlPoint.getLatitude())
        ));
        setGeometry(new LineString(new LineStringCoordinates(coordinates)));
    }

    public String getColor() {
        return color;
    }

    public Stroke getStroke() {
        return stroke;
    }

    @Override
    public BusinessObjectViewEdge getViewEdge() {
        return this.viewEdge;
    }

    @Override
    public String getEdgeLabel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setEdgeLabel(String label) {//6
        if (mapProvider.getComputeEdgesLength() && label != null) {
            computeLength(null, length -> {
                this.length = length;
                String txt = String.format(
                    ts.getTranslatedString("module.ospman.map.edge.length"), 
                    getFormattedText(), 
                    UnitOfLength.convertMeters(length, mapProvider.getUnitOfLength()), 
                    UnitOfLength.getTranslatedString(mapProvider.getUnitOfLength(), ts)
                );
                text.setText(txt);
                vectorSource.updateFeature(this);
            });
        } else {
            text.setText(getFormattedText());
            vectorSource.updateFeature(this);
        }
    }

    @Override
    public void setControlPoints(List<GeoCoordinate> controlPoints) {
        List<PointCoordinates> coordinates = new ArrayList();
        controlPoints.forEach(controlPoint -> {
            coordinates.add(new PointCoordinates(controlPoint.getLongitude(), controlPoint.getLatitude()));
        });
        setGeometry(new LineString(new LineStringCoordinates(coordinates)));
        vectorSource.updateFeature(this);
    }

    @Override
    public boolean getClickableEdge() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setClickableEdge(boolean clickable) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getEditableEdge() {//5
        return false;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setEditableEdge(boolean editable) {//4
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getPlayAnimation() {
        return true;
    }

    @Override
    public void setPlayAnimation(boolean playAnimation) {
        if (playAnimation) {
            vectorSource.animateFeature(this);
        }
    }

    @Override
    public void getEdgeLabelPosition(Consumer<GeoCoordinate> consumerEdgeLabelPosition) {
        this.vectorSource.getMap().getElement().executeJs("return this.toLonLat(this.getFeature($0).getGeometry().getCoordinateAt(0.5));", this.toJsonValue()).then(JsonArray.class, result -> {
            double x = result.getNumber(0);
            double y = result.getNumber(1);
            GeoCoordinate geoCoordinate = new GeoCoordinate(y, x);
            consumerEdgeLabelPosition.accept(geoCoordinate);
        });
    }

    @Override
    public boolean getEdgeVisible() {
        return visible;
    }

    @Override
    public void setEdgeVisible(boolean visible) {
        if (!(this.visible == visible)) {
            this.visible = visible;
            if (visible) {
                vectorSource.addFeature(this);
            } else {
                vectorSource.removeFeature(this);
            }
        }
    }

    @Override
    public Double getLength() {
        return length;
    }

    @Override
    public void setLength(Double length) {
        this.length = length;
    }

    @Override
    public void computeLength(List<GeoCoordinate> controlPoints, Consumer<Double> consumerLength) {
        Objects.requireNonNull(consumerLength);
        vectorSource.getMap().getElement()
                .executeJs("return this.getLength($0);", this.toJsonValue())
                .then(Double.class, length -> consumerLength.accept(length));
    }

    @Override
    public void addClickEventListener(ClickEvent.ClickEventListener clickEventListener) {//1
        clickEventListeners.add(clickEventListener);
    }

    @Override
    public void removeClickEventListener(ClickEvent.ClickEventListener clickEventListener) {
        clickEventListeners.removeIf(l -> l.equals(clickEventListener));
    }

    @Override
    public void removeAllClickEventListeners() {
        clickEventListeners.clear();
    }

    @Override
    public void addRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener) {//2
        rightClickEventListeners.add(rightClickEventListener);
    }

    @Override
    public void removeRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener) {
        rightClickEventListeners.removeIf(l -> l.equals(rightClickEventListener));
    }

    @Override
    public void removeAllRightClickEventListeners() {
        rightClickEventListeners.clear();
    }

    @Override
    public void addPathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener) {//3
        pathChangedEventListeners.add(pathChangedEventListener);
    }

    @Override
    public void removePathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener) {
        pathChangedEventListeners.removeIf(l -> l.equals(pathChangedEventListener));
    }

    @Override
    public void removeAllPathChangedEventListeners() {
        pathChangedEventListeners.clear();
    }

    @Override
    public void fireClickEvent() {
        new ArrayList<>(clickEventListeners).forEach(listener -> {
            if (clickable && clickEventListeners.contains(listener)) {
                listener.accept(new ClickEvent(listener));
            }
        });
    }

    @Override
    public void fireRightClickEvent() {
        new ArrayList<>(rightClickEventListeners).forEach(listener -> {
            if (clickable && rightClickEventListeners.contains(listener)) {
                listener.accept(new RightClickEvent(listener));
            }
        });
    }

    @Override
    public void fireModifyEndEvent(JsonObject feature) {
        List<GeoCoordinate> geoCoordinates = new ArrayList();

        JsonArray coordinatesArray = feature.getObject("geometry").getArray("coordinates");
        List<PointCoordinates> coordinates = new ArrayList();
        for (int i = 0; i < coordinatesArray.length(); i++) {
            JsonArray c = coordinatesArray.getArray(i);
            double x = c.getNumber(0);
            double y = c.getNumber(1);
            coordinates.add(new PointCoordinates(x, y));
            geoCoordinates.add(new GeoCoordinate(y, x));
        }
        setGeometry(new LineString(new LineStringCoordinates(coordinates)));

        new ArrayList<>(pathChangedEventListeners).forEach(listener -> {
            if (pathChangedEventListeners.contains(listener)) {
                listener.accept(new PathChangedEvent(geoCoordinates, listener));
            }
        });
    }
    
    private String getFormattedText() {
        BusinessObjectLight businessObject = getViewEdge().getIdentifier();
        if (businessObject.getValidators() != null) {
            StringBuilder prefixBuilder = new StringBuilder();
            StringBuilder suffixBuilder = new StringBuilder();

            businessObject.getValidators().forEach(validator -> {
                if (validator.getProperties() != null) {
                    if (validator.getProperties().containsKey(Validator.PROPERTY_PREFIX))
                        prefixBuilder.append(String.format("%s ", validator.getProperties().getProperty(Validator.PROPERTY_PREFIX)));
                    if (validator.getProperties().containsKey(Validator.PROPERTY_SUFFIX))
                        suffixBuilder.append(String.format("%s ", validator.getProperties().getProperty(Validator.PROPERTY_SUFFIX)));
                }
            });
            return String.format("%s%s %s", prefixBuilder.toString(), businessObject.getName(), suffixBuilder.toString());
        }
        return businessObject.getName();
    }
}
