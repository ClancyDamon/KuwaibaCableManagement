/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.web.ui;

import com.neotropic.flow.component.mxgraph.MxGraphApi;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Login form.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("")
@StyleSheet("css/main.css")
@CssImport(value="./styles/custom-spliter.css", themeFor="vaadin-split-layout")
@CssImport(value="./styles/compact-grid.css", themeFor="vaadin-grid")
@CssImport(value="./styles/icon-button.css", themeFor="vaadin-button")
@CssImport(value="./styles/vaadin-notification-card.css", themeFor = "vaadin-notification-card")
@CssImport(value = "./styles/tepman-subitem-menubar-buttons.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/main-menu-bar-buttons.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/vaadin-accordion-panel.css", themeFor = "vaadin-accordion-panel")
@CssImport(value = "./styles/vaadin-menu-bar-buttons.css", themeFor = "vaadin-menu-bar")
@CssImport(value = "./styles/vaadin-details.css", themeFor = "vaadin-details")
@CssImport(value = "./styles/vaadin-text-field.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-grid.css", themeFor = "vaadin-grid")
public class LoginUI extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    /**
     * User name text field.
     */
    private TextField txtUsername;
    /**
     * Password text field.
     */
    private PasswordField txtPassword;
    /**
     * Button that uses the data filled in by the user to create a session.
     */
    private Button btnLogin;
    /**
     * Reference to the Application Entity Manager to authenticate the user.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the internationalization service.
     */
    @Autowired
    private TranslationService ts;
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        HorizontalLayout lytTopFiller = new HorizontalLayout();
        lytTopFiller.setSizeFull();
        add(lytTopFiller); // Top filler
        
        //The URLs are hard-coded so they're not easily removed
        Image imgProjectLogo = new Image("img/logo_project.svg", ts.getTranslatedString("module.login.ui.project-logo-caption"));
        imgProjectLogo.setWidth("150px");
        Anchor lnkProjectLogo = new Anchor("https://www.kuwaiba.org", imgProjectLogo);
        lnkProjectLogo.setTarget("_blank");
        
        HorizontalLayout lytKuwaibaLogo = new HorizontalLayout(lnkProjectLogo);
        lytKuwaibaLogo.setPadding(false);
        lytKuwaibaLogo.setSpacing(false);
        lytKuwaibaLogo.setWidthFull();
        lytKuwaibaLogo.setDefaultVerticalComponentAlignment(Alignment.END);
        lytKuwaibaLogo.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        lytKuwaibaLogo.setWidthFull();
        
        add(lytKuwaibaLogo);

        HorizontalLayout lytRightFiller = new HorizontalLayout();
        lytRightFiller.setSizeFull();
        HorizontalLayout lytLeftFiller = new HorizontalLayout();
        lytLeftFiller.setSizeFull();
        HorizontalLayout lytMidContent = new HorizontalLayout(lytLeftFiller /* Left filler */, 
                buildLoginForm(), /* Content */
                lytRightFiller /* Right filler */);
        lytMidContent.setSizeFull();
        add(lytMidContent);
        HorizontalLayout lytFooterContent = new HorizontalLayout(new HorizontalLayout() /* Left filler */, 
                buildLoginFooter(), /* Footer content */ 
                new HorizontalLayout() /* Right filler */);
        lytFooterContent.setSizeFull();
        add(lytFooterContent);
        
        MxGraphApi mxGraphApi = new MxGraphApi();
        add(mxGraphApi);
    }
     
    /**
     * Used to create the log in form
     * @return a form layout holding the needed fields to authenticate
     */
    private VerticalLayout buildLoginForm() {
        setClassName("login-background");
        
        txtUsername = new TextField();
        txtUsername.setPlaceholder(ts.getTranslatedString("module.login.ui.user"));
        txtUsername.setWidthFull();
        txtUsername.focus();
        txtUsername.setThemeName("login-field");
        
        txtPassword = new PasswordField();
        txtPassword.setPlaceholder(ts.getTranslatedString("module.login.ui.password"));
        txtPassword.setWidthFull();
        txtPassword.setThemeName("login-field");
        
        Icon loginIcon = new Icon(VaadinIcon.ROCKET);
        loginIcon.setColor("#ffffff");
        btnLogin = new Button(ts.getTranslatedString("module.login.ui.login"), loginIcon);
        btnLogin.setClassName("nav-button");
        btnLogin.setWidthFull();
        
        // This is used to press Enter instead of clicking the button
        btnLogin.addClickShortcut(Key.ENTER);
        
        btnLogin.addClickListener(event ->{
            getUI().ifPresent(ui -> { 
                try {
                    // Create the session object
                    Session aSession = aem.createSession(txtUsername.getValue(), //NOI18N
                            txtPassword.getValue(), Session.TYPE_WEB);
                    if (aSession.getUser().getPrivileges().isEmpty()) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), 
                            ts.getTranslatedString("module.login.no-privileges-set"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                        return;
                    }
                    // Send the session object to browser's session
                    ui.getSession().setAttribute(Session.class, aSession);
                    // Navigate to Welcome page
                    ui.navigate(HomeUI.class);
                } catch (InventoryException ex) { // User not found is no longer caught. Generic exception for any other unexpected situation
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                            ts.getTranslatedString("module.login.ui.cant-login"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                } catch (Exception ex) {
                    Logger.getLogger(LoginUI.class.getName()).log(Level.SEVERE, ex.getMessage());
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                            ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
             });
        });
        
        VerticalLayout lytForm = new VerticalLayout();
        
        HorizontalLayout lytUser = new HorizontalLayout(txtUsername);
        lytUser.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytUser.setWidthFull();
        
        HorizontalLayout lytPassword = new HorizontalLayout(txtPassword);
        lytPassword.setWidthFull();
        lytPassword.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        lytForm.add(txtUsername, txtPassword, btnLogin);
        lytForm.setWidth("700px");
        return lytForm;
     }
    
    private VerticalLayout buildLoginFooter() {
        //The URLs are hard-coded so they're not easily removed
        Image imgCorporateLogo = new Image("img/logo_neotropic.svg", ts.getTranslatedString("module.login.ui.neotropic-logo-caption"));
        imgCorporateLogo.setWidth("100px");
        Anchor lnkProjectLogo = new Anchor("https://www.neotropic.co", imgCorporateLogo);
        lnkProjectLogo.setTarget("_blank");
        
        Label lblPowered = new Label (ts.getTranslatedString("module.login.ui.powered-by"));
        lblPowered.setClassName("login-text");
        
        Label lblCopyright = new Label(ts.getTranslatedString("module.login.ui.copyright"));
        lblCopyright.setClassName("login-text");
        
        Label lblVersion = new Label(ts.getTranslatedString("module.login.ui.version"));
        lblVersion.setClassName("login-text");
        
        VerticalLayout lytFooter = new VerticalLayout(lblPowered, lnkProjectLogo, lblCopyright, lblVersion);
        lytFooter.setPadding(false);
        lytFooter.setSpacing(false);
        lytFooter.setSizeFull();
        lytFooter.setAlignItems(Alignment.CENTER);
        return lytFooter;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (UI.getCurrent().getSession().getAttribute(Session.class) != null) // If there is an active session, redirect to the home page, else, show the login form
            event.forwardTo(HomeUI.class);
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.login.ui.title");
    }
}
