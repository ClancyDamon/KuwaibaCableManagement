/*
 * Copyright 2023-2023. Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.modules.commercial.sync.connectors.snmp.reference;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ArraySizeMismatchException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncFinding;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncUtil;
import org.neotropic.kuwaiba.modules.commercial.sync.model.TableData;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class EntPhysicalSynchronizer {
    /**
     * The class name of the object
     */
    private final String className;
    /**
     * Device id
     */
    private final String id;
    /**
     * Device Data Source Configuration id
     */
    private final long dsConfigId;
    /**
     * Current structure of the device
     */
    private final HashMap<Long, List<BusinessObjectLight>> currentObjectStructure;
    /**
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> currentVirtualPorts;
    /**
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> currentServiceInstances;
    /**
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> foundVirtualPorts;
    /**
     * The current ports in the device
     */
    private final List<BusinessObjectLight> currentPorts;
    /**
     * Current MPLS tunnels
     */
    private final List<BusinessObjectLight> currentMplsTunnels;
    /**
     * To load the structure of the actual device
     */
    private final List<BusinessObjectLight> foundMplsTunnels;
    /**
     * To keep a trace of the new ports created during synchronization
     */
    private final List<BusinessObject> newPorts;
    /**
     * The ports of the device before the synchronization
     */
    private final List<BusinessObjectLight> notMatchedPorts;
    /**
     * To keep a trace of the list types evaluated, to not create them twice
     */
    private final List<String> listTypeEvaluated;
    /**
     * a map of the file to create the objects
     */
    private final HashMap<String, List<String>> mapOfFile = new HashMap<>();
    /**
     * the result finding list
     */
    private final List<SyncResult> results = new ArrayList<>();
    /**
     * The entity table loaded into the memory
     */
    private final HashMap<String, List<String>> entityData;
    /**
     * The if-mib table loaded into the memory
     */
    private final HashMap<String, List<String>> ifXTable;
    /**
     * reference to the bem
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to {@link ApplicationEntityManager} aem
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to {@link MetadataEntityManager} mem
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to{@link TranslationService} Translation Service
     */
    private final TranslationService ts;
    private final HashMap<String, String> createdIdsToMap;
    private final HashMap<String, List<BusinessObject>> newCreatedPortsToCreate;
    /**
     * debugMode
     */
    private final boolean debugMode;
    /**
     * The current first level children of the actual device
     */
    private List<BusinessObjectLight> currentFirstLevelChildren;
    /**
     * An aux variable, used to store the branch of the old object structure
     * while the objects are checked, before the creations of the branch
     */
    private List<BusinessObjectLight> tempAuxOldBranch = new ArrayList<>();
    /**
     * a map of the file to create the classes in the containment hierarchy
     */
    private HashMap<String, List<String>> mapOfClasses = new HashMap<>();
    /**
     * Default initial ParentId in the SNMP table data
     */
    private String INITAL_ID;
    /**
     * To keep the objects during synchronization
     */
    private List<BusinessObject> branch;
    /**
     * Helper used to read the actual structure recursively
     */
    private long k = 0;

    public EntPhysicalSynchronizer(long dsConfigId, BusinessObjectLight obj, List<TableData> data, BusinessEntityManager bem
            , ApplicationEntityManager aem, MetadataEntityManager mem, TranslationService ts) {
        this.bem = bem;
        this.aem = aem;
        this.mem = mem;
        this.ts = ts;

        this.className = obj.getClassName();
        this.id = obj.getId();
        this.dsConfigId = dsConfigId;
        entityData = (HashMap<String, List<String>>) data.get(0).getValue();
        ifXTable = (HashMap<String, List<String>>) data.get(1).getValue();
        currentObjectStructure = new HashMap<>();
        newPorts = new ArrayList<>();
        currentPorts = new ArrayList<>();
        listTypeEvaluated = new ArrayList<>();
        branch = new ArrayList<>();
        notMatchedPorts = new ArrayList<>();
        currentFirstLevelChildren = new ArrayList<>();
        currentVirtualPorts = new ArrayList<>();
        currentMplsTunnels = new ArrayList<>();
        currentServiceInstances = new ArrayList<>();
        createdIdsToMap = new HashMap<>();
        newCreatedPortsToCreate = new HashMap<>();
        debugMode = false;//(Boolean) aem.getConfiguration().get("debugMode");
        foundMplsTunnels = new ArrayList<>();
        foundVirtualPorts = new ArrayList<>();
    }


    public List<SyncResult> sync() throws MetadataObjectNotFoundException,
            BusinessObjectNotFoundException, InvalidArgumentException,
            OperationNotPermittedException, ApplicationObjectNotFoundException, ArraySizeMismatchException
            , NotAuthorizedException {
        readData();
        loadClassHierarchy();
        readCurrentFirstLevelChildren();
        readCurrentDeviceStructure(bem.getObjectChildren(className, id, -1));
        readCurrentSpecialStructure(bem.getObjectSpecialChildren(className, id));
        //printData(); //<- for debuging
        checkObjects(id, "", "");
        checkPortsToMigrate();
        checkDataToBeDeleted();
        checkPortsWithNoMatch();
        syncIfMibData();
        return results;
    }

    //<editor-fold desc="Methods to read the data and load it into memory" defaultstate="collapsed">

    /**
     * Reads the data loaded into memory
     */
    public void readData() {
        //The initial id is the id of the chassis in most of the cases is 0, except in the model SG500
        INITAL_ID = entityData.get("entPhysicalContainedIn").get(entityData.get("entPhysicalClass").indexOf("3"));
        if (entityData.get("entPhysicalContainedIn").contains(INITAL_ID))
            createTreeFromFile(INITAL_ID);
        else
            results.add(new SyncResult(dsConfigId, SyncFinding.EVENT_ERROR, ts.getTranslatedString("no_inital_id_was_found")
                    , ts.getTranslatedString("check_initial_id_in_snmp_data")));
        removeChildrenless();
        createMapOfClasses();
    }

//<editor-fold desc="for debuging" defaultstate="collapsed">
//    private void printData(){
//        for(String key : mapOfFile.keySet()){
//            String parsedClass;
//            List<String> children = mapOfFile.get(key);
//            if(!key.equals("0")){
//                int i = allData.get("instance").indexOf(key);
//                parsedClass = parseClass(allData.get("entPhysicalClass").get(i),
//                        allData.get("entPhysicalName").get(i),
//                        allData.get("entPhysicalDescr").get(i));
//                System.out.println("id: " + key + " " + allData.get("entPhysicalName").get(i) + "["+parsedClass+"]");
//            }
//            else
//                System.out.println("R A C K -> ");
//
//            for (String child : children) {
//                int childIndex = allData.get("instance").indexOf(child);
//                parsedClass = parseClass(allData.get("entPhysicalClass").get(childIndex),
//                    allData.get("entPhysicalName").get(childIndex),
//                    allData.get("entPhysicalDescr").get(childIndex));
//                System.out.println("P:" + allData.get("entPhysicalContainedIn").get(childIndex) + " -id: " + child + " " + allData.get("entPhysicalName").get(childIndex) + "["+parsedClass+"]");
//            }
//        }
//    }
//</editor-fold>

    /**
     * Translate the Hash map lists into a map with parentsIds and his
     * childrenIds
     *
     * @param parentId alleged parent Id
     */
    private void createTreeFromFile(String parentId) {
        for (int i = 0; i < entityData.get("entPhysicalContainedIn").size(); i++) {
            if (entityData.get("entPhysicalContainedIn").get(i).equals(parentId)) {
                if (isClassUsed(entityData.get("entPhysicalClass").get(i),
                        entityData.get("entPhysicalDescr").get(i))) {
                    saveInTreeMap(parentId, entityData.get("instance").get(i));
                    createTreeFromFile(entityData.get("instance").get(i));
                }
            }
        }
    }

    /**
     * Puts the data into a HashMap
     *
     * @param parent the parent's class
     * @param child  the child's classes
     */
    private void saveInTreeMap(String parent, String child) {
        List<String> childrenLines = mapOfFile.get(parent);
        if (childrenLines == null) {
            childrenLines = new ArrayList<>();
            childrenLines.add(child);
            mapOfFile.put(parent, childrenLines);
        } else {
            mapOfFile.get(parent).add(child);
        }
    }

    /**
     * Removes keys without children
     */
    private void removeChildrenless() {
        List<String> keysToremove = new ArrayList<>();
        for (String key : mapOfFile.keySet()) {
            List<String> children = mapOfFile.get(key);
            if (children.isEmpty()) {
                keysToremove.add(key);
            }
        }
        keysToremove.forEach(mapOfFile::remove);
    }

    /**
     * Creates the Hash map of classes to create the hierarchy containment
     */
    private void createMapOfClasses() {
        mapOfClasses = new HashMap<>();
        for (String key : mapOfFile.keySet()) {
            if (!key.equals("0")) {
                List<String> childrenId = mapOfFile.get(key);
                int w = entityData.get("instance").indexOf(key);
                String patentClassParsed = parseClass(
                        entityData.get("entPhysicalModelName").get(w),
                        entityData.get("entPhysicalClass").get(w),
                        entityData.get("entPhysicalName").get(w),
                        entityData.get("entPhysicalDescr").get(w)//NOI18N
                );

                if (patentClassParsed != null) {
                    List<String> childrenParsed = mapOfClasses.get(patentClassParsed);
                    if (childrenParsed == null)
                        childrenParsed = new ArrayList<>();

                    for (String child : childrenId) {
                        int indexOfChild = entityData.get("instance").indexOf(child);
                        String childParsedClass = parseClass(
                                entityData.get("entPhysicalModelName").get(w),
                                entityData.get("entPhysicalClass").get(indexOfChild),
                                entityData.get("entPhysicalName").get(indexOfChild),
                                entityData.get("entPhysicalDescr").get(indexOfChild)//NOI18N
                        );

                        if (childParsedClass != null && !childrenParsed.contains(childParsedClass))
                            childrenParsed.add(childParsedClass);
                    }
                    mapOfClasses.put(patentClassParsed, childrenParsed);
                }
            }
        }
    }

    /**
     * Creates a kuwaiba's class hierarchy from the SNMP file
     *
     * @param deviceModel the device model
     * @param className_  the given class name
     * @param name        name of the element
     * @param descr       description of the element
     * @return equivalent kuwaiba's class
     */
    public String parseClass(String deviceModel, String className_, String name, String descr) {
        if (className_.isEmpty())
            return null;

        int classId = Integer.parseInt(className_);
        if (classId == 3 && (!name.isEmpty() && !descr.isEmpty())) //chassis
            return className;
        else if (deviceModel != null && classId == 10 && deviceModel.contains("2960"))
            return "ElectricalPort";
        else if (classId == 10) { //port
            if (name.toLowerCase().contains("usb") || descr.toLowerCase().contains("usb")) //NOI18N
                return "USBPort";//NOI18N
            else if (name.toLowerCase().contains("fastethernet") || name.toLowerCase().contains("mgmteth") ||
                    name.toLowerCase().contains("cpu") || name.toLowerCase().contains("control") ||
                    (descr.toLowerCase().contains("ethernet") && !descr.toLowerCase().contains("gigabit")) ||
                    descr.toLowerCase().contains("fast") || descr.toLowerCase().contains("management")) //NOI18N
                return "ElectricalPort";//NOI18N
            else if (!name.isEmpty() && !descr.isEmpty())
                return "OpticalPort";//NOI18N

        } else if (classId == 5) { //container
            if (!descr.contains("Disk")) //NOI18N
                return "Slot";//NOI18N

        } else if (classId == 6 && name.toLowerCase().contains("power") && !name.toLowerCase().contains("module")
                || classId == 6 && descr.toLowerCase().contains("power") || classId == 6 && descr.toLowerCase().contains("psu"))
            return "PowerPort";//NOI18N
        else if (classId == 6 && name.contains("Module")) //NOI18N
            return "HybridBoard"; //NOI18N
        else if (classId == 9) { //module
            //In Routers ASR9001 the Transceivers, sometimes have an empty desc
            if ((name.split("/").length > 3 || name.toLowerCase().contains("transceiver")
                    || descr.toLowerCase().contains("transceiver") || ((descr.toLowerCase().contains("sfp") && !name.toLowerCase().contains("card"))
                    || descr.toLowerCase().contains("xfp") || descr.toLowerCase().contains("cpak")
                    || descr.equalsIgnoreCase("ge t"))) && !name.toLowerCase().contains("spa") && !descr.toLowerCase().contains("spa"))
                return "Transceiver"; //NOI18N

            return "IPBoard"; //NOI18N
        } else if (classId == 1 && descr.contains("switch processor"))
            return "SwitchProcessor"; //NOI18N

        return null;
    }

    /**
     * Returns if the class is used or not
     *
     * @param classId_ the line of the SNMP to extract the className
     * @param descr    the line of the SNMP to extract name of the element
     * @return false if is a sensor, true in the most of the cases for now
     */
    private boolean isClassUsed(String classId_, String descr) {
        int classId = Integer.parseInt(classId_);
        //chassis(3) port(10) powerSupply(6) module(9) container(5)
        switch (classId) {
            case 3:
            case 10:
            case 6:
            case 9:
                return true;
            case 1:
                return descr.trim().toLowerCase().contains("switch processor");//NOI18N
            case 5:
                return !descr.trim().toLowerCase().contains("disk");//NOI18N
        }
        return false;
    }
    //</editor-fold>

    /**
     * Updates the hierarchy containment in Kuwaiba
     */
    private void loadClassHierarchy() {
        try {
            for (String parentClass : mapOfClasses.keySet()) {

                List<String> possibleChildrenToAdd = new ArrayList<>();
                List<ClassMetadataLight> currentPossibleChildren = mem.getPossibleChildren(parentClass, true);
                List<String> possibleChildren = mapOfClasses.get(parentClass);

                if (possibleChildren != null) {
                    //JsonArray children = Json.createArrayBuilder().build();
                    for (String possibleChildToAdd : possibleChildren) {
                        boolean isPossibleChild = false;
                        for (ClassMetadataLight currentPossibleClassName : currentPossibleChildren) {
                            if (possibleChildToAdd.equals(currentPossibleClassName.getName())) {
                                isPossibleChild = true;
                                break;
                            }
                        }

                        if (!isPossibleChild)
                            possibleChildrenToAdd.add(possibleChildToAdd);
                    }
                    if (!possibleChildrenToAdd.isEmpty()) {
                        mem.addPossibleChildren(parentClass, possibleChildrenToAdd.toArray(new String[possibleChildrenToAdd.size()]));
                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                String.format("%s are now children of class %s ", possibleChildrenToAdd, parentClass),
                                "The class hierarchy was updated"));
                    }
                }
            } //end for
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR, ex.getLocalizedMessage(),
                    "The class Hieararchy was not updated"));
        }
    }

    /**
     * Create into kuwaiba's objects the lines read it from the SNMP
     * it creates a branch every time it finds a port or an element with
     * no children
     * e.g. branch 1)
     * slot0/0[Slot]+
     * |_board0/0[Board]+
     * |_ port0/0/0[Port]
     * (end of branch)
     * -------------------------------------------------------------------------
     * branch 2)                              |_0/0/1[Port]
     * (end of branch)
     * -------------------------------------------------------------------------
     * branch 3)                              |_0/0/2[Port]
     * (end of branch)
     * -------------------------------------------------------------------------
     * branch 4)
     * slot0/1[Slot]+
     * |_board0/1[Board]
     * (end of branch)
     * -------------------------------------------------------------------------
     *
     * @param parentId        parent id
     * @param parentName      parent name
     * @param parentClassName parent class name
     * @throws MetadataObjectNotFoundException can not find the class name of the device related with the data source configuration.
     * @throws BusinessObjectNotFoundException the requested object(electrical, sftp, optical, virtual port or mpls tunnel) can't be found
     * @throws InvalidArgumentException        if some element of the list of syncDataSourceConfiguration has more paramNames than paramValues
     */
    private void checkObjects(String parentId, String parentName, String parentClassName)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        if (mapOfFile.isEmpty())
            throw new InvalidArgumentException("The router model you are trying to synchronize is not yet supported. Contact your administrator");

        if (parentId != null && parentId.equals(id))//If is the first element
            parentId = INITAL_ID;

        List<String> childrenIds = mapOfFile.get(parentId);
        if (childrenIds != null) {
            for (String childId : childrenIds) {

                int i = entityData.get("instance").indexOf(childId); //NOI18N
                parentId = entityData.get("entPhysicalContainedIn").get(i); //NOI18N
                if (parentClassName.equals(className)) //if is the chassis we must keep the id
                    parentId = id;

                String objectName = entityData.get("entPhysicalName").get(i); //NOI18N
                //We parse the class Id from SNMP into kuwaiba's class name
                String mappedClass = parseClass(
                        entityData.get("entPhysicalModelName").get(i),
                        entityData.get("entPhysicalClass").get(i),
                        objectName,
                        entityData.get("entPhysicalDescr").get(i)); //NOI18N
                //We standarized the port names
                if (!className.equals(mappedClass) && SyncUtil.isSynchronizable(objectName)
                        && mappedClass.toLowerCase().contains("port") && !objectName.contains("Power")
                        && !mappedClass.contains("Power"))
                    objectName = SyncUtil.normalizePortName(objectName);

                if (mappedClass == null) //it was impossible to parse the SNMP class into kuwaiba's class
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            String.format("The data has empty fields. Kuwaiba can not map entry: %s"
                                    , entityData.get("entPhysicalClass").get(i)), "the entry will not be synchronized")
                    );
                else {
                    HashMap<String, Object> newAttributes = createNewAttributes(mappedClass, i);
                    //This applies only for the chassis
                    if (className.contains(mappedClass)) {
                        newAttributes.remove("name"); //NOI18N the router name won't be changed
                        BusinessObject etnObject = bem.getObject(className, id);
                        HashMap<String, String> etnAttributes = (HashMap<String, String>) etnObject.getAttributes().entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                        HashMap<String, String> newdAttributes = (HashMap<String, String>) newAttributes.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                        HashMap<String, String> comparedAttributes = SyncUtil.compareAttributes(etnAttributes, newdAttributes);
                        if (!comparedAttributes.isEmpty()) {
                            comparedAttributes.put("name", etnObject.getAttributes().containsValue("name") ?
                                    (String) etnObject.getAttributes().get("name") : "");//we need to add the name as atribute again to show the name in the results
                            updateObjectAttributes(id, mappedClass, comparedAttributes);
                        }
                        //all the data except the chassis
                    } else {

                        newAttributes.put("parentId", parentId);
                        newAttributes.put("parentName", parentName);
                        newAttributes.put("parentClassName", parentClassName);

                        BusinessObject newObj = new BusinessObject(mappedClass, childId, (String) newAttributes.get(Constants.PROPERTY_NAME)
                                , newAttributes);

                        if (mappedClass.contains("Port") && !objectName.contains("Power") && !mappedClass.contains("Power"))
                            newPorts.add(newObj);

                        //check if is already created
                        isDeviceAlreadyCreated(newObj);
                        branch.add(newObj);

                    }
                    checkObjects(childId, objectName, mappedClass);

                    //End of a branch
                    if (((mapOfFile.get(childId) == null) || mappedClass.contains("Port")) && !branch.isEmpty()) {
                        //The is first time is trying to sync from SNMP
                        if (!isBranchAlreadyCreated(branch)) {
                            //Loaded from snmp first time
                            createBranch(branch);
                        }
                        branch = new ArrayList<>();
                    }
                }//end of each
            }
        }
    }

    /**
     * if is the second time that you are running the sync with the SNMP, this
     * method check the structure of the object and search for a given branch of elements.
     *
     * @param newBranchToEvalueate the attribute branch to evaluate
     * @return a RemoteBusinessObjectLight with the object if exists otherwise returns null
     * @throws InvalidArgumentException        if some element of the list of syncDataSourceConfiguration has more paramNames than paramValues
     * @throws MetadataObjectNotFoundException can not find the class name of the device related with the data source configuration.
     * @throws BusinessObjectNotFoundException the requested object(electrical, sftp, optical, virtual port or mpls tunnel) can't be found
     */
    private boolean isBranchAlreadyCreated(List<BusinessObject> newBranchToEvalueate) throws InvalidArgumentException,
            MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        List<List<BusinessObjectLight>> oldBranchesWithMatches = searchInOldStructure(newBranchToEvalueate);
        if (oldBranchesWithMatches == null)//we found the branch in the current structure, nothing else to do
            return true;
        else if (!oldBranchesWithMatches.isEmpty()) {//at least a part of the branch its already created
            int indexOfTheLargestsize = 0; //first we find the longest path, if there are more than one
            int sizeOfThelargestPath = oldBranchesWithMatches.get(0).size();
            for (int i = 1; i < oldBranchesWithMatches.size(); i++) {
                if (sizeOfThelargestPath < oldBranchesWithMatches.get(1).size()) {
                    sizeOfThelargestPath = oldBranchesWithMatches.get(1).size();
                    indexOfTheLargestsize = i;
                }
            }
            List<BusinessObjectLight> oldBranch = oldBranchesWithMatches.get(indexOfTheLargestsize);
            return partOfTheBranchMustBeCreated(oldBranch, newBranchToEvalueate);
        }
        return false; //if is empty all the branch should be created
    }

    private boolean partOfTheBranchMustBeCreated(List<BusinessObjectLight> oldBranch,
                                                 List<BusinessObject> newBranchToEvalueate) throws InvalidArgumentException,
            BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        //A new element to add we look for the parent id of the new element
        if (oldBranch.size() < newBranchToEvalueate.size()) {
            List<BusinessObject> newPartOfTheBranch = newBranchToEvalueate.subList(oldBranch.size(), newBranchToEvalueate.size());
            if (!newPartOfTheBranch.isEmpty()) {//search if the part exists
                List<List<BusinessObjectLight>> oldBranchesWithMatches = searchInOldStructure(newPartOfTheBranch);
                if (oldBranchesWithMatches != null && !oldBranchesWithMatches.isEmpty()) {//we found something
                    oldBranch.addAll(oldBranchesWithMatches.get(0));
                    return partOfTheBranchMustBeCreated(oldBranch, newBranchToEvalueate);
                }
                if (oldBranchesWithMatches == null)//we find the other part, the branch exists
                    return true;
                else if (oldBranchesWithMatches.isEmpty()) { //the new part of the branch doesn't exist, so we are going to create it a remove the part that exists
                    BusinessObjectLight oldObj = oldBranch.get(oldBranch.size() - 1);
                    //The last object found in the current structure and the new evaluated branch
                    BusinessObject currentObj = branch.get(oldBranch.size() - 1);
                    String currentObjClassName = currentObj.getClassName();
                    String currentObjName = (String) branch.get(oldBranch.size() - 1).getAttributes().get("name");
                    //new object
                    BusinessObject newObj = branch.get(oldBranch.size());
                    String objParentName = (String) newObj.getAttributes().get("parentName");
                    String objParentClassName = (String) newObj.getAttributes().get("parentClassName");
                    //We check again if the parenst match
                    if (currentObjName.equals(oldObj.getName()) && currentObjClassName.equals(oldObj.getClassName()) &&
                            oldObj.getName().equals(objParentName) && oldObj.getClassName().equals(objParentClassName)) {
                        newObj.getAttributes().put("deviceParentId", oldObj.getId());
                        if (newObj.getClassName().contains("Port"))
                            editNewPortWithDeviceParentId(newObj);
                        branch.set(oldBranch.size(), newObj);
                    }
                    //we remove the part of the branch that already exists, and update the attributes if is necessary
                    int matchesToDelete = 0;
                    for (int i = 0; i < oldBranch.size(); i++) {
                        oldObj = oldBranch.get(i);
                        BusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                        oldParent = SyncUtil.wrapPortName(oldParent);

                        HashMap<String, Object> oldAttributes = bem.getObject(oldObj.getClassName(), oldObj.getId()).getAttributes();
                        HashMap<String, String> olddAttributes = (HashMap<String, String>) oldAttributes.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

                        currentObj = branch.get(i);
                        currentObjClassName = currentObj.getClassName();
                        currentObjName = (String) branch.get(i).getAttributes().get("name");
                        objParentName = (String) currentObj.getAttributes().get("parentName");
                        objParentClassName = (String) currentObj.getAttributes().get("parentClassName");
                        if (oldParent.getClassName().equals(className) && objParentClassName.equals(className))
                            objParentName = oldParent.getName();

                        if (currentObjName.equals(oldObj.getName()) && currentObjClassName.equals(oldObj.getClassName()) &&
                                oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)) {
                            updateAttributesInBranch(oldObj, olddAttributes, currentObj);
                            matchesToDelete++;
                        }
                    }
                    if (matchesToDelete > 0) {
                        branch.subList(0, matchesToDelete).clear();
                    }

                    return false;
                }
            }
        } else {//something has been removed
            List<BusinessObjectLight> subOldBranchToRemove = oldBranch.subList(branch.size(), oldBranch.size() - 1);
            for (BusinessObjectLight removedObj : subOldBranchToRemove) {
                System.out.println("check this");
            }
            return true;
        }
        return true;
    }

    /**
     * Searches for a new branch in the current structure if it finds a part of the new branch in the old one
     *
     * @param newBranchToEvalueate branch to evaluate
     * @return list of new business object
     * @throws InvalidArgumentException        if some element of the list of syncDataSourceConfiguration has more paramNames than paramValues
     * @throws MetadataObjectNotFoundException can not find the class name of the device related with the data source configuration.
     * @throws BusinessObjectNotFoundException the requested object(electrical, sftp, optical, virtual port or mpls tunnel) can't be found
     */
    private List<List<BusinessObjectLight>> searchInOldStructure(List<BusinessObject> newBranchToEvalueate)
            throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        List<List<BusinessObjectLight>> oldBranchesWithMatches = new ArrayList<>();
        List<BusinessObjectLight> foundPath = new ArrayList<>();
        boolean hasBeenFound = false; //This is usded because some branch are in disorder

        for (long i : currentObjectStructure.keySet()) {
            foundPath = new ArrayList<>();
            List<BusinessObjectLight> oldBranch = currentObjectStructure.get(i);
            int end = oldBranch.size() > newBranchToEvalueate.size() ? newBranchToEvalueate.size() : oldBranch.size();
            for (int w = 0; w < end; w++) {
                String objClassName = newBranchToEvalueate.get(w).getClassName();
                String objParentName = (String) newBranchToEvalueate.get(w).getAttributes().get("parentName");
                String objParentClassName = (String) newBranchToEvalueate.get(w).getAttributes().get("parentClassName");
                String newObjName = (String) newBranchToEvalueate.get(w).getAttributes().get("name");

                if (!className.equals(objClassName)) {
                    BusinessObjectLight oldObj = oldBranch.get(w);
                    BusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                    oldParent = SyncUtil.wrapPortName(oldParent); //we standardize if the parent object is a port, important in ASR9001 hwere ports are not leafs in the structure

                    //The name of the Router will be diferent in most of the cases, so we must avoid the name in this case.
                    if (oldParent.getClassName().equals(className) && objParentClassName.equals(className))
                        objParentName = oldParent.getName();
                    if (oldObj.getName().equals(newObjName) && oldObj.getClassName().equals(objClassName)) {
                        if (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName))
                            foundPath.add(oldObj);
                    }
                }//end if not router
            }//we find the whole path
            if (foundPath.size() == newBranchToEvalueate.size()) {
                for (int j = 0; j < foundPath.size(); j++) {
                    BusinessObjectLight oldObj = foundPath.get(j);
                    HashMap<String, Object> oldAttributes = bem.getObject(oldObj.getClassName(), oldObj.getId()).getAttributes();
                    HashMap<String, String> olddAttributes = (HashMap<String, String>) oldAttributes.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                    BusinessObject newObj = newBranchToEvalueate.get(j); //this is the new object from SNMP

                    updateAttributesInBranch(oldObj, olddAttributes, newObj);//we check if some attributes need to be updated
                }
                return null; //we find the whole path, we return null
            }
            //Some paths exist but in order to find them we should look for them backwards in the branch, because the SNMP doesn't order the
            //first son in the same way of kuwaiba
            //e.g. from kuwaiba:                  form SNMP:
            //          Board0/2                   Board0/2
            //                 |_Gi0/0/2/0[Port]          |_Gi0/0/2/16[Port]<---the first son from SNMP is not in the same
            //current branch in kuwaiba:
            //slot0/2, Board0/2, Gi0/0/2/0[Port]
            //from SNMP:
            //slot0/2, Board0/2, Gi0/0/2/16[Port] <-- this branch exists (the first branch will always bring the parents)
            //                   Gi0/0/2/00[Port] <-- this too, but is not in the same order  (the other children don't bring the parents)
            if (foundPath.isEmpty()) {
                for (int n = newBranchToEvalueate.size(), o = oldBranch.size(); (n * o) > 0; n--, o--) {
                    String objClassName = newBranchToEvalueate.get(n - 1).getClassName();
                    String objParentName = (String) newBranchToEvalueate.get(n - 1).getAttributes().get("parentName");
                    String objParentClassName = (String) newBranchToEvalueate.get(n - 1).getAttributes().get("parentClassName");
                    String newObjName = (String) newBranchToEvalueate.get(n - 1).getAttributes().get("name");

                    if (!className.equals(objClassName)) {
                        BusinessObjectLight oldObj = oldBranch.get(o - 1);
                        BusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                        oldParent = SyncUtil.wrapPortName(oldParent); //we standardize if the parent object is a port, important in ASR9001 hwere ports are not leafs in the structure

                        //The Router name will be different in most cases, so we should avoid the name in this case.
                        if (oldParent.getClassName().equals(className) && objParentClassName.equals(className))
                            objParentName = oldParent.getName();
                        if (oldObj.getName().equals(newObjName) && oldObj.getClassName().equals(objClassName) && (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName))) {
                            foundPath.add(oldObj);
                            hasBeenFound = true;
                        }
                    }//end if not router
                }
                //we find the whole path, if the path si found backwards, the comparation of the attributes should be done it backwards too
                if (foundPath.size() == newBranchToEvalueate.size()) {
                    for (int j = foundPath.size() - 1, r = 0; j > -1; j--, r++) {
                        BusinessObjectLight oldObj = foundPath.get(j);
                        HashMap<String, Object> oldAttributes = bem.getObject(oldObj.getClassName(), oldObj.getId()).getAttributes();
                        HashMap<String, String> olddAttributes = (HashMap<String, String>) oldAttributes.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                        BusinessObject newObj = newBranchToEvalueate.get(r); //this is the new object from SNMP
                        updateAttributesInBranch(oldObj, olddAttributes, newObj);//we check if some attributes need to be updated
                    }
                    return null;
                }
            } else if (!foundPath.isEmpty())
                oldBranchesWithMatches.add(foundPath);
        }//end for
        if (foundPath.isEmpty()) {
            //we must search again to check if there are is a match between the last element of the old structure and the first element of the new branch
            for (long i : currentObjectStructure.keySet()) {
                List<BusinessObjectLight> oldBranch = currentObjectStructure.get(i);
                String objClassName = newBranchToEvalueate.get(0).getClassName();
                String objParentName = (String) newBranchToEvalueate.get(0).getAttributes().get("parentName");
                String objParentClassName = (String) newBranchToEvalueate.get(0).getAttributes().get("parentClassName");
                String newObjName = (String) newBranchToEvalueate.get(0).getAttributes().get("name");

                if (!className.equals(objClassName)) {
                    BusinessObjectLight oldObj = oldBranch.get(oldBranch.size() - 1);
                    BusinessObjectLight oldParent = bem.getParent(oldObj.getClassName(), oldObj.getId());
                    oldParent = SyncUtil.wrapPortName(oldParent);//we standardize if the parent object is a port, important in ASR9001 hwere ports are not leafs in the structure

                    //The Router name will be different in most cases, so we should avoid the name in this case.
                    if (oldParent.getClassName().equals(className) && objParentClassName.equals(className))
                        objParentName = oldParent.getName();
                    if (oldObj.getName().equals(newObjName) && oldObj.getClassName().equals(objClassName) && oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)) {
                        foundPath.add(oldObj);
                        break;
                    }
                }//end if not router
            }
        }
        if (!foundPath.isEmpty() && !oldBranchesWithMatches.contains(foundPath))
            oldBranchesWithMatches.add(foundPath);
            //we check if the branch starts in the first level, direct child of the chassis
        else if (!hasBeenFound && oldBranchesWithMatches.isEmpty()) {
            if (newBranchToEvalueate.get(0).getAttributes().get("parentClassName").equals(className)) {
                BusinessObject obj = newBranchToEvalueate.get(0);
                obj.getAttributes().put("deviceParentId", id);
                branch.set(0, obj);
                return new ArrayList<>();
            }
            //This is the less accurate method
            //we only check for the parent name of the first element of the new
            //branch with the last element of the old branch.
            for (long i : currentObjectStructure.keySet()) {
                List<BusinessObjectLight> oldBranch = currentObjectStructure.get(i);
                String objParentName = (String) newBranchToEvalueate.get(0).getAttributes().get("parentName");
                String objParentClassName = (String) newBranchToEvalueate.get(0).getAttributes().get("parentClassName");
                if (oldBranch.size() > 2) { //we only check old branches with more than one element, otherwise they are ports
                    BusinessObjectLight oldParent = oldBranch.get(oldBranch.size() - 2);
                    if (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)) {
                        BusinessObject obj = branch.get(0);
                        obj.getAttributes().put("deviceParentId", oldParent.getId());
                        if (obj.getClassName().contains("Port"))
                            editNewPortWithDeviceParentId(obj);
                        branch.set(0, obj);
                        break;
                    }
                }
            }
            //if the branch could not be found still its necessary to check again
            //in the old branch for the second element (an IPboard)
            for (long i : currentObjectStructure.keySet()) {
                List<BusinessObjectLight> oldBranch = currentObjectStructure.get(i);
                String objParentName = (String) newBranchToEvalueate.get(0).getAttributes().get("parentName");
                String objParentClassName = (String) newBranchToEvalueate.get(0).getAttributes().get("parentClassName");
                if (oldBranch.size() > 1) {
                    BusinessObjectLight oldParent = oldBranch.get(1);
                    if (oldParent.getName().equals(objParentName) && oldParent.getClassName().equals(objParentClassName)) {
                        BusinessObject obj = branch.get(0);
                        obj.getAttributes().put("deviceParentId", oldParent.getId());
                        if (obj.getClassName().contains("Port"))
                            editNewPortWithDeviceParentId(obj);
                        branch.set(0, obj);
                        break;
                    }
                }
            }
        }
        return oldBranchesWithMatches;
    }

    private void updateAttributesInBranch(BusinessObjectLight oldObj, HashMap<String, String> oldAttributes
            , BusinessObject newObj) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try {

            HashMap<String, Object> attributes = newObj.getAttributes();
            HashMap<String, String> dattributes = (HashMap<String, String>) attributes.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

            HashMap<String, String> attributeChanges = SyncUtil.compareAttributes(oldAttributes, dattributes);
            attributeChanges.remove("parentName");
            attributeChanges.remove("parentClassName");
            attributeChanges.remove("parentId");
            attributeChanges.remove("deviceParentId");
            if (!attributeChanges.isEmpty()) {
                bem.updateObject(oldObj.getClassName(), oldObj.getId(), attributeChanges);

                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                        String.format(ts.getTranslatedString("object_attributes_changed"), oldObj),
                        newObj.toString()));
            }
        } catch (OperationNotPermittedException ex) {

            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                    String.format(ts.getTranslatedString("object_attributes_changed"), oldObj),
                    ex.getLocalizedMessage()));
        }
    }

    /**
     * checks if the first level of children has changes
     *
     * @param newObj the new object
     * @return true if is already created
     */
    private boolean isDeviceAlreadyCreated(BusinessObject newObj) {
        BusinessObjectLight objFound = null;

        for (BusinessObjectLight currentFirstLevelChild : currentFirstLevelChildren) {
            if (currentFirstLevelChild.getClassName().equals(newObj.getClassName()) &&
                    currentFirstLevelChild.getName().equals(newObj.getAttributes().get("name"))) {
                objFound = currentFirstLevelChild;
                break;
            }
        }
        if (objFound != null)
            currentFirstLevelChildren.remove(objFound);
        return false;
    }

    /**
     * Reads the device's special children
     *
     * @param children special children
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws BusinessObjectNotFoundException If the object could not be found
     */
    private void readCurrentSpecialStructure(List<BusinessObjectLight> children)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        for (BusinessObjectLight child : children) {
            switch (child.getClassName()) {
                case Constants.CLASS_MPLSTUNNEL:
                    currentMplsTunnels.add(child);
                    break;
                case Constants.CLASS_VIRTUALPORT:
                    currentVirtualPorts.add(child);
                    break;
                case "ServiceInstance":
                    currentServiceInstances.add(child);
                    break;
            }
            readCurrentSpecialStructure(bem.getObjectSpecialChildren(child.getClassName(), child.getId()));
        }
    }

    /**
     * Reads the current object and make a copy of the structure, from this
     * structure the ports can be updated and moved to the new created tree in
     * order to keep the special relationships.
     *
     * @param objects the list of elements of a level
     * @throws MetadataObjectNotFoundException if something goes wrong with the class metadata
     * @throws BusinessObjectNotFoundException if some object can not be found
     */
    private void readCurrentDeviceStructure(List<BusinessObjectLight> objects)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        for (BusinessObjectLight object : objects) {
            if (!mem.isSubclassOf("GenericLogicalPort", object.getClassName()) && !mem.isSubclassOf("Pseudowire", object.getClassName())) {
                //We standarized the port names
                //object = SyncUtil.wrapPortName(object);
                tempAuxOldBranch.add(object);
            }

            if (object.getClassName().contains("Port") && !object.getClassName().contains("Channel") && !object.getClassName().contains("Virtual") && !object.getClassName().contains("Power"))
                currentPorts.add(object);

            else if (object.getClassName().contains("Virtual") || object.getClassName().equals(Constants.CLASS_PORTCHANNEL))
                currentVirtualPorts.add(object);

            List<BusinessObjectLight> children = bem.getObjectChildren(object.getClassName(), object.getId(), -1);
            if (!children.isEmpty())
                readCurrentDeviceStructure(children);
            else {
                if (!tempAuxOldBranch.isEmpty())
                    currentObjectStructure.put(k, tempAuxOldBranch);
                tempAuxOldBranch = new ArrayList<>();
                k++;
            }
        }
    }

    private void readCurrentFirstLevelChildren() throws InvalidArgumentException {
        try {
            currentFirstLevelChildren = bem.getObjectChildren(className, id, -1);
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Logger.getLogger(EntPhysicalSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void createBranch(List<BusinessObject> branch) {
        boolean segmentDependsOfPort = false;
        List<BusinessObject> extraBranch = new ArrayList<>();
        for (BusinessObject businessObject : branch) {
            String parentId = null;

            String tempParentId = (String) businessObject.getAttributes().get("parentId");
            if (businessObject.getAttributes().get("deviceParentId") != null)
                parentId = (String) businessObject.getAttributes().get("deviceParentId");
            //for transcivers
            if (segmentDependsOfPort || businessObject.getClassName().equals("OpticalPort")) {
                segmentDependsOfPort = true;
                extraBranch.add(businessObject);
                newCreatedPortsToCreate.put(businessObject.getId(), extraBranch);
            } else {
                if (businessObject.getAttributes().get("deviceParentId") == null)
                    parentId = createdIdsToMap.get(tempParentId) != null ? createdIdsToMap.get(tempParentId) : tempParentId;

                else//if we are updating a branch
                    createdIdsToMap.put(tempParentId, parentId);

                if (!businessObject.getClassName().contains("Port")
                        || (businessObject.getAttributes().containsKey("name")
                        && ((String) businessObject.getAttributes().get("name")).contains("Power"))
                        || businessObject.getClassName().contains("PowerPort")) {
                    try {
                        businessObject.getAttributes().remove("parentId");
                        businessObject.getAttributes().remove("parentName");
                        String parentClassName = (String) businessObject.getAttributes().remove("parentClassName");
                        businessObject.getAttributes().remove("deviceParentId");
                        HashMap<String, String> businessObjectAttribute = (HashMap<String, String>) businessObject.getAttributes().entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

                        String createdObjectId = bem.createObject(businessObject.getClassName(), parentClassName, parentId,
                                businessObjectAttribute, null);
                        createdIdsToMap.put(businessObject.getId(), createdObjectId);
                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                String.format("%s [%s]", businessObject.getName(), businessObject.getClassName()),
                                "Created successfully"));

                    } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException |
                             InvalidArgumentException | OperationNotPermittedException |
                             ApplicationObjectNotFoundException ex) {
                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                String.format("%s [%s]", businessObject.getName(), businessObject.getClassName()),
                                ex.getLocalizedMessage()));
                    }
                }
            }
        }
    }

    private void updateObjectAttributes(String deviceId, String deviceClassName,
                                        HashMap<String, String> newAttributes) {
        try {
            bem.updateObject(deviceClassName, deviceId, newAttributes);

            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                    String.format("This %s attributes were updated in the chassis", newAttributes),
                    "The attributes were updated"));

        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException |
                 InvalidArgumentException ex) {
            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                    ex.getLocalizedMessage(), "The attributes were not updated"));
        }
    }

    /**
     * Create a hash map for the attributes of the given index encathc of the data read it from SNMP
     *
     * @param index the index of the entry
     * @return a HashMap with attribute name(key) attribute value (value)
     * @throws MetadataObjectNotFoundException if the attributes we are trying to sync doesn't exist in the classmetadata
     */
    private HashMap<String, Object> createNewAttributes(String mappedClass, int index)
            throws MetadataObjectNotFoundException {
        HashMap<String, Object> attributes = new HashMap<>();
        ClassMetadata mappedClassMetadata = mem.getClass(mappedClass);

        String objectName = entityData.get("entPhysicalName").get(index);//NOI18N
        //We standarized the port names
        if (!className.equals(mappedClass) && SyncUtil.isSynchronizable(objectName) && mappedClass.toLowerCase().contains("port") && !objectName.contains("Power") && !mappedClass.contains("Power"))
            objectName = SyncUtil.normalizePortName(objectName);

        attributes.put("name", objectName);//NOI18N
        String description = entityData.get("entPhysicalDescr").get(index).trim();
        if (!description.isEmpty()) {
            if (mappedClassMetadata.getAttribute("description") == null)
                createAttributeError(mappedClass, "description", "String");

            attributes.put("description", description);
        }
        if (!entityData.get("entPhysicalMfgName").get(index).isEmpty()) {
            if (mappedClassMetadata.getAttribute("vendor") != null) {
                String vendor = findingListTypeId(index, "EquipmentVendor");//NOI18N
                if (vendor != null)
                    attributes.put("vendor", vendor); //NOI18N
            } else
                createAttributeError(mappedClass, "vendor", "EquipmentVendor");
        }
        if (!entityData.get("entPhysicalSerialNum").get(index).isEmpty()) {
            if (mappedClassMetadata.getAttribute("serialNumber") == null)
                createAttributeError(mappedClass, "serialNumber", "String");

            attributes.put("serialNumber", entityData.get("entPhysicalSerialNum").get(index).trim());//NOI18N
        }
        if (!entityData.get("entPhysicalModelName").get(index).isEmpty()) {
            if (mappedClass != null) {
                AttributeMetadata modelAttribute = mappedClassMetadata.getAttribute("model");
                if (modelAttribute != null) {
                    String model = findingListTypeId(index, modelAttribute.getType());//NOI18N
                    if (model != null)
                        attributes.put("model", model);//NOI18N
                } else
                    createAttributeError(mappedClass, "model", "EquipmentModel or any list type");
            }
        }
        return attributes;
    }

    public void createAttributeError(String aClass, String attributeName, String type) {
        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                String.format(ts.getTranslatedString("attribute_does_not_exist_in_class"), attributeName, type, aClass),
                Json.createObjectBuilder().add("type", "error")
                        .add("className", aClass)
                        .add("attributeType", type)
                        .add("attributeName", attributeName).build().toString()));
    }

    //Things to be deleted
    public void removeObjectFromDelete(BusinessObjectLight obj) {
        for (long branchId : currentObjectStructure.keySet())
            currentObjectStructure.get(branchId).remove(obj);
    }

    public void checkDataToBeDeleted() throws MetadataObjectNotFoundException, InvalidArgumentException {
        for (BusinessObjectLight currentChildFirstLevel : currentFirstLevelChildren) {
            if (!mem.isSubclassOf("Pseudowire", currentChildFirstLevel.getClassName()) &&
                    !currentChildFirstLevel.getName().equalsIgnoreCase("gi0") &&
                    !currentChildFirstLevel.getClassName().equals(Constants.CLASS_PORTCHANNEL)) {
                try {
                    bem.deleteObject(currentChildFirstLevel.getClassName(), currentChildFirstLevel.getId(), false);
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                            String.format("%s [%s]", currentChildFirstLevel.getName(), currentChildFirstLevel.getClassName()),
                            "Deleted successfully"));

                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                         OperationNotPermittedException ex) {
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            String.format("%s [%s] not deleted", currentChildFirstLevel.getName(), currentChildFirstLevel.getClassName()),
                            ex.getLocalizedMessage()));
                }
            }
        }

        for (long key : currentObjectStructure.keySet()) {
            List<BusinessObjectLight> branchToDelete = currentObjectStructure.get(key);
            for (BusinessObjectLight deviceToDelete : branchToDelete) {
                JsonObject jsont = Json.createObjectBuilder().add("type", "old_object_to_delete").build();
                if (deviceToDelete.getClassName().contains("Transceiver")) {
                    try {
                        BusinessObjectLight tParent = bem.getParent(deviceToDelete.getClassName(), deviceToDelete.getId());

                        if (tParent.getClassName().contains("Port")) {
                            bem.deleteObject(deviceToDelete.getClassName(), deviceToDelete.getId(), false);
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s [%s]", deviceToDelete.getName(), deviceToDelete.getClassName()),
                                    "Deleted successfully"));
                        }

                    } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                             InvalidArgumentException | OperationNotPermittedException ex) {
                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                                String.format("%s [%s] not deleted", deviceToDelete.getName(), deviceToDelete.getClassName()),
                                ex.getLocalizedMessage()));
                    }
                }
            }
        }
    }

    //<editor-fold desc="Ports" defaultstate="collapsed">

    /**
     * Create findings with the ports with no match
     */
    public void checkPortsWithNoMatch() {
        for (BusinessObjectLight oldPort : notMatchedPorts) {
            try {
                bem.deleteObject(oldPort.getClassName(), oldPort.getId(), false);
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                        String.format("%s [%s]", oldPort.getName(), oldPort.getClassName()),
                        "Deleted successfully"));
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                     OperationNotPermittedException | InvalidArgumentException ex) {
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                        String.format("%s [%s] not deleted", oldPort.getName(), oldPort.getClassName()),
                        ex.getLocalizedMessage()));
            }
        }

        for (BusinessObject newPort : newPorts) {
            String parentId;
            if (newPort.getAttributes().get("deviceParentId") != null)
                parentId = (String) newPort.getAttributes().get("deviceParentId");


            else
                parentId = createdIdsToMap.get(newPort.getAttributes().get("parentId"));


            if (parentId == null)
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                        String.format("%s", newPort),
                        "Can not determinate the parent id"));
            else {
                newPort.getAttributes().remove("deviceParentId");
                newPort.getAttributes().remove("parentId");
                String parentClassName = (String) newPort.getAttributes().remove("parentClassName");
                newPort.getAttributes().remove("parentName");

                try {
                    HashMap<String, String> newPortAttributes = (HashMap<String, String>) newPort.getAttributes().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

                    parentId = bem.createObject(newPort.getClassName(), parentClassName, parentId, newPortAttributes, null);
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                            String.format("%s", newPort),
                            "Port created"));
                    createdIdsToMap.put(newPort.getId(), parentId);
                    //this only applies for ASR9001 here you define the creation order of the ports-transceivers
                    List<BusinessObject> extraBranch = newCreatedPortsToCreate.get(newPort.getId());
                    if (extraBranch != null && extraBranch.size() > 1) {
                        for (int i = 1; i < extraBranch.size(); i++) {
                            String tempParentId = (String) extraBranch.get(i).getAttributes().get("parentId");

                            if (extraBranch.get(i).getAttributes().remove("deviceParentId") == null) ;
                            parentId = createdIdsToMap.get(tempParentId);

                            parentClassName = (String) extraBranch.get(i).getAttributes().remove("parentClassName");
                            extraBranch.get(i).getAttributes().remove("parentName");

                            HashMap<String, String> extraBranchAttributes = (HashMap<String, String>) extraBranch.get(i).getAttributes().entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                            parentId = bem.createObject(extraBranch.get(i).getClassName(),
                                    parentClassName, parentId, extraBranchAttributes, null);

                            createdIdsToMap.put(extraBranch.get(i).getId(), parentId);
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s", newPort),
                                    "Inventory object created"));
                        }
                    }

                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException |
                         OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            String.format("%s", newPort),
                            "Can not create port"));
                }
            }
        }
    }

    /**
     * Compare the old ports with the new ones, the new ports should have been
     * created in the last steps if they didn't exist, after the creation we compare
     * the names of the old ports with the names of the new ones, if a match is
     * found the old port will be moved to its new location, and the new
     * port won't be created, this way we keep the relationships of the old port
     */
    private void checkPortsToMigrate() {
        List<BusinessObject> foundNewPorts = new ArrayList<>(); //for debug
        for (BusinessObjectLight oldPort : currentPorts) {
            if (!oldPort.getName().equalsIgnoreCase("gi0")) {
                try {
                    //We copy the new attributes into the old port, to keep the relationships
                    BusinessObjectLight oldPortParent = bem.getParent(oldPort.getClassName(), oldPort.getId());
                    BusinessObject portFound = searchOldPortInNewPorts(oldPort);

                    if (portFound != null) {
                        String parentName = oldPortParent.getName();
                        foundNewPorts.add(portFound); //for debug
                        if (SyncUtil.isSynchronizable(portFound.getName()))
                            portFound = SyncUtil.wrapPortName(portFound);
                        //We found the port, but needs to be moved
                        if (!parentName.equals(portFound.getAttributes().get("parentName"))) {
                            portFound.setId(oldPort.getId());

                            String parentId = getParentPortIdIfExists(portFound);
                            if (parentId != null) //we check if the parent of the port is already created in an old structure
                                portFound.getAttributes().put("deviceParentId", parentId);
                            else {
                                portFound.getAttributes().remove("parentId");
                                portFound.getAttributes().remove("parentName");

                                portFound.getAttributes().remove("deviceParentId");
                                HashMap<String, String[]> objectsToMove = new HashMap<>();
                                String[] ids = {portFound.getId()};
                                objectsToMove.put(className, ids);

                                HashMap<String, String> portFoundAttributes = (HashMap<String, String>) portFound.getAttributes().entrySet().stream()
                                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                                bem.updateObject(portFound.getClassName(), portFound.getId(), portFoundAttributes);
                                bem.moveObjects(portFoundAttributes.get("parentClassName"), parentId, objectsToMove);

                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                        String.format("%s", portFound),
                                        "Moved to an updated position"));
                            }
                        } else {//The port is in the right place, but its attributes need to be updated.
                            HashMap<String, Object> oldAttributes = bem.getObject(oldPort.getClassName(), oldPort.getId()).getAttributes();
                            HashMap<String, String> oldPortAttributes = (HashMap<String, String>) oldAttributes.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

                            HashMap<String, String> portFoundAttributes = (HashMap<String, String>) portFound.getAttributes().entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                            HashMap<String, String> changedAttributes = SyncUtil.compareAttributes(oldPortAttributes, portFoundAttributes);
                            changedAttributes.remove("parentId");
                            changedAttributes.remove("parentClassName");
                            changedAttributes.remove("parentName");
                            changedAttributes.remove("deviceParentId");
                            if (!changedAttributes.isEmpty()) {
                                bem.updateObject(oldPort.getClassName(), oldPort.getId(), changedAttributes);
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                        String.format("From: %s to %s: in %s", oldAttributes, changedAttributes, oldPort),
                                        "Attributes updated"));
                            }
                        }
                    } else
                        notMatchedPorts.add(oldPort);

                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException |
                         OperationNotPermittedException | InvalidArgumentException ex) {
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            String.format("%s ", oldPort), "Checking port"));
                }
            }
        }//end for
        //we remove the ports from both lists the old port and the new ones
        //for debuging don't delete
//        for (BusinessObjectLight goodPort : foundOldPorts)
//            currentPorts.remove(goodPort);

//        we must delete de new ports that were found in the old structure, the
//        remaining new ports that were not found will be created
        for (BusinessObject foundNewPort : foundNewPorts) {
            int index = removeMatchedNewPorts(foundNewPort);
            if (index > -1)
                newPorts.remove(index);
        }
    }

    private int removeMatchedNewPorts(BusinessObject newportFound) {
        for (int i = 0; i < newPorts.size(); i++) {
            if (newPorts.get(i).getName()
                    .equals(newportFound.getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Search by name an old port in the list of the new created ports
     *
     * @param port_ the old port
     * @return a String pair key = The new parent Id (in the SNMP map) value = the new port
     */
    private boolean searchPortInNewPorts(BusinessObjectLight port_) {
        for (BusinessObject port : newPorts) {
            if (SyncUtil.compareLegacyPortNames(port_.getName(), port_.getClassName(),
                    port.getName(),
                    port.getClassName())
            )
                return true;
        }
        return false;
    }

    /**
     * Search by name an old port in the list of the new created ports
     *
     * @param oldPort the old port
     * @return a String pair key = The new parent Id (in the SNMP map) value = the new port
     * @throws InvalidArgumentException        if some element of the list of syncDataSourceConfiguration has more paramNames than paramValues
     * @throws BusinessObjectNotFoundException if some object can not be found
     * @throws MetadataObjectNotFoundException if something goes wrong with the class metadata
     */
    private BusinessObject searchOldPortInNewPorts(BusinessObjectLight oldPort)
            throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        for (BusinessObject port : newPorts) {
            if (SyncUtil.compareLegacyPortNames(oldPort.getName(), oldPort.getClassName(),
                    port.getName(),
                    port.getClassName())
            )
                return port;
        }
        return null;
    }

    private String getParentPortIdIfExists(BusinessObject portFound)
            throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        String objParentName = (String) portFound.getAttributes().get("parentName");
        String objParentClassName = (String) portFound.getAttributes().get("parentClassName");

        for (long key : currentObjectStructure.keySet()) {
            List<BusinessObjectLight> oldBranch = currentObjectStructure.get(key);
            for (BusinessObjectLight oldObj : oldBranch) {
                HashMap<String, Object> oldAttributes = bem.getObject(oldObj.getClassName(), oldObj.getId()).getAttributes();

                if (oldAttributes.get(Constants.PROPERTY_NAME).equals(objParentName)
                        && objParentClassName.equals(oldObj.getClassName()))
                    return oldObj.getId();
            }
        }
        return null;
    }

    private void editNewPortWithDeviceParentId(BusinessObject newPortWithDeviceParentId) {
        for (int i = 0; i < newPorts.size(); i++) {
            if (newPortWithDeviceParentId.getAttributes().get("name").equals(newPorts.get(i).getAttributes().get("name")) &&
                    newPortWithDeviceParentId.getClassName().equals(newPorts.get(i).getClassName())) {
                newPorts.set(i, newPortWithDeviceParentId);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="List Types" defaultstate="collapsed">

    /**
     * Returns the listTypeId if exists or creates a finding to create the list-type
     * in case that the list type doesn't exist in Kuwaiba
     * <p>
     * JSon structure
     * {
     * type: listType
     * name: "the new list type name"
     * }
     *
     * @param i the index of the list type in the SNMP data
     * @return the list type (if exists, otherwise is an empty String)
     */
    private String findingListTypeId(int i, String listTypeClassName) {
        String SNMPoid, listTypeId = null;
        if (listTypeClassName.equals("EquipmentVendor")) //NOI18N
            SNMPoid = "entPhysicalMfgName"; //NOI18N
        else
            SNMPoid = "entPhysicalModelName"; //NOI18N

        if (!entityData.get(SNMPoid).get(i).isEmpty()) {
            try {
                String listTypeNameFromSNMP = entityData.get(SNMPoid).get(i);

                String id_ = matchListTypeNames(listTypeNameFromSNMP, listTypeClassName);
                if (id_ != null)
                    listTypeId = id_;
                else {//The list type doesn't exist, we create a finding
                    if (!listTypeEvaluated.contains(listTypeNameFromSNMP)) {
                        String createListTypeItem = aem.createListTypeItem(listTypeClassName, listTypeNameFromSNMP.trim(), listTypeNameFromSNMP.trim());
                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                String.format("the list type %s", listTypeNameFromSNMP),
                                "A list type was created"));

                        listTypeEvaluated.add(listTypeNameFromSNMP);
                        return createListTypeItem;
                    }
                }
            } catch (MetadataObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException ex) {
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                        String.format("the list type %s", listTypeClassName),
                        "A list type was not created"));
            }
        }
        return listTypeId;
    }

    /**
     * Compare the names from the SNMP in order to find one that match with a
     * created list types in Kuwaiba
     *
     * @param listTypeNameToLoad the list type name
     * @return the kuwaiba's list type item id
     * @throws MetadataObjectNotFoundException if the list type doesn't exist
     * @throws InvalidArgumentException        if the class name provided is not a list type
     */
    private String matchListTypeNames(String listTypeNameToLoad, String listTypeClassName)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(listTypeClassName);
        List<String> onlyNameListtypes = new ArrayList<>();
        for (BusinessObjectLight createdLitType : listTypeItems)
            onlyNameListtypes.add(createdLitType.getName());

        if (onlyNameListtypes.contains(listTypeNameToLoad))
            return listTypeItems.get(onlyNameListtypes.indexOf(listTypeNameToLoad)).getId();

        for (BusinessObjectLight createdLitType : listTypeItems) {
            int matches = 0;
            int maxLength = listTypeNameToLoad.length() > createdLitType.getName().length() ? listTypeNameToLoad.length() : createdLitType.getName().length();
            listTypeNameToLoad = listTypeNameToLoad.toLowerCase().trim();
            String nameCreatedInKuwaiba = createdLitType.getName().toLowerCase().trim();
            if (listTypeNameToLoad.equals(nameCreatedInKuwaiba)) {
                return createdLitType.getId();
            }
            for (int i = 1; i < maxLength; i++) {
                String a, b;
                if (listTypeNameToLoad.length() < i)
                    break;
                else
                    a = listTypeNameToLoad.substring(i - 1, i);
                if (nameCreatedInKuwaiba.length() < i)
                    break;
                else
                    b = nameCreatedInKuwaiba.substring(i - 1, i);
                if (a.equals(b))
                    matches++;
            }
            if (matches == listTypeNameToLoad.length())
                return createdLitType.getId();
        }
        return null;
    }
    //</editor-fold>

    private void syncIfMibData() {
        List<String> services = ifXTable.get("ifAlias"); //NOI18N
        List<String> portNames = ifXTable.get("ifName"); //NOI18N
        List<String> portSpeeds = ifXTable.get("ifHighSpeed"); //NOI18N

        for (String ifName : portNames) { //if name is the individual port names
            String ifAlias = services.get(portNames.indexOf(ifName)); //service name
            String portSpeed = portSpeeds.get(portNames.indexOf(ifName));

            HashMap<String, String> attributes = new HashMap<>();
            attributes.put(Constants.PROPERTY_NAME, SyncUtil.normalizePortName(ifName));
            attributes.put("highSpeed", portSpeed);  //NOI18N
            if (!ifAlias.isEmpty())
                attributes.put("ifAlias", ifAlias);

            String createdId;
            try {
                //We must create the Mngmnt Port, virtualPorts, tunnels and Loopbacks
                if (SyncUtil.isSynchronizable(ifName)) {
                    BusinessObjectLight currentInterface;
                    BusinessObjectLight currentLogicalInterface = null;
                    //First we search the interfaces in the current structure
                    //We must add the s when we look for po ports because posx/x/x ports has no s in the if mib
                    currentInterface = searchInCurrentStructure(SyncUtil.normalizePortName(ifName), 1);
                    if (currentInterface == null && !ifName.contains("."))//maybe it is a PortChannel a virtual port with no . in the name just a Po1
                        currentInterface = searchInCurrentStructure(SyncUtil.normalizePortName(ifName), 2);

                    //Virtual Ports
                    if (ifName.contains(".") && !ifName.toLowerCase().contains(".si.")) {
                        currentInterface = searchInCurrentStructure(SyncUtil.normalizePortName(ifName.split("\\.")[0]), 1);
                        //it is possible than the service instance was created with the whole name, not only the numeric part
                        currentLogicalInterface = searchInCurrentStructure(ifName, 4);
                        if (currentInterface != null && currentLogicalInterface == null) {
                            List<BusinessObjectLight> virtualPorts = bem.getObjectChildren(currentInterface.getClassName(), currentInterface.getId(), -1);
                            for (BusinessObjectLight virtualPort : virtualPorts) {
                                if (virtualPort.getName().equals(SyncUtil.normalizePortName(ifName.split("\\.")[1]))) {
                                    currentLogicalInterface = virtualPort;
                                    break;
                                }
                            }
                        }
                    }//Service Instances
                    else if (ifName.contains(".") && ifName.toLowerCase().contains(".si.")) {
                        currentInterface = searchInCurrentStructure(SyncUtil.normalizePortName(ifName.split("\\.")[0]), 1);
                        if (currentInterface == null)//for port channels
                            currentInterface = searchInCurrentStructure(SyncUtil.normalizePortName(ifName.split("\\.")[0]), 2);
                        //it is possible than the virtual port was created with the whole name, not only the numeric part
                        currentLogicalInterface = searchInCurrentStructure(ifName, 4);
                        if (currentInterface != null && currentLogicalInterface == null) {
                            List<BusinessObjectLight> virtualPorts = bem.getObjectChildren(currentInterface.getClassName(), currentInterface.getId(), -1);
                            for (BusinessObjectLight virtualPort : virtualPorts) {
                                if (virtualPort.getName().equals(SyncUtil.normalizePortName(ifName.split("\\.")[2]))) {
                                    currentLogicalInterface = virtualPort;
                                    break;
                                }
                            }
                        }
                    }//Loopback
                    else if (ifName.toLowerCase().contains("lo")) //NOI18N
                        currentLogicalInterface = searchInCurrentStructure(SyncUtil.normalizePortName(ifName), 2);
                        //MPLS Tunnel
                    else if (ifName.toLowerCase().contains("tu")) //NOI18N
                        currentLogicalInterface = searchInCurrentStructure(SyncUtil.normalizePortName(ifName), 3);

                    if (currentInterface == null && (ifName.equalsIgnoreCase("gi0") || ifName.startsWith("Po")
                            || ifName.toLowerCase().contains("se"))) {
                        if (ifName.equalsIgnoreCase("gi0")) {
                            createdId = bem.createObject(Constants.CLASS_ELECTRICALPORT, className, id, attributes, null);
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s [%s]", attributes.get(Constants.PROPERTY_NAME), Constants.CLASS_ELECTRICALPORT),
                                    "Inventory object created"));
                            checkServices(ifAlias, ifName, createdId, Constants.CLASS_ELECTRICALPORT);
                            currentPorts.add(new BusinessObjectLight(Constants.CLASS_ELECTRICALPORT, createdId, attributes.get(Constants.PROPERTY_NAME)));
                        } else if (ifName.startsWith("Po") && ifName.length() < 4) { //port channel
                            attributes.put(Constants.PROPERTY_NAME, ifName.split("\\.")[0]);
                            createdId = bem.createObject("PortChannel", className, id, attributes, null);
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s [%s]", attributes.get(Constants.PROPERTY_NAME), "PortChannel"),
                                    "Inventory object created"));
                            checkServices(ifAlias, ifName.split("\\.")[0], createdId, "PortChannel");
                            currentVirtualPorts.add(new BusinessObjectLight("PortChannel", createdId, attributes.get(Constants.PROPERTY_NAME)));
                        } else if (ifName.toLowerCase().contains("se")) {
                            createdId = bem.createObject(Constants.CLASS_SERIALPORT, className, id, attributes, null);
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s [%s]", attributes.get(Constants.PROPERTY_NAME), Constants.CLASS_SERIALPORT),
                                    "Inventory object created"));
                            checkServices(ifAlias, ifName, createdId, Constants.CLASS_SERIALPORT);
                            currentPorts.add(new BusinessObjectLight(Constants.CLASS_SERIALPORT, createdId, attributes.get(Constants.PROPERTY_NAME)));
                        }
                    }//logical interfaces special children of the device
                    if (currentLogicalInterface == null && (ifName.toLowerCase().contains("tu") || (ifName.toLowerCase().contains("lo")))) {
                        if (ifName.toLowerCase().contains("tu")) { //MPLSTunnel
                            createdId = bem.createSpecialObject(Constants.CLASS_MPLSTUNNEL, className, id, attributes, null);
                            currentMplsTunnels.add(new BusinessObject(Constants.CLASS_MPLSTUNNEL, createdId, ifName));
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s [%s]", attributes.get(Constants.PROPERTY_NAME), Constants.CLASS_MPLSTUNNEL),
                                    "Inventory object created"));
                            checkServices(ifAlias, ifName, createdId, Constants.CLASS_MPLSTUNNEL);
                        }//LoopBacks
                        else if (ifName.toLowerCase().contains("lo")) {
                            createdId = bem.createSpecialObject(Constants.CLASS_VIRTUALPORT, className, id, attributes, null);
                            currentVirtualPorts.add(new BusinessObject(Constants.CLASS_VIRTUALPORT, createdId, ifName));
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s [%s]", attributes.get(Constants.PROPERTY_NAME), Constants.CLASS_VIRTUALPORT),
                                    "Inventory object created"));
                            checkServices(ifAlias, ifName, createdId, Constants.CLASS_VIRTUALPORT);
                        }
                    }
                    //logical interfaces virtual ports and service instances with physical port as parent
                    if (currentInterface != null && currentLogicalInterface == null) {
                        if (ifName.toLowerCase().contains(".si")) {
                            attributes.put(Constants.PROPERTY_NAME, ifName.split("\\.")[2]);
                            createdId = bem.createObject(Constants.CLASS_SERVICE_INSTANCE, currentInterface.getClassName(), currentInterface.getId(), attributes, null);
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s [%s], with parent: %s ", attributes.get(Constants.PROPERTY_NAME), Constants.CLASS_SERVICE_INSTANCE, currentInterface),
                                    "Inventory object created"));
                            checkServices(ifAlias, ifName, createdId, Constants.CLASS_SERVICE_INSTANCE);
                            currentServiceInstances.add(new BusinessObject(Constants.CLASS_SERVICE_INSTANCE, createdId, attributes.get(Constants.PROPERTY_NAME)));
                        } else if (ifName.toLowerCase().contains(".")) {
                            attributes.put(Constants.PROPERTY_NAME, ifName.split("\\.")[1]);
                            createdId = bem.createObject(Constants.CLASS_VIRTUALPORT, currentInterface.getClassName(), currentInterface.getId(), attributes, null);
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                    String.format("%s [%s], with parent: %s ", attributes.get(Constants.PROPERTY_NAME), Constants.CLASS_VIRTUALPORT, currentInterface),
                                    "Inventory object created"));
                            checkServices(ifAlias, ifName, createdId, Constants.CLASS_VIRTUALPORT);
                            currentServiceInstances.add(new BusinessObject(Constants.CLASS_VIRTUALPORT, createdId, attributes.get(Constants.PROPERTY_NAME)));
                        }
                    }
                    //we Update attributes, for now only high speed
                    if (currentInterface != null || ((ifName.contains("\\.") && currentLogicalInterface != null && currentInterface != null)
                            || (currentInterface == null && (ifName.toLowerCase().contains("tu") || ifName.toLowerCase().contains("lo"))))) {
                        BusinessObjectLight interfaceToUpdate;
                        if (currentInterface != null && !ifName.contains("."))
                            interfaceToUpdate = currentInterface;
                        else
                            interfaceToUpdate = currentLogicalInterface;
                        //we save the virtual ports and tunnels to recheck what should be deleted
                        if (currentLogicalInterface != null && currentLogicalInterface.getClassName().equals(Constants.CLASS_VIRTUALPORT))
                            foundVirtualPorts.add(currentLogicalInterface);
                        else if (currentLogicalInterface != null && currentLogicalInterface.getClassName().equals(Constants.CLASS_MPLSTUNNEL))
                            foundMplsTunnels.add(currentLogicalInterface);

                        if (interfaceToUpdate != null) {
                            HashMap<String, Object> currentAttributes = bem.getObject(interfaceToUpdate.getClassName(), interfaceToUpdate.getId()).getAttributes();
                            attributes = new HashMap<>();
                            //We must update the speedPort
                            String currentHighSpeed = (String) currentAttributes.get("highSpeed");
                            if (currentHighSpeed == null || !currentHighSpeed.equals(portSpeed)) {
                                attributes.put("highSpeed", portSpeed);
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                        String.format("The attribute highSpeed was updated from: %s to %s", currentHighSpeed, portSpeed),
                                        String.format("highSpeed updated in interface: %s", interfaceToUpdate)));
                            }//We must check if the ifAlias must be updated
                            String currentiIfAlias = (String) currentAttributes.get("ifAlias");
                            if (!ifAlias.isEmpty() && (currentiIfAlias == null || !currentiIfAlias.equals(ifAlias))) {
                                attributes.put("ifAlias", ifAlias);
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                        String.format("The attribute ifAlias was updated from: %s to %s", currentiIfAlias, ifAlias),
                                        String.format("ifAlias updated in interface: %s", interfaceToUpdate)));
                            }
                            bem.updateObject(interfaceToUpdate.getClassName(), interfaceToUpdate.getId(), attributes);
                            if (!ifAlias.isEmpty())
                                checkServices(ifAlias, ifName, interfaceToUpdate.getId(), interfaceToUpdate.getClassName());
                            //we update the name for taking only the numeric part
                            //for virtual ports
                            if (currentLogicalInterface != null && ifName.contains(".") && currentLogicalInterface.getName().contains(".")) {
                                attributes.put(Constants.PROPERTY_NAME, SyncUtil.normalizePortName(ifName));
                                bem.updateObject(currentLogicalInterface.getClassName(), currentLogicalInterface.getId(), attributes);
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                        String.format("The attribute name was updated from: %s to %s", currentAttributes.get(Constants.PROPERTY_NAME), attributes.get(Constants.PROPERTY_NAME)),
                                        String.format("name updated in interface: %s", currentLogicalInterface)));
                            }//a service instance
                            else if (currentLogicalInterface != null && ifName.contains(".") && ifName.contains(".si") && ifName.split("\\.").length == 3 && currentLogicalInterface.getName().contains(".")) {
                                attributes.put(Constants.PROPERTY_NAME, SyncUtil.normalizePortName(ifName));
                                bem.updateObject(currentLogicalInterface.getClassName(), currentLogicalInterface.getId(), attributes);
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                        String.format("The attribute name was updated from: %s to %s", currentAttributes.get(Constants.PROPERTY_NAME), attributes.get(Constants.PROPERTY_NAME)),
                                        String.format("name updated in interface: %s", currentLogicalInterface)));
                            }//The name should be normalized applies for loopbacks
                            else if (currentLogicalInterface != null && ifName.toLowerCase().contains("lo") && currentLogicalInterface.getName().length() < 5) {
                                attributes.put(Constants.PROPERTY_NAME, SyncUtil.normalizePortName(ifName));
                                bem.updateObject(currentLogicalInterface.getClassName(), currentLogicalInterface.getId(), attributes);
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                        String.format("The attribute name was updated from: %s to %s", currentAttributes.get(Constants.PROPERTY_NAME), attributes.get(Constants.PROPERTY_NAME)),
                                        String.format("name updated in Loopback: %s", currentLogicalInterface)));
                            }//The name should be normalized applies for mplsTunnels
                            else if (currentLogicalInterface != null && ifName.toLowerCase().contains("tu") && currentLogicalInterface.getName().length() < 5) {
                                attributes.put(Constants.PROPERTY_NAME, SyncUtil.normalizePortName(ifName));
                                bem.updateObject(currentLogicalInterface.getClassName(), currentLogicalInterface.getId(), attributes);
                                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                        String.format("The attribute name was updated from: %s to %s", currentAttributes.get(Constants.PROPERTY_NAME), attributes.get(Constants.PROPERTY_NAME)),
                                        String.format("name updated in MPLSTunnel: %s", currentLogicalInterface)));
                            }
                        }
                    }
                }//end for ifNames
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException |
                     OperationNotPermittedException | InvalidArgumentException |
                     ApplicationObjectNotFoundException ex) {
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                        String.format("Creating interface %s", ifName),
                        ex.getLocalizedMessage()));
            }
        }//the not found things
        for (BusinessObjectLight foundVirtualPort : foundVirtualPorts)
            currentVirtualPorts.remove(foundVirtualPort);

        for (BusinessObjectLight foundVMplsTunnel : foundMplsTunnels)
            currentMplsTunnels.remove(foundVMplsTunnel);

        for (BusinessObjectLight currentVirtualPort : currentVirtualPorts) {
            if (!currentVirtualPort.getClassName().equals(Constants.CLASS_PORTCHANNEL)) {
                try {
                    bem.deleteObject(currentVirtualPort.getClassName(), currentVirtualPort.getId(), false);
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            String.format("Deleting interface %s", currentVirtualPort),
                            "The interface was deleted because no math was found"));
                } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                         OperationNotPermittedException | InvalidArgumentException ex) {
                    results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                            String.format("Deleting virtual interface %s", currentVirtualPort),
                            ex.getLocalizedMessage()));
                }
            }
        }

        for (BusinessObjectLight currentMplesTunnel : currentMplsTunnels) {
            try {
                bem.deleteObject(currentMplesTunnel.getClassName(), currentMplesTunnel.getId(), false);
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                        String.format("Deleting interface %s", currentMplesTunnel),
                        "The interface was deleted because no math was found"));
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException |
                     OperationNotPermittedException | InvalidArgumentException ex) {
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                        String.format("Deleting virtual interface %s", currentMplesTunnel),
                        ex.getLocalizedMessage()));
            }
        }

    }

    /**
     * Checks if a given service name exists in kuwaiba in order to
     * associate the resource read it form the if-mib
     *
     * @param serviceName   the service read it from the  if-mib
     * @param portId        the port of the resource created
     * @param portClassName the class name of the port read it from the if-mib
     */
    private void checkServices(String serviceName, String portName, String portId, String portClassName) {
        try {
            List<BusinessObjectLight> servicesCreatedInKuwaiba = new ArrayList<>();
            //We get the services created in kuwaiba
            List<InventoryObjectPool> serviceRoot = bem.getRootPools(Constants.CLASS_GENERICCUSTOMER, 2, false);
            for (InventoryObjectPool customerPool : serviceRoot) {
                //TelecoOperators
                List<BusinessObjectLight> poolItems = bem.getPoolItems(customerPool.getId(), -1);
                for (BusinessObjectLight telecoOperator : poolItems) {
                    List<InventoryObjectPool> poolsInObject = bem.getPoolsInObject(telecoOperator.getClassName()
                            , telecoOperator.getId(), "GenericService");
                    //Service Pool
                    for (Pool servicePool : poolsInObject) {
                        List<BusinessObjectLight> actualServices = bem.getPoolItems(servicePool.getId(), -1);
                        servicesCreatedInKuwaiba.addAll(actualServices);
                    }
                }
            } //Now we check the resources with the given serviceName or ifAlias
            boolean related = false;
            for (BusinessObjectLight currentService : servicesCreatedInKuwaiba) {
                //The service is al ready created in kuwaiba
                if (serviceName.equals(currentService.getName())) {
                    List<BusinessObjectLight> serviceResources = bem.getSpecialAttribute(currentService.getClassName()
                            , currentService.getId(), "uses");
                    for (BusinessObjectLight resource : serviceResources) {
                        if (resource.getId() != null && resource.getId().equals(portId)) { //The port is already a resource of the service
                            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_INFORMATION,
                                    String.format("Interface %s [%s] and service %s", portName, portClassName
                                            , currentService.getName()), "Are already related"));
                            related = true;
                            break;
                        }
                    }
                    if (!related) {
                        bem.createSpecialRelationship(currentService.getClassName(), currentService.getId(), portClassName
                                , portId, "uses", true);
                        related = true;
                        results.add(new SyncResult(dsConfigId, SyncResult.TYPE_SUCCESS,
                                String.format("Interface %s [%s] and service %s", portName, portClassName, currentService.getName()),
                                "were related successfully"));
                    }
                }
            }
            if (!related)
                results.add(new SyncResult(dsConfigId, SyncResult.TYPE_WARNING,
                        "Searching service", String.format("The service: %s Not found, the interface: %s will not be related"
                        , serviceName, portName)));

        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | OperationNotPermittedException |
                 ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            results.add(new SyncResult(dsConfigId, SyncResult.TYPE_ERROR,
                    String.format("Serching service %s, related with interface: %s ", serviceName, portName),
                    String.format("due to: %s ", ex.getLocalizedMessage())));
        }
    }

    /**
     * Checks if a given port exists in the current structure, we search for
     * the wrappedifName and the ifName
     *
     * @param interfaceName a given name for port, virtual port or MPLS Tunnel
     * @param type          1 port, 2 virtual port, 3 MPLSTunnel, 4 bdi, 5 VLAN
     * @return the object, null doesn't exist in the current structure
     */
    private BusinessObjectLight searchInCurrentStructure(String interfaceName, int type) {
        switch (type) {
            case 1:
                for (BusinessObjectLight currentPort : currentPorts) {
                    if (currentPort.getName().equals(interfaceName))
                        return currentPort;
                }
                break;
            case 2:
                for (BusinessObjectLight currentVirtualPort : currentVirtualPorts) {
                    if (currentVirtualPort.getName().equals(interfaceName))
                        return currentVirtualPort;
                }
                break;
            case 3:
                for (BusinessObjectLight currentMPLSTunnel : currentMplsTunnels) {
                    if (currentMPLSTunnel.getName().equals(interfaceName))
                        return currentMPLSTunnel;
                }
                break;
            case 4:
                for (BusinessObjectLight currentVirtualPort : currentServiceInstances) {
                    if (currentVirtualPort.getName().equals(interfaceName))
                        return currentVirtualPort;
                }
                break;
        }
        return null;
    }
}
