package com.ahaines.machinelearning.api.dataset.quantiser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ContinuousFeatureQuantisers {

	public static ContinuousFeatureQuantiser getAveragePivotQuantiser(){
		
		return new ContinuousFeatureQuantiser(){

			@Override
			public <T extends Number & Comparable<T>> Collection<RangeFeature<T>> quantise(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<T>> featureQuantiserType, QuantiserEventProcessor processor) {
				
				final Collection<RangeFeature<T>> allFeatureRanges = new ArrayList<RangeFeature<T>>();
				
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
				
				RangeFeature<T> newFeature = createRangeFeature(converter.getMinPossibleValue(), converter.castToType(average), allFeatureRanges);
				
				processor.newRangeDetermined(newFeature, Iterables.filter(instances, new Predicate<ClassifiedFeatureSet>() {
					
					public boolean apply(ClassifiedFeatureSet instance){
						return instance.getFeature(featureQuantiserType).getValue().intValue() < average;
					}
					
				}));
				
				newFeature = createRangeFeature(converter.castToType(average), converter.getMaxPossibleValue(), true, allFeatureRanges);
				
				processor.newRangeDetermined(newFeature, Iterables.filter(instances, new Predicate<ClassifiedFeatureSet>() {
					
					public boolean apply(ClassifiedFeatureSet instance){
						return instance.getFeature(featureQuantiserType).getValue().intValue() >= average;
					}
					
				}));
				
				return allFeatureRanges;
			}
			
		};
	}
	
	private static <T extends Number & Comparable<T>> RangeFeature<T> createRangeFeature(T lowerBound, T upperBound, Collection<RangeFeature<T>> allFeatureRanges){
		RangeFeature<T> newRange = new RangeFeature<T>(lowerBound, upperBound);
		allFeatureRanges.add(newRange);
		
		return newRange;
	}
	
	private static <T extends Number & Comparable<T>> RangeFeature<T> createRangeFeature(T lowerBound, T upperBound, boolean inclusive, Collection<RangeFeature<T>> allFeatureRanges){
		RangeFeature<T> newRange = new RangeFeature<T>(lowerBound, upperBound, inclusive);
		allFeatureRanges.add(newRange);
		
		return newRange;
	}
	
	public static ContinuousFeatureQuantiser getClusteredQuantiser(){
		return new ContinuousFeatureQuantiser(){

			@Override
			public <T extends Number & Comparable<T>> Collection<RangeFeature<T>> quantise(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<T>> featureQuantiserType, QuantiserEventProcessor processor) {
				
				final Collection<RangeFeature<T>> allFeatureRanges = new ArrayList<RangeFeature<T>>();

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
							
							RangeFeature<T> newRange = createRangeFeature(min, newRecord, allFeatureRanges);
							processor.newRangeDetermined(newRange, tempInstanceSplit);
							tempInstanceSplit = new ArrayList<ClassifiedFeatureSet>();
							min = newRecord;
							lastRecord = newRecord;
						}
					}
					
					tempInstanceSplit.add(instance);
				}
				
				processor.newRangeDetermined(createRangeFeature(min, converter.getMaxPossibleValue(), true, allFeatureRanges), tempInstanceSplit);
				
				return allFeatureRanges;
			}
			
		};
	}
	
	/**
	 * Returns a feature quantiser that will break a continuous feature into {@link #numBuckets} sizes. The buckets have an equal range of values they respond to. The buckets split the
	 * range between the min and max of the instances quantised with two buckets used for the remaining real numbers possible. The follows the general form of:
	 * 
	 * infinity < min
	 * min < range[1]
	 * range[1] < range[2]
	 * range[2] < range[3]
	 * ...
	 * range[n-3] < range[n-2] // note that the -2 is because 2 of the ranges are used for the < min and > max extermity bounds
	 * max > infinity
	 * 
	 * The minimum buckets you can have is 3 so if this is < 3, it will be bottom capped to 3
	 * 
	 * @param numBuckets
	 * @return
	 */
	public static ContinuousFeatureQuantiser getConstantBucketQuantiser(final int numBuckets){
		return new ContinuousFeatureQuantiser(){

			@Override
			public <T extends Number & Comparable<T>> Collection<RangeFeature<T>> quantise(Iterable<ClassifiedFeatureSet> instances, final Class<? extends ContinuousFeature<T>> featureQuantiserType, QuantiserEventProcessor processor) {
				final List<ClassifiedFeatureSet> sortedList = sortInstancesBasedOnFeature(instances, featureQuantiserType);
				final Collection<RangeFeature<T>> allFeatureRanges = new ArrayList<RangeFeature<T>>();
				final NumberConverter<T> converter = sortedList.get(0).getFeature(featureQuantiserType).getNumberConverter();
				
				T minValue = sortedList.get(0).getFeature(featureQuantiserType).getValue();
				minValue = converter.castToType(minValue.doubleValue() + 1);
				T maxValue = sortedList.get(sortedList.size()-1).getFeature(featureQuantiserType).getValue();
				
				if (minValue.compareTo(maxValue) >= 0){
					// there is no range to split as min > max. Just create a single range feature for the entire natural value of numbers
					RangeFeature<T> range = createRangeFeature(converter.getMinPossibleValue(), converter.getMaxPossibleValue(), true, allFeatureRanges);
					processor.newRangeDetermined(range, sortedList);
					
					return allFeatureRanges;
				}
				
				RangeFeature<T> lowerBound = createRangeFeature(converter.getMinPossibleValue(), minValue, allFeatureRanges);
				
				SingleValueIteratorFromSortedList<T> it = new SingleValueIteratorFromSortedList<T>(minValue, sortedList, featureQuantiserType, true);
				
				processor.newRangeDetermined(lowerBound, it);
				
				// inbetween bands
				
				int numRangeBuckets = numBuckets - 2; // subtract the 2 extremity bounds
				
				if (numRangeBuckets < 1){
					numRangeBuckets = 1; // we have to have at least 1 range bucket
				}
				
				double range = (maxValue.doubleValue() - minValue.doubleValue()) / numRangeBuckets;
				
				final AtomicInteger latestInstanceIndex = new AtomicInteger(it.getIteratorSize());
				for (int i = 0; i < numRangeBuckets; i++){
					double lowerBoundRange = range * i + minValue.doubleValue();
					final double upperBoundRange = lowerBoundRange + range;
					
					
					final T typedLowerBound = converter.castToType(lowerBoundRange);
					final T typedUpperBound = converter.castToType(upperBoundRange);
					
					RangeFeature<T> newRange = createRangeFeature(typedLowerBound, typedUpperBound, allFeatureRanges);
					final int seekForwardAmount = latestInstanceIndex.get(); // optimisation because we dont want to re-iterate over instance that we have already added. Note that this only works if, the caller iterates.
					processor.newRangeDetermined(newRange, new Iterable<ClassifiedFeatureSet>(){

						@Override
						public Iterator<ClassifiedFeatureSet> iterator() {
							return new Iterator<ClassifiedFeatureSet>(){
								
								private int currentIndex = seekForwardAmount;

								@Override
								public boolean hasNext() {
									
									boolean isInRange = false;
									while(!isInRange && currentIndex < sortedList.size()){
										if (sortedList.get(currentIndex).getFeature(featureQuantiserType).getValue().compareTo(typedLowerBound) < 0){
											currentIndex++;
										} else{
											isInRange = true;
										}
									}
									return sortedList.size() > currentIndex && sortedList.get(currentIndex).getFeature(featureQuantiserType).getValue().compareTo(typedUpperBound) < 0;
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
				
				RangeFeature<T> upperBound = createRangeFeature(maxValue, converter.getMaxPossibleValue(), true, allFeatureRanges);
				
				processor.newRangeDetermined(upperBound, new SingleValueIteratorFromSortedList<T>(maxValue, sortedList, featureQuantiserType, false));
				
				return allFeatureRanges;
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
	
	private static class SingleValueIteratorFromSortedList<T extends Number & Comparable<T>> implements Iterable<ClassifiedFeatureSet>{

		private static final int NOT_INITALISED = -1;
		private final T value;
		private final Iterable<ClassifiedFeatureSet> items;
		private final Class<? extends ContinuousFeature<T>> featureType;
		private final boolean greaterThan;
		private int iteratorSize = NOT_INITALISED;
		
		private SingleValueIteratorFromSortedList(T value, List<ClassifiedFeatureSet> items, Class<? extends ContinuousFeature<T>> featureType, boolean greaterThan){
			this.value = value;
			if(greaterThan){
				this.items = items;
			} else{
				this.items = Lists.reverse(items);
			}
			this.featureType = featureType;
			this.greaterThan = greaterThan;
		}
		@Override
		public Iterator<ClassifiedFeatureSet> iterator() {
			
			final Iterator<ClassifiedFeatureSet> masterIt = items.iterator();
			iteratorSize = 0;
			return new Iterator<ClassifiedFeatureSet>(){

				boolean hasFinished = false;
				ClassifiedFeatureSet nextValue;
				@Override
				public boolean hasNext() {
					
					if (!masterIt.hasNext() || hasFinished){
						return false;
					}
					if (nextValue == null){
						nextValue = masterIt.next();
					}
					
					if (greaterThan){
						if (nextValue.getFeature(featureType).getValue().compareTo(value) > 0){
							hasFinished = true;
						}
					} else {
						if (nextValue.getFeature(featureType).getValue().compareTo(value) < 0){
							hasFinished = true;
						}
					}
					
					return !hasFinished;
				}

				@Override
				public ClassifiedFeatureSet next() {
					if (nextValue == null){
						nextValue = masterIt.next();
					}
					try{
						return nextValue;
					} finally{
						nextValue = null;
						iteratorSize++;
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
		public int getIteratorSize() {
			if (iteratorSize == NOT_INITALISED){
				for (ClassifiedFeatureSet value: this){}
			}
			return iteratorSize;
		}
		
	}
}
