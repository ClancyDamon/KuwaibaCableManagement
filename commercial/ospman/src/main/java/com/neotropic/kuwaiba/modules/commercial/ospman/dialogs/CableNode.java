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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Represents a cable in the mid-span access and splicing view
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CableNode extends TreeLayoutNode {
    private final BusinessObjectLight cableObject;
    private final TreeLayout treeLayout;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private List<TreeLayoutNode> children;
    private final PhysicalConnectionsService physicalConnectionsService;
    private final Consumer<BusinessObjectLight> consumerObjectChange;
    
    private final LinkedHashMap<String, String> cableStyle = new LinkedHashMap();
    {
        cableStyle.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        cableStyle.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        cableStyle.put(MxConstants.STYLE_LABEL_POSITION, MxConstants.ALIGN_CENTER);
        cableStyle.put(MxConstants.STYLE_VERTICAL_LABEL_POSITION, MxConstants.ALIGN_TOP);
        cableStyle.put(MxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "white"); //NOI18N
        cableStyle.put(MxConstants.STYLE_FONTCOLOR, "black"); //NOI18N
        cableStyle.put(MxConstants.STYLE_FONTSIZE, "11"); //NOI18N
    }
    
    public CableNode(BusinessObjectLight cableObject, TreeLayout treeLayout, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts,
        PhysicalConnectionsService physicalConnectionsService, Consumer<BusinessObjectLight> consumerObjectChange) 
        throws InventoryException {
        super(cableObject);
        Objects.requireNonNull(cableObject);
        Objects.requireNonNull(treeLayout);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.cableObject = cableObject;
        this.treeLayout = treeLayout;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
        this.consumerObjectChange = consumerObjectChange;
        
        setNodeWidth(350);
        setNodeHeight(6);
        
        cableStyle.put(MxConstants.STYLE_FILLCOLOR, BusinessObjectUtil.getBusinessObjectColor(cableObject, aem, bem, mem));
        setRawStyle(cableStyle);
        setGeometry(getNodeX(), getNodeY(), getNodeWidth(), getNodeHeight());
        setLabel(cableObject.getName() != null && !cableObject.getName().isEmpty() ? 
            String.format("%.50s", cableObject.getName()) + (cableObject.getName().length() > 50 ? "..." : "") : 
            ts.getTranslatedString("module.propertysheet.labels.null-value-property")
        );
        
        addCellAddedListener(event -> {
            event.unregisterListener();
            setTooltip(cableObject.getName() != null && !cableObject.getName().isEmpty() ? 
                cableObject.getName() : 
                ts.getTranslatedString("module.propertysheet.labels.null-value-property")
            );
            overrideStyle();
            setIsSelectable(false);
            setConnectable(false);
        });
        setCellParent(treeLayout.getUuid());
        treeLayout.getGraph().addNode(this);
    }
    
    @Override
    public List<TreeLayoutNode> getNodeChildren() {
        if (children == null) {
            if (isNodeExpanded()) {
                children = new ArrayList();
                try {
                    List<BusinessObjectLight> objectChildren = bem.getObjectSpecialChildren(cableObject.getClassName(), cableObject.getId());
                    Collections.sort(objectChildren, Comparator.comparing(BusinessObjectLight::getName));
                    for (BusinessObjectLight objectChild : objectChildren) {
                        if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, objectChild.getClassName()))
                            children.add(new FiberNode(objectChild, treeLayout, aem, bem, mem, ts, physicalConnectionsService, consumerObjectChange));
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                    children = null;
                    return Collections.EMPTY_LIST;
                }
            }
            else
                return Collections.EMPTY_LIST;
        }
        return children;
    }
}
