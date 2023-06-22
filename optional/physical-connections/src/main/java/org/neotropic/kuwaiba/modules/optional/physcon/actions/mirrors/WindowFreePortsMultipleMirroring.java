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
package org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to generate multiple mirror using free ports.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowFreePortsMultipleMirroring extends AbstractWindowMirrorFreePorts {
    private final BusinessObjectLight businessObject;
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    private final Command cmdUpdateMirrors;
    
    public WindowFreePortsMultipleMirroring(BusinessObjectLight businessObject, BusinessEntityManager bem, TranslationService ts, Command cmdUpdateMirrors) {
        this.businessObject = businessObject;
        this.bem = bem;
        this.ts = ts;
        this.cmdUpdateMirrors = cmdUpdateMirrors;
    }
    
    @Override
    public void open() {
        try {
            setContentSizeFull();
            setWidth("70%");
            setHeight("70%");
            setDraggable(true);
            setResizable(true);
            setCloseOnOutsideClick(false);
            
            List<BusinessObjectLight> ports = bem.getChildrenOfClassLightRecursive(
                businessObject.getId(), businessObject.getClassName(), 
                Constants.CLASS_GENERICPORT, null, -1, -1
            );
            List<BusinessObjectLight> freePorts = new ArrayList();
            for (BusinessObjectLight port : ports) {
                if (!bem.hasSpecialRelationship(port.getClassName(), port.getId(), "mirror", 1)) //NOI18N
                    freePorts.add(port);
            }
            if (freePorts.isEmpty()) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.physcon.mirror-man.notification.info.no-free-ports-to-mirror-multiple"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
                return;
            }
            List<BusinessObjectLight> suffixInPorts = new ArrayList();
            List<BusinessObjectLight> suffixOutPorts = new ArrayList();
            List<BusinessObjectLight> suffixFrontPorts = new ArrayList();
            List<BusinessObjectLight> suffixBackPorts = new ArrayList();
            List<BusinessObjectLight> prefixInPorts = new ArrayList();
            List<BusinessObjectLight> prefixOutPorts = new ArrayList();
            List<BusinessObjectLight> prefixFrontPorts = new ArrayList();
            List<BusinessObjectLight> prefixBackPorts = new ArrayList();
            
            freePorts.forEach(freePort -> {
                String freePortName = freePort.getName().toLowerCase();
                if (freePortName.contains(SUFFIX_IN))
                    suffixInPorts.add(freePort);
                else if (freePortName.contains(SUFFIX_OUT)) {
                    try {
                        if (!bem.hasSpecialRelationship(freePort.getClassName(), freePort.getId(), "mirrorMultiple", 1))
                            suffixOutPorts.add(freePort);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR,
                            ts
                        ).open();
                    }
                }
                else if (freePortName.contains(SUFFIX_FRONT))
                    suffixFrontPorts.add(freePort);
                else if (freePortName.contains(SUFFIX_BACK)) {
                    try {
                        if (!bem.hasSpecialRelationship(freePort.getClassName(), freePort.getId(), "mirrorMultiple", 1))
                            suffixBackPorts.add(freePort);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR,
                            ts
                        ).open();
                    }
                }
                else if (freePortName.contains(PREFIX_IN))
                    prefixInPorts.add(freePort);
                else if (freePortName.contains(PREFIX_OUT)) {
                    try {
                        if (!bem.hasSpecialRelationship(freePort.getClassName(), freePort.getId(), "mirrorMultiple", 1))
                            prefixOutPorts.add(freePort);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR,
                            ts
                        ).open();
                    }
                }
                else if (freePortName.contains(PREFIX_FRONT))
                    prefixFrontPorts.add(freePort);
                else if (freePortName.contains(PREFIX_BACK)) {
                    try {
                        if (!bem.hasSpecialRelationship(freePort.getClassName(), freePort.getId(), "mirrorMultiple", 1))
                            prefixBackPorts.add(freePort);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR,
                            ts
                        ).open();
                    }
                }
            });
            if ((suffixOutPorts.isEmpty() && suffixBackPorts.isEmpty()) && 
                (prefixOutPorts.isEmpty() && prefixBackPorts.isEmpty())) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.physcon.mirror-man.notification.info.no-free-ports-to-mirror-multiple"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
                return;
            }
            Collections.sort(suffixInPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(suffixOutPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(suffixFrontPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(suffixBackPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(prefixInPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(prefixOutPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(prefixFrontPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(prefixBackPorts, Comparator.comparing(BusinessObjectLight::getName));
            
            LinkedHashMap<BusinessObjectLight, List<BusinessObjectLight>> inOutMirrors = new LinkedHashMap();
            LinkedHashMap<BusinessObjectLight, List<BusinessObjectLight>> frontBackMirrors = new LinkedHashMap();
            suffixInPorts.forEach(inPort -> {
                if (!suffixOutPorts.isEmpty()) {
                    inOutMirrors.put(inPort, new ArrayList());
                    suffixOutPorts.forEach(outPort -> inOutMirrors.get(inPort).add(outPort));
                }
            });
            prefixInPorts.forEach(inPort -> {
                if (!prefixOutPorts.isEmpty()) {
                    inOutMirrors.put(inPort, new ArrayList());
                    prefixOutPorts.forEach(outPort -> inOutMirrors.get(inPort).add(outPort));
                }
            });
            suffixFrontPorts.forEach(frontPort -> {
                if (!suffixBackPorts.isEmpty()) {
                    frontBackMirrors.put(frontPort, new ArrayList());
                    suffixBackPorts.forEach(backPort -> frontBackMirrors.get(frontPort).add(backPort));
                }
            });
            prefixFrontPorts.forEach(frontPort -> {
                if (!prefixBackPorts.isEmpty()) {
                    frontBackMirrors.put(frontPort, new ArrayList());
                    prefixBackPorts.forEach(backPort -> frontBackMirrors.get(frontPort).add(backPort));
                }
            });
            List<MultipleMirror> multipleMirrors = new ArrayList();
            inOutMirrors.forEach((key, value) -> 
                multipleMirrors.add(new MultipleMirror(key, value))
            );
            frontBackMirrors.forEach((key, value) -> 
                multipleMirrors.add(new MultipleMirror(key, value))
            );
            Label lblMultipleMirrorsGenerated = new Label(ts.getTranslatedString("module.physcon.mirror-man.lbl.text.multiple-mirrors-generated"));
            
            String itemSelectAll = ts.getTranslatedString("module.physcon.mirror-man.lst.item.select-all");
            MultiSelectListBox<String> lstSelectAll = new MultiSelectListBox();
            lstSelectAll.setWidthFull();
            lstSelectAll.setItems(itemSelectAll);
            
            MultiSelectListBox<MultipleMirror> lstMultipleMirrors = new MultiSelectListBox();
            lstMultipleMirrors.setSizeFull();
            lstMultipleMirrors.setRenderer(new ComponentRenderer<>(mirror -> new MultipleMirrorComponent(mirror, bem, ts)));
            lstMultipleMirrors.setItems(multipleMirrors);
            
            Scroller scrollerMultipleMirrors = new Scroller(lstMultipleMirrors);
            scrollerMultipleMirrors.setSizeFull();
            
            VerticalLayout lytContent = new VerticalLayout(lblMultipleMirrorsGenerated, lstSelectAll, scrollerMultipleMirrors);
            lytContent.setSizeFull();
            lytContent.setSpacing(false);
            lytContent.setMargin(false);
            lytContent.setPadding(false);
            
            lstSelectAll.addValueChangeListener(valueChangeEvent -> {
                if (!valueChangeEvent.getValue().isEmpty())
                    lstMultipleMirrors.select(multipleMirrors);
                else
                    lstMultipleMirrors.deselect(multipleMirrors);
            });
            ActionButton btnCancel = new ActionButton(ts.getTranslatedString("module.general.messages.cancel"));
            btnCancel.addClickListener(clickEvent -> close());
            ActionButton btnCreateMultipleMirrors = new ActionButton(ts.getTranslatedString("module.physcon.mirror-man.button.text.create-multiple-mirrors"));
            btnCreateMultipleMirrors.addClickListener(clickEvent -> {
                lstMultipleMirrors.getSelectedItems().forEach(mirrorMultiple -> {
                    mirrorMultiple.getTargets().forEach(target -> {
                        try {
                            bem.createSpecialRelationship(
                                mirrorMultiple.getSource().getClassName(), mirrorMultiple.getSource().getId(),
                                target.getClassName(), target.getId(),
                                "mirrorMultiple", true //NOI18N
                            );
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR,
                                ts
                            ).open();
                        }
                    });
                });
                cmdUpdateMirrors.execute();
                close();
            });
            HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnCreateMultipleMirrors);
            lytFooter.setSizeFull();
            lytFooter.setFlexGrow(1, btnCancel, btnCreateMultipleMirrors);
            
            setHeader(ts.getTranslatedString("module.physcon.mirror-man.window.header.multiple-mirror-free-ports"));
            setContent(lytContent);
            setFooter(lytFooter);
            super.open();
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(),
                AbstractNotification.NotificationType.ERROR,
                ts
            ).open();
        }
    }
    
    private class MultipleMirror {
        private final BusinessObjectLight source;
        private final List<BusinessObjectLight> targets;
        
        public MultipleMirror(BusinessObjectLight source, List<BusinessObjectLight> targets) {
            this.source = source;
            this.targets = targets;
        }
        
        public BusinessObjectLight getSource() {
            return source;
        }
        
        public List<BusinessObjectLight> getTargets() {
            return targets;
        }
    }
    
    private class MultipleMirrorComponent extends HorizontalLayout {
        private final MultipleMirror multipleMirror;
        private final BusinessEntityManager bem;
        private final TranslationService ts;
        
        public MultipleMirrorComponent(MultipleMirror multipleMirror, BusinessEntityManager bem, TranslationService ts) {
            this.multipleMirror = multipleMirror;
            this.bem = bem;
            this.ts = ts;
        }

        @Override
        protected void onAttach(AttachEvent attachEvent) {
            super.onAttach(attachEvent);
            
            FormattedObjectDisplayNameSpan sourceComponent = new FormattedObjectDisplayNameSpan(multipleMirror.getSource(), false, false, true, false);
            
            VerticalLayout lytSource = new VerticalLayout(sourceComponent);
            lytSource.setWidthFull();
            lytSource.setHorizontalComponentAlignment(Alignment.CENTER, sourceComponent);
            
            add(lytSource);
            setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, lytSource);
            
            VerticalLayout lytTargets = new VerticalLayout();
            
            multipleMirror.getTargets().forEach(target -> {
                FormattedObjectDisplayNameSpan targetComponent = new FormattedObjectDisplayNameSpan(target, false, false, true, false);
                lytTargets.add(targetComponent);
            });
            add(lytTargets);
        }
    }
}
