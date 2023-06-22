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

package org.neotropic.kuwaiba.core.sessman;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.sessman.actions.TerminateSessionVisualAction;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Session manager UI entry point.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route (value = "sessman", layout = SessionManagerLayout.class)
public class SessionManagerUI extends VerticalLayout implements HasDynamicTitle, ActionCompletedListener, AbstractUI {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Terminates a user session.
     */
    @Autowired
    private TerminateSessionVisualAction terminateSessionVisualAction;
    /**
     * A table with the active sessions. 
     */
    private Grid<Session> tblSessions;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @Override
    public void onDetach(DetachEvent ev) {
        terminateSessionVisualAction.unregisterListener(this);
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.sessman.title");
    }

    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.sessman.ui.session-management"), 
                    ev.getMessage(), AbstractNotification.NotificationType.INFO, ts).open();
            tblSessions.setItems(aem.getSessions().values());
        } else
            new SimpleNotification(ts.getTranslatedString("module.sessman.ui.session-management"), 
                    ev.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
    }

    @Override
    public void initContent() {
        setSizeFull();
        tblSessions = new Grid<>();
        tblSessions.addColumn(Session::getUser).setHeader(ts.getTranslatedString("module.sessman.ui.user"));
        tblSessions.addColumn(aSession -> aSession.getUser().getEmail()).setHeader(ts.getTranslatedString("module.sessman.ui.email"));
        tblSessions.addColumn(aSession -> { 
                        switch (aSession.getSessionType()) {
                            case Session.TYPE_DESKTOP:
                                return ts.getTranslatedString("module.sessman.ui.session-type-desktop");
                            case Session.TYPE_MOBILE:
                                return ts.getTranslatedString("module.sessman.ui.session-type-mobile");
                            case Session.TYPE_WEB:
                                return ts.getTranslatedString("module.sessman.ui.session-type-web");
                            case Session.TYPE_WS:
                                return ts.getTranslatedString("module.sessman.ui.session-type-ws");
                            default:
                                return ts.getTranslatedString("module.sessman.ui.session-type-other");
                        }
                    }).setHeader(ts.getTranslatedString("module.sessman.ui.session-type"));
        tblSessions.addColumn(Session::getLoginTime).setHeader(ts.getTranslatedString("module.sessman.ui.login-time"));
        tblSessions.addColumn(new ComponentRenderer<>( aSession -> { 
                Button btnTerminate = new Button(ts.getTranslatedString("module.sessman.ui.terminate-session"));
                btnTerminate.addClickListener((clickEvent) -> {
                    getUI().ifPresent(ui -> {
                        if (ui.getSession().getAttribute(Session.class).equals(aSession))
                            new SimpleNotification(terminateSessionVisualAction.getModuleAction().getDisplayName(), 
                                    ts.getTranslatedString("module.sessman.actions.terminate-session.cant-terminate-own-session"), 
                                    AbstractNotification.NotificationType.WARNING, ts).open();
                        else
                            terminateSessionVisualAction.getVisualComponent(new ModuleActionParameterSet(
                                            new ModuleActionParameter("session", aSession))).open();
                    });
                });
                return btnTerminate;
            })).setHeader(ts.getTranslatedString("module.sessman.ui.actions"));
        tblSessions.setItems(aem.getSessions().values());
        tblSessions.setSizeFull();
        add(tblSessions);
        setSizeFull();
        
        terminateSessionVisualAction.registerActionCompletedLister(this);
    }
}