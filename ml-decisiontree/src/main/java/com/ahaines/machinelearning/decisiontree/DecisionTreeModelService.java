package com.ahaines.machinelearning.decisiontree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.ahaines.machinelearning.api.dataset.DiscreteFeature;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.Feature.Features;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.decisiontree.Id3Node.DecisionId3Node;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor.ImpurityProcessors;
import com.ahaines.machinelearning.decisiontree.MissingFeatureClassifier.MissingFeatureClassifiers;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
public class DecisionTreeModelService implements ModelService<Id3Model>{

	private static final double DEFAULT_HOMOGENIOUS_THRESHOLD = 0.95;
	private final ImpurityProcessor impurityProcessor;
	private final ContinuousFeatureSplitter continousFeatureSplitter;
	private final double homogeniousThreshold;
	private final MissingFeatureClassifier missingFeatureClassifier;
	
	public DecisionTreeModelService(ImpurityProcessor impurityProcessor, ContinuousFeatureSplitter continousFeatureSplitter, double homogeniousThreshold, MissingFeatureClassifier missingFeatureClassifier){
		this.impurityProcessor = impurityProcessor;
		this.continousFeatureSplitter = continousFeatureSplitter;
		this.homogeniousThreshold = homogeniousThreshold;
		this.missingFeatureClassifier = missingFeatureClassifier;
	}
	
	public DecisionTreeModelService(ImpurityProcessor impurityProcessor, ContinuousFeatureSplitter continousFeatureSplitter){
		this(impurityProcessor, continousFeatureSplitter, DEFAULT_HOMOGENIOUS_THRESHOLD, MissingFeatureClassifier.CLASSIFIERS.getHomogeniousMissingFeatureClassifier());
	}
	
	@Override
	public Id3Model trainModel(ClassifiedDataset trainingData){
		
		Id3Node root = growTree(trainingData.getInstances(), trainingData.getFeatureTypes(), new FeatureDefinition(Features.ROOT, Features.class));
		Id3Model newModel = new Id3Model(root);
		
		return newModel;
	}
	
	public boolean isHomogenious(HomogeniousRating rating) {
		return rating.maximumClassificationSplit > homogeniousThreshold;
	}
	
	private Id3Node growTree(Iterable<ClassifiedFeatureSet> instances, Iterable<Class<? extends Feature<?>>> featureTypes, FeatureDefinition featureDef){
		HomogeniousRating homogenious = getHomogeniousRating(instances);
		if (isHomogenious(homogenious) || Iterables.isEmpty(featureTypes)){
			return new Id3Node(homogenious.mostHomogeniousClassification, featureDef);
		}
		
		FeatureSplits bestFeatureSplit = getBestSplit(instances, featureTypes);
		DecisionId3Node parentNode;
		if (featureDef.getFeature() == Features.ROOT){
			parentNode = new DecisionId3Node(homogenious.mostHomogeniousClassification, new FeatureDefinition(Features.ROOT, bestFeatureSplit.featureType), missingFeatureClassifier);
		} else{
			parentNode = new DecisionId3Node(homogenious.mostHomogeniousClassification, new FeatureDefinition(featureDef.getFeature(), bestFeatureSplit.featureType), missingFeatureClassifier);
		}
		
		for (final Split split: bestFeatureSplit.splits){
			if (Iterables.isEmpty(split.getInstancesInSplit())){
				parentNode.addDecisionNode(new Id3Node(homogenious.mostHomogeniousClassification, split.getFeature()));
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
	
	private double getImpurityOfSplit(Iterable<Split> splits, int totalInstances){
		double totalImpurity = 0;
		
		for (Split split: splits){
			totalImpurity += getWeight(split, totalInstances) * impurityProcessor.getImpurity(split.getInstancesInSplit());
		}
		
		return totalImpurity;
	}

	private static double getWeight(Split split, int totalInstances) {
		return (double)split.getInstancesInSplit().size() / (double)totalInstances;
	}

	/*
	 * Looks at the proportions of all classifications and takes the biggest proportion
	 */
	private HomogeniousRating getHomogeniousRating(Iterable<ClassifiedFeatureSet> instances){
		
		Map<Enum<?>, Double> proportions = ImpurityProcessors.getProportions(instances);
		
		Double totalClassifications = 0D;
		double maximumClassificationProportion = 0;
		Enum<?> currentBestClassification = null;
		for (Entry<Enum<?>, Double> classificationProportion: proportions.entrySet()){
			totalClassifications += classificationProportion.getValue();
			if (maximumClassificationProportion < classificationProportion.getValue()){
				maximumClassificationProportion = classificationProportion.getValue();
				currentBestClassification = classificationProportion.getKey();
			}
		}
		
		return new HomogeniousRating(maximumClassificationProportion, currentBestClassification);
	}
	
	@SuppressWarnings("unchecked")	
	private FeatureSplits getBestSplit(Iterable<ClassifiedFeatureSet> instances, Iterable<Class<? extends Feature<?>>> featureTypes) {
		double minImpurity = Double.MAX_VALUE;
		Iterable<Split> bestSplits = null;
		Class<? extends Feature<?>> bestFeatureType = null;
		for (Class<? extends Feature<?>> featureType: featureTypes){
			// split instances based on feature properties.
			
			Iterable<Split> splits;
			if (DiscreteFeature.class.isAssignableFrom(featureType)){
				splits = splitDiscreteFeature(instances, (Class<? extends DiscreteFeature<?>>) featureType);
			} else if (ContinuousFeature.class.isAssignableFrom(featureType)){
				splits = splitContinuousFeature(instances, (Class<? extends ContinuousFeature<?>>)featureType);
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
		return new FeatureSplits(bestFeatureType, bestSplits);
	}
	
	private Iterable<Split> splitDiscreteFeature(Iterable<ClassifiedFeatureSet> instances, Class<? extends DiscreteFeature<?>> featureType) {
		Class<? extends DiscreteFeature<?>> discreteType = (Class<? extends DiscreteFeature<?>>)featureType;

		Collection<? extends Feature<?>> featureValues = Lists.newArrayList(discreteType.getEnumConstants());
		
		Collection<Split> allSplits = new ArrayList<Split>(featureValues.size());
		for (Feature<?> featureValue: featureValues){
			Collection<ClassifiedFeatureSet> featureSplit = Utils.toCollection(ClassifiedDataset.filterFeatureSet(instances, new FeatureDefinition(featureValue, featureType)));
			allSplits.add(new Split(new FeatureDefinition(featureValue, featureType), featureSplit));
		}
		
		return allSplits;
		
	}

	private Iterable<Split> splitContinuousFeature(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<?>> featureType) {
		
		return continousFeatureSplitter.splitInstances(instances, featureType);
	}

	private int sizeOf(Iterable<Split> splits) {
		int total = 0;
		
		for (Split split: splits){
			total += Iterables.size(split.getInstancesInSplit());
		}
		
		return total;
	}

	@Override
	public ClassifiedDataset classifyDataset(Dataset<? extends FeatureSet> dataset, Id3Model model) {
		Map<Identifier, Classification<?>> classifications = new HashMap<Identifier, Classification<?>>();
		for (FeatureSet instance: dataset.getInstances()){
			try{
				classifications.put(instance.getId(), model.getClassification(instance));
			} catch (Exception e){
				throw new RuntimeException("issue with classifying instance: "+instance, e);
			}
		}
		
		return ClassifiedDataset.create(dataset, classifications);
	}
	
	private static class HomogeniousRating{
		
		private final double maximumClassificationSplit;
		private final Enum<?> mostHomogeniousClassification;
		
		private HomogeniousRating(double rating, Enum<?> mostHomogeniousClassification){
			this.maximumClassificationSplit = rating;
			this.mostHomogeniousClassification = mostHomogeniousClassification;
		}
	}
	
	static class Split{
		private final FeatureDefinition feature;
		private final Collection<ClassifiedFeatureSet> instancesInSplit;
		
		public Split(FeatureDefinition feature, Collection<ClassifiedFeatureSet> instances){
			this.feature = feature;
			this.instancesInSplit = instances;
		}

		public Collection<ClassifiedFeatureSet> getInstancesInSplit() {
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
	
	static class FeatureSplits{
		
		private final Class<? extends Feature<?>> featureType;
		private final Iterable<Split> splits;
		
		public FeatureSplits(Class<? extends Feature<?>> featureType, Iterable<Split> splits){
			this.featureType = featureType;
			this.splits = splits;
		}
	}

}
