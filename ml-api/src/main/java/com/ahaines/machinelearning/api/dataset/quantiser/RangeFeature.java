package com.ahaines.machinelearning.api.dataset.quantiser;


import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.ahaines.machinelearning.api.dataset.Feature;

/**
 * A type of feature that intercepts on a range of different values.
 * @author andrewhaines
 *
 * @param <T>
 */
public class RangeFeature<T extends Number & Comparable<T>> implements Feature<T>, Comparable<Feature<T>>{
	
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
	
	@Override
	public String toString(){
		return lowerBound+" >= x "+(inclusive?"=":"")+"< "+upperBound;
	}
	
	@Override
	public int hashCode(){
		int hash = 31;
		
		hash *= lowerBound.hashCode();
		hash *= upperBound.hashCode();
		hash *= (inclusive)?1:2;
		
		return hash;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof RangeFeature){
			RangeFeature<?> other = (RangeFeature<?>)obj;
			
			return other.lowerBound.equals(this.lowerBound) && other.upperBound.equals(this.upperBound) && other.inclusive == this.inclusive;
		}
		
		return false;
	}

	@Override
	public int compareTo(Feature<T> o) {
		if (o instanceof RangeFeature){
			return this.lowerBound.compareTo(((RangeFeature<T>)o).lowerBound);
		} else if (o instanceof ContinuousFeature){ // bit mental this bit is. A way of using a binary search to determine where a standard continuous value sits with ranges. Ideally this should be a seperate comparator to isolate the concerns better 
			if (this.lowerBound.compareTo(o.getValue()) > 0){
				return 1;
			} else if (inclusive && this.upperBound.compareTo(o.getValue()) < 0 || (!inclusive && this.upperBound.compareTo(o.getValue()) <= 0)){
				return -1;
			} else{
				return 0;
			}
		} else{
			throw new IllegalArgumentException("Cant compare a range value to anything else then another range value or a continuous value that sits in this range");
		}
	}
}