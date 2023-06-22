/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.visualization.mxgraph;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.colorpicker.ColorPicker;
import org.neotropic.util.visual.general.BoldLabel;

/**
 * Basic property editor for mxGraph components
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class BasicStyleEditor extends VerticalLayout {

    List<Component> edgeControls;
    List<Component> vertexControls;
    MxGraphCell currentMxGraphCell;
    ColorPicker pkrStrokeColor;
    ColorPicker pkrFillColor;
    ColorPicker pkrFontColor;
    ComboBox<String> cmbFont;
    ComboBox<String> cmbLineType;
    Checkbox chkNoneFillColor;
    Checkbox chkRounded;
    NumberField txtStrokeWidth;
    NumberField txtFontSize;
    public static ArrayList<String> supportedNodeStyles;
    public static ArrayList<String> supportedEdgeStyles;
    TranslationService ts;
    
    public BasicStyleEditor(TranslationService ts) {
       this.ts = ts;
        
       supportedNodeStyles = new ArrayList(Arrays.asList(
                          MxConstants.STYLE_STROKECOLOR, MxConstants.STYLE_FILLCOLOR, MxConstants.STYLE_DASHED,
                          MxConstants.STYLE_ROUNDED, MxConstants.STYLE_STROKEWIDTH, MxConstants.STYLE_FONTSIZE,
                          MxConstants.STYLE_FONTCOLOR, MxConstants.STYLE_FONTFAMILY));
       supportedEdgeStyles = new ArrayList(Arrays.asList(
                          MxConstants.STYLE_STROKECOLOR, MxConstants.STYLE_DASHED,
                          MxConstants.STYLE_ROUNDED, MxConstants.STYLE_STROKEWIDTH, MxConstants.STYLE_FONTSIZE,
                          MxConstants.STYLE_FONTCOLOR, MxConstants.STYLE_FONTFAMILY));
       initControls();
    }
    
     public BasicStyleEditor(TranslationService ts, ArrayList supportedNodeStyles) {
       this.ts = ts;
        
       BasicStyleEditor.supportedNodeStyles = supportedNodeStyles;
       supportedEdgeStyles = new ArrayList(Arrays.asList(
                          MxConstants.STYLE_STROKECOLOR, MxConstants.STYLE_DASHED,
                          MxConstants.STYLE_ROUNDED, MxConstants.STYLE_STROKEWIDTH, MxConstants.STYLE_FONTSIZE,
                          MxConstants.STYLE_FONTCOLOR, MxConstants.STYLE_FONTFAMILY));
       initControls();
    }
    
    private void initControls() {
        
       setMargin(false);
       setSpacing(false);
       edgeControls = new ArrayList<>();
       vertexControls = new ArrayList<>();
        
       pkrStrokeColor = new ColorPicker();
       pkrStrokeColor.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setStyle(MxConstants.STYLE_STROKECOLOR, listener.getValue()); 
           } 
       });
       HorizontalLayout lytStrokeColor = createStyleEntry(ts.getTranslatedString("module.topoman.stroke-color"), pkrStrokeColor);
       if (supportedNodeStyles.contains(MxConstants.STYLE_STROKECOLOR))
           vertexControls.add(lytStrokeColor);
       if (supportedEdgeStyles.contains(MxConstants.STYLE_STROKECOLOR))
           edgeControls.add(lytStrokeColor);
       
       pkrFillColor = new ColorPicker();
       pkrFillColor.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
               chkNoneFillColor.setValue(false);
               currentMxGraphCell.setStyle(MxConstants.STYLE_FILLCOLOR, listener.getValue()); 
           } 
        });
       chkNoneFillColor = new Checkbox(ts.getTranslatedString("module.topoman.none"));
       chkNoneFillColor.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
               if (listener.getValue())
                   currentMxGraphCell.setStyle(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE); 
               else
                   currentMxGraphCell.setStyle(MxConstants.STYLE_FILLCOLOR, pkrFillColor.getValue());
           } 
        });
       HorizontalLayout lytFillColor = createStyleEntry(ts.getTranslatedString("module.topoman.fill-color"), pkrFillColor, chkNoneFillColor);  
       if (supportedNodeStyles.contains(MxConstants.STYLE_FILLCOLOR))
           vertexControls.add(lytFillColor);
     
       cmbLineType = new ComboBox<>();
       ArrayList<String> lineTypes = new ArrayList(Arrays.asList("Solid", "Dashed"));
       cmbLineType.setItems(lineTypes);
       cmbLineType.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setStyle(MxConstants.STYLE_DASHED, listener.getValue().equals("Dashed") ? "1" : "0"); 
           } 
        });
       HorizontalLayout lytDashed = createStyleEntry(ts.getTranslatedString("module.topoman.line-type"), cmbLineType);
       if (supportedNodeStyles.contains(MxConstants.STYLE_DASHED))
            vertexControls.add(lytDashed);
       if (supportedEdgeStyles.contains(MxConstants.STYLE_DASHED))
           edgeControls.add(lytDashed);
       
       chkRounded = new Checkbox();
       chkRounded.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setStyle(MxConstants.STYLE_ROUNDED, listener.getValue() ? "1" : "0"); 
           } 
        });
       HorizontalLayout lytRounded = createStyleEntry(ts.getTranslatedString("module.topoman.rounded-corners"), chkRounded);
       if (supportedNodeStyles.contains(MxConstants.STYLE_ROUNDED))
           vertexControls.add(lytRounded);
       if (supportedEdgeStyles.contains(MxConstants.STYLE_ROUNDED))
           edgeControls.add(lytRounded);
       
       txtStrokeWidth = new NumberField();
       txtStrokeWidth.setMin(1);
       txtStrokeWidth.setMax(50);
       txtStrokeWidth.setStep(1);
       txtStrokeWidth.setHasControls(true);
       txtStrokeWidth.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setStyle(MxConstants.STYLE_STROKEWIDTH, listener.getValue() + ""); 
           } 
        });
       HorizontalLayout lytStrokeWidth = createStyleEntry(ts.getTranslatedString("module.topoman.stroke-width"), txtStrokeWidth);
       if (supportedNodeStyles.contains(MxConstants.STYLE_STROKEWIDTH))
           vertexControls.add(lytStrokeWidth);
       if (supportedEdgeStyles.contains(MxConstants.STYLE_STROKEWIDTH))
           edgeControls.add(lytStrokeWidth);
       
       txtFontSize = new NumberField();
       txtFontSize.setMin(1);
       txtFontSize.setMax(500);
       txtFontSize.setStep(1);
       txtFontSize.setHasControls(true);
       txtFontSize.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setStyle(MxConstants.STYLE_FONTSIZE, listener.getValue() + ""); 
                   if (currentMxGraphCell.getIsVertex() && currentMxGraphCell.getShape().equals(MxConstants.SHAPE_LABEL))
                       currentMxGraphCell.updateCellSize();
           } 
        });
       HorizontalLayout lytFontSize = createStyleEntry(ts.getTranslatedString("module.topoman.font-size"), txtFontSize);
       if (supportedNodeStyles.contains(MxConstants.STYLE_FONTSIZE))
           vertexControls.add(lytFontSize);
       if (supportedEdgeStyles.contains(MxConstants.STYLE_FONTSIZE))
           edgeControls.add(lytFontSize);
       
       pkrFontColor = new ColorPicker();
       pkrFontColor.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setStyle(MxConstants.STYLE_FONTCOLOR, listener.getValue()); 
           } 
       });
       HorizontalLayout lytFontColor = createStyleEntry(ts.getTranslatedString("module.topoman.font-color"), pkrFontColor);
       if (supportedEdgeStyles.contains(MxConstants.STYLE_FONTCOLOR))
           edgeControls.add(lytFontColor);
       if (supportedNodeStyles.contains(MxConstants.STYLE_FONTCOLOR))
           vertexControls.add(lytFontColor);
       
       cmbFont = new ComboBox();
       cmbFont.setItems(getFonts());
       cmbFont.addValueChangeListener(listener -> {
           if (currentMxGraphCell != null) {
                   currentMxGraphCell.setStyle(MxConstants.STYLE_FONTFAMILY, listener.getValue()); 
           } 
       });
       HorizontalLayout lytFontFam = createStyleEntry(ts.getTranslatedString("module.topoman.font-family"), cmbFont);
       if (supportedEdgeStyles.contains(MxConstants.STYLE_FONTFAMILY))
           edgeControls.add(lytFontFam);
       if (supportedNodeStyles.contains(MxConstants.STYLE_FONTFAMILY))
           vertexControls.add(lytFontFam);
       
       add(lytStrokeColor, lytFillColor, lytFontFam, lytFontSize, lytFontColor, lytDashed, lytRounded, lytStrokeWidth);
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
    
    public void update(MxGraphCell cell) {
        updateControlsVisibility(cell);
        if (cell == null)
            return;
        currentMxGraphCell = cell;
        HashMap<String, String> styles = cell.getRawStyleAsMap();
        
        if (styles.containsKey(MxConstants.STYLE_STROKECOLOR))
            pkrStrokeColor.setValue(styles.get(MxConstants.STYLE_STROKECOLOR));
        
        if (styles.containsKey(MxConstants.STYLE_FILLCOLOR)) {
            if (styles.get(MxConstants.STYLE_FILLCOLOR).equals(MxConstants.NONE)) {
                chkNoneFillColor.setValue(true);
            } else {
                chkNoneFillColor.setValue(false);
                pkrFillColor.setValue(styles.get(MxConstants.STYLE_FILLCOLOR));
            }          
        } else {
            chkNoneFillColor.setValue(false);
            pkrFillColor.setValue("black");
        }
        if (styles.containsKey(MxConstants.STYLE_DASHED))
            cmbLineType.setValue(styles.get(MxConstants.STYLE_DASHED).equals("1") ? "Dashed" : "Solid");
        else 
            cmbLineType.setValue("Solid");
        if (styles.containsKey(MxConstants.STYLE_ROUNDED))
            chkRounded.setValue(styles.get(MxConstants.STYLE_ROUNDED).equals("1"));
        else 
            chkRounded.setValue(false);
        if (styles.containsKey(MxConstants.STYLE_STROKEWIDTH))
            txtStrokeWidth.setValue(Double.parseDouble(styles.get(MxConstants.STYLE_STROKEWIDTH)));
        else 
            txtStrokeWidth.setValue(1d);
        
        if (styles.containsKey(MxConstants.STYLE_FONTCOLOR))
            pkrFontColor.setValue(styles.get(MxConstants.STYLE_FONTCOLOR));
        if (styles.containsKey(MxConstants.STYLE_FONTSIZE))
            txtFontSize.setValue(Double.parseDouble(styles.get(MxConstants.STYLE_FONTSIZE)));
         else 
            txtFontSize.setValue(11d);
        if (styles.containsKey(MxConstants.STYLE_FONTFAMILY))
            cmbFont.setValue(styles.get(MxConstants.STYLE_FONTFAMILY));
         else 
            cmbFont.setValue(getFonts().get(0));
                   
    }

    public void updateControlsVisibility(MxGraphCell cell) {
         if (cell == null){
               getChildren().forEach(item -> item.setVisible(false));             
               return;
         }
         if (cell.getIsEdge()) {
             vertexControls.stream().forEach(item -> item.setVisible(false));
             edgeControls.stream().forEach(item -> item.setVisible(true));
         }
         else {
             edgeControls.stream().forEach(item -> item.setVisible(false));
             vertexControls.stream().forEach(item -> item.setVisible(true));
         }
    }

    private List<String> getFonts() {
         ArrayList<String> list = new ArrayList<>();
         list.add("Helvetica");
         list.add("Arial");
         list.add("Times New Roman");
         list.add("Times");
         list.add("Courier New");
         list.add("Courier");
         list.add("Verdana");
         list.add("Georgia");
         list.add("Palatino");
         list.add("Garamond");
         list.add("Bookman");
         list.add("Tahoma");
         list.add("Trebuchet MS");
         list.add("Arial Black");
         list.add("Impact");
         list.add("Comic Sans MS");

         return list;
    }
    
    
    
    
}
