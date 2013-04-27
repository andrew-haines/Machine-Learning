package com.ahaines.machinelearning.naivebayes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.ahaines.machinelearning.api.dataset.DiscreteFeature;
import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.api.dataset.Identifier;

public class NaiveBayesModel<Classification extends Enum<Classification>> implements Model{

	private final Map<Classification, Double> priorProbabilities;
	private final Map<Classification, Map<FeatureDefinition, Double>> posteriorProbilities;
	private final Metrics metrics;
	
	public NaiveBayesModel(Map<Classification, Double> priorProbabilities, Map<Classification, Map<FeatureDefinition, Double>> posteriorProbilities){
		this.priorProbabilities = Collections.unmodifiableMap(priorProbabilities);
		this.posteriorProbilities = Collections.unmodifiableMap(posteriorProbilities);
		this.metrics = new Metrics();
	}
	
	@Override
	public Metrics getMetrics() {
		return metrics;
	}
	
	public ClassificationProbability<Classification> getClassification(FeatureSet instance) {
		double maxProbability = 0;
		Classification maxClassification = null;
		
		for(Classification classification: priorProbabilities.keySet()){
			
			double posteriorProbabilityProduct = 1;
			Map<FeatureDefinition, Double> givenClassificationProbabilities = posteriorProbilities.get(classification);
			
			if (givenClassificationProbabilities != null){ // if we have no probability then do not consider this classification
		
				for (Class<? extends Feature<?>> featureType: instance.getFeatureTypes()){
					Double probability = givenClassificationProbabilities.get(new FeatureDefinition(instance.getFeature(featureType), featureType));
					if (probability == null){
						posteriorProbabilityProduct = 0; // not possible. no probabilities determined for this value
						break;// no point in looking at other probabilities
					}
					posteriorProbabilityProduct *= probability;
				}
				
				posteriorProbabilityProduct *= priorProbabilities.get(classification);
				
				if (maxProbability < posteriorProbabilityProduct){
					maxProbability = posteriorProbabilityProduct;
					maxClassification = classification;
				}
			}
		}
		
		return new ClassificationProbability<Classification>(instance.getId(), maxClassification, maxProbability);
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
			Integer currentCount = accumulator.get(key);
			
			if (currentCount == null){
				currentCount = 0;
			}
			
			accumulator.put(key, ++currentCount);
		}

		public NaiveBayesModel<Classification> getModel() {
			
			// now calculate the prior and posterior probabilities
			
			Map<Classification, Double> priorProbabilities = new HashMap<Classification, Double>();
			Map<Classification, Map<FeatureDefinition, Double>> posteriorProbabilities = new HashMap<Classification, Map<FeatureDefinition, Double>>();
			
			// prior
			
			for (Entry<Classification, Integer> priorCount: priorCounts.entrySet()){
				priorProbabilities.put(priorCount.getKey(), new Double((double)priorCount.getValue() / (double)totalInstancesSeen));
			}
			
			// posterior
			
			for (Entry<Classification, PosteriorCounts> posteriorCount: featureCounts.entrySet()){
				Map<FeatureDefinition, Double> featureProbabilities = new HashMap<FeatureDefinition, Double>();
				
				int totalInstancesInClassification = priorCounts.get(posteriorCount.getKey());
				for (Entry<Class<? extends Feature<?>>, FeatureCounts> featureCount: posteriorCount.getValue().featureCounts.entrySet()){
					for (Entry<Object, Integer> feature: featureCount.getValue().featureCounts.entrySet()){
						double featurePosteriorProbability = (double)feature.getValue() / (double)totalInstancesInClassification;
						
						//featureProbabilities.put(new feature.getKey(), featurePosteriorProbability);
					}
				}
				
				posteriorProbabilities.put(posteriorCount.getKey(), featureProbabilities);
			}
			
			return new NaiveBayesModel<Classification>(priorProbabilities, posteriorProbabilities);
		}
		
	}
	
	private static class PosteriorCounts{
		
		private final Map<Class<? extends Feature<?>>, FeatureCounts> featureCounts = new HashMap<Class<? extends Feature<?>>, FeatureCounts>();
		
		private void seenFeature(Class<? extends Feature<?>> featureType, Feature<?> featureValue){
			FeatureCounts currentCount = featureCounts.get(featureType);
			
			if (currentCount == null){
				currentCount = new FeatureCounts();
			}
			
			currentCount.increment(featureValue);
			
			featureCounts.put(featureType, currentCount);
		}
	}
	
	private static class FeatureCounts {
		private final Map<Object, Integer> featureCounts = new HashMap<Object, Integer>();
		
		private void increment(Feature<?> featureValue){
			if (featureValue == Feature.Features.MISSING){ // we assume all of these features are the same likelyhood.
				for (Entry<Object, Integer> featureCount: featureCounts.entrySet()){
					int count = featureCount.getValue();
					featureCount.setValue(++count);
				}
			} else{
				
				Object key = null;
				if (featureValue instanceof DiscreteFeature){
					key = featureValue.getValue();
				} else if(featureValue instanceof ContinuousFeature){
					key = null;
				}
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
