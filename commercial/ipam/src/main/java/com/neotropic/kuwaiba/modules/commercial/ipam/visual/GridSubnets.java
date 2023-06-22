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

import com.neotropic.kuwaiba.modules.commercial.ipam.IpamService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Creates a subnet grid details
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class GridSubnets extends Grid<BusinessObject>{

    /**
     * if the grid creation is requesting by a subnet parent
     */
    public final static int TYPE_SUBNET = 0;
    /**
     * if the grid creation is requesting by a folder parent
     */
    public final static int TYPE_FOLDER = 1;
    /**
     * Reference to the ipam service module
     */
    private final IpamService ipamService;
    /**
     * Reference to the business entity manager service
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the i18n service
     */
    private final TranslationService ts;

    public GridSubnets(String parentId, String className, int type
            , IpamService ipamService, BusinessEntityManager bem
            , TranslationService ts) 
    {
        this.ipamService = ipamService;
        this.bem = bem;
        this.ts = ts;
        this.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.MATERIAL_COLUMN_DIVIDERS);
        this.setWidthFull();
        this.addComponentColumn(subnet -> createTitle(subnet))
            .setFlexGrow(0).setWidth("135px")
            .setHeader(ts.getTranslatedString("module.ipam.lbl.cidr"));
        
        this.addColumn(subnet -> String.format("%s - %s"
                , subnet.getAttributes().get(Constants.PROPERTY_NETWORK_IP)
                , subnet.getAttributes().get(Constants.PROPERTY_BROADCAST_IP)))
                .setWidth("200px").setResizable(true)
                .setHeader(ts.getTranslatedString("module.ipam.subnet.range"));
        
        this.addColumn(item-> item.getAttributes().get(Constants.PROPERTY_HOSTS))
            .setFlexGrow(0).setWidth("50px")
            .setHeader(ts.getTranslatedString("module.ipam.subnet.hosts"));
        
        this.addColumn(item-> item.getAttributes().get(Constants.PROPERTY_MASK))
            .setFlexGrow(0).setWidth("130px")
            .setHeader(ts.getTranslatedString("module.ipam.subnet.net-mask"));
        
        this.addColumn(item-> item.getAttributes().get(Constants.PROPERTY_DESCRIPTION))
            .setAutoWidth(true)
            .setHeader(ts.getTranslatedString("module.general.labels.description"));
        
        if(type == TYPE_SUBNET) //subnet
            this.setDataProvider(createDataproviderSubnet(parentId, className));
        else if(type == TYPE_FOLDER)
            this.setDataProvider(createDataproviderFolder(parentId, className));
    }
   
    /**
     * Data provider when the children of a subnet is requested
     * @param parentId the subnet id
     * @param className the subnet class name
     * @return a data provider when a subnet is the parent
     */
    private  DataProvider<BusinessObject, Void> createDataproviderSubnet(String parentId, String className){
        DataProvider<BusinessObject, Void> dataProvider = DataProvider.fromCallbacks(query -> {   
            try {
                    List<BusinessObjectLight> subnetsLight = ipamService.getSubnetsInSubnet(parentId, className, query.getOffset(), query.getLimit());
                    List<BusinessObject> subnets = new ArrayList<>();
                    for (BusinessObjectLight subnetL : subnetsLight)
                        subnets.add(bem.getObject(subnetL.getClassName(), subnetL.getId()));
                    return subnets.stream();
                } catch (BusinessObjectNotFoundException |NotAuthorizedException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                    return Collections.<BusinessObject>emptyList().stream();
                }
            }, query -> {
            try {
                int count = (int)ipamService.getSubnetsInSubnetCount(parentId, className);
                if(count <= 10)
                    this.setHeightByRows(true);
                return count;
            }catch (InvalidArgumentException | NotAuthorizedException | MetadataObjectNotFoundException | BusinessObjectNotFoundException  ex){
                return 0;
            }
        });
        return dataProvider;
    }
    
    /**
     * Create the subnet children data provider when the selected parent is a folder
     * @param parentId the folder id
     * @param className the folder class name
     * @return a data provider to 
     */
    private  DataProvider<BusinessObject, Void> createDataproviderFolder(String parentId, String className){
        DataProvider<BusinessObject, Void> dataProvider = DataProvider.fromCallbacks(query -> {   
                try {
                    List<BusinessObjectLight> subnetsLight = ipamService.getFolderItems(parentId, className, query.getOffset(), query.getLimit());
                    List<BusinessObject> subnets = new ArrayList<>();
                    for (BusinessObjectLight subnetL : subnetsLight)
                        subnets.add(bem.getObject(subnetL.getClassName(), subnetL.getId()));
                    return subnets.stream();
                } catch (BusinessObjectNotFoundException |NotAuthorizedException | InvalidArgumentException | MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                    return Collections.<BusinessObject>emptyList().stream();
                }
            }, query -> {
            try {
                int count = (int)ipamService.getFolderItemsCount(parentId, className);
                if(count <= 10)
                    this.setHeightByRows(true);
                return count;
            }catch (InvalidArgumentException | NotAuthorizedException | ApplicationObjectNotFoundException   ex){
                return 0;
            }
        });
        return dataProvider;
    }
 
    /**
     * Creates the title of the subnet with an icon
     * @param item the item subnet
     * @return a layout with a label and a icon
     */
    private Component createTitle(BusinessObjectLight item) {
        Icon icnLvlSubnet = new Icon(VaadinIcon.SITEMAP);
        icnLvlSubnet.setSize("16px");
        
        HorizontalLayout lytTitle = new HorizontalLayout(icnLvlSubnet, new Label(item.getName()));
        lytTitle.setPadding(false);
        lytTitle.setMargin(false);
        return lytTitle;
    }
}
