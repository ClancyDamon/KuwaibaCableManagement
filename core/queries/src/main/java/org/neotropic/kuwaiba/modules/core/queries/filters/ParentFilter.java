/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class ParentFilter extends AbstractFilter{

    ComboBox<ClassMetadataLight> cbxFilter;
    List<ClassMetadataLight> items;
    private String className;
     
    public ParentFilter(TranslationService ts, List<ClassMetadataLight> items) {
        super(ts);
        this.items = items;           
        cbxFilter = new ComboBox<>(ts.getTranslatedString("module.general.labels.class-name"));
        cbxFilter.setItems(items);  
        cbxFilter.setWidthFull();
        cbxFilter.addValueChangeListener(listener-> {
            this.value = cbxFilter.getValue().getId() + "";
            this.className = cbxFilter.getValue().getName();
        });
        cbxFilter.setItemLabelGenerator(item -> item.getName());
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    @Override
    public Component getComponent() {
        return new VerticalLayout(cbxFilter);
    }

    @Override
    public String getValueAsString() {
        return cbxFilter.getValue() == null ? "NULL" :  cbxFilter.getValue().getName();
    }

    @Override
    public boolean isValid() {
        return cbxFilter.getValue() != null;
    }

    public List<ClassMetadataLight> getItems() {
        return items;
    }

    public void setItems(List<ClassMetadataLight> items) {
        this.items = items;
    }
    
    

}
