package com.ahaines.machinelearning.api.dataset.quantiser;

import com.ahaines.machinelearning.api.dataset.Feature;

/**
 * A type of feature that intercepts on a range of different values.
 * @author andrewhaines
 *
 * @param <T>
 */
public class RangeFeature<T extends Number & Comparable<T>> implements Feature<T>{

	private final T lowerBound;
	private final T upperBound;
	private final boolean inclusive;
	
	public RangeFeature(T lowerBound, T upperBound, boolean inclusive){
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.inclusive = inclusive;
	}
	
	public RangeFeature(T lowerBound, T upperBound){
		this(lowerBound, upperBound, false);
	}
	
	@Override
	public T getValue() {
		throw new UnsupportedOperationException("getValue not supported on a range feature");
	}

	@Override
	public boolean intersects(Feature<T> otherFeature) {
		int lowBoundVal = lowerBound.compareTo(otherFeature.getValue());
		int upBoundVal = upperBound.compareTo(otherFeature.getValue());
		if (inclusive){
			return lowBoundVal <= 0 && upBoundVal >= 0;
		} else {
			return lowBoundVal <= 0 && upBoundVal > 0;
		}
	}
	
	public String toString(){
		return lowerBound+" >= x "+(inclusive?"=":"")+"< "+upperBound;
	}
}