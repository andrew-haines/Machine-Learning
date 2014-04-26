package com.ahaines.machinelearning.neuralnetwork.preprocessing;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.FeatureSet;

/**
 * A preprocessor that transforms a dataset for a particular node in the {@link PreProcessingPipeline}
 * state machine.
 * @author haines
 *
 */
public interface PreProcessor<T extends Model> {

	/**
	 * Transforms the input dataset into the output. The dataset may or may not have the same
	 * features as the original dataset but it will need to be the same type (ie {@link ClassifiedFeatureSet} or
	 * {@link com.ahaines.machinelearning.api.dataset.FeatureSet.FeatureSetImpl})). Note that datasets
	 * are immutable so the returned instance must be a seperate instance
	 * @param dataset
	 * @param the model used to store any state required. This state should only be mutated by the 
	 * processor if it's in a training phase
	 * 
	 * @return
	 */
	public <F extends FeatureSet> Dataset<F> process(Dataset<F> dataset, T model);
}
