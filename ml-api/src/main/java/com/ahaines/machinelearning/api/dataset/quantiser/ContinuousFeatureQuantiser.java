package com.ahaines.machinelearning.api.dataset.quantiser;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;

public interface ContinuousFeatureQuantiser {

	void quantise(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureQuantiserType, QuantiserEventProcessor processor);
	
	public static interface QuantiserEventProcessor {
		
		void newRangeDetermined(RangeFeature<? extends Number> range, Iterable<ClassifiedFeatureSet> instanceInSplit);
	}
}
