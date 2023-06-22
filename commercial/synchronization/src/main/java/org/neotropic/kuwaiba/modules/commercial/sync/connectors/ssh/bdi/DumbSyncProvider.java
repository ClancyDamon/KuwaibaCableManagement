/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi;

import com.jcraft.jsch.ChannelExec;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.modules.commercial.sync.connectors.ssh.bdi.parsers.GenericRouterParser;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractDataEntity;
import org.neotropic.kuwaiba.modules.commercial.sync.model.AbstractSyncProvider;
import org.neotropic.kuwaiba.modules.commercial.sync.model.PollResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncAction;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncFinding;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SynchronizationGroup;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This provider connects to Cisco routers via SSH, retrieves the bridge domain configuration, and creates/updates the relationships between
 * the bridge domains and the logical/physical
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
@Component
public class DumbSyncProvider extends AbstractSyncProvider {

    @Override
    public String getDisplayName() {
        //Bridge Domains and Bridge Domain Interfaces Sync Provider
        return "Dumb Domains";
    }

    @Override
    public String getId() {
        return DumbSyncProvider.class.getName();
    }

    @Override
    public boolean isAutomated() {
        return true;
    }

    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    public PollResult fetchData(SyncDataSourceConfiguration dataSourceConfiguration) {

        PollResult res = new PollResult();

        for (int i = 0; i < 1000; i++) {
            GenericRouterParser parser = new GenericRouterParser();
            UUID uuid = UUID.randomUUID();
            //simulate read process
            res.getResult().put(dataSourceConfiguration,
                    parser.parse(uuid.toString()));
            System.out.println("router found " + uuid.toString());
            try {
                Thread.sleep(3);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {
        List<SyncResult> res = new ArrayList<>();

        for (SyncDataSourceConfiguration dataSourceConfiguration : pollResult.getResult().keySet()) {
            for (int i = 0; i < 1000; i++) {

                //The bridge domains found in the real device
                List<AbstractDataEntity> routersInDevice = pollResult.getResult().get(dataSourceConfiguration);
                for (AbstractDataEntity routerInDevice : routersInDevice) {
                    UUID uuid = UUID.randomUUID();
                    // generate random numbers between 0 to 4 
                    // Math.random() generates random number from 0.0 to 0.999
                    // Hence, Math.random()*5 will be from 0.0 to 4.999
                    double doubleRandomNumber = Math.random() * 5;
                    String newRouter = "Router " + uuid.toString();
                    res.add(new SyncResult(dataSourceConfiguration.getId()
                            , (int) doubleRandomNumber
                            , String.format("Check if Bridge Domain %s exists within %s"
                            , routerInDevice.getName(), newRouter),
                            "The Bridge Domain did not exist and was created successfully"));

                    try {
                        Thread.sleep(5);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return res;
    }

    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support automated sync for unmapped pollings");
    }

    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support supervised sync");
    }

    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("This provider does not support supervised sync");
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("This provider does not support this operation"); //Not used for now
    }

    /**
     * Reads the channel's input stream into a string.
     *
     * @param channel The session's channel.
     * @return The string with the result of the command execution.
     * @throws InvalidArgumentException if there was an error executing the command or reading its result.
     */
    private String readCommandExecutionResult(ChannelExec channel) throws InvalidArgumentException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(channel.getInputStream()))) {
            String result = buffer.lines().collect(Collectors.joining("\n"));
            return channel.getExitStatus() == 0 ? result : null;
        } catch (IOException ex) {
            throw new InvalidArgumentException(String.format("Error reading the command execution result: %s", ex.getLocalizedMessage()));
        }
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}