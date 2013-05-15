package com.ahaines.machinelearning.main;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantisers;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModel;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModelService;

public class NaiveBayesIntegrationTest extends SimpleIntegrationTest<NaiveBayesModel>{

	@Override
	protected ModelService<NaiveBayesModel> getModelService() {
		return new NaiveBayesModelService(ContinuousFeatureQuantisers.getConstantBucketQuantiser(4));
	}

}
