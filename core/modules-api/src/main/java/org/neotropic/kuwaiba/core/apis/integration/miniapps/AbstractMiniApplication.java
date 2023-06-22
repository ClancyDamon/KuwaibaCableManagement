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
package org.neotropic.kuwaiba.core.apis.integration.miniapps;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;

/**
 * This class defines the behavior of all mini applications. A mini application is a piece of code (wrapped in a graphical or console element
 * such as window or a panel) that can be used to perform complex tasks. These mini applications are thought to be used in an automated context,
 * so in most cases, the mini applications will be instantiated using reflection in scripts or modules that don't know beforehand what's the class of the miniapplication to be launched
 * 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <D> The type of the mini application component to be used in detached mode
 * @param <E> The type of the mini application component to be used in embedded mode
 */
public abstract class AbstractMiniApplication<D, E> {
    /**
     * The mini application is a console application
     */
    public static int TYPE_CONSOLE = 1;
    /**
     * The mini application is a desktop (Swing) application
     */
    public static int TYPE_DESKTOP = 2;
    /**
     * The mini application is a web (Vaadin) application
     */
    public static int TYPE_WEB = 3;
    /**
     * A set of parameters used as input for the logic implemented by the mini application
     */
    protected Properties inputParameters;
    /**
     * The results of the operations performed by the mini applications can be 
     */
    protected Properties miniApplicationData;
    /**
     * Listeners to events during the execution of the mini application
     */
    List<MiniApplicationEventListener> listeners;
    /**
     * Reference to the application entity manager.
     */
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    protected BusinessEntityManager bem;
    /**
     * Reference to the metadata entity manager.
     */
    protected MetadataEntityManager mem;
    /**
     * Default constructor. Use [AbstractMiniApplicationSubclass].getConstructor(Properties.class).newInstance(...) to create instances using reflection
     * @param inputParameters Input configuration parameters for the current mini application
     */
    public AbstractMiniApplication(Properties inputParameters) {
        this.inputParameters = inputParameters;
        this.miniApplicationData = new Properties();
        this.listeners = new ArrayList<>();
    }
        
    public void setApplicationEntityManager(ApplicationEntityManager aem) {
        this.aem = aem;
    }
    
    public void setBusinessEntityManager(BusinessEntityManager bem) {
        this.bem = bem;
    }
    
    public void setMetadataEntityManager(MetadataEntityManager mem) {
        this.mem = mem;
    }
    
    public Properties getInputParameters() {
        return inputParameters;
    }
    
    public void setInputParameters(Properties inputParameters) {
        this.inputParameters = inputParameters;
    }
    
    public Properties getOutputParameters() {
        return miniApplicationData;
    }
    
    public void setOutputParameters(Properties outputParameters) {
        this.miniApplicationData = outputParameters;
    }
    
    /**
     * Adds a result listener
     * @param listener The listener to be added
     */
    public void addEventListener(MiniApplicationEventListener listener){
        this.listeners.add(listener);
    }
    
    /**
     * Removes a listener
     * @param listener The listener to be removed
     */
    public void removeEventListener(MiniApplicationEventListener listener) {
        this.listeners.remove(listener);
    }
    
    /**
     * What does the application do
     * @return The description of the mini application
     */
    public abstract String getDescription();
    
    /**
     * Launches the mini application in a separate window
     * @return The component (JFrame, Window, etc) to be displayed
     */
    public abstract D launchDetached();
    
    /**
     * Launches the mini application in a component that allows the caller application to embed the contents somewhere else.
     * @return The component to be embedded (JPanel, VericalLayout, etc)
     */
    public abstract E launchEmbedded();
    
    /**
     * Retrieves the results of the operations performed by the mini application
     * @return An object with a list of (optional) results (like inventory objects or result descriptors) of the miniapplication execution
     */
    public Properties getMiniApplicationData() {
        return this.miniApplicationData;
    }
    
    /**
     * Retrieves the type of the application
     * @return The type of mini application. See TYPE_XXX for valid values
     */
    public abstract int getType();
}