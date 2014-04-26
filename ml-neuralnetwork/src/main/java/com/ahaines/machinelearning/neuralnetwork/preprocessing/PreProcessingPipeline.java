package com.ahaines.machinelearning.neuralnetwork.preprocessing;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.FeatureSet;

/**
 * The following represents a single transition state machine that processes input data
 * sequentially through a pipeline prior to being fed into the neural network. Data is transformed
 * as it visits each state in the graph with the new state being fed into the following node state and
 * so on.
 * @author haines
 *
 */
public class PreProcessingPipeline<T extends Model> {

	private final Iterable<PreProcessor<T>> processors;
	
	PreProcessingPipeline(Iterable<PreProcessor<T>> processors){
		this.processors = processors;
	}
	
	/**
	 * Pre process the input dataset based on the configured processors
	 * @param dataset
	 * @return
	 */
	public <F extends FeatureSet> Dataset<F> preProcess(Dataset<F> dataset, T model){
		for (PreProcessor<T> preProcessor: processors){
			dataset = preProcessor.process(dataset, model);
		}
		
		return dataset;
	}
}
