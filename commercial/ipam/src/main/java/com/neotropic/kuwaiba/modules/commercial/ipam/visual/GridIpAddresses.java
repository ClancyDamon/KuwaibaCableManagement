/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.ipAddr
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ipam.visual;

import com.neotropic.kuwaiba.modules.commercial.ipam.IpamModule;
import com.neotropic.kuwaiba.modules.commercial.ipam.IpamService;
import com.neotropic.kuwaiba.modules.commercial.ipam.IpamUI;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.RelateIpToNetworkInterfaceVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.ReleaseIpAddrFromNetworkInterfaceAction;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Creates a grid of ip addresses of a given parent, a subnet or a folder
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class GridIpAddresses extends Grid<BusinessObjectLight>{
    /**
     * Reference to the ipam service module
     */
    private final IpamService ipamService;
    /**
     * Reference to the business entity manager service
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the i18n service
     */
    private final TranslationService ts;
    /**
     * Reference to release IP address from network interface
     */    
    private final ReleaseIpAddrFromNetworkInterfaceAction actReleaseIpAddrFromNetworkInterface;
    
    private BusinessObjectLight currentPort;

    List<BusinessObjectLight> parentsLocation;
    
    public GridIpAddresses(String parentId, String parentClassName
            , String childClassName, IpamService ipamService
            , BusinessEntityManager bem, MetadataEntityManager mem
            , ReleaseIpAddrFromNetworkInterfaceAction actReleaseIpAddrFromNetworkInterface
            , TranslationService ts) 
    {
        this.ipamService = ipamService;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.actReleaseIpAddrFromNetworkInterface = actReleaseIpAddrFromNetworkInterface;
        
        currentPort = null;
        parentsLocation = new ArrayList<>();
        
        this.setPageSize(20);
        this.addThemeVariants(GridVariant.LUMO_COMPACT);
        this.setWidthFull();
        this.addComponentColumn(item -> new FormattedObjectDisplayNameSpan(item, false, true, false, false))
            .setHeader(ts.getTranslatedString("module.ipam.lbl.ip-address"))
            .setFlexGrow(0).setWidth("120px");
       
        this.addColumn(item -> createServicesColumn(item))
           .setAutoWidth(true).setHeader(ts.getTranslatedString("module.ipam.subnet.ip-addr.related-service"));
         
        this.addComponentColumn(item -> createInterfaceColumn(item))
           .setAutoWidth(true).setHeader(ts.getTranslatedString("module.ipam.subnet.ip-addr.related-interface"));

        this.addComponentColumn(i -> createLocationColumn()).setAutoWidth(true)
                .setHeader(ts.getTranslatedString("module.ipam.subnet.ip-addr.related-device-location"));
        
        if(childClassName != null && childClassName.equals(Constants.CLASS_IP_ADDRESS))
            this.setDataProvider(createDataproviderFolder(parentId));
        else
            this.setDataProvider(createDataproviderSubnet(parentId, parentClassName));
    }

    private HorizontalLayout createInterfaceColumn(BusinessObjectLight ipAddr){
        
        HorizontalLayout lytInterface = new HorizontalLayout();
        lytInterface.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytInterface.setPadding(false);
        lytInterface.setMargin(false);

        try {
            HashMap<String, List<BusinessObjectLight>> rels = bem.getSpecialAttributes(
                    ipAddr.getClassName(), ipAddr.getId(), IpamModule.RELATIONSHIP_IPAMHASADDRESS);
            //The port related with the  IP address
            for (Map.Entry<String, List<BusinessObjectLight>> entry : rels.entrySet()) {
                List<BusinessObjectLight> ports = entry.getValue();
                //it should be only one port related
                if(currentPort == null && mem.isSubclassOf(Constants.CLASS_GENERICPORT, ports.get(0).getClassName())){
                    currentPort = ports.get(0);

                    ActionButton btnReleaseIpAddrFromNetworkInterface =  new ActionButton(new Icon(VaadinIcon.UNLINK)
                        , actReleaseIpAddrFromNetworkInterface.getDescription());
                    Label lblNetworkInterface = new Label(ports.get(0).getName());
                    lytInterface.add(btnReleaseIpAddrFromNetworkInterface, lblNetworkInterface);
                    //Release Ip address from network interface
                    btnReleaseIpAddrFromNetworkInterface.addClickListener(c -> {
                        try{
                            actReleaseIpAddrFromNetworkInterface.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>(Constants.PROPERTY_ID, ipAddr.getId()),                                    
                                new ModuleActionParameter<>(RelateIpToNetworkInterfaceVisualAction.PARAM_NETWORK_INTERFACE_ID, ports.get(0).getId()),
                                new ModuleActionParameter<>(RelateIpToNetworkInterfaceVisualAction.PARAM_NETWORK_INTERFACE_CLASSNAME, ports.get(0).getClassName())));

                            btnReleaseIpAddrFromNetworkInterface.setEnabled(false);
                            lblNetworkInterface.setText("");
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                    ts.getTranslatedString("module.ipam.actions.release-ipaddr-from-network-interface.sucess"), 
                                    AbstractNotification.NotificationType.INFO, ts).open();
                        } catch (ModuleActionException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                                ts.getTranslatedString("module.ipam.actions.release-ipaddr-from-network-interface.failed"), 
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    });
                    return lytInterface;
                }
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(IpamUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lytInterface;
    }
    
    
    private String createServicesColumn(BusinessObjectLight ipAddr){
        String servicesNames = "";
        try {
            List<BusinessObjectLight> relatedServices = bem.getSpecialAttribute(ipAddr.getClassName(), ipAddr.getId(),"uses");
            if(relatedServices != null){
                for (BusinessObjectLight service : relatedServices)
                    servicesNames += service.getName();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(IpamUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return servicesNames;
    }
   
    /**
     * Creates the location Column
     * @return a div with the parents bread crumbs
     */
    private Div createLocationColumn(){
        Div divPowerline = new Div();
        divPowerline.setWidthFull();
        divPowerline.setClassName("parents-breadcrumbs");
        divPowerline.getStyle().set("justify-content", "start");
        try {
            if(currentPort != null){
                List<BusinessObjectLight> parents = bem.getParents(currentPort.getClassName(), currentPort.getId());
                for (BusinessObjectLight parent : parents){
                    if(!parent.getClassName().equals(Constants.DUMMY_ROOT))
                    {
                        boolean isDevice = mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, parent.getClassName());
                        if(isDevice || parent.getClassName().equals(Constants.CLASS_CITY)){
                            Span span = new Span(new Label(parent.getName()));
                            span.setSizeUndefined();
                            //Tool tip
                            span.setTitle(String.format("[%s]", (parent.getClassDisplayName() == null || parent.getClassDisplayName().isEmpty()) ?
                                parent.getClassName() : parent.getClassDisplayName()));
                            span.addClassNames("parent-backward", isDevice ? "ipam-parents-color" : "location-parent-color" );
                            divPowerline.add(span);
                        }
                    }
                }
                currentPort = null;
                parentsLocation = new ArrayList<>();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(GridIpAddresses.class.getName()).log(Level.SEVERE, null, ex);
        }
        return divPowerline;
    }
    
    private DataProvider<BusinessObjectLight, Void> createDataproviderSubnet(String parentId, String parentClassName){
        DataProvider<BusinessObjectLight, Void> dataProvider = DataProvider.fromCallbacks(query -> {   
                try {
                    List<BusinessObjectLight> ipAddrs = ipamService.getSubnetIpAddrCreated(parentId, parentClassName, query.getOffset(), query.getLimit());
                    return ipAddrs.stream();
                } catch (BusinessObjectNotFoundException | NotAuthorizedException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                    return Collections.<BusinessObjectLight>emptyList().stream();
                }
            }, query -> {
            try {
                int count = (int) ipamService.getSubnetIpAddrCreated(parentId, parentClassName, -1, -1).size();
                this.setHeightByRows(true);
                return count;
            }catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | NotAuthorizedException  ex){
                return 0;
            }
        });
        return dataProvider;
    }
    
    private DataProvider<BusinessObjectLight, Void> createDataproviderFolder(String parentId){
        DataProvider<BusinessObjectLight, Void> dataProvider = DataProvider.fromCallbacks(query -> {   
                try {
                    List<BusinessObjectLight> ipAddrs = ipamService.getFolderItems(parentId, Constants.CLASS_IP_ADDRESS, query.getOffset(), query.getLimit());
                    return ipAddrs.stream();
                } catch (ApplicationObjectNotFoundException | NotAuthorizedException | InvalidArgumentException ex) {
                    return Collections.<BusinessObjectLight>emptyList().stream();
                }
            }, query -> {
            try {
                int count = (int) ipamService.getFolderItemsCount(parentId, Constants.CLASS_IP_ADDRESS);
                this.setHeightByRows(true);
                return count;
            }catch (InvalidArgumentException | NotAuthorizedException | ApplicationObjectNotFoundException  ex){
                return 0;
            }
        });
        return dataProvider;
    }
}