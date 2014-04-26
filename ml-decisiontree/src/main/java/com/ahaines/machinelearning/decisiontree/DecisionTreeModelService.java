package com.ahaines.machinelearning.decisiontree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.decisiontree.ContinuousFeatureSplitter.ContinuousFeatureSplitters;
import com.ahaines.machinelearning.decisiontree.Id3Node.DecisionId3Node;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor.ImpurityProcessors;
import com.ahaines.machinelearning.decisiontree.MissingFeatureClassifier.MissingFeatureClassifiers;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.haines.ml.model.ContinuousFeature;
import com.haines.ml.model.DiscreteFeature;
import com.haines.ml.model.Feature;
import com.haines.ml.model.Feature.Features;

/**
 * An id3 based implementation of a decision tree classification. Note that this has been altered to handle both
 * continuous features and missing values. Missing features are treated in the following way:
 * 
 * - when training a dataset, instances with a given feature marked as missing are assumed to be part of all
 *   possible splits (as we have no idea what split the instance belongs to but it might be able to help classify subsequent features)
 *   
 * - when classifying an instance from a trained model, missing features can be classifier in one of two ways as specified in
 *   {@link MissingFeatureClassifiers}.
 *   
 * Continuous features are defined according to the {@link ContinuousFeatureSplitter} and have 2 implementations.
 * 
 * 	  - Instances are split around a single pivot value which is calculated as the average of the training set feature values
 *    - Instances are split around clusters of incremental values that result in similar classifications
 *    
 * Other configuable options are also available:
 * 
 * homogeniousThreshold: This is a property that determines how similar a collection of instance classifications should be
 *                       before they are classes as homogenious and therefore stop branching
 * 
 * impurityProcessor: This defines a number of different functions that can be used to define how impure a split of instances
 * 					  is. The provided implementations are minority class, gini index and entropy.
 *   
 * @author andrewhaines
 *
 */
public class DecisionTreeModelService<C extends Enum<C>> implements ModelService<Id3Model<C>, C>{

	private static final double DEFAULT_HOMOGENIOUS_THRESHOLD = 0.95;
	private final ImpurityProcessor impurityProcessor;
	private final ContinuousFeatureSplitter continousFeatureSplitter;
	private final double homogeniousThreshold;
	private final MissingFeatureClassifier missingFeatureClassifier;
	private final Logger LOG = LoggerFactory.getLogger(DecisionTreeModelService.class);
	
	public DecisionTreeModelService(ImpurityProcessor impurityProcessor, ContinuousFeatureQuantiser continuousFeatureQuantiser, double homogeniousThreshold, MissingFeatureClassifier missingFeatureClassifier){
		this.impurityProcessor = impurityProcessor;
		this.homogeniousThreshold = homogeniousThreshold;
		this.missingFeatureClassifier = missingFeatureClassifier;
		this.continousFeatureSplitter = ContinuousFeatureSplitters.getFeatureSplitter(continuousFeatureQuantiser);
	}
	
	public DecisionTreeModelService(ImpurityProcessor impurityProcessor, ContinuousFeatureQuantiser continuousFeatureQuantiser){
		this(impurityProcessor, continuousFeatureQuantiser, DEFAULT_HOMOGENIOUS_THRESHOLD, MissingFeatureClassifier.CLASSIFIERS.getHomogeniousMissingFeatureClassifier());
	}
	
	@Override
	public Id3Model<C> trainModel(ClassifiedDataset<C> trainingData){
		
		Id3Node<C> root = growTree(trainingData.getInstances(), trainingData.getFeatureTypes(), new FeatureDefinition(Features.ROOT, Features.class));
		Id3Model<C> newModel = new Id3Model<C>(root);
		
		if (LOG.isDebugEnabled()){
			LOG.debug("model trained against "+Iterables.size(trainingData.getInstances()));
		}
		return newModel;
	}

	public boolean isHomogenious(HomogeniousRating<C> rating) {
		return rating.maximumClassificationSplit >= homogeniousThreshold;
	}
	
	private Id3Node<C> growTree(Iterable<ClassifiedFeatureSet<C>> instances, Iterable<Class<? extends Feature<?>>> featureTypes, FeatureDefinition featureDef){
		HomogeniousRating<C> homogenious = getHomogeniousRating(instances);
		if (isHomogenious(homogenious) || Iterables.isEmpty(featureTypes)){
			if (isHomogenious(homogenious)){
				LOG.debug("Prunning");
			} else{
				LOG.debug("no more feature to split on");
			}
			return new Id3Node<C>(homogenious.mostHomogeniousClassification, featureDef);
		}
		
		FeatureSplits<C> bestFeatureSplit = getBestSplit(instances, featureTypes);
		DecisionId3Node<C> parentNode;
		if (featureDef.getFeature() == Features.ROOT){
			parentNode = new DecisionId3Node<C>(homogenious.mostHomogeniousClassification, new FeatureDefinition(Features.ROOT, bestFeatureSplit.featureType), missingFeatureClassifier);
		} else{
			parentNode = new DecisionId3Node<C>(homogenious.mostHomogeniousClassification, new FeatureDefinition(featureDef.getFeature(), bestFeatureSplit.featureType), missingFeatureClassifier);
		}
		
		for (final Split<C> split: bestFeatureSplit.splits){
			if (Iterables.isEmpty(split.getInstancesInSplit())){
				parentNode.addDecisionNode(new Id3Node<C>(homogenious.mostHomogeniousClassification, split.getFeature()));
			} else{
				parentNode.addDecisionNode(growTree(split.getInstancesInSplit(), Iterables.filter(featureTypes, new Predicate<Class<? extends Feature<?>>>(){
					
					@Override
					public boolean apply(Class<? extends Feature<?>> feature){
						return !feature.equals(split.getFeature().getFeatureType());
					}
				}), split.feature));
			}
		}
		
		
		return parentNode;
	}
	
	private double getImpurityOfSplit(Iterable<Split<C>> splits, int totalInstances){
		double totalImpurity = 0;
		
		for (Split<C> split: splits){
			totalImpurity += getWeight(split, totalInstances) * impurityProcessor.getImpurity(split.getInstancesInSplit());
		}
		
		return totalImpurity;
	}

	private static <C extends Enum<C>> double getWeight(Split<C> split, int totalInstances) {
		return (double)split.getInstancesInSplit().size() / (double)totalInstances;
	}

	/*
	 * Looks at the proportions of all classifications and takes the biggest proportion
	 */
	private HomogeniousRating<C> getHomogeniousRating(Iterable<ClassifiedFeatureSet<C>> instances){
		Map<C, Double> proportions = ImpurityProcessors.getProportions(instances);
		
		Double totalClassifications = 0D;
		double maximumClassificationProportion = 0;
		C currentBestClassification = null;
		for (Entry<C, Double> classificationProportion: proportions.entrySet()){
			totalClassifications += classificationProportion.getValue();
			if (maximumClassificationProportion < classificationProportion.getValue()){
				maximumClassificationProportion = classificationProportion.getValue();
				currentBestClassification = classificationProportion.getKey();
			}
		}
		return new HomogeniousRating<C>(maximumClassificationProportion, currentBestClassification);

	}
	
	@SuppressWarnings("unchecked")
	private FeatureSplits<C> getBestSplit(Iterable<ClassifiedFeatureSet<C>> instances, Iterable<Class<? extends Feature<?>>> featureTypes) {
		double minImpurity = Double.MAX_VALUE;
		Iterable<Split<C>> bestSplits = null;
		Class<? extends Feature<?>> bestFeatureType = null;
		for (Class<? extends Feature<?>> featureType: featureTypes){
			// split instances based on feature properties.
			
			Iterable<Split<C>> splits;
			if (DiscreteFeature.class.isAssignableFrom(featureType)){
				splits = splitDiscreteFeature(instances, (Class<DiscreteFeature<?>>) featureType);
			} else if (ContinuousFeature.class.isAssignableFrom(featureType)){
				splits = splitContinuousFeature(instances, (Class)featureType);
			} else{
				throw new IllegalArgumentException("unknown type of feature");
			}
			
			// now work out the impurity of this split and check if it is less then the current minimum
			
			double currentSplitImpurity = getImpurityOfSplit(splits, sizeOf(splits));
			if (currentSplitImpurity < minImpurity){
				minImpurity = currentSplitImpurity;
				bestSplits = splits;
				bestFeatureType = featureType;
			}
		}
		return new FeatureSplits<C>(bestFeatureType, bestSplits);

	}
	
	private Iterable<Split<C>> splitDiscreteFeature(Iterable<ClassifiedFeatureSet<C>> instances, Class<? extends DiscreteFeature<?>> featureType) {
		Class<? extends DiscreteFeature<?>> discreteType = (Class<? extends DiscreteFeature<?>>)featureType;

		Collection<? extends Feature<?>> featureValues = Lists.newArrayList(discreteType.getEnumConstants());
		
		return splitDiscreteFeature(instances, featureType, featureValues);
	}
	
	protected Iterable<Split<C>> splitDiscreteFeature(Iterable<ClassifiedFeatureSet<C>> instances, Class<? extends Feature<?>> featureType, Collection<? extends Feature<?>> allPossibleDiscreteValues){
		Collection<Split<C>> allSplits = new ArrayList<Split<C>>(allPossibleDiscreteValues.size());
		
		Map<Feature<?>, Collection<ClassifiedFeatureSet<C>>> splits = new HashMap<Feature<?>, Collection<ClassifiedFeatureSet<C>>>();
		
		for (Feature<?> feature: allPossibleDiscreteValues){
			splits.put(feature, new ArrayList<ClassifiedFeatureSet<C>>());
		}
		
		for (ClassifiedFeatureSet<C> instance: instances){
			Feature<?> featureValue = instance.getFeature(featureType);
			
			if (featureValue == Features.MISSING){ // missing features should get added to all splits.
				for (Collection<ClassifiedFeatureSet<C>> splitInstances: splits.values()){
					splitInstances.add(instance);
				}
				
			} else {
				splits.get(featureValue).add(instance);
			}
		}
		
		for (Entry<Feature<?>, Collection<ClassifiedFeatureSet<C>>> featureEntry: splits.entrySet()){
			allSplits.add(new Split<C>(new FeatureDefinition(featureEntry.getKey(), featureType), featureEntry.getValue()));
		}

		
		return allSplits;
	}

	protected <T extends Number & Comparable<T>> Iterable<Split<C>> splitContinuousFeature(Iterable<ClassifiedFeatureSet<C>> instances, Class<? extends ContinuousFeature<T>> featureType) {
		
		return continousFeatureSplitter.splitInstances(instances, featureType);

	}

	private int sizeOf(Iterable<Split<C>> splits) {
		int total = 0;
		
		for (Split<C> split: splits){
			total += Iterables.size(split.getInstancesInSplit());
		}
		
		return total;
	}

	@Override
	public ClassifiedDataset<C> classifyDataset(Dataset<? extends FeatureSet> dataset, Id3Model<C> model) {
		Map<Identifier, Classification<C>> classifications = new HashMap<Identifier, Classification<C>>();
		for (FeatureSet instance: dataset.getInstances()){
			try{
				classifications.put(instance.getId(), model.getClassification(instance));
			} catch (Exception e){
				throw new RuntimeException("issue with classifying instance: "+instance, e);
			}
		}
		
		return ClassifiedDataset.FACTORY.create(dataset, classifications);
	}
	
	private static class HomogeniousRating<C extends Enum<C>>{
		
		private final double maximumClassificationSplit;
		private final C mostHomogeniousClassification;
		
		private HomogeniousRating(double rating, C mostHomogeniousClassification){
			this.maximumClassificationSplit = rating;
			this.mostHomogeniousClassification = mostHomogeniousClassification;
		}
	}
	
	static class Split<C extends Enum<C>>{
		private final FeatureDefinition feature;
		private final Collection<ClassifiedFeatureSet<C>> instancesInSplit;
		
		public Split(FeatureDefinition feature, Collection<ClassifiedFeatureSet<C>> instances){
			this.feature = feature;
			this.instancesInSplit = instances;
		}

		public Collection<ClassifiedFeatureSet<C>> getInstancesInSplit() {
			return instancesInSplit;
		}

		public FeatureDefinition getFeature() {
			return feature;
		}
		
		@Override
		public String toString(){
			return feature+": "+instancesInSplit;
		}
	}
	
	static class FeatureSplits<C extends Enum<C>>{
		
		private final Class<? extends Feature<?>> featureType;
		private final Iterable<Split<C>> splits;
		
		public FeatureSplits(Class<? extends Feature<?>> featureType, Iterable<Split<C>> splits){
			this.featureType = featureType;
			this.splits = splits;
		}
	}

}
