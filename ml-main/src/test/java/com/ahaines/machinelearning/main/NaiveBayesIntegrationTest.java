package com.ahaines.machinelearning.main;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.adultearnings.AdultEarningsClassificationType;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantisers;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModel;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModelService;

public class NaiveBayesIntegrationTest extends SimpleIntegrationTest<NaiveBayesModel<AdultEarningsClassificationType>>{

	@Override
	protected ModelService<NaiveBayesModel<AdultEarningsClassificationType>> getModelService() {
		return new NaiveBayesModelService<AdultEarningsClassificationType>(ContinuousFeatureQuantisers.getConstantBucketQuantiser(4));
	}

}
