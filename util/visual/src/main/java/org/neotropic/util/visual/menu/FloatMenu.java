/*
 * Copyright 2010-2022 Neotropic SAS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.util.visual.menu;

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.neotropic.util.visual.button.ActionButton;

/**
 *
 * @author adr
 */
public class FloatMenu extends PaperDialog{
    private @Getter final ActionButton btnOpen;
    private final FlexLayout lytMenuContent;
    private final List<HorizontalLayout> menuItems;
    
    public FloatMenu(String id) {
        this.getStyle().set("border-top-left-radius", "5px");
        this.getStyle().set("border-top-right-radius", "5px");
        this.setWidth("200px");
        this.setId("menu-" + id);
        this.setNoOverlap(true);
        this.setMargin(false);
        
        lytMenuContent = new FlexLayout();
        lytMenuContent.setSizeFull();
        lytMenuContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        lytMenuContent.getStyle().set("margin", "0px"); //NOI18N
        lytMenuContent.getStyle().set("padding", "0px"); //NOI18N
        
        menuItems = new ArrayList<>();
        this.add(lytMenuContent);
        btnOpen = new ActionButton(new Icon(VaadinIcon.ELLIPSIS_DOTS_H));
        btnOpen.addClickListener(e -> this.open("menu-" + id, btnOpen, false));
    }
    
    public Component addMenuItem(String itemName
            , ComponentEventListener<ClickEvent<HorizontalLayout>> clickListener)
    {
        HorizontalLayout lytMenuElementNewItem = new HorizontalLayout(new Span(itemName));
        lytMenuElementNewItem.addClickListener(clickListener);
        lytMenuElementNewItem.setClassName("sub-menu-element");
        lytMenuContent.add(lytMenuElementNewItem);
        menuItems.add(lytMenuElementNewItem);
        
        return lytMenuElementNewItem;
    }
}
