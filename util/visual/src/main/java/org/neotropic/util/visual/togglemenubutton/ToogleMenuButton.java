/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.togglemenubutton;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.Command;

/**
 * Custom button that allows toggle a given section or component in the page
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ToogleMenuButton extends Button {
     private boolean menuVisible = true;
     private Command visibleCommand; 
     private Command noVisibleCommand; 
     
     public ToogleMenuButton(String buttonText, String sectionId, Command visibleCommand, Command noVisibleCommand) {
         super(buttonText, new Icon(VaadinIcon.LINES));
         this.visibleCommand = visibleCommand;
         this.noVisibleCommand = noVisibleCommand;
         this.addClickListener(evt -> {
                Page page = UI.getCurrent().getPage();
                page.executeJs("$('" + sectionId + "').fadeToggle(400);");
                toogleMenu();
                if (menuVisible && noVisibleCommand != null) 
                    this.noVisibleCommand.execute(); // executed when go from 'visible' to 'no visible' state             
                else if (visibleCommand != null) 
                    this.visibleCommand.execute();
                
            });
    }
     
     public ToogleMenuButton(String buttonText, String sectionId) {
         this(buttonText, sectionId, null, null);
     }

    public Command getVisibleCommand() {
        return visibleCommand;
    }

    public void setVisibleCommand(Command visibleCommand) {
        this.visibleCommand = visibleCommand;
    }

    public Command getNoVisibleCommand() {
        return noVisibleCommand;
    }

    public void setNoVisibleCommand(Command noVisibleCommand) {
        this.noVisibleCommand = noVisibleCommand;
    }
     
    public void toogleMenu() {
        menuVisible = !menuVisible;
    }

    public boolean isMenuVisible() {
        return menuVisible;
    }

    public void setMenuVisible(boolean menuVisible) {
        this.menuVisible = menuVisible;
    }
    
    
           
}
