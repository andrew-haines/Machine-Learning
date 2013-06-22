package com.ahaines.machinelearning.api.dataset.adultearnings;

import java.io.IOException;

import org.junit.Test;

import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedDatasetLoader;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.equalTo;

public class AdultEarningsDatasetLoadersUnitTest {

	@Test
	public void givenCandidate_whenCallingGetTrainingDatasetLoader_thenCorrectLoaderReturned() throws IOException{
		ClassifiedDatasetLoader<AdultEarningsClassificationType> loader = AdultEarningsDatasetLoaders.getTrainingDatasetLoader();
		
		ClassifiedDataset<AdultEarningsClassificationType> dataset = loader.getClassifiedDataset();
		
		assertThat(dataset, is(not(nullValue())));
		
		int size = 0;
		for (ClassifiedFeatureSet<AdultEarningsClassificationType> features: dataset.getInstances()){
			assertThat(features, is(not(nullValue())));
			size++;
		}
		
		assertThat(size, is(equalTo(32561)));
	}
}
