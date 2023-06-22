/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.core.services.threading;

import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;

/**
 * A class containing metadata information about a job, such as its creation/end 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ManagedJobDescriptor {
    /**
    * The job is stopped. 
    */
   public static final int STATE_CREATED = 0;
   /**
    * The job is in progress. 
    */
   public static final int STATE_RUNNING = 1;
   /**
    * The job has been scheduled to start later. 
    */
   public static final int STATE_SCHEDULED = 2;
   /**
    * The job ended successfully. 
    */
   public static final int STATE_END_SUCCESS = 3;
   /**
    * The job ended with error. 
    */
   public static final int STATE_END_ERROR = 4;
   /**
    * The job stopped killed by the user. 
    */
   public static final int STATE_END_KILLED = 5;
   /**
    * UUID to uniquely identify the job. This id will be used by the consumers of the service to 
    * keep track of the execution of each job polling constantly to inquire for the current state.
    */
   private String id;
   /**
    * The creation date in milliseconds.
    */
   private long creationTime;
   /**
    * The creation date in milliseconds.
    */
   private long endTime;
   /**
    * The user that launched the job.
    */
   private UserProfileLight user;
   /**
    * Type of result. See STATE_XX for possible values.
    */
   private int state;
   /**
    * Current job progress as a percentage.
    */
   private float progress;
   /**
    * A short description of what this job is supposed to do.
    */
   private String description;
   
   public ManagedJobDescriptor(UserProfileLight user) {
       this.id = UUID.randomUUID().toString();
       this.creationTime = Calendar.getInstance().getTimeInMillis();
       this.state = STATE_CREATED;
       this.user = user;
   }

   public String getId() {
       return this.id;
   }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public UserProfileLight getUser() {
        return user;
    }

    public void setUser(UserProfileLight user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

   public int getState() {
       return this.state;
   }

   public void setState(int state) {
       this.state = state;
   }

   public float getProgress() {
       return progress;
   }

   public void setProgress(float progress) {
       this.progress = progress;
   }

   @Override
   public String toString() {
       return this.id;
   }

   @Override
   public boolean equals(Object obj) {
       return obj instanceof ManagedJobDescriptor ? ((ManagedJobDescriptor)obj).getId().equals(this.id): false;
   }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        return hash;
    }
}
