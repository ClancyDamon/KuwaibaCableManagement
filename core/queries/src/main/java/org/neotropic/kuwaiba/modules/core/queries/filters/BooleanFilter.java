/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class BooleanFilter extends AbstractFilter{

    RadioButtonGroup rdFilter;
     
    public BooleanFilter(TranslationService ts) {
        super(ts);        
        this.criteria = Criteria.EQUAL;
        rdFilter = new RadioButtonGroup();
        rdFilter.setItems("True", "False");
        value = "False";
        rdFilter.addValueChangeListener(list -> {
            value = (String) list.getValue();
        });
    }

    @Override
    public Component getComponent() {  
        Label lblValue = new Label("Value");
        return new VerticalLayout(lblValue ,rdFilter);
    }

    @Override
    public String getValueAsString() {
        return " is " + (String) rdFilter.getValue();
    }

    @Override
    public boolean isValid() {
        return rdFilter.getValue() != null;
    }

}
