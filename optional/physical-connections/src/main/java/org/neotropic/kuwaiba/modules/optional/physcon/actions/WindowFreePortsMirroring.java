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
package org.neotropic.kuwaiba.modules.optional.physcon.actions;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
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
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors.AbstractWindowMirrorFreePorts;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to mirror free ports.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowFreePortsMirroring extends AbstractWindowMirrorFreePorts {
    private final BusinessObjectLight businessObject;
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    private final Command cmdUpdateMirrors;
    
    public WindowFreePortsMirroring(BusinessObjectLight businessObject, BusinessEntityManager bem, TranslationService ts, Command cmdUpdateMirrors) {
        Objects.requireNonNull(businessObject);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(cmdUpdateMirrors);
        
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
                if (!bem.hasSpecialRelationship(port.getClassName(), port.getId(), "mirror", 1) &&
                    !bem.hasSpecialRelationship(port.getClassName(), port.getId(), "mirrorMultiple", 1))
                    freePorts.add(port);
            }
            if (freePorts.isEmpty()) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.physcon.mirror-man.notification.info.no-free-ports-to-mirror"), 
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
                else if (freePortName.contains(SUFFIX_OUT))
                    suffixOutPorts.add(freePort);
                else if (freePortName.contains(SUFFIX_FRONT))
                    suffixFrontPorts.add(freePort);
                else if (freePortName.contains(SUFFIX_BACK))
                    suffixBackPorts.add(freePort);
                else if (freePortName.contains(PREFIX_IN))
                    prefixInPorts.add(freePort);
                else if (freePortName.contains(PREFIX_OUT))
                    prefixOutPorts.add(freePort);
                else if (freePortName.contains(PREFIX_FRONT))
                    prefixFrontPorts.add(freePort);
                else if (freePortName.contains(PREFIX_BACK))
                    prefixBackPorts.add(freePort);
            });
            Collections.sort(suffixInPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(suffixOutPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(suffixFrontPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(suffixBackPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(prefixInPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(prefixOutPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(prefixFrontPorts, Comparator.comparing(BusinessObjectLight::getName));
            Collections.sort(prefixBackPorts, Comparator.comparing(BusinessObjectLight::getName));
            
            LinkedHashMap<BusinessObjectLight, BusinessObjectLight> inOutMirrors = new LinkedHashMap();
            LinkedHashMap<BusinessObjectLight, BusinessObjectLight> frontBackMirrors = new LinkedHashMap();
            
            suffixInPorts.forEach(inPort -> {
                String[] inPortName = inPort.getName().toLowerCase().split(SUFFIX_IN);
                if (inPortName.length > 0) {
                    String inPortName0 = inPortName[0];
                    suffixOutPorts.forEach(outPort -> {
                        String[] outPortName = outPort.getName().toLowerCase().split(SUFFIX_OUT);
                        if (outPortName.length > 0) {
                            String outPortName0 = outPortName[0];

                            if (inPortName0.equals(outPortName0))
                                inOutMirrors.put(inPort, outPort);
                        }
                    });
                }
            });
            prefixInPorts.forEach(inPort -> {
                String[] inPortName = inPort.getName().toLowerCase().split(PREFIX_IN);
                if (inPortName.length > 0) {
                    String inPortName1 = inPortName[1];
                    prefixOutPorts.forEach(outPort -> {
                        String[] outPortName = outPort.getName().toLowerCase().split(PREFIX_OUT);
                        if (outPortName.length > 0) {
                            String outPortName1 = outPortName[1];
                            
                            if (inPortName1.equals(outPortName1))
                                inOutMirrors.put(inPort, outPort);
                        }
                    });
                }
            });
            suffixFrontPorts.forEach(frontPort -> {
                String[] frontPortName = frontPort.getName().toLowerCase().split(SUFFIX_FRONT);
                if (frontPortName.length > 0) {
                    String frontPortName0 = frontPortName[0];
                    suffixBackPorts.forEach(backPort -> {
                        String[] backPortName = backPort.getName().toLowerCase().split(SUFFIX_BACK);
                        if (backPortName.length > 0) {
                            String backPortName0 = backPortName[0];
                            if (frontPortName0.equals(backPortName0))
                                frontBackMirrors.put(frontPort, backPort);
                        }
                    });
                }
            });
            prefixFrontPorts.forEach(frontPort -> {
                String[] frontPortName = frontPort.getName().toLowerCase().split(PREFIX_FRONT);
                if (frontPortName.length > 0) {
                    String frontPortName1 = frontPortName[1];
                    prefixBackPorts.forEach(backPort -> {
                        String[] backPortName = backPort.getName().toLowerCase().split(PREFIX_BACK);
                        if (backPortName.length > 0) {
                            String backPortName1 = backPortName[1];
                            if (frontPortName1.equals(backPortName1))
                                frontBackMirrors.put(frontPort, backPort);
                        }
                    });
                }
            });
            Label lblMirrorsGenerated = new Label(ts.getTranslatedString("module.physcon.mirror-man.lbl.text.mirrors-generated"));
            
            String itemSelectAll = ts.getTranslatedString("module.physcon.mirror-man.lst.item.select-all");
            MultiSelectListBox<String> lstSelectAll = new MultiSelectListBox();
            lstSelectAll.setWidthFull();
            lstSelectAll.setItems(itemSelectAll);
            
            MultiSelectListBox<Mirror> lstMirrors = new MultiSelectListBox();
            lstMirrors.setSizeFull();
            lstMirrors.setRenderer(new ComponentRenderer<>(mirror -> {
                HorizontalLayout lyt = new HorizontalLayout(
                    new FormattedObjectDisplayNameSpan(mirror.getSource(), false, false, true, false),
                    new FormattedObjectDisplayNameSpan(mirror.getTarget(), false, false, true, false)
                );
                return lyt;
            }));
            Scroller scrollerMirrorsGenerated = new Scroller();
            scrollerMirrorsGenerated.setSizeFull();
            scrollerMirrorsGenerated.setContent(lstMirrors);
            
            VerticalLayout lytContent = new VerticalLayout(lblMirrorsGenerated, lstSelectAll, scrollerMirrorsGenerated);
            lytContent.setSizeFull();
            lytContent.setSpacing(false);
            
            List<Mirror> mirrors = new ArrayList();
            
            inOutMirrors.forEach((key, value) -> 
                mirrors.add(new Mirror(key, value))
            );
            frontBackMirrors.forEach((key, value) -> 
                mirrors.add(new Mirror(key, value))
            );
            lstMirrors.setItems(mirrors);
            lstSelectAll.addValueChangeListener(valueChangeEvent -> {
                if (!valueChangeEvent.getValue().isEmpty())
                    lstMirrors.select(mirrors);
                else
                    lstMirrors.deselect(mirrors);
            });
            lstSelectAll.select(itemSelectAll);
            ActionButton btnCancel = new ActionButton(ts.getTranslatedString("module.general.messages.cancel"));
            btnCancel.addClickListener(clickEvent -> close());
            
            ActionButton btnCreateMirrors = new ActionButton(ts.getTranslatedString("module.physcon.mirror-man.button.text.create-mirrors"));
            btnCreateMirrors.addClickListener(clickEvent -> {
                lstMirrors.getSelectedItems().forEach(mirror -> {
                    try {
                        bem.createSpecialRelationship(
                                mirror.getSource().getClassName(), mirror.getSource().getId(),
                                mirror.getTarget().getClassName(), mirror.getTarget().getId(),
                                "mirror", true //NOI18N
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
                cmdUpdateMirrors.execute();
                close();
            });
            
            HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnCreateMirrors);
            lytFooter.setSizeFull();
            lytFooter.setFlexGrow(1, btnCancel, btnCreateMirrors);
            
            setHeader(ts.getTranslatedString("module.physcon.mirror-man.window.header.mirror-free-ports"));
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
    
    private class Mirror {
        private final BusinessObjectLight source;
        private final BusinessObjectLight target;
        
        public Mirror(BusinessObjectLight source, BusinessObjectLight target) {
            this.source = source;
            this.target = target;
        }
        
        public BusinessObjectLight getSource() {
            return source;
        }
        
        public BusinessObjectLight getTarget() {
            return target;
        }
    }
}
