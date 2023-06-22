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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.components;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.AbstractUiElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.UiElementFactory;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.AbstractElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementButton;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementCheckBox;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementComboBox;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementDateField;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementGrid;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementGridLayout;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementHorizontalLayout;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementImage;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementLabel;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementListSelectFilter;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementMiniApplication;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementSubform;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementTextArea;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementTextField;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementUpload;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementVerticalLayout;
import com.vaadin.flow.component.Component;

/**
 * Creates UI elements in Vaadin
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentFactory implements UiElementFactory<Component> {
    private final String processEnginePath;
    
    public ComponentFactory(String processEnginePath) {
        this.processEnginePath = processEnginePath;
    }
    
    @Override
    public AbstractUiElement<? extends AbstractElement, ? extends Component> getUiElement(AbstractElement element) {
        AbstractUiElement<? extends AbstractElement, ? extends Component> uiElement = null;
        if (element instanceof ElementButton)
            uiElement = new ComponentButton((ElementButton) element);
        if (element instanceof ElementCheckBox)
            uiElement = new ComponentCheckBox((ElementCheckBox) element);
        if (element instanceof ElementComboBox)
            uiElement = new ComponentComboBox((ElementComboBox) element);
        if (element instanceof ElementDateField)
            uiElement = new ComponentDateField((ElementDateField) element);
        if (element instanceof ElementGrid)
            uiElement = new ComponentGrid((ElementGrid) element);
        if (element instanceof ElementGridLayout)
            uiElement = new ComponentGridLayout((ElementGridLayout) element);
        if (element instanceof ElementHorizontalLayout)
            uiElement = new ComponentHorizontalLayout((ElementHorizontalLayout) element);
        if (element instanceof ElementImage)
            uiElement = new ComponentImage((ElementImage) element, processEnginePath);
        if (element instanceof ElementLabel)
            uiElement = new ComponentLabel((ElementLabel) element);
        if (element instanceof ElementListSelectFilter)
            uiElement = new ComponentListSelectFilter((ElementListSelectFilter) element);
        if (element instanceof ElementMiniApplication)
            uiElement = new ComponentMiniApplication((ElementMiniApplication) element);
        if (element instanceof ElementSubform)
            uiElement = new ComponentSubform((ElementSubform) element);
        if (element instanceof ElementTextArea)
            uiElement = new ComponentTextArea((ElementTextArea) element);
        if (element instanceof ElementTextField)
            uiElement = new ComponentTextField((ElementTextField) element);
        if (element instanceof ElementUpload)
            uiElement = new ComponentUpload((ElementUpload) element);
        if (element instanceof ElementVerticalLayout)
            uiElement = new ComponentVerticalLayout((ElementVerticalLayout) element);
        uiElement.build();
        return uiElement;
    }
}
