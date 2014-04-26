package com.ahaines.machinelearning.neuralnetwork;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.neuralnetwork.postprocessing.PostProcessingPipeline;
import com.ahaines.machinelearning.neuralnetwork.preprocessing.PreProcessingPipeline;

public class NetworkModelService<C, T extends NetworkModel<C, T>> implements ModelService<T, C>{
	
	private final PreProcessingPipeline<T> preProcessingPipeline;
	private final NetworkModelFactory<C, T> networkModelFactory;
	private final PostProcessingPipeline<T> postProcessingPipeline;
	
	private NetworkModelService(PreProcessingPipeline<T> preProcessingPipeline, NetworkModelFactory<C, T> networkModelFactory, PostProcessingPipeline<T> postProcessingPipeline){
		this.preProcessingPipeline = preProcessingPipeline;
		this.networkModelFactory = networkModelFactory;
		this.postProcessingPipeline = postProcessingPipeline;
	}
	
	@Override
	public T trainModel(ClassifiedDataset<C> trainingData) {
		
		T networkModel = networkModelFactory.createModel();
		
		preProcessingPipeline.preProcess(trainingData, networkModel);
		
		networkModel = networkModel.trainModel(trainingData, postProcessingPipeline);
		
		return networkModel;
	}

	@Override
	public ClassifiedDataset<C> classifyDataset(Dataset<? extends FeatureSet> dataset, T model) {
		return postProcessingPipeline.postProcess(model.classify(dataset), model);
	}

}
