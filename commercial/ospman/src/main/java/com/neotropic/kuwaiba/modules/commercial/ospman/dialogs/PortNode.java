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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import org.neotropic.util.visual.views.util.BusinessObjectUtil;
import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * Class to port node in the splice box
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PortNode extends MxBusinessObjectNode {
    private final int CHARACTER_LIMIT = 30;
    public static final int WIDTH = 210;
    public static final int HEIGHT = 40;
    private final String FONT_COLOR = "#000000";
    private final int FONT_SIZE = 10;
    private final int SPACING_LEFT = 10;
    private final String FOLDABLE = String.valueOf(0);
    
    private final LinkedHashMap<String, String> PORT_STYLE = new LinkedHashMap();
    {
        PORT_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        PORT_STYLE.put(MxConstants.STYLE_ALIGN, MxConstants.ALIGN_LEFT);
        PORT_STYLE.put(MxConstants.STYLE_SPACING_LEFT, String.valueOf(SPACING_LEFT));
        PORT_STYLE.put(MxConstants.STYLE_FONTCOLOR, FONT_COLOR);
        PORT_STYLE.put(MxConstants.STYLE_FONTSIZE, String.valueOf(FONT_SIZE));
        PORT_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        PORT_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        PORT_STYLE.put(MxConstants.STYLE_FOLDABLE, FOLDABLE);
    }
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
     * Translation Service
     */
    private final TranslationService ts;
    /**
     * The fiber spliced to the port
     */
    private BusinessObjectLight fiber;
    
    private LinkedHashMap<String, String> portStyle = new LinkedHashMap(PORT_STYLE);
    
    private final MxGraph graph;
    
    private String fiberColor;
    
    public PortNode(BusinessObjectLight port, MxGraph graph,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        
        super(port);
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.graph = graph;
        setFiber();
        setGeometry(0, 0, WIDTH, HEIGHT);
        addCellAddedListener(event -> {
            event.unregisterListener();
            
            graph.setCellsLocked(false);
            
            if (getBusinessObject() != null) {
                setLabel(getPortLabel());
                setTooltip(getBusinessObject() != null ? getBusinessObject().getName() : null);
                if (getFiber() != null)
                    spliceFiber();
                else
                    portStyle.put(MxConstants.STYLE_FILLCOLOR, getPortColor());
            }
            setRawStyle(portStyle);
            overrideStyle();
            
            graph.setCellsLocked(true);
        });
        graph.addNode(this);
    }
    public String getFiberColor() {
        return fiberColor;
    }
    public void releasePort() {
        portStyle = new LinkedHashMap(PORT_STYLE);
        portStyle.put(MxConstants.STYLE_FILLCOLOR, getPortColor());
        setRawStyle(portStyle);
        overrideStyle();
        removeOverlayButtons();
        setIsSelectable(true);
        setConnectable(true);
        setFiber(null);
    }
    public void spliceFiber() {
        try {
            if (fiber == null)
                return;
            setIsSelectable(false);
            setConnectable(false);
            fiberColor = BusinessObjectUtil.getBusinessObjectColor(getFiber(), aem, bem, mem);
            portStyle.put(MxConstants.STYLE_FILLCOLOR, getPortColor());
            if (fiberColor != null)
                portStyle.put(MxConstants.STYLE_STROKECOLOR, fiberColor);
            portStyle.put(MxConstants.STYLE_FILL_OPACITY, "25");
        } catch (InventoryException ex) {
            Logger.getLogger(PortNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public BusinessObjectLight getFiber() {
        return fiber;
    }
    public void setFiber(BusinessObjectLight fiber) {
        this.fiber = fiber;
        spliceFiber();
        setRawStyle(portStyle);
    }
    private void setFiber() {
        Objects.requireNonNull(getBusinessObject());
        try {
            HashMap<String, List<BusinessObjectLight>> endpoints = bem.getSpecialAttributes(getBusinessObject().getClassName(), getBusinessObject().getId(),
                OspConstants.SPECIAL_ATTR_ENDPOINT_A, OspConstants.SPECIAL_ATTR_ENDPOINT_B
            );
            if (endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_A) != null && 
                !endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_A).isEmpty())
                fiber = endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_A).get(0);
            else if (endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_B) != null &&
                !endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_B).isEmpty())
                fiber = endpoints.get(OspConstants.SPECIAL_ATTR_ENDPOINT_B).get(0);
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
    private String getPortColor() {
        Objects.requireNonNull(getBusinessObject());
        try {
            ClassMetadata portClass = mem.getClass(getBusinessObject().getClassName());
            return UtilHtml.toHexString(new Color(portClass.getColor()));
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
            return null;
        }
    }
    private String getPortLabel() {
        Objects.requireNonNull(getBusinessObject());
        if (getBusinessObject().getName() != null)
            return getBusinessObject().getName().length() < CHARACTER_LIMIT ? getBusinessObject().getName() : String.format("%s ...", getBusinessObject().getName().substring(0, CHARACTER_LIMIT + 1));
        return null;
    }
}

