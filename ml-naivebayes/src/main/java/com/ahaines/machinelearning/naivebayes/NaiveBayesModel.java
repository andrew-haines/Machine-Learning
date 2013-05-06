package com.ahaines.machinelearning.naivebayes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.ahaines.machinelearning.api.dataset.DiscreteFeature;
import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.api.dataset.Feature.Features;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser.QuantiserEventProcessor;
import com.ahaines.machinelearning.api.dataset.quantiser.RangeFeature;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class NaiveBayesModel<CLASSIFICATION extends Enum<CLASSIFICATION>> implements Model{

	private static final Logger LOG = LoggerFactory.getLogger(NaiveBayesModel.class);
	
	private final Map<CLASSIFICATION, Double> priorClassificationProbabilities;
	private final Map<CLASSIFICATION, Map<Class<? extends Feature<?>>, Probability>> likelihoodProbilities;
	private final Map<FeatureDefinition, Double> priorFeatureProbabilities;
	private final Metrics metrics;
	
	public NaiveBayesModel(Map<CLASSIFICATION, Double> priorClassificationProbabilities, Map<CLASSIFICATION, Map<Class<? extends Feature<?>>, Probability>> likelihoodProbilities, Map<FeatureDefinition, Double> priorFeatureProbabilities){
		this.priorClassificationProbabilities = Collections.unmodifiableMap(priorClassificationProbabilities);
		this.likelihoodProbilities = Collections.unmodifiableMap(likelihoodProbilities);
		this.metrics = new Metrics();
		this.priorFeatureProbabilities = priorFeatureProbabilities;
	}
	
	@Override
	public Metrics getMetrics() {
		return metrics;
	}
	
	public ClassificationProbability<CLASSIFICATION> getClassification(FeatureSet instance) {
		double maxProbability = 0;
		CLASSIFICATION maxClassification = null;
		
		for(CLASSIFICATION classification: priorClassificationProbabilities.keySet()){
			
			double posteriorProbabilityProduct = 1;
			Map<Class<? extends Feature<?>>, Probability> givenClassificationProbabilities = likelihoodProbilities.get(classification);
			
			if (givenClassificationProbabilities != null){ // if we have no probability then do not consider this classification
		
				for (Class<? extends Feature<?>> featureType: instance.getFeatureTypes()){
					
					Feature<?> featureValue = instance.getFeature(featureType);
					if (featureValue == Features.MISSING){
						continue; // we cannot use this feature to contribute to the probability as we dont have it!
					}
					
					// if this is a continuous probability then we need to consider all the range features and use the probability of the 
					Probability probability = givenClassificationProbabilities.get(featureType);
					if (probability == null){
						posteriorProbabilityProduct *= 0.01; // not possible. no probabilities determined for this value so this means the likely hood is very small. add an appropriately small probability
						continue;
					}
					posteriorProbabilityProduct *= probability.getProbability(featureValue);
				}
				
				posteriorProbabilityProduct *= priorClassificationProbabilities.get(classification);
				
				if (maxProbability < posteriorProbabilityProduct){
					maxProbability = posteriorProbabilityProduct;
					maxClassification = classification;
				}
			}
		}
		
		if (maxClassification == null){
			// just use the prior probabilities if there is no further information 
			
			for (Entry<CLASSIFICATION, Double> entry: priorClassificationProbabilities.entrySet()){
				if (maxProbability < entry.getValue()){
					maxProbability = entry.getValue();
					maxClassification = entry.getKey();
				}
			}
		}
		
		// calculate the maximum probability for this classification
		
		maxProbability = maxProbability / getPriorFeatureProbabilitiesProduct(instance);
		
		return new ClassificationProbability<CLASSIFICATION>(instance.getId(), maxClassification, maxProbability);
	}
	
	private double getPriorFeatureProbabilitiesProduct(FeatureSet instance) {
		double priorProduct = 1;
		for (Feature<?> feature: instance.getFeatures()){
			if (feature == Features.MISSING){
				continue;
			}
			try{
				// if the feature is a continuous feature then we need to 
				priorProduct *= priorFeatureProbabilities.get(new FeatureDefinition(feature));
			} catch (NullPointerException e){
				throw e;
			}
		}
		
		return priorProduct;
	}

	static class NaiveBayesModelFactory<CLASSIFICATION extends Enum<CLASSIFICATION>>{
		
		private final Map<CLASSIFICATION, Integer> priorCounts = new HashMap<CLASSIFICATION, Integer>();
		private final Map<CLASSIFICATION, LikelihoodCounts> discreteFeatureCounts = new HashMap<CLASSIFICATION, LikelihoodCounts>();
		private final Set<Class<? extends ContinuousFeature<?>>> continuousFeatures = new HashSet<Class<? extends ContinuousFeature<?>>>();
		private final Collection<ClassifiedFeatureSet> allInstances = new ArrayList<ClassifiedFeatureSet>();
		
		private int totalInstancesSeen = 0;
		private final ContinuousFeatureQuantiser quantiser;
		
		NaiveBayesModelFactory(ContinuousFeatureQuantiser quantiser){
			this.quantiser = quantiser;
		}
		
		@SuppressWarnings("unchecked")
		void addInstance(ClassifiedFeatureSet instance){
			totalInstancesSeen++;
			allInstances.add(instance);
			CLASSIFICATION instanceClass = getClassOfInstance(instance);
			incrementCount(instanceClass, priorCounts);
			for (Class<? extends Feature<?>> featureType: instance.getFeatureTypes()){
				Feature<?> feature = instance.getFeature(featureType);
				if (feature instanceof ContinuousFeature){
					continuousFeatures.add((Class<? extends ContinuousFeature<?>>)featureType);
				} else {
					addDiscreteFeatureCount(instanceClass, featureType, feature);
				}
			}
		}
		
		private void addDiscreteFeatureCount(CLASSIFICATION instanceClass, Class<? extends Feature<?>> featureType, Feature<?> feature){
			LikelihoodCounts counts = discreteFeatureCounts.get(instanceClass);
			
			if (counts == null){
				counts = new LikelihoodCounts();
			}
			
			counts.seenFeature(featureType, feature);
			
			discreteFeatureCounts.put(instanceClass, counts);
		}
		
		@SuppressWarnings("unchecked")
		private CLASSIFICATION getClassOfInstance(ClassifiedFeatureSet instance){
			return (CLASSIFICATION)instance.getClassification().getValue();
		}
		
		private <T> void incrementCount(T key, Map<T, Integer> accumulator){
			incrementCount(key, accumulator, 1);
		}
		
		private <T> void incrementCount(T key, Map<T, Integer> accumulator, int amount) {
			Integer currentCount = accumulator.get(key);
			
			if (currentCount == null){
				currentCount = 0;
			}
			
			accumulator.put(key, currentCount+amount);
		}

		public NaiveBayesModel<CLASSIFICATION> getModel() {
			
			// now calculate the prior and posterior probabilities
			
			Map<CLASSIFICATION, Double> priorClassificationProbabilities = new HashMap<CLASSIFICATION, Double>();
			Map<CLASSIFICATION, Map<Class<? extends Feature<?>>, Probability>> likelihoodProbabilities = new HashMap<CLASSIFICATION, Map<Class<? extends Feature<?>>, Probability>>();
			Map<FeatureDefinition, Integer> priorFeatureCounts = new HashMap<FeatureDefinition, Integer>();
			
			// prior
			
			for (Entry<CLASSIFICATION, Integer> priorCount: priorCounts.entrySet()){
				double priorProbability = (double)priorCount.getValue() / (double)totalInstancesSeen;
				priorClassificationProbabilities.put(priorCount.getKey(), priorProbability);
				LOG.debug("p("+priorCount.getKey()+") = "+priorCount.getValue()+" / "+totalInstancesSeen+" = "+priorProbability);
			}
			
			// posterior
			
			// continuous quantisation
			
			for (final Class<? extends ContinuousFeature<?>> featureType: continuousFeatures){
				quantiser.quantise(allInstances, featureType, new QuantiserEventProcessor() {
					
					@Override
					public void newRangeDetermined(RangeFeature<? extends Number> range, Iterable<ClassifiedFeatureSet> instancesInSplit) {
						for (ClassifiedFeatureSet instance: instancesInSplit){
							CLASSIFICATION instanceClass = getClassOfInstance(instance);
							
							addDiscreteFeatureCount(instanceClass, featureType, range);
						}
					}
				});
			}
			
			//discrete
			
			for (Entry<CLASSIFICATION, LikelihoodCounts> posteriorCount: discreteFeatureCounts.entrySet()){
				Map<Class<? extends Feature<?>>, ProbabilityBuilder> featureProbabilities = new HashMap<Class<? extends Feature<?>>, ProbabilityBuilder>();
				
				int totalInstancesInClassification = priorCounts.get(posteriorCount.getKey());
				for (Entry<Class<? extends Feature<?>>, FeatureCounts> featureCount: posteriorCount.getValue().featureCounts.entrySet()){
					for (Entry<FeatureDefinition, Integer> feature: featureCount.getValue().featureCounts.entrySet()){
						incrementCount(feature.getKey(), priorFeatureCounts, feature.getValue());
						
						double featurePosteriorProbability = (double)feature.getValue() / (double)totalInstancesInClassification;
						
						Feature<?> featureInstance = feature.getKey().getFeature();
						
						LOG.debug("p("+getFeatureString(feature.getKey())+"|"+posteriorCount.getKey()+") = "+feature.getValue()+" / "+totalInstancesInClassification +" = "+featurePosteriorProbability);
						
						if (featureInstance == Features.MISSING){
							// this should have already been dealt with and therefore should never happen
							throw new UnsupportedOperationException();
						} else if (featureInstance instanceof DiscreteFeature){
							ProbabilityBuilder builder = featureProbabilities.get(featureCount.getKey());
							if (builder == null){
								builder = new ProbabilityBuilder(featureCount.getKey());
							}
							builder.addProbabilityValue(featureInstance, featurePosteriorProbability);
							
							featureProbabilities.put(featureCount.getKey(), builder);
						} else if (featureInstance instanceof ContinuousFeature){
							// we should not be dealing with continuous features in this way. All continuous features should have been quantised by this point
							throw new UnsupportedOperationException();
						}
					}
				}
				
				likelihoodProbabilities.put(posteriorCount.getKey(), new HashMap<Class<? extends Feature<?>>, Probability>(Maps.transformValues(featureProbabilities, new Function<ProbabilityBuilder, Probability>(){
					
					public Probability apply(ProbabilityBuilder builder){
						return builder.build();
					}
				})));
			}
			
			return new NaiveBayesModel<CLASSIFICATION>(priorClassificationProbabilities, likelihoodProbabilities, new HashMap<FeatureDefinition, Double>(Maps.transformEntries(priorFeatureCounts, new EntryTransformer<FeatureDefinition, Integer, Double>(){
				
				public Double transformEntry(FeatureDefinition definition, Integer value){
					double priorProbability = (double)value / (double)totalInstancesSeen;
					LOG.debug("p("+getFeatureString(definition)+") = "+value+" / "+totalInstancesSeen+" = "+priorProbability);
					return priorProbability;
				}
			})));
		}

		private String getFeatureString(FeatureDefinition feature) {
			return feature.getFeatureType().getSimpleName()+"#"+((feature.getFeature() instanceof RangeFeature)?feature.getFeature().toString():feature.getFeature().getValue());
		}
	}
	
	private static class LikelihoodCounts{
		
		private final Map<Class<? extends Feature<?>>, FeatureCounts> featureCounts = new HashMap<Class<? extends Feature<?>>, FeatureCounts>();
		
		private void seenFeature(Class<? extends Feature<?>> featureType, Feature<?> featureValue){
			FeatureCounts currentCount = featureCounts.get(featureType);
			
			if (currentCount == null){
				currentCount = new FeatureCounts();
			}
			
			currentCount.increment(new FeatureDefinition(featureValue, featureType));
			
			featureCounts.put(featureType, currentCount);
		}
	}
	
	private static class FeatureCounts {
		private final Map<FeatureDefinition, Integer> featureCounts = new HashMap<FeatureDefinition, Integer>();
		
		private void increment(FeatureDefinition featureValue){
			if (featureValue.getFeature() == Feature.Features.MISSING){ // we assume all of these features are the same likelyhood.
				for (Entry<FeatureDefinition, Integer> featureCount: featureCounts.entrySet()){
					int count = featureCount.getValue();
					featureCount.setValue(++count);
				}
			} else{
				
				FeatureDefinition key = featureValue;
				Integer count = featureCounts.get(key);
				
				if (count == null){
					count = 0;
				}
				featureCounts.put(key, ++count);
			}
		}
	}
	
	public static class ClassificationProbability<T extends Enum<T>> extends com.ahaines.machinelearning.api.dataset.Classification<T>{

		private final double probability;
		
		public ClassificationProbability(Identifier id, T value, double probability) {
			super(id, value);
			this.probability = probability;
		}
		
		public double getProbability() {
			return probability;
		}
		
		public String toString(){
			return super.toString() + "("+(probability*100)+"%)";
		}
	}
	
	private static interface Probability{
		
		double getProbability(Feature<?> feature);
	}
	
	private static class DiscreteProbability implements Probability{

		private final Map<? extends Feature<?>, Double> valueProbabilities;
		
		private DiscreteProbability(Map<? extends Feature<?>, Double> valueProbabilities){
			this.valueProbabilities = valueProbabilities;
		}
		@Override
		public double getProbability(Feature<?> feature) {
			return valueProbabilities.get(feature);
		}
	}
	
	private static class RangeBasedProbability implements Probability {

		private final Map<RangeFeature<?>, Double> rangeProbabilities;
		private final List<RangeFeature<?>> binarySearch;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private RangeBasedProbability(Map<RangeFeature<?>, Double> rangeProbabilities){
			this.rangeProbabilities = rangeProbabilities;
			
			List<RangeFeature<?>> binarySearch = new ArrayList<RangeFeature<?>>(rangeProbabilities.keySet());
			
			Collections.sort((List)binarySearch);
			
			this.binarySearch = Collections.unmodifiableList(binarySearch);
		}
		@Override
		public double getProbability(Feature<?> feature) {
			int foundRangeIndex = Collections.binarySearch(binarySearch, feature);
			
			if (foundRangeIndex != -1){
				return rangeProbabilities.get(binarySearch.get(foundRangeIndex));
			} else {
				throw new IllegalStateException("Unknown range found for value: "+feature);
			}
		}
	}
	
	private static class ProbabilityBuilder{
		
		private final Map<Feature<?>, Double> rawProbabilities = new HashMap<Feature<?>, Double>();
		private final Class<? extends Feature<?>> featureType;
		
		private ProbabilityBuilder(Class<? extends Feature<?>> featureType){
			this.featureType = featureType;
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Probability build(){
			if (DiscreteFeature.class.isAssignableFrom(featureType)){
				return new DiscreteProbability(rawProbabilities);
			} else if (RangeFeature.class.isAssignableFrom(featureType)){
				return new RangeBasedProbability((Map<RangeFeature<?>, Double>)(Map)rawProbabilities);
			} else {
				throw new IllegalStateException("Unknown type: "+featureType);
			}
		}
		
		private void addProbabilityValue(Feature<?> featureValue, double probability){
			rawProbabilities.put(featureValue, probability);
		}
	}
}
