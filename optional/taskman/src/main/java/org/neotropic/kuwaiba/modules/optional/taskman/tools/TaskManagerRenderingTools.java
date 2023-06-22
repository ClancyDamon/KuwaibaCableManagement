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
package org.neotropic.kuwaiba.modules.optional.taskman.tools;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This class help another classes to create complex components it will show in
 * front end components
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class TaskManagerRenderingTools {
    /**
     * Transform local date to long
     * @param localDate
     * @return long
     */
    public static long convertLocalDateToLong(LocalDate localDate) {
        ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }
    
    /**
     * Transform long to local date
     * @param localDate
     * @return localDate
     */
    public static LocalDate convertLongToLocalDate(long localDate) {
        return Instant.ofEpochMilli(localDate).atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    /**
     * Transform LocalDateTime to long
     * @param localDateTime
     * @return long
     */
    public static long convertLocalDateTimeToLong(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Transform long to LocalDateTime@return localDateTime
     * @param localDateTime
     * @return localDateTime
     */
    public static LocalDateTime convertLongToLocalDateTime(long localDateTime) {
        return Instant.ofEpochMilli(localDateTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
}