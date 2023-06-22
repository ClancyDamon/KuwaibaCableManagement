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

package org.neotropic.kuwaiba.web;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * The Spring basic automated configuration file. 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Configuration
@EnableAsync
// Spring beans
@ComponentScan(basePackages = { "org.neotropic.kuwaiba.core", // Core services and utilities
                                "org.neotropic.kuwaiba.modules", // Core and optional modules
                                "com.neotropic.kuwaiba.modules", // Commercial modules
                                "org.neotropic.kuwaiba.northbound", // The SOAP-based web service interface implementation.
                                "org.neotropic.kuwaiba.visualization", // Temporary location of some core beans. TODO: Relocate them all
                              })
// Vaadin routes
@EnableVaadin(value = { "org.neotropic.kuwaiba.core", // General settings and administration stuff 
                        "org.neotropic.kuwaiba.modules",  // UIs for core and optional modules
                        "com.neotropic.kuwaiba.modules", // UIs for commercial modules
                        "org.neotropic.kuwaiba.web.ui"}) // General purpose UIs
public class SpringConfiguration { 
    /**
     * Max number of threads.
     */
    @Value("${services.threading.max-threads}")
    private int maxThreads;
    /**
     * Max number of CPU cores to be used (if available).
     */
    @Value("${services.threading.max-cores}")
    private int maxCores;
    /**
     * Max size of the queue of tasks to be executed.
     */
    @Value("${services.threading.queue-size}")
    private int queueSize;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    
    /**
     * Configures the default threading behavior through an Executor instance. Configuration 
     * parameters can be found in the application.properties file with the prefix services.threading
     * @return An instance of executor with the configuration specified in the properties file. 
     */
    @Bean (name = "taskExecutor")
    public Executor taskExecutor() {
        Logger.getLogger(SpringConfiguration.class.getName()).log(Level.SEVERE, 
                String.format(ts.getTranslatedString("module.general.messages.thread-executor-initialized"), 
                    new Object[] { maxCores, maxThreads, queueSize }));
        
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maxCores);
        executor.setMaxPoolSize(maxThreads);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix("kuwaiba-thread-");
        executor.initialize();
        return executor;
    }
}
