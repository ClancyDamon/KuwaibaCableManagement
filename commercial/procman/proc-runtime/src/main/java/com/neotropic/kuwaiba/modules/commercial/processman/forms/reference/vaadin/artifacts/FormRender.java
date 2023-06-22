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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.AbstractUiElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.UiElementContainer;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.AbstractElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.AbstractElementContainer;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.Constants;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementForm;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementMiniApplication;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementSubform;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FormDefinitionLoader;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FormStructure;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.components.ComponentFactory;
import com.neotropic.kuwaiba.modules.commercial.processman.scripts.ScriptQueryExecutorImpl;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.layout.GridLayout;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormRender extends Div {
    private final VerticalLayout content;
    private final FormDefinitionLoader formLoader;
    private final HashMap<Component, AbstractUiElement<? extends AbstractElement, ? extends Component>> components = new HashMap();
    private final ProcessInstance processInstance;
    private final ComponentFactory componentFactory;
    
    public FormRender(FormDefinitionLoader formLoader, ProcessInstance processInstance, String processEnginePath, TranslationService ts) {
                        
        this.formLoader = formLoader;
        this.processInstance = processInstance;
        
        if (formLoader.getRoot() != null && 
            formLoader.getRoot().getFormStructure() != null &&
            formLoader.getRoot().getFormStructure().getElementI18N() != null) {
            formLoader.getRoot().getFormStructure().getElementI18N().setLang(ts.getCurrentLanguage().toString());
        }
        setSizeFull();
        content = new VerticalLayout();
        content.setSizeFull();
        add(content);
        
        this.componentFactory = new ComponentFactory(processEnginePath);
    }
    
    public FormStructure getFormStructure() {
        
        if (formLoader.getRoot() != null)
            return formLoader.getRoot().getFormStructure();
        
        return null;
    }
    
    public void render(ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts/*WebserviceBean wsBean, RemoteSession session*/) {
        //TODO: search uses and implemetation for ObjectHierarchyProvider
////        ObjectHierarchyProvider.getInstance().setWebserviceBeanLocal(wsBean);
////        ObjectHierarchyProvider.getInstance().setRemoteSession(session);

        content.removeAll();
        
        
////        ComponentFactory.getInstance().setWebserviceBean(wsBean);
        
        renderRecursive(formLoader.getRoot(), content);
        //TODO: Implement the scripted query executor
        Session session = UI.getCurrent().getSession().getAttribute(Session.class);
        formLoader.fireOnload(new ScriptQueryExecutorImpl(processInstance, session, aem, bem, mem, ts));
    }
        
    private void renderRecursive(AbstractElement parentElement, Component parentComponent) {
        
        if (parentElement instanceof AbstractElementContainer && 
            ((AbstractElementContainer) parentElement).getChildren() != null) {
            
            for (AbstractElement childElement : ((AbstractElementContainer) parentElement).getChildren()) {

                Component childComponent = null;

                if (childElement instanceof ElementForm) {
                } else {
                    AbstractUiElement<? extends AbstractElement, ? extends Component> childGraphicalComponent = componentFactory.getUiElement(childElement);
                    
                    if (childGraphicalComponent == null)
                        continue;
                    
                    childComponent = childGraphicalComponent.getUiElement();
                    
                    if (childComponent == null)
                        continue;
                    
                    components.put(childComponent, childGraphicalComponent);
                                        
                    if (components.get(parentComponent) instanceof UiElementContainer)
                        ((UiElementContainer) components.get(parentComponent)).addChildren(components.get(childComponent));
                }
                
                if (childComponent != null) {
                    
                    if (!(childElement instanceof ElementSubform) && 
                        !(childElement instanceof ElementMiniApplication && Constants.Attribute.Mode.DETACHED.equals(((ElementMiniApplication) childElement).getMode()))) {
                        
                        if (!childElement.isHidden()) {
                            if (parentComponent instanceof HasComponents) {
                                if (parentComponent instanceof GridLayout) {
                                    List<Integer> area = childElement.getArea();
                                    if (area != null) {
                                        if (area.size() == 2) {
                                            int x1 = area.get(0) + 1;
                                            int y1 = area.get(1) + 1;
                                            ((GridLayout) parentComponent).add(childComponent, x1, y1);
                                        } else if (area.size() == 4) {
                                            int x1 = area.get(0) + 1;
                                            int y1 = area.get(1) + 1;
                                            int x2 = area.get(2) + 2;
                                            int y2 = area.get(3) + 1;
                                            ((GridLayout) parentComponent).add(childComponent, x1, y1, x2, y2);
                                        }
                                    }
                                    else
                                        ((HasComponents) parentComponent).add(childComponent);
                                }
                                else
                                    ((HasComponents) parentComponent).add(childComponent);
                            }
                            if (childElement.getAlignment() != null)
                                setAligment(childElement.getAlignment(), childComponent);
                        }
                    }
                    renderRecursive(childElement, childComponent);
                }
            }
        }
    }
    
    private void setAligment(String key, Component component) {
        Objects.requireNonNull(component);
        switch (key) {
            case Constants.Property.Alignment.BOTTOM_CENTER:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.END.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.CENTER.toString());
            break;
            case Constants.Property.Alignment.BOTTOM_LEFT:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.END.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.START.toString());
            break;
            case Constants.Property.Alignment.BOTTOM_RIGHT:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.END.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.END.toString());
            break;
            case Constants.Property.Alignment.MIDDLE_CENTER:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.CENTER.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.CENTER.toString());
            break;
            case Constants.Property.Alignment.MIDDLE_LEFT:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.CENTER.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.START.toString());
            break;
            case Constants.Property.Alignment.MIDDLE_RIGHT:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.CENTER.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.END.toString());
                
            break;
            case Constants.Property.Alignment.TOP_CENTER:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.START.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.CENTER.toString());
                
            break;
            case Constants.Property.Alignment.TOP_LEFT:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.START.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.START.toString());
            break;
            case Constants.Property.Alignment.TOP_RIGHT:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.START.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.END.toString());
            break;
            default:
                component.getElement().getStyle().set("align-self", GridLayout.Alignment.START.toString());
                component.getElement().getStyle().set("justify-self", GridLayout.Alignment.END.toString());
        }
    }
}
