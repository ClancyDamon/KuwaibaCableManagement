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
package org.neotropic.kuwaiba.modules.optional.serviceman.components;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.ValueProvider;

/**
 * Custom template for treegrid row
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class ServiceManTree<T> extends TreeGrid<T> {

    public Column<T> addHierarchyColumn(ValueProvider<T, ?> itemProvider, ValueProvider<T, ?> nameProvider, ValueProvider<T, ?> classnameProvider) {
        TemplateRenderer<T> template = TemplateRenderer.<T>of(
                "<vaadin-grid-tree-toggle leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>"
                + "<iron-icon icon='[[item.icon]]' style='margin-right: 0.2em; width: 13px; height: 13px;[[item.iconDisplay]]'></iron-icon>"
                + "<image src='[[item.imgSrc]]' style='margin-right: 0.2em;width: 13px; height: 13px; [[item.imgDisplay]]' >"
                + "[[item.name]] [[[item.classname]]]"
                + "</vaadin-grid-tree-toggle>")
                .withProperty("leaf", item -> !getDataCommunicator().hasChildren(item))
                .withProperty("icon", item -> {
                    ServiceManTreeNode apply = (ServiceManTreeNode) itemProvider.apply(item);
                    if (apply.getIcon() != null) {
                        return apply.getIcon().getElement().getAttribute("icon");
                    } else {
                        return "";
                    }
                })
                .withProperty("iconDisplay", item -> {
                    ServiceManTreeNode apply = (ServiceManTreeNode) itemProvider.apply(item);
                    if (apply.getIcon() != null) {
                        return "";
                    } else {
                        return "display: none;";
                    }
                })
                .withProperty("imgSrc", item -> {
                    ServiceManTreeNode apply = (ServiceManTreeNode) itemProvider.apply(item);
                    if (apply.getImage() != null) {
                        return apply.getImage().getSrc();
                    } else {
                        return "";
                    }
                })
                .withProperty("imgDisplay", item -> {
                    ServiceManTreeNode apply = (ServiceManTreeNode) itemProvider.apply(item);
                    if (apply.getImage() != null) {
                        return "";
                    } else {
                        return "display: none;";
                    }
                })
                .withProperty("classname", classname -> String.valueOf(classnameProvider.apply(classname)))
                .withProperty("name", item -> String.valueOf(nameProvider.apply(item)));

        final Column<T> column = addColumn(template);    
        final SerializableComparator<T> comparator
                = (a, b) -> compareMaybeComparables(nameProvider.apply(a),
                        nameProvider.apply(b));
        column.setComparator(comparator);

        return column;
    }

}
