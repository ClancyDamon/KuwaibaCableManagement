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
package org.neotropic.util.visual.button;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import org.neotropic.util.visual.icon.ActionIcon;

/**
 * Custom button, it allow set a toolTip 
  * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class ActionButton extends Button{

    public ActionButton(String text) {
        super(text);
        this.setClassName("action-button-icon");
    }

    public ActionButton(Component icon) {
        super(icon);
        this.setClassName("action-button-icon");
    }

    public ActionButton(String text, String toolTip) {
        super(text);
        this.getElement().setProperty("title", toolTip);
        this.setClassName("action-button-icon");
    }

    public ActionButton(Component icon, String toolTip) {
        super(icon);
        this.getElement().setProperty("title", toolTip);
        this.setClassName("action-button-icon");
    }
    
    public ActionButton(String text, Component icon) {
        super(text, icon);
        this.setClassName("action-button-icon");
    }
    
    public ActionButton(String text, Component icon, String toolTip) {
        super(text, icon);
        this.getElement().setProperty("title", toolTip);
        this.setClassName("action-button-icon");
    }
    
    public void setToolTip(String toolTip){
        this.getElement().setProperty("title", toolTip);        
    }
    
    public ActionIcon getButtonIcon(){
        return (ActionIcon) getIcon();
    }
}
