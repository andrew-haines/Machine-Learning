package com.ahaines.machinelearning.api.dataset.quantiser;

import com.ahaines.machinelearning.api.dataset.Feature;

/**
 * A type of feature that intercepts on a range of different values.
 * @author andrewhaines
 *
 * @param <T>
 */
public class RangeFeature<T extends Comparable<T>> implements Feature<T>{

	private final T lowerBound;
	private final T upperBound;
	
	RangeFeature(T lowerBound, T upperBound){
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	@Override
	public T getValue() {
		throw new UnsupportedOperationException("getValue not supported on a range feature");
	}

	@Override
	public boolean intersects(Feature<T> otherFeature) {
		int lowBoundVal = lowerBound.compareTo(otherFeature.getValue());
		int upBoundVal = upperBound.compareTo(otherFeature.getValue());
		
		return lowBoundVal <= 0 && upBoundVal > 0;
	}
	
	public String toString(){
		return lowerBound+" >= x < "+upperBound;
	}
	
}