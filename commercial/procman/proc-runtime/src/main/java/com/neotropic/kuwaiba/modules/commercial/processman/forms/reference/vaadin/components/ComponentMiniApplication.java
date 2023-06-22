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
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Constants;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementMiniApplication;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.miniapps.AbstractMiniApplication;

/**
 * UI element to render the {@link ElementMiniApplication miniApplication} element
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentMiniApplication extends AbstractUiElement<ElementMiniApplication, Component> {
    private AbstractMiniApplication<? extends Component, ? extends Component> miniApplication;
    private Dialog dialog;

    public ComponentMiniApplication(ElementMiniApplication element) {
        super(element, null);
    }

    @Override
    public Component getUiElement() {
        return miniApplication.launchEmbedded();
    }
    
    @Override
    protected void postConstruct() {
        try {
            Class<?> aClass = Class.forName(String.format("%s.%s", getElement().getClassPackage(), getElement().getClassName()));
            Constructor<?> constructor = aClass.getConstructor(Properties.class);
            Object object = constructor.newInstance(new Object[] { new Properties() });
            if (object instanceof AbstractMiniApplication)
                miniApplication = (AbstractMiniApplication) object;
            
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ComponentMiniApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setId(String id) {}

    @Override
    public void setWidth(String width) {}

    @Override
    public void setHeight(String height) {}

    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName())) {
            if (Constants.Function.OPEN.equals(event.getPropertyName())) {
                Component component = miniApplication.launchDetached();
                
                if (component instanceof Dialog)
                    dialog = (Dialog) component;
                else if (component instanceof Component) {
                    dialog = new Dialog();
                    dialog.add(component);
                }
                if (dialog != null)
                    dialog.open();
                
            } else if (Constants.Function.CLOSE.equals(event.getPropertyName())) {
                if (dialog != null)
                    dialog.close();
            } else if (Constants.Function.CLEAN.equals(event.getPropertyName())) {
            }
            
        } else if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (Constants.Property.INPUT_PARAMETERS.equals(event.getPropertyName())) {
                if (event.getNewValue() instanceof Properties) {
                    miniApplication.setInputParameters((Properties) event.getNewValue());
                    
                }
            }
        }
    }
}
