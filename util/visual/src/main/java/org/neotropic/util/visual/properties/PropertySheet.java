/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.util.visual.properties;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.JsonObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * An embeddable property sheet.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
public class PropertySheet extends Grid<AbstractProperty> {

    private TranslationService ts;
    
    private List<IPropertyValueChangedListener> propertyValueChangedListeners;
    
    private Button currentBtnCancelInEditProperty;
    
    private boolean readOnly;
       
    private Command comandUndoLastEdit;
    
    private Object lastValue;
    
    private String propertyName;

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public PropertySheet(TranslationService ts) {
        this(ts, false);
    }
     
    public PropertySheet(TranslationService ts, boolean showAdvancedEditorColumn) {
        this.ts = ts;
        addClassName("grid-compact");
        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        setHeightByRows(true);
        propertyValueChangedListeners = new ArrayList<>();
        Map<Integer, StreamResource> colors = new HashMap<>();
        addComponentColumn(property -> {         
            HorizontalLayout lytName = new HorizontalLayout();
            lytName.setClassName("property-sheet-name-cell");
            Label lblName = new BoldLabel(property.toString()); 
            Label lblType = new BoldLabel(property.getType()); 
            lblType.setClassName("text-secondary");
            
            int color = new Color(76, 129, 161).getRGB();
            if (property.isReadOnly()) {
                lblName.getElement().getStyle().set("color", Color.gray.toString());
                color = Color.gray.getRGB();
            }
            if (property.isUnique()) {
                lblName.addClassName("purpleColor");
                color = new Color(151, 5, 156).getRGB();
            }
            if (property.isMandatory()) {
                lblName.addClassName("redColor");
                color = Color.RED.getRGB();
            }
//            if (colors.containsKey(co))
            StreamResource icon = buildIcon("color" + color + ".png", getIcon(new Color(color), 8, 8));
            VaadinSession.getCurrent().getResourceRegistry().registerResource(icon);
            Image imgIcon = new Image(icon, "");
            
            lblName.setTitle(property.getDescription() == null || property.getDescription().isEmpty()
                    ? property.toString() : property.getDescription());
            lytName.add(imgIcon, lblName, lblType);
            lytName.setAlignItems(FlexComponent.Alignment.CENTER);
            return lytName;
        }).setKey("name")
                .setFlexGrow(3);
        TextField firstNameField = new TextField();
        firstNameField.getElement().getStyle().set("display", "none");
        addComponentColumn(property -> {
            HorizontalLayout lytValue = new HorizontalLayout();
            lytValue.setAlignItems(FlexComponent.Alignment.CENTER);
            lytValue.setPadding(false);
            
            String txtProperty = property.getAsString();         
            Label lblValue = new Label(txtProperty);      
            lblValue.getElement().setAttribute("title", ts.getTranslatedString("module.propertysheet.labels.dbl-click-edit"));
 
            Image imgLink = new Image("img/link.png", ts.getTranslatedString("module.propertysheet.open-link"));
        
            Anchor link = new Anchor(txtProperty, imgLink);            
            updateLabel(txtProperty, link, lblValue);
            lblValue.getElement().getStyle().set("width", "100%");
            lytValue.add(lblValue);
            
            if (!property.isReadOnly() && !readOnly && property.supportsInplaceEditor()) {
                Button btnEdit = new Button(new Icon(VaadinIcon.CHECK_CIRCLE_O));
                Button btnCancel = new Button(new Icon(VaadinIcon.CLOSE_SMALL));              
                AbstractField editField = property.getInplaceEditor();
                editField.setVisible(false);
                editField.getElement().getStyle().set("width", "95%");
                // if the property doesnt have a binder, then set the value manually
                if (!property.hasBinder())
                    editField.setValue(property.getAsString().equals(AbstractProperty.NULL_LABEL) ? "" : property.getValue());
               
                btnEdit.addClassName("icon-button");             
                btnCancel.addClassName("icon-button");               
                
                editField.getElement().addEventListener("keydown", event ->
                {   
                    JsonObject data =  event.getEventData();
                    if (13 == data.getNumber("event.which")) 
                            btnEdit.click();                  
                    if (27 == data.getNumber("event.which")) 
                            btnCancel.click();
                    
                }).addEventData("event.which");

                btnEdit.addClickListener(e -> {
                    lblValue.setVisible(true);
                    editField.setVisible(false);
                    if (showAdvancedEditorColumn)
                        this.getColumnByKey("advancedEditor").setVisible(true);        
                    if (editField.getValue() == null) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                                ts.getTranslatedString("module.general.messages.error-null-value"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                        return;
                    }
//                    String lastStrgValue = lblValue.getText();
            
                    updateLabel(editField.getValue().toString(), link, lblValue);
                    propertyName = property.getName();
                    lastValue = property.getValue();
                                     
                    if (!property.hasBinder())
                        property.setValue(editField.getValue());
                    lblValue.setText(property.getAsString());  
                    comandUndoLastEdit = () -> {
                        property.setValue(lastValue);
                        updateLabel(lastValue.toString(), link, lblValue);
                        lblValue.setText(lastValue.toString());
                        editField.setValue(lastValue);
                    };
                    this.currentBtnCancelInEditProperty = null;
                    setHeightByRows(!isHeightByRows());
                    firePropertyValueChangedEvent(property);
                });

                btnCancel.addClickListener(ev -> {
                    if (!property.hasBinder())
                        editField.setValue(property.getValue());                   
                    setHeightByRows(!isHeightByRows());
                    lblValue.setVisible(true);
                    editField.setVisible(false);
                    if (showAdvancedEditorColumn)
                        this.getColumnByKey("advancedEditor").setVisible(true);                 
                    this.currentBtnCancelInEditProperty = null;
                });
               
                lblValue.getElement().addEventListener("dblclick", e -> {
                    
                    if (property.supportsInplaceEditor()) {
		        		                        
                        if (this.currentBtnCancelInEditProperty != null) 
                            currentBtnCancelInEditProperty.click();
                        else
                            setHeightByRows(!isHeightByRows());

                        this.currentBtnCancelInEditProperty = btnCancel;

                        lblValue.setVisible(false);
                        editField.setVisible(true);
                        if (showAdvancedEditorColumn)
                            this.getColumnByKey("advancedEditor").setVisible(false);     
                        try {
                            ((Focusable) editField).focus();
                          }
                        catch (Exception ex) {
                              // the component doesnt implements Focusable interface,
                              // but is not necesary show any message
                        }
                                               
                    } else if (property.supportsAdvancedEditor())
                        openAdvancedEditor(property);
                });
                
                lytValue.add(editField, link);
            }
            lblValue.getElement().addEventListener("dblclick", e -> {
                if (!property.supportsInplaceEditor() && property.supportsAdvancedEditor())
                    openAdvancedEditor(property);
            });
            return lytValue;
        }).setKey("value").setFlexGrow(5);
               
        if (showAdvancedEditorColumn)
            addComponentColumn((property) -> {
                if (!property.isReadOnly() && !readOnly && property.supportsAdvancedEditor() && showAdvancedEditorColumn) {
                     Button btnAdvancedEditor = new Button("...", ev -> {
                         openAdvancedEditor(property);
                     });
                     btnAdvancedEditor.setClassName("compact-button");
                     return btnAdvancedEditor;
                }
                return new HorizontalLayout();
            }).setKey("advancedEditor").setWidth("50px");
        
         }

    public PropertySheet(TranslationService ts, List<AbstractProperty> properties) {
        this(ts);
        setItems(properties); 
    }
    
    private void openAdvancedEditor(AbstractProperty property) {
        AdvancedEditorDialog dialog = new AdvancedEditorDialog(property, ts);
        dialog.getAccept().addClickListener(clickEv -> {
            dialog.loadNewValueIntoProperty();
            firePropertyValueChangedEvent(dialog.getProperty());
            getDataProvider().refreshAll();
            dialog.close();
        });
        dialog.open();
    }

    public void clear() {
        setItems();
    }
    
    public void undoLastEdit() {
        if (comandUndoLastEdit != null)
            comandUndoLastEdit.execute();
    }
    
    public Object lastValue(String propertyName) {
        if (propertyName != null && this.propertyName != null && this.lastValue != null) {
            if (propertyName.endsWith(this.propertyName)) 
                return lastValue;
        }        
        return null;
    }
    
    public interface IPropertyValueChangedListener{
         void updatePropertyChanged(AbstractProperty<? extends Object> property);
    }
    
    public void firePropertyValueChangedEvent(AbstractProperty property) {
        for (IPropertyValueChangedListener l : propertyValueChangedListeners) {
            l.updatePropertyChanged(property);
        }
    }
    
    public void addPropertyValueChangedListener(IPropertyValueChangedListener iPropertyValueChangedListener) {
        propertyValueChangedListeners.add(iPropertyValueChangedListener);
    }
    
     /**
     * Creates (or retrieves a cached version) of a squared colored icon
     * @param color The color of the icon
     * @param width The width of the icon
     * @param height The height of the icon
     * @return The icon as a byte array
     */
    public byte[] getIcon(Color color, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(color);
            graphics.fillRect(0, 0, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * Builds an icon resource
     * @param name the name of the resource
     * @param icon the icon as byte array
     * @return An icon resource which is not registry
     */
    public StreamResource buildIcon(String name, byte[] icon) {
        return new StreamResource(name, new InputStreamFactory() {
            @Override
            public InputStream createInputStream() {
                return new ByteArrayInputStream(icon);
            }
        });                                
    }
    
    /**
     * Updates the anchor link for URL string properties
     * @param value the string to check
     * @param link the anchor component
     * @param label the label component that shows the property
     */
    public void updateLabel(String value, Anchor link, Label label) {

        try {
            URL url = new URL(value);
            link.setTarget("_blank");
            link.setHref(value);
            link.setVisible(true);
            label.setMaxWidth("90%");
        } catch (MalformedURLException ex) { // is not an URL
            link.setVisible(false);
            label.setMaxWidth("100%");
        }
    }
}
