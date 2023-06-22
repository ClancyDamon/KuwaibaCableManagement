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
package org.neotropic.util.visual.icon;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Custom icon, for replace action buttons, it has a preferred size and toolTip;
 * however, optionally, you can set other icon size
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class ActionIcon extends Icon {

    public ActionIcon(VaadinIcon icon) {
        super(icon);        
    }

    public ActionIcon(VaadinIcon icon, String tooltip) {
        super(icon);        
        this.getElement().setProperty("title", tooltip);
    }

    public ActionIcon(VaadinIcon icon, String tooltip, String iconSize) {
        super(icon);
        this.setSize(iconSize);
        this.getElement().setProperty("title", tooltip);
    }

    public ActionIcon(VaadinIcon icon, String tooltip, String iconSize, String color) {
        super(icon);
        this.setSize(iconSize);
        this.getElement().setProperty("title", tooltip);        
        this.setColor(color);
    }
}
