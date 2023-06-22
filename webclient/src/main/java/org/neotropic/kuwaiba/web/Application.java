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

import com.neotropic.kuwaiba.modules.commercial.sdh.SdhModule;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.ws.Endpoint;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.processman.ProcessManagerService;
import org.neotropic.kuwaiba.northbound.ws.KuwaibaSoapWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

/**
 * Application entry point.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Component
    public static class Bootstrap {
        // Web service properties
        @Value("${ws.port}")
        private int wsPort;
        // General properties
        @Value("${general.enable-security-manager}")
        private boolean enableSecurityManager;
        @Value("${general.locale}")
        private String locale;
        
        // Connection properties
        @Value("${db.path}")
        private String dbPath;
        @Value("${db.host}")
        private String dbHost;
        @Value("${db.port}")
        private int dbPort;
        
        // Application properties
        @Value("${aem.enforce-business-rules}")
        private String enforceBusinessRules;
        @Value("${aem.process-engine-path}")
        private String processEnginePath;
        @Value("${aem.processes-path}")
        private String processesPath;
        @Value("${aem.max-routes}")
        private String maxRoutes;
        @Value("${aem.backgrounds-path}")
        private String backgroundsPath;
        
        // Business properties
        @Value("${bem.attachments-path}")
        private String attachmentsPath;
        @Value("${bem.max-attachment-size}")
        private String maxAttachmentSize;
        
        @Autowired
        private PersistenceService persistenceService;
        @Autowired
        private TranslationService ts;
        @Autowired
        private KuwaibaSoapWebService ws;
        @Autowired
        private SdhModule modSdh;
        @Autowired
        private ProcessManagerService processManagerService;
        
        @PostConstruct
        void init() {
            // Register a custom logger
            LogManager.getLogManager().reset();
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.addHandler(new LogHandler());
            
            try {
                ts.setCurrentlanguage(locale);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.INFO, ex.getMessage());
            }

            Properties generalProperties = new Properties();
            generalProperties.put("enableSecurityManager", enableSecurityManager);
            persistenceService.setGeneralProperties(generalProperties);
            
            Properties connectionProperties = new Properties();
            connectionProperties.put("dbPath", dbPath);
            connectionProperties.put("dbHost", dbHost);
            connectionProperties.put("dbPort", dbPort);
            persistenceService.setConnectionProperties(connectionProperties);

            persistenceService.setMetadataProperties(new Properties());
            
            Properties applicationProperties = new Properties();
            applicationProperties.put("enforceBusinessRules", enforceBusinessRules);
            applicationProperties.put("processEnginePath", processEnginePath);
            applicationProperties.put("processesPath", processesPath);
            applicationProperties.put("maxRoutes", maxRoutes);
            applicationProperties.put("backgroundsPath", backgroundsPath);
            persistenceService.setApplicationProperties(applicationProperties);
            
            Properties businessProperties = new Properties();
            businessProperties.put("attachmentsPath", attachmentsPath);
            businessProperties.put("maxAttachmentSize", maxAttachmentSize);
            persistenceService.setBusinessProperties(businessProperties);
            
            try {
                persistenceService.start();
            } catch (IllegalStateException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, 
                        String.format(ts.getTranslatedString("module.persistence.messages.cant-start-persistence-service"), 
                            ex.getLocalizedMessage()));
            }
            
            Logger.getLogger(SdhModule.class.getName()).log(Level.INFO, 
                    String.format(ts.getTranslatedString("module.general.messages.initializing"), modSdh.getName()));
            modSdh.configureModule(persistenceService.getMem(), persistenceService.getAem(), persistenceService.getBem());
            processManagerService.init(persistenceService, ts);

            if (persistenceService.getState().equals(PersistenceService.EXECUTION_STATE.RUNNING)) {
                // After java 8 SAAJMetaFactoryImpl is no longer the default implementation of SAAJMetaFactory, so for later versions
                // the mapping has to be done manually
                System.setProperty("javax.xml.soap.MetaFactory","com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
                Endpoint.publish(String.format("http://0.0.0.0:%s/kuwaiba/KuwaibaService", wsPort), ws);
                Logger.getLogger(PersistenceService.class.getName()).log(Level.INFO, 
                    String.format("Web service initialized and running on port %s", wsPort));
            } else
                Logger.getLogger(PersistenceService.class.getName()).log(Level.SEVERE, 
                    "Web service could not be initialized because the Persistence Service is not running");
        }
        
        @PreDestroy
        void shutdown() {
            try {
                persistenceService.stop();
            } catch (IllegalStateException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }
}
