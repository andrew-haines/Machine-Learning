package com.ahaines.machinelearning.neuralnetwork;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.neuralnetwork.postprocessing.PostProcessingPipeline;

public abstract class NetworkModel<C, T extends NetworkModel<C, T>> implements Model{

	/**
	 * Trains the current instance of the model based on the supplied dataset and provided post processors.
	 * This implementation might change so that the post processor pipeline is not required. This would mean
	 * some generic functionality might need to be extracted from the training process and place in the
	 * service class where it really belongs.
	 * @param dataset
	 * @param postProcessors
	 * @return
	 */
	/*DEFAULT*/abstract T trainModel(ClassifiedDataset<C> dataset, PostProcessingPipeline<T> postProcessors);
	
	/**
	 * Given a dataset, return a classified representation of that dataset
	 * @param dataset
	 * @return
	 */
	/*DEFAULT*/ abstract ClassifiedDataset<C> classify(Dataset<? extends FeatureSet> dataset);
	
	
	@Override
	public Metrics getMetrics() {
		// TODO Auto-generated method stub
		return null;
	}

}
