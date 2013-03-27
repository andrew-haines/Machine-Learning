package com.ahaines.machinelearning.decisiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.decisiontree.DecisionTreeModelService.Split;
import com.google.common.collect.Lists;

public interface ContinuousFeatureSplitter {

	Collection<Split> splitInstances(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureType);
	
	public static class ContinuousFeatureSplitters {
		
		/**
		 * splits based on an average pivot value
		 * @return
		 */
		public static ContinuousFeatureSplitter getAverageFeatureSplitter(){
			return new ContinuousFeatureSplitter(){

				@Override
				public Collection<Split> splitInstances(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureType) {
					// calculate average
					
					int count = 0;
					
					int sum = 0;
					
					for (ClassifiedFeatureSet instance: instances){
						sum += instance.getFeature(featureType).getValue().intValue();
						count++;
					}
					
					double average = (double)sum / (double)count;
					
					// now put all instances in buckets above and below the average
					
					Collection<ClassifiedFeatureSet> lessThanAverage = new ArrayList<ClassifiedFeatureSet>();
					Collection<ClassifiedFeatureSet> greaterThanOrEqualAverage = new ArrayList<ClassifiedFeatureSet>();
					
					for (ClassifiedFeatureSet instance: instances){
						if (instance.getFeature(featureType).getValue().intValue() < average){
							lessThanAverage.add(instance);
						} else{
							greaterThanOrEqualAverage.add(instance);
						}
					}
					return Arrays.asList(new Split(new FeatureDefinition(new RangeFeature<Integer>(0, (int)average), featureType), lessThanAverage), new Split(new FeatureDefinition(new RangeFeature<Integer>((int)average, Integer.MAX_VALUE), featureType), greaterThanOrEqualAverage));
				}
				
			};
		}
		
		/**
		 * splits based on incremental clustering of classifications
		 * @return
		 */
		public static ContinuousFeatureSplitter getClusterSplitter(){
			return new ContinuousFeatureSplitter(){

				@Override
				public Collection<Split> splitInstances(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<?>> featureType) {
					
					/*
					 * This algorithm basically sorts the instance set by the feature and then runs through creating
					 * RangeFeature's with lower, upper bounds as the classification of each instance changes
					 */
					
					List<ClassifiedFeatureSet> sortedList = Lists.newArrayList(instances);
					
					Collections.sort(sortedList, new Comparator<ClassifiedFeatureSet>(){

						@Override
						public int compare(ClassifiedFeatureSet o1, ClassifiedFeatureSet o2) {
							int feature1 = o1.getFeature(featureType).getValue().intValue();
							int feature2 = o2.getFeature(featureType).getValue().intValue();
							return feature1 - feature2;
						}
						
					});
					
					/*
					 *  not sure if this is correct. What if a particular classification does not exist in this set?
					 *  Wont this mean its imposible to get this classification if it ends up going down this branch?
					 */
					
					int min = 0;
					int lastRecord = 0;
					Enum<?> lastClassification = null;
					Collection<Split> splits = new ArrayList<Split>();
					Collection<ClassifiedFeatureSet> tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
					
					for (ClassifiedFeatureSet instance: sortedList){
						int newRecord = instance.getFeature(featureType).getValue().intValue();
						if (lastClassification == null){
							lastClassification = instance.getClassification().getValue();
							lastRecord = newRecord;
							
						} else if (!lastClassification.equals(instance.getClassification().getValue())){
							if (lastRecord != newRecord){ // only split if the value has actually changed else we will split with this value twice
								lastClassification = instance.getClassification().getValue();
								splits.add(new Split(new FeatureDefinition(new RangeFeature<Integer>(min, newRecord), featureType), tempInstanceSplit));
								tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
								min = newRecord;
								lastRecord = newRecord;
							}
						}
						
						tempInstanceSplit.add(instance);
					}
					
					splits.add(new Split(new FeatureDefinition(new RangeFeature<Integer>(min, Integer.MAX_VALUE), featureType), tempInstanceSplit));
					
					return splits;
				}
				
			};
		}
		
		/**
		 * A type of feature that intercepts on a range of different values.
		 * @author andrewhaines
		 *
		 * @param <T>
		 */
		static class RangeFeature<T extends Comparable<T>> implements Feature<T>{

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
	}
}
