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

import com.neotropic.flow.component.olmap.Feature;
import com.neotropic.flow.component.olmap.LineString;
import com.neotropic.flow.component.olmap.LineStringCoordinates;
import com.neotropic.flow.component.olmap.OlMap;
import com.neotropic.flow.component.olmap.PointCoordinates;
import com.neotropic.flow.component.olmap.Properties;
import com.neotropic.flow.component.olmap.VectorSource;
import com.neotropic.flow.component.olmap.interaction.Modify;
import com.neotropic.flow.component.olmap.interaction.Select;
import com.neotropic.flow.component.olmap.style.Stroke;
import com.neotropic.flow.component.olmap.style.Style;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.WindowDrawContainerTools;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Helper to draw a edge on open layers
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OlEdgeHelper {

    private FeatureNode source;
    private FeatureNode target;
    private final Feature lineString;

    private final TranslationService ts;
    private final BiConsumer<HashMap<String, Object>, Runnable> consumerEdgeComplete;
    private final OlMap olMap;
    private final Modify modify;
    private final Select select;
    private final VectorSource vectorSource;
    private final List<PointCoordinates> coordinates = new ArrayList();
    private final List<PointCoordinates> tmpCoordinates = new ArrayList();
    private final List<Registration> registrations = new ArrayList();

    protected OlEdgeHelper(BiConsumer<HashMap<String, Object>, Runnable> consumerEdgeComplete,
            OlMap olMap, Modify modify, Select select, VectorSource vectorSource, TranslationService ts) {
        this.consumerEdgeComplete = consumerEdgeComplete;
        this.olMap = olMap;
        this.modify = modify;
        this.select = select;
        this.vectorSource = vectorSource;
        this.lineString = getFeatureLineString();
        this.ts = ts;
    }

    private Feature getFeatureLineString() {
        Feature featureLineString = new Feature();
        featureLineString.setId(UUID.randomUUID().toString());
        featureLineString.setGeometry(new LineString(new LineStringCoordinates(tmpCoordinates)));
        Properties properties = new Properties() {
            @Override
            public JsonObject toJsonValue() {
                JsonObject properties = Json.createObject();
                Style style = new Style();
                Stroke stroke = new Stroke();
                stroke.setColor("red");
                stroke.setWidth(OspConstants.EDGE_STROKE_WEIGHT);
                style.setStroke(stroke);

                properties.put("style", style.toJsonValue());
                properties.put("selectedStyle", style.toJsonValue());
                return properties;
            }
        };
        featureLineString.setProperties(properties);
        return featureLineString;
    }

    private void updateLineString() {
        if (tmpCoordinates.size() >= 2) {
            if (vectorSource.getFeatureById(lineString.getId()) == null) {
                vectorSource.addFeature(lineString);
            } else {
                vectorSource.updateFeature(lineString);
            }
        }
    }

    public void init() {
        cancel();
        modify.setActive(false);
        olMap.updateInteraction(modify);

        registrations.add(olMap.addMapPointerMoveListener(event -> {
            if (source != null) {
                tmpCoordinates.clear();
                tmpCoordinates.addAll(coordinates);
                tmpCoordinates.add(new PointCoordinates(event.getCoordinate().getX(), event.getCoordinate().getY()));
                updateLineString();
            }
        }));
        registrations.add(olMap.addMapSingleClickListener(event -> {
            if (source != null) {
                coordinates.add(new PointCoordinates(event.getCoordinate().getX(), event.getCoordinate().getY()));
                updateLineString();
            }
        }));
        registrations.add(olMap.addMapViewportContextMenu(event -> {
            WindowDrawContainerTools wdw = new WindowDrawContainerTools(
                    tmpCoordinates, () -> {
                        coordinates.remove(coordinates.size() - 1);
                        updateLineString();
                    }, () -> init(), ts);
            wdw.open();
        }));
        registrations.add(select.addSelectListener(event -> {
            event.getFeatureSelectedIds().forEach(featureSelectedId -> {
                Feature feature = vectorSource.getFeatureById(featureSelectedId);
                if (feature instanceof FeatureNode) {
                    if (source == null) {
                        source = (FeatureNode) feature;
                        coordinates.add((PointCoordinates) source.getGeometry().getCoordinates());
                    } else if (target == null) {
                        target = (FeatureNode) feature;

                        coordinates.remove(coordinates.size() - 1);
                        coordinates.add((PointCoordinates) target.getGeometry().getCoordinates());
                        List<GeoCoordinate> controlPoints = new ArrayList();
                        coordinates.forEach(coordinate -> controlPoints.add(new GeoCoordinate(coordinate.getY(), coordinate.getX())));

                        HashMap<String, Object> parameters = new HashMap();
                        parameters.put(OspConstants.BUSINESS_OBJECT_SOURCE, source.getViewNode().getIdentifier());
                        parameters.put(OspConstants.BUSINESS_OBJECT_TARGET, target.getViewNode().getIdentifier());
                        parameters.put(OspConstants.PROPERTY_CONTROL_POINTS, controlPoints);

                        consumerEdgeComplete.accept(parameters, () -> init());
                    }
                }
            });
        }));
    }

    public void cancel() {
        source = null;
        target = null;
        coordinates.clear();
        tmpCoordinates.clear();

        registrations.forEach(registration -> registration.remove());
        registrations.clear();

        modify.setActive(true);
        olMap.updateInteraction(modify);

        vectorSource.removeFeature(lineString);
    }
}
