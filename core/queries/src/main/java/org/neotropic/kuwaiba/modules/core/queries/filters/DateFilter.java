/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Arrays;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class DateFilter extends AbstractFilter{

    ComboBox<Criteria> cboxTypeFilter;
    DatePicker dateFilter;
     
    public DateFilter(TranslationService ts) {
        super(ts);
        cboxTypeFilter = new ComboBox(ts.getTranslatedString("module.queries.filter-type"));
        cboxTypeFilter.setItems(Arrays.asList(Criteria.EQUAL, 
                Criteria.GREATER_THAN, 
                Criteria.LESS_THAN));
        cboxTypeFilter.setItemLabelGenerator(itemLabelGenerator -> {
            return itemLabelGenerator.label();
        });
        cboxTypeFilter.setWidthFull();
        cboxTypeFilter.addValueChangeListener(listener-> {
            this.criteria = cboxTypeFilter.getValue();
        });
        dateFilter = new DatePicker(ts.getTranslatedString("module.general.labels.value"));
        dateFilter.addValueChangeListener(listener -> {
           value = listener.getValue().toString();
        });
        dateFilter.setWidthFull();
    }

    @Override
    public Component getComponent() {   
        return new VerticalLayout(cboxTypeFilter, dateFilter);
    }

    @Override
    public String getValueAsString() {
        return cboxTypeFilter.getValue() != null ? ("is " + cboxTypeFilter.getValue().label() + " " + dateFilter.getValue())
                : "";
    }

    @Override
    public boolean isValid() {
        return cboxTypeFilter.getValue() != null && dateFilter.getValue() != null;
    }

}
