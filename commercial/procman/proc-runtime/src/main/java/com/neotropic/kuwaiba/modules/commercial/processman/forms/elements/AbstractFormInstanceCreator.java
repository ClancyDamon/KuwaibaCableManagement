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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * Create an instance of a Form layout
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class AbstractFormInstanceCreator {
    private final FormStructure formStructure;
    HashMap<String, String> sharedInformation = new HashMap();
    
    public AbstractFormInstanceCreator(FormStructure formStructure) {
        this.formStructure = formStructure;
    }
    
    public HashMap<String, String> getSharedInformation() {
        return sharedInformation;        
    }
        
    public byte[] getStructure() {
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            xmlew.add(xmlef.createStartElement(FormDefinitionLoader.TAG_ROOT, null, null));
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VERSION, formStructure.getVersion());
            
            getStructureRecursive(xmlew, xmlef, formStructure.getElements().get(0));
                        
            xmlew.add(xmlef.createEndElement(FormDefinitionLoader.TAG_ROOT, null));
            
            xmlew.close();
            return baos.toByteArray();
            
        } catch (XMLStreamException ex) {
            Logger.getLogger(AbstractFormInstanceCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
        
    private void getStructureRecursive(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElement parent) throws XMLStreamException {
        if (parent != null) {
            String tagName = parent.getTagName();
            
            if (Constants.Tag.FORM.equals(tagName))
                tagName = Constants.Tag.FORM_INSTANCE;
                        
            QName tag = new QName(tagName);
            
            if (!parent.isSave())
                return;
            
            if (parent instanceof ElementSubform)
                return;
            
            xmlew.add(xmlef.createStartElement(tag, null, null));
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.ID, parent.getId());
            
            if (Constants.Tag.FORM_INSTANCE.equals(tagName))
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.FORM_ID, ((ElementForm) parent).getFormId());
            
            if (parent instanceof AbstractElementField) {
                
                AbstractElementField elementField = (AbstractElementField) parent;
                
                if (elementField.isShared()) {
                    
                    if (elementField.getId() != null && elementField.getValue() != null) {
                        
                        if (isRemoteObjectLight(elementField.getValue())) {

                            HashMap<String, String> info = getRemoteObjectLightInformation(elementField.getValue());

                            sharedInformation.put(elementField.getId() + Constants.Attribute.DATA_TYPE, info.get(Constants.Attribute.DATA_TYPE));
                            sharedInformation.put(elementField.getId() + Constants.Attribute.OBJECT_NAME, info.get(Constants.Attribute.OBJECT_NAME));
                            sharedInformation.put(elementField.getId() + Constants.Attribute.OBJECT_ID, info.get(Constants.Attribute.OBJECT_ID));
                            sharedInformation.put(elementField.getId() + Constants.Attribute.CLASS_ID, info.get(Constants.Attribute.CLASS_ID));
                            sharedInformation.put(elementField.getId() + Constants.Attribute.CLASS_NAME, info.get(Constants.Attribute.CLASS_NAME));

                        } else if (isAttachment(elementField.getValue())) {
                            HashMap<String, String> info = getRemoteObjectLightInformation(elementField.getValue());

                            sharedInformation.put(elementField.getId() + Constants.Attribute.DATA_TYPE, info.get(Constants.Attribute.DATA_TYPE));                                        
                            sharedInformation.put(elementField.getId() + Constants.Attribute.NAME, info.get(Constants.Attribute.NAME));
                            sharedInformation.put(elementField.getId() + Constants.Attribute.PATH, info.get(Constants.Attribute.PATH));
                            
                        } else if (isInventoryObjectPool(elementField.getValue())) {
                            HashMap<String, String> info = getInventoryObjectPoolInformation(elementField.getValue());
                            
                            sharedInformation.put(elementField.getId() + Constants.Attribute.DATA_TYPE, info.get(Constants.Attribute.DATA_TYPE));
                            sharedInformation.put(elementField.getId() + Constants.Attribute.POOL_ID, info.get(Constants.Attribute.POOL_ID));
                            
                        } else {
                            sharedInformation.put(elementField.getId(), elementField.getValue().toString());
                        }
                    }
                }
                if (elementField instanceof ElementUpload)
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CAPTION, ((ElementUpload) elementField).getCaption());
                
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, ((AbstractElementField) parent).getDataType());
                
                addTagAttributes(xmlew, xmlef, ((AbstractElementField) parent));
                
            } if (parent instanceof ElementGrid) {
                ElementGrid elementGrid = (ElementGrid) parent;
                
                List<List<Object>> rows = ((ElementGrid) parent).getRows();
                
                if (rows != null) {
                    
                    if (!rows.isEmpty()) {
                        
                        if (elementGrid.getId() != null && elementGrid.isShared()) {
                            int rowsCount = rows.size();
                            int columnsCount = rows.get(0).size();
                            
                            sharedInformation.put(elementGrid.getId() + "rowscount", String.valueOf(rowsCount));
                            sharedInformation.put(elementGrid.getId() + "columnscount", String.valueOf(columnsCount));
                            
                            for (int i = 0; i < rowsCount; i++) {
                                
                                for (int j = 0; j < columnsCount; j++) {                                 
                                    Object data = rows.get(i).get(j);
                                    
                                    sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.STRING);
                                    sharedInformation.put(elementGrid.getId() + i + j, data == null ? "" : data.toString());
                                    
                                    if (isRemoteObjectLight(data)) {
                                        
                                        HashMap<String, String> info = getRemoteObjectLightInformation(data);
                                                                                
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.DATA_TYPE, info.get(Constants.Attribute.DATA_TYPE));
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.OBJECT_NAME, info.get(Constants.Attribute.OBJECT_NAME));
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.OBJECT_ID, info.get(Constants.Attribute.OBJECT_ID));
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.CLASS_ID, info.get(Constants.Attribute.CLASS_ID));
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.CLASS_NAME, info.get(Constants.Attribute.CLASS_NAME));
                                                                                
                                    } else if (isAttachment(data)) {
                                        HashMap<String, String> info = getAttachmentInformation(data);
                                        
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.DATA_TYPE, info.get(Constants.Attribute.DATA_TYPE));                                        
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.NAME, info.get(Constants.Attribute.NAME));
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.PATH, info.get(Constants.Attribute.PATH));
                                    } else if (isInventoryObjectPool(data)) {
                                        HashMap<String, String> info = getInventoryObjectPoolInformation(data);
                                        
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.DATA_TYPE, info.get(Constants.Attribute.DATA_TYPE));
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.POOL_ID, info.get(Constants.Attribute.POOL_ID));
                                    } else if(isClass(data)) {
                                        HashMap<String, String> info = getClassInformation(data);
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.DATA_TYPE, info.get(Constants.Attribute.DATA_TYPE));
                                        sharedInformation.put(elementGrid.getId() + i + j + Constants.Attribute.NAME, info.get(Constants.Attribute.NAME));
                                    }
                                }
                            }
                        }
                                                                        
                        QName tagRows = new QName(Constants.Tag.ROWS);
                        xmlew.add(xmlef.createStartElement(tagRows, null, null));

                        for (List<Object> row : rows)
                            addGridRow(xmlew, xmlef, row);

                        xmlew.add(xmlef.createEndElement(tagRows, null));
                    }
                }
            } else if (parent instanceof AbstractElementContainer) {
                List<AbstractElement> children = ((AbstractElementContainer) parent).getChildren();

                if (children != null) {

                    for (AbstractElement child : children) {
                        getStructureRecursive(xmlew, xmlef, child);
                        
                    }
                }
            }
            xmlew.add(xmlef.createEndElement(tag, null));
        }
    }
    
    public void addGridRow(XMLEventWriter xmlew, XMLEventFactory xmlef, List<Object> row) throws XMLStreamException {
        QName tagRow = new QName(Constants.Tag.ROW);
        QName tagData = new QName(Constants.Tag.DATA);
                
        xmlew.add(xmlef.createStartElement(tagRow, null, null));
                        
        for (Object data : row) {
            
            xmlew.add(xmlef.createStartElement(tagData, null, null));
            xmlew.add(xmlef.createCharacters(data != null ? data.toString() : ""));
            xmlew.add(xmlef.createEndElement(tagData, null));
        }        
        xmlew.add(xmlef.createEndElement(tagRow, null));
    }
        
    /**
     * Add a set of attributes based on a given data type
     */
    private void addTagAttributes(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        if (element.getDataType() == null)
            return;
        
        switch(element.getDataType()) {
            case Constants.Attribute.DataType.ATTACHMENT:
                addAttachment(xmlew, xmlef, element.getValue());                
            break;
            case Constants.Attribute.DataType.INVENTORY_OBJECT_POOL:
                addInventoryObjectPool(xmlew, xmlef, element);
            break;
            case Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH:
                addRemoteObjectLight(xmlew, xmlef, element.getValue());
            break;
            case Constants.Attribute.DataType.CLASS_INFO_LIGTH:
                addClassInfoLight(xmlew, xmlef, element);
            break;
            case Constants.Attribute.DataType.STRING:
                if (element.getValue() instanceof String)
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VALUE, String.valueOf(element.getValue()));
            break;
            case Constants.Attribute.DataType.INTEGER:
                try {
                    
                    if (Integer.valueOf(String.valueOf(element.getValue())) instanceof Integer)                    
                        XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VALUE, String.valueOf(element.getValue()));
                    
                } catch(NumberFormatException nfe) {
                    
                }
            break;
            case Constants.Attribute.DataType.DATE:
                if (element.getValue() instanceof LocalDate) {
                    LocalDate localDate = (LocalDate) element.getValue();
                                        
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VALUE, localDate.toString());
                }
            break;
            case Constants.Attribute.DataType.BOOLEAN:
                if (element.getValue() instanceof Boolean)
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.VALUE, String.valueOf(element.getValue()));
            break;
        }
    }
    protected abstract void addInventoryObjectPool(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException;
    
    protected abstract void addRemoteObjectLight(XMLEventWriter xmlew, XMLEventFactory xmlef, Object object) throws XMLStreamException;
    
    protected abstract void addClassInfoLight(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException;
    
    protected abstract void addAttachment(XMLEventWriter xmlew, XMLEventFactory xmlef, Object object) throws XMLStreamException;
    
    protected abstract boolean isInventoryObjectPool(Object object);
    
    protected abstract boolean isRemoteObjectLight(Object object);
    
    protected abstract boolean isAttachment(Object object);
    
    protected abstract boolean isClass(Object object);
    
    protected abstract HashMap<String, String> getInventoryObjectPoolInformation(Object object);
    
    protected abstract HashMap<String, String> getRemoteObjectLightInformation(Object object);
    
    protected abstract HashMap<String, String> getAttachmentInformation(Object object);
    
    protected abstract HashMap<String, String> getClassInformation(Object object);
}
