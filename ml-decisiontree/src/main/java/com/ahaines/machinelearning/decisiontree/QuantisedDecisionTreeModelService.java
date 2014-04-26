package com.ahaines.machinelearning.decisiontree;

import java.util.Collection;
import java.util.Map;

import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.QuantisedDataset;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.api.dataset.quantiser.RangeFeature;
import com.haines.ml.model.ContinuousFeature;

public class QuantisedDecisionTreeModelService<C extends Enum<C>> extends DecisionTreeModelService<C>{
	
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
	public Id3Model<C> trainModel(ClassifiedDataset<C> trainingData) {
		
		// consider quantising continuous values before growing the tree. Should result in more generalised performance and eleminate the
		// expensive task of re evaluating the quantisation each time a continuous feature is considered.
		
		QuantisedDataset<C> quantisedTrainingData = preQuantiseDataSet(trainingData);
		
		quantisedRanges = quantisedTrainingData.getQuantisedRanges();
		return super.trainModel(quantisedTrainingData);
	}
	
	private QuantisedDataset<C> preQuantiseDataSet(ClassifiedDataset<C> trainingData) {
		
		return QuantisedDataset.discretise(trainingData, continuousFeatureQuantiser);
	}

	@Override
	protected <T extends Number & Comparable<T>> Iterable<Split<C>> splitContinuousFeature(Iterable<ClassifiedFeatureSet<C>> instances, Class<? extends ContinuousFeature<T>> featureType) {
		return super.splitDiscreteFeature(instances, featureType, quantisedRanges.get(featureType));
	}
}
