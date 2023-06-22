/*
 * Copyright 2021 Neotropic SAS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neotropic.kuwaiba.core.apis.integration.views;

/**
 * Defines the methods for exporting views as image in different formats.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 * @param <T> The type of data source that will be used to be exported
 */
public abstract class AbstractImageExporter<T> {
    public abstract byte [] export(T data);
}
