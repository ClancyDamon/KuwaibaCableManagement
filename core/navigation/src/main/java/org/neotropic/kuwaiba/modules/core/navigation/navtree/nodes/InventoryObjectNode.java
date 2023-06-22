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
package org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Represents a Node in the Navigation module's TreeGrid.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class InventoryObjectNode extends AbstractNode<BusinessObjectLight> {
    /**
     * layout that contains the action buttons
     */
    private final ActionButton btnMenu;
    /**
     * Keeps a lists of the action buttons need it if we need to hide/show/enable/disable them
     */
    private List<ActionButton> actionButtons;
    /**
     * Field to edit the node value, mostly the name of the BusinessObjectLight
     */
    private TextField nodeEditor;
    
    public InventoryObjectNode(BusinessObjectLight object) {
        super(object);
        this.id = object.getId();
        this.id = object.getId();
        this.name = object.getName();
        this.className = object.getClassName();
        Icon icnMenu = new Icon(VaadinIcon.ELLIPSIS_DOTS_H);
        btnMenu = new ActionButton(icnMenu);
    }
   
    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
