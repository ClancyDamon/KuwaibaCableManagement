/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * POJO wrapper of a <b>grid</b> element in a Form Artifact Definition.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ElementGrid extends AbstractElement<Object> {
    public static final String SELECTION_MODE_SINGLE = "single";
    public static final String SELECTION_MODE_MULTI = "multi";
    public static final String SELECTION_MODE_NONE = "none";
    
    private List<ElementColumn> columns;
    private List<List<Object>> rows;
    private boolean shared = false;
    private boolean usePagination = false; // attribute to determine if pagination will be used on the grid
    private long selectedRow = -1;
    private String selectionMode;
    private List<Long> selectedRows;
        
    public ElementGrid() {
        
    }
    
    public long getSelectedRow() {
        return selectedRow;
    }
        
    public void setSelectedRow(long selectedRow) {
        this.selectedRow = selectedRow;
    }
        
    public void setColumns(List<ElementColumn> columns) {
        this.columns = columns;        
    }
    
    public List<ElementColumn> getColums() {
        return columns;
    }
    
    public List<List<Object>> getRows() {
        return rows;
    }
    
    public List<Long> getSelectedRows() {
        return selectedRows != null ? selectedRows : Collections.EMPTY_LIST;
    }
    
    public void setSelectedRows(List<Long> selectedRows) {
        this.selectedRows = selectedRows;
    }
    
    public boolean addRow(List<Object> row) {
        if (row == null)                
            return false;
        
        if (rows == null)
            rows = new ArrayList();
        
        rows.add(row);
        
        return true;
    }
    
    public boolean editRow(List<Object> newRow, long rowToEdit) {
        
        if (newRow != null && rows != null && rowToEdit != -1 && rowToEdit < rows.size()) {
            
            List<Object> oldRow = rows.get((int) rowToEdit);
            
            int oldRowSize = oldRow.size();
                    
            for (int i = 0; i < newRow.size(); i += 1) {
                
                if (i < oldRowSize)
                    oldRow.set(i, newRow.get(i));
                else
                    oldRow.add(newRow.get(i));
            }
            return true;
        }
        return false;
    }
    
    public boolean removeRow(long rowToRemove) {        
        if (rows != null && rowToRemove != -1 && rowToRemove < rows.size()) {
            rows.remove((int) rowToRemove);
            return true;
        }
        return false;
    }
    
    public void setRows(List<List<Object>> rows) {
        this.rows = rows;
    }
    
    public boolean isShared() {
        return shared;
    }
    
    public void setShared(boolean shared) {
        this.shared = shared;        
    }
    
    public List<Object> getRow() {
        return getRow(Integer.valueOf(String.valueOf(selectedRow)));
    }    
    
    public List<Object> getRow(int idRow) {
        if (rows != null) {
            
            if (idRow >= 0 && idRow < rows.size())
                return rows.get(idRow);
        }
        return null;
    }
    
    public Object getData(int idRow, int idColumn) {
        if (rows != null) {
            
            if (idRow >= 0 && idRow < rows.size()) {
                
                List<Object> row = rows.get(idRow);
                
                if (idColumn >= 0 && idColumn < row.size()) {
                    
                    Object data = row.get(idColumn);
                    
                    if (data != null)
                        return data;
                }
            }
        }
        return null;        
    }
    
    public String getSelectionMode() {
        return selectionMode;        
    }
        
    public void setSelectionMode(String selectionMode) {
        this.selectionMode = selectionMode;        
    }

    public boolean isUsePagination() {
        return usePagination;
    }
            
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        setShared(reader);
        setSelectionMode(reader);
        setUsePagination(reader);
        
        columns = new ArrayList();
        QName tagGrid = new QName(Constants.Tag.GRID);
        QName tagColumn = new QName(Constants.Tag.COLUMN);        
        
        while (true) {
            reader.nextTag();
                        
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(tagColumn)) {
                    ElementColumn column = new ElementColumn();
                    column.initFromXML(reader);
                                        
                    columns.add(column);
                }
            }
            
            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                
                if (reader.getName().equals(tagGrid))
                    return;
            }
        }
    }
    
    private void setShared(XMLStreamReader reader) {
        shared = Boolean.valueOf(reader.getAttributeValue(null, Constants.Attribute.SHARED));
    }
    
    private void setUsePagination(XMLStreamReader reader) {
        usePagination = Boolean.valueOf(reader.getAttributeValue(null, Constants.Attribute.USE_PAGINATION));
    }
    
    public void setSelectionMode(XMLStreamReader reader) {
        selectionMode = reader.getAttributeValue(null, Constants.Attribute.SELECTION_MODE);
    }
    
    @Override
    public void propertyChange() {
        if (hasProperty(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.ROWS)) {
            List<String> list = getEvents().get(Constants.EventAttribute.ONPROPERTYCHANGE).get(Constants.Property.ROWS);
            loadValue(list);
        }
        else if (hasProperty(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.SELECTED_ROW)) {
            long oldValue = getSelectedRow();
            long newValue = (long) getNewValue(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.SELECTED_ROW);

            setSelectedRow(newValue);

            firePropertyChangeEvent();

            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.SELECTED_ROW, newValue, oldValue));
        }
        else {
            super.propertyChange();
        }
    }
    
    @Override
    public void onUiElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (event.getNewValue() != null || event.getOldValue() != null) {
                
                if (Constants.Property.SELECTED_ROW.equals(event.getPropertyName())) {
                    setSelectedRow((long) event.getNewValue());
                    firePropertyChangeEvent();
                }
                if (Constants.Property.ROWS.equals(event.getPropertyName())) {
                    setRows((List<List<Object>>) event.getNewValue());
                    firePropertyChangeEvent();
                }
                if (Constants.Property.SELECTED_ROWS.equals(event.getPropertyName())) {
                    setSelectedRows(
                        event.getNewValue() instanceof List ? 
                        (List) event.getNewValue() : 
                        Collections.EMPTY_LIST);
                    firePropertyChangeEvent();
                }
            }
        }
        super.onUiElementEvent(event);        
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.GRID;
    }
    
    @Override
    public void fireOnLoad() {
        super.fireOnLoad(); 
        
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.ROWS)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.ROWS);
            
            loadValue(list);
        }                        
    }
    
    public final void loadValue(List<String> list) {
        if (list != null && !list.isEmpty()) {
            List<List<Object>> oldRows = getRows();

            String functionName = list.get(0);

            Runner runner = getFormStructure().getElementScript().getFunctionByName(functionName);
            if (runner == null) {
                System.out.println("[Process Engine] Function with name " + functionName + " is not found");
                return;
            }
            List parameters = new ArrayList();

            for (int i = 1; i < list.size(); i += 1) {
                AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                
                if (anElement == null) {
                    if (getFormStructure().getElementScript() != null && 
                        getFormStructure().getElementScript().getFunctions() != null) {

                        if (getFormStructure().getElementScript().getFunctions().containsKey(list.get(i))) {

                            Runner paramRunner = getFormStructure().getElementScript().getFunctions().get(list.get(i));

                            if (paramRunner != null) {
                                parameters.add(paramRunner);
                                continue;
                            }
                        }
                    }
                }
                parameters.add(anElement != null ? anElement : list.get(i));
            }

            Object newValue = runner.run(parameters);
            
            if (newValue != null) {
                
                setRows((List<List<Object>>) newValue);

                fireElementEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.ROWS, 
                    getRows(), 
                    oldRows));
            }
        }
    }
    
    @Override
    public boolean hasProperty(String propertyName) {
        switch (propertyName) {
            case Constants.Property.ROWS:
                return true;
            case Constants.Property.SELECTED_ROW:
                return true;
            default:
                return super.hasProperty(propertyName);
        }
    }
    
    @Override
    public Object getPropertyValue(String propertyName) {
        switch (propertyName) {
            case Constants.Property.ROWS:
                return getRows();
            default:
                return super.getPropertyValue(propertyName);
        }
    }  
    
}
