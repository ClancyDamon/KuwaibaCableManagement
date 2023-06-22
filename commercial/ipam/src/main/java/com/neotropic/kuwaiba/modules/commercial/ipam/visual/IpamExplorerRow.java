/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ipam.visual;

import com.neotropic.kuwaiba.modules.commercial.ipam.engine.IpamEngine;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

/**
 * Represents a row in the parent containment graphical display  in the ipam
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class IpamExplorerRow extends HorizontalLayout{

    public static final int FOLDER = 1;
    public static final int SUBENT = 0;
    
    private String objId;
    private String name;
    public IpamExplorerRow() {}
    
    public IpamExplorerRow(String objId, String name, String className, boolean isFirst, boolean isLeaf, int levels) {
        this.objId = objId;
        this.name = name;
        this.setMargin(false);
        this.setPadding(false);
        this.setSpacing(false);
        this.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        
        Label lblName = new Label(name);
        Icon icn;
        if(IpamEngine.isCIDRFormat(name))
            icn = new Icon(VaadinIcon.SITEMAP);
        else if(className.equals(Constants.CLASS_IP_ADDRESS))
            icn = new Icon(VaadinIcon.PROGRESSBAR);
        else 
            icn = new Icon(VaadinIcon.FOLDER_OPEN);
        icn.setSize("16px");

        if(levels > 0){
            double x = levels;
            this.getStyle().set("padding-left", Double.toString(x * 0.9) + "em");
        }
        else
            this.getStyle().set("padding-left", "16px");
        
        
        if(isFirst)
            add(icn, new Html("<span>&emsp;</span>"), lblName);
        else if(isLeaf){
            Icon icnLvlUp = new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL);
            icnLvlUp.setSize("16px");
            add(icnLvlUp, icn, new Html("<span>&emsp;</span>"), lblName);
        }
        else{
            Icon icnLvlUp = new Icon(VaadinIcon.LEVEL_UP);
            icnLvlUp.setSize("16px");
            add(icnLvlUp, icn, new Html("<span>&emsp;</span>"), lblName);
        }
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
