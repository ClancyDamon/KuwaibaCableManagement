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
 * 
 */
package org.neotropic.kuwaiba.northbound.ws.model.application;

import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper of BackgroundJob
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteBackgroundJob {
    /**
     * The execution id used to identify the current instance within the local job manager
     */
    private long id;
    /**
     * The unique string that allows the system to create an instance of the job (see META-INF.batch-jobs)
     */
    private String jobTag;
    /**
     * The percentage of progress
     */
    private int progress;
    /**
     * Allow or not multiple instances of this job
     */
    private boolean allowConcurrence;
    /**
     * What's the current status of the job
     */
    private String status;
    /**
     * The job result. This field will be set once the job is finished
     */
    private Object jobResult;
    /**
     * Used to catch the exception thrown during the batch job execution
     */
    /*
    private Exception exceptionThrownByTheJob;
    */
    
    private long startTime;
    
    private long endTime;

    public RemoteBackgroundJob(long id, String jobTag, int progress, boolean allowConcurrence, String status, Date startTime, Date endTime) {
        this.id = id;
        this.jobTag = jobTag;
        this.progress = progress;
        this.allowConcurrence = allowConcurrence;
        this.status = status;
        this.startTime = startTime != null ? startTime.getTime() : -1;
        this.endTime = endTime != null ? endTime.getTime() : -1;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJobTag() {
        return jobTag;
    }

    public void setJobTag(String jobTag) {
        this.jobTag = jobTag;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isAllowConcurrence() {
        return allowConcurrence;
    }

    public void setAllowConcurrence(boolean allowConcurrence) {
        this.allowConcurrence = allowConcurrence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getJobResult() {
        return jobResult;
    }

    public void setJobResult(Object jobResult) {
        this.jobResult = jobResult;
    }
    /*
    public Exception getExceptionThrownByTheJob() {
        return exceptionThrownByTheJob;
    }

    public void setExceptionThrownByTheJob(Exception exceptionThrownByTheJob) {
        this.exceptionThrownByTheJob = exceptionThrownByTheJob;
    }
    */
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime.getTime();
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime.getTime();
    }
            
    @Override
    public boolean equals (Object obj) {
        return obj instanceof RemoteBackgroundJob && ((RemoteBackgroundJob)obj).getId() == this.id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 97 * hash + Objects.hashCode(this.jobTag);
        return hash;
    }
}


