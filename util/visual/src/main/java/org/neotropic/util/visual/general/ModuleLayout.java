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

package org.neotropic.util.visual.general;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import java.util.Optional;
import java.util.function.Supplier;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.PopupAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.util.visual.menu.MenuButton;
import org.neotropic.util.visual.menu.MenuLayout;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * The super class of all flex layouts used in every module of the application.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@StyleSheet("css/main.css")
@StyleSheet("css/main-layout.css")
public abstract class ModuleLayout extends FlexLayout implements RouterLayout {
    /**
     * Header component.
     */
    private final HorizontalLayout lytHeader;
    /**
     * The left side menu
     */
    private final MenuLayout lytMenu;
    /**
     * Content to be injected.
     */
    private final VerticalLayout lytContent;
    /**
     * Footer content.
     */
    //private VerticalLayout lytFooter;
    /**
     * Reference to the translation service.
     */
    @Autowired
    protected TranslationService ts;
    /**
     * Reference to the Application Entity Manager so we can check if the Vaadin session is still valid.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the module registry.
     */
    @Autowired
    protected ModuleRegistry moduleRegistry;
    
    public ModuleLayout() {
        setId("main-layout");
        setSizeFull();
        this.lytHeader = new HorizontalLayout();
        this.lytContent = new VerticalLayout();
        this.lytMenu = new MenuLayout();

        this.lytMenu.setId("sidebar_menu");        
        this.lytHeader.setId("main-layout-header");
        this.lytContent.setId("main-layout-content");
        
        this.lytHeader.setWidthFull();
        
        add(this.lytHeader);
        add(this.lytContent);
    }
    
    public void buildHeader(Session session){
        lytHeader.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytHeader.setHeight("40px");
        lytHeader.setSpacing(false);
        lytHeader.setMargin(false);
        lytHeader.setPadding(false);
        
        Image imgCorporateLogo = new Image("img/company_logo_micro.svg", ts.getTranslatedString("module.home.menu.home"));
        imgCorporateLogo.addClassName("img-logo");
        imgCorporateLogo.getElement().setAttribute("style", "cursor: pointer");
        imgCorporateLogo.addClickListener((ev) -> UI.getCurrent().navigate("home"));
        lytHeader.add(imgCorporateLogo);
        
        VerticalLayout lytMenuWrapper = new VerticalLayout();
        lytMenuWrapper.setSpacing(false);
        lytMenuWrapper.setMargin(false);
        lytMenuWrapper.setPadding(false);
        buildSideMenu(UI.getCurrent().getSession().getAttribute(Session.class));
        lytMenuWrapper.add(lytMenu);
        lytMenuWrapper.setHorizontalComponentAlignment(Alignment.CENTER, lytMenu);
        lytHeader.add(lytMenuWrapper);
        
        Icon icnUser = new Icon(VaadinIcon.USER);
        icnUser.setSize("16px");

        Label lblUserInfo = new Label();
        if (session.getUser() != null)
            lblUserInfo.setText(session.getUser().toString());
        lblUserInfo.getStyle().set("font-size", "smaller");
        lblUserInfo.getStyle().set("white-space", "nowrap");
        
        Label lblLogout = new Label(ts.getTranslatedString("module.home.menu.logout"));
        lblLogout.getElement().setAttribute("style", "cursor: pointer");
        lblLogout.getStyle().set("font-size", "smaller");
        
        FlexLayout lytLogout = new FlexLayout(lblLogout);
        lytLogout.addClickListener(e -> {
            getUI().ifPresent( ui -> { 
                try {
                    aem.closeSession(ui.getSession().getAttribute(Session.class).getToken());
                    ui.getSession().setAttribute(Session.class, null); // Closing the session doesn't -oddly- clean up the session attributes
                    ui.getSession().close();
                    ui.navigate("");
                } catch (NotAuthorizedException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
        });
        
        FlexLayout lytUser = new FlexLayout(lblUserInfo, lytLogout);
        lytUser.setFlexDirection(FlexDirection.COLUMN);
        lytUser.setJustifyContentMode(JustifyContentMode.CENTER);
        
        HorizontalLayout lytX = new HorizontalLayout(icnUser, lytUser);
        lytX.setDefaultVerticalComponentAlignment(Alignment.CENTER);
                
        imgCorporateLogo.getElement().getStyle().set("margin-right", "auto");
        lytHeader.add(lytX);
        lytHeader.addClassName("compact-header");
    }
    
    /**
     * Builds a side menu with the options organized in categories (each category a top-level menu).
     * @param session The session object. This object is used to validate the sections available according to the user privileges.
     */
    public void buildSideMenu(Session session) {
        MenuButton<AbstractModule> btnAdministration = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.administration"), new Icon(VaadinIcon.CHART_3D));
        btnAdministration.setThemeName("main-menubar-button");
        lytMenu.add(btnAdministration);
        
        MenuButton<AbstractModule> btnNavigation = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.navigation"), new Icon(VaadinIcon.LOCATION_ARROW_CIRCLE));
        btnNavigation.setThemeName("main-menubar-button");
        lytMenu.add(btnNavigation);
        
        MenuButton<AbstractModule> btnPhysical = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.physical"), new Icon(VaadinIcon.GRID_BIG));
        btnPhysical.setThemeName("main-menubar-button");
        lytMenu.add(btnPhysical);
        
        MenuButton<AbstractModule> btnLogical = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.logical"), new Icon(VaadinIcon.GRID_BIG_O));
        btnLogical.setThemeName("main-menubar-button");
        lytMenu.add(btnLogical);
        
        MenuButton<AbstractModule> btnBusiness = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.business"), new Icon(VaadinIcon.CUBES));
        btnBusiness.setThemeName("main-menubar-button");
        lytMenu.add(btnBusiness);
        
        MenuButton<AbstractModule> btnPlanning = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.planning"), new Icon(VaadinIcon.MAGIC));
        btnPlanning.setThemeName("main-menubar-button");
        lytMenu.add(btnPlanning);
        
        MenuButton<AbstractModule> btnOther = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.other"), new Icon(VaadinIcon.FORM));
        btnOther.setThemeName("main-menubar-button");
        lytMenu.add(btnOther);
        
        MenuButton<AbstractModule> btnSettings = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.settings"), new Icon(VaadinIcon.COG));
        btnSettings.setThemeName("main-menubar-button");
        lytMenu.add(btnSettings);
        
        MenuButton btnAbout = new MenuButton(lytMenu, ts.getTranslatedString("module.home.menu.about"), "about",
                new Icon(VaadinIcon.INFO_CIRCLE_O));
        btnAbout.setThemeName("main-menubar-button");
        lytMenu.add(btnAbout);
        
        UserProfile user = session.getUser();
        this.moduleRegistry.getModules().values().stream().forEach(aModule -> {
            Optional<Privilege> op = user.getPrivileges().stream().
                    filter(pr -> pr.getFeatureToken().equals(aModule.getId())).findAny(); 
            if ((op.isPresent() && op.get().getAccessLevel() == Privilege.ACCESS_LEVEL_READ_WRITE)) {

                if (aModule.isEnabled()) {
                    Runnable runnableSelectedItem = () -> {
                        if (aModule instanceof PopupAction)
                            ((PopupAction) aModule).open();
                        else
                            UI.getCurrent().navigate(aModule.getId());
                    };
                    Supplier<String> supplierHref = () -> aModule instanceof PopupAction ? null : aModule.getId();

                    switch (aModule.getCategory()) {
                        case AbstractModule.CATEGORY_NAVIGATION:
                            btnNavigation.addMenuItem(aModule, aModule.getName(), supplierHref.get(), runnableSelectedItem);
                            break;
                        case AbstractModule.CATEGORY_PHYSICAL:
                            btnPhysical.addMenuItem(aModule, aModule.getName(), supplierHref.get(), runnableSelectedItem);
                            break;
                        case AbstractModule.CATEGORY_LOGICAL:
                            btnLogical.addMenuItem(aModule, aModule.getName(), supplierHref.get(), runnableSelectedItem);
                            break;
                        case AbstractModule.CATEGORY_BUSINESS:
                            btnBusiness.addMenuItem(aModule, aModule.getName(), supplierHref.get(), runnableSelectedItem);
                            break;
                        case AbstractModule.CATEGORY_PLANNING:
                            btnPlanning.addMenuItem(aModule, aModule.getName(), supplierHref.get(), runnableSelectedItem);
                            break;
                        case AbstractModule.CATEGORY_ADMINISTRATION:
                            btnAdministration.addMenuItem(aModule, aModule.getName(), supplierHref.get(), runnableSelectedItem);
                            break;
                        case AbstractModule.CATEGORY_SETTINGS:
                            btnSettings.addMenuItem(aModule, aModule.getName(), supplierHref.get(), runnableSelectedItem);
                            break;
                        case AbstractModule.CATEGORY_OTHER:
                        default:
                            btnOther.addMenuItem(aModule, aModule.getName(), supplierHref.get(), runnableSelectedItem);
                    }
                }
            } 
        });
    }
    
    /**
     * Builds a header where the items are not grouped in menus.
     * @param session The current session.
     * @return A component, most likely an horizontal layout with the items arranged in a single line.
     */
    public Component buildExtendedHeader(Session session) { return new HorizontalLayout(); }
    
    @Override
    public void onAttach(AttachEvent ev) {
        if (UI.getCurrent().getSession().getAttribute(Session.class) == null)
            UI.getCurrent().navigate("");
        else {
            this.lytHeader.removeAll();
            buildHeader(UI.getCurrent().getSession().getAttribute(Session.class));
        }
    }
    
    @Override
    public void showRouterLayoutContent(HasElement content) {
        this.lytContent.removeAll();
        if (UI.getCurrent().getSession().getAttribute(Session.class) != null) {
            if (content != null) {
                if (content instanceof AbstractUI) {
                    ((AbstractUI)content).initContent();
                    this.lytContent.add((Component)content);
                } else                
                    this.lytContent.add(new Label(ts.getTranslatedString("module.home.ui.wrong-ui")));
            }
        }
    }
}
