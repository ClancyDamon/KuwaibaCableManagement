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
package org.neotropic.util.visual.notifications;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Default implementation of a notification component.
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleNotification extends AbstractNotification {

    public SimpleNotification(String title, String text, NotificationType type, TranslationService ts) {
        super(title, text, type, ts);
    }

    @Override
    public void open() {
        Notification notification = new Notification();
        
        Icon icn;
        
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Label lblTitle = new Label(this.title);
        lblTitle.setClassName("notification-title");
        Label lblText = new Label(this.text);
        
        VerticalLayout mainLayout = new VerticalLayout(headerLayout, lblText);
        mainLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        mainLayout.addClickListener(e -> notification.close());
        mainLayout.setPadding(false);
        mainLayout.setMargin(false);
        mainLayout.setMinWidth("300px");
        mainLayout.setMinHeight("50px");        
        
        Button btnCopyToClipboard = new Button(ts.getTranslatedString("module.general.labels.copy-to-clipboard"));
        btnCopyToClipboard.setHeight("18px");

        btnCopyToClipboard.setClassName("notification-buttons");
        btnCopyToClipboard.addAttachListener(event -> copyToClipboard(event, this.text));        

        notification.setThemeName("notification");
        notification.setDuration(4000);
        notification.setPosition(Notification.Position.TOP_END);
        
        switch (type) {
            default:
            case INFO:
                icn = new Icon(VaadinIcon.CHECK_CIRCLE_O);
                icn.setClassName("notification-info");
                icn.setSize("16px");
                
                lblTitle.setClassName("notification-info");
                headerLayout.add(icn, lblTitle);
                
                notification.addThemeName("notification-info");
                notification.add(mainLayout);
                notification.open();
                break;
            case WARNING:
                icn = new Icon(VaadinIcon.EXCLAMATION_CIRCLE_O);
                icn.setClassName("notification-warn");
                icn.setSize("16px");
                
                lblTitle.setClassName("notification-warn");
                headerLayout.add(icn, lblTitle);

                notification.addThemeName("notification-warn");
                notification.add(mainLayout);
                notification.open();
                break;
            case ERROR:
                Notification errorNotification = new Notification();
                errorNotification.setPosition(Notification.Position.MIDDLE);
                notification.setThemeName("notification");
                                
                icn = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
                icn.setClassName("notification-error");
                icn.setSize("16px");
                
                lblTitle.setClassName("notification-error");
                headerLayout.add(icn, lblTitle);
                
                Span separator = new Span("|");
                separator.setClassName("notification-error");
                
                Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"));
                btnClose.addClickListener(e -> errorNotification.close());
                btnClose.setClassName("notification-buttons");
                btnClose.setHeight("18px");
                
                HorizontalLayout lytErrorActions = new HorizontalLayout(btnCopyToClipboard, separator, btnClose);
                lytErrorActions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                
                mainLayout.add(lytErrorActions);
                mainLayout.setMinWidth("600px");

                errorNotification.add(mainLayout);
                errorNotification.addThemeName("notification-error");
                errorNotification.open();
        }
    }

    @Override
    public void close() {}

    private void copyToClipboard(AttachEvent event, String errorText) {
        StringBuilder javascript = new StringBuilder();
        // JavaScript code in a String
        javascript.append("    const el = document.createElement('textarea');\n");
        javascript.append("    el.value = $0;\n");
        javascript.append("    el.setAttribute('readonly', '');\n");
        javascript.append("    el.style.position = 'absolute';\n");
        javascript.append("    el.style.left = '-9999px';\n");
        javascript.append("    document.body.appendChild(el);\n");
        javascript.append("    el.select();\n");
        javascript.append("    document.execCommand('copy');\n");
        javascript.append("    document.body.removeChild(el);");
        // call function from script file
        event.getSource().getElement().executeJs(javascript.toString(), errorText);
    }
}