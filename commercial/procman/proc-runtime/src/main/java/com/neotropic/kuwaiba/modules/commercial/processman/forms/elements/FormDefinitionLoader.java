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
import java.io.File;
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
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessDefinitionLoader;
/**
 * This class reads an structure xml and create the form elements containment 
 * hierarchy.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormDefinitionLoader {
    public static final QName TAG_ROOT = new QName(Constants.Tag.ROOT);
    public static final QName TAG_FORM = new QName(Constants.Tag.FORM);
    public static final QName TAG_GRID_LAYOUT = new QName(Constants.Tag.GRID_LAYOUT);
    public static final QName TAG_I18N = new QName(Constants.Tag.I18N);
    public static final QName TAG_LABEL = new QName(Constants.Tag.LABEL);
    public static final QName TAG_TEXT_FIELD = new QName(Constants.Tag.TEXT_FIELD);
    public static final QName TAG_VERTICAL_LAYOUT = new QName(Constants.Tag.VERTICAL_LAYOUT);
    public static final QName TAG_TEXT_AREA = new QName(Constants.Tag.TEXT_AREA);
    public static final QName TAG_DATE_FIELD = new QName(Constants.Tag.DATE_FIELD);
    public static final QName TAG_COMBO_BOX = new QName(Constants.Tag.COMBO_BOX);
    public static final QName TAG_GRID = new QName(Constants.Tag.GRID);
    public static final QName TAG_BUTTON = new QName(Constants.Tag.BUTTON);
    public static final QName TAG_SUBFORM = new QName(Constants.Tag.SUBFORM);
    public static final QName TAG_HORIZONTAL_LAYOUT = new QName(Constants.Tag.HORIZONTAL_LAYOUT);
    public static final QName TAG_IMAGE = new QName(Constants.Tag.IMAGE);
    public static final QName TAG_SCRIPT = new QName(Constants.Tag.SCRIPT);
    public static final QName TAG_TREE = new QName(Constants.Tag.TREE);
    public static final QName TAG_LIST_SELECT_FILTER = new QName(Constants.Tag.LIST_SELECT_FILTER);
    public static final QName TAG_UPLOAD = new QName(Constants.Tag.UPLOAD);
    public static final QName TAG_MINI_APPLICATION = new QName(Constants.Tag.MINI_APPLICATION);
    public static final QName TAG_CHECK_BOX = new QName(Constants.Tag.CHECK_BOX);
    
    private final List<QName> containers;
    
    private ElementForm root;
        
    private final List<AbstractElement> elements = new ArrayList();
    private final ElementScript elementScript;
    private ElementI18N elementI18N;
    
    private final byte[] structure;
    private final String formDefinitionsDirectory;
        
    public FormDefinitionLoader(String formDefinitionsDirectory, byte[] structure, Consumer<FunctionRunnerException> consumerFuncRunnerEx, HashMap<String, Object> funcRunnerParams) {
        Objects.requireNonNull(formDefinitionsDirectory);
        this.formDefinitionsDirectory = formDefinitionsDirectory;
        
        this.structure = structure;        
        containers = new ArrayList();
        containers.add(TAG_GRID_LAYOUT);
        containers.add(TAG_VERTICAL_LAYOUT);
        containers.add(TAG_SUBFORM);
        containers.add(TAG_HORIZONTAL_LAYOUT);
        this.elementScript = new ElementScript(consumerFuncRunnerEx, funcRunnerParams);
    }
        
    public ElementForm getRoot() {
        return root;
    }
    
    private int createFormContaimentHierarchy(AbstractElement parent, XMLStreamReader reader, int event) throws XMLStreamException {
        
        while (reader.hasNext()) {
            event = reader.next();
            
            if (event == XMLStreamConstants.END_ELEMENT) {
                if (containers.contains(reader.getName()))
                    return event;
            }
            
            if (event == XMLStreamConstants.START_ELEMENT) {
                
                AbstractElement child = null;                
                
                if (reader.getName().equals(TAG_GRID_LAYOUT)) {
                    child = new ElementGridLayout();
                    
                }
                else if (reader.getName().equals(TAG_VERTICAL_LAYOUT)) {
                    child = new ElementVerticalLayout();
                    
                }
                else if (reader.getName().equals(TAG_SUBFORM)) {
                    child = new ElementSubform();
                    
                }
                else if (reader.getName().equals(TAG_HORIZONTAL_LAYOUT)) {
                    child = new ElementHorizontalLayout();
                    
                }
                else if (reader.getName().equals(TAG_LABEL)) {
                    child = new ElementLabel();
                    
                }
                else if (reader.getName().equals(TAG_TEXT_FIELD)) {
                    child = new ElementTextField();
                    
                }
                else if (reader.getName().equals(TAG_TEXT_AREA)) {
                    child = new ElementTextArea();
                    
                }
                else if (reader.getName().equals(TAG_DATE_FIELD)) {
                    child = new ElementDateField();
                    
                }
                else if (reader.getName().equals(TAG_COMBO_BOX)) {
                    child = new ElementComboBox();
                    
                }
                else if (reader.getName().equals(TAG_GRID)) {
                    child = new ElementGrid();
                    
                }
                else if (reader.getName().equals(TAG_BUTTON)) {
                    child = new ElementButton();
                    
                }
                else if (reader.getName().equals(TAG_IMAGE)) {
                    child = new ElementImage();
                    
                }
                else if (reader.getName().equals(TAG_TREE)) {
                    child = new ElementTree();
                    
                }
                else if (reader.getName().equals(TAG_LIST_SELECT_FILTER)) {
                    child = new ElementListSelectFilter();
                    
                }
                else if (reader.getName().equals(TAG_UPLOAD)) {
                    child = new ElementUpload();
                }
                else if (reader.getName().equals(TAG_MINI_APPLICATION)) {
                    child = new ElementMiniApplication();
                }
                else if (reader.getName().equals(TAG_CHECK_BOX)) {
                    child = new ElementCheckBox();
                }
                else if (reader.getName().equals(TAG_I18N)) {
                    return event;
                }
                else if (reader.getName().equals(TAG_SCRIPT)) {
                    return event;
                }
                                
                if (child != null) {
                    child.initFromXML(reader);
                    
                    elements.add(child);
                                                            
                    if (parent instanceof AbstractElementContainer)
                        ((AbstractElementContainer) parent).addChild(child);
                    
                    if (child instanceof AbstractElementContainer)
                        event = createFormContaimentHierarchy(child, reader, event);
                }
            }
        }
        return event;
    }    
    
    private void loadExternalScript(String srcs) {
        if (srcs != null) {
            
            String[] arraySrcs = srcs.split(" ");
            
            for (String src : arraySrcs) {
                
                File file = new File(formDefinitionsDirectory + "/form/scripts/" + src); //NOI18N
                byte [] externalScript = ProcessDefinitionLoader.getFileAsByteArray(file);

                if (externalScript != null) {

                    try {
                        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                        ByteArrayInputStream bais = new ByteArrayInputStream(externalScript);
                        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

                        while (reader.hasNext()) {

                            int event = reader.next();

                            if (event == XMLStreamConstants.START_ELEMENT) {

                                if (reader.getName().equals(TAG_SCRIPT))
                                    elementScript.initFromXML(reader);
                            }
                        }

                    } catch (XMLStreamException ex) {
                    }
                }
            }
        }
    }
    
    private void loadExternalI18n(String srcs) {
        if (srcs != null) {
            String[] sources = srcs.split(" ");
            for (String source : sources) {
                File file = new File(String.format("%s/form/i18n/%s", formDefinitionsDirectory, source)); //NOI18N
                byte[] i18n = ProcessDefinitionLoader.getFileAsByteArray(file);
                if (i18n != null) {
                    try {
                        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                        ByteArrayInputStream bais = new ByteArrayInputStream(i18n);
                        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

                        while (reader.hasNext()) {
                            int event = reader.next();
                            if (event == XMLStreamConstants.START_ELEMENT) {
                                if (reader.getName().equals(TAG_I18N))
                                    elementI18N.initFromXML(reader);
                            }
                        }
                    } catch(XMLStreamException ex) {
                    }
                }
            }
        }
    }
    
    public static ElementScript loadExternalScripts(String formDefinitionsDirectory, String srcs, Consumer<FunctionRunnerException> consumerFuncRunnerEx, HashMap<String, Object> funcRunnerParams) {
        if (srcs != null) {
            ElementScript result = new ElementScript(consumerFuncRunnerEx, funcRunnerParams);
            String[] externalScripts = srcs.split(" ");
            
            for (String externalScript : externalScripts) {
                File file = new File(formDefinitionsDirectory + "/form/scripts/" + externalScript); //NOI18N
                byte [] byteArray = ProcessDefinitionLoader.getFileAsByteArray(file);
                try {
                    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                    ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                    XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
                    
                    while (reader.hasNext()) {
                        int event = reader.next();
                        if (event == XMLStreamConstants.START_ELEMENT) {
                            if (reader.getName().equals(TAG_SCRIPT))
                                result.initFromXML(reader);
                        }
                    }
                } catch (XMLStreamException ex) {
                }
            }
            return result;
        }
        return null;
    }
    
    public void build() {
        
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais, "utf-8");
                                    
            while (reader.hasNext()) {
                int event = reader.next();
                
                if (event == XMLStreamConstants.START_ELEMENT) {
                    
                    if (reader.getName().equals(TAG_FORM)) {
                        root = new ElementForm();
                        root.initFromXML(reader);
                                                
                        elements.add(root);
                                                                                    
                        event = createFormContaimentHierarchy(root, reader, event);
                    }
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(TAG_I18N)) {
                        elementI18N = new ElementI18N();
                        
                        String src = reader.getAttributeValue(null, Constants.Attribute.SRC);
                        if (src != null)
                            loadExternalI18n(src);
                        
                        elementI18N.initFromXML(reader);
                    }
                    if (reader.getName().equals(TAG_SCRIPT)) {
                        String src = reader.getAttributeValue(null, Constants.Attribute.SRC);
                        
                        if (src != null)
                            loadExternalScript(src);
                        
                        elementScript.initFromXML(reader);
                    }
                }
            }
            reader.close();
            
            FormStructure formStructure = new FormStructure(elements, elementScript, elementI18N);
                    
                        
            for (AbstractElement element : elements)
                element.setFormStructure(formStructure);
                
            if (elementScript != null && elementScript.getFunctions() != null)
                elementScript.getFunctions().put(Constants.Function.I18N, new FunctionI18NRunner(elementI18N));
                
        } catch (XMLStreamException ex) {
            Logger.getLogger(FormDefinitionLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private List<AbstractElement> getElementChildrenRecursively(AbstractElement parentElement) {
        
        List<AbstractElement> elementChildren = new ArrayList();
        
        if (parentElement instanceof AbstractElementContainer) {
            
            AbstractElementContainer elementContainer = (AbstractElementContainer) parentElement;
            
            if (elementContainer.getChildren() != null) {
                                
                for (AbstractElement child : elementContainer.getChildren())
                    elementChildren.addAll(getElementChildrenRecursively(child));
            }
            else
                elementChildren.add(parentElement);
        }
        else
            elementChildren.add(parentElement);
        
        return elementChildren;
    }
    
    public void fireOnload(ScriptQueryExecutor scriptQueryExecutor) {
        for (Runner runner : elementScript.getFunctions().values())
            runner.setScriptQueryExecutor(scriptQueryExecutor);

        List<AbstractElement> subformsChildren = new ArrayList();
                
        for (AbstractElement element : elements) {
            if (element instanceof ElementSubform)
                subformsChildren.addAll(getElementChildrenRecursively(element));
        }
                        
        for (AbstractElement element : elements) {
            if (!subformsChildren.contains(element))
                element.fireOnLoad();
        }
    }
    
    
    
}
