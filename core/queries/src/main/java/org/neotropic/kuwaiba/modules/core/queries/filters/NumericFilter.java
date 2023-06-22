/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import java.util.Arrays;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class NumericFilter extends AbstractFilter{

    ComboBox<Criteria> cboxTypeFilter;
    NumberField txtFilter;
     
    public NumericFilter(TranslationService ts) {
        super(ts);
        cboxTypeFilter = new ComboBox(ts.getTranslatedString("module.queries.filter-type"));
        cboxTypeFilter.setItems(Arrays.asList(Criteria.EQUAL, 
                             Criteria.EQUAL_OR_GREATER_THAN,
                             Criteria.GREATER_THAN,
                             Criteria.EQUAL_OR_LESS_THAN,
                             Criteria.LESS_THAN));
        cboxTypeFilter.setWidthFull();
        cboxTypeFilter.setItemLabelGenerator(itemLabelGenerator -> {
            return itemLabelGenerator.label();
        });
        cboxTypeFilter.addValueChangeListener(listener-> {
            this.criteria = cboxTypeFilter.getValue();
        });
        
        txtFilter = new NumberField(ts.getTranslatedString("module.general.labels.value"));
        txtFilter.addValueChangeListener(listener -> {
            this.value = listener.getValue().toString();
        });
        txtFilter.setWidthFull();
    }

    @Override
    public Component getComponent() {  
        return new VerticalLayout(cboxTypeFilter, txtFilter);
    }

    @Override
    public String getValueAsString() {
        return  "is " + cboxTypeFilter.getValue().label() + " " + txtFilter.getValue();
    }

    @Override
    public boolean isValid() {
        return cboxTypeFilter.getValue() != null && txtFilter.getValue() != null;
    }

}
