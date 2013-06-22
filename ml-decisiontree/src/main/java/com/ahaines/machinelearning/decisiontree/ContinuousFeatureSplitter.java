package com.ahaines.machinelearning.decisiontree;

import java.util.Collection;
import java.util.LinkedList;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser.QuantiserEventProcessor;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantisers;
import com.ahaines.machinelearning.api.dataset.quantiser.RangeFeature;
import com.ahaines.machinelearning.api.util.Utils;
import com.ahaines.machinelearning.decisiontree.DecisionTreeModelService.Split;

public interface ContinuousFeatureSplitter {

	<T extends Number & Comparable<T>, C extends Enum<C>> Iterable<Split<C>> splitInstances(Iterable<ClassifiedFeatureSet<C>> instances, Class<? extends ContinuousFeature<T>> featureType);
	
	public static class ContinuousFeatureSplitters {
		
		/**
		 * splits based on an average pivot value
		 * @return
		 */
		public static ContinuousFeatureSplitter getAverageFeatureSplitter(){
			return getFeatureSplitter(ContinuousFeatureQuantisers.getAveragePivotQuantiser());
		}
		
		private static <T extends Number & Comparable<T>, C extends Enum<C>> Iterable<Split<C>> getSplits(Iterable<ClassifiedFeatureSet<C>> instances, final Class<? extends ContinuousFeature<T>> featureType, ContinuousFeatureQuantiser quantiser){
			final Collection<Split<C>> splits = new LinkedList<Split<C>>();
			quantiser.quantise(instances, featureType, new QuantiserEventProcessor(){

				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange, Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instancesInSplit) {
					splits.add(new Split(new FeatureDefinition(newRange, featureType), Utils.toCollection(instancesInSplit)));
				}
			});

				
			
			return splits;
		}
		
		/**
		 * splits based on incremental clustering of classifications
		 * @return
		 */
		public static ContinuousFeatureSplitter getClusterSplitter(){
			return getFeatureSplitter(ContinuousFeatureQuantisers.getClusteredQuantiser());
		}
		
		public static ContinuousFeatureSplitter getConstantBucketFeatureSplitter(int numBuckets){
			return getFeatureSplitter(ContinuousFeatureQuantisers.getConstantBucketQuantiser(numBuckets));
		}
		
		public static ContinuousFeatureSplitter getFeatureSplitter(final ContinuousFeatureQuantiser quantiser){
			return new ContinuousFeatureSplitter(){

				@Override
				public <T extends Number & Comparable<T>, C extends Enum<C>> Iterable<Split<C>> splitInstances(Iterable<ClassifiedFeatureSet<C>> instances, Class<? extends ContinuousFeature<T>> featureType) {
					
					return getSplits(instances, featureType, quantiser);
				}
			};
				
		}
	}
}
