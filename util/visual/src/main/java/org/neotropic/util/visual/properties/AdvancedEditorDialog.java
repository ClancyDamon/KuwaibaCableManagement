/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.util.visual.properties;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Dialog that allows editing for properties that support the advanced editor
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class AdvancedEditorDialog extends Dialog {
    
    private AbstractProperty property;
    private AbstractField mainComponentEditor;
    private Button accept;
    private Button cancel;

    public Component getMainComponentEditor() {
        return mainComponentEditor;
    }

    public Button getAccept() {
        return accept;
    }

    public void setAccept(Button accept) {
        this.accept = accept;
    }

    public Button getCancel() {
        return cancel;
    }

    public void setCancel(Button cancel) {
        this.cancel = cancel;
    } 

    public void setMainComponentEditor(AbstractField mainComponentEditor) {
        this.mainComponentEditor = mainComponentEditor;
    }

    public AbstractProperty getProperty() {
        return property;
    }

    public void setProperty(AbstractProperty property) {
        this.property = property;
    }
    
    
    
    public AdvancedEditorDialog(AbstractProperty property, TranslationService ts) {
        
        setWidth("500px");
        VerticalLayout lytMainLayout = new VerticalLayout();
        
        this.property = property;
        this.mainComponentEditor = property.getAdvancedEditor();
        if (property.getAsString().equals(AbstractProperty.NULL_LABEL))
            mainComponentEditor.setValue(null);
        lytMainLayout.add(mainComponentEditor);
        
        accept = new Button(ts.getTranslatedString("module.propertysheet.advanced-editor.accept"));
        cancel = new Button(ts.getTranslatedString("module.propertysheet.advanced-editor.cancel"), ev -> {
            this.close();
        });
        
        HorizontalLayout lytButtons = new HorizontalLayout(cancel, accept);
        lytButtons.setWidthFull();
        lytButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        lytButtons.setFlexGrow(1, cancel, accept);
        lytMainLayout.add(lytButtons);
                
        add(lytMainLayout);
    }

    void loadNewValueIntoProperty() {
        if (!property.hasBinder())
            property.setValue(mainComponentEditor.getValue());
    }
    
}
