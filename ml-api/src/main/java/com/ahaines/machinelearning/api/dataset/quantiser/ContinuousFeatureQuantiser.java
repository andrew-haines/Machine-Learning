package com.ahaines.machinelearning.api.dataset.quantiser;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;

public interface ContinuousFeatureQuantiser {

	<T extends Number & Comparable<T>>void quantise(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<T>> featureQuantiserType, QuantiserEventProcessor processor);
	
	public static interface QuantiserEventProcessor {
		
		<T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> range, Iterable<ClassifiedFeatureSet> instanceInSplit);
	}
}
