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
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;

/**
 * Represents a fiber in the mid-span access and splicing view
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FiberNode extends TreeLayoutNode {
    private final BusinessObjectLight fiberObject;
    private final BusinessEntityManager bem;
    
    private final LinkedHashMap<String, String> fiberStyle = new LinkedHashMap();
    {
        fiberStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        fiberStyle.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        fiberStyle.put(MxConstants.STYLE_LABEL_POSITION, MxConstants.ALIGN_CENTER);
        fiberStyle.put(MxConstants.STYLE_VERTICAL_LABEL_POSITION, MxConstants.ALIGN_TOP);
        fiberStyle.put(MxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white"); //NOI18N
        fiberStyle.put(MxConstants.STYLE_FONTCOLOR, "black"); //NOI18N
        fiberStyle.put(MxConstants.STYLE_FONTSIZE, "11");
    }
    private final String fiberColor;
    private final boolean leftover;
    
    public FiberNode(BusinessObjectLight fiberObject, TreeLayout treeLayout, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, 
        PhysicalConnectionsService physicalConnectionsService, Consumer<BusinessObjectLight> consumerObjectChange) 
        throws InventoryException {
        super(fiberObject);
        Objects.requireNonNull(fiberObject);
        Objects.requireNonNull(treeLayout);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(physicalConnectionsService);
        
        this.fiberObject = fiberObject;
        this.bem = bem;
        leftover = Boolean.valueOf(bem.getAttributeValueAsString(fiberObject.getClassName(), fiberObject.getId(), "leftover")); //NOI18N
        setNodeVisible(!leftover);
        setCellVisible(!leftover);
        
        fiberColor = BusinessObjectUtil.getBusinessObjectColor(fiberObject, aem, bem, mem);
        fiberStyle.put(MxConstants.STYLE_FILLCOLOR, fiberColor);
        setIsLeaf(true);
        setLabel(fiberObject.getName() != null && !fiberObject.getName().isEmpty() ? 
            String.format("%.50s", fiberObject.getName()) + (fiberObject.getName().length() > 50 ? "..." : "") : 
            ts.getTranslatedString("module.propertysheet.labels.null-value-property")
        );
        setNodeWidth(350);
        setNodeHeight(6);
        
        setGeometry(getNodeX(), getNodeY(), getNodeWidth(), getNodeHeight());
        splice();
        
        addCellAddedListener(event -> {
            event.unregisterListener();            
            setTooltip(fiberObject.getName() != null && !fiberObject.getName().isEmpty() ? 
                fiberObject.getName() : ts.getTranslatedString("module.propertysheet.labels.null-value-property")
            );
            overrideStyle();
        });
        setCellParent(treeLayout.getUuid());
        treeLayout.getGraph().addNode(this);
        
        addRightClickCellListener(rightClickEvent -> 
            new WindowFiberTools(fiberObject, aem, bem, mem, ts, 
                physicalConnectionsService, 
                consumerObjectChange
            ).open()
        );
    }
    
    public boolean isLeftover() {
        return leftover;
    }
    
    public String getColor() {
        return fiberColor;
    }
    
    public BusinessObjectLight getFiberObject() {
        return fiberObject;
    }
    
    public void splice() throws InventoryException {
        boolean hasEndpointA = bem.hasSpecialAttribute(fiberObject.getClassName(), fiberObject.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_A);
        boolean hasEndpointB = bem.hasSpecialAttribute(fiberObject.getClassName(), fiberObject.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_B);
        if (hasEndpointA || hasEndpointB) {
            fiberStyle.put(MxConstants.STYLE_FILL_OPACITY, "25");
            if (hasEndpointA && hasEndpointB) {
                setIsSelectable(false);
                setConnectable(false);
            }
        } else {
            fiberStyle.put(MxConstants.STYLE_FILL_OPACITY, "100");
            setIsSelectable(true);
            setConnectable(true);
        }
        setRawStyle(fiberStyle);
        overrideStyle();
    }
    
    public void release() throws InventoryException {
        splice();
        overrideStyle();
    }
}
