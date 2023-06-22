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

package com.neotropic.kuwaiba.modules.commercial.mpls;

import com.neotropic.kuwaiba.modules.commercial.mpls.model.MplsConnectionDefinition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessRuleException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * This class implements the functionality corresponding to the MPLS module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class MplsService {
    /*
    * Translation service
    */
    @Autowired
    private TranslationService ts;
    /**
     * The MetadataEntityManager instance
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The ApplicationEntityManager instance
     */
    @Autowired
    private ApplicationEntityManager aem;
    //Constants
        /**
     * Class to identify all views made using the MPLS module
     */
    public static String VIEW_CLASS = "MPLSModuleView";
    /**
     * A side in a tributary link
     */
    public static String RELATIONSHIP_MPLSENDPOINTA = "mplsEndpointA";
    /**
     * B side in a tributary link
     */
    public static String RELATIONSHIP_MPLSENDPOINTB = "mplsEndpointB";
    /**
     * Relates a pseudowire and its output interface, the output interface is the endpoint of a MPLS link if is a port
     */
    public static String RELATIONSHIP_MPLS_PW_ISRELATEDWITH_VFI = "mplsPseudowireIsRelatedWithVFI";
    /**
     * Relates two pseudowires that are logical linked inside a MPLS device
     */
    public static String RELATIONSHIP_MPLS_PW_ISRELATEDWITH_PW = "mplsPseudowireIsRelatedWithPseudowire";
    /**
     * Relates the MPLS link directly with the GenericNetworkElements parents of 
     * the end points of the MPLS link, it is used to explore the MPLS links in a 
     * MPLS device or the routing between devices
     */
    public static String RELATIONSHIP_MPLSLINK = "mplsLink";           
    
    
    /**
     * Creates a MPLS Link
     * @param endpointAClassName className of the endpoint side A
     * @param endpointAId id of the end point side A
     * @param endpointBClassName className of the endpoint side B
     * @param endpointBId ind of the endpoint side B
     * @param attributesToBeSet attributes for the new MPLS link
     * @param userName the user name who is executing the method, to update the activity log
     * @return the id of the new MPLS link
     * @throws InvalidArgumentException If the given linkType is no subclass of GenericLogicalConnection
     *                              If any of the requested objects can't be found
     *                              If any of the classes provided can not be found
     *                              If any of the objects involved can't be connected
     */
    public String createMPLSLink(String endpointAClassName, String endpointAId, 
            String endpointBClassName, String endpointBId, 
            HashMap<String, String> attributesToBeSet, String userName) throws InvalidArgumentException {
        if (bem == null || mem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.unexpected-error"));
        String newConnectionId = null;
        try {
            if (!mem.isSubclassOf("GenericLogicalConnection", Constants.CLASS_MPLSLINK))
                throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.not-generic-connection-subclass"), Constants.CLASS_MPLSLINK));
            
            BusinessObject communicationsEquipmentA, communicationsEquipmentB;
            //at least one side should be not null to create the MPLS link
            if(endpointAClassName != null && endpointAId != null || endpointBClassName != null && endpointBId != null){
                newConnectionId = bem.createSpecialObject(Constants.CLASS_MPLSLINK, null, "-1", attributesToBeSet, null);
                aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s [%s] created", attributesToBeSet.get(Constants.PROPERTY_NAME), Constants.CLASS_MPLSLINK));
            }
            //Side A
            if(endpointAClassName != null && endpointAId != null){
                communicationsEquipmentA= bem.getFirstParentOfClass(endpointAClassName, endpointAId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            
                if (communicationsEquipmentA == null)
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.port-not-located-in-communications-equipment"), endpointAClassName, endpointAId));
             
                if(mem.isSubclassOf(Constants.CLASS_GENERICPORT, endpointAClassName)){
                    String endPointName = bem.getAttributeValueAsString(endpointAClassName, endpointAId, Constants.PROPERTY_NAME);
                    bem.createSpecialRelationship(Constants.CLASS_MPLSLINK, newConnectionId, endpointAClassName, endpointAId, RELATIONSHIP_MPLSENDPOINTA, true);
                    aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s[%s] - %s - %s", attributesToBeSet.get(Constants.PROPERTY_NAME), RELATIONSHIP_MPLSLINK, RELATIONSHIP_MPLSENDPOINTB, endPointName));
                }
                else 
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.not-subclass-genericport-cant-be-endpoint"), endpointAClassName));
                //besides the reletionship of the MPLS link with its endpoints, we create a direct relatioship between the mplsLink and the device 
                //this relationships helps to easily check the mplsLink in a device and to creates a simple MPLS map
                bem.createSpecialRelationship(communicationsEquipmentA.getClassName(), communicationsEquipmentA.getId(), Constants.CLASS_MPLSLINK, newConnectionId, RELATIONSHIP_MPLSLINK, false);
                aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - %s - %s", communicationsEquipmentA.getName(), RELATIONSHIP_MPLSLINK, attributesToBeSet.get(Constants.PROPERTY_NAME)));
            }
            //Side B
            if(endpointBClassName != null && endpointBId != null){
                communicationsEquipmentB  = bem.getFirstParentOfClass(endpointBClassName, endpointBId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
           
                if (communicationsEquipmentB == null)
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.port-not-located-in-communications-equipment"), endpointBClassName, endpointBId));
            
                if(mem.isSubclassOf(Constants.CLASS_GENERICPORT, endpointBClassName)){
                    String endPointName = bem.getAttributeValueAsString(endpointBClassName, endpointBId, Constants.PROPERTY_NAME);
                    bem.createSpecialRelationship(Constants.CLASS_MPLSLINK, newConnectionId, endpointBClassName, endpointBId, RELATIONSHIP_MPLSENDPOINTB, true);
                    aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s[%s] - %s - %s", attributesToBeSet.get(Constants.PROPERTY_NAME), RELATIONSHIP_MPLSLINK, RELATIONSHIP_MPLSENDPOINTB, endPointName));
                }
                else 
                    throw new InvalidArgumentException(String.format("%s is not subClass of GenericPort, can not be endpoint of a mplsLink", endpointBClassName));
                //Direct relationship with the device
                bem.createSpecialRelationship(Constants.CLASS_MPLSLINK, newConnectionId, communicationsEquipmentB.getClassName(), communicationsEquipmentB.getId(), RELATIONSHIP_MPLSLINK, false);
                aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - %s - %s", communicationsEquipmentB.getName(), RELATIONSHIP_MPLSLINK, attributesToBeSet.get(Constants.PROPERTY_NAME)));
            }

            return newConnectionId;
        } catch (Exception e) {
            //TODO: This should be replace with a transaction that lasts as long as everything in this method has been done, instead of
            //doing commits in every call to the BEM
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null) {
                try {
                    bem.deleteObject(Constants.CLASS_MPLSLINK, newConnectionId, true);
                } catch (Exception ex) {
                    Logger.getLogger(MplsService.class.getName()).log(Level.SEVERE, null, ex);
                    throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.unexpected-error"));
                } 
            }

            throw new InvalidArgumentException(e.getMessage());
        }
    }
    
    /**
     * Deletes a MPLS Link
     * @param linkId the mplslink id
     * @param forceDelete true deletes the mpls link even if have more relationships, false does not deletes the mpls link if have relationships
     * @param userName the user name who is executing the method, to update the activity log
     * @throws InvalidArgumentException If the object can not be found
     *                             If either the object class or the attribute can not be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    public void deleteMPLSLink(String linkId, boolean forceDelete, String userName) throws InvalidArgumentException{
        if (bem == null || mem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.unexpected-error"));
        try{
            BusinessObjectLight mplsLink =  bem.getObjectLight(Constants.CLASS_MPLSLINK, linkId);
            if (!mplsLink.getClassName().equals(Constants.CLASS_MPLSLINK)) //NOI18N
                    throw new InvalidArgumentException(ts.getTranslatedString("module.mpls.only-mpls-links-can-be-deleted"));

            bem.deleteObject(mplsLink.getClassName(), linkId, forceDelete);
            aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, String.format("%s deleted", mplsLink, Constants.CLASS_MPLSLINK));
        }catch(Exception ex){
            throw new InvalidArgumentException(ex.getMessage());
        }
    }
    
    /**
     * Get the MPLS link details, its output interfaces, pseudowires, tunnels
     * @param connectionId MPLS link id
     * @return MPLS link endpoints
     * @throws InvalidArgumentException 
     */
    public MplsConnectionDefinition getMPLSLinkDetails(String connectionId) 
            throws InvalidArgumentException 
    {
        if (bem == null || mem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.unexpected-error"));
        try{
            BusinessObject mplsLink = bem.getObject(Constants.CLASS_MPLSLINK, connectionId);
           
            List<BusinessObjectLight> endpointAs = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTA);
            List<BusinessObjectLight> endpointBs = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTB);
            MplsConnectionDefinition mplsConnectionDefinition = new MplsConnectionDefinition(mplsLink);
            //side A
            if(!endpointAs.isEmpty()){
                BusinessObjectLight endpointA = endpointAs.get(0);
                mplsConnectionDefinition.setEndpointA(endpointA);
                BusinessObject deviceA = bem.getFirstParentOfClass(endpointA.getClassName(), endpointA.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                mplsConnectionDefinition.setDeviceA(deviceA);
                
                if(endpointA.getClassName().equals(Constants.CLASS_PSEUDOWIRE)){
                    List<BusinessObjectLight> vfis = bem.getSpecialAttribute(endpointA.getClassName(), endpointA.getId(), RELATIONSHIP_MPLS_PW_ISRELATEDWITH_VFI);
                    if(!vfis.isEmpty())
                        mplsConnectionDefinition.setVfiA(vfis.get(0));
                }
            }//side B
            if(!endpointBs.isEmpty()){
                BusinessObjectLight endpointB = endpointBs.get(0);
                mplsConnectionDefinition.setEndpointB(endpointB);
                BusinessObject deviceB = bem.getFirstParentOfClass(endpointB.getClassName(), endpointB.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                mplsConnectionDefinition.setDeviceB(deviceB);
                
                if(endpointB.getClassName().equals(Constants.CLASS_PSEUDOWIRE)){
                    List<BusinessObjectLight> vfis = bem.getSpecialAttribute(endpointB.getClassName(), endpointB.getId(), RELATIONSHIP_MPLS_PW_ISRELATEDWITH_VFI);
                    if(!vfis.isEmpty())
                        mplsConnectionDefinition.setVfiB(vfis.get(0));
                }
            }

            return mplsConnectionDefinition;
            
        }catch(Exception ex){
            throw new InvalidArgumentException(ex.getMessage());
        }
    }
    
    /**
     * Connect MPLS links
     * @param sideAClassNames endpoint side A class names
     * @param sideAIds endpoint side A ids
     * @param linksIds MPLS links ids
     * @param sideBClassNames endpoint side B class names
     * @param sideBIds endpoint side B ids
     * @param userName the user who executes this method in order to update the activity log
     * @throws InvalidArgumentException 
     * @throws MetadataObjectNotFoundException 
     * @throws BusinessObjectNotFoundException 
     * @throws BusinessRuleException 
     * @throws OperationNotPermittedException 
     */
    public void connectMplsLink(String[] sideAClassNames, String[] sideAIds, 
            String[] linksIds, String[] sideBClassNames, String[] sideBIds, String userName)
            throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, BusinessRuleException, OperationNotPermittedException
    { 
        try{
            for (int i = 0; i < linksIds.length; i++){
                BusinessObjectLight mplsLink = null;
                if (linksIds[i] != null)
                    mplsLink = bem.getObjectLight(Constants.CLASS_MPLSLINK, linksIds[i]);
                if(mplsLink != null && !mplsLink.getClassName().equals(Constants.CLASS_MPLSLINK)) //NOI18N
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.class-is-not-a-mpls-link"), mplsLink.getClassName()));
                if (sideAClassNames[i] != null && !mem.isSubclassOf(Constants.CLASS_GENERICPORT, sideAClassNames[i])) //NOI18N
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.class-is-not-a-port"), sideAClassNames[i]));
                if (sideBClassNames[i] != null && !mem.isSubclassOf(Constants.CLASS_GENERICPORT, sideBClassNames[i])) //NOI18N
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.class-is-not-a-port"), sideBClassNames[i]));
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new InvalidArgumentException(ts.getTranslatedString("module.mpls.cant-connect-port-itself"));

                if(mplsLink != null){
                    String endpointAName = RELATIONSHIP_MPLSENDPOINTA, endpointBName = RELATIONSHIP_MPLSENDPOINTB;

                    List<BusinessObjectLight> aEndpointList = bem.getSpecialAttribute(mplsLink.getClassName(), mplsLink.getId(), endpointAName); //NOI18N
                    List<BusinessObjectLight> bEndpointList = bem.getSpecialAttribute(mplsLink.getClassName(), mplsLink.getId(), endpointBName); //NOI18N

                    if (!aEndpointList.isEmpty()){
                        if (Objects.equals(aEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(aEndpointList.get(0).getId(), sideBIds[i]))
                            throw new InvalidArgumentException(ts.getTranslatedString("module.mpls.link-endpoint-already-related"));
                    }

                    if (!bEndpointList.isEmpty()){
                        if (Objects.equals(bEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(bEndpointList.get(0).getId(), sideBIds[i]))
                            throw new InvalidArgumentException(ts.getTranslatedString("module.mpls.link-endpoint-already-related"));
                    }

                    if (sideAIds[i] != null && sideAClassNames[i] != null) {
                        if (!bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], endpointAName).isEmpty() || //NOI18N
                            !bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], endpointBName).isEmpty()) //NOI18N
                            throw new InvalidArgumentException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideAClassNames[i], sideAIds[i])));

                        if (aEndpointList.isEmpty()) {
                            aem.checkRelationshipByAttributeValueBusinessRules(mplsLink.getClassName(), mplsLink.getId(), sideAClassNames[i], sideAIds[i]);
                            bem.createSpecialRelationship(mplsLink.getClassName(), mplsLink.getId(), sideAClassNames[i], sideAIds[i], endpointAName, true); //NOI18N
                            
                            aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format("%s - %s",  sideAIds[i], mplsLink.getName()));
                        }
                        else
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.link-already-has-endpointa"), bem.getObjectLight(mplsLink.getClassName(), mplsLink.getId())));
                    }
                    if (sideBIds[i] != null && sideBClassNames[i] != null) {
                        if (!bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], endpointBName).isEmpty() || //NOI18N
                            !bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], endpointAName).isEmpty()) //NOI18N
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.selected-endpoint-already-connecte"), bem.getObjectLight(sideBClassNames[i], sideBIds[i])));

                        if (bEndpointList.isEmpty()) {
                            aem.checkRelationshipByAttributeValueBusinessRules(mplsLink.getClassName(), mplsLink.getId(), sideBClassNames[i], sideBIds[i]);
                            bem.createSpecialRelationship(mplsLink.getClassName(), mplsLink.getId(), sideBClassNames[i], sideBIds[i], endpointBName, true); //NOI18N
                            
                            aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, String.format(" %s - %s", mplsLink.getName(), sideBIds[i]));
                        }
                        else
                            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.link-already-has-endpointb"), bem.getObjectLight(mplsLink.getClassName(), mplsLink.getId())));
                    }
                }
            }
        } catch (InventoryException ex) {
            throw new InvalidArgumentException(ex.getMessage());
        }
    }
    
    
    /**
     * Disconnect a mplsLink from its endpoints
     * @param connectionId MPLS link id
     * @param sideToDisconnect if is side A or side B or both sides
     * @param userName the user name who is executing the method, to update the activity log
     * @throws InvalidArgumentException 
     */
    public void disconnectMPLSLink(String connectionId, int sideToDisconnect, String userName) throws InvalidArgumentException{
        if (bem == null || mem == null)
            throw new InvalidArgumentException(ts.getTranslatedString("module.general.messages.unexpected-error"));
        try{
            String  affectedProperties = "", oldValues = "";
            switch (sideToDisconnect) {
                case 1: //A side
                    BusinessObjectLight endpointA = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTA).get(0); //NOI18N                    
                    bem.releaseRelationships(Constants.CLASS_MPLSLINK, connectionId, Arrays.asList(RELATIONSHIP_MPLSENDPOINTA)); //NOI18N

                    affectedProperties += RELATIONSHIP_MPLSENDPOINTA + " "; //NOI18N
                    oldValues += endpointA.getId() + " ";
                    break;
                case 2: //B side
                    BusinessObjectLight endpointB = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTB).get(0); //NOI18N                    
                    bem.releaseRelationships(Constants.CLASS_MPLSLINK, connectionId, Arrays.asList(RELATIONSHIP_MPLSENDPOINTB)); //NOI18N

                    affectedProperties += RELATIONSHIP_MPLSENDPOINTB + " "; //NOI18N
                    oldValues += endpointB.getId() + " ";
                    break;
                case 3: //Both sides
                    endpointA = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTA).get(0); //NOI18N
                    endpointB = bem.getSpecialAttribute(Constants.CLASS_MPLSLINK, connectionId, RELATIONSHIP_MPLSENDPOINTB).get(0); //NOI18N
                    bem.releaseRelationships(Constants.CLASS_MPLSLINK, connectionId, Arrays.asList(RELATIONSHIP_MPLSENDPOINTA, RELATIONSHIP_MPLSENDPOINTB)); //NOI18N

                    affectedProperties += RELATIONSHIP_MPLSENDPOINTA + " "; //NOI18N
                    oldValues += endpointA.getId() + " ";

                    affectedProperties += RELATIONSHIP_MPLSENDPOINTB + " "; //NOI18N
                    oldValues += endpointB.getId() + " ";
                    break;
                default:
                    throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.mpls.wrong-side-disconnect-option")));
            }
            aem.createObjectActivityLogEntry(userName, Constants.CLASS_MPLSLINK, connectionId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                affectedProperties, oldValues, "", ""); //NOI18N
        } catch(Exception ex) {
            throw new InvalidArgumentException(ex.getMessage());
        }
    }
}