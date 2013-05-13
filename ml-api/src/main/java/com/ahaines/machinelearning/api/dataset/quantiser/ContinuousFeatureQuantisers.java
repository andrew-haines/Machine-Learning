package com.ahaines.machinelearning.api.dataset.quantiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ContinuousFeatureQuantisers {

	public static ContinuousFeatureQuantiser getAveragePivotQuantiser(){
		
		return new ContinuousFeatureQuantiser(){

			@Override
			public <T extends Number & Comparable<T>> void quantise(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<T>> featureQuantiserType, QuantiserEventProcessor processor) {
				// calculate average
				
				int count = 0;
				NumberConverter<T> converter = null;
				
				double sum = 0;
				
				for (ClassifiedFeatureSet instance: instances){
					
					ContinuousFeature<T> conFeature = instance.getFeature(featureQuantiserType);
					if (converter == null){
						converter = conFeature.getNumberConverter();
					}
					sum += conFeature.getValue().doubleValue();
					count++;
				}
				
				final double average = (double)sum / (double)count;
				
				// now put all instances in buckets above and below the average
				
				RangeFeature<T> newFeature = new RangeFeature<T>(converter.getMinPossibleValue(), converter.castToType(average));
				
				processor.newRangeDetermined(newFeature, Iterables.filter(instances, new Predicate<ClassifiedFeatureSet>() {
					
					public boolean apply(ClassifiedFeatureSet instance){
						return instance.getFeature(featureQuantiserType).getValue().intValue() < average;
					}
					
				}));
				
				newFeature = new RangeFeature<T>(converter.castToType(average), converter.getMaxPossibleValue(), true);
				
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

			@Override
			public <T extends Number & Comparable<T>> void quantise(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<T>> featureQuantiserType, QuantiserEventProcessor processor) {
				
				/*
				 * This algorithm basically sorts the instance set by the feature and then runs through creating
				 * RangeFeature's with lower, upper bounds as the classification of each instance changes
				 */
				
				List<ClassifiedFeatureSet> sortedList = sortInstancesBasedOnFeature(instances, featureQuantiserType);
				
				/*
				 *  not sure if this is correct. What if a particular classification does not exist in this set?
				 *  Wont this mean its impossible to get this classification if it ends up going down this branch?
				 */
				
				T min = null;
				T lastRecord = null;
				Enum<?> lastClassification = null;
				Collection<ClassifiedFeatureSet> tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
				NumberConverter<T> converter = null;
				
				for (ClassifiedFeatureSet instance: sortedList){
					T newRecord = instance.getFeature(featureQuantiserType).getValue();
					if (lastClassification == null){
						lastClassification = instance.getClassification().getValue();
						lastRecord = newRecord;
						converter = instance.getFeature(featureQuantiserType).getNumberConverter();
						min = converter.getMinPossibleValue();
						
					} else if (!lastClassification.equals(instance.getClassification().getValue())){
						if (!lastRecord.equals(newRecord)){ // only split if the value has actually changed else we will split with this value twice
							lastClassification = instance.getClassification().getValue();
							
							RangeFeature<T> newRange = new RangeFeature<T>(min, newRecord);
							processor.newRangeDetermined(newRange, tempInstanceSplit);
							tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
							min = newRecord;
							lastRecord = newRecord;
						}
					}
					
					tempInstanceSplit.add(instance);
				}
				
				processor.newRangeDetermined(new RangeFeature<T>(min, converter.getMaxPossibleValue(), true), tempInstanceSplit);
			}
			
		};
	}
	
	public static ContinuousFeatureQuantiser getConstantBucketQuantiser(final int numBuckets){
		return new ContinuousFeatureQuantiser(){

			@Override
			public <T extends Number & Comparable<T>> void quantise(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<T>> featureQuantiserType, QuantiserEventProcessor processor) {
				final List<ClassifiedFeatureSet> sortedList = sortInstancesBasedOnFeature(instances, featureQuantiserType);
				
				final NumberConverter<T> converter = sortedList.get(0).getFeature(featureQuantiserType).getNumberConverter();
				T minValue = sortedList.get(0).getFeature(featureQuantiserType).getValue();
				T maxValue = sortedList.get(sortedList.size()-1).getFeature(featureQuantiserType).getValue();
				
				RangeFeature<T> lowerBound = new RangeFeature<T>(converter.getMinPossibleValue(), minValue);
				
				processor.newRangeDetermined(lowerBound, Arrays.asList(sortedList.get(0)));
				
				// inbetween bands
				
				int numRangeBuckets = numBuckets - 2; // subtract the 2 extremity bounds
				
				double range = (maxValue.doubleValue() - minValue.doubleValue()) / numRangeBuckets;
				final AtomicInteger latestInstanceIndex = new AtomicInteger(1);
				for (int i = 0; i < numRangeBuckets; i++){
					double lowerBoundRange = range * i + minValue.doubleValue();
					final double upperBoundRange = lowerBoundRange + range;
					
					RangeFeature<T> newRange = new RangeFeature<T>(converter.castToType(lowerBoundRange), converter.castToType(upperBoundRange));
					final int seekForwardAmount = latestInstanceIndex.get(); // optimisation because we dont want to 
					processor.newRangeDetermined(newRange, new Iterable<ClassifiedFeatureSet>(){

						@Override
						public Iterator<ClassifiedFeatureSet> iterator() {
							return new Iterator<ClassifiedFeatureSet>(){
								
								private int currentIndex = seekForwardAmount + 1;

								@Override
								public boolean hasNext() {
									return sortedList.size() > currentIndex && sortedList.get(currentIndex).getFeature(featureQuantiserType).getValue().compareTo(converter.castToType(upperBoundRange)) < 0;
								}

								@Override
								public ClassifiedFeatureSet next() {
									ClassifiedFeatureSet instance = sortedList.get(currentIndex++);
									latestInstanceIndex.set(currentIndex);
									
									return instance;
								}

								@Override
								public void remove() {
									throw new UnsupportedOperationException();
								}
								
							};
						}
						
					});
				}
				
				RangeFeature<T> upperBound = new RangeFeature<T>(maxValue, converter.getMaxPossibleValue(), true);
				
				processor.newRangeDetermined(upperBound, Arrays.asList(sortedList.get(sortedList.size()-1)));
				
			}
			
		};
	}
	
	private static List<ClassifiedFeatureSet> sortInstancesBasedOnFeature(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<?>> featureType){
		List<ClassifiedFeatureSet> sortedList = Lists.newArrayList(instances);
		Collections.sort(sortedList, new Comparator<ClassifiedFeatureSet>(){

			@Override
			public int compare(ClassifiedFeatureSet o1, ClassifiedFeatureSet o2) {
				Number feature1 = o1.getFeature(featureType).getValue();
				Number feature2 = o2.getFeature(featureType).getValue();
				return (int)(feature1.doubleValue() - feature2.doubleValue());
			}
			
		});
		
		return sortedList;
	}
}
