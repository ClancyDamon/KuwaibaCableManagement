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
package org.neotropic.kuwaiba.modules.core.containmentman;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;

/**
 * Represents a possible children as a visual component in the containment manager module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class PossibleChildComponent extends HorizontalLayout{
    /**
     * Class metadata represented by the component
     */
    private final ClassMetadataLight classMetadataLight;
    /**
     * The list ob subclasses displayed in the component
     */
    private List<ClassMetadataLight> subClasses;
    /**
     * To remove the possible children from the possible children zone
     */
    private Icon icnRemove;

    
    public PossibleChildComponent(ClassMetadataLight classMetadataLight, boolean abstractSubclass) {
        this.classMetadataLight = classMetadataLight;
        this.setSpacing(false);
        this.setPadding(false);
        this.setMargin(false);
        
        Label lblPossibleChildrenName = new Label(classMetadataLight.getName());
        lblPossibleChildrenName.setClassName("possible_children_label");
        
        this.setSizeUndefined();
        this.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);
    
        if(abstractSubclass){
            this.setClassName("possible_abstract_child");
            
            add(lblPossibleChildrenName);
        }else{
            icnRemove = new Icon(VaadinIcon.CLOSE_SMALL);
            icnRemove.setClassName("possible_children_action");

            this.setFlexGrow(1, lblPossibleChildrenName);
            this.setClassName("possible_child");
            
            add(lblPossibleChildrenName, icnRemove);
        }
    }

    /**
     * To create a Container for subclasses of an abstract class
     * @param classMetadataLight
     * @param subClasses 
     */
    public PossibleChildComponent(ClassMetadataLight classMetadataLight, List<ClassMetadataLight> subClasses) {
        this.classMetadataLight = classMetadataLight;
        this.subClasses = subClasses;
        this.setSpacing(false);
        this.setPadding(false);
        this.setMargin(false);
        
        Label lblPossibleChildrenName = new Label(classMetadataLight.getName());
        lblPossibleChildrenName.setClassName("abstract_possible_children_label");
        
        this.icnRemove = new Icon(VaadinIcon.CLOSE_SMALL);
        this.icnRemove.setClassName("possible_children_action");

        this.setClassName("possible_abstract_children_container");
        this.setSizeUndefined();
        this.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        this.setJustifyContentMode(JustifyContentMode.CENTER);
        add(lblPossibleChildrenName, icnRemove);
    }

    public List<ClassMetadataLight> getSubClasses() {
        return subClasses;
    }
    
    public ClassMetadataLight getClassMetadataLight() {
        return classMetadataLight;
    }
    
    public Icon getIcnRemove() {
        return icnRemove;
    }
}
