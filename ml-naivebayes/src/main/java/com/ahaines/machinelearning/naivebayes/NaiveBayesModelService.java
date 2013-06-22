package com.ahaines.machinelearning.naivebayes;

import java.util.Map;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset.ClassifiedDatasetImpl;
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

public class NaiveBayesModelService<C extends Enum<C>> implements ModelService<NaiveBayesModel<C>, C>{

	private final ContinuousFeatureQuantiser quantiser;
	
	public NaiveBayesModelService(ContinuousFeatureQuantiser quantiser){
		this.quantiser = quantiser;
	}
	@Override
	public NaiveBayesModel<C> trainModel(ClassifiedDataset<C> trainingData) {
		NaiveBayesModelFactory<C> modelFactory = new NaiveBayesModelFactory<C>(quantiser);
		
		for (ClassifiedFeatureSet<C> instance: trainingData.getInstances()){
			modelFactory.addInstance(instance);
		}
		
		return modelFactory.getModel();
	}

	@Override
	public ClassifiedProbabilityDataSet<C> classifyDataset(Dataset<? extends FeatureSet> dataset, final NaiveBayesModel<C> model) {
		
		Iterable<ClassificationProbability<C>> classifications = Iterables.transform(dataset.getInstances(), new Function<FeatureSet, ClassificationProbability<C>>(){
			
			public ClassificationProbability<C> apply(FeatureSet instance){
				return model.getClassification(instance);
			}
		});
		
		return new ClassifiedProbabilityDataSet<C>(dataset, Identifiable.UTIL.index(classifications));
	}

	public static class ClassifiedProbabilityDataSet<C extends Enum<C>> extends ClassifiedDatasetImpl<C>{

		protected ClassifiedProbabilityDataSet(Dataset<? extends FeatureSet> dataset,
				Map<Identifier, ? extends ClassificationProbability<C>> classifications) {
			super(dataset, classifications);
		}

		@Override
		public ClassifiedProbabilityFeatureSet<C> getInstance(Identifier instanceId) {
			FeatureSet instance = dataset.getInstance(instanceId);
			
			ClassificationProbability<C> classification = (ClassificationProbability<C>)classifications.get(instanceId);
			
			return new ClassifiedProbabilityFeatureSet<C>(instance, classification);
		}
		
	}
	
	public static class ClassifiedProbabilityFeatureSet<C extends Enum<C>> extends ClassifiedFeatureSet<C>{

		public ClassifiedProbabilityFeatureSet(FeatureSet instance,
				ClassificationProbability<C> classification) {
			super(instance, classification);
		}

		@Override
		public ClassificationProbability<C> getClassification() {
			return (ClassificationProbability<C>)super.getClassification();
		}
	}
}
