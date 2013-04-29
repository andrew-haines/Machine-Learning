package com.ahaines.machinelearning.naivebayes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class NaiveBayesModel<CLASSIFICATION extends Enum<CLASSIFICATION>> implements Model{

	private static final Logger LOG = LoggerFactory.getLogger(NaiveBayesModel.class);
	
	private final Map<CLASSIFICATION, Double> priorClassificationProbabilities;
	private final Map<CLASSIFICATION, Map<FeatureDefinition, Double>> likelihoodProbilities;
	private final Map<FeatureDefinition, Double> priorFeatureProbabilities;
	private final Metrics metrics;
	
	public NaiveBayesModel(Map<CLASSIFICATION, Double> priorClassificationProbabilities, Map<CLASSIFICATION, Map<FeatureDefinition, Double>> likelihoodProbilities, Map<FeatureDefinition, Double> priorFeatureProbabilities){
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
			Map<FeatureDefinition, Double> givenClassificationProbabilities = likelihoodProbilities.get(classification);
			
			if (givenClassificationProbabilities != null){ // if we have no probability then do not consider this classification
		
				for (Class<? extends Feature<?>> featureType: instance.getFeatureTypes()){
					
					if (instance.getFeature(featureType) == Features.MISSING){
						continue; // we cannot use this feature to contribute to the probability as we dont have it!
					}
					
					Double probability = givenClassificationProbabilities.get(new FeatureDefinition(instance.getFeature(featureType), featureType));
					if (probability == null){
						posteriorProbabilityProduct *= 0.01; // not possible. no probabilities determined for this value so this means the likely hood is very small. add an appropriately small probability
						continue;
					}
					posteriorProbabilityProduct *= probability;
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
			priorProduct *= priorFeatureProbabilities.get(new FeatureDefinition(feature));
		}
		
		return priorProduct;
	}

	static class NaiveBayesModelFactory<Classification extends Enum<Classification>>{
		
		private final Map<Classification, Integer> priorCounts = new HashMap<Classification, Integer>();
		private final Map<Classification, PosteriorCounts> featureCounts = new HashMap<Classification, PosteriorCounts>();
		private int totalInstancesSeen = 0;
		
		@SuppressWarnings("unchecked")
		void addInstance(ClassifiedFeatureSet instance){
			totalInstancesSeen++;
			Classification instanceClass = (Classification)instance.getClassification().getValue();
			incrementCount(instanceClass, priorCounts);
			for (Class<? extends Feature<?>> featureType: instance.getFeatureTypes()){
				Feature<?> feature = instance.getFeature(featureType);
				if (feature == Feature.Features.MISSING){
					// if this is a missing feature type then 
				} else if (feature instanceof ContinuousFeature){
					// create feature split
				} else {
					PosteriorCounts counts = featureCounts.get(instanceClass);
					
					if (counts == null){
						counts = new PosteriorCounts();
					}
					
					counts.seenFeature(featureType, feature);
					
					featureCounts.put(instanceClass, counts);
				}
				
			}
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

		public NaiveBayesModel<Classification> getModel() {
			
			// now calculate the prior and posterior probabilities
			
			Map<Classification, Double> priorClassificationProbabilities = new HashMap<Classification, Double>();
			Map<Classification, Map<FeatureDefinition, Double>> likelihoodProbabilities = new HashMap<Classification, Map<FeatureDefinition, Double>>();
			Map<FeatureDefinition, Integer> priorFeatureCounts = new HashMap<FeatureDefinition, Integer>();
			
			// prior
			
			for (Entry<Classification, Integer> priorCount: priorCounts.entrySet()){
				double priorProbability = (double)priorCount.getValue() / (double)totalInstancesSeen;
				priorClassificationProbabilities.put(priorCount.getKey(), priorProbability);
				LOG.debug("p("+priorCount.getKey()+") = "+priorCount.getValue()+" / "+totalInstancesSeen+" = "+priorProbability);
			}
			
			// posterior
			
			for (Entry<Classification, PosteriorCounts> posteriorCount: featureCounts.entrySet()){
				Map<FeatureDefinition, Double> featureProbabilities = new HashMap<FeatureDefinition, Double>();
				
				int totalInstancesInClassification = priorCounts.get(posteriorCount.getKey());
				for (Entry<Class<? extends Feature<?>>, FeatureCounts> featureCount: posteriorCount.getValue().featureCounts.entrySet()){
					for (Entry<FeatureDefinition, Integer> feature: featureCount.getValue().featureCounts.entrySet()){
						incrementCount(feature.getKey(), priorFeatureCounts, feature.getValue());
						
						double featurePosteriorProbability = (double)feature.getValue() / (double)totalInstancesInClassification;
						
						Feature<?> featureInstance = feature.getKey().getFeature();
						
						LOG.debug("p("+feature.getKey().getFeatureType().getSimpleName()+"#"+feature.getKey().getFeature().getValue()+"|"+posteriorCount.getKey()+") = "+feature.getValue()+" / "+totalInstancesInClassification +" = "+featurePosteriorProbability);
						
						if (featureInstance == Features.MISSING){
							// for all features add a count
							throw new UnsupportedOperationException();
						} else if (featureInstance instanceof DiscreteFeature){
							featureProbabilities.put(feature.getKey(), featurePosteriorProbability);
						} else if (featureInstance instanceof ContinuousFeature){
							// work out what the ranges are for this split type.
							throw new UnsupportedOperationException();
						}
					}
				}
				
				likelihoodProbabilities.put(posteriorCount.getKey(), featureProbabilities);
			}
			
			return new NaiveBayesModel<Classification>(priorClassificationProbabilities, likelihoodProbabilities, new HashMap<FeatureDefinition, Double>(Maps.transformValues(priorFeatureCounts, new Function<Integer, Double>(){
				
				public Double apply(Integer value){
					return (double)value / (double)totalInstancesSeen;
				}
			})));
		}
	}
	
	private static class PosteriorCounts{
		
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
}
