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

package org.neotropic.kuwaiba.modules.commercial.sync.components;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import lombok.Getter;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncResult;

import java.util.List;

/**
 * Dialog for synxchronizationp rogress
 * @author Hardy Ryan Chingal Martinez <ryan.chingal@neotropic.co>
 */
public class ProgressViewDialog extends Dialog {
    private final VerticalLayout bodyDialog;
    private final Div fetchProgressBarLabel;
    private final Div analizedProgressBarLabel;
    private final Span fetchMessage;
    private final Span analizeMessage;
    private final ProgressBar fetchProgressBar;
    private final ProgressBar analizedProgressBar;
    @Getter
    private final Button btnConfirm;
    private final TranslationService ts;

    public ProgressViewDialog(TranslationService ts) {

        setMinWidth("75%");
        this.bodyDialog = new VerticalLayout();
        HorizontalLayout fetchData = new HorizontalLayout();
        HorizontalLayout analizeData = new HorizontalLayout();
        this.btnConfirm = new Button(ts.getTranslatedString("module.sync.data-source.button.accept"));
        this.fetchMessage = new Span();
        this.analizeMessage = new Span();
        this.fetchProgressBar = new ProgressBar();
        this.analizedProgressBar = new ProgressBar();
        this.fetchProgressBarLabel = new Div();
        this.analizedProgressBarLabel = new Div();
        this.ts = ts;
        this.setDraggable(true);
        this.setResizable(true);
        this.setModal(false);
        Accordion accordion = new Accordion();
        // Set general attribute properties
        fetchData.setWidthFull();
        fetchData.addAndExpand(fetchMessage);
        fetchProgressBar.setWidthFull();
        VerticalLayout fetchVL = new VerticalLayout(fetchProgressBarLabel, fetchProgressBar);
        fetchVL.setHorizontalComponentAlignment(FlexComponent.Alignment.END, fetchProgressBarLabel );
        fetchData.add(fetchVL);
        fetchData.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        analizeData.setWidthFull();
        analizeData.addAndExpand(analizeMessage);
        analizedProgressBar.setWidthFull();
        VerticalLayout analizedVL = new VerticalLayout(analizedProgressBarLabel, analizedProgressBar);
        analizedVL.setHorizontalComponentAlignment(FlexComponent.Alignment.END, analizedProgressBarLabel );
        analizeData.add(analizedVL);
        analizeData.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        this.bodyDialog.add(fetchData, analizeData);
        accordion.add(ts.getTranslatedString("module.sync.dialog.tittle"), bodyDialog);
        this.add(accordion);
        this.btnConfirm.addClickListener( event -> this.close());
        this.btnConfirm.setClassName("confirm-button");
        this.btnConfirm.addThemeVariants(ButtonVariant.LUMO_LARGE);
        this.btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    public void updateValues(JobProgressMessage progress, List<SyncResult> syncResults){
        if(progress.getStep() == EAsyncStep.FETCH){
            fetchMessage.setText(String.format("%s %s", ts.getTranslatedString(progress.getStep().getValue())
                    , ts.getTranslatedString(progress.getState().getValue())));
            fetchProgressBarLabel.setText(String.format("Tasks (%s/%s)", progress.getElement(), progress.getTotalElements()));
            fetchProgressBar.setValue( progress.getProgress());

        } else if (progress.getStep() == EAsyncStep.ANALYZE) {
            analizeMessage.setText(String.format("%s %s", ts.getTranslatedString(progress.getStep().getValue())
                    , ts.getTranslatedString(progress.getState().getValue())));
            analizedProgressBarLabel.setText(String.format("Tasks (%s/%s)", progress.getElement(), progress.getTotalElements()));
            analizedProgressBar.setValue( progress.getProgress());
        }

        if (!syncResults.isEmpty()) {
            Grid<SyncResult> grdSyncResult = new Grid<>();
            grdSyncResult.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT);
            grdSyncResult.addComponentColumn(item -> {
                Div state = new Div();
                state.getElement().setProperty("innerHTML", item.getTypeAsString());
                if (item.getTypeAsString().toUpperCase().equals(ESyncResultState.SUCCESS.toString()))
                    state.setClassName(ESyncResultState.SUCCESS.getValue());
                else if (item.getTypeAsString().toUpperCase().equals(ESyncResultState.ERROR.toString()))
                    state.setClassName(ESyncResultState.ERROR.getValue());
                else if (item.getTypeAsString().toUpperCase().equals(ESyncResultState.WARNING.toString()))
                    state.setClassName(ESyncResultState.WARNING.getValue());
                else if (item.getTypeAsString().toUpperCase().equals(ESyncResultState.INFORMATION.toString()))
                    state.setClassName(ESyncResultState.INFORMATION.getValue());
                return state;
            }).setAutoWidth(true).setResizable(true);
            grdSyncResult.addColumn(SyncResult::getResult)
                    .setResizable(true).setFlexGrow(1).setClassNameGenerator(item -> "wrap-cell");
            grdSyncResult.addColumn(SyncResult::getActionDescription)
                    .setResizable(true).setFlexGrow(1).setClassNameGenerator(item -> "wrap-cell");

            grdSyncResult.setItems(syncResults);
            this.bodyDialog.add(grdSyncResult);
        }
        if(progress.getStep() == EAsyncStep.ANALYZE && progress.getState() == EJobState.FINISH) {
            this.bodyDialog.add(btnConfirm);
            this.bodyDialog.setHorizontalComponentAlignment(FlexComponent.Alignment.END, btnConfirm);
        }
    }
}
