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

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectDashboard;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsModule;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.views.RackView;
import org.neotropic.kuwaiba.modules.optional.physcon.widgets.PhysicalPathViewWidget;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Widget that shows the port summary for an inventory object.
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class PortSummaryVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    @Autowired
    private PortSummaryAction portSummaryAction;
     /**
     * Reference to the Physical Connection Service.
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionsService;
     /**
     * Reference to the Physical PAth widget.
     */
    @Autowired
    private PhysicalPathViewWidget physicalPathViewWidget;


    public PortSummaryVisualAction() {
        super(PhysicalConnectionsModule.MODULE_ID);
    }
    
    
    @Override
    public String appliesTo() {
        return Constants.CLASS_GENERICCOMMUNICATIONSELEMENT;
    }
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            if (!parameters.containsKey("businessObject"))
                return null;
            BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get("businessObject");
                       
            List<BusinessObjectLight> ports = bem.getChildrenOfClassLightRecursive(businessObject.getId(), businessObject.getClassName(), Constants.CLASS_GENERICPORT, null, -1, -1);

            Grid<BusinessObjectLight> tblPorts = new Grid();
            tblPorts.setWidthFull();
            tblPorts.setMaxHeight("450px");
            tblPorts.setItems(ports);
            tblPorts.addComponentColumn(item -> {
                try {
                    String txtName = item.getName() == null || item.getName().isEmpty() ? "<Name Not Set>" : item.getName();
                    VerticalLayout lytName = new VerticalLayout(new BoldLabel(txtName));
                    List<BusinessObjectLight> parents = bem.getParents(item.getClassName(), item.getId());
                    String path = "";
                    for (BusinessObjectLight parent : parents) {
                        if (parent.equals(businessObject)) 
                            break;

                        path += parent.getName() + "/";
                    }
                    Label lblPath = new Label(path);
                    lblPath.addClassName("text-secondary-b");
                    lytName.setSpacing(false);
                    lytName.setMargin(false);
                    lytName.setMargin(false);
                    lytName.setPadding(false);
                    lytName.add(lblPath);
                    return lytName;
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                    return new Label("");
                }
            }).setHeader(ts.getTranslatedString("module.visualization.rack-view-port-name"));

            tblPorts.addComponentColumn(item -> {
                try {
                    List<BusinessObjectLight> physicalPath = physicalConnectionsService.getPhysicalPath(item.getClassName(), item.getId());
                    if (physicalPath.size() > 0) {
                        BusinessObjectLight endPoint = physicalPath.get(physicalPath.size()-1);
                        BusinessObjectLight parent = bem.getFirstParentOfClass(endPoint.getClassName(), endPoint.getId(),
                                Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                        if (parent == null) {
                            parent = bem.getFirstParentOfClass(endPoint.getClassName(), endPoint.getId(),
                                Constants.CLASS_GENERICDISTRIBUTIONFRAME);
                            if (parent == null) {
                                parent = bem.getFirstParentOfClass(endPoint.getClassName(), endPoint.getId(),
                                Constants.CLASS_GENERICSPLICINGDEVICE);
                             if (parent == null)
                                parent = new BusinessObjectLight("", "", "");
                            }
                        }
                        Label lblEndPoint = new Label(parent.getName() + " : " + endPoint.getName());
                        Button btnPhysicalPath = new Button(new Icon(VaadinIcon.FILE_TREE_SUB), evtPhysicalPath -> {
                            try {
                                EnhancedDialog dlgPhysicalPath = new EnhancedDialog();
                                dlgPhysicalPath.setWidth("95%");

                                Button btnClosePhysicalPath = new Button(ts.getTranslatedString("module.general.messages.close"), evtDlgPhysicalPath -> 
                                        dlgPhysicalPath.close());
                                BoldLabel lblPhysicalPath = new BoldLabel(ts.getTranslatedString("module.visualization.physical-path-view-name") + " : " + item.toString());                                                       

                                dlgPhysicalPath.setHeader(lblPhysicalPath);
                                dlgPhysicalPath.setContent(physicalPathViewWidget.build(item));
                                dlgPhysicalPath.setFooter(btnClosePhysicalPath);
                                dlgPhysicalPath.setThemeVariants(EnhancedDialogVariant.SIZE_LARGE);
                                dlgPhysicalPath.open();
                            } catch (InvalidArgumentException ex) {
                                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                 new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                            } catch (InventoryException ex) {
                                Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                            }
                        });
                        btnPhysicalPath.getElement().setProperty("title", ts.getTranslatedString("module.visualization.physical-path-view-show"));     
                        HorizontalLayout lytEndPoint = new HorizontalLayout(lblEndPoint, btnPhysicalPath);
                        lytEndPoint.setFlexGrow(1, lblEndPoint);
                        lytEndPoint.setAlignItems(FlexComponent.Alignment.BASELINE);
                        return lytEndPoint;
                    } else 
                        return new Label(ts.getTranslatedString("module.visualization.rack-view-disconnected"));

                } catch (MetadataObjectNotFoundException | InvalidArgumentException 
                        | IllegalStateException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                    return new Label("");
                } 
            }).setHeader(ts.getTranslatedString("module.visualization.rack-view-connected-to"));
            if (!(this instanceof SplicingDevicePortSummaryVisualAction) 
                    && !(this instanceof DistributionFramePortSummaryVisualAction))
                tblPorts.addComponentColumn(item -> {
                    try {
                        List<BusinessObjectLight> rel = bem.getSpecialAttribute(item.getClassName(), item.getId(), "ipamHasIpAddress");
                        if (rel.isEmpty())
                            return new Label(ts.getTranslatedString("module.visualization.rack-view-na"));
                        VerticalLayout ips = new VerticalLayout();
                        ips.setPadding(false);
                        ips.setSpacing(false);
                        rel.forEach(ip -> ips.add(new Label(ip.getName())));
                        return ips;
                    } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                        Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                        return new Label(ts.getTranslatedString("module.visualization.rack-view-na"));
                    }
                }).setHeader(ts.getTranslatedString("module.visualization.rack-view-ip-related"));
            tblPorts.addComponentColumn(item -> {
                try {
                    HashMap<String, List<BusinessObjectLight>> uses = bem.getSpecialAttributes(item.getClassName(), item.getId(), "uses");
                    if (!uses.containsKey("uses")) 
                        return new Label(ts.getTranslatedString("module.visualization.rack-view-na"));

                    VerticalLayout lytUses = new VerticalLayout();
                    uses.get("uses").forEach(obj -> {
                        Label lblService = new Label(obj.getName());
                        Button btnGoToOOP = new Button(VaadinIcon.EXTERNAL_LINK.create(), evt -> {
                            UI.getCurrent().getUI().ifPresent(ui -> {
                            ui.getSession().setAttribute(BusinessObjectLight.class, obj);
                            ui.getPage().open(new RouterLink("", ObjectDashboard.class).getHref(), "_blank");
                            });
                        });
                        HorizontalLayout lytService = new HorizontalLayout(lblService, btnGoToOOP);
                        lytService.setPadding(false);
                        lytUses.add(lytService);
                            });
                       
                    lytUses.setMargin(false);
                    lytUses.setMargin(false);
                    lytUses.setPadding(false);
                    return lytUses;
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(RackView.class.getName()).log(Level.SEVERE, null, ex);
                    return new Label("");
                }
            }).setHeader(ts.getTranslatedString("module.visualization.rack-view-services"));
         
            ConfirmDialog dlgAction = new ConfirmDialog(ts, ts.getTranslatedString("module.visualization.rack-view-port-summary"), "");
            dlgAction.setWidth("90%");
            dlgAction.setHeight("550px");
            dlgAction.setContent(tblPorts);
            dlgAction.getBtnConfirm().setVisible(false);
            dlgAction.getBtnCancel().setText(ts.getTranslatedString("module.general.messages.close"));
            return dlgAction;
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(PortSummaryVisualAction.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            return null;
        }
    }
    
    @Override
    public int getRequiredSelectedObjects() {
         return 1;
    }

    @Override
    public AbstractAction getModuleAction() {
       return portSummaryAction;
    }

}
