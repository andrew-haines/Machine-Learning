package com.ahaines.machinelearning.neuralnetwork.postprocessing;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;

public class PostProcessingPipeline<T extends Model> {

	private final Iterable<PostProcessor<T>> processors;
	
	PostProcessingPipeline(Iterable<PostProcessor<T>> processors){
		this.processors = processors;
	}
	
	/**
	 * Post process the input dataset based on the configured processors
	 * @param dataset
	 * @return
	 */
	public <C> ClassifiedDataset<C> postProcess(ClassifiedDataset<C> dataset, T model){
		for (PostProcessor<T> postProcessor: processors){
			dataset = postProcessor.process(dataset, model);
		}
		
		return dataset;
	}
}
