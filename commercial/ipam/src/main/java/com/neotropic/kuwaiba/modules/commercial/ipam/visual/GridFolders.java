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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Collections;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * A custom Grid to show the folders in ipam module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class GridFolders extends Grid<InventoryObjectPool>{
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
    
    public GridFolders(String parentId, String parentClassName, IpamService ipamService, BusinessEntityManager bem, TranslationService ts) {
        this.ipamService = ipamService;
        this.bem = bem;
        this.ts = ts;

        this.setWidthFull();
        this.addComponentColumn(item -> createTitle(item))
                .setWidth("100px")
                .setHeader(ts.getTranslatedString("module.general.labels.name"));
        this.addColumn(InventoryObjectPool::getDescription)
                .setWidth("150px")
                .setHeader(ts.getTranslatedString("module.general.labels.description"))
                .setTextAlign(ColumnTextAlign.START);

        this.setDataProvider(createDataprovider(parentId, parentClassName));
    }
    
    private DataProvider<InventoryObjectPool, Void> createDataprovider(String parentId, String parentClassName){
            DataProvider<InventoryObjectPool, Void> provider =  DataProvider.fromCallbacks(
            query ->  {   
                try {
                    //add code for filtering
                    List<InventoryObjectPool> folders = ipamService.getFoldersInFolder(parentId, parentClassName, query.getOffset(), query.getLimit());
                    return folders.stream();
                } catch (NotAuthorizedException | ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                    return Collections.<InventoryObjectPool>emptyList().stream();
                }
            }, query -> {
            try {
                int count = (int) ipamService.getFolderInFolderCount(parentId, parentClassName);
                if(count <= 11)
                    this.setHeightByRows(true);
                return count;
            }catch (InvalidArgumentException | NotAuthorizedException | ApplicationObjectNotFoundException ex){
                return 0;
            }
        });
        
        return provider;
    }

    private Component createTitle(InventoryObjectPool item) {
        Icon icnLvlSubnet = new Icon(VaadinIcon.FOLDER_O);
        icnLvlSubnet.setSize("16px");
        
        HorizontalLayout lytTitle = new HorizontalLayout(icnLvlSubnet, new Label(item.getName()));
        lytTitle.setPadding(false);
        lytTitle.setMargin(false);
        return lytTitle;
    }
}
