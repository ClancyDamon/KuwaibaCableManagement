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

package org.neotropic.util.visual.wizard;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;


/**
 * A simple, general purpose wizard implementation that can be easily embedded in miniapplications and dashboard widgets
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class Wizard extends VerticalLayout {
    /**
     * Main panel
     */
    private Div lytMainContent;
    /**
     * The next or finish action
     */
    private Button btnAction;
    /**
     * The cancel action
     */
    private Button btnCancel;
    /**
     * Reference to the current step
     */
    private Step currentStep;
    /**
     * The event listeners
     */
    private List<WizardEventListener> listeners;
    
    protected TranslationService ts;
    
    private HorizontalLayout lytButtons;

    public Wizard(TranslationService ts) {
        this.ts = ts;
    }
    
    public void build(Step firstStep) {
        this.setSizeFull();
        this.lytMainContent = new Div();
        lytMainContent.setSizeFull();
        this.btnAction = new Button();
        this.btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"));

        this.btnAction.addClickListener((event) -> {
            try {
                Step nextStep = currentStep.next();
                if (nextStep != null) {
                    currentStep = nextStep;
                    lytMainContent.removeAll();
                    lytMainContent.add(currentStep);

                    if (currentStep.isFinal())
                        btnAction.setText(ts.getTranslatedString("module.general.messages.finish"));
                    else
                        btnAction.setText(ts.getTranslatedString("module.general.messages.next"));

                    fireEvent(new WizardEvent(WizardEvent.TYPE_NEXT_STEP, currentStep.getProperties()));
                } else 
                    fireEvent(new WizardEvent(WizardEvent.TYPE_FINAL_STEP,currentStep.getProperties()));
            } catch (Exception ex) {
                fireEvent(new WizardEvent(WizardEvent.TYPE_STEP_REJECTED, currentStep.getProperties()));
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            }

        });
        btnAction.setClassName("primary-button");
        btnAction.addThemeVariants(ButtonVariant.LUMO_PRIMARY); 
        btnAction.getElement().getStyle().set("margin-left", "auto");

        btnCancel.addClickListener((event) -> {
            fireEvent(new WizardEvent(WizardEvent.TYPE_CANCEL, currentStep.getProperties()));
        });

        this.lytMainContent.add(firstStep);
        this.currentStep = firstStep;
        if (currentStep.isFinal())
            this.btnAction.setText(ts.getTranslatedString("module.general.messages.finish"));
        else
            this.btnAction.setText(ts.getTranslatedString("module.general.messages.next"));

        lytButtons = new HorizontalLayout(btnAction, btnCancel);
        lytButtons.setPadding(false);
        lytButtons.setMargin(false);
        lytButtons.setSpacing(false);
        this.add(lytMainContent, lytButtons);

        this.listeners = new ArrayList<>();
    }

    public Div getLytMainContent() {
        return lytMainContent;
    }

    public void setLytMainContent(Div lytMainContent) {
        this.lytMainContent = lytMainContent;
    }

    public HorizontalLayout getLytButtons() {
        return lytButtons;
    }

    public void setLytButtons(HorizontalLayout lytButtons) {
        this.lytButtons = lytButtons;
    }
    
    

    public void fireEvent(WizardEvent event) {
        listeners.forEach((listener) -> { listener.eventFired(event); });
    }

    public void addEventListener(WizardEventListener listener) {
        listeners.add(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }
    
    public class WizardEvent {
        /**
         * When passing from one step to the next
         */
        public final static int TYPE_NEXT_STEP = 1;
        /**
         * When the wizard ends and the user hits Finish
         */
        public final static int TYPE_FINAL_STEP = 2;
        /**
         * If passing from one step to the next is rejected by the validate() method
         */
        public final static int TYPE_STEP_REJECTED = 3;
        /**
         * The user hit "Cancel"
         */
        public final static int TYPE_CANCEL = 4;
        /**
         * Type of event. See TYPE_XXX for possible values
         */
        private int type;
        /**
         * The information the issuer consider relevant for the listener to receive
         */
        private Properties information;

        public WizardEvent(int type, Properties information) {
            this.type = type;
            this.information = information;
        }

        public int getType() {
            return type;
        }

        public Properties getInformation() {
            return information;
        }
    }
    
    /**
     * Interface to be implemented by all components interested in listening to the events fired by a {@link Wizard}
     */
    public interface WizardEventListener extends EventListener {
        /**
         * What to do when an event is fired
         * @param event The event to be fired
         */
        public void eventFired(WizardEvent event);
    }
    
    /**
     * Interface implemented by all the steps in the wizard
     */
    public abstract class Step extends VerticalLayout {
        /**
         * What to do next. This method also validates if the information requested in the step is correct. Should be called upon the user clicking on th "Next/Finish" button
         * @return Null if it's the final step, or the next step otherwise
         * @throws InvalidArgumentException If the information provided by the user in the current step in missing or inconsistent
         */
        public abstract Step next() throws InvalidArgumentException;
        /**
         * Indicates if the step is the last one
         * @return True if the step is the last one, false otherwise
         */
        public abstract boolean isFinal();
        /**
         * Each step saves the information related to it (forms, tree selections, etc) in a Properties object. This method allows to retrieve it. 
         * Also, the last step of a wizard packs the information in the WizardEvent.TYPE_FINAL_STEP step
         * @return A Properties instance with the information collected in that step or anything relevant to the WizardEventListener
         */
        public abstract Properties getProperties();
    }    
}