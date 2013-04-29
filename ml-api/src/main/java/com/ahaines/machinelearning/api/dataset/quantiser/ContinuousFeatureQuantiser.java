package com.ahaines.machinelearning.api.dataset.quantiser;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;

public interface ContinuousFeatureQuantiser {

	public void quantiser(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureQuantiserType, QuantiserEventListener listener);
	
	public static interface QuantiserEventListener {
		
		public void newRangeDetermined(RangeFeature<? extends Number> newRange, Iterable<ClassifiedFeatureSet> instanceInSplit);
	}
}
