package com.ahaines.machinelearning.naivebayes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
import com.ahaines.machinelearning.test.spam.Email.EmailClassification;
import com.ahaines.machinelearning.test.spam.Email.Features;
import com.ahaines.machinelearning.test.spam.Email.Features.Contains;
import com.ahaines.machinelearning.test.spam.Email.Features.EnlargementFeature;
import com.ahaines.machinelearning.test.spam.Email.Features.ViagraFeature;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public class NaiveBayesModelServiceUnitTest {

	private NaiveBayesModelService candidate;

	private static final Logger LOG = LoggerFactory.getLogger(NaiveBayesModelServiceUnitTest.class);
	
	private static Iterable<? extends Class<? extends Feature<?>>> DISCRETE_FEATURE_TYPES = Arrays.asList(EnlargementFeature.class, ViagraFeature.class);

	private static final FeatureSet.FeatureSetFactory DISCRETE_FACTORY = new FeatureSet.FeatureSetFactory(DISCRETE_FEATURE_TYPES);
	private static final ClassifiedDataset TEST_DISCRETE_TRAINING_SET = loadDiscreteTrainingSet();
	private static final Dataset<FeatureSet> DISCRETE_TEST_SET = loadDiscreteTestSet();
	
	private static final FeatureSet.FeatureSetFactory CONTINUOUS_FACTORY = new FeatureSet.FeatureSetFactory(Features.ALL_FEATURE_TYPES);
	private static final ClassifiedDataset TEST_CONTINUOUS_TRAINING_SET = loadContinuousTrainingSet();
	private static final Dataset<FeatureSet> CONTINUOUS_TEST_SET = loadContinuousTestSet();
	
	@Before
	public void before(){
		candidate = new NaiveBayesModelService(ContinuousFeatureQuantisers.getConstantBucketQuantiser(4));
	}
	
	private static Dataset<FeatureSet> loadContinuousTestSet() {
		DatasetBuilder builder = new DatasetBuilder(Features.ALL_FEATURE_TYPES);
		builder.addInstance(createContinuousInstance(1, Contains.ABSENT, Contains.ABSENT, 12)); // should be ham
		builder.addInstance(createContinuousInstance(2, Contains.ABSENT, Contains.ABSENT, 50)); // should be ham
		builder.addInstance(createContinuousInstance(3, Contains.ABSENT, Contains.ABSENT, 55)); // should be ham
		builder.addInstance(createContinuousInstance(4, Contains.ABSENT, Contains.ABSENT, 78)); // should be spam
		builder.addInstance(createContinuousInstance(5, Contains.ABSENT, Contains.ABSENT, 96)); // should be spam
		builder.addInstance(createContinuousInstance(6, Contains.ABSENT, Contains.ABSENT, 104)); // should be spam
		builder.addInstance(createContinuousInstance(7, Contains.ABSENT, Contains.ABSENT, 2)); // should be ham
		builder.addInstance(createContinuousInstance(8, Contains.ABSENT, Contains.ABSENT, 45)); // should be ham
		builder.addInstance(createContinuousInstance(9, Contains.ABSENT, Contains.ABSENT, 46)); // should be spam
		builder.addInstance(createContinuousInstance(10, Contains.ABSENT, Contains.ABSENT, 90)); // should be spam
		return builder.build();
	}

	private static ClassifiedDataset loadContinuousTrainingSet() {
		DatasetBuilder builder = new DatasetBuilder(Features.ALL_FEATURE_TYPES);
		Collection<Classification<EmailClassification>> classifications = new LinkedList<Classification<EmailClassification>>();
		
		builder.addInstance(createContinuousInstance(1, Contains.ABSENT, Contains.ABSENT, 56));
		classifications.add(createClassification(1, EmailClassification.HAM));
		builder.addInstance(createContinuousInstance(2, Contains.ABSENT, Contains.ABSENT, 90));
		classifications.add(createClassification(2, EmailClassification.SPAM));
		builder.addInstance(createContinuousInstance(3, Contains.ABSENT, Contains.ABSENT, 2));
		classifications.add(createClassification(3, EmailClassification.HAM));
		builder.addInstance(createContinuousInstance(4, Contains.ABSENT, Contains.ABSENT, 42));
		classifications.add(createClassification(4, EmailClassification.SPAM));
		builder.addInstance(createContinuousInstance(5, Contains.ABSENT, Contains.ABSENT, 32));
		classifications.add(createClassification(5, EmailClassification.HAM));
		builder.addInstance(createContinuousInstance(6, Contains.ABSENT, Contains.ABSENT, 76));
		classifications.add(createClassification(6, EmailClassification.SPAM));
		builder.addInstance(createContinuousInstance(7, Contains.ABSENT, Contains.ABSENT, 68));
		classifications.add(createClassification(7, EmailClassification.SPAM));
		builder.addInstance(createContinuousInstance(8, Contains.ABSENT, Contains.ABSENT, 2));
		classifications.add(createClassification(8, EmailClassification.HAM));
		builder.addInstance(createContinuousInstance(9, Contains.ABSENT, Contains.ABSENT, 43));
		classifications.add(createClassification(9, EmailClassification.HAM));
		builder.addInstance(createContinuousInstance(10, Contains.ABSENT, Contains.ABSENT, 50));
		classifications.add(createClassification(10, EmailClassification.SPAM));
		builder.addInstance(createContinuousInstance(11, Contains.ABSENT, Contains.ABSENT, 67));
		classifications.add(createClassification(11, EmailClassification.SPAM));
		builder.addInstance(createContinuousInstance(12, Contains.ABSENT, Contains.ABSENT, 87));
		classifications.add(createClassification(12, EmailClassification.SPAM));
		builder.addInstance(createContinuousInstance(13, Contains.ABSENT, Contains.ABSENT, 77));
		classifications.add(createClassification(13, EmailClassification.SPAM));
		
		return ClassifiedDataset.create(builder.build(), classifications);
	}
	
	private static Classification<EmailClassification> createClassification(int id, EmailClassification classification){
		return new Classification<EmailClassification>(Identifier.FACTORY.createIdentifier(id), classification);
	}

	private static Dataset<FeatureSet> loadDiscreteTestSet() {
		DatasetBuilder builder = new DatasetBuilder(DISCRETE_FEATURE_TYPES);
		
		builder.addInstance(createDiscreteInstance(1, Contains.ABSENT, Contains.ABSENT));
		builder.addInstance(createDiscreteInstance(2, Contains.PRESENT, Contains.PRESENT));
		builder.addInstance(createDiscreteInstance(3, Contains.PRESENT, Contains.ABSENT));
		builder.addInstance(createDiscreteInstance(4, Contains.ABSENT, Contains.PRESENT));
		
		return builder.build();
	}

	private static ClassifiedDataset loadDiscreteTrainingSet() {
		
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
			
			builder.addInstance(createDiscreteInstance(i, viagra, enlargement));
			
			classifications.add(new Classification<EmailClassification>(Identifier.FACTORY.createIdentifier(i), classification));
		}
		
		return ClassifiedDataset.create(builder.build(), classifications);
	}

	private static FeatureSet createDiscreteInstance(int id, Contains viagra, Contains enlargment) {
		return DISCRETE_FACTORY.createFeatureSet(Identifier.FACTORY.createIdentifier(id), Arrays.asList(new FeatureDefinition(new Features.ViagraFeature(viagra)), 
																							   new FeatureDefinition(new Features.EnlargementFeature(enlargment))));
	}
	
	private static FeatureSet createContinuousInstance(int id, Contains viagra, Contains enlargment, int hoursIgnored){
		return CONTINUOUS_FACTORY.createFeatureSet(Identifier.FACTORY.createIdentifier(id), Arrays.asList(new FeatureDefinition(new Features.ViagraFeature(viagra)), 
																							   new FeatureDefinition(new Features.EnlargementFeature(enlargment)),
																							   new FeatureDefinition(new Features.HoursIgnoredFeature(hoursIgnored))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenDiscreteCandidate_whenTraining2Instances_thenCorrectProbabilitiesCalculated(){
		LOG.debug("training with discrete dataset");
		NaiveBayesModel model = candidate.trainModel(TEST_DISCRETE_TRAINING_SET);
		
		ClassifiedProbabilityDataSet classifiedDataset = candidate.classifyDataset(DISCRETE_TEST_SET, model);
		
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenContinuousCandidate_whenTraining2Instances_thenCorrectProbabilitiesCalculated(){
		LOG.debug("training with continuous dataset");
		NaiveBayesModel model = candidate.trainModel(TEST_CONTINUOUS_TRAINING_SET);
		
		ClassifiedProbabilityDataSet classifiedDataset = candidate.classifyDataset(CONTINUOUS_TEST_SET, model);
		
		ClassificationProbability<EmailClassification> classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(1)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.HAM)));
		assertThat(classification.getProbability(), is(equalTo(0.6666666666666666)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(2)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(0.8571428571428572)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(3)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(0.8571428571428572)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(4)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(0.8571428571428572)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(5)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(1.0)));
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(6)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(1.0)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(7)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.HAM)));
		assertThat(classification.getProbability(), is(equalTo(1.0)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(8)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.HAM)));
		assertThat(classification.getProbability(), is(equalTo(0.6666666666666666)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(9)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(0.8571428571428572)));
		
		classification = (ClassificationProbability<EmailClassification>)classifiedDataset.getInstance(Identifier.FACTORY.createIdentifier(10)).getClassification();
		
		assertThat(classification.getValue(), is(equalTo(EmailClassification.SPAM)));
		assertThat(classification.getProbability(), is(equalTo(1.0)));
	}
}
