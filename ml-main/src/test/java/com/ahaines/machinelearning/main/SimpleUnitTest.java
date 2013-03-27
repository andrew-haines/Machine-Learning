package com.ahaines.machinelearning.main;

import java.io.IOException;

import org.junit.Test;

import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedDatasetLoader;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.api.dataset.adultearnings.AdultEarningsClassification;
import com.ahaines.machinelearning.api.dataset.adultearnings.AdultEarningsDatasetLoaders;
import com.ahaines.machinelearning.decisiontree.Id3Model;
import com.ahaines.machinelearning.decisiontree.DecisionTreeModelService;
import com.ahaines.machinelearning.decisiontree.ContinuousFeatureSplitter.ContinuousFeatureSplitters;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor.ImpurityProcessors;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class SimpleUnitTest {

	private static final String TEST_LOCATION_SMALL_DISCRETE = "/adult.data-discrete.txt";
	private static final String TEST_LOCATION_SMALL_DISCRETE_TEST = "/adult.data-discrete-test.txt";
	private static final String TEST_LOCATION_SMALL_CONTINUOUS = "/adult.data-continuous.txt";
	private static final String TEST_LOCATION_SMALL_CONTINUOUS_TEST = "/adult.data-continuous-test.txt";
	private static final String TEST_LOCATION_SMALL_MIXED = "/adult.data-mixed.txt";
	private static final String TEST_LOCATION_SMALL_MIXED_TEST = "/adult.data-mixed-test.txt";

	@Test
	public void givenDiscreteFeatureDifferences_whenCallingTrainModel_thenModelBuiltCorrectly() throws IOException{
		ModelService<Id3Model> service = new DecisionTreeModelService(ImpurityProcessors.getEntropyImpurityProcessor(), ContinuousFeatureSplitters.getAverageFeatureSplitter());
		
		ClassifiedDatasetLoader loader = AdultEarningsDatasetLoaders.getTrainingDatasetLoader(TEST_LOCATION_SMALL_DISCRETE);
		ClassifiedDatasetLoader loaderTest = AdultEarningsDatasetLoaders.getTrainingDatasetLoader(TEST_LOCATION_SMALL_DISCRETE_TEST);
		Id3Model model = service.trainModel(loader.getClassifiedDataset());
		
		System.out.println(model);
		
		ClassifiedDataset classifiedInstances = service.classifyDataset(loaderTest.getClassifiedDataset(), model);
		
		for (ClassifiedFeatureSet featureSet: classifiedInstances.getInstances()){
			if (featureSet.getId().equals(Identifier.FACTORY.createIdentifier(0))){
				assertThat(""+featureSet, featureSet.getClassification().getValue(), is(equalTo((Object)AdultEarningsClassification.getLessThen50K())));
			} else{
				assertThat(""+featureSet, featureSet.getClassification().getValue(), is(equalTo((Object)AdultEarningsClassification.getGreaterThen50KClassification())));
			}
		}
	}
	
	@Test
	public void givenContinuousFeatureDifferences_whenCallingTrainModel_thenModelBuiltCorrectly() throws IOException{
		ModelService<Id3Model> service = new DecisionTreeModelService(ImpurityProcessors.getEntropyImpurityProcessor(), ContinuousFeatureSplitters.getClusterSplitter());
		
		ClassifiedDatasetLoader loader = AdultEarningsDatasetLoaders.getTrainingDatasetLoader(TEST_LOCATION_SMALL_CONTINUOUS);
		ClassifiedDatasetLoader loaderTest = AdultEarningsDatasetLoaders.getTrainingDatasetLoader(TEST_LOCATION_SMALL_CONTINUOUS_TEST);
		Id3Model model = service.trainModel(loader.getClassifiedDataset());
		
		System.out.println(model);
		
		ClassifiedDataset classifiedInstances = service.classifyDataset(loaderTest.getClassifiedDataset(), model);
		
		for (ClassifiedFeatureSet featureSet: classifiedInstances.getInstances()){
			if (featureSet.getId().equals(Identifier.FACTORY.createIdentifier(0))){
				assertThat(""+featureSet, featureSet.getClassification().getValue(), is(equalTo((Object)AdultEarningsClassification.getLessThen50K())));
			} else{
				assertThat(""+featureSet, featureSet.getClassification().getValue(), is(equalTo((Object)AdultEarningsClassification.getGreaterThen50KClassification())));
			}
		}
	}
	
	@Test
	public void givenMixedFeatureDifferences_whenCallingTrainModel_thenModelBuiltCorrectly() throws IOException{
		
		ModelService<Id3Model> service = new DecisionTreeModelService(ImpurityProcessors.getEntropyImpurityProcessor(), ContinuousFeatureSplitters.getClusterSplitter());
		
		ClassifiedDataset dataset = AdultEarningsDatasetLoaders.getTrainingDatasetLoader(TEST_LOCATION_SMALL_MIXED).getClassifiedDataset();
		ClassifiedDataset testdataset = AdultEarningsDatasetLoaders.getTrainingDatasetLoader(TEST_LOCATION_SMALL_MIXED_TEST).getClassifiedDataset();
		Id3Model model = service.trainModel(dataset);
		
		System.out.println(model);
		ClassifiedDataset classifiedInstances = service.classifyDataset(testdataset, model);
		for (ClassifiedFeatureSet featureSet: classifiedInstances.getInstances()){
			if (featureSet.getId().equals(Identifier.FACTORY.createIdentifier(0)) || featureSet.getId().equals(Identifier.FACTORY.createIdentifier(2))){
				assertThat(""+featureSet, featureSet.getClassification().getValue(), is(equalTo((Object)AdultEarningsClassification.getLessThen50K())));
			} else{
				assertThat(""+featureSet, featureSet.getClassification().getValue(), is(equalTo((Object)AdultEarningsClassification.getGreaterThen50KClassification())));
			}
		}
	}
}
