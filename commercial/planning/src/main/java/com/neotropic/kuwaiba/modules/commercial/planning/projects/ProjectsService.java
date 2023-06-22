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

package com.neotropic.kuwaiba.modules.commercial.planning.projects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The service that provides the actual functionality exposed by this module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Service
public class ProjectsService {
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
     * The BusinessEntityManagerImpl instance
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The ApplicationEntityManager instance
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Relationship project to object
     */
    public static String RELATIONSHIP_PROJECTSPROJECTUSES = "projectsProjectUses";
    
    public ProjectsService() { }
    
    /**
     * Create a project
     * @param projectPoolId The project pool id.
     * @param projectClass The project class. Must be subclass of GenericProject.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param userName The user name of the session.
     * @return The id of the newly created project.
     * @throws MetadataObjectNotFoundException If the project class can't be found.
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found.
     * @throws InvalidArgumentException If any of the attributes or its type is invalid.
     * @throws BusinessObjectNotFoundException If the requested project can't be found.
     */
    public String createProject(String projectPoolId, String projectClass, HashMap<String, String> attributes, String userName) 
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new MetadataObjectNotFoundException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), projectClass, Constants.CLASS_GENERICPROJECT));

        String projectId = bem.createPoolItem(projectPoolId, projectClass, attributes, "");

        String projectName = getProject(projectClass, projectId).getName();
        aem.createGeneralActivityLogEntry(userName,
                 ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.project.new-project-created-log")
                         , projectName, projectId, projectClass));

        return projectId;
    }
    
    /**
     * Get project properties
     * @param projectClass The project class. Must be subclass of GenericProject.
     * @param projectId The project id.
     * @return The project properties.
     * @throws MetadataObjectNotFoundException If the project class can't be found.
     * @throws InvalidArgumentException If the project id can't be found.
     * @throws BusinessObjectNotFoundException If the requested project can't be found.
     */
    public BusinessObject getProject(String projectClass, String projectId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), projectClass, Constants.CLASS_GENERICPROJECT));

        BusinessObject businessObject = (BusinessObject) bem.getObject(projectClass, projectId);
        if (businessObject == null)
                throw new BusinessObjectNotFoundException(String.format(ts.getTranslatedString("module.projects.project-id-not-found"), projectId));
              
        return businessObject;
    }
    
    /**
     * Deletes a project and delete its association with the related inventory objects. These objects will remain untouched.
     * @param projectClass The project class.
     * @param projectId The project id.
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the project class can't be found.
     * @throws InvalidArgumentException If the project id can't be found.
     * @throws BusinessObjectNotFoundException If the requested project can't be found.
     * @throws OperationNotPermittedException If the project can't be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws ApplicationObjectNotFoundException If the project activity log can't be found.
     */
    public void deleteProject(String projectClass, String projectId, String userName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException,
             ApplicationObjectNotFoundException, OperationNotPermittedException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), projectClass, Constants.CLASS_GENERICPROJECT));

        BusinessObject project = (BusinessObject) getProject(projectClass, projectId);
        if (project == null)
            throw new BusinessObjectNotFoundException(String.format(ts.getTranslatedString("module.projects.project-id-not-found"), projectId));
        
        bem.deleteObject(projectClass, projectId, true);
        
        aem.createGeneralActivityLogEntry(userName,
                 ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.project.delete-project-deleted-log")
                         , project.getName(), project.getId(), project.getClassName()));
    }
        
    /**
     * Updates one or many project attributes.
     * @param projectClass The project class.
     * @param projectId The project id.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the requested project can't be found.
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the project is blocked.
     * @throws InvalidArgumentException If any of the initial attributes can't be mapped.
     * @throws MetadataObjectNotFoundException If the project class can't be found.
     * @throws ApplicationObjectNotFoundException If the project activity log can't be found.
     */
    public void updateProject(String projectClass, String projectId, HashMap<String,String> attributes, String userName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException
            , OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), projectClass, Constants.CLASS_GENERICPROJECT));

        BusinessObject project = (BusinessObject) getProject(projectClass, projectId);
        if (project == null)
            throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.projects.project-id-not-found"), projectId));
        
        bem.updateObject(projectClass, projectId, attributes);
        
        aem.createGeneralActivityLogEntry(userName,
                 ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.project.update-project-updated-log")
                         , project.getId(), project.getClassName()));
    }

    /**
     * Creates a project pool.
     * @param poolName The pool name. Must be subclass of GenericProject.
     * @param poolDescription The pool description.
     * @param poolClass The pool class. What kind of objects can this pool contain?
     * @param userName The user name of the session.
     * @return The id of the newly created project pool.
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericProject. 
     * @throws ApplicationObjectNotFoundException If the pool activity log can't be found.
     */
    public String createProjectPool(String poolName, String poolDescription, String poolClass, String userName)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICPROJECT));
         String poolId = aem.createRootPool(poolName, poolDescription, poolClass, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.pool.new-pool-created-log")
                        , poolName, poolId, poolClass));
        
        return poolId;
    }
    
    /**
     * Gets the project pools properties.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @return The pool properties.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericProject.
     */
    public InventoryObjectPool getProjectPool(String poolId, String poolClass)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICPROJECT));

        return bem.getPool(poolId);
    }
        
    /**
     * Updates the attributes of a project pool.
     * @param poolId The id of the pool to be updated.
     * @param poolClass The pool class.
     * @param poolName The attribute value for pool name.
     * @param poolDescription The attribute value for pool description.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws InvalidArgumentException If an unknown attribute name is provided.
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericProject.
     */
    public void updateProjectPool(String poolId, String poolClass, String poolName, String poolDescription, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICPROJECT));
        
        aem.setPoolProperties(poolId, poolName, poolDescription);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.pool.update-pool-updated-log"), poolId, poolClass));
    }
  /**
     * Deletes a project pool.
     * @param poolId The pool id.
     * @param poolClass The pool class.
     * @param userName The user name of the session.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws OperationNotPermittedException If any of the objects in the pool can't be deleted because it's not a business related instance (it's more a security restriction).
     * @throws MetadataObjectNotFoundException If poolClass is not a valid subclass of GenericProject.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public void deleteProjectPool(String poolId, String poolClass, String userName)
            throws ApplicationObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, poolClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), poolClass, Constants.CLASS_GENERICPROJECT));
            
        InventoryObjectPool pool = getProjectPool(poolId, poolClass);
        
        String[] projectPoolId = {poolId};
        aem.deletePools(projectPoolId);
        
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.pool.delete-pool-deleted-log")
                        , pool.getName(), pool.getId(), pool.getClassName()));
    }
    
    /**
     * Retrieves the projects pool list.
     * @return The available project pools.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid
     */
    public List<InventoryObjectPool> getProjectPools() throws InvalidArgumentException {
        return bem.getRootPools(Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, true);
    }
    
    /** 
     * Gets the projects inside a project pool
     * @param poolId The pool id.
     * @param limit The results limit per page. Use -1 to retrieve all
     * @return The projects list.
     * @throws ApplicationObjectNotFoundException If the pool can't be found.
     * @throws InvalidArgumentException If the pool id is null or the result pool does not have uuid.
     */
    public List<BusinessObjectLight> getProjectsInPool(String poolId, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        return bem.getPoolItems(poolId, limit);
    }
       
    /**
     * Get all projects, without filters.
     * @param page Page number of results to skip. Use -1 to retrieve all
     * @param limit Max number of results per page. Use -1 to retrieve all
     * @return The projects list. 
     * @throws InvalidArgumentException If any project node could not be mapped into a Java object.
     * @throws MetadataObjectNotFoundException If the provided class name doesn't exists.
     */
    public List<BusinessObjectLight> getAllProjects(long page, long limit)
            throws InvalidArgumentException, MetadataObjectNotFoundException {
        return bem.getObjectsOfClassLight(Constants.CLASS_GENERICPROJECT, page, limit);
    }
   
    /**
     * Get the projects according to the filter value.
     * @param filters Map of filters key: attribute name, value: attribute value.
     * @param page Page or number of elements to skip.
     * @param limit Max count of child per page.
     * @return The projects list. 
     * @throws InvalidArgumentException If any project node could not be mapped into a Java object.
     * @throws MetadataObjectNotFoundException If the provided class name doesn't exists.
     */
    public List<BusinessObjectLight> getProjectsWithFilter(HashMap <String, String> filters, long page, long limit)
            throws InvalidArgumentException, MetadataObjectNotFoundException {
        return bem.getObjectsOfClassLight(Constants.CLASS_GENERICPROJECT, filters, page, limit);
    }
   
    /**
     * Relates a set of objects to a project.
     * @param projectClass The project class.
     * @param projectId The project id.
     * @param objectClass The object class.
     * @param objectId The object Id.
     * @param userName The user name of the session.
     * @throws InvalidArgumentException If the project is not subclass of GenericProject.
     * @throws ArraySizeMismatchException If array sizes of objectClass and objectId are not the same.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object).
     * @throws MetadataObjectNotFoundException If any of the classes provided can't be found.
     * @throws ApplicationObjectNotFoundException If the object activity log can't be found.
     */
    public void relateObjectsToProject(String projectClass, String projectId, String[] objectClass, String[] objectId, String userName)
            throws InvalidArgumentException, ArraySizeMismatchException, BusinessObjectNotFoundException
            , OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), projectClass, Constants.CLASS_GENERICPROJECT));
        
        if (objectClass.length != objectId.length)
            throw new ArraySizeMismatchException("objectClass", "objectId");
        
        for (int i = 0; i < objectId.length; i++) 
            bem.createSpecialRelationship(projectClass, projectId, objectClass[i], objectId[i], RELATIONSHIP_PROJECTSPROJECTUSES, true);
        
        aem.createObjectActivityLogEntry(userName, projectClass, projectId,
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, RELATIONSHIP_PROJECTSPROJECTUSES, "", "",
                String.format(ts.getTranslatedString("module.projects.actions.relate-objects-to-project.relationship-log")));
    }
        
    /**
     * Relates an object to a project
     * @param projectClass The project class.
     * @param projectId The project id.
     * @param objectClass The object class.
     * @param objectId The object Id.
     * @param userName The user name of the session.
     * @throws InvalidArgumentException If the project is not subclass of GenericProject.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws OperationNotPermittedException If any of the objects involved can't be connected (i.e. if it's not an inventory object).
     * @throws MetadataObjectNotFoundException If the object class provided can't be found.
     * @throws ApplicationObjectNotFoundException If the object activity log can't be found.
     */
    public void relateObjectToProject(String projectClass, String projectId, String objectClass, String objectId, String userName) throws 
        InvalidArgumentException, BusinessObjectNotFoundException, OperationNotPermittedException, 
        MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), projectClass, Constants.CLASS_GENERICPROJECT));
        
        bem.createSpecialRelationship(projectClass, projectId, objectClass, objectId, RELATIONSHIP_PROJECTSPROJECTUSES, true);
        
        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, "",
                RELATIONSHIP_PROJECTSPROJECTUSES, String.format(ts.getTranslatedString("module.projects.actions.relate-object-to-project.relationship-log"), objectId, projectId));
    }

    /**
     * Releases an object from project.
     * @param objectClass The object class.
     * @param objectId The object Id.
     * @param projectClass The project class.
     * @param projectId The project id.
     * @param userName The user name of the session.
     * @throws InvalidArgumentException If the project is not subclass of GenericProject.
     * @throws BusinessObjectNotFoundException If any of the objects can't be found.
     * @throws MetadataObjectNotFoundException If the object class provided can't be found.
     * @throws ApplicationObjectNotFoundException If the object activity log can't be found.
     */
    public void releaseObjectFromProject(String objectClass, String objectId, String projectClass, String projectId, String userName)
            throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , projectClass, Constants.CLASS_GENERICPROJECT));
        
        bem.releaseSpecialRelationship(objectClass, objectId, projectId, RELATIONSHIP_PROJECTSPROJECTUSES);
        
        aem.createObjectActivityLogEntry(userName, objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, RELATIONSHIP_PROJECTSPROJECTUSES
                , "", String.format(ts.getTranslatedString("module.projects.actions.release-object-from-project.relationship-log"), objectId, projectId));
    }
    
    /**
     * Gets the projects related to an object.
     * @param objectClass The object class.
     * @param objectId The object Id.
     * @return The list projects related to an object.
     * @throws BusinessObjectNotFoundException If the object can't be found.
     * @throws MetadataObjectNotFoundException If the object class provided can't be found.
     * @throws InvalidArgumentException If the object id is null or the result object does not have uuid.
     */
    public List<BusinessObjectLight> getProjectsRelatedToObject(String objectClass, String objectId) 
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        return bem.getSpecialAttribute(objectClass, objectId, RELATIONSHIP_PROJECTSPROJECTUSES);
    }
    
    /**
     * Gets the resources related to a project.
     * @param projectClass Project class.
     * @param projectId Project Id.
     * @return The project resources list.
     * @throws InvalidArgumentException If the project is not subclass of GenericProject.
     * @throws BusinessObjectNotFoundException If the project can't be found.
     * @throws MetadataObjectNotFoundException If the project class provided can't be found.
     */
    public List<BusinessObjectLight> getProjectResources(String projectClass, String projectId) throws 
        InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , projectClass, Constants.CLASS_GENERICPROJECT));
        
        return bem.getSpecialAttribute(projectClass, projectId, RELATIONSHIP_PROJECTSPROJECTUSES);
    }
        
    /**
     * Gets the activities inside a project.
     * @param projectClass The project class.
     * @param projectId The project Id.
     * @return The activities list.
     * @throws InvalidArgumentException If the project is not subclass of GenericProject.
     * @throws MetadataObjectNotFoundException If the project class provided can't be found.
     * @throws BusinessObjectNotFoundException If the project can't be found.
     */
    public List<BusinessObjectLight> getProjectActivities(String projectClass, String projectId) 
        throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , projectClass, Constants.CLASS_GENERICPROJECT));
        
        List<BusinessObjectLight> children = bem.getObjectSpecialChildren(projectClass, projectId);
        List<BusinessObjectLight> activities = new ArrayList();
        
        for (BusinessObjectLight child : children) {
            if (mem.isSubclassOf(Constants.CLASS_GENERICACTIVITY, child.getClassName()))
                activities.add(child);
        }
        return activities;
    }
    
    /**
     * Creates an Activity inside a project.
     * @param projectId The project id.
     * @param projectClass The project class.
     * @param activityClass The activity class. Must be subclass of GenericActivity.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param userName The user name of the session.
     * @return The id of the newly created activity.
     * @throws MetadataObjectNotFoundException If any of the object classes can't be found.
     * @throws BusinessObjectNotFoundException If the project id can't be found.
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue.
     * @throws ApplicationObjectNotFoundException If the specified template can't be found.
     */
    public String createActivity(String projectId, String projectClass, String activityClass, HashMap<String, String> attributes, String userName)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException
            , OperationNotPermittedException, ApplicationObjectNotFoundException {
                     
        if (!mem.isSubclassOf(Constants.CLASS_GENERICACTIVITY, activityClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), activityClass, Constants.CLASS_GENERICACTIVITY));
        
        String activityId = bem.createSpecialObject(activityClass, projectClass, projectId, attributes, null);
        
        BusinessObject activity = getActivity(activityClass, activityId);
        
        aem.createGeneralActivityLogEntry(userName,
                 ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.activity.new-activity-created-log")
                         , activity.getName(), activity.getId(), activity.getClassName()));
    
        return activityId;
    }
    
    /**
     * Get activity properties
     * @param activityClass The activity class. Must be subclass of GenericActivity.
     * @param activityId The activity id.
     * @return The activity properties.
     * @throws MetadataObjectNotFoundException If the activity class can't be found.
     * @throws InvalidArgumentException If the activity id can't be found. 
     * @throws BusinessObjectNotFoundException If the requested activity can't be found. 
     */
    public BusinessObject getActivity(String activityClass, String activityId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException {

        if (!mem.isSubclassOf(Constants.CLASS_GENERICACTIVITY, activityClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), activityClass, Constants.CLASS_GENERICACTIVITY));
        
        BusinessObject businessObject = (BusinessObject) bem.getObject(activityClass, activityId);
        if (businessObject == null)
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.projects.activity-id-not-found"), activityId));

        return businessObject;
    }
    
    /** 
     * Updates one or many activity attributes.
     * @param activityClass The activity class. Must be subclass of GenericActivity.
     * @param activityId The activity id.
     * @param attributes The set of initial attributes. If no attribute name is specified, an empty string will be used.
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the activity class can't be found.
     * @throws InvalidArgumentException If the activity id can't be found.
     * @throws BusinessObjectNotFoundException If the requested activity can't be found.
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the activity is blocked.
     * @throws ApplicationObjectNotFoundException If the object activity log can't be found.
     */
    public void updateActivity(String activityClass, String activityId, HashMap<String,String> attributes, String userName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException
            , OperationNotPermittedException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICACTIVITY, activityClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), activityClass, Constants.CLASS_GENERICACTIVITY));
        
        BusinessObject activity = (BusinessObject) getActivity(activityClass, activityId);
        if (activity == null)
            throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.projects.activity-id-not-found"), activityId));
        
        bem.updateObject(activityClass, activityId, attributes);
        
        aem.createGeneralActivityLogEntry(userName,
                 ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.activity.update-activity-updated-log")
                         , activity.getId(), activity.getClassName()));
    }
        
    /**
     * Deletes an activity and delete its association with the related inventory objects. These objects will remain untouched.
     * @param activityClass The activity class. Must be subclass of GenericActivity.
     * @param activityId The activity id.
     * @param releaseRelationships Release of existing relationships.
     * @param userName The user name of the session.
     * @throws BusinessObjectNotFoundException If the requested activity can't be found.
     * @throws MetadataObjectNotFoundException If the activity class can't be found.
     * @throws InvalidArgumentException If the activity id can't be found.
     * @throws OperationNotPermittedException If the activity could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     * @throws ApplicationObjectNotFoundException If the object activity log can't be found.
     */
    public void deleteActivity(String activityClass, String activityId, boolean releaseRelationships, String userName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException
            , InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (!mem.isSubclassOf(Constants.CLASS_GENERICACTIVITY, activityClass))
            throw new MetadataObjectNotFoundException(String.format(
                    ts.getTranslatedString("module.general.messages.is-not-subclass"), activityClass, Constants.CLASS_GENERICACTIVITY));
        
        BusinessObject activity = getActivity(activityClass, activityId);

        bem.deleteObject(activityClass, activityId, releaseRelationships);

        aem.createGeneralActivityLogEntry(userName,
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.activity.delete-activity.deleted-log")
                        , activity.getName(), activity.getId(), activity.getClassName()));
    }
    
    /**
     * Creates a copy of a project.
     * @param poolId The pool id.
     * @param projectClass The project class.
     * @param projectId The project id.
     * @param userName The user name of the session.
     * @return The newly created project id.
     * @throws ApplicationObjectNotFoundException If the pool node can not be found.
     * @throws InvalidArgumentException If the project can not be copy to the selected pool.
     * @throws BusinessObjectNotFoundException If the project can not be found.
     * @throws MetadataObjectNotFoundException If the project class name can no be found.
     */
    public String copyProjectToPool(String poolId, String projectClass, String projectId, String userName)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        String projectOid = bem.copyPoolItem(poolId, projectClass, projectId, true);
                
        List<BusinessObjectLight>  activities = getProjectActivities(projectClass, projectId);
        if (!activities.isEmpty()) {
            HashMap<String, List<String>> objects = new HashMap();
            activities.forEach(activity -> objects.put(activity.getClassName(), Arrays.asList(activity.getId())));
            bem.copySpecialObjects(projectClass, projectOid, objects, true);
        }
 
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT
                , String.format(ts.getTranslatedString("module.projects.actions.copy-object-to-pool.copied-log"), projectOid, poolId));
        
        return projectOid;
    }
    
    /**
     * Moves a project from a pool to another pool.
     * @param poolId The pool id.
     * @param projectClass The project class.
     * @param projectId The project id.
     * @param userName The user name of the session.
     * @throws MetadataObjectNotFoundException If the project class name can no be found.
     * @throws InvalidArgumentException If the project can not be move to the selected pool.
     * @throws ApplicationObjectNotFoundException If the pool node can not be found.
     * @throws BusinessObjectNotFoundException If the project can not be found.
     */
    public void moveProjectToPool(String poolId, String projectClass, String projectId, String userName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException {
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass")
                    , Constants.CLASS_GENERICPROJECT, projectClass));
        
        bem.movePoolItem(poolId, projectClass, projectId);
        aem.createGeneralActivityLogEntry(userName, ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT
                , String.format(ts.getTranslatedString("module.projects.actions.move-object-to-pool.moved-log"), projectId, poolId));
    }
}