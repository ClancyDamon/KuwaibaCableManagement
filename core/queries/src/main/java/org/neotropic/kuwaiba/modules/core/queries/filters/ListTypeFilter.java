/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ListTypeFilter extends AbstractFilter{

    ComboBox<BusinessObjectLight> cbxFilter;
    List<BusinessObjectLight> items;
    boolean useAdvancedSearch;
    private String className;
     
    public ListTypeFilter(TranslationService ts, List<BusinessObjectLight> items) {
        super(ts);
        this.items = items;           
        cbxFilter = new ComboBox<>();
        cbxFilter.setItems(items);  
        cbxFilter.setWidthFull();
        cbxFilter.addValueChangeListener(listener-> {
            this.value = cbxFilter.getValue().getId();
            this.className = cbxFilter.getValue().getClassName();
        });
        cbxFilter.setItemLabelGenerator(item -> item.getName());
    }

    public boolean isUseAdvancedSearch() {
        return useAdvancedSearch;
    }

    public void setUseAdvancedSearch(boolean useAdvancedSearch) {
        this.useAdvancedSearch = useAdvancedSearch;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    @Override
    public Component getComponent() {
        RadioButtonGroup rdioListType = new RadioButtonGroup();
        rdioListType.setItems(ts.getTranslatedString("module.queries.select-ltitem"), ts.getTranslatedString("module.queries.advanced-search"));
        rdioListType.setValue(ts.getTranslatedString("module.queries.select-ltitem"));
        rdioListType.addValueChangeListener(listener -> {
            useAdvancedSearch = listener.getValue().equals(ts.getTranslatedString("module.queries.advanced-search"));
            cbxFilter.setEnabled(!useAdvancedSearch);
        });
        return new VerticalLayout(rdioListType, cbxFilter);
    }

    @Override
    public String getValueAsString() {
        return cbxFilter.getValue() == null ? "NULL" :  "  is " + cbxFilter.getValue().getName();
    }

    @Override
    public boolean isValid() {
        return cbxFilter.getValue() != null;
    }

}
