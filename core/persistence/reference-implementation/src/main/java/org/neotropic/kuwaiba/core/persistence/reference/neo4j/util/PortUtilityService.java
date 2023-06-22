/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        https://apache.org/licenses/LICENSE-2.0.txt
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.neotropic.kuwaiba.core.persistence.reference.neo4j.util;

import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods related with ports operations, like mirring ports, naming standariotation
 * e.g. the connection of mirror ports, splits a given list of ports into side A and B in order to relate mirror ports
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Service
public class PortUtilityService {
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Creates a mirror port relationship for a given list of ports, if the ports have the same numeric index
     * @param ports the list of ports
     * @param objectClassName the objects 
     * @throws InvalidArgumentException 
     */
    public void mirrorPorts(List<StringPair> ports, String objectClassName) throws InvalidArgumentException {
        List<String> endPointsA = new ArrayList();
        List<String> endPointsB = new ArrayList();
        
        if(ports.size() % 2 == 0){
            for (int i=0; i < ports.size(); i++) {
                StringPair endPointA = ports.get(i);
                if(endPointA != null){
                    for (int j=i+1; j < ports.size(); j++) {
                        StringPair endPointB = ports.get(j);
                        if(endPointB != null && !(endPointB.getKey().equals(endPointA.getKey())) ){
                            if(matchMirrorPortsNames(endPointA.getValue(), endPointB.getValue())){
                                endPointsA.add(endPointA.getKey());
                                endPointsB.add(ports.get(j).getKey());
                                ports.set(j, null);
                                ports.set(i, null);
                                break;
                            }
                        }
                    }//end for
                }
            }//end for
            if(endPointsA.size() != endPointsB.size())
                throw new InvalidArgumentException(ts.getTranslatedString("util.port.number-of-ports-odd"));
            else {
                try {
                    for (int i=0; i < endPointsA.size(); i++) 
                        bem.createSpecialRelationship(objectClassName, endPointsA.get(i), objectClassName, endPointsB.get(i), "mirror", true); //NOI18N
                } catch (BusinessObjectNotFoundException | OperationNotPermittedException | MetadataObjectNotFoundException ex) {
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("util.port.ports-cannot-mirror"), ex.getLocalizedMessage()));
                }
            }
        }
        else
            throw new InvalidArgumentException(ts.getTranslatedString("util.port.number-of-ports-odd"));
    }
    
    /**
     * Checks if two port names have the same numeric index
     * @param backPortName a given name e.g. 067-back or 067-out
     * @param frontPortName a given name e.g. 067-front or 067-in
     * @return true if the name match
     */
    private boolean matchMirrorPortsNames(String backPortName, String frontPortName){
        backPortName = backPortName.toLowerCase();
        frontPortName = frontPortName.toLowerCase();
        String frontNumericPart = "";
        for (int i=1; i < frontPortName.length(); i++){
            if(isNumeric(frontPortName.substring(i-1, i)))
                frontNumericPart += frontPortName.substring(i-1,i);
        }
        String backNumericPart = "";
        
        for (int i=1; i < backPortName.length(); i++){
            if(isNumeric(backPortName.substring(i-1, i)))
                backNumericPart += backPortName.substring(i-1, i);
        }

        return backNumericPart.equals(frontNumericPart);
    }
    
    /**
     * Checks if a given String is a number or not
     * @param s the given string
     * @return true if is a number false if not
     */
    private static boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
    } 
}
