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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expregss or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.templateman.visual;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Represents a node in template manager module TreeGrid editor for template
 *
 * @author Adrian Martinez Molina {@literal <adrian.martinez@neotropic.co>}
 */
public class TemplateNode extends AbstractNode<TemplateObjectLight> {

    /**
     * An icon generator for create icons
     */
    private final ClassNameIconGenerator iconGenerator;
    private final IconNameCellGrid node;

    public TemplateNode(TemplateObjectLight template, ResourceFactory resourceFactory) {
        super(template);
        this.id = template.getId();
        this.name = template.getName();
        this.className = template.getClassName();
        this.iconGenerator = new ClassNameIconGenerator(resourceFactory);
        this.node = new IconNameCellGrid(this.name, this.className, iconGenerator);
    }

    public HorizontalLayout render() {
        HorizontalLayout lytValue = new HorizontalLayout(node);
        lytValue.setAlignItems(FlexComponent.Alignment.CENTER);
        lytValue.setPadding(false);
        lytValue.setMargin(false);
        lytValue.setSpacing(false);
        return lytValue;
    }

    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
