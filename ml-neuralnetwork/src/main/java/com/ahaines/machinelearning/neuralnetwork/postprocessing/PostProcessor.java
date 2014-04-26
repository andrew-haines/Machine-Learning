package com.ahaines.machinelearning.neuralnetwork.postprocessing;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;

public interface PostProcessor<T extends Model> {

	<C> ClassifiedDataset<C> process(ClassifiedDataset<C> classifiedDataSet, T model);
}
