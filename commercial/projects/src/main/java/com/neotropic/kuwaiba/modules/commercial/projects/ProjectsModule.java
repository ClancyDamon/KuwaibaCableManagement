/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractCommercialModule;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ArraySizeMismatchException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemotePool;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Project management module
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProjectsModule extends AbstractCommercialModule {
    
    /**
    * translation service
    */
    @Autowired
    private TranslationService ts;
    /**
     * The MetadataEntityManager instance
     */
    private MetadataEntityManager mem;
    /**
     * The ApplicationEntityManager instance
     */
    private ApplicationEntityManager aem;
    /**
     * The BusinessEntityManager instance
     */
    private BusinessEntityManager bem;
    /**
     * Relationship project to object
     */
    public static String RELATIONSHIP_PROJECTSPROJECTUSES = "projectsProjectUses";
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.projects.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.projects.description");
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public int getCategory() {
        return CATEGORY_PLANNING;
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_PERPETUAL_LICENSE;
    }

        
    /**
     * Gets the project pools
     * @return The list of project pools
     * @throws InvalidArgumentException If any of the required data model components necessary to process the request could not be found.
     */
    public List<RemotePool> getProjectPools() throws InvalidArgumentException {
        List<InventoryObjectPool> projectPools = bem.getRootPools(Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT, true);
        List<RemotePool> remoteProjPools = new ArrayList();
        
        for (InventoryObjectPool projectPool : projectPools)
            remoteProjPools.add(new RemotePool(projectPool));
        
        return remoteProjPools;
    }
    
    /** Gets the project in a Project pool 
     * @param poolId Project pool id
     * @param limit Max result number, -1 without limit
     * @return The list of projects
     * @throws ApplicationObjectNotFoundException If the Project pool is not found
     * @throws InvalidArgumentException If the pool does not have uuid
     */
    public List<BusinessObjectLight> getProjectsInProjectPool(String poolId, int limit) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        return bem.getPoolItems(poolId, limit);
    }
        
    /**
     * Adds a Project
     * @param parentId Parent Id
     * @param parentClassName Parent class name
     * @param className Class name
     * @param attributeNames Attribute names
     * @param attributeValues Attribute values
     * @return The Project id
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws MetadataObjectNotFoundException If the class name could not be found
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public String addProject(String parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, ArraySizeMismatchException, MetadataObjectNotFoundException  {

        bem.getPool(parentId);
        Map<String, String> attributes = IntStream.range(0, attributeNames.length).boxed()
             .collect(Collectors.toMap(i -> attributeNames[i], i -> attributeValues[i]));
        return bem.createPoolItem(parentId, className, new HashMap(attributes), null);
    }
        
    /**
     * Deletes a Project
     * @param className Class name
     * @param oid Object id
     * @param releaseRelationships Release relationships
     * @throws BusinessObjectNotFoundException If the object couldn't be found
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships
     * @throws InvalidArgumentException If the project does not have uudi
     */
    public void deleteProject(String className, String oid, boolean releaseRelationships) throws 
        BusinessObjectNotFoundException, MetadataObjectNotFoundException, 
        OperationNotPermittedException, InvalidArgumentException {
        
        bem.deleteObject(className, oid, releaseRelationships);
    }
            
    /**
     * Adds an Activity.
     * @param parentId Parent Id
     * @param parentClassName Parent class name
     * @param className Class name
     * @param attributeNames Attribute names
     * @param attributeValues Attribute values
     * @return The Activity id
     * @throws MetadataObjectNotFoundException If the object's class can't be found
     * @throws BusinessObjectNotFoundException If the parent id is not found
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws ApplicationObjectNotFoundException If the specified template could not be found
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     */
    public String addActivity(String parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues) throws 
        MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, 
        OperationNotPermittedException, ApplicationObjectNotFoundException, ArraySizeMismatchException {
        
        
        HashMap<String, String> attributes = new HashMap<>();
        
        if (attributeNames != null && attributeValues != null) {
            
            if (attributeNames.length != attributeValues.length)
                throw new ArraySizeMismatchException("attributeNames", "attributeValues");
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);
        }
        return bem.createSpecialObject(className, parentClassName, parentId, attributes, null);
    }
    
    /**
     * Deletes an activity
     * @param className Class name
     * @param oid Object id
     * @param releaseRelationships Release relationships
     * @throws BusinessObjectNotFoundException If the object couldn't be found
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships
     * @throws InvalidArgumentException If the activity does not have uuid
     */
    public void deleteActivity(String className, String oid, boolean releaseRelationships) throws 
        BusinessObjectNotFoundException, MetadataObjectNotFoundException, 
        OperationNotPermittedException, InvalidArgumentException {
        
        bem.deleteObject(className, oid, releaseRelationships);
    }
    
    /**
     * Gets the resources associates with an Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @return The list of project resources
     * @throws InvalidArgumentException  If the project is not subclass of GenericProject
     * @throws BusinessObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    public List<BusinessObjectLight> getProjectResurces(String projectClass, String projectId) throws 
        InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        return bem.getSpecialAttribute(projectClass, projectId, RELATIONSHIP_PROJECTSPROJECTUSES);
    }
        
    /**
     * Gets the activities associates to an Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @return The list of Activities
     * @throws InvalidArgumentException If the project is not subclass of GenericProject
     * @throws MetadataObjectNotFoundException If the project class is not found
     * @throws BusinessObjectNotFoundException If the project is not found
     */
    public List<BusinessObjectLight> getProjectActivities(String projectClass, String projectId) 
        throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        List<BusinessObjectLight> children = bem.getObjectSpecialChildren(projectClass, projectId);
        List<BusinessObjectLight> activities = new ArrayList();
        
        for (BusinessObjectLight child : children) {
            if (mem.isSubclassOf(Constants.CLASS_GENERICACTIVITY, child.getClassName()))
                activities.add(child);
        }
        return activities;
    }
            
    /**
     * Associates objects to a Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @param objectClass Object class
     * @param objectId Object Id
     * @throws InvalidArgumentException If the project is not subclass of GenericProject
     * @throws ArraySizeMismatchException if array sizes of objectClass and objectId are not the same
     * @throws BusinessObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException
     */
    public void associateObjectsToProject(String projectClass, String projectId, String[] objectClass, String[] objectId) throws 
        InvalidArgumentException, ArraySizeMismatchException, BusinessObjectNotFoundException, 
        OperationNotPermittedException, MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        if (objectClass.length != objectId.length)
            throw new ArraySizeMismatchException("objectClass", "objectId");
        
        for (int i = 0; i < objectId.length; i++) 
            bem.createSpecialRelationship(projectClass, projectId, objectClass[i], objectId[i], RELATIONSHIP_PROJECTSPROJECTUSES, true);
    }
        
    /**
     * Associates an object to a Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @param objectClass Object class
     * @param objectId Object id
     * @throws InvalidArgumentException If the project is not subclass of GenericProject
     * @throws BusinessObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException
     */
    public void associateObjectToProject(String projectClass, String projectId, String objectClass, String objectId) throws 
        InvalidArgumentException, BusinessObjectNotFoundException, OperationNotPermittedException, 
        MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        bem.createSpecialRelationship(projectClass, projectId, objectClass, objectId, RELATIONSHIP_PROJECTSPROJECTUSES, true);
    }
        
    /**
     * Releases an object from Project
     * @param objectClass Object class
     * @param objectId Object id
     * @param projectClass Project class
     * @param projectId Project id
     * @throws InvalidArgumentException If the project is not subclass of GenericProject
     * @throws BusinessObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    public void releaseObjectFromProject(String objectClass, String objectId, String projectClass, String projectId) throws 
        InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        bem.releaseSpecialRelationship(objectClass, objectId, projectId, RELATIONSHIP_PROJECTSPROJECTUSES);
    }
    
    /**
     * Gets the project associate to an object
     * @param objectClass Object class
     * @param objectId Object Id
     * @return The list of projects
     * @throws BusinessObjectNotFoundException If the project is no found
     * @throws MetadataObjectNotFoundException If the project class is no found
     * @throws InvalidArgumentException If the object does not have uuid
     */
    public List<BusinessObjectLight> getProjectsAssociateToObject(String objectClass, String objectId) 
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        return bem.getSpecialAttribute(objectClass, objectId, RELATIONSHIP_PROJECTSPROJECTUSES);
    }
    
    /**
     * Creates a Project Pool
     * @param name Project Pool name
     * @param description Project Pool description
     * @param instanceOfClass Project Pool class
     * @return The new Project Pool id
     * @throws MetadataObjectNotFoundException If he project pool class is no found
     */
    public String createProjectPool(String name, String description, String instanceOfClass) throws MetadataObjectNotFoundException {
        return aem.createRootPool(name, description, instanceOfClass, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT);
    }
                    
    @Override
    public void configureModule(MetadataEntityManager mem, ApplicationEntityManager aem,  BusinessEntityManager bem) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_PROJECTSPROJECTUSES, "Resource associated to this project");
    }

    @Override
    public void validate() throws OperationNotPermittedException { }

    @Override
    public String getId() {
        return "projects";}

}
