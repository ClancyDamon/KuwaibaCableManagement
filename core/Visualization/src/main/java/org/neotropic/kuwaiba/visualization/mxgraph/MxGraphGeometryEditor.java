/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.visualization.mxgraph;

import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;

/**
 * Basic component to edit mxCells geometry
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class MxGraphGeometryEditor extends VerticalLayout {

    MxGraphCell currentMxGraphCell;
    NumberField txtX;
    NumberField txtY;
    NumberField txtWidth;
    NumberField txtHeight;
    TranslationService ts;
    
    public MxGraphGeometryEditor(TranslationService ts) {
        
       this.ts = ts;
       setMargin(false);
       setSpacing(false);
       
       txtX = new NumberField();
       txtX.setHasControls(true);
       txtX.setStep(1);
       txtX.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setX(listener.getValue());
           } 
       });
       HorizontalLayout lytX = createStyleEntry(ts.getTranslatedString("X"), txtX);
       
       txtY = new NumberField();
       txtY.setHasControls(true);
       txtY.setStep(1);
       txtY.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setY(listener.getValue());
           } 
       });
       HorizontalLayout lytY = createStyleEntry(ts.getTranslatedString("Y"), txtY);
       
       txtWidth = new NumberField();
       txtWidth.setHasControls(true);
       txtWidth.setStep(1);
       txtWidth.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setWidth(listener.getValue());
           } 
       });
       HorizontalLayout lytWidth = createStyleEntry(ts.getTranslatedString("Width"), txtWidth);
       
       txtHeight = new NumberField();
       txtHeight.setHasControls(true);
       txtHeight.setStep(1);
       txtHeight.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setHeight(listener.getValue());
           } 
       });
       HorizontalLayout lytHeight = createStyleEntry(ts.getTranslatedString("Height"), txtHeight);
      
       add(lytX, lytY, lytWidth, lytHeight);       
    }
    
    public void update(MxGraphCell cell) {
        updateControlsVisibility(cell);
        if (cell == null)
            return;
        currentMxGraphCell = cell;
        
        txtX.setValue(cell.getX());
        txtY.setValue(cell.getY());
        txtWidth.setValue(cell.getWidth());
        txtHeight.setValue(cell.getHeight());
     }
     
    public void updateControlsVisibility(MxGraphCell cell) {
         if (cell == null)
             getChildren().forEach(item -> item.setVisible(false));             
         else 
             getChildren().forEach(item -> item.setVisible(true));                     
    }
    
    private HorizontalLayout createStyleEntry(String label, Component... components) {
        BoldLabel lblTitle = new BoldLabel(label);
        HorizontalLayout lytEntry = new HorizontalLayout(lblTitle);
        lytEntry.add(components);
        lytEntry.setWidthFull();
        lytEntry.setAlignItems(Alignment.CENTER);
        lytEntry.setFlexGrow(1, lblTitle);
        return lytEntry;
    }     
    
}
