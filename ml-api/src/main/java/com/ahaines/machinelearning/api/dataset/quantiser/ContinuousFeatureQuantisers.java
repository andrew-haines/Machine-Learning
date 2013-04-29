package com.ahaines.machinelearning.api.dataset.quantiser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ContinuousFeatureQuantisers {

	public static ContinuousFeatureQuantiser getAveragePivotQuantiser(){
		
		return new ContinuousFeatureQuantiser(){

			@Override
			public void quantiser(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<?>> featureQuantiserType, QuantiserEventListener listener) {
				// calculate average
				
				int count = 0;
				
				int sum = 0;
				
				for (ClassifiedFeatureSet instance: instances){
					sum += instance.getFeature(featureQuantiserType).getValue().intValue();
					count++;
				}
				
				final double average = (double)sum / (double)count;
				
				// now put all instances in buckets above and below the average
				
				listener.newRangeDetermined(new RangeFeature<Integer>(0, (int)average), Iterables.filter(instances, new Predicate<ClassifiedFeatureSet>() {
					
					public boolean apply(ClassifiedFeatureSet instance){
						return instance.getFeature(featureQuantiserType).getValue().intValue() < average;
					}
					
				}));
				
				listener.newRangeDetermined(new RangeFeature<Integer>((int)average, Integer.MAX_VALUE), Iterables.filter(instances, new Predicate<ClassifiedFeatureSet>() {
					
					public boolean apply(ClassifiedFeatureSet instance){
						return instance.getFeature(featureQuantiserType).getValue().intValue() >= average;
					}
					
				}));
			}
			
		};
	}
	
	public static ContinuousFeatureQuantiser getClusteredQuantiser(){
		return new ContinuousFeatureQuantiser(){

			@Override
			public void quantiser(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<?>> featureQuantiserType, QuantiserEventListener listener) {
				
				/*
				 * This algorithm basically sorts the instance set by the feature and then runs through creating
				 * RangeFeature's with lower, upper bounds as the classification of each instance changes
				 */
				
				List<ClassifiedFeatureSet> sortedList = Lists.newArrayList(instances);
				
				Collections.sort(sortedList, new Comparator<ClassifiedFeatureSet>(){

					@Override
					public int compare(ClassifiedFeatureSet o1, ClassifiedFeatureSet o2) {
						int feature1 = o1.getFeature(featureQuantiserType).getValue().intValue();
						int feature2 = o2.getFeature(featureQuantiserType).getValue().intValue();
						return feature1 - feature2;
					}
					
				});
				
				/*
				 *  not sure if this is correct. What if a particular classification does not exist in this set?
				 *  Wont this mean its impossible to get this classification if it ends up going down this branch?
				 */
				
				int min = 0;
				int lastRecord = 0;
				Enum<?> lastClassification = null;
				Collection<ClassifiedFeatureSet> tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
				
				for (ClassifiedFeatureSet instance: sortedList){
					int newRecord = instance.getFeature(featureQuantiserType).getValue().intValue();
					if (lastClassification == null){
						lastClassification = instance.getClassification().getValue();
						lastRecord = newRecord;
						
					} else if (!lastClassification.equals(instance.getClassification().getValue())){
						if (lastRecord != newRecord){ // only split if the value has actually changed else we will split with this value twice
							lastClassification = instance.getClassification().getValue();
							
							listener.newRangeDetermined(new RangeFeature<Integer>(min, newRecord), tempInstanceSplit);
							tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
							min = newRecord;
							lastRecord = newRecord;
						}
					}
					
					tempInstanceSplit.add(instance);
				}
				
				listener.newRangeDetermined(new RangeFeature<Integer>(min, Integer.MAX_VALUE), tempInstanceSplit);
			}
			
		};
	}
}
