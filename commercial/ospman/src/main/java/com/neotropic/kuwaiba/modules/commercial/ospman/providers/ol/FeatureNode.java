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
import com.neotropic.flow.component.olmap.Point;
import com.neotropic.flow.component.olmap.PointCoordinates;
import com.neotropic.flow.component.olmap.Properties;
import com.neotropic.flow.component.olmap.VectorSource;
import com.neotropic.flow.component.olmap.style.Fill;
import com.neotropic.flow.component.olmap.style.Icon;
import com.neotropic.flow.component.olmap.style.Style;
import com.neotropic.flow.component.olmap.style.Text;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ClickEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.PositionChangedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.RightClickEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.ClickEventDispatcher;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.ModifyEndEventDispatcher;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.eventDispatcher.RightClickEventDispatcher;
import com.vaadin.flow.server.StreamResourceRegistry;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;

/**
 * A node wrapper to features with geometry of type point. 1/7
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FeatureNode extends Feature implements MapNode, ClickEventDispatcher, RightClickEventDispatcher,
        ModifyEndEventDispatcher {

    private final BusinessObjectViewNode viewNode;
    private final VectorSource vectorSource;
    private final List<ClickEvent.ClickEventListener> clickEventListeners = new ArrayList();
    private final List<RightClickEvent.RightClickEventListener> rightClickEventListeners = new ArrayList();
    private final List<PositionChangedEvent.PositionChangedEventListener> positionChangedEventListeners = new ArrayList();

    private boolean clickable = true;
    private Text text;
    private boolean visible = true;

    public FeatureNode(BusinessObjectViewNode viewNode, VectorSource vectorSource, ResourceFactory resourceFactory,
            String styleTextBackgroundFillColor,
            String styleTextFillColor,
            String selectedStyleTextBackgroundFillColor,
            String styleTextFontSize,
            double styleTextMinZoom) {
        this.viewNode = viewNode;
        this.vectorSource = vectorSource;

        setId(viewNode.getIdentifier().getId());
        double x = (double) viewNode.getProperties().get(OspConstants.ATTR_LON);
        double y = (double) viewNode.getProperties().get(OspConstants.ATTR_LAT);
        setGeometry(new Point(new PointCoordinates(x, y)));
        Properties properties = new Properties() {
            @Override
            public JsonObject toJsonValue() {
                JsonObject properties = Json.createObject();
                Style style = new Style();

                Icon image = new Icon();
                image.setSrc(StreamResourceRegistry.getURI(resourceFactory.getClassIcon(viewNode.getIdentifier().getClassName())).toString());
                style.setImage(image);

                text = new Text();
                text.setFont(String.format("%s sans-serif", styleTextFontSize));
                text.setMinZoom(styleTextMinZoom);
                text.setText(getFormattedText(viewNode.getIdentifier()));

                Fill fill = new Fill();
                fill.setColor(styleTextFillColor);
                text.setFill(fill);

                Fill backgroundFill = new Fill();
                backgroundFill.setColor(styleTextBackgroundFillColor);
                text.setBackgroundFill(backgroundFill);

                style.setText(text);

                properties.put("style", style.toJsonValue());

                backgroundFill.setColor(selectedStyleTextBackgroundFillColor);
                properties.put("selectedStyle", style.toJsonValue());
                return properties;
            }
        };
        setProperties(properties);
    }

    @Override
    public BusinessObjectViewNode getViewNode() {
        return viewNode;
    }

    @Override
    public void setPosition(GeoCoordinate position) {
        setGeometry(new Point(new PointCoordinates(
                position.getLongitude(), position.getLatitude())));
        vectorSource.updateFeature(this);
    }

    @Override
    public String getNodeLabel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNodeLabel(String label) {//6
        text.setText(label);
        vectorSource.updateFeature(this);
    }

    @Override
    public String getNodeTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNodeTitle(String title) {//5
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getClickableNode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setClickableNode(boolean clickable) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getDraggableNode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDraggableNode(boolean draggable) {//3
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
    public boolean getNodeVisible() {
        return visible;
    }

    @Override
    public void setNodeVisible(boolean visible) {
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
    public void addPositionChangedEventListener(PositionChangedEvent.PositionChangedEventListener positionChangedEventListener) {//4
        positionChangedEventListeners.add(positionChangedEventListener);
    }

    @Override
    public void removePositionChangedEventListener(PositionChangedEvent.PositionChangedEventListener positionChangedEventListener) {
        positionChangedEventListeners.removeIf(l -> l.equals(positionChangedEventListener));
    }

    @Override
    public void removeAllPositionChangedEventListeners() {
        positionChangedEventListeners.clear();
    }

    @Override
    public void fireClickEvent() {//7
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
        JsonArray coordinates = feature.getObject("geometry").getArray("coordinates");
        double x = coordinates.getNumber(0);
        double y = coordinates.getNumber(1);
        setGeometry(new Point(new PointCoordinates(x, y)));
        new ArrayList<>(positionChangedEventListeners).forEach(listener -> {
            if (positionChangedEventListeners.contains(listener)) {
                listener.accept(new PositionChangedEvent(y, x, listener));
            }
        });
    }

    private String getFormattedText(BusinessObjectLight businessObject) {
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
