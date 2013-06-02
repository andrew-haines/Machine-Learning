package com.ahaines.machinelearning.main;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantisers;
import com.ahaines.machinelearning.decisiontree.DecisionTreeModelService;
import com.ahaines.machinelearning.decisiontree.Id3Model;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor.ImpurityProcessors;

public class DecisionTreeIntegrationTest extends SimpleIntegrationTest<Id3Model>{

	@Override
	protected ModelService<Id3Model> getModelService() {
		return new DecisionTreeModelService(ImpurityProcessors.getEntropyImpurityProcessor(), ContinuousFeatureQuantisers.getAveragePivotQuantiser());
	}

}
