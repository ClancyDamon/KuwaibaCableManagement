/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.util.visual.widgets;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * A small embeddable component that can be inserted into an AbstractDashboard. A DashboardWidget has two "faces": 
 * A cover that shows a summary or simply a title, and content, with the actual information to be shown. This can be seen as a 
 * Tile in a MS Windows Metro interface in most of the cases.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractDashboardWidget extends VerticalLayout {
    /**
     * The active content to be displayed (cover or content)
     */
    protected ActiveContent activeContent;
    /**
     * The component with the cover information
     */
    protected Div coverComponent;
    /**
     * The component with the detailed information (actual content)
     */
    protected Component contentComponent;
    /**
     * Dashboard widget title
     */
    protected String title;
    /**
     * Reference to the Metadata Entity Manager.
     */
    protected MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    protected ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    protected BusinessEntityManager bem;
    /**
     * Reference to the translation service.
     */
    protected TranslationService ts;

    /**
     * Use this constructor only for those widgets that won't be accessing the database.
     */
    public AbstractDashboardWidget() { }
    
    public AbstractDashboardWidget(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, TranslationService ts) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.ts = ts;
    }

    public ActiveContent getActiveContent() {
        return activeContent;
    }

    public void setActiveContent(ActiveContent activeContent) {
        this.activeContent = activeContent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Div getCoverComponent() {
        return coverComponent;
    }

    public void setCoverComponent(Div coverComponent) {
        this.coverComponent = coverComponent;
    }

    public Component getContentComponent() {
        if (contentComponent == null)
            createContent();
        return contentComponent;
    }

    public void setContentComponent(Component contentComponent) {
        this.contentComponent = contentComponent;
    }
 
    /**
     * Flips the current displayed component. That is, instead of the cover component, the component widget will be displayed
     */
    public void flip() {
        if (this.activeContent == ActiveContent.CONTENT_COVER) {
            replace(coverComponent, contentComponent);
            activeContent = ActiveContent.CONTENT_CONTENT;
        } else {
            replace(contentComponent, coverComponent);
            activeContent = ActiveContent.CONTENT_COVER;
        }
    }
    
    /**
     * Displays the contents of the content widget in a separate modal window.
     * @return The dialog.
     */
    public Dialog launchAsNewWindow() {
        Dialog wdwContent = new Dialog();
        if (contentComponent != null)
            wdwContent.add(contentComponent);
        else
            wdwContent.add(new Label(ts.getTranslatedString("")));
        
        return wdwContent;
    }
    
    /**
     * Creates the cover component. Note that implementors must set the coverComponent attribute and manage the respective events
     * The default implementation creates a colored rectangle displaying the title of the widget without any style. For simple widgets it's recommended to use 
     * this implementation (that is, call super.createCover()) and set a style afterwards. 
     */
    public void createCover() {
        Div divCover = new Div();
        Label lblTitle = new Label(title);
        lblTitle.addClassName("widgets-standard-cover-content");
        divCover.addClassName("widgets-standard-cover");
        divCover.add(lblTitle);
        
        divCover.addClickListener(event -> {
            this.createContent();
            this.launchAsNewWindow().open();
        });
        
        removeAll();
        this.coverComponent = divCover;
        add(this.coverComponent);
    }
    
    public abstract void createContent();
    
    public enum ActiveContent {
        CONTENT_COVER,
        CONTENT_CONTENT
    }
}
