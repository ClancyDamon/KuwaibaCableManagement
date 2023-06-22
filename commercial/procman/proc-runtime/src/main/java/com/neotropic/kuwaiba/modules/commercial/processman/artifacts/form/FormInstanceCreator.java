/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.processman.artifacts.form;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.AbstractElementField;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.AbstractFormInstanceCreator;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Constants;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FileInformation;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FormStructure;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.XMLUtil;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Class to build the form instance content
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormInstanceCreator extends AbstractFormInstanceCreator {
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    public FormInstanceCreator(FormStructure formStructure, MetadataEntityManager mem, TranslationService ts) {
        super(formStructure);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        this.mem = mem;
        this.ts = ts;
    }
    
    @Override
    public void addGridRow(XMLEventWriter xmlew, XMLEventFactory xmlef, List<Object> row) throws XMLStreamException {
        QName tagRow = new QName(Constants.Tag.ROW);
        QName tagData = new QName(Constants.Tag.DATA);
                
        xmlew.add(xmlef.createStartElement(tagRow, null, null));
                        
        for (Object data : row) {
            
            xmlew.add(xmlef.createStartElement(tagData, null, null));
            
            if (data instanceof BusinessObjectLight) {
                BusinessObjectLight businessObject = (BusinessObjectLight) data;

                try {
                    ClassMetadata classMetadata = mem.getClass(businessObject.getClassName());
                    
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH);
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_ID, String.valueOf(businessObject.getId()));
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_NAME, businessObject.getName());
                    XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classMetadata.getId()));
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            }
            else if(data instanceof FileInformation) {
                
                FileInformation fileInfo = (FileInformation) data;
                
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.ATTACHMENT);
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.NAME, fileInfo.getName());
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.PATH, fileInfo.getPath());
                
            }
            else if (data instanceof String) {
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.STRING);
            }
            else if (data instanceof Integer) {
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.INTEGER);
            }
            else if (data instanceof LocalDate) {
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.DATE);
                
            } else if (data instanceof ClassMetadataLight) {
                ClassMetadataLight aClass = (ClassMetadataLight) data;
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.CLASS_INFO_LIGTH);
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(aClass.getId()));
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_NAME, aClass.getName());
            }
            xmlew.add(xmlef.createCharacters(data == null ? "" : data.toString()));
            xmlew.add(xmlef.createEndElement(tagData, null));
        }        
        xmlew.add(xmlef.createEndElement(tagRow, null));
    }

    @Override
    protected void addRemoteObjectLight(XMLEventWriter xmlew, XMLEventFactory xmlef, Object object) throws XMLStreamException {
        if (object instanceof BusinessObjectLight) {
                        
            BusinessObjectLight businessObject = (BusinessObjectLight) object;
                        
            try {
                ClassMetadata classMetadata = mem.getClass(businessObject.getClassName());
                
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_ID, String.valueOf(businessObject.getId()));
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.OBJECT_NAME, businessObject.getName());
                XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classMetadata.getId()));
                
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        }
    }
    
    @Override
    protected void addInventoryObjectPool(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        if (element.getValue() instanceof InventoryObjectPool) {
            InventoryObjectPool inventoryObjectPool = (InventoryObjectPool) element.getValue();
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.POOL_ID, inventoryObjectPool.getId());
        }
    }

    @Override
    protected void addClassInfoLight(XMLEventWriter xmlew, XMLEventFactory xmlef, AbstractElementField element) throws XMLStreamException {
        if (element.getValue() instanceof ClassMetadataLight) {
            
            ClassMetadataLight classMetadata = (ClassMetadataLight) element.getValue();
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_ID, String.valueOf(classMetadata.getId()));
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.CLASS_NAME, classMetadata.getName());
        }
    }
    
    @Override
    protected void addAttachment(XMLEventWriter xmlew, XMLEventFactory xmlef, Object object) throws XMLStreamException {
        if (object instanceof FileInformation) {
            
            FileInformation fileInfo = (FileInformation) object;
            
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.NAME, fileInfo.getName());
            XMLUtil.getInstance().createAttribute(xmlew, xmlef, Constants.Attribute.PATH, fileInfo.getPath());
        }
    }
    
    @Override
    protected boolean isInventoryObjectPool(Object object) {
        return object instanceof InventoryObjectPool;
    }
    
    @Override
    protected boolean isRemoteObjectLight(Object object) {
        return object instanceof BusinessObjectLight;
    }
    
    @Override
    protected boolean isAttachment(Object object) {
        return object instanceof FileInformation;
    }
    
    @Override
    protected boolean isClass(Object object) {
        return object instanceof ClassMetadataLight;
    }
    
    @Override
    protected HashMap<String, String> getInventoryObjectPoolInformation(Object object) {
        if (object instanceof InventoryObjectPool) {
            HashMap<String, String> info = new HashMap();
            info.put(Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.INVENTORY_OBJECT_POOL);
            info.put(Constants.Attribute.POOL_ID, ((InventoryObjectPool) object).getId());
            return info;
        }
        return null;
    }
    
    @Override
    protected HashMap<String, String> getRemoteObjectLightInformation(Object object) {
        try {
            
            if (object instanceof BusinessObjectLight) {
                
                BusinessObjectLight businessObject = (BusinessObjectLight) object;

                HashMap<String, String> info = new HashMap();
                
                ClassMetadata classMetadata = mem.getClass(businessObject.getClassName());
                
                info.put(Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH);
                info.put(Constants.Attribute.OBJECT_NAME, businessObject.getName());
                info.put(Constants.Attribute.OBJECT_ID, String.valueOf(businessObject.getId()));
                info.put(Constants.Attribute.CLASS_ID, String.valueOf(classMetadata.getId()));
                info.put(Constants.Attribute.CLASS_NAME, String.valueOf(classMetadata.getName()));
                
                return info;            
            }
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        return null;
    }
    
    @Override
    protected HashMap<String, String> getAttachmentInformation(Object object) {
        if (object instanceof FileInformation) {

            FileInformation fileInfo = (FileInformation) object;

            HashMap<String, String> info = new HashMap();

            info.put(Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.ATTACHMENT);
            info.put(Constants.Attribute.NAME, fileInfo.getName());
            info.put(Constants.Attribute.PATH, fileInfo.getPath());
            
            return info;
        }
        return null;
    }
    
    @Override
    protected HashMap<String, String> getClassInformation(Object object) {
        if (object instanceof ClassMetadataLight) {
            ClassMetadataLight aClass = (ClassMetadataLight) object;
            
            HashMap<String, String> info = new HashMap();
            info.put(Constants.Attribute.DATA_TYPE, Constants.Attribute.DataType.CLASS_INFO_LIGTH);
            info.put(Constants.Attribute.NAME, aClass.getName());
            
            return info;
        }
        return null;
    }
    
}
