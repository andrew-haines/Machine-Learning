package com.ahaines.machinelearning.api.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser.QuantiserEventProcessor;
import com.ahaines.machinelearning.api.dataset.quantiser.RangeFeature;
import com.google.common.collect.Iterables;

/**
 *Returns a dataset that contains no continuous values. All continuous values will have been discretised
 * @author andrewhaines
 *
 */
public final class QuantisedDataset<C> implements ClassifiedDataset<C>{

	private final ClassifiedDataset<C> dataset;
	private final Map<Class<? extends ContinuousFeature<?>>, Collection<RangeFeature<?>>> quantisedRanges;
	
	private QuantisedDataset(ClassifiedDataset<C> dataset, Map<Class<? extends ContinuousFeature<?>>, Collection<RangeFeature<?>>> quantisedRanges){
		this.dataset = dataset;
		this.quantisedRanges = quantisedRanges;
	}
	
	@Override
	public ClassifiedFeatureSet<C> getInstance(Identifier instanceId) {
		return dataset.getInstance(instanceId);
	}

	@Override
	public Iterable<ClassifiedFeatureSet<C>> getInstances() {
		return dataset.getInstances();
	}

	@Override
	public Iterable<Class<? extends Feature<?>>> getFeatureTypes() {
		return dataset.getFeatureTypes();
	}
	
	public Map<Class<? extends ContinuousFeature<?>>, Collection<RangeFeature<?>>> getQuantisedRanges() {
		return quantisedRanges;
	}
	
	@Override
	public Map<Identifier, ? extends Classification<C>> getClassifications() {
		return dataset.getClassifications();
	}
	
	public static <C extends Enum<C>> QuantisedDataset<C> discretise(ClassifiedDataset<C> dataset, ContinuousFeatureQuantiser quantiser){
		
		FeatureSet.FeatureSetFactory instanceFactory = new FeatureSet.FeatureSetFactory(dataset.getFeatureTypes());
		final Map<Identifier, Map<Class<? extends Feature<?>>, RangeFeature<?>>> ranges = new HashMap<Identifier, Map<Class<? extends Feature<?>>, RangeFeature<?>>>();
		Map<Class<? extends ContinuousFeature<?>>, Collection<RangeFeature<?>>> quantisedRanges = new HashMap<Class<? extends ContinuousFeature<?>>, Collection<RangeFeature<?>>>();
		int featureSetSize = Iterables.size(dataset.getFeatureTypes());
		
		// work out ranges based on all features
		for (final Class<? extends Feature<?>> featureType : dataset.getFeatureTypes()){
			if (ContinuousFeature.class.isAssignableFrom(featureType)){
				// discretise
				Collection<RangeFeature<?>> allFeatureRanges = quantiser.quantise(dataset.getInstances(), (Class)featureType, new QuantiserEventProcessor(){

					@Override
					public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> range, Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instanceInSplit) {
						for (ClassifiedFeatureSet<? extends Enum<?>> instance: instanceInSplit){
							
							Map<Class<? extends Feature<?>>, RangeFeature<?>> rangeValues = ranges.get(instance.getId());
							
							if (rangeValues == null){
								rangeValues = new HashMap<Class<? extends Feature<?>>, RangeFeature<?>>();
							}
							if (rangeValues.put(featureType, range) != null){
								throw new IllegalStateException("There should not have been a feature range already assigned for type: "+featureType.getSimpleName());
							}
							
							ranges.put(instance.getId(), rangeValues);
						}
					}
					
				});
				quantisedRanges.put((Class<? extends ContinuousFeature<?>>)featureType, allFeatureRanges);
			}
		}
		
		// now re add instances replacing continuous values with their computed ranges
		
		Dataset.DatasetBuilder builder = new Dataset.DatasetBuilder(dataset.getFeatureTypes());
		Map<Identifier, ? extends Classification<C>> classifications = dataset.getClassifications();
		
		for (ClassifiedFeatureSet<C> instance: dataset.getInstances()){
			List<FeatureDefinition> features = new ArrayList<FeatureDefinition>(featureSetSize);
			
			Map<Class<? extends Feature<?>>, RangeFeature<?>> discretisedFeatures = ranges.get(instance.getId());
			
			for (Class<? extends Feature<?>> featureType: instance.getFeatureTypes()){
				
				Feature<?> featureValue = instance.getFeature(featureType);
				if (discretisedFeatures != null){
					Feature<?> discretisedFeatureValue = discretisedFeatures.get(featureType); // see if we have a discretised feature for this type first
					
					if (discretisedFeatureValue != null){ // if not use the original feature value
						featureValue = discretisedFeatureValue;
					}
				}
				features.add(new FeatureDefinition(featureValue, featureType));
			}
				
			builder.addInstance(instanceFactory.createFeatureSet(instance.getId(), features));
		}
	
		return new QuantisedDataset<C>(ClassifiedDataset.FACTORY.create(builder.build(), classifications), quantisedRanges);
	}
}
