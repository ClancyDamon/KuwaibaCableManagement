/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.navigation.navtree;

import org.neotropic.util.visual.grids.BusinessObjectLightGridFilter;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

/**
 * Holds a set of objects of the same class name in the navigation module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 * @param <T> This is a 
 */
public class NavResultGrid<T> extends Grid<T>{
    /**
     * The class name of the objects in the grid
     */
    private String className;
    /**
     * The searched text is added to the filter
     */
    private final String searchedText;
    /**
     * filter for the grid only use name to filter 
     */
    private final BusinessObjectLightGridFilter gridFilter;
    /**
     * To use the filter and the provider in the grid
     */
    private ConfigurableFilterDataProvider<T, Void, BusinessObjectLightGridFilter> dpConfigurableFilter;
    /**
     * The first column of the grid for filter the name
     */
    private Grid.Column<T> firstColumn;

    private TextField txtFilterField;
    /**
     * Reference to the business entity manager
     */
    private final BusinessEntityManager bem;
    /**
     * The grid provider
     */
    private CallbackDataProvider<T, BusinessObjectLightGridFilter> provider;
    /**
     * The initial results when the grid results are showed for first time
     * before using the filters
     */
    private final List<T> initialResults;
    
    public NavResultGrid(BusinessEntityManager bem, String className, 
            String searchedText, List<T> initialResults) {
        super();
        this.bem = bem;
        this.className = className;
        this.txtFilterField = new TextField();
        this.setSelectionMode(Grid.SelectionMode.SINGLE);
        this.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        this.addThemeVariants(GridVariant.LUMO_COMPACT);
        this.searchedText = searchedText;
        this.initialResults = initialResults;
        this.gridFilter = new BusinessObjectLightGridFilter();
    }
    
    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public void setClassName(String className) {
        this.className = className;
    }

    public ConfigurableFilterDataProvider<T, Void, BusinessObjectLightGridFilter> getDpConfigurableFilter() {
        return dpConfigurableFilter;
    }

    public void setDpConfigurableFilter(ConfigurableFilterDataProvider<T, Void, BusinessObjectLightGridFilter> dpConfigurableFilter) {
        this.dpConfigurableFilter = dpConfigurableFilter;
    }

    public void setFirstColumn(Column<T> firstColumnsMap) {
        this.firstColumn = firstColumnsMap;
    }

    public TextField getObjNameField() {
        return txtFilterField;
    }

    public void setObjNameField(TextField objNameField) {
        this.txtFilterField = objNameField;
    }
    
    /**
     * Creates the filter for every grid in the header row 
     */
    public void createGridFilter(){
        HeaderRow filterNameRow = appendHeaderRow();
        // object name filter
        txtFilterField.addValueChangeListener(event -> {
            if(!event.getValue().isEmpty())
                gridFilter.setBusinessObjectName(event.getValue());
            else
                gridFilter.setBusinessObjectName(null);

           dpConfigurableFilter.refreshAll();
        });

        txtFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterNameRow.getCell(firstColumn).setComponent(txtFilterField);
        txtFilterField.setSizeFull();
        txtFilterField.setPlaceholder(" ...");
    }
        
    /**
     * Creates a provider for the grid, by default the filter limited to name
     */
    public void createDataProviderPaginateGrid(){
        provider = DataProvider.fromFilteringCallbacks(query -> {   
            BusinessObjectLightGridFilter filter = query.getFilter().orElse(null);
            HashMap<String, String> valuesToFilter = new HashMap<>();
            //this would be executed only the first time
            if(filter == null || filter.getBusinessObjectName() == null){
                for (T obj : initialResults) {
                    if(((BusinessObjectLight)obj).getName().toLowerCase().contains(searchedText.toLowerCase()) 
                            && !className.toLowerCase().contains(searchedText.toLowerCase())){
                        valuesToFilter.put(Constants.PROPERTY_NAME, searchedText);
                        break;
                    }
                }
            }
            else
                valuesToFilter.put(Constants.PROPERTY_NAME, filter.getBusinessObjectName());
            
            List<T> objs = new ArrayList<>();
            try {
                objs.addAll((Collection<? extends T>) bem.getObjectsOfClassLight(className, valuesToFilter, 
                    query.getOffset(), query.getLimit()));
                return objs.stream();
            }catch (InvalidArgumentException | MetadataObjectNotFoundException ex){
                return objs.stream();
            }
        }, query -> {
            try {
                BusinessObjectLightGridFilter filter = query.getFilter().orElse(null);
                HashMap<String, String> filters = new HashMap<>();
                //this should be executed only the first time
                 if(filter == null || filter.getBusinessObjectName() == null){
                    for (T obj : initialResults) {
                        if(((BusinessObjectLight)obj).getName().toLowerCase().contains(searchedText.toLowerCase()) 
                                && !className.toLowerCase().contains(searchedText.toLowerCase())){
                            filters.put(Constants.PROPERTY_NAME, searchedText);
                            break;
                        }
                    }
                }
                else
                    filters.put(Constants.PROPERTY_NAME, filter.getBusinessObjectName());
                
                int count = bem.getObjectsOfClassLight(className, filters, 
                            query.getOffset(), query.getLimit()).size();
                if(count <= 11)
                    setHeightByRows(true);
                return count;
            }catch (InvalidArgumentException | MetadataObjectNotFoundException ex){
                return 0;
            }
        });
        
        dpConfigurableFilter = provider.withConfigurableFilter();
        dpConfigurableFilter.setFilter(gridFilter);
        setDataProvider(dpConfigurableFilter);
    }
}
