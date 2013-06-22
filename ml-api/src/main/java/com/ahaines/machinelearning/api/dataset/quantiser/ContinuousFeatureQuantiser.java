package com.ahaines.machinelearning.api.dataset.quantiser;

import java.util.Collection;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;

public interface ContinuousFeatureQuantiser {

	/**
	 * Quantises a given continuous feature type into discrete values. The provided processor is notified as it detects a new feature range. The
	 * return type of this method is a collection of all the discrete ranges that this feature can be quantised into.
	 * @param instances
	 * @param featureQuantiserType
	 * @param processor
	 * @return
	 */
	<T extends Number & Comparable<T>> Collection<RangeFeature<T>> quantise(Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instances, Class<? extends ContinuousFeature<T>> featureQuantiserType, QuantiserEventProcessor processor);
	
	public static interface QuantiserEventProcessor {
		
		<T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> range, Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instanceInSplit);
	}
}
