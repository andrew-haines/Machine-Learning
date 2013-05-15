package com.ahaines.machinelearning.naivebayes;

import java.util.Map;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.api.dataset.Identifiable;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModel.ClassificationProbability;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModel.NaiveBayesModelFactory;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class NaiveBayesModelService implements ModelService<NaiveBayesModel>{

	private final ContinuousFeatureQuantiser quantiser;
	
	public NaiveBayesModelService(ContinuousFeatureQuantiser quantiser){
		this.quantiser = quantiser;
	}
	@Override
	public NaiveBayesModel trainModel(ClassifiedDataset trainingData) {
		NaiveBayesModelFactory modelFactory = new NaiveBayesModelFactory(quantiser);
		
		for (ClassifiedFeatureSet instance: trainingData.getInstances()){
			modelFactory.addInstance(instance);
		}
		
		return modelFactory.getModel();
	}

	@Override
	public ClassifiedProbabilityDataSet classifyDataset(Dataset<? extends FeatureSet> dataset, final NaiveBayesModel model) {
		
		Iterable<ClassificationProbability<?>> classifications = Iterables.transform(dataset.getInstances(), new Function<FeatureSet, ClassificationProbability<?>>(){
			
			public ClassificationProbability<?> apply(FeatureSet instance){
				return model.getClassification(instance);
			}
		});
		
		return new ClassifiedProbabilityDataSet(dataset, Identifiable.UTIL.index(classifications));
	}

	public static class ClassifiedProbabilityDataSet extends ClassifiedDataset{

		protected ClassifiedProbabilityDataSet(Dataset<? extends FeatureSet> dataset,
				Map<Identifier, ? extends ClassificationProbability<? extends Enum<?>>> classifications) {
			super(dataset, classifications);
		}

		@Override
		public ClassifiedProbabilityFeatureSet getInstance(Identifier instanceId) {
			FeatureSet instance = dataset.getInstance(instanceId);
			
			ClassificationProbability<?> classification = (ClassificationProbability<?>)classifications.get(instanceId);
			
			return new ClassifiedProbabilityFeatureSet(instance, classification);
		}
		
	}
	
	public static class ClassifiedProbabilityFeatureSet extends ClassifiedFeatureSet{

		public ClassifiedProbabilityFeatureSet(FeatureSet instance,
				ClassificationProbability<?> classification) {
			super(instance, classification);
		}

		@Override
		public ClassificationProbability<? extends Enum<?>> getClassification() {
			return (ClassificationProbability<?>)super.getClassification();
		}
	}
}
