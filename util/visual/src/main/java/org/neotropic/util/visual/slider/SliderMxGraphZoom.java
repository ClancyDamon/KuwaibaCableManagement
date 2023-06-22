/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.util.visual.slider;

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Custom slider control for mxgraph zoom
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class SliderMxGraphZoom extends PaperSlider {
    /**
     * The canvas whose zoom will be managed by this slider.
     */
    private MxGraph canvas;
    public static String TITLE = "Zoom:";

    public SliderMxGraphZoom(MxGraph mxGraph) {
        this.canvas = mxGraph;
        setMax(300);
        setMin(10);
        setStep(5);
        setValue(100);
        addValueChangeListener(listener-> {
            double value = new Double(listener.getValue()) / 100;
            this.canvas.setScale(value);
        });
        addListener(SliderValueChangingEvent.class, listener -> {
            double value = new Double(getImmediateValue()) / 100;
            this.canvas.setScale(value);
        });
        setActiveProgressBarColor("var(--primary-color)");
        setKnobColor("var(--primary-color)");
    }
    
    public HorizontalLayout getAsLaveledHorizontalLayout() {
        Label lblTitle = new Label(TITLE);
        HorizontalLayout lytSlider = new HorizontalLayout(lblTitle, this);
        lytSlider.setMargin(false);
        lytSlider.setPadding(false);
        lytSlider.setAlignItems(FlexComponent.Alignment.CENTER);
        return lytSlider;
    }
    
    public VerticalLayout getAsLaveledVerticalLayout() {
        Label lblTitle = new Label(TITLE);
        VerticalLayout lytSlider = new VerticalLayout(lblTitle, this);
        lytSlider.setMargin(false);
        lytSlider.setSpacing(false);
        return lytSlider;
    }

    public MxGraph getCanvas() {
        return canvas;
    }

    public void setCanvas(MxGraph canvas) {
        this.canvas = canvas;
    }
}
