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
package com.neotropic.kuwaiba.modules.commercial.ipam;

import com.neotropic.kuwaiba.modules.commercial.ipam.explorers.DialogIpamSearch;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.AddIpAddrToFolderVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.AddIpAddrToSubnetAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.visual.DivIpAddr;
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.IpamEngine;
import com.neotropic.kuwaiba.modules.commercial.ipam.engine.SubnetDetail;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.AddIpAddrToSubnetVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.DeleteFolderVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.DeleteSubnetVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.NewFolderVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.NewSubnetVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.DeleteIpAddrFromFolderVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.ReleaseIpAddrFromNetworkInterfaceAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.SplitSubnetVisualAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.actions.UpdateIpAddrAction;
import com.neotropic.kuwaiba.modules.commercial.ipam.visual.ChartSubnetUsage;
import com.neotropic.kuwaiba.modules.commercial.ipam.visual.GridIpAddresses;
import com.neotropic.kuwaiba.modules.commercial.ipam.visual.GridSubnets;
import com.neotropic.kuwaiba.modules.commercial.ipam.visual.IpamNode;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.NavTreeGrid;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main entry point for IP Address manager module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Route(value = "ipam", layout = IpamLayout.class)
public class IpamUI extends VerticalLayout implements ActionCompletedListener
        , HasDynamicTitle, AbstractUI, PropertySheet.IPropertyValueChangedListener
{
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to ip address manager service
     */
    @Autowired
    private IpamService ipamService;
    /**
     * Reference to the action that creates a new subnet
     */
    @Autowired
    private NewSubnetVisualAction actNewSubnet;
    /**
     * Reference to the action that deletes a subnet
     */
    @Autowired
    private DeleteSubnetVisualAction actDelSubnet;
    /**
     * Reference to the action that creates a new folder
     */
    @Autowired
    private NewFolderVisualAction actNewFolder;
    /**
     * Reference to the action that updates an ip address
     */
    @Autowired
    private UpdateIpAddrAction actUpdateIpAddr;
    /**
     * Reference to the action that creates an ip address
     */
    @Autowired
    private AddIpAddrToSubnetAction actAddIpAddr;
    /**
     * Reference to the action that deletes folder
     */
    @Autowired
    private DeleteFolderVisualAction actDelFolder;
    /**
     * Reference to the action that add an IP address to a subnet
     */
    @Autowired
    private AddIpAddrToSubnetVisualAction actAddIpAddrVisual;
    /**
     * Reference to the action that deletes an IP address from a subnet
     */
    @Autowired
    private DeleteIpAddrFromFolderVisualAction actDeleteIpAddrFromFolder;
    /**
     * Reference to action release an individual IP address form a networkInterface
     */
    @Autowired
    private ReleaseIpAddrFromNetworkInterfaceAction actReleaseIpAddrFromNetworkInterface;
     /**
     * Reference to the action that allows the creation of a single 
     * ip addr without a subnet in a folder
     */    
    @Autowired
    private AddIpAddrToFolderVisualAction actAddIpAddrToFolder;
    /**
     * Allows to split a subnet in several subnets graphically
     */
    @Autowired
    private SplitSubnetVisualAction actSplitSubnet;
    /**
     * The current selected ip address version 4 or 6
     */
    private int seletedIpv;
    //gui
    /**
     * The actual content.
     */
    private VerticalLayout lytContent;
    /**
     * The page header contains the subnet selector and a search box
     */
    private HorizontalLayout lytHeader;
    /**
     * The left side contains a tree with the subnets and folders of the 
     * selected kind o subnets
     */
    private VerticalLayout lytSearchResults;
    /**
     * Panel left
     */
    private VerticalLayout lytLeftSide;
    /**
     * The right side contains the details of a selected subnet
     */
    private VerticalLayout lytCenter;
    /**
     * Message under the nav tree to display no children
     */
    private Label lblNoChildren;
    /**
     * The right side contains the details of a selected subnet
     */
    private VerticalLayout lytRight;
    /**
     * The right top layout that contains the properties and chart
     */
    private HorizontalLayout lytDetails;
    /**
     * bottom table that contains  actual content. Initially it is just a search box then it can become
     * a page displaying the service/customer details.
     */
    private VerticalLayout lytProperties;
    /**
     * The current selected item (a folder or subnet)
     */
    private VerticalLayout lytSubnetUsageChart;
    /**
     * table for created/used/reserved ip addresses in a subnet or folder
     */
    private VerticalLayout lytTableGridIpAddrs;
    /**
     * table for created/nested subnets in a subnet or folder
     */
    private VerticalLayout lytTableGridSubnets;
    /**
     * bottom table that contains the a visual representation of the ip 
     * addresses in a subnet
     */
    private FlexLayout lytDivsAddrs;
    /**
     * search text field for subnets grid
     */    
    private DialogIpamSearch searchDialog;
    /**
     * explorer tree for the current selected object
     */
    private NavTreeGrid<IpamNode> navTree;
    /**
     * to keep record of the item who launch an action
     */
    private List<IpamNode> actionAffectedNode;
    /**
     * current selected node
     */
    private IpamNode currentSelectedNode;
    /**
     * Property sheet
     */
    private PropertySheet propertySheetPool;
    /**
     * Show the IP address for folder or subnets
     */
    private GridIpAddresses gridIpAddrs;
    /**
     * Shows the subnets in a folder or subnet
     */
    private GridSubnets gridSubnets;
    /**
     * Title for subent's grid
     */
    private Label lblSubnetsTitle;
    /**
     * title for ip address table
     */
    private Label lblIAddrsTittle;
    /**
     * Right header contains the title
     */
    private HorizontalLayout lytRightTitle;
    /**
     * Title on the right side, contains the name of the selected item
     */
    private Label lblRightTitle;
    /**
     * the grid in the left side,that shows the current search
     */
    private Grid<IpamNode> grdSearchResults;
    /**
     * Current search results
     */
    private List<IpamNode> lastSearchResults;
    /**
     * Current Tree data in the navigation tree
     */
    private List<IpamNode> currentTreeData;
    /**
     * We keep the last search
     */
    private String lastSearch;
    /**
     * Global variable resume of the classes to show in the IP address location
     */
    //private String locationResume;
    /**
     * Button to navigate IP address root IPv4
     */
    private Button btnIpv4;
    /**
     * Button to navigate to IP address root IPv6
     */
    private Button btnIpv6;
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.actNewSubnet.unregisterListener(this);
        this.actSplitSubnet.unregisterListener(this);
        this.actDelSubnet.unregisterListener(this);
        this.actNewFolder.unregisterListener(this);
        this.actDelFolder.unregisterListener(this);
        
        this.actAddIpAddrVisual.unregisterListener(this);
        this.actAddIpAddrToFolder.unregisterListener(this);
        this.actDeleteIpAddrFromFolder.unregisterListener(this);
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try{
                if(ev.getActionResponse() != null && (ev.getActionResponse().containsKey(ActionResponse.ActionType.ADD))){
                    if(!actionAffectedNode.isEmpty() && actionAffectedNode.get(0) != null){
                        if(((String)ev.getActionResponse().get(ActionResponse.ActionType.ADD)).isEmpty()){
                            navTree.update(actionAffectedNode.get(0));
                            lblNoChildren.setText("");
                        }
                        else{//is an ip address we must update is parent(if is a subnet) to disable some actions
                            if(navTree.getTreeData().contains(actionAffectedNode.get(0))){
                                navTree.getDataProvider().refreshItem(actionAffectedNode.get(0), false);
                                navTree.select(actionAffectedNode.get(0));
                            }
                            else //the action could be called from the search results grid
                                grdSearchResults.getDataProvider().refreshAll();
                        }
                    }
                }
                else if(ev.getActionResponse() != null && ev.getActionResponse().containsKey(ActionResponse.ActionType.REMOVE)){
                    if(!actionAffectedNode.isEmpty() && actionAffectedNode.get(0) != null && navTree.getTreeData().contains(actionAffectedNode.get(0)))
                        navTree.remove(actionAffectedNode.get(0));
                    
                    if(lastSearchResults.contains(actionAffectedNode.get(0))){
                        grdSearchResults.deselect(actionAffectedNode.get(0));
                        createSearchResultsDataProvider();
                    }
                    //we must deselect the current selected node if the affecetated node is the same
                    if(actionAffectedNode.get(0).getId().equals(currentSelectedNode.getId()))
                        currentSelectedNode = null;
                }
                if(gridIpAddrs != null)
                    gridIpAddrs.getDataProvider().refreshAll();
                
                actionAffectedNode.set(0, null);
                updatePropertySheet();
                
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            } catch (Exception ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.ipam.title");
    }

    @Override
    public void initContent() {
        this.setPadding(false);
        this.setMargin(false);
        this.setSpacing(false);
        this.actionAffectedNode = new ArrayList<>();
        this.actionAffectedNode.add(null);
        this.currentTreeData = new ArrayList<>();
        this.seletedIpv = 4;
        this.lastSearchResults = new ArrayList<>();
        this.lblNoChildren = new Label();
        this.lblIAddrsTittle = new Label();
        this.lblRightTitle = new Label();
        this.lblSubnetsTitle = new Label();
        //in case we are updating the page
        this.actNewSubnet.unregisterListener(this);
        this.actSplitSubnet.unregisterListener(this);
        this.actDelSubnet.unregisterListener(this);
        this.actNewFolder.unregisterListener(this);
        this.actDelFolder.unregisterListener(this);
        this.actAddIpAddrVisual.unregisterListener(this);
        this.actAddIpAddrToFolder.unregisterListener(this);
        this.actDeleteIpAddrFromFolder.unregisterListener(this);

        //Register actions
        this.actNewSubnet.registerActionCompletedLister(this);
        this.actSplitSubnet.registerActionCompletedLister(this);
        this.actDelSubnet.registerActionCompletedLister(this);
        this.actNewFolder.registerActionCompletedLister(this);
        this.actDelFolder.registerActionCompletedLister(this);
        this.actAddIpAddrVisual.registerActionCompletedLister(this);
        this.actAddIpAddrToFolder.registerActionCompletedLister(this);
        this.actDeleteIpAddrFromFolder.registerActionCompletedLister(this);
        
        this.propertySheetPool = new PropertySheet(ts, new ArrayList<>());
        this.propertySheetPool.addPropertyValueChangedListener(this);
        this.propertySheetPool.setHeightFull();
        
        setupLayouts();
        createStartPage();
    }
       
    /**
     * Creates the right panel with the selected item details and children
     * @param ipamNode a selected row
     */
    private void createDeatilsRightPanel(Object selectedItem){
        try {
            lytSubnetUsageChart.removeAll();
            lytDivsAddrs.removeAll();
            
            gridIpAddrs = null;
            gridSubnets = null;
            
            lytTableGridIpAddrs.removeAll();
            lytTableGridSubnets.removeAll();
            
            if(selectedItem instanceof InventoryObjectPool){
                createIpAddrInFolder(((InventoryObjectPool)selectedItem).getId());
                createSubnetsInFolder(((InventoryObjectPool)selectedItem).getId());
            }
            else if(selectedItem instanceof BusinessObjectLight){
                if(!((BusinessObjectLight)selectedItem).getClassName().equals(Constants.CLASS_IP_ADDRESS)){
                    BusinessObjectLight subnet = (BusinessObjectLight)selectedItem;          

                    ChartSubnetUsage chart = new ChartSubnetUsage(subnet.getId(), subnet.getClassName(), ipamService, bem, ts);
                    lytSubnetUsageChart.add(chart);
                    
                    createSubnetsInSubnet(subnet.getId(), subnet.getClassName());
                    
                    long subnets = ipamService.getSubnetsInSubnetCount(subnet.getId(), subnet.getClassName());
                    List<BusinessObjectLight> ipAddrs = ipamService.getSubnetIpAddrsInUse(subnet.getId(), subnet.getClassName());

                    if((subnets == 0 && ipAddrs.isEmpty()) || !ipAddrs.isEmpty()){
                        createIpAddrInSubnet(subnet.getId(), subnet.getClassName());

                        if(subnet.getClassName().equals(Constants.CLASS_SUBNET_IPV4)){
                            //the business object ligth subnet name its in cidr format
                            SubnetDetail subnetDetail = new SubnetDetail(subnet.getId(), subnet.getName()); 
                            IpamEngine.ipv4SubnetCalculation(subnetDetail);

                            //We only create graphical ip address represetantio for subnets /24
                            if(subnetDetail.getMaskBits() >= 24)
                                createGraphicalIpAddressesDetails(subnetDetail, subnet.getId(), subnet.getClassName());
                        }
                    }
                }
                else{
                    BusinessObjectLight ipAddr = (BusinessObjectLight)selectedItem;    
                    createSelectedIpAddrDetails(ipAddr.getId(), ipAddr.getClassName());
                }
            }
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Creates the subnet in a selected folder
     * @param folderId the selected folder id
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException
     * @throws InvalidArgumentException 
     */
    private void createSubnetsInFolder(String folderId) 
            throws ApplicationObjectNotFoundException, 
            NotAuthorizedException, InvalidArgumentException
    {
        long subnetsInFolderCount = ipamService.getFolderItemsCount(folderId, seletedIpv == 4 ? Constants.CLASS_SUBNET_IPV4 : Constants.CLASS_SUBNET_IPV6);
        if (subnetsInFolderCount > 0) {
            lblSubnetsTitle.setText(String.format("%s: %s"
                    , ts.getTranslatedString("module.ipam.subnet.has-nested-subnets"), subnetsInFolderCount));
            if(gridSubnets != null)
                gridSubnets.getDataProvider().refreshAll();
            else{
                gridSubnets = new GridSubnets(folderId, seletedIpv == 4 ? Constants.CLASS_SUBNET_IPV4 : Constants.CLASS_SUBNET_IPV6, GridSubnets.TYPE_FOLDER, ipamService, bem, ts);
                lytTableGridSubnets.add(lblSubnetsTitle, gridSubnets);
            }
        }
    }
    
    /**
     * Creates the subnet in a subnet
     * @param folderId the selected folder id
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException
     * @throws InvalidArgumentException 
     */
    private void createSubnetsInSubnet(String subnetId, String subnetClassName) 
            throws MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, NotAuthorizedException, 
            InvalidArgumentException
    {
        long nestedSubnets = ipamService.getSubnetsInSubnetCount(subnetId, subnetClassName);
        if(nestedSubnets > 0 ){ 
            
            lblSubnetsTitle.setText(String.format("%s: %s",
                ts.getTranslatedString("module.ipam.subnet.has-nested-subnets"), nestedSubnets));
            if(gridSubnets != null)
                gridSubnets.getDataProvider().refreshAll();
            else{
                lytTableGridSubnets.setVisible(true);
                gridSubnets = new GridSubnets(subnetId, subnetClassName, GridSubnets.TYPE_SUBNET, ipamService, bem, ts);
                lytTableGridSubnets.add(lblSubnetsTitle, gridSubnets);
            }
        }
    }
    
    /**
     * Creates the IP address table when a folder is selected
     * @param folderId the parent folder id
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException
     * @throws InvalidArgumentException 
     */
    private void createIpAddrInFolder(String folderId) 
            throws ApplicationObjectNotFoundException, NotAuthorizedException, 
            InvalidArgumentException
    {
        long ipAddrCount = ipamService.getFolderItemsCount(folderId, Constants.CLASS_IP_ADDRESS);
        if(ipAddrCount > 0){
            lblIAddrsTittle.setText(String.format("%s: %s"
                , ts.getTranslatedString("module.ipam.lbl.created-ip-addresses"), ipAddrCount));
            if(gridIpAddrs != null)
                gridIpAddrs.getDataProvider().refreshAll();
            else{
                
                gridIpAddrs = new GridIpAddresses(folderId, null
                                    , Constants.CLASS_IP_ADDRESS, ipamService
                                    , bem, mem, actReleaseIpAddrFromNetworkInterface, ts);
                lytTableGridIpAddrs.add(gridIpAddrs);
            }
        }
    }
    
    /**
     * Creates the IP address table when a subnet is selected
     * @param subnetId the subnet parent id
     * @param subnetClassName the subnet parent class name
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws NotAuthorizedException
     * @throws InvalidArgumentException 
     */
    private void createIpAddrInSubnet(String subnetId, String subnetClassName){
        if(gridIpAddrs != null)
            gridIpAddrs.getDataProvider().refreshAll();
        
        else{
            gridIpAddrs = new GridIpAddresses(subnetId, subnetClassName, null
                            , ipamService, bem, mem, actReleaseIpAddrFromNetworkInterface, ts);
            gridIpAddrs.addSelectionListener(i -> {
                i.getFirstSelectedItem().ifPresent(o ->{
                    currentSelectedNode = new IpamNode(o);
                    updatePropertySheet();
                });
            });
            lblIAddrsTittle.setText(ts.getTranslatedString("module.ipam.lbl.created-ip-addresses"));
            gridIpAddrs.getDataProvider().refreshAll();
            lytTableGridIpAddrs.add(gridIpAddrs);
        }
    }
    
    private void createSelectedIpAddrDetails(String id, String className){
        try {
            HashMap<String, List<BusinessObjectLight>> specialAttributes = bem.getSpecialAttributes(
                    className, id, IpamModule.RELATIONSHIP_IPAMHASADDRESS);
            List<BusinessObjectLight> parentsLocation = new ArrayList<>();
            BusinessObjectLight device = null; 
            //The port related with the  IP address
            for (Map.Entry<String, List<BusinessObjectLight>> entry : specialAttributes.entrySet()) {
                List<BusinessObjectLight> ports = entry.getValue();
                //it should be only one port related
                if(mem.isSubclassOf(Constants.CLASS_GENERICPORT, ports.get(0).getClassName())){
                    Label lblNetworkInterface = new Label(String.format("%s: %s"
                            , ts.getTranslatedString("module.ipam.subnet.ip-addr.interface")
                            , ports.get(0).toString()));
                    lytTableGridIpAddrs.add(lblNetworkInterface);
                    List<BusinessObjectLight> parents = bem.getParents(ports.get(0).getClassName(), ports.get(0).getId());
                    boolean isLocation = false;
                    for (BusinessObjectLight parent : parents) {
                        if (!parent.getClassName().equals(Constants.DUMMY_ROOT) && mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, parent.getClassName())){
                            isLocation = true;
                            device = parent;
                        }
                        else if(isLocation)
                            parentsLocation.add(parent);
                    }
                }
            }
            //Gui device
            if(device != null){
                lytTableGridIpAddrs.add(new Label(String.format("%s: %s"
                        , ts.getTranslatedString("module.ipam.subnet.ip-addr.device")
                        , device.getName())));
                //Location 
                HorizontalLayout lytLocation = new HorizontalLayout(new Label(String.format("%s: ", ts.getTranslatedString("module.ipam.subnet.ip-addr.location")))
                        , createIpaddrBreadCrumbs(parentsLocation));
                lytTableGridIpAddrs.add(lytLocation);
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    /**
     * Creates a graphical representation in squares of all the IP addresses of
     * the last segment of a given IPv4 subnet
     * @param subnet the given subnet
     */    
    private void createGraphicalIpAddressesDetails(SubnetDetail subnetDetail, String subnetId, String subnetClassName){
        try {
            List<String> splitedRange = new ArrayList();
            int start = Integer.valueOf(subnetDetail.getNetworkIpAddr().split("\\.")[3]);
            int end = Integer.valueOf(subnetDetail.getBroadCastIpAddr().split("\\.")[3]);
            
            for(int i = start; i <= end; i++)
                splitedRange.add(Integer.toString(i));
            
            List<BusinessObjectLight> ipsCreated = ipamService.getSubnetIpAddrCreated(subnetId, subnetClassName, -1, -1);
            
            for (String ipAddrNum : splitedRange){
                String state = DivIpAddr.STATE_NOT_CREATED;
                
                BusinessObjectLight ipAddrress = null;
                
                for (BusinessObjectLight usedIpAddr : ipsCreated) {
                    if(IpamEngine.getIpv4Segment(usedIpAddr.getName(), 3).equals(ipAddrNum)){
                        
                        List<BusinessObjectLight> rels = bem.getSpecialAttribute(
                                Constants.CLASS_IP_ADDRESS, usedIpAddr.getId()
                                , IpamModule.RELATIONSHIP_IPAMHASADDRESS);
                        
                        if(rels.isEmpty()){
                            state = DivIpAddr.STATE_FREE;
                            String reserverd = bem.getAttributeValueAsString(Constants.CLASS_IP_ADDRESS, usedIpAddr.getId(), Constants.PROPERTY_STATE);
                            if(reserverd != null && reserverd.toLowerCase().equals("reserved"))
                                state = DivIpAddr.STATE_RESERVED;
                        }
                        else
                            state = DivIpAddr.STATE_BUSY;
                        ipAddrress = usedIpAddr;
                    }
                }
                
                DivIpAddr divIp = new DivIpAddr(ipAddrress, subnetDetail, ipAddrNum, state);
                
                divIp.addClickListener(e -> createShortDetailDialog(((DivIpAddr) e.getSource())));
                lytDivsAddrs.add(divIp);
            }
        } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | BusinessObjectNotFoundException | NotAuthorizedException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Creates a short dialog with the IP address details, does not include relationships
     * @param divIp a component div ip
     */
    private void createShortDetailDialog(DivIpAddr divIp){
        Dialog dlgIpAddr = new Dialog();
        dlgIpAddr.setDraggable(true);
        dlgIpAddr.setCloseOnEsc(true);
        dlgIpAddr.setWidth("950px");
        
        Icon icnLock = new Icon(VaadinIcon.LOCK);
        icnLock.setSize("16px");

        Checkbox btnReserved = new Checkbox(ts.getTranslatedString("module.ipam.states.ip-addr.reserved"));
        btnReserved.setValue(false);
        btnReserved.addValueChangeListener(e -> {
            try {
                if(divIp.getIpAddrId() == null){
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(Constants.PROPERTY_NAME, divIp.getSubnetSegment() + divIp.getIpAddr());
                    attributes.put(Constants.PROPERTY_IS_MANAGEMENT, Boolean.toString(e.getValue()));
                    attributes.put(Constants.PROPERTY_MASK, divIp.getMask());

                    ActionResponse actionResponse = actAddIpAddr.getCallback().execute(
                            new ModuleActionParameterSet(new ModuleActionParameter<>(Constants.PROPERTY_PARENT_ID, divIp.getSubnetId()),
                                    new ModuleActionParameter<>(Constants.PROPERTY_PARENT_CLASS_NAME, divIp.getParentClassName()),
                                    new ModuleActionParameter<>(Constants.PROPERTY_ATTRIBUTES, attributes))
                    );
                    divIp.setIpAddrId((String)actionResponse.getProperty(Constants.PROPERTY_ID));
                    divIp.setClassName(DivIpAddr.STATE_FREE);
                }
                if(divIp.getIpAddrId() != null){
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(Constants.PROPERTY_STATE, "Reserved");
                        actUpdateIpAddr.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>(Constants.PROPERTY_ID, divIp.getIpAddrId()),
                                new ModuleActionParameter<>(Constants.PROPERTY_ATTRIBUTES, attributes)
                        ));
                    if(e.getValue()){
                        divIp.removeClassName(DivIpAddr.STATE_FREE);
                        divIp.setClassName(DivIpAddr.STATE_RESERVED);
                    }
                    else{
                        divIp.removeClassName(DivIpAddr.STATE_RESERVED);
                        divIp.setClassName(DivIpAddr.STATE_FREE);
                    }
                }
                if(gridIpAddrs != null){
                    gridIpAddrs.getDataProvider().refreshAll();

                    for (IpamNode node : currentTreeData) {
                        if(node.getId().equals(divIp.getSubnetId()))
                            navTree.getDataProvider().refreshItem(node);
                    }
                }
                
            } catch (ModuleActionException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
        
        Label lblTitle = new Label(String.format("%s: %s%s"
                , ts.getTranslatedString("module.ipam.lbl.details-ip-address")
                , divIp.getSubnetSegment(), divIp.getIpAddr()
        ));
        
        HorizontalLayout lytButtons = new HorizontalLayout();
        HorizontalLayout lytTitle = new HorizontalLayout(lblTitle, lytButtons);
        lytTitle.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        
        VerticalLayout lytIpAddrContent = new VerticalLayout(lytTitle);
        
        try {
            if(divIp.getIpAddrId() == null)
                lytButtons.add(btnReserved);
            else{
                HashMap<String, String> attributes = bem.getAttributeValuesAsString(Constants.CLASS_IP_ADDRESS, divIp.getIpAddrId());
                if(attributes.containsKey(Constants.PROPERTY_STATE))
                    btnReserved.setValue(attributes.get(Constants.PROPERTY_STATE) != null && attributes.get(Constants.PROPERTY_STATE).toLowerCase().equals("reserved"));
                
                HashMap<String, List<BusinessObjectLight>> interfaces = bem.getSpecialAttributes(Constants.CLASS_IP_ADDRESS, divIp.getIpAddrId(), IpamModule.RELATIONSHIP_IPAMHASADDRESS);
                List<BusinessObjectLight> parentsLocation = new ArrayList<>();
                BusinessObjectLight device = null; 
                
                //The port related with the  IP address
                String relatedInterface = "";
                for (Map.Entry<String, List<BusinessObjectLight>> entry : interfaces.entrySet()) {
                    List<BusinessObjectLight> ports = entry.getValue();
                    //it should be only one port related
                    if(ports.size() == 1 && mem.isSubclassOf(Constants.CLASS_GENERICPORT, ports.get(0).getClassName())){
                        relatedInterface = ports.get(0).toString();
                        List<BusinessObjectLight> parents = bem.getParents(ports.get(0).getClassName(), ports.get(0).getId());
                        boolean isLocation = false;
                        for (BusinessObjectLight parent : parents) {
                            if (!parent.getClassName().equals(Constants.DUMMY_ROOT) && mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, parent.getClassName())){
                                isLocation = true;
                                device = parent;
                            }
                            else if(isLocation)
                                parentsLocation.add(parent);
                        }
                    }
                }
                lytIpAddrContent.add(new Label(String.format("%s: %s"
                                , ts.getTranslatedString("module.ipam.subnet.ip-addr.related-interface")
                                , relatedInterface)));
                 
                String relatedServices = "";
                HashMap<String, List<BusinessObjectLight>> relServices = bem.getSpecialAttributes(Constants.CLASS_IP_ADDRESS, divIp.getIpAddrId(), "uses");
                for (Map.Entry<String, List<BusinessObjectLight>> entry : relServices.entrySet()) {
                    List<BusinessObjectLight> services = entry.getValue();
                    for (BusinessObjectLight s : services)
                        relatedServices += s.getName() + ", ";
                }
                
                lytIpAddrContent.add(new Label(String.format("%s: %s"
                                , ts.getTranslatedString("module.ipam.subnet.ip-addr.related-service")
                                , relatedServices)));
                //Gui device
                if(device != null){
                    FormattedObjectDisplayNameSpan deviceName = new FormattedObjectDisplayNameSpan(device, false, false, true, false);
                    HorizontalLayout lytDevice = new HorizontalLayout(new Label(String.format("%s: ", ts.getTranslatedString("module.ipam.subnet.ip-addr.device")))
                            , deviceName);
                    lytIpAddrContent.add(lytDevice);
                    //Location 
                    HorizontalLayout lytLocation = new HorizontalLayout(new Label(String.format("%s: ", ts.getTranslatedString("module.ipam.subnet.ip-addr.related-device-location")))
                            , createIpaddrBreadCrumbs(parentsLocation));
                    lytIpAddrContent.add(lytLocation);
                }
                if(interfaces.isEmpty() && device == null)
                    lytButtons.add(btnReserved);
            }
        } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
        }
        
        dlgIpAddr.add(lytIpAddrContent);
        dlgIpAddr.open();
    }
    
    /**
     * Creates a graphical path of a given objects location
     * @param parents the list of objects 
     * @return Graphical path
     */
    private Div createIpaddrBreadCrumbs(List<BusinessObjectLight> parents){
        Div divPowerline = new Div();
        divPowerline.setWidthFull();
        divPowerline.getStyle().set("display", "flex");
        
        parents.forEach(parent -> {
            Span span = new Span(new Label(parent.getClassName().equals(Constants.DUMMY_ROOT) ? "/" : parent.getName()));
            span.setSizeUndefined();
            span.setTitle(String.format("[%s]", parent.getClassDisplayName() == null || parent.getClassDisplayName().isEmpty()));
            span.addClassNames("parent-backward", "ipam-parents-color");
            divPowerline.add(span);
        });
        return divPowerline;
    }
   
    /**
     * Creates the initial content details for the selected root
     */
    private void createStartPage(){
        try {//We load the default nodes one time
            if(ipamService.getIpv4Root() == null || ipamService.getIpv6Root()== null)
                ipamService.getFoldersInFolder("-1", null, 0, 0);

            lytSearchResults.removeAll();
            lytSubnetUsageChart.removeAll();
            lytDivsAddrs.removeAll();
            lytTableGridIpAddrs.removeAll();
            lytTableGridSubnets.removeAll();
                        
            List<IpamNode> results = new ArrayList<>();
            results.add(seletedIpv == 4 ? new IpamNode(ipamService.getIpv4Root()) : new IpamNode(ipamService.getIpv6Root()));
            grdSearchResults = new Grid();
            
            grdSearchResults.addThemeVariants(GridVariant.LUMO_COMPACT,
                    GridVariant.LUMO_ROW_STRIPES);
            
            grdSearchResults.setSelectionMode(Grid.SelectionMode.SINGLE);
            grdSearchResults.setHeightByRows(true);
            grdSearchResults.setItems(results);
            grdSearchResults.addComponentColumn(item -> {
                createActions(item, true);
                return item.getNodeIconLabel();
            });
             
            grdSearchResults.addComponentColumn(item -> item.getActionsVisualComponent())
                    .setTextAlign(ColumnTextAlign.END);
           
            grdSearchResults.addSelectionListener(item -> {
                item.getFirstSelectedItem().ifPresent(i -> {
                    currentSelectedNode = i;
                    updatePropertySheet();
                    loadTreeGrid(results.get(0));
                    i.setSelected(true);
                    grdSearchResults.getDataProvider().refreshItem(i);
                });
            });
            
            Label lblResults = new Label(ts.getTranslatedString("module.general.labels.search-results"));
            HorizontalLayout lytTitle = new HorizontalLayout(lblResults);
            lytTitle.setPadding(false);
            lytTitle.setMargin(false);
            lytTitle.setSpacing(false);
            lytTitle.setHeight("20px");
            lytSearchResults.add(lytTitle, grdSearchResults);
            grdSearchResults.select(results.get(0));
            
            btnIpv4.addClickListener(e -> {
                    seletedIpv = 4;
                    createStartPage();
            });    

            btnIpv6.addClickListener(e -> {
                    seletedIpv = 6;
                    createStartPage();
            });
            
        } catch (NotAuthorizedException | ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * After search if the enter key is pressed a table with the search results 
     * is created
     */
    private void createResultsGrid(IpamNode selectedItem){
        lytSearchResults.removeAll();
        List<IpamNode> lastSelected = new ArrayList<>();
        grdSearchResults = new Grid();

        grdSearchResults.addThemeVariants(GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_ROW_STRIPES);
        grdSearchResults.setSelectionMode(Grid.SelectionMode.SINGLE);
        grdSearchResults.setHeightByRows(true);

        grdSearchResults.addComponentColumn(item -> {
            createActions(item, false);
            return item.getNodeIconLabel();
        });

        grdSearchResults.addComponentColumn(item -> item.getActionsVisualComponent())
                .setTextAlign(ColumnTextAlign.END);

        grdSearchResults.addSelectionListener(t -> {
            t.getFirstSelectedItem().ifPresent(o ->{
                currentSelectedNode = o;
                updatePropertySheet();

                navTree.getTreeData().clear();
                currentTreeData.clear();
                loadTreeGrid(o);

                o.setSelected(true);
                grdSearchResults.getDataProvider().refreshItem(o);

                if(!lastSelected.isEmpty() && lastSelected.size() == 1){
                    lastSelected.get(0).setSelected(false);
                    grdSearchResults.getDataProvider().refreshItem(lastSelected.get(0));
                    lastSelected.clear();
                }

                if(lastSelected.isEmpty())
                    lastSelected.add(o);

                createDeatilsRightPanel(o.getObject());
            });
        });

        grdSearchResults.setItems(lastSearchResults);

        if(grdSearchResults.getDataProvider().size(new Query<>()) == 0)
            createStartPage();
        else{ //we must select to create everything if a click was made in the search results
            if(selectedItem != null)
                grdSearchResults.select(lastSearchResults.get(lastSearchResults.indexOf(selectedItem)));
            else
                grdSearchResults.select(lastSearchResults.get(0));

            Label lblResults = new Label(ts.getTranslatedString("module.general.labels.search-results"));
            HorizontalLayout lytTitle = new HorizontalLayout(lblResults);
            lytTitle.setPadding(false);
            lytTitle.setMargin(false);
            lytTitle.setSpacing(false);
            lytTitle.setHeight("20px");
            lytSearchResults.add(lytTitle, grdSearchResults);
        }
    }
       
    /**
     * Populates the left table after a search or a modification
     */
    private void createSearchResultsDataProvider(){
        try {
            lastSearchResults.clear();
            HashMap<String, List<BusinessObjectLight>> searchResults;
            HashMap<String, List<InventoryObjectPool>> suggestedPoolsByName;
            
            List<String> classes = new ArrayList<>();
            classes.add(Constants.CLASS_SUBNET_IPV4);
            classes.add(Constants.CLASS_SUBNET_IPV6);
            classes.add(Constants.CLASS_IP_ADDRESS);

            searchResults = bem.getSuggestedObjectsWithFilterGroupedByClassName(classes
                    , lastSearch != null ? lastSearch : ""
                    , 0, 5    
                    , 0, 10);

            List<String> folders = new ArrayList<>();
            folders.add(Constants.CLASS_GENERICADDRESS);

            suggestedPoolsByName = bem.getSuggestedPoolsByName(folders
                    , lastSearch != null ? lastSearch : ""
                    , 0, 5, 0, 10);
            
            searchResults.entrySet().forEach(entry ->
                entry.getValue().forEach(o -> lastSearchResults.add(new IpamNode(o)))
            );
            
            suggestedPoolsByName.entrySet().forEach(entry ->
                    entry.getValue().forEach(o -> lastSearchResults.add(new IpamNode(o)))
            );
            
            grdSearchResults.setItems(lastSearchResults);
            
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * The central navigation tree
     * @param rootNode the selected item in the search results table
     */
    private void loadTreeGrid(IpamNode aNode){
        navTree = new NavTreeGrid<IpamNode>() {
            @Override
            public List<IpamNode> fetchData(IpamNode node) {
                List<IpamNode> childrenNodes  = new ArrayList<>();
                try{
                    if(node.isPool()){
                        //subnets
                        List<BusinessObjectLight> subnetsInfolder = ipamService.getFolderItems(node.getId()
                                , seletedIpv == 4 ? Constants.CLASS_SUBNET_IPV4 : Constants.CLASS_SUBNET_IPV6
                                , 0, 50);
                        subnetsInfolder.forEach(item -> childrenNodes.add(new IpamNode(item)));
                        //Indivudal ip addreses
                        List<BusinessObjectLight> ipsInfolder = ipamService.getFolderItems(node.getId()
                                , Constants.CLASS_IP_ADDRESS, 0, 50);
                        ipsInfolder.forEach(item -> childrenNodes.add(new IpamNode(item)));
                        //folders in folder
                        List<InventoryObjectPool> foldersInfolder = ipamService.getFoldersInFolder(node.getId(), node.getClassName(), 0, 50);
                        foldersInfolder.forEach(folder -> childrenNodes.add(new IpamNode(folder)));
                    }
                    else{
                        List<BusinessObjectLight> subnetsInSubnet = ipamService.getSubnetsInSubnet(node.getId(), node.getClassName(), 0, 50);
                        subnetsInSubnet.forEach(subnet -> childrenNodes.add(new IpamNode(subnet)));
                    }
                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | NotAuthorizedException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts).open();
                }

                return childrenNodes;
            }
        };
        //we load the data
        navTree.createDataProvider(aNode);
        
        navTree.addComponentHierarchyColumn(item -> {
            createActions(item, false);
            return item.getNodeIconLabel();
        });
        //actions
        navTree.addComponentColumn(item -> item.getActionsVisualComponent())
                .setTextAlign(ColumnTextAlign.END);
        
        navTree.addThemeVariants(GridVariant.LUMO_COMPACT);
        navTree.setSelectionMode(Grid.SelectionMode.SINGLE);
        navTree.setHeightByRows(true);
        
        navTree.addSelectionListener(e -> {
            e.getFirstSelectedItem().ifPresent(o -> {
                currentSelectedNode = o;
                createDeatilsRightPanel(o.getObject());
            });
            updatePropertySheet();
        });   
        
        lytCenter.removeAll();
        lytCenter.setVisible(true);
        
        Icon icn = aNode.isPool() ? new Icon(VaadinIcon.FOLDER_OPEN) : new Icon(VaadinIcon.SITEMAP);
        icn.setSize("18px");
        
        HorizontalLayout lytTitle = new HorizontalLayout(icn, new Label(aNode.getName()));
        lytTitle.setPadding(false);
        lytTitle.setMargin(false);
        lytTitle.setSpacing(true);
        lytTitle.setHeight("20px");
        lytTitle.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        
        lytCenter.add(lytTitle);
        lytCenter.add(navTree);
        lytCenter.add(lblNoChildren);
        
        int size = navTree.getDataProvider().size(new HierarchicalQuery<>(null, null));
        if(size == 0)
            lblNoChildren.setText(!aNode.isPool() ? ts.getTranslatedString("module.ipam.lbl.no-nested-subnets") : 
                ts.getTranslatedString("module.ipam.lbl.folder-empty"));
        else
            lblNoChildren.setText("");
    }
    
    /**
     * Create actions for nav tree
     * @param item the selected item in the nav tree
     * @return a horizontal layout with the button actions
     */
    private void createActions(IpamNode item, boolean isRoot){
        ActionButton btnMarkAsFavorite =  new ActionButton(new Icon(VaadinIcon.STAR_O));
        btnMarkAsFavorite.setId(IpamNode.ID_ACTION_BTN_MARK_AS_FAVORITE);
        btnMarkAsFavorite.setEnabled(false);
            
        if(item.isPool()){ //Folders
            ActionButton btnAddFolder = new ActionButton(new ActionIcon(VaadinIcon.FOLDER_ADD)
                , actNewFolder.getModuleAction().getDescription());
            btnAddFolder.setId(IpamNode.ID_ACTION_ADD_FOLDER);
            btnAddFolder.addClickListener(e -> {
                actionAffectedNode.set(0, item);
                this.actNewFolder.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(NewFolderVisualAction.PARAM_PARENT_FOLDER_ID, item.getId())
                        , new ModuleActionParameter(NewFolderVisualAction.PARAM_PARENT_FOLDER_CLASSNAME, item.getClassName())))
                        .open();
            });

            ActionButton btnDeleteFolder = new ActionButton( new ActionIcon(VaadinIcon.FOLDER_REMOVE)
                    , actDelFolder.getModuleAction().getDescription());
            btnDeleteFolder.setId(IpamNode.ID_ACTION_DEL_FOLDER);
            btnDeleteFolder.addClickListener(e -> {
                actionAffectedNode.set(0, item);
                this.actDelFolder.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(DeleteFolderVisualAction.PARAM_FOLDER, item.getObject())
                )).open();
            });

            ActionButton btnAddsubnetInFolder = new ActionButton(new ActionIcon(VaadinIcon.PLUS)
                    , actNewSubnet.getModuleAction().getDescription());
            btnAddsubnetInFolder.setId(IpamNode.ID_ACTION_ADD_SUBNET_TO_FOLDER);
            btnAddsubnetInFolder.addClickListener(e -> {
                actionAffectedNode.set(0, item);
                this.actNewSubnet.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(NewSubnetVisualAction.PARAM_PARENT_ID,  item.getId())
                    , new ModuleActionParameter(NewSubnetVisualAction.PARAM_PARENT_CLASSNAME,  Constants.CLASS_GENERICADDRESS)
                    , new ModuleActionParameter(NewSubnetVisualAction.PARAM_CLASSNAME, seletedIpv == 4 ? Constants.CLASS_SUBNET_IPV4 : Constants.CLASS_SUBNET_IPV6)
                )).open();
            });
            
            if(isRoot)
                item.addVisualActions(btnAddFolder, btnAddsubnetInFolder);
            
            else{
                ActionButton btnDeleteSubnetFromFolder = new ActionButton(new Icon(VaadinIcon.TRASH)
                    , actDelSubnet.getModuleAction().getDescription());
                btnDeleteSubnetFromFolder.setId(IpamNode.ID_ACTION_DEL_SUBNET_FROM_FOLDER);
                btnDeleteSubnetFromFolder.addClickListener(e -> {
                        actionAffectedNode.set(0, item);
                        this.actDelSubnet.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(DeleteSubnetVisualAction.PARAM_SUBNET, item.getObject())
                        , new ModuleActionParameter(DeleteSubnetVisualAction.PARAM_CLASSNAME, item.getClassName())
                    )).open();
                });

                ActionButton btnAddIpAddrToFolder = new ActionButton(new Icon(VaadinIcon.PLUS_CIRCLE)
                        , actAddIpAddrToFolder.getModuleAction().getDescription());
                btnAddIpAddrToFolder.setId(IpamNode.ID_ACTION_BTN_ADD_IP_TO_FOLDER);
                btnAddIpAddrToFolder.addClickListener(e -> {
                    actionAffectedNode.set(0, item);
                    this.actAddIpAddrToFolder.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter(AddIpAddrToFolderVisualAction.PARAM_PARENT_ID, item.getId())
                        , new ModuleActionParameter(AddIpAddrToFolderVisualAction.PARAM_IPV, seletedIpv))).open();
                });
                
                item.addVisualActions(btnAddFolder, btnDeleteFolder, btnAddsubnetInFolder, btnAddIpAddrToFolder, btnMarkAsFavorite);
            }
        }
        else if(item.getClassName().equals(Constants.CLASS_IP_ADDRESS)){
            ActionButton btnDelIndividualIpAddr =  new ActionButton(new Icon(VaadinIcon.TRASH)
                    , actDeleteIpAddrFromFolder.getModuleAction().getDescription());
            btnDelIndividualIpAddr.setId(IpamNode.ID_ACTION_BTN_DEL_IP_TO_SUBNET);
            btnDelIndividualIpAddr.addClickListener(e -> {
                actionAffectedNode.set(0, item);
                this.actDeleteIpAddrFromFolder.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(DeleteIpAddrFromFolderVisualAction.PARAM_IP_ADDR, item.getObject())
                )).open();
            });

            item.addVisualActions(btnDelIndividualIpAddr);
        }
        else if(!item.getClassName().equals(Constants.CLASS_IP_ADDRESS)){
            try {
                ActionButton btnDeleteSubnetFromSubnet = new ActionButton(new Icon(VaadinIcon.TRASH)
                        , actDelSubnet.getModuleAction().getDescription());
                btnDeleteSubnetFromSubnet.setId(IpamNode.ID_ACTION_BTN_DEL_SUBNET_FROM_SUBNET);
                btnDeleteSubnetFromSubnet.addClickListener(e -> {
                    actionAffectedNode.set(0, item);
                    this.actDelSubnet.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(DeleteSubnetVisualAction.PARAM_SUBNET, item.getObject())
                            , new ModuleActionParameter(DeleteSubnetVisualAction.PARAM_CLASSNAME, item.getClassName())
                    )).open();
                });
                
                ActionButton btnAddsubnetInSubnet = new ActionButton(new ActionIcon(VaadinIcon.PLUS)
                        , actNewSubnet.getModuleAction().getDescription());
                btnAddsubnetInSubnet.setId(IpamNode.ID_ACTION_BTN_ADD_SUBNET_IN_SUBNET);
                btnAddsubnetInSubnet.addClickListener(e -> {
                    actionAffectedNode.set(0, item);
                    this.actNewSubnet.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(NewSubnetVisualAction.PARAM_PARENT_ID, item.getId())
                            , new ModuleActionParameter(NewSubnetVisualAction.PARAM_CIDR, item.getName())
                            , new ModuleActionParameter(NewSubnetVisualAction.PARAM_CLASSNAME, item.getClassName())
                            , new ModuleActionParameter(NewSubnetVisualAction.PARAM_PARENT_CLASSNAME, item.getClassName())  
                    )).open();
                });

                ActionButton btnSplitSubnet = new ActionButton(new Icon(VaadinIcon.SPLIT)
                        , actSplitSubnet.getModuleAction().getDescription());
                btnSplitSubnet.setId(IpamNode.ID_ACTION_BTN_SPLIT_SUBNET);
                btnSplitSubnet.addClickListener(e -> {
                    actionAffectedNode.set(0, item);
                    try {
                        BusinessObject subnet = bem.getObject(item.getClassName(), item.getId());
                        this.actSplitSubnet.getVisualComponent(new ModuleActionParameterSet(
                                new ModuleActionParameter(SplitSubnetVisualAction.PARAM_SUBNET, subnet)
                        )).open();
                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                    }
                });

                ActionButton btnAddIpAddrInSubnet = new ActionButton(new Icon(VaadinIcon.PLUS_CIRCLE)
                        , actAddIpAddrVisual.getModuleAction().getDescription());
                btnAddIpAddrInSubnet.setId(IpamNode.ID_ACTION_BTN_ADD_IP_TO_SUBNET);
                btnAddIpAddrInSubnet.addClickListener(e -> {
                    actionAffectedNode.set(0, item);
                    this.actAddIpAddrVisual.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter(AddIpAddrToSubnetVisualAction.PARAM_SUBNET, (BusinessObjectLight)item.getObject())
                    )).open();
                });
                    
                if(item.getClassName().equals(Constants.CLASS_SUBNET_IPV4))
                    item.addVisualActions(btnAddsubnetInSubnet, btnDeleteSubnetFromSubnet, btnSplitSubnet, btnAddIpAddrInSubnet, btnMarkAsFavorite);
                else if(item.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
                    item.addVisualActions(btnAddsubnetInSubnet, btnDeleteSubnetFromSubnet, btnAddIpAddrInSubnet, btnMarkAsFavorite);
                //we enable or disable actions     
                long subnets = ipamService.getSubnetsInSubnetCount(item.getId(), item.getClassName());
                List<BusinessObjectLight> ipAddrs = ipamService.getSubnetIpAddrCreated(item.getId(), item.getClassName(), -1, -1);
                
                if(ipAddrs.isEmpty() && subnets == 0){
                    item.enableSubnetsActions(true);
                    item.enableIpAddressActions(true);
                }
                else if(subnets > 0 && ipAddrs.isEmpty()){
                    item.enableSubnetsActions(true);
                    item.enableIpAddressActions(false);
                }
                else if(!ipAddrs.isEmpty() && subnets == 0){
                   item.enableIpAddressActions(true);
                   item.enableSubnetsActions(false);
                }
                
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | NotAuthorizedException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
        else{
            ActionButton btnToogleReserveIpAddr =  new ActionButton(new Icon(VaadinIcon.TRASH)
                    , actDeleteIpAddrFromFolder.getModuleAction().getDescription());
            
            btnToogleReserveIpAddr.addClickListener(e -> {
                actionAffectedNode.set(0, item);
                this.actDeleteIpAddrFromFolder.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter(Constants.PROPERTY_ID, item.getId()))).open();
            });
            
            item.addVisualActions(btnToogleReserveIpAddr, btnMarkAsFavorite);
        }
    }
    
    /**
     * Setups the UI components
     * header
     * subnets | details subnets
     * table IP address
     * table visual IPs 
     */
    private void setupLayouts(){
        searchDialog = new DialogIpamSearch(ts, bem,  e ->{
            grdSearchResults = null; 
            if(!searchDialog.getResults().isEmpty()){
                lastSearchResults = searchDialog.getResults();
                if(!(e instanceof String))//we click on a single element
                    createResultsGrid((IpamNode)e);
                lastSearch = searchDialog.getSearchedText();

                createResultsGrid(null);
                searchDialog.close();
                searchDialog.clearSearch();
            }
        });
        
        btnIpv4 = new Button(ts.getTranslatedString("module.ipam.actions.explore.ipv4")); 
        btnIpv6 = new Button(ts.getTranslatedString("module.ipam.actions.explore.ipv6"));
        
        lytHeader = new HorizontalLayout(searchDialog, btnIpv4, btnIpv6);
        lytHeader.setSpacing(true);
        lytHeader.setPadding(false);
        lytHeader.setMargin(false);
        lytHeader.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytHeader.setJustifyContentMode(JustifyContentMode.CENTER);
        lytHeader.setWidthFull();
        lytHeader.setId("lyt-header");
        //End header
        
        //left side
        lytSearchResults = new VerticalLayout();
        lytSearchResults.setSpacing(true);
        lytSearchResults.setMargin(false);
        lytSearchResults.setPadding(false);
        lytSearchResults.setId("lyt-search-result");
        
        lytLeftSide = new VerticalLayout();
        lytLeftSide.setWidth("27%");
        lytLeftSide.setSpacing(true);
        lytLeftSide.setMargin(false);
        lytLeftSide.setPadding(true);
        lytLeftSide.setId("lyt-left-side");
        lytLeftSide.add(lytSearchResults);
//Center column        
        lytCenter = new VerticalLayout();
        lytCenter.setWidth("33%");
        lytCenter.setBoxSizing(BoxSizing.BORDER_BOX);
        lytCenter.setSpacing(true);
        lytCenter.setMargin(false);
        lytCenter.setPadding(true);
        lytCenter.setId("central-side");
//Right side
        lytProperties = new VerticalLayout();
        lytProperties.setBoxSizing(BoxSizing.BORDER_BOX);
        lytProperties.setSpacing(false);
        lytProperties.setMargin(false);
        lytProperties.setPadding(false);
        lytProperties.setId("properties");
        lytProperties.setHeight("100%");
        lytProperties.add(propertySheetPool);
            
        lytSubnetUsageChart = new VerticalLayout();
        lytSubnetUsageChart.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        lytSubnetUsageChart.setId("chart-container");
        lytSubnetUsageChart.setSpacing(false);
        lytSubnetUsageChart.setMargin(false);
        lytSubnetUsageChart.setPadding(false);
        
        lytDetails = new HorizontalLayout(lytSubnetUsageChart, lytProperties);
        lytDetails.setWidth("92%");
        lytDetails.setBoxSizing(BoxSizing.BORDER_BOX);
        lytDetails.setSpacing(false);
        lytDetails.setMargin(false);
        lytDetails.setPadding(false);
        lytDetails.setId("tittle");
        
        lytTableGridIpAddrs = new VerticalLayout();
        lytTableGridIpAddrs.setWidth("92%");
        lytTableGridIpAddrs.setBoxSizing(BoxSizing.BORDER_BOX);
        lytTableGridIpAddrs.setSpacing(true);
        lytTableGridIpAddrs.setMargin(false);
        lytTableGridIpAddrs.setPadding(false);
        lytTableGridIpAddrs.setId("usdedIpaddrTable");
        
        lytTableGridSubnets = new VerticalLayout();
        lytTableGridSubnets.setWidth("92%");
        lytTableGridSubnets.setBoxSizing(BoxSizing.BORDER_BOX);
        lytTableGridSubnets.setSpacing(true);
        lytTableGridSubnets.setMargin(false);
        lytTableGridSubnets.setPadding(false);
        lytTableGridSubnets.setId("subnetsTable");

//the graphical representation of IP addresses in small squares
        lytDivsAddrs = new FlexLayout();
        lytDivsAddrs.setClassName("ipam-square"); //NOI18N
        lytDivsAddrs.setWidth("98%");
        
        lblRightTitle.setClassName("ipam-property-sheet-details");
        lytRightTitle = new HorizontalLayout(lblRightTitle);
        lytRightTitle.setId("right-side-title");
        lytRightTitle.setPadding(false);
        lytRightTitle.setMargin(false);
        lytRightTitle.setSpacing(false);
        lytRightTitle.setJustifyContentMode(JustifyContentMode.START);
        lytRightTitle.setWidthFull();
        
        lytRight = new VerticalLayout();
        lytRight.setWidth("40%");
        lytRight.setBoxSizing(BoxSizing.BORDER_BOX);
        lytRight.setSpacing(true);
        lytRight.setMargin(true);
        lytRight.setPadding(false);
        lytRight.setId("detail-rigth-side");
        lytRight.setDefaultHorizontalComponentAlignment(Alignment.START);
        lytRight.add(lytRightTitle, lytDetails, lytTableGridSubnets, lytTableGridIpAddrs, lytDivsAddrs);
        
        HorizontalLayout lytMain = new HorizontalLayout();
        lytMain.setId("ipam-content-id");
        lytMain.setWidthFull();
        lytMain.setHeight("9%");
        lytMain.setHeightFull();
        lytMain.setSpacing(false);
        lytMain.setMargin(false);
        lytMain.setPadding(false);
        lytMain.add(lytLeftSide, lytCenter, lytRight);
        
        lytContent = new VerticalLayout(lytHeader, lytMain);
        lytContent.setId("ipam-content-id");
        lytContent.setSizeFull();
        lytContent.setHeight("91%");
        lytContent.setSpacing(false);
        lytContent.setMargin(false);
        lytContent.setPadding(false);
        
        add(lytContent);
    }
    
    /**
     * Property sheet
     */
    private void updatePropertySheet() {
        if(currentSelectedNode != null){
            lblRightTitle.setText(currentSelectedNode.getName());
            if(currentSelectedNode.isPool()){
                    lytSubnetUsageChart.setWidth("0px");
                    lytSubnetUsageChart.setHeight("0px");
                    lytSubnetUsageChart.removeAll();
                    lytProperties.setWidthFull();
                    lytDetails.setHeight("65px");
                    
                    List<AbstractProperty> properties = PropertyFactory
                                .propertiesFromPoolWithoutClassName(
                                        (InventoryObjectPool)currentSelectedNode.getObject(), ts);
                            
                    if(currentSelectedNode.getId().equals(ipamService.getIpv4Root().getId()) 
                            || currentSelectedNode.getId().equals(ipamService.getIpv6Root().getId()))
                        properties.forEach(p -> p.setReadOnly(true));
                    
                    propertySheetPool.setItems(properties);
            }
            else{
                if(!currentSelectedNode.getClassName().equals(Constants.CLASS_IP_ADDRESS)){
                    lytSubnetUsageChart.setWidth("45%");
                    lytProperties.setWidth("50%");
                    lytDetails.setHeight("230px");
                }
                else{
                    lytSubnetUsageChart.setWidth("0px");
                    lytSubnetUsageChart.setHeight("0px");
                    lytSubnetUsageChart.removeAll();
                    lytProperties.setWidthFull();
                    lytDetails.setHeight("191px");
                }
                try {
                    BusinessObject object = bem.getObject(currentSelectedNode.getClassName(), currentSelectedNode.getId());
                    List<AbstractProperty> properties = PropertyFactory.propertiesFromBusinessObject(object, ts, aem, mem);
                    for (AbstractProperty p : properties) {
                        if(!p.getName().equals(Constants.PROPERTY_DESCRIPTION))
                            p.setReadOnly(true);
                    }
                    propertySheetPool.setItems(properties);

                } catch (Exception ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
                } 
            }
        }
        else{
            lblRightTitle.setText("");
            lytDetails.setHeight("0px");
            propertySheetPool.setItems(new ArrayList<>());
            lytSubnetUsageChart.removeAll();
            lytDivsAddrs.removeAll();
            gridIpAddrs = null;
            lytTableGridIpAddrs.removeAll();
        }
    }
    
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        if (currentSelectedNode != null) {
            try {
                if(currentSelectedNode.isPool()){
                    if(property.getName().equals(Constants.PROPERTY_NAME)){
                        aem.setPoolProperties(currentSelectedNode.getId()
                            , property.getAsString(), ((InventoryObjectPool)currentSelectedNode.getObject()).getDescription());
                        currentSelectedNode.setName(property.getAsString());
                        ((InventoryObjectPool)currentSelectedNode.getObject()).setName(property.getAsString());
                    }
                    else if(property.getName().equals(Constants.PROPERTY_DESCRIPTION)){
                        aem.setPoolProperties(currentSelectedNode.getId()
                            , currentSelectedNode.getName(), property.getAsString());
                        ((InventoryObjectPool)currentSelectedNode.getObject()).setDescription(property.getAsString());
                    }
                }
                else{
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                    bem.updateObject(currentSelectedNode.getClassName(), currentSelectedNode.getId(), attributes);
                   
                    if(property.getName().equals(Constants.PROPERTY_NAME)){
                        currentSelectedNode.setName(property.getAsString());
                        ((BusinessObjectLight)currentSelectedNode.getObject()).setName(property.getAsString());
                    }
                }
                
                if(navTree.getTreeData().contains(currentSelectedNode))
                    navTree.getDataProvider().refreshItem(currentSelectedNode);
                
                updatePropertySheet();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.general.messages.property-updated-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();

            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }
}
