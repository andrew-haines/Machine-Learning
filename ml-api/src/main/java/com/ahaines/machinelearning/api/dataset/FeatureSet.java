package com.ahaines.machinelearning.api.dataset;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Defines a set of features that make up an instance.
 * @author andrewhaines
 *
 */
public interface FeatureSet extends Identifiable{
	
	/**
	 * Given a type, this returns the appropriate feature. Note that only types defined in
	 * {@link #getFeatureTypes()} can be used here.
	 * @param featureType
	 * @return
	 */
	<T extends Feature<?>> T getFeature(Class<T> featureType);
	
	/**
	 * Returns all the types of this instance
	 * @return
	 */
	Iterable<Class<? extends Feature<?>>> getFeatureTypes();
	
	Iterable<Feature<?>> getFeatures();
	
	static class FeatureSetImpl implements FeatureSet{

		private final Map<Class<? extends Feature<?>>, Feature<?>> features;
		private final Identifier id;
		private final Iterable<Class<? extends Feature<?>>> featureTypes;
		
		private FeatureSetImpl(Identifier id, Map<Class<? extends Feature<?>>, Feature<?>> features, Iterable<Class<? extends Feature<?>>> featureTypes){
			this.features = features;
			this.id = id;
			this.featureTypes = featureTypes;
		}
		
		@Override
		public <T extends Feature<?>> T getFeature(Class<T> featureType){
			@SuppressWarnings("unchecked")
			T feature = (T)features.get(featureType);
			
			if (feature == null){
				throw new IllegalArgumentException("a feature of type: "+featureType+" does not exist in this feature set");
			}
			
			return feature;
		}
		
		public Identifier getId() {
			return id;
		}
		
		public String toString(){
			StringBuilder builder = new StringBuilder("(");
			builder.append(getId());
			builder.append(")");
			for (Entry<Class<? extends Feature<?>>, Feature<?>> feature: features.entrySet()){
				builder.append("{")
					   .append(feature.getKey().getSimpleName())
					   .append(",")
					   .append(feature.getValue())
					   .append("} ");
			}
			
			return builder.toString();
		}
		
		@Override
		public Iterable<Class<? extends Feature<?>>> getFeatureTypes(){
			return featureTypes;
		}

		@Override
		public Iterable<Feature<?>> getFeatures() {
			return features.values();
		}
	}
	
	/**
	 * This factory can be used to create immutable {@link FeatureSet} instances with all features
	 * verified and set.
	 * @author andrewhaines
	 *
	 */
	public static class FeatureSetFactory{
		private final Map<Class<? extends Feature<?>>, Integer> featureWeights;
		private final int expectedFeatureWeight;
		private final Iterable<? extends Class<? extends Feature<?>>> featureTypes;
		
		public FeatureSetFactory(Iterable<? extends Class<? extends Feature<?>>> featureTypes){
			this.featureWeights = new HashMap<Class<? extends Feature<?>>, Integer>();
			
			int weightAccumulator = 0;
			
			int i = 1;
			for (Class<? extends Feature<?>> featureType: featureTypes){
				int featureWeight = (31* i);
				weightAccumulator += featureWeight;
				featureWeights.put(featureType, featureWeight);
			}
			
			this.expectedFeatureWeight = weightAccumulator;
			this.featureTypes = featureTypes;
		}
		
		@SuppressWarnings("unchecked")
		public Iterable<Class<? extends Feature<?>>> getFeatureTypes(){
			return (Iterable<Class<? extends Feature<?>>>)(Iterable<?>)featureTypes;
		}

		public FeatureSet createFeatureSet(Identifier id, Iterable<FeatureDefinition> features){
			Map<Class<? extends Feature<?>>, Feature<?>> featuresMap = new HashMap<Class<? extends Feature<?>>, Feature<?>>();
			
			int actualFeatureWeights = 0;
			for (FeatureDefinition featureDef: features){
				Class<? extends Feature<?>> currentFeatureType = featureDef.getFeatureType();
				Feature<?> feature = featureDef.getFeature();
				featuresMap.put(currentFeatureType, feature);

				Integer weight = featureWeights.get(currentFeatureType);
				
				if (weight == null){
					throw new IllegalArgumentException("the feature type: "+currentFeatureType+" is not valid for this feature set");
				}
				
				actualFeatureWeights += weight;
			}
			
			if (featureWeights.size() != featuresMap.size() || expectedFeatureWeight != actualFeatureWeights){
				throw new IllegalArgumentException("The features supplied do not contain all the features expected. feature types: "+featuresMap.keySet()+", expected feature types: "+featureWeights.keySet());
			}
			
			return new FeatureSetImpl(id, featuresMap, getFeatureTypes());
		}
	}
}
