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

public class NaiveBayesModel<C extends Enum<C>> implements Model{

	private static final Logger LOG = LoggerFactory.getLogger(NaiveBayesModel.class);
	private static final double DEFAULT_NEGLIGABLE_PROBABILITY = 0.001;
	
	private final Map<C, Double> priorClassificationProbabilities;
	private final Map<C, Map<Class<? extends Feature<?>>, Probability>> likelihoodProbilities;
	private final Map<Class<? extends Feature<?>>, Probability> priorFeatureProbabilities;
	private final Metrics metrics;
	
	public NaiveBayesModel(Map<C, Double> priorClassificationProbabilities, Map<C, Map<Class<? extends Feature<?>>, Probability>> likelihoodProbilities, Map<Class<? extends Feature<?>>, Probability> priorFeatureProbabilities){
		this.priorClassificationProbabilities = Collections.unmodifiableMap(priorClassificationProbabilities);
		this.likelihoodProbilities = Collections.unmodifiableMap(likelihoodProbilities);
		this.metrics = new Metrics();
		this.priorFeatureProbabilities = priorFeatureProbabilities;
	}
	
	@Override
	public Metrics getMetrics() {
		return metrics;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ClassificationProbability<C> getClassification(FeatureSet instance) {
		double maxProbability = 0;
		Enum<?> maxClassification = null;
		
		for(C classification: priorClassificationProbabilities.keySet()){
			
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
						posteriorProbabilityProduct *= DEFAULT_NEGLIGABLE_PROBABILITY; // not possible. no probabilities determined for this value so this means the likely hood is very small. add an appropriately small probability
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
			
			for (Entry<C, Double> entry: priorClassificationProbabilities.entrySet()){
				if (maxProbability < entry.getValue()){
					maxProbability = entry.getValue();
					maxClassification = entry.getKey();
				}
			}
		}
		
		// calculate the maximum probability for this classification
		
		maxProbability = maxProbability / getPriorFeatureProbabilitiesProduct(instance);
		
		return new ClassificationProbability(instance.getId(), maxClassification, maxProbability);
	}
	
	private double getPriorFeatureProbabilitiesProduct(FeatureSet instance) {
		double priorProduct = 1;
		for (Class<? extends Feature<?>> featureType: instance.getFeatureTypes()){
			Feature<?> feature = instance.getFeature(featureType);
			if (feature == Features.MISSING){
				continue;
			}
			try{
				// if the feature is a continuous feature then we need to 
				priorProduct *= priorFeatureProbabilities.get(featureType).getProbability(feature);
			} catch (NullPointerException e){
				throw e;
			}
		}
		
		return priorProduct;
	}

	static class NaiveBayesModelFactory<C extends Enum<C>>{
		
		private final Map<C, Integer> priorCounts = new HashMap<C, Integer>();
		private final Map<C, LikelihoodCounts> discreteFeatureCounts = new HashMap<C, LikelihoodCounts>();
		private final Set<Class<? extends ContinuousFeature<?>>> continuousFeatures = new HashSet<Class<? extends ContinuousFeature<?>>>();
		private final Collection<ClassifiedFeatureSet<C>> allInstances = new ArrayList<ClassifiedFeatureSet<C>>();
		
		private int totalInstancesSeen = 0;
		private final ContinuousFeatureQuantiser quantiser;
		
		NaiveBayesModelFactory(ContinuousFeatureQuantiser quantiser){
			this.quantiser = quantiser;
		}
		
		@SuppressWarnings("unchecked")
		void addInstance(ClassifiedFeatureSet<C> instance){
			totalInstancesSeen++;
			allInstances.add(instance);
			C instanceClass = getClassOfInstance(instance);
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
		
		private void addDiscreteFeatureCount(C instanceClass, Class<? extends Feature<?>> featureType, Feature<?> feature){
			LikelihoodCounts counts = discreteFeatureCounts.get(instanceClass);
			
			if (counts == null){
				counts = new LikelihoodCounts();
			}
			
			counts.seenFeature(featureType, feature);
			
			discreteFeatureCounts.put(instanceClass, counts);
		}
		
		private C getClassOfInstance(ClassifiedFeatureSet<C> instance){
			return instance.getClassification().getValue();
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

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public NaiveBayesModel<C> getModel() {
			
			// now calculate the prior and posterior probabilities
			
			Map<C, Double> priorClassificationProbabilities = new HashMap<C, Double>();
			Map<C, Map<Class<? extends Feature<?>>, Probability>> likelihoodProbabilities = new HashMap<C, Map<Class<? extends Feature<?>>, Probability>>();
			Map<FeatureDefinition, Integer> priorFeatureCounts = new HashMap<FeatureDefinition, Integer>();
			
			// prior
			
			for (Entry<C, Integer> priorCount: priorCounts.entrySet()){
				double priorProbability = (double)priorCount.getValue() / (double)totalInstancesSeen;
				priorClassificationProbabilities.put(priorCount.getKey(), priorProbability);
				LOG.debug("p("+priorCount.getKey()+") = "+priorCount.getValue()+" / "+totalInstancesSeen+" = "+priorProbability);
			}
			
			// posterior
			
			// continuous quantisation
			
			for (final Class<? extends ContinuousFeature<?>> featureType: continuousFeatures){
				quantiser.quantise(allInstances, (Class)featureType, new QuantiserEventProcessor() {
					
					@Override
					public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> range, Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instancesInSplit) {
						LOG.debug(featureType.getSimpleName()+" range determined as: "+range);
						for (ClassifiedFeatureSet instance: instancesInSplit){
							C instanceClass = getClassOfInstance(instance);
							
							addDiscreteFeatureCount(instanceClass, featureType, range);
						}
					}
				});
			}
			
			//discrete
			
			for (Entry<C, LikelihoodCounts> posteriorCount: discreteFeatureCounts.entrySet()){
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
						} else if (featureInstance instanceof DiscreteFeature || featureInstance instanceof RangeFeature){
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
			
			// now calculate the prior feature probabilities
			
			Map<Class<? extends Feature<?>>, ProbabilityBuilder> priorFeatureProbabilities = new HashMap<Class<? extends Feature<?>>, ProbabilityBuilder>();
			for (Entry<FeatureDefinition, Integer> entry: priorFeatureCounts.entrySet()){
				double priorProbability = (double)entry.getValue() / (double)totalInstancesSeen;
				LOG.debug("p("+getFeatureString(entry.getKey())+") = "+entry.getValue()+" / "+totalInstancesSeen+" = "+priorProbability);
				
				ProbabilityBuilder builder = priorFeatureProbabilities.get(entry.getKey().getFeatureType());
				
				if (builder == null){
					builder = new ProbabilityBuilder(entry.getKey().getFeatureType());
				}
				
				builder.addProbabilityValue(entry.getKey().getFeature(), priorProbability);
				priorFeatureProbabilities.put(entry.getKey().getFeatureType(), builder);
			}
			
			return new NaiveBayesModel<C>(priorClassificationProbabilities, likelihoodProbabilities, new HashMap<Class<? extends Feature<?>>, Probability>(Maps.transformValues(priorFeatureProbabilities, new Function<ProbabilityBuilder, Probability>(){
				
				public Probability apply(ProbabilityBuilder value){
					return value.build();
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
			if (valueProbabilities == null){
				throw new NullPointerException();
			}
			this.valueProbabilities = valueProbabilities;
		}
		@Override
		public double getProbability(Feature<?> feature) {
			Double probability = valueProbabilities.get(feature);
			if (probability == null){
				return DEFAULT_NEGLIGABLE_PROBABILITY;
			}
			return probability;
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
			
			if (foundRangeIndex < 0){
				return DEFAULT_NEGLIGABLE_PROBABILITY;
			} else {
				return rangeProbabilities.get(binarySearch.get(foundRangeIndex));
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
			} else if (ContinuousFeature.class.isAssignableFrom(featureType)){
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
