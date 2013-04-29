package com.ahaines.machinelearning.decisiontree;

import java.util.Collection;
import java.util.LinkedList;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser.QuantiserEventListener;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantisers;
import com.ahaines.machinelearning.api.dataset.quantiser.RangeFeature;
import com.ahaines.machinelearning.api.util.Utils;
import com.ahaines.machinelearning.decisiontree.DecisionTreeModelService.Split;

public interface ContinuousFeatureSplitter {

	Iterable<Split> splitInstances(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureType);
	
	public static class ContinuousFeatureSplitters {
		
		/**
		 * splits based on an average pivot value
		 * @return
		 */
		public static ContinuousFeatureSplitter getAverageFeatureSplitter(){
			return new ContinuousFeatureSplitter(){

				@Override
				public Iterable<Split> splitInstances(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureType) {
					
					return getSplits(instances, featureType, ContinuousFeatureQuantisers.getAveragePivotQuantiser());
				}
				
			};
		}
		
		private static Iterable<Split> getSplits(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureType, ContinuousFeatureQuantiser quantiser){
			final Collection<Split> splits = new LinkedList<Split>();
			quantiser.quantiser(instances, featureType, new QuantiserEventListener(){

				@Override
				public void newRangeDetermined(RangeFeature<? extends Number> newRange, Iterable<ClassifiedFeatureSet> instancesInSplit) {
					splits.add(new Split(new FeatureDefinition(newRange), Utils.toCollection(instancesInSplit)));
				}
				
			});
			
			return splits;
		}
		
		/**
		 * splits based on incremental clustering of classifications
		 * @return
		 */
		public static ContinuousFeatureSplitter getClusterSplitter(){
			return new ContinuousFeatureSplitter(){

				@Override
				public Iterable<Split> splitInstances(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureType) {
					
					return getSplits(instances, featureType, ContinuousFeatureQuantisers.getClusteredQuantiser());
				}
				
			};
		}
	}
}
