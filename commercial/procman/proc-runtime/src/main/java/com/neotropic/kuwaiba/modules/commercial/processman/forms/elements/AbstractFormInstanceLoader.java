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

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Abstract class to define the procedure to load a form instance
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class AbstractFormInstanceLoader {
    private String formid;
    private final HashMap<String, Object> values;
    private final HashMap<String, String> captions;
    private final String formDefinitionsDirectory;
    private final Consumer<FunctionRunnerException> consumerFuncRunnerEx;
    private final HashMap<String, Object> funcRunnerParams;
            
    public AbstractFormInstanceLoader(String formDefinitionsDirectory, Consumer<FunctionRunnerException> consumerFuncRunnerEx, 
        HashMap<String, Object> funcRunnerParams) {
        Objects.requireNonNull(formDefinitionsDirectory);
        values = new HashMap();
        captions = new HashMap();
        this.formDefinitionsDirectory = formDefinitionsDirectory;
        this.consumerFuncRunnerEx = consumerFuncRunnerEx;
        this.funcRunnerParams = funcRunnerParams;
    }
    
    public abstract Object getInventoryObjectPool(String poolId);
    public abstract Object getRemoteObjectLight(long classId, String objectId);
    public abstract Object getClassInfoLight(long classId);
    public abstract Object getAttachment(String name, String path);
    
    private Object getValue(XMLStreamReader reader, String dataType) throws XMLStreamException {
        switch (dataType) {
            case Constants.Attribute.DataType.CLASS_INFO_LIGTH:
                
                String classId = reader.getAttributeValue(null, Constants.Attribute.CLASS_ID);
                
                String className = reader.getAttributeValue(null, Constants.Attribute.CLASS_NAME);
                                
                if (classId != null) {
                    
                    Object cli = getClassInfoLight(Long.valueOf(classId));
                    
                    if (cli != null)
                        return cli;
                }
                if (className != null) {
                    //TODO: In the null case, load only the name, remenber make a instance of the expected data type
                }
            break;
            case Constants.Attribute.DataType.INVENTORY_OBJECT_POOL:
                String poolId = reader.getAttributeValue(null, Constants.Attribute.POOL_ID);
                if (poolId != null) {
                    Object pool = getInventoryObjectPool(poolId);
                    if (pool != null)
                        return pool;
                }
            break;
            case Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH:
                                
                String objectId = reader.getAttributeValue(null, Constants.Attribute.OBJECT_ID);
                classId = reader.getAttributeValue(null, Constants.Attribute.CLASS_ID);
                
                String objectName = reader.getAttributeValue(null, Constants.Attribute.OBJECT_NAME);
                
                if (objectId != null && classId != null) {
                    
                    Object rol = getRemoteObjectLight(Long.valueOf(classId), objectId);
                                        
                    if (rol != null)
                        return rol;
                }
                                
                if (objectName != null) {
                    //TODO: In the null case, load only the name, remenber make a instance of the expected data type
                }
            break;
            case Constants.Attribute.DataType.ATTACHMENT:
                
                String fileName = reader.getAttributeValue(null, Constants.Attribute.NAME);
                String filePath = reader.getAttributeValue(null, Constants.Attribute.PATH);
                
                if (fileName != null && filePath != null)
                    return getAttachment(fileName, filePath);
            break;
            case Constants.Attribute.DataType.STRING:
                String value = reader.getAttributeValue(null, Constants.Attribute.VALUE);
                
                if (value == null)
                    value = reader.getElementText();//.getAttributeValue(null, Constants.Attribute.VALUE);
                                
                if (value != null)
                    return value;
            break;
            case Constants.Attribute.DataType.DATE:
                value = reader.getAttributeValue(null, Constants.Attribute.VALUE);
                
                if (value == null)
                    value = reader.getElementText();//.getAttributeValue(null, Constants.Attribute.VALUE);
                
                if (value != null && !value.isEmpty())
                    return LocalDate.parse(value);
            break;
            case Constants.Attribute.DataType.BOOLEAN:
                value = reader.getAttributeValue(null, Constants.Attribute.VALUE);
                
                if (value == null)
                    value = reader.getElementText();//.getAttributeValue(null, Constants.Attribute.VALUE);
                
                if (value != null)
                    return Boolean.valueOf(value);
            break;
            case Constants.Attribute.DataType.INTEGER:
                value = reader.getAttributeValue(null, Constants.Attribute.VALUE);
                
                if (value == null)
                    value = reader.getElementText();//.getAttributeValue(null, Constants.Attribute.VALUE);
                                
                if (value != null)
                    return Integer.valueOf(value);
            break;
            default:
                return null;
        }
        return null;
    }
        
    public FormDefinitionLoader load(byte[] definition, byte[] content) {

        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            XMLStreamReader reader = xmlif.createXMLStreamReader(bais);
            
            QName tagGrid = new QName(Constants.Tag.GRID);
            QName tagRows = new QName(Constants.Tag.ROWS);
            QName tagRow = new QName(Constants.Tag.ROW);
            QName tagData = new QName(Constants.Tag.DATA);
            
            while (reader.hasNext()) {
                reader.next();
                
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        
                    if (formid == null)
                        formid = reader.getAttributeValue(null, Constants.Attribute.FORM_ID);
                    
                    String id = reader.getAttributeValue(null, Constants.Attribute.ID);
                    if (id != null) {
                        String dataType = reader.getAttributeValue(null, Constants.Attribute.DATA_TYPE);
                        String caption = reader.getAttributeValue(null, Constants.Attribute.CAPTION);
                        
                        if (dataType != null) {
                            
                            Object value = getValue(reader, dataType);
                                                        
                            if (value != null)
                                values.put(id, value);
                        }
                        
                        if (caption != null)
                            captions.put(id, caption);
                        
                        if (reader.getName().equals(tagGrid)) {
                            
                            List<List<Object>> rows = new ArrayList();
                            
                            while (true) {
                                
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                    
                                    if (reader.getName().equals(tagRow)) {
                                        
                                        List<Object> row = new ArrayList();
                                                                                
                                        while (true) {
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                                
                                                if (reader.getName().equals(tagData)) {
                                                    
                                                    String rowDataType = reader.getAttributeValue(null, Constants.Attribute.DATA_TYPE);
                                                                                                        
                                                    if (rowDataType != null) {

                                                        Object value = getValue(reader, rowDataType);
                                                        
                                                        if (value != null)                                                            
                                                            row.add(value);
                                                        else
                                                            row.add(reader.getElementText());
                                                    }
                                                    else
                                                        row.add(reader.getElementText());
                                                }
                                                
                                            } if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                                                
                                                if (reader.getName().equals(tagRow))
                                                    break;
                                            }
                                            reader.next();
                                        }
                                        rows.add(row);
                                    }
                                }
                                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                                    
                                    if (reader.getName().equals(tagGrid) || reader.getName().equals(tagRows))
                                        break;
                                }
                                reader.next();
                            }
                            values.put(id, rows);
                        }
                    }
                }
            }
            reader.close();
                        
        } catch (XMLStreamException ex) {
            Logger.getLogger(AbstractFormInstanceLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (definition != null) {
            
            FormDefinitionLoader formLoader = new FormDefinitionLoader(formDefinitionsDirectory, definition, consumerFuncRunnerEx, funcRunnerParams);
            formLoader.build();

            FormStructure formStructure = formLoader.getRoot().getFormStructure();

            for (String id : values.keySet()) {
                
                AbstractElement element = formStructure.getElementById(id);
                
                if (element != null) {
                    if (element instanceof AbstractElementField) {
                        ((AbstractElementField) element).setValue(values.get(id));
                        
                        if (element instanceof ElementUpload)
                            ((ElementUpload) element).setCaption(captions.get(id));
                                                
                    } else if (element instanceof ElementGrid) {
                        ((ElementGrid) element).setRows((List<List<Object>>) values.get(id));
                        
                    }
                }
            }
            return formLoader;
        }
        return null;
    }    
}
