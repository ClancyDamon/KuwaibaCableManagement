/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.Arrays;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class StringFilter extends AbstractFilter{

    ComboBox<Criteria> cboxTypeFilter;
    TextField txtFilter;
     
    public StringFilter(TranslationService ts) {
        super(ts);
        cboxTypeFilter = new ComboBox(ts.getTranslatedString("module.queries.filter-type"));
        cboxTypeFilter.setWidthFull();
        cboxTypeFilter.setItems(Arrays.asList(Criteria.CONTAINS, Criteria.EXACT_MATCH));  
        cboxTypeFilter.setItemLabelGenerator(itemLabelGenerator -> {
            return itemLabelGenerator.label();
        });
        cboxTypeFilter.addValueChangeListener(listener-> {
            this.criteria = cboxTypeFilter.getValue();
        });
        txtFilter = new TextField(ts.getTranslatedString("module.general.labels.value"));
        txtFilter.setWidthFull();
        txtFilter.addValueChangeListener(listener -> {
            this.value = listener.getValue();
        });
    }

    @Override
    public Component getComponent() {   
        return new VerticalLayout(cboxTypeFilter, txtFilter);
    }

    @Override
    public String getValueAsString() {
        if (txtFilter.getValue() == null || txtFilter.getValue().isEmpty())
            return "is Empty Value";
        else return (cboxTypeFilter.getValue().equals(Criteria.EXACT_MATCH) ? "is Equals to " : cboxTypeFilter.getValue().label()) + " " + txtFilter.getValue();
    }

    @Override
    public boolean isValid() {
        return cboxTypeFilter.getValue() != null && txtFilter.getValue() != null;
    }

}
