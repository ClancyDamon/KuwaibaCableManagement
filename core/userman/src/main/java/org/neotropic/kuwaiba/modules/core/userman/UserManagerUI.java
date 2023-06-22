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

package org.neotropic.kuwaiba.modules.core.userman;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.userman.actions.DeleteGroupVisualAction;
import org.neotropic.kuwaiba.modules.core.userman.actions.DeleteUserVisualAction;
import org.neotropic.kuwaiba.modules.core.userman.actions.NewGroupVisualAction;
import org.neotropic.kuwaiba.modules.core.userman.actions.NewUserVisualAction;
import org.neotropic.kuwaiba.modules.core.userman.actions.UpdateGroupVisualAction;
import org.neotropic.kuwaiba.modules.core.userman.nodes.GroupNode;
import org.neotropic.kuwaiba.modules.core.userman.nodes.UserNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.icon.ActionIcon;
import org.neotropic.util.visual.icons.BasicTreeNodeIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.neotropic.util.visual.tree.BasicTree;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Main for User Manager. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "userman", layout = UserManagerLayout.class)
public class UserManagerUI extends VerticalLayout implements HasDynamicTitle, AbstractUI {
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;

    @Autowired
    private UpdateGroupVisualAction updateGroupVisualAction;
    /**
     * listener to remove update action
     */
    private ActionCompletedListener listenerUpdateGroupAction;
    
    @Autowired
    private DeleteGroupVisualAction deleteGroupVisualAction;
     /**
     * listener to remove object action
     */
    private ActionCompletedListener listenerDeleteGroupAction;
    
    @Autowired
    private NewGroupVisualAction newGroupVisualAction;
     /**
     * listener to remove object action
     */
    private ActionCompletedListener listenerNewGroupAction;
    
    @Autowired
    private NewUserVisualAction newUserVisualAction;
     /**
     * listener to remove object action
     */
    private ActionCompletedListener listenerNewUserAction;
    
    @Autowired
    private DeleteUserVisualAction deleteUserVisualAction;
     /**
     * listener to remove object action
     */
    private ActionCompletedListener listenerDeleteUserAction;
    
    /**
     * Reference to the module registry.
     */
    @Autowired
    protected ModuleRegistry moduleRegistry;
    
    /**
     * factory to build resources from data source
     */  
    @Autowired
    private ResourceFactory resourceFactory;
    
    /**
     * Layout content
     */
    private HorizontalLayout lytContent;
    /**
     * Fields
     */
    private Label lblUserTitle;
    private TextField txtUserName;
    private TextField txtFirstName;
    private TextField txtLastName;
    private TextField txtEmail;
    private PasswordField txtPassword;
    private Checkbox chkEnabled;
    private ComboBox<Integer> cmbType;  
    /**
     * Object selected
     */
    private UserProfile selectedUser;
    private GroupProfile seleGroupProfile;
    /**
     * Privileges
     */
    private VerticalLayout lytPrivileges;
    private List<Privilege> lstCurrentPrivileges;
    private boolean savePrivilege = true;
    /**
     * TreeGrid for users
     */
    private TreeGrid<AbstractNode> treeUsers;
    
    public UserManagerUI() {
        super();
        setSizeFull();
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.updateGroupVisualAction.unregisterListener(listenerUpdateGroupAction);
        this.deleteGroupVisualAction.unregisterListener(listenerDeleteGroupAction);
        this.newGroupVisualAction.unregisterListener(listenerDeleteGroupAction);
        this.newUserVisualAction.unregisterListener(listenerNewUserAction);
        this.deleteUserVisualAction.unregisterListener(listenerDeleteUserAction);
    }
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();                                          
            } catch (Exception ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ts.getTranslatedString("module.general.messages.unexpected-error"),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }
    
    /**
     * Initialize the class report tree
     */
    private void initializeClassReportsTree() {
        try {
            List<GroupProfile> grps = aem.getGroups();
            HierarchicalDataProvider dataProvider = buildHierarchicalDataProvider(grps);

            treeUsers = new BasicTree(dataProvider, new BasicTreeNodeIconGenerator(resourceFactory));
            treeUsers.addComponentColumn(item -> buildGroupAction(item)).setTextAlign(ColumnTextAlign.END).setFlexGrow(0);
            treeUsers.addItemClickListener(evt -> {
                if (evt.getItem() instanceof GroupNode)
                    seleGroupProfile = (GroupProfile) evt.getItem().getObject();
                else {
                    selectedUser = (UserProfile) evt.getItem().getObject();
                    lytContent.setVisible(true);
                    updateUserContent();
                    buildPrivilegesGrid();
                    lblUserTitle.setText(String.format("%s %s (%s)", selectedUser.getFirstName(), selectedUser.getLastName(), selectedUser.getUserName()));
                }
            });
        } catch (Exception ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.general.messages.unexpected-error"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        } 
    }
    
    private HorizontalLayout buildGroupAction(AbstractNode item) {
        HorizontalLayout lytActions = new HorizontalLayout();
        lytActions.setSpacing(false);
        lytActions.setPadding(false);
        lytActions.setMargin(false);
        
        if (item instanceof GroupNode) {
            ActionButton btnNewUser = new ActionButton(new ActionIcon(VaadinIcon.USER_CHECK),
                    ts.getTranslatedString("module.userman.actions.new-user.name"));
            btnNewUser.addClickListener(event
                    -> this.newUserVisualAction.getVisualComponent(
                            new ModuleActionParameterSet(new ModuleActionParameter("group", item.getObject()))).open());

            ActionButton btnUpdateGroup = new ActionButton(new ActionIcon(VaadinIcon.EDIT),
                    ts.getTranslatedString("module.userman.actions.update-group.name"));
            btnUpdateGroup.addClickListener(event
                    -> this.updateGroupVisualAction.getVisualComponent(
                            new ModuleActionParameterSet(new ModuleActionParameter("group", item.getObject()))).open());

            ActionButton btnDeleteGroup = new ActionButton(new ActionIcon(VaadinIcon.TRASH,
                    ts.getTranslatedString("module.userman.actions.delete-group.name"))); 
            btnDeleteGroup.addClickListener(event
                    -> this.deleteGroupVisualAction.getVisualComponent(
                            new ModuleActionParameterSet(new ModuleActionParameter("group", item.getObject()))).open());

            lytActions.add(btnNewUser, btnUpdateGroup, btnDeleteGroup);
        }
        return lytActions;
    }
    
     private HierarchicalDataProvider buildHierarchicalDataProvider(List<GroupProfile> groups) {
        return new AbstractBackEndHierarchicalDataProvider() {
            @Override
            protected Stream fetchChildrenFromBackEnd(HierarchicalQuery hq) {
                if (hq.getParent() == null) {
                    List<GroupNode> groupNodes = new ArrayList();
                    groups.forEach(group -> groupNodes.add(new GroupNode(group, group.getName())));
                    return groupNodes.stream();
                }
                if (hq.getParent() instanceof GroupNode) {
                    try {
                        GroupNode groupNode = (GroupNode) hq.getParent();
                        List<UserProfile> users = aem.getUsersInGroup(groupNode.getObject().getId());
                        List<UserNode> userNodes = new ArrayList();
                        users.forEach(user -> userNodes.add(new UserNode(user, 
                                             String.format("%s %s (%s)", user.getFirstName(),
                                                     user.getLastName(), user.getUserName()))));
                        return userNodes.stream();
                    } catch (ApplicationObjectNotFoundException ex) {
                        return Collections.EMPTY_SET.stream();
                    }
                } else
                    return Collections.EMPTY_SET.stream();
            }

            @Override
            public int getChildCount(HierarchicalQuery hq) {
                if (hq.getParent() == null) {
                    return groups.size();
                }
                if (hq.getParent() instanceof GroupNode) {                 
                    GroupNode grp = (GroupNode) hq.getParent();
                    try {
                        return aem.getUsersInGroup(grp.getObject().getId()).size();
                    } catch (ApplicationObjectNotFoundException ex) {
                        return 0;
                    }
                } else 
                    return 0;
            }

            @Override
            public boolean hasChildren(Object t) {
                return t instanceof GroupNode;
            }
        };
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.userman.title");
    }

    @Override
    public void initContent() {
        setSizeFull();
        
        // in case we are updating the page
        this.updateGroupVisualAction.unregisterListener(listenerUpdateGroupAction);
        this.deleteGroupVisualAction.unregisterListener(listenerDeleteGroupAction);
        this.deleteUserVisualAction.unregisterListener(listenerDeleteUserAction);
        this.newGroupVisualAction.unregisterListener(listenerDeleteGroupAction);
        this.newUserVisualAction.unregisterListener(listenerNewUserAction);
        
        listenerNewGroupAction = (ActionCompletedListener.ActionCompletedEvent ev) -> { 
            loadGroups();
            treeUsers.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
        };
        this.newGroupVisualAction.registerActionCompletedLister(listenerNewGroupAction);
        listenerDeleteGroupAction = (ActionCompletedListener.ActionCompletedEvent ev) -> { 
            loadGroups();
            treeUsers.getDataProvider().refreshAll();
            seleGroupProfile = null;
            lytContent.setVisible(false);
            showActionCompledMessages(ev);
        };
        this.deleteGroupVisualAction.registerActionCompletedLister(listenerDeleteGroupAction);
        
        listenerUpdateGroupAction = (ActionCompletedListener.ActionCompletedEvent ev) -> { 
            loadGroups();
            treeUsers.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
        };
        this.updateGroupVisualAction.registerActionCompletedLister(listenerUpdateGroupAction);
        
        listenerNewUserAction = (ActionCompletedListener.ActionCompletedEvent ev) -> { 
            treeUsers.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
        };
        this.newUserVisualAction.registerActionCompletedLister(listenerNewUserAction);
        
        listenerDeleteUserAction = (ActionCompletedListener.ActionCompletedEvent ev) -> { 
            selectedUser = null;
            lytContent.setVisible(false);
            treeUsers.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
        };
        this.deleteUserVisualAction.registerActionCompletedLister(listenerDeleteUserAction);
        
        Button btnNewGroup = new Button(ts.getTranslatedString("module.userman.actions.new-group.name"), 
                new Icon(VaadinIcon.PLUS), evt ->
                   this.newGroupVisualAction.getVisualComponent(new ModuleActionParameterSet()).open());
        btnNewGroup.setWidthFull();
                
        initializeClassReportsTree();
        VerticalLayout lytPrimaryContent = new VerticalLayout(btnNewGroup, treeUsers);
        lytPrimaryContent.setId("lytPrimaryContent");
        lytPrimaryContent.setHeightFull();
        
        // Sec Content
        Button btnRelateToGroup = new Button(ts.getTranslatedString("module.userman.relate-to-group"), new Icon(VaadinIcon.SIGN_IN), evt -> {
            openRelateToGroupDlg();
        });
        Button btnRemoveFromGroup = new Button(ts.getTranslatedString("module.userman.remove-from-group"), new Icon(VaadinIcon.SIGN_OUT), evt -> {
            openReleaseFromGroupDlg();
        });
        Button btnSaveUserData = new Button(ts.getTranslatedString("module.general.messages.save"), new Icon(VaadinIcon.DOWNLOAD), evt -> {
            saveUserData();
        });
        Button btnDeleteUser = new Button(ts.getTranslatedString("module.userman.actions.delete-user.name"), new Icon(VaadinIcon.TRASH), evt -> {
            this.deleteUserVisualAction.getVisualComponent(
                    new ModuleActionParameterSet(new ModuleActionParameter("user", selectedUser))).open();
        });
        HorizontalLayout lytUserActions = new HorizontalLayout(btnSaveUserData, btnRelateToGroup, btnRemoveFromGroup, btnDeleteUser);
        
        buildUserData();
        FormLayout lytUserData = new FormLayout(txtUserName, txtPassword, txtFirstName, txtLastName, txtEmail, cmbType, chkEnabled);
        lytUserData.setWidth("550px");
        
        lblUserTitle = new Label();
        lblUserTitle.setClassName("dialog-title");
        
        VerticalLayout lytUserInfo = new VerticalLayout(lblUserTitle, lytUserActions, lytUserData); 
               
        lytPrivileges = new VerticalLayout();
        lytContent = new HorizontalLayout(lytUserInfo, lytPrivileges);
        lytContent.setSizeFull();
        lytContent.setVisible(false);
        HorizontalLayout lytSecContent = new HorizontalLayout();
        lytSecContent.addAndExpand(lytContent);
        lytSecContent.setPadding(false);
        lytSecContent.setMargin(false);
        lytSecContent.setSpacing(false);
        
        Scroller scroller = new Scroller(lytPrimaryContent);
        scroller.setSizeUndefined();
        
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25); 
        splitLayout.addToPrimary(scroller);
        splitLayout.addToSecondary(lytSecContent);      
        add(splitLayout);
    }
    
    private void buildUserData() {
        txtUserName = new TextField(ts.getTranslatedString("module.userman.user-name"));
        txtPassword = new PasswordField(ts.getTranslatedString("module.login.ui.password"));
        txtPassword.setPlaceholder("********");
        txtFirstName = new TextField(ts.getTranslatedString("module.userman.first-name"));
        txtLastName = new TextField(ts.getTranslatedString("module.userman.last-name"));
        txtEmail = new TextField(ts.getTranslatedString("module.userman.email"));
        chkEnabled = new Checkbox(ts.getTranslatedString("module.userman.enabled"));
        chkEnabled.getElement().getStyle().set("margin-top", "10px");
        cmbType = new ComboBox(ts.getTranslatedString("module.userman.user-type"));
        cmbType.setItems(Arrays.asList(UserProfile.USER_TYPE_GUI, UserProfile.USER_TYPE_WEB_SERVICE, UserProfile.USER_TYPE_SOUTHBOUND));
        cmbType.setItemLabelGenerator(item -> {
                switch(item) {
                    case UserProfile.USER_TYPE_GUI: 
                        return "GUI User"; // I18N
                    case UserProfile.USER_TYPE_WEB_SERVICE: 
                        return "Web Service Interface User"; // I18N
                    default:
                        return "Southbound Interface User"; // I18N
                }
        });
    }

    private void buildPrivilegesGrid() {
        lstCurrentPrivileges = new ArrayList<>(selectedUser.getPrivileges());
        Grid<AbstractModule> grdPrivileges = new Grid<>();
        grdPrivileges.setItems(moduleRegistry.getModules().values());
        grdPrivileges.addColumn(item -> item.getName()).setHeader(ts.getTranslatedString("module.userman.module"));
        
        List<Checkbox> lstCheckRead = new ArrayList<>();
        List<Checkbox> lstCheckReadWrite = new ArrayList<>();
        
        Checkbox chkAllRead = new Checkbox(ts.getTranslatedString("module.userman.all-read"));
        Checkbox chkAllReadWrite = new Checkbox(ts.getTranslatedString("module.userman.all-write"));
        
        Column<AbstractModule> col = grdPrivileges.addComponentColumn(item -> {
            Checkbox chkRead = new Checkbox(ts.getTranslatedString("module.userman.read"));
            Checkbox chkReadWrite = new Checkbox(ts.getTranslatedString("module.userman.write"));
            lstCheckRead.add(chkRead);
            lstCheckReadWrite.add(chkReadWrite);
            Optional<Privilege> op = lstCurrentPrivileges.stream().
                    filter(pr -> pr.getFeatureToken().equals(item.getId())).findAny(); 
            
            if (op.isPresent()) {             
                chkRead.setValue(op.get().getAccessLevel() == Privilege.ACCESS_LEVEL_READ ||
                        op.get().getAccessLevel() == Privilege.ACCESS_LEVEL_READ_WRITE);
                if (op.get().getAccessLevel() == Privilege.ACCESS_LEVEL_READ_WRITE)
                    chkRead.setEnabled(false);
            }
           
            chkRead.addValueChangeListener(listener -> {
                if (chkReadWrite.getValue()) {
                    return;
                }
                Optional<Privilege> priOptional = lstCurrentPrivileges.stream().
                        filter(pr -> pr.getFeatureToken().equals(item.getId())).findAny();
                if (listener.getValue()) {
                    if (priOptional.isPresent())
                        priOptional.get().setAccessLevel(Privilege.ACCESS_LEVEL_READ);
                    else
                        lstCurrentPrivileges.add(new Privilege(item.getId(), (Privilege.ACCESS_LEVEL_READ)));
                } else if (priOptional.isPresent())
                    lstCurrentPrivileges.remove(priOptional.get());

                if (savePrivilege)
                    saveUserData();
            });
            
            if (op.isPresent() && op.get().getAccessLevel() == Privilege.ACCESS_LEVEL_READ_WRITE) 
                chkReadWrite.setValue(true);
            
            chkReadWrite.addValueChangeListener(listener -> {
                Optional<Privilege> priOptional = lstCurrentPrivileges.stream().
                        filter(pr -> pr.getFeatureToken().equals(item)).findAny();
                if (listener.getValue()) {
                    chkRead.setValue(true);
                    chkRead.setEnabled(false);
                    if (priOptional.isPresent())
                        priOptional.get().setAccessLevel(Privilege.ACCESS_LEVEL_READ_WRITE);
                    else
                        lstCurrentPrivileges.add(new Privilege(item.getId(), (Privilege.ACCESS_LEVEL_READ_WRITE)));
                } else {
                    chkRead.setValue(false);
                    chkRead.setEnabled(true);
                    if (priOptional.isPresent())
                        lstCurrentPrivileges.remove(priOptional.get());
                }
                
                if (savePrivilege)
                    saveUserData();
            });
            return new HorizontalLayout(chkRead, chkReadWrite);
        });

        chkAllRead.setValue(moduleRegistry.getModules().size() == lstCurrentPrivileges.size());
        chkAllRead.addValueChangeListener(listener -> {
            savePrivilege = false;
            lstCheckRead.forEach(item -> item.setValue(listener.getValue()));
            saveUserData();
            savePrivilege = true;
        });
        
        chkAllReadWrite.setValue(moduleRegistry.getModules().size() == lstCurrentPrivileges.size());
        chkAllReadWrite.addValueChangeListener(listener -> {
            savePrivilege = false;
            lstCheckReadWrite.forEach(item -> item.setValue(listener.getValue()));
            saveUserData();
            savePrivilege = true;
        });
        
        HorizontalLayout lytCheckAll = new HorizontalLayout(chkAllReadWrite);
        lytCheckAll.setWidthFull();
        lytCheckAll.setJustifyContentMode(JustifyContentMode.END);
        col.setHeader(lytCheckAll);

        lytPrivileges.removeAll();
        lytPrivileges.add(grdPrivileges);
    }

    private void loadGroups() {
        List<GroupProfile> grps = aem.getGroups();
        HierarchicalDataProvider dataProvider = buildHierarchicalDataProvider(grps);
        treeUsers.setDataProvider(dataProvider);
    }

    private void updateUserContent() {
        txtUserName.setValue(selectedUser.getUserName() == null ? "": selectedUser.getUserName());
        txtFirstName.setValue(selectedUser.getFirstName() == null ? "": selectedUser.getFirstName());
        txtLastName.setValue(selectedUser.getLastName() == null ? "": selectedUser.getLastName());
        txtEmail.setValue(selectedUser.getEmail() == null ? "": selectedUser.getEmail());
        txtPassword.setValue("");
        chkEnabled.setValue(selectedUser.isEnabled());
        cmbType.setValue(selectedUser.getType());
    }

    private void openRelateToGroupDlg() {
         ListBox<GroupProfile> lstGrp = new ListBox();
         List<GroupProfile> grps = aem.getGroups();
         lstGrp.setItems(grps);
         lstGrp.setRenderer(new ComponentRenderer<>(item -> {
                    return new Label(item.getName());
         }));
         ConfirmDialog dlgRelate = new ConfirmDialog(ts, ts.getTranslatedString("module.userman.relate-to-group"), ts.getTranslatedString("module.general.messages.ok"));
         dlgRelate.setContent(lstGrp);
         dlgRelate.getBtnConfirm().addClickListener(evt -> {    
             if (lstGrp.getValue() != null) {
                 try {
                     aem.addUserToGroup(selectedUser.getId(), lstGrp.getValue().getId());
                     treeUsers.getDataProvider().refreshAll();
                     dlgRelate.close();
                     new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.userman.user-related"), 
                            AbstractNotification.NotificationType.INFO, ts).open();  
                 } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                     new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                             AbstractNotification.NotificationType.ERROR, ts).open();
                 }
             } else 
                 new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.userman.select-group"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();  
         });
         dlgRelate.open();
    }
    
    private void openReleaseFromGroupDlg() {
        if (selectedUser != null)
            try {
                Label lblInfo = new Label(ts.getTranslatedString("module.userman.info-remove-from-group"));
                ListBox<GroupProfileLight> lstGrp = new ListBox();
                List<GroupProfileLight> grps = aem.getGroupsForUser(selectedUser.getId());
                lstGrp.setItems(grps);             
                lstGrp.setWidthFull();
                lstGrp.setRenderer(new ComponentRenderer<>(item -> {
                    return new Label(item.getName());
                }));
                ConfirmDialog dlgRelate = new ConfirmDialog(ts, ts.getTranslatedString("module.userman.remove-from-group"), ts.getTranslatedString("module.general.messages.ok"));
                VerticalLayout lytContentDlg = new VerticalLayout(lblInfo, lstGrp);
                lytContentDlg.setSpacing(false);
                lytContentDlg.setPadding(false);
                dlgRelate.setContent(lytContentDlg);
                dlgRelate.getBtnConfirm().addClickListener(evt -> {
                    if (lstGrp.getValue() != null) {
                        try {
                            aem.removeUserFromGroup(selectedUser.getId(), lstGrp.getValue().getId());
                            treeUsers.getDataProvider().refreshAll();
                            dlgRelate.close();
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.userman.user-removed"),
                                    AbstractNotification.NotificationType.INFO, ts).open();
                        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    } else
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.userman.select-group"),
                                AbstractNotification.NotificationType.WARNING, ts).open();
                });
                dlgRelate.open();
            } catch (ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
    }

    private void saveUserData() {
        if (selectedUser != null)
            try {
                if (!txtUserName.getValue().equals(selectedUser.getUserName())
                        || !txtFirstName.getValue().equals(selectedUser.getFirstName())
                        || !txtLastName.getValue().equals(selectedUser.getLastName()))
                    treeUsers.getDataProvider().refreshAll();
                
                aem.setUserProperties(selectedUser.getId(), selectedUser.getUserName().equals(txtUserName.getValue()) ? null : txtUserName.getValue(),
                        (txtPassword.getValue() == null || txtPassword.getValue().isEmpty()) ? null : txtPassword.getValue(), txtFirstName.getValue(),
                        txtLastName.getValue(), chkEnabled.getValue() ? 1 : -1, cmbType.getValue(), txtEmail.getValue());
                lblUserTitle.setText(String.format("%s %s (%s)", txtFirstName.getValue(), txtLastName.getValue(), txtUserName.getValue()));
                for (Privilege pr : selectedUser.getPrivileges()) {
                    if (!lstCurrentPrivileges.contains(pr))
                        aem.removePrivilegeFromUser(selectedUser.getId(), pr.getFeatureToken());
                }
                for (Privilege pr : lstCurrentPrivileges) 
                    aem.setPrivilegeToUser(selectedUser.getId(), pr.getFeatureToken(), pr.getAccessLevel());
                
                selectedUser.setPrivileges(lstCurrentPrivileges);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.userman.user-info-updated"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
    }

    @Override
    public Optional<String> getId() {
        return super.getId(); //To change body of generated methods, choose Tools | Templates.
    }
}