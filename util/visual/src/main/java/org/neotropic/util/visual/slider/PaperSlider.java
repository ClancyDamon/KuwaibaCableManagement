/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.util.visual.slider;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

/**
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Tag("paper-slider")
@NpmPackage(value = "@polymer/paper-slider",
        version = "3.0.1")
@JsModule("@polymer/paper-slider/paper-slider.js")
public class PaperSlider
        extends AbstractSinglePropertyField<PaperSlider, Integer> {

    public PaperSlider() {
        super("value", 0, false);
    }

    public void setMax(double max) {
        this.getElement().setProperty("max", max);
    }

    public void setMin(double min) {
        this.getElement().setProperty("min", min);
    }

    public void setStep(double step) {
        this.getElement().setProperty("step", step);
    }

    public void setSnaps(boolean step) {
        this.getElement().setProperty("snaps", step);
    }

    public void setPin(boolean pin) {
        this.getElement().setProperty("pin", pin);
    }

    public void setKnobColor(String color) {
        this.getElement().getStyle().set("--paper-slider-knob-color", color);
    }

    public void setBarColor(String color) {
        this.getElement().getStyle().set("--paper-slider-container-color", color);
    }
    
    public void setContainerColor(String color) {
        this.getElement().getStyle().set("--paper-slider-bar-color", color);
    }

    public void setActiveProgressBarColor(String color) {
        this.getElement().getStyle().set("--paper-slider-active-color", color);
    }

    public void setSecundaryProgressBarColor(String color) {
        this.getElement().getStyle().set("--paper-slider-secondary-color", color);
    }

    @Synchronize(property = "immediateValue", value = "immediate-value-changed")
    public Integer getImmediateValue() {
        return this.getElement().getProperty("immediateValue", 1);
    }

    public Registration addSliderValueChangingListener(ComponentEventListener<SliderValueChangingEvent> eventListener) {
        return super.addListener(SliderValueChangingEvent.class, eventListener);
    }

    @DomEvent("immediate-value-change")
    public static class SliderValueChangingEvent extends ComponentEvent<PaperSlider> {

        public SliderValueChangingEvent(PaperSlider source, boolean fromClient) {
            super(source, fromClient);
        }
    }

}
