/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.util.visual.properties;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

/**
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class DoubleToLongConverter implements Converter<Double, Long> {
	
	private static final long serialVersionUID = 1L;

	@Override
	    public Result<Long> convertToModel(Double presentation, ValueContext valueContext) {
	        return Result.ok(presentation == null ? null : presentation.longValue());
	    }

	    @Override
	    public Double convertToPresentation(Long model, ValueContext valueContext) {
	    	return model == null ? null :model.doubleValue();
	    }

}
