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

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void quantise(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<?>> featureQuantiserType, QuantiserEventProcessor processor) {
				// calculate average
				
				int count = 0;
				NumberConverter<?> converter = null;
				
				double sum = 0;
				
				for (ClassifiedFeatureSet instance: instances){
					
					ContinuousFeature<?> conFeature = instance.getFeature(featureQuantiserType);
					if (converter == null){
						converter = conFeature.getNumberConverter();
					}
					sum += conFeature.getValue().doubleValue();
					count++;
				}
				
				final double average = (double)sum / (double)count;
				
				// now put all instances in buckets above and below the average
				
				RangeFeature<?> newFeature = new RangeFeature(converter.getMinPossibleValue(), converter.castToType(average));
				
				processor.newRangeDetermined(newFeature, Iterables.filter(instances, new Predicate<ClassifiedFeatureSet>() {
					
					public boolean apply(ClassifiedFeatureSet instance){
						return instance.getFeature(featureQuantiserType).getValue().intValue() < average;
					}
					
				}));
				
				newFeature = new RangeFeature(converter.castToType(average), converter.getMaxPossibleValue(), true);
				
				processor.newRangeDetermined(newFeature, Iterables.filter(instances, new Predicate<ClassifiedFeatureSet>() {
					
					public boolean apply(ClassifiedFeatureSet instance){
						return instance.getFeature(featureQuantiserType).getValue().intValue() >= average;
					}
					
				}));
			}
			
		};
	}
	
	public static ContinuousFeatureQuantiser getClusteredQuantiser(){
		return new ContinuousFeatureQuantiser(){

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void quantise(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<?>> featureQuantiserType, QuantiserEventProcessor processor) {
				
				/*
				 * This algorithm basically sorts the instance set by the feature and then runs through creating
				 * RangeFeature's with lower, upper bounds as the classification of each instance changes
				 */
				
				List<ClassifiedFeatureSet> sortedList = Lists.newArrayList(instances);
				
				Collections.sort(sortedList, new Comparator<ClassifiedFeatureSet>(){

					@Override
					public int compare(ClassifiedFeatureSet o1, ClassifiedFeatureSet o2) {
						Number feature1 = o1.getFeature(featureQuantiserType).getValue();
						Number feature2 = o2.getFeature(featureQuantiserType).getValue();
						return (int)(feature1.doubleValue() - feature2.doubleValue());
					}
					
				});
				
				/*
				 *  not sure if this is correct. What if a particular classification does not exist in this set?
				 *  Wont this mean its impossible to get this classification if it ends up going down this branch?
				 */
				
				Number min = null;
				Number lastRecord = null;
				Enum<?> lastClassification = null;
				Collection<ClassifiedFeatureSet> tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
				NumberConverter<?> converter = null;
				
				for (ClassifiedFeatureSet instance: sortedList){
					Number newRecord = instance.getFeature(featureQuantiserType).getValue();
					if (lastClassification == null){
						lastClassification = instance.getClassification().getValue();
						lastRecord = newRecord;
						converter = instance.getFeature(featureQuantiserType).getNumberConverter();
						min = converter.getMinPossibleValue();
						
					} else if (!lastClassification.equals(instance.getClassification().getValue())){
						if (!lastRecord.equals(newRecord)){ // only split if the value has actually changed else we will split with this value twice
							lastClassification = instance.getClassification().getValue();
							
							RangeFeature<?> newRange = new RangeFeature(min, newRecord);
							processor.newRangeDetermined(newRange, tempInstanceSplit);
							tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
							min = newRecord;
							lastRecord = newRecord;
						}
					}
					
					tempInstanceSplit.add(instance);
				}
				
				processor.newRangeDetermined(new RangeFeature(min, converter.getMaxPossibleValue(), true), tempInstanceSplit);
			}
			
		};
	}
}
