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
package org.neotropic.util.visual.menu;

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Button that  displays a menu below when clicked.
 * @param <T> The type of the menu items.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MenuButton<T> extends Button {
    private final List<T> items = new ArrayList();
    private final HashMap<T, String> itemLabels = new HashMap();
    private final HashMap<T, String> itemHrefs = new HashMap();
    private final HashMap<T, Runnable> runnableSelectedItems = new HashMap();
    private final MenuLayout menuLayout;
    private final String btnText;
    
    public MenuButton(MenuLayout menuLayout, String btnText, String target, Component icon){
        super(icon);
        Objects.requireNonNull(menuLayout);
        this.menuLayout = menuLayout;
        this.btnText = btnText;
        this.setClassName("main-menu-icon-button");
        this.getElement().setProperty("title", btnText);
        
        addClickListener(e -> {
            UI.getCurrent().navigate(target);
        });
    }
    
    public MenuButton(MenuLayout menuLayout, String btnText, Component icon) {
        super(icon);
        Objects.requireNonNull(menuLayout);
        this.menuLayout = menuLayout;
        this.btnText = btnText;
        this.setClassName("main-menu-icon-button");
        this.getElement().setProperty("title", btnText);
        
        addClickListener(c -> {
            if (this.menuLayout.getMenuDialog() != null)
                this.menuLayout.removeMenuDialog();
            initMenuDialog();
        });
    }
    
    @SuppressWarnings("empty-statement")
    private void initMenuDialog() {
        if (!items.isEmpty()) {
            PaperDialog paperDialog = new PaperDialog();
            paperDialog.getStyle().set("border-top-left-radius", "5px");
            paperDialog.getStyle().set("border-top-right-radius", "5px");
            paperDialog.setWidth("200px");
            paperDialog.setId(btnText + "-id");
            paperDialog.setNoOverlap(true);
            paperDialog.setMargin(false);
            
            Label lblActionTitle = new Label(btnText);
            lblActionTitle.getStyle().set("margin-left", "1.1em");
            HorizontalLayout lytTitle = new HorizontalLayout(lblActionTitle);
            lytTitle.setAlignItems(FlexComponent.Alignment.CENTER);
            lytTitle.setWidthFull();
            lytTitle.setClassName("sub-menu");
            
            FlexLayout lytContent = new FlexLayout(lytTitle);
            lytContent.setSizeFull();
            lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            lytContent.getStyle().set("margin", "0px"); //NOI18N
            lytContent.getStyle().set("padding", "0px"); //NOI18N
            
            for (T item : items) {
                HorizontalLayout lytSubMenuElement = new HorizontalLayout();
                lytSubMenuElement.setClassName("sub-menu-element");
                
                Div div = new Div();
                div.add(itemLabels.get(item));
                
                HorizontalLayout lyt = new HorizontalLayout();
                lyt.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                lyt.add(div);
                lyt.addClickListener(c -> {
                    runnableSelectedItems.get(item).run();
                    paperDialog.close();
                });
                lytSubMenuElement.add(lyt);
                if (itemHrefs.containsKey(item)) {
                    Anchor anchor = new Anchor();
                    anchor.setHref(itemHrefs.get(item));
                    Icon icon = VaadinIcon.EXTERNAL_LINK.create();
                    icon.setSize("14px");
                    icon.setColor("var(--kuwaiba-primary-text-color)");
                    
                    anchor.add(icon);
                    anchor.setTarget("_blank");
                    anchor.getElement().getStyle().set("margin-left", "auto");
                    
                    lytSubMenuElement.add(anchor);
                }
                lytContent.add(lytSubMenuElement);
            };
            paperDialog.add(lytContent);
            menuLayout.addMenuDialog(paperDialog);
            
            open(paperDialog, btnText + "-id", (Button)this, true);
        }
    }
    
    private void open(PaperDialog paperDialog, String paperDialogId, Component positionTarget, boolean relative){
        if(relative) {
            paperDialog.getElement().executeJs("document.getElementById($0).style.position='absolute';"
                + "document.getElementById($0).style.top = $1.offsetTop + $1.offsetHeight + 'px';"
                + "document.getElementById($0).style.left = $1.offsetLeft + 'px';", paperDialogId, positionTarget); //NOI18N
        }
        paperDialog.open();
    }
    
    public void addMenuItem(T item, String itemLabel, String itemHref, Runnable runnableSelectedItem) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(itemLabel);
        Objects.requireNonNull(runnableSelectedItem);
        items.add(item);
        itemLabels.put(item, itemLabel);
        if (itemHref != null &&  !itemHref.isEmpty())
            itemHrefs.put(item, itemHref);
        runnableSelectedItems.put(item, runnableSelectedItem);
    }
}
