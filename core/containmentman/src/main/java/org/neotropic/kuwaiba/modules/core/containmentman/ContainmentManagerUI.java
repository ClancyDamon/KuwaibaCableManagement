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
package org.neotropic.kuwaiba.modules.core.containmentman;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main UI for containment manager module, initialize all display elements and business logic.
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Route(value = "containment", layout = ContainmentManagerLayout.class)
public class ContainmentManagerUI extends VerticalLayout implements ActionCompletedListener, 
        HasDynamicTitle, AbstractUI {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Layout of classes
     */
    private VerticalLayout lytClasses;
    /**
     * The grid with the classes
     */
    private Grid<ClassMetadataLight> tblClasses;
    /**
     * Object to save list Classes
     */
    private List<ClassMetadataLight> listClasses;
    /**
     * Object to save list children Classes
     */
    private List<ClassMetadataLight> listChildren;
    /**
     * Object to filter for class name
     */
    private ComboBox<ClassMetadataLight> cmbFilterClassNameField;
    /**
     * Objects to validate hierarchy type
     * true for Standard hierarchy
     * false for Special hierarchy
     */
    private boolean isCurrentContainmentHierarchyStandard;
    /**
     * Zone to show the possible children of the selected class
     */
    private Div divPossibleChildren;
    /**
     * To switch from normal hierarchy to special hierarchy
     */
    private Tabs tabContainmentHierarchyType;
    /**
     * to keep track of exceptions when a child is added
     */
    private boolean exFromAdd;
    /**
     * to keep track of exceptions when a child is removed
     */
    private boolean exFromRemove;
    /**
     * To keep track of subclasses to be deselected
     */
    private boolean deselectAbstractClass;
    /**
     * To keep track of subclasses to be reselected if are sub classes of 
     * an abstract
     */
    private boolean reselectSubClassOfAbstract;
    
    public ContainmentManagerUI() {
        super();
        setSizeFull();
        listClasses = new ArrayList<>();
        listChildren = new ArrayList<>();
        this.exFromAdd = false;
        this.exFromRemove = false;
        this.deselectAbstractClass = false;
        this.reselectSubClassOfAbstract = false;
        this.setPadding(false);
        this.setMargin(false);
        this.setSpacing(false);
    }

    /**
     * build the Containment selector if is physical or special containment
     */
    private void buildContainmentOptions() {
        HorizontalLayout lytContainmentHierarchyType = new HorizontalLayout();
        
        Tab tabStandard = new Tab(ts.getTranslatedString("module.containmentman.title.standard"));
        Tab tabSpecial = new Tab(ts.getTranslatedString("module.containmentman.title.special"));
        tabContainmentHierarchyType = new Tabs(tabStandard, tabSpecial);
        isCurrentContainmentHierarchyStandard = true;
        lytContainmentHierarchyType.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        tabContainmentHierarchyType.addSelectedChangeListener(event -> {
            isCurrentContainmentHierarchyStandard = event.getSelectedTab().equals(tabStandard);
            
            if (cmbFilterClassNameField.getValue() != null) {
                lytClasses.removeAll();
                loadPossibleChildren(cmbFilterClassNameField.getValue());
                createBodyTab();
            }
        });

        lytContainmentHierarchyType.add(tabContainmentHierarchyType);
        add(lytContainmentHierarchyType);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytContainmentHierarchyType);
    }

    /**
     * Create Combo box filter to choose a class.
     */
    private void createComboBoxClasses() {
        try {
            //list of classes
            listClasses = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, false, false);
            ClassMetadataLight dumyRoot = new ClassMetadataLight(-1, null, Constants.NODE_DUMMYROOT);
            listClasses.add(dumyRoot);
            // First filter
            cmbFilterClassNameField = new ComboBox<>();
            cmbFilterClassNameField.setWidth("300px");
            cmbFilterClassNameField.setPlaceholder(ts.getTranslatedString("module.containmentman.combobox.filter.placeholder"));
            cmbFilterClassNameField.setItems(listClasses);
            cmbFilterClassNameField.setAllowCustomValue(false);
            cmbFilterClassNameField.setClearButtonVisible(true);
            cmbFilterClassNameField.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    lytClasses.setVisible(true);
                    lytClasses.removeAll();
                    loadPossibleChildren(event.getValue());
                    createBodyTab();
                } else { //wwhen the clear button is used in the Combobox
                    lytClasses.setVisible(false);
                    lytClasses.removeAll();
                    divPossibleChildren.removeAll();
                }
            });
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    /**
     * Populates the visual component with the possible children
     */
    private void loadVisualPossibleChildren(){
        divPossibleChildren.removeAll();

        Label lblPossibleChildren = new Label(ts.getTranslatedString("module.containmentman.current-possible-children"));
        lblPossibleChildren.setClassName("possible_children_label_title");
        Div firstLine = new Div(lblPossibleChildren);
        firstLine.setWidthFull();
        divPossibleChildren.add(firstLine);
        addVisualComponentsPossibleChildren(listChildren);
    }
    
    /**
     * Loads possible children of a given class. 
     * @param parent The name of the parent class.
     */
    private void loadPossibleChildren(ClassMetadataLight parent) {
        if (isCurrentContainmentHierarchyStandard) {
            try {
                listChildren = mem.getPossibleChildrenNoRecursive(parent.getName());
            } catch (MetadataObjectNotFoundException ex) {
                listChildren = Collections.EMPTY_LIST;
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else {
            try {
                listChildren = mem.getPossibleSpecialChildrenNoRecursive(parent.getName());
            } catch (MetadataObjectNotFoundException ex) {
                listChildren = Collections.EMPTY_LIST;
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
    }

    /**
     * Build classes grid and selection listener.
     */    
    private void buildGridClasses() {
        try {
            tblClasses = new Grid<>();
            listClasses = mem.getAllClassesLight(false, false);
            ListDataProvider<ClassMetadataLight> dataProvider = new ListDataProvider<>(listClasses);
            tblClasses.setDataProvider(dataProvider);
            tblClasses.setSelectionMode(Grid.SelectionMode.MULTI);
            tblClasses.setHeightFull();
            Grid.Column<ClassMetadataLight> column = tblClasses.addComponentColumn(aClass -> {
                HashMap<ClassMetadataLight, List<ClassMetadataLight>> updatedChildren = new HashMap<>();

                try {//we also must select the subclasses of the abstract classes already selected
                    for (ClassMetadataLight g : listChildren) {
                        if(g.isAbstract()){
                            if(updatedChildren.get(g) == null)
                                updatedChildren.put(g, new ArrayList<>());
                        
                            updatedChildren.get(g).addAll(mem.getSubClassesLight(g.getName(), false, false));
                        }
                    }
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
                boolean isSubClass = false;
                HorizontalLayout lytRow = new HorizontalLayout();
                for (Map.Entry<ClassMetadataLight, List<ClassMetadataLight>> entry : updatedChildren.entrySet()) {
                    ClassMetadataLight c = entry.getKey();
                    List <ClassMetadataLight> listC = entry.getValue();
                    if(listC.contains(aClass)){
                        Label lblParent = new Label(String.format("%s %s"
                                , ts.getTranslatedString("module.containmentman.possible-child-of")
                                , c.getName()));
                        lblParent.setClassName("abstract_parent_hint");
                        lytRow.add(new Label(aClass.getName()), lblParent);
                        lytRow.setDefaultVerticalComponentAlignment(Alignment.END);
                        isSubClass = true;
                    }
                }
                if(isSubClass)
                    return lytRow;
                else
                    return new Label(aClass.getName());
            });
            // Pre-select items
            tblClasses.asMultiSelect().select(listChildren);
            // Add grid to the layout
            lytClasses.add(tblClasses);
            // Filter Class by Name
            HeaderRow filterRow = tblClasses.appendHeaderRow();
            TextField txtClassName = createTxtFieldClassName(dataProvider);
            filterRow.getCell(column).setComponent(txtClassName);
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
    
    /**
     * Create a new input field to classes in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private TextField createTxtFieldClassName(ListDataProvider<ClassMetadataLight> dataProvider) {
        Icon icon = VaadinIcon.SEARCH.create();
        icon.setClassName("icon-filter");

        TextField txtClassName = new TextField();
        txtClassName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        
        txtClassName.setValueChangeMode(ValueChangeMode.EAGER);
        txtClassName.setWidthFull();
        txtClassName.setSuffixComponent(icon);
        txtClassName.addValueChangeListener(e -> dataProvider.addFilter(
                classes -> StringUtils.containsIgnoreCase(classes.getName(), txtClassName.getValue())
        ));
        return txtClassName;
    }
    
    /**
     * Adds a listener to the grid to add or remove items
     */
    public void buildGridListener(){
        tblClasses.asMultiSelect().addSelectionListener(event -> {
            try{
                // Get old and new values
                Collection<ClassMetadataLight> oldValues = event.getOldValue();
                Collection<ClassMetadataLight> newValues = event.getValue();
                // Compare old and new values
                Collection<ClassMetadataLight> equal = new HashSet<>(oldValues);
                Collection<ClassMetadataLight> different = new HashSet<>();
                different.addAll(oldValues);
                different.addAll(newValues);
                equal.retainAll(newValues);
                // Only changes
                different.removeAll(equal);

                List<ClassMetadataLight> difAbstractSubClass = new ArrayList<>();
                List<ClassMetadataLight> differentNonAbstract = new ArrayList<>();
                List<ClassMetadataLight> addDifferentSubClss = new ArrayList<>();

                //we separete the classes in non abstract and abstract, we also get its subclasses
                for (ClassMetadataLight clss : different) {
                    if(clss.isAbstract())
                        difAbstractSubClass.addAll(mem.getSubClassesLight(clss.getName(), false, false));
                    else
                        differentNonAbstract.add(clss);
                }
                //we check if the new one is already sub class of an abstract class
                for (ClassMetadataLight cp : newValues) {
                    if(cp.isAbstract()){
                        for (ClassMetadataLight c : differentNonAbstract) {
                            if(mem.isSubclassOf(cp.getName(), c.getName()))
                                addDifferentSubClss.add(c);
                        }
                    }
                }
                
                HashMap<ClassMetadataLight, List<ClassMetadataLight>> mapClass = new HashMap<>();
                //we separete the classes in non abstract and abstract, we also get its subclasses
                for (ClassMetadataLight clss : oldValues) {
                    if(clss.isAbstract())
                        mapClass.put(clss, mem.getSubClassesLight(clss.getName(), false, false));
                }
                
                if (event.getValue().size() > event.getOldValue().size() && !exFromRemove){
                    if(!reselectSubClassOfAbstract){
                        //we don't do nothig becasue they are alredy selected 
                        different.removeAll(addDifferentSubClss);
                        addNewChildren(different);
                    }
                    else{
                        reselectSubClassOfAbstract = false; 
                        different.forEach(clss -> {
                            mapClass.entrySet().forEach(entry -> {
                                ClassMetadataLight abstractParent = entry.getKey();
                                List<ClassMetadataLight> subClasses = entry.getValue();
                                if (subClasses.contains(clss)) {
                                    new SimpleNotification(ts.getTranslatedString("module.general.messages.waring"),
                                            String.format("%s,  %s:  %s"
                                                    , clss.getName()
                                                    , ts.getTranslatedString("module.containmentman.error.actions.remove.already-abstract-possible-children")
                                                    , abstractParent.getName()), AbstractNotification.NotificationType.ERROR, ts).open();
                                }
                            });
                        });
                    }
                    //we select the subclasses of any abstract class only in the table
                    if(!exFromAdd && !reselectSubClassOfAbstract)
                        difAbstractSubClass.forEach(tblClasses::select);
                    else if(exFromAdd){
                        difAbstractSubClass.clear();
                        exFromAdd = false;
                    }
                }
                else if (event.getValue().size() < event.getOldValue().size() && !exFromAdd){
                    different.removeAll(addDifferentSubClss);
                    if(!addDifferentSubClss.isEmpty()){
                        reselectSubClassOfAbstract = true;
                        addDifferentSubClss.forEach(tblClasses::select);
                    }
                    if(!deselectAbstractClass)
                        removeChildren(different);
                }

                tblClasses.getDataProvider().refreshAll();
                if(exFromAdd && difAbstractSubClass.isEmpty())
                    exFromAdd = false;
                
                exFromRemove = false;
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        });
    }
    
    /**
     * Method for add possible children.
     * @param classMetadataLight The possible children to add.
     */
    private void addNewChildren(Collection<ClassMetadataLight> classMetadataLight) {
        if(!classMetadataLight.isEmpty()){
            if (cmbFilterClassNameField.getValue() == null) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        ts.getTranslatedString("module.containmentman.error.actions.new-child.right-no-selected"),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            } else if (tblClasses.getSelectedItems() == null || tblClasses.getSelectedItems().isEmpty()) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                        isCurrentContainmentHierarchyStandard ? ts.getTranslatedString("module.containmentman.error.actions.new-child.right-no-selected")
                                : ts.getTranslatedString("module.containmentman.error.actions.new-special-child.right-no-selected"),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            } else {
                ClassMetadataLight parentClass = cmbFilterClassNameField.getValue();
                if (isCurrentContainmentHierarchyStandard) { // Standard hierarchy
                    try {         
                        mem.addPossibleChildren(parentClass.getName(),
                                classMetadataLight.stream().map(element -> element.getName()).toArray(String[]::new));
                        // Add to visual component
                        addVisualComponentsPossibleChildren(classMetadataLight);
                        listChildren.addAll(classMetadataLight);
                        // Notification success
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.containmentman.successful.actions.new-child"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                    } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        exFromAdd = true;
                        tblClasses.asMultiSelect().deselect(classMetadataLight);
                    }
                } else { // Special hierarchy
                    try {
                        mem.addPossibleSpecialChildren(parentClass.getName(), 
                                classMetadataLight.stream().map(element -> element.getName()).toArray(String[]::new));
                        // Add to visual component
                        addVisualComponentsPossibleChildren(classMetadataLight);
                        // Notification success
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                                ts.getTranslatedString("module.containmentman.successful.actions.new-special-child"),
                                AbstractNotification.NotificationType.INFO, ts).open();
                    } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        tblClasses.asMultiSelect().deselect(classMetadataLight);
                    }
                }
            }
        }
    }
    
    /**
     * Method for remove possible children.
     * @param classMetadataLight The possible children to remove.
     */
    private void removeChildren(Collection<ClassMetadataLight> classMetadataLight) {
        if (cmbFilterClassNameField.getValue() == null) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.containmentman.error.actions.new-child.right-no-selected"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        } else {
            ClassMetadataLight parentClass = cmbFilterClassNameField.getValue();
            long[] childrenToBeRemoved = new long[classMetadataLight.size()];
            //we keep track of abstract classes to remove its children
            List<ClassMetadataLight> abstrctClss = new ArrayList<>();
            classMetadataLight.stream().filter(c -> (c.isAbstract())).forEachOrdered(c -> {
                abstrctClss.add(c);
            });

            for (int i = 0; i < classMetadataLight.size(); i++)
                childrenToBeRemoved[i] = (long) classMetadataLight.stream().map(element -> element.getId()).toArray()[i];
            
            if (isCurrentContainmentHierarchyStandard) { // Standard hierarchy
                try {
                    if(!classMetadataLight.isEmpty()){
                        List<ClassMetadataLight> subAbsClss = new ArrayList<>();
                        mem.removePossibleChildren(parentClass.getId(), childrenToBeRemoved);

                        for (ClassMetadataLight aclss : abstrctClss)
                            subAbsClss = mem.getSubClassesLight(aclss.getName(), false, false);

                        deselectAbstractClass = true;
                        tblClasses.asMultiSelect().deselect(subAbsClss);
                        deselectAbstractClass = false;

                        new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.containmentman.successful.actions.remove-child"),
                            AbstractNotification.NotificationType.INFO, ts).open();

                        listChildren.removeAll(classMetadataLight);
                    }
                } catch (MetadataObjectNotFoundException ex) {
                    exFromRemove = true;
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else { // Special hierarchy
                try {
                    mem.removePossibleSpecialChildren(parentClass.getId(), childrenToBeRemoved);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.containmentman.successful.actions.remove-child"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
            //we also remove the possible children from the gui
            Stream<Component> componentChildren = divPossibleChildren.getChildren();
            componentChildren.forEach(c -> {
                if(c instanceof PossibleChildComponent){
                    for (long id : childrenToBeRemoved) {
                        if(id == (((PossibleChildComponent)c).getClassMetadataLight()).getId())
                            divPossibleChildren.remove(c);
                    }
                }
            });
        }
    }
    
    /**
     * Create body tab, grid, possible children layout
     */
    private void createBodyTab() {
        buildGridClasses();
        buildGridListener();
        loadVisualPossibleChildren();
    }
     
    /**
     * Creates a children as a visual component in the possible children div
     * @param possibleChildren the list of possible children to be shown
     */
    private void addVisualComponentsPossibleChildren(Collection<ClassMetadataLight> possibleChildren){
        possibleChildren.forEach(possibleChild -> {
            //We create every child of an abstract class
            if(possibleChild.isAbstract()){
                try {
                    List<ClassMetadataLight> subChildren = mem.getSubClassesLight(possibleChild.getName(), false, false);
                    PossibleChildComponent divAbstractContainer = 
                            new PossibleChildComponent(possibleChild, subChildren);
                    
                    subChildren.stream().map(subChild -> new PossibleChildComponent(subChild, true)).forEachOrdered(divPossibleChild -> {
                        divAbstractContainer.add(divPossibleChild);
                    });
                    
                    divAbstractContainer.getIcnRemove().addClickListener(e -> {
                        e.getSource().getParent().ifPresent(p -> {
                            Collection<ClassMetadataLight> childrenToRemove = new HashSet<>();
                            childrenToRemove.add(((PossibleChildComponent)p).getClassMetadataLight());
                            childrenToRemove.addAll(((PossibleChildComponent)p).getSubClasses()); //we also deselect the subclasses of the bastract class
                            tblClasses.asMultiSelect().deselect(childrenToRemove);
                            divPossibleChildren.remove(p);
                        });
                    });
                
                    divPossibleChildren.add(divAbstractContainer);
                } catch (MetadataObjectNotFoundException ex) {
                    Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            }else{
                PossibleChildComponent divPossibleChild = new PossibleChildComponent(possibleChild, false);
                //click listener in the icon component to remove the possible child
                divPossibleChild.getIcnRemove().addClickListener(e -> {
                    e.getSource().getParent().ifPresent(p -> {
                        Collection<ClassMetadataLight> childrenToRemove = new HashSet<>();
                        childrenToRemove.add(((PossibleChildComponent)p).getClassMetadataLight());
                        tblClasses.asMultiSelect().deselect(childrenToRemove);
                        divPossibleChildren.remove(p);
                    });
                });
                
                divPossibleChildren.add(divPossibleChild);
            }
        });
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.containmentman.title");
    }

    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        // Does nothing for now
    }

    @Override
    public void initContent() {
        setSizeFull();
        buildContainmentOptions();
        VerticalLayout lytMainContent = new VerticalLayout();
        lytMainContent.setSizeFull();
        createComboBoxClasses();
        isCurrentContainmentHierarchyStandard = true;
        
        divPossibleChildren = new Div();
        divPossibleChildren.setClassName("possible_children_container");
        divPossibleChildren.setId("lyt_possible_children");
        
        lytClasses = new VerticalLayout();
        lytClasses.setHeightFull();
        lytClasses.setWidthFull();
        lytClasses.setSizeFull();
        lytClasses.setVisible(false);
        lytClasses.setPadding(false);
        lytClasses.setSpacing(false);
        lytClasses.setMargin(false);
        
        lytMainContent.add(cmbFilterClassNameField, divPossibleChildren, lytClasses);
        lytMainContent.setHorizontalComponentAlignment(Alignment.CENTER, cmbFilterClassNameField);
        add(lytMainContent);
    }
}