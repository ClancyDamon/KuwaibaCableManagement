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
package org.neotropic.kuwaiba.core.apis.integration.views;

import com.vaadin.flow.component.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;

/**
 * Defines the behavior of views that can be plugged and played such as End to End views.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <C> The class of the visual component returned by this view.
 */
public abstract class AbstractView<C extends Component> {
    /**
     * The default view map. This view map must be created when either {@link  #buildEmptyView()} or {@link  #buildWithBusinessObject(java.lang.Object)} is called.
     */
    protected ViewMap viewMap;
    /**
     * The properties associated an instance of the view, typically an id, a name and a description.
     */
    protected Properties properties;
    /**
     * List of selection events.
     */
    protected List<ViewEventListener> lstSelectionEvents;
    /**
     * List for deselect events.
     */
    protected List<ViewEventListener> lstDeselectEvents;
    
    public AbstractView() {
        this.properties = new Properties();
        this.lstDeselectEvents = new ArrayList<>();
        this.lstSelectionEvents = new ArrayList<>();
    }
    
    /**
     * Cleans the view so some other thing can be painted on the canvas. Typically this is used in 
     * refresh buttons.
     */
    public abstract void clean();
    
    /**
     * The properties associated to an instance of the view, typically an id, a name and a description.
     * @return The set or properties.
     */
    public Properties getProperties() {
        return this.properties;
    }     
    
    /**
     * Exports the view to XML. It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return A byte array with an XML document representing the view. The format of the document must follow the Standard Kuwaiba View Text Format (SKTF)
     */
    public abstract byte[] getAsXml();
    /**
     * Exports the view as image using the given exporter. It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @param exporter The exporter used to generate the byte array.
     * @return A byte array with a formated byte array of the view.
     */
    public abstract byte[] getAsImage(AbstractImageExporter exporter);
    /**
     * Gets an embeddable visual component that can be rendered in a dashboard.It most likely will have to be called after calling {@link #build()} or {@link #build(java.lang.Object)}.
     * @return An embeddable component (Panel, VerticalLayout, etc)
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException If the component could not be created for some reason (most likely, misconfiguration).
     */
    public abstract C getAsUiElement() throws InvalidArgumentException;
    /**
     * Exports the view as a ViewMap (a representation of the view as a set of Java objects related to each other). It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return The view map of the view.
     */
    public ViewMap getAsViewMap() {
        return viewMap;
    }
    /**
     * Builds the view. Call this method if no business object is required to build the view. It just loads the elements from an view definition structure (like an XML document).
     * @param view The view to be rendered.
     */
    public abstract void buildFromSavedView(byte[] view);
    /**
     * Adds a node to views that are not generated automatically.
     * @param businessObject The business object behind the node to be added. Nodes that already exist will not be added.
     * @param properties The properties associated to this object, such as the location that will be used to place it or the URL of the icon that will represent the node.
     * @return A reference to the newly added node.
     */
    public abstract AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties);
    /**
     * Adds an edge to views that are not generated automatically.
     * @param businessObject The business object behind the edge to be added. Edges that already exist will not be added.
     * @param sourceBusinessObject The business object behind the source node to the edge to be created. 
     * @param targetBusinessObject The business object behind the target node to the edge to be created.
     * @param properties The properties associated to this object, such as the control points of the edge, or its color.
     * @return A reference to the newly added edge.
     */
    public abstract AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, 
            BusinessObjectLight targetBusinessObject, Properties properties);
    /**
     * remove a node from the viewMap
     * @param businessObject The business object behind the node to be removed
     */
    public abstract void removeNode(BusinessObjectLight businessObject);
    /**
     * remove a edge from the viewMap
     * @param businessObject The business object behind the edge to be removed
     */
    public abstract void removeEdge(BusinessObjectLight businessObject);
    /**
     * Adds a listener to the node click events.
     * @param listener The listener object.
     */   
    public abstract void nodeClickListener(ViewEventListener listener);
    /**
     * Adds a listener to the edge click events.
     * @param listener The listener object.
     */
    public abstract void edgeClickListener(ViewEventListener listener);
    /**
     * Adds a listener to any selection event.
     * @param listener The listener object.
     */
    public void addSelectionListener(ViewEventListener listener) {
        if (listener != null)
            lstSelectionEvents.add(listener);
    }
    /**
     * Adds a deselect to any selection event.
     * @param listener The listener object.
     */
    public void addDeselectionListener(ViewEventListener listener){
        if (listener != null)
            lstDeselectEvents.add(listener);
    }
    /**
     * remove selection listener.
     * @param listener The listener object.
     */
    public void removeSelectionListener(ViewEventListener listener) {
        if (listener != null)
            lstSelectionEvents.remove(listener);
    }
    /**
     * remove a deselect listener.
     * @param listener The listener object.
     */
    public void removeDeselectListener(ViewEventListener listener){
        if (listener != null)
            lstDeselectEvents.remove(listener);
    }
    /**
     * Fire all selection events with the given data.
     * @param source The selected object
     */
    public void fireSelectionEvents(Object source) {
        lstSelectionEvents.forEach(item -> item.eventProcessed(source, ViewEventListener.EventType.TYPE_CLICK));
    }
    /**
     * Fire all deselect events with the given data.
     * @param source The deselected object
     */
    public void fireDeselectEvents(Object source) {
        lstDeselectEvents.forEach(item -> item.eventProcessed(source, ViewEventListener.EventType.TYPE_CLICK));
    }
}