package com.ahaines.machinelearning.naivebayes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.Dataset.DatasetBuilder;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantisers;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModel.ClassificationProbability;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModelService.ClassifiedProbabilityDataSet;
import com.ahaines.machinelearning.test.spam.Email;
import com.ahaines.machinelearning.test.spam.Email.EmailClassification;
import com.ahaines.machinelearning.test.spam.Email.Features.Contains;
import com.ahaines.machinelearning.test.spam.Email.Features.EnlargementFeature;
import com.ahaines.machinelearning.test.spam.Email.Features.ViagraFeature;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public class NaiveBayesModelServiceUnitTest {

	private NaiveBayesModelService<EmailClassification> candidate;

	private static final Logger LOG = LoggerFactory.getLogger(NaiveBayesModelServiceUnitTest.class);
	
	private static Iterable<? extends Class<? extends Feature<?>>> DISCRETE_FEATURE_TYPES = Arrays.asList(EnlargementFeature.class, ViagraFeature.class);

	private static final FeatureSet.FeatureSetFactory FACTORY = new FeatureSet.FeatureSetFactory(DISCRETE_FEATURE_TYPES);
	private static final ClassifiedDataset TEST_TRAINING_SET = loadTrainingSet();
	private static final Dataset<FeatureSet> TEST_SET = loadTestSet();
	
	@Before
	public void before(){
		candidate = new NaiveBayesModelService<EmailClassification>(ContinuousFeatureQuantisers.getClusteredQuantiser());
	}
	
	private static Dataset<FeatureSet> loadTestSet() {
		DatasetBuilder builder = new DatasetBuilder(DISCRETE_FEATURE_TYPES);
		
		builder.addInstance(createInstance(1, Contains.ABSENT, Contains.ABSENT));
		builder.addInstance(createInstance(2, Contains.PRESENT, Contains.PRESENT));
		builder.addInstance(createInstance(3, Contains.PRESENT, Contains.ABSENT));
		builder.addInstance(createInstance(4, Contains.ABSENT, Contains.PRESENT));
		
		return builder.build();
	}

	private static ClassifiedDataset loadTrainingSet() {
		
		DatasetBuilder builder = new DatasetBuilder(DISCRETE_FEATURE_TYPES);
		
		// example from https://www.bionicspirit.com/blog/2012/02/09/howto-build-naive-bayes-classifier.html
		List<Classification<EmailClassification>> classifications = new ArrayList<Classification<EmailClassification>>(74);
		for (int i = 0 ;i < 74; i++){
			EmailClassification classification = EmailClassification.HAM;
			Contains viagra = Contains.ABSENT;
			Contains enlargement = Contains.ABSENT;
			if (i< 30){
				classification = EmailClassification.SPAM;
			}
			
			if (i < 20 || (i >= 30 && i < (30+(51-20)))){ // first 20 are spam ones, after 30 these are ham emails containing enlargement
				enlargement = Contains.PRESENT;
			}
			
			if (i >= 6 && i < 30){ // 24 viagra ones marked as spam
				viagra = Contains.PRESENT;
			}
			
			if (i == 45){ // makes the 25
				viagra = Contains.PRESENT;
			}
			
			builder.addInstance(createInstance(i, viagra, enlargement));
			
			classifications.add(new Classification<EmailClassification>(Identifier.FACTORY.createIdentifier(i), classification));
		}
		
		return ClassifiedDataset.create(builder.build(), classifications);
	}

	private static FeatureSet createInstance(int id, Contains viagra, Contains enlargment) {
		return FACTORY.createFeatureSet(Identifier.FACTORY.createIdentifier(id), Arrays.asList(new FeatureDefinition(new Email.Features.ViagraFeature(viagra)), 
																							   new FeatureDefinition(new Email.Features.EnlargementFeature(enlargment))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenCandidate_whenTraining2Instances_thenCorrectProbabilitiesCalculated(){
		NaiveBayesModel<EmailClassification> model = candidate.trainModel(TEST_TRAINING_SET);
		
		ClassifiedProbabilityDataSet classifiedDataset = candidate.classifyDataset(TEST_SET, model);
		
		ClassificationProbability<EmailClassification> classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(1)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.HAM)));
		assertThat(classification.getProbability(), is(equalTo(0.8341937565540051)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(2)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(0.9286274509803922)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(3)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(1.0295652173913046))); //TODO investigate why this is > 1
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(4)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.HAM)));
		assertThat(classification.getProbability(), is(equalTo(0.8971042962639604)));
	}
}
