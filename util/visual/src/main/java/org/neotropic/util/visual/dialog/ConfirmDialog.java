/*
 * Copyright 2010-2022. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.util.visual.dialog;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Generic Dialog that provides the capability to confirm specific actions.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
@CssImport(value = "./styles/theme-confirm-dialog.css", themeFor = "vcf-enhanced-dialog-overlay")
//@CssImport(value = "./css/custom-vcf-enhanced-dialog-overlay.css", themeFor="vcf-enhanced-dialog-overlay")
public class ConfirmDialog extends EnhancedDialog {
    
    private Button btnConfirm;
    
    private Button btnCancel;
    
    public ConfirmDialog() {}
    
    @Override
    public void setHeader(String headerText) {        
        Label lbl = new Label(headerText);
        lbl.addClassName("lbl-bold"); //NOI18N
        setHeader(lbl);
        setHeader(new H5(lbl));
    }
    
    public ConfirmDialog(TranslationService ts, String title, String text, String confirmButtonText) {
        btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), e -> close());
        
        btnConfirm = new Button(confirmButtonText);
        btnConfirm.setClassName("confirm-button");
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_LARGE);
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnConfirm.focus();
        btnConfirm.addClickShortcut(Key.ENTER);
        
        HorizontalLayout lytButtons = new HorizontalLayout(btnCancel, btnConfirm);
        lytButtons.setPadding(false);
        lytButtons.setMargin(false);
        lytButtons.setSpacing(false);
        // expands the empty space left of button two
        btnConfirm.getElement().getStyle().set("margin-left", "auto");
        
        setHeader(title);
        setContent(new Span(text));
        setFooter(lytButtons);
        setMinWidth("40%");
    }
        
     public ConfirmDialog(TranslationService ts, String title, String confirmButtonText) {
        this(ts, title, "", confirmButtonText);      
    }
    /**
     * Creates a confirm dialog with a component as content.
     * @param ts An instance of the translation service
     * @param title The title of the confirm dialog
     * @param content The component used as content of the confirm dialog
     * @param confirmButtonText Text to show in the confirm button
     * @param confirmAction Action to execute on confirm
     */
    public ConfirmDialog(TranslationService ts, String title, Component content, String confirmButtonText, Command confirmAction) {
        this(ts, content, confirmButtonText, confirmAction);
        setHeader(title);
        setMinWidth("40%");
    }
    /**
     * Creates a confirm dialog with a component as content.
     * @param ts An instance of the translation service
     * @param content The component used as content of the confirm dialog
     * @param confirmButtonText Text to show in the confirm button
     * @param confirmAction Action to execute on confirm
     */
    public ConfirmDialog(TranslationService ts, Component content, String confirmButtonText, Command confirmAction) {
        btnCancel = new Button(
            ts.getTranslatedString("module.general.messages.cancel"), 
            event -> { close(); event.unregisterListener();});
        
        
        btnConfirm = new Button(confirmButtonText, 
            event -> { confirmAction.execute(); event.unregisterListener(); close();});
        btnConfirm.setClassName("confirm-button"); //NOI18N
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_LARGE);
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnConfirm.focus();
        btnConfirm.addClickShortcut(Key.ENTER);
        
        HorizontalLayout lytButtons = new HorizontalLayout(btnCancel, btnConfirm);
        lytButtons.setPadding(false);
        lytButtons.setMargin(false);
        lytButtons.setSpacing(false);
        // expands the empty space left of button two
        btnConfirm.getElement().getStyle().set("margin-left", "auto");
        
        setContentSizeFull();
        VerticalLayout lytContent = new VerticalLayout(content);
        lytContent.setSizeFull();
        lytContent.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, content);
        
        setContent(lytContent);
        setFooter(lytButtons);
        setMinWidth("40%");
    }
    
    /**
     * Creates a confirm dialog with a component as content.
     * @param ts An instance of the translation service
     * @param title The title of the confirm dialog
     * @param content The component used as content of the confirm dialog
     * @param confirmButtonText Text to show in the confirm button
     */
    public ConfirmDialog(TranslationService ts, String title, Component content, String confirmButtonText) {
        btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), e -> close());
        
        btnConfirm = new Button(confirmButtonText);
        btnConfirm.setClassName("confirm-button"); //NOI18N
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_LARGE);
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnConfirm.focus();
        btnConfirm.addClickShortcut(Key.ENTER);
        
        setHeader(title);
        setContent(content);
        
        HorizontalLayout lytButtons = new HorizontalLayout(btnCancel, btnConfirm);
        lytButtons.setPadding(false);
        lytButtons.setMargin(false);
        lytButtons.setSpacing(false);
        // expands the empty space left of button two
        btnConfirm.getElement().getStyle().set("margin-left", "auto");
        
        setFooter(lytButtons);
        setMinWidth("40%");
    }
    
    /**
     * Creates a simple confirm dialog. No clickshortcut
     * @param ts An instance of the translation service
     * @param confirmButtonText Text to show in the confirm button
     */
    public ConfirmDialog(TranslationService ts, String confirmButtonText) {
        btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), e -> close());
        
        btnConfirm = new Button(confirmButtonText);
        btnConfirm.setClassName("confirm-button"); //NOI18N
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_LARGE);
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnConfirm.focus();
                
        HorizontalLayout lytButtons = new HorizontalLayout(btnCancel, btnConfirm);
        lytButtons.setPadding(false);
        lytButtons.setMargin(false);
        lytButtons.setSpacing(false);
        // expands the empty space left of button two
        btnConfirm.getElement().getStyle().set("margin-left", "auto");
        
        setFooter(lytButtons);
        setMinWidth("40%");
    }
    
    public Button getBtnConfirm() {
        return btnConfirm;
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    /**
     * Set confirmation button to be enabled or disabled, remove style and themes if disabled
     * @param enable true if the button should be enabled
     */
    public void setEnableBtnConfirm(boolean enable){
        btnConfirm.setEnabled(enable);
        if(enable) {
            btnConfirm.setClassName("confirm-button"); //NOI18N
            btnConfirm.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_PRIMARY);
        } else {
            btnConfirm.removeClassName("confirm-button"); //NOI18N
            btnConfirm.removeThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_PRIMARY);
        }
    }

    public final void setContentSizeFull() {
        // Adding class full-size-enhanced-dialog-content
        getChildren().forEach(component -> {
            String elementClass = component.getElement().getAttribute("class"); //NOI18N
            if ("enhanced-dialog-content".contains(elementClass)) { //NOI18N
                component.getElement().setAttribute(
                    "class", //NOI18N
                    elementClass + " full-size-enhanced-dialog-content" //NOI18N
                ); 
            }
        });
    }
}