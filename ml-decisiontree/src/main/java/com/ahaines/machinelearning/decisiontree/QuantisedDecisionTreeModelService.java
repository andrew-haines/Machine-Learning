package com.ahaines.machinelearning.decisiontree;

import java.util.Collection;
import java.util.Map;

import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature;
import com.ahaines.machinelearning.api.dataset.QuantisedDataset;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.api.dataset.quantiser.RangeFeature;

public class QuantisedDecisionTreeModelService extends DecisionTreeModelService{
	
	private final ContinuousFeatureQuantiser continuousFeatureQuantiser;
	private Map<Class<? extends ContinuousFeature<?>>, Collection<RangeFeature<?>>> quantisedRanges;
	
	public QuantisedDecisionTreeModelService(ImpurityProcessor impurityProcessor, ContinuousFeatureQuantiser continuousFeatureQuantiser) {
		super(impurityProcessor, continuousFeatureQuantiser);
		
		this.continuousFeatureQuantiser = continuousFeatureQuantiser;
	}

	public QuantisedDecisionTreeModelService(ImpurityProcessor impurityProcessor, 
			ContinuousFeatureQuantiser continuousFeatureQuantiser,
			double homogeniousThreshold,
			MissingFeatureClassifier missingFeatureClassifier) {
		
		super(impurityProcessor, 
				continuousFeatureQuantiser, 
				homogeniousThreshold,
				missingFeatureClassifier);
		
		this.continuousFeatureQuantiser = continuousFeatureQuantiser;
	}

	@Override
	public Id3Model trainModel(ClassifiedDataset trainingData) {
		
		// consider quantising continuous values before growing the tree. Should result in more generalised performance and eleminate the
		// expensive task of re evaluating the quantisation each time a continuous feature is considered.
		
		QuantisedDataset quantisedTrainingData = preQuantiseDataSet(trainingData);
		
		quantisedRanges = quantisedTrainingData.getQuantisedRanges();
		return super.trainModel(quantisedTrainingData);
	}
	
	private QuantisedDataset preQuantiseDataSet(ClassifiedDataset trainingData) {
		
		return QuantisedDataset.discretise(trainingData, continuousFeatureQuantiser);
	}

	@Override
	protected <T extends Number & Comparable<T>> Iterable<Split> splitContinuousFeature(Iterable<ClassifiedFeatureSet> instances, Class<? extends ContinuousFeature<T>> featureType) {
		return super.splitDiscreteFeature(instances, featureType, quantisedRanges.get(featureType));
	}
}
