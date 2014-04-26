package com.ahaines.machinelearning.decisiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet.FeatureSetFactory;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor.ImpurityProcessors;
import com.haines.ml.model.DiscreteFeature;
import com.haines.ml.model.Feature;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.closeTo;

public class ImpurityProcessorUnitTest {

	private ImpurityProcessor minorityClassCandidate;
	private ImpurityProcessor giniCandidate;
	private ImpurityProcessor entropyCandidate;
	
	@Before
	public void before(){
		this.minorityClassCandidate = ImpurityProcessors.getMinorityClassImpurityProcessor();
		this.giniCandidate = ImpurityProcessors.getGiniIndexImpurityProcessor();
		this.entropyCandidate = ImpurityProcessors.getEntropyImpurityProcessor();
	}
	
	@Test
	public void givenPureSet_whenCallingGiniGetImpurity_thenCorrectValueReturned(){
		double impurity = giniCandidate.getImpurity(getPureFeatureSet());
		
		assertThat(impurity, is(equalTo(0.0)));
	}
	
	@Test
	public void givenImpureSet_whenCallingGiniGetImpurity_thenCorrectValueReturned(){
		double impurity = giniCandidate.getImpurity(getImpureFeatureSet());
		
		assertThat(impurity, is(closeTo(0.44, 0.01)));
	}
	
	@Test
	public void givenPureSet_whenCallingMinClassGetImpurity_thenCorrectValueReturned(){
		double impurity = minorityClassCandidate.getImpurity(getPureFeatureSet());
		
		assertThat(impurity, is(equalTo(0.0)));
	}
	
	@Test
	public void givenImpureSet_whenCallingMinClassGetImpurity_thenCorrectValueReturned(){
		double impurity = minorityClassCandidate.getImpurity(getImpureFeatureSet());
		
		assertThat(impurity, is(closeTo(0.33, 0.01)));
	}
	
	@Test
	public void givenPureSet_whenCallingEntropyGetImpurity_thenCorrectValueReturned(){
		double impurity = entropyCandidate.getImpurity(getPureFeatureSet());
		
		assertThat(impurity, is(equalTo(0.0)));
	}
	
	@Test
	public void givenImpureSet_whenCallingEntropyGetImpurity_thenCorrectValueReturned(){
		double impurity = entropyCandidate.getImpurity(getImpureFeatureSet());
		
		assertThat(impurity, is(closeTo(0.91, 0.01)));
	}

	private static Iterable<ClassifiedFeatureSet<TestClassification>> getPureFeatureSet() {
		List<ClassifiedFeatureSet<TestClassification>> features = new ArrayList<ClassifiedFeatureSet<TestClassification>>();
		
		FeatureSetFactory featureSetFactory = createFeatureSetFactory();
		
		Identifier id = Identifier.FACTORY.createIdentifier(1);
		
		ClassifiedFeatureSet<TestClassification> instance1 = new ClassifiedFeatureSet<TestClassification>(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.TRUE, BinaryFeature1.class),
				   new FeatureDefinition(BinaryFeature2.TRUE, BinaryFeature2.class))), new Classification<TestClassification>(id, TestClassification.TRUE));
		
		id = Identifier.FACTORY.createIdentifier(2);
		
		ClassifiedFeatureSet<TestClassification> instance2 = new ClassifiedFeatureSet<TestClassification>(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.TRUE, BinaryFeature1.class),
				   																														   new FeatureDefinition(BinaryFeature2.FALSE, BinaryFeature2.class))), new Classification<TestClassification>(id, TestClassification.TRUE));
		
		id = Identifier.FACTORY.createIdentifier(3);
		ClassifiedFeatureSet<TestClassification> instance3 = new ClassifiedFeatureSet<TestClassification>(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.FALSE, BinaryFeature1.class),
				   new FeatureDefinition(BinaryFeature2.FALSE, BinaryFeature2.class))), new Classification<TestClassification>(id, TestClassification.TRUE));
	
		
		features.add(instance1);
		features.add(instance2);
		features.add(instance3);
		
		return features;
	}
	
	private static Iterable<ClassifiedFeatureSet<TestClassification>> getImpureFeatureSet() {
		List<ClassifiedFeatureSet<TestClassification>> features = new ArrayList<ClassifiedFeatureSet<TestClassification>>();
		
		FeatureSetFactory featureSetFactory = createFeatureSetFactory();
		
		Identifier id = Identifier.FACTORY.createIdentifier(1);
		
		ClassifiedFeatureSet<TestClassification> instance1 = new ClassifiedFeatureSet<TestClassification>(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.TRUE, BinaryFeature1.class),
				   new FeatureDefinition(BinaryFeature2.TRUE, BinaryFeature2.class))), new Classification<TestClassification>(id, TestClassification.TRUE));
		
		id = Identifier.FACTORY.createIdentifier(2);
		
		ClassifiedFeatureSet<TestClassification> instance2 = new ClassifiedFeatureSet<TestClassification>(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.TRUE, BinaryFeature1.class),
				   																														   new FeatureDefinition(BinaryFeature2.FALSE, BinaryFeature2.class))), 
				   																	    new Classification<TestClassification>(id, TestClassification.FALSE));
		
		id = Identifier.FACTORY.createIdentifier(3);
		ClassifiedFeatureSet<TestClassification> instance3 = new ClassifiedFeatureSet<TestClassification>(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.FALSE, BinaryFeature1.class),
				   new FeatureDefinition(BinaryFeature2.FALSE, BinaryFeature2.class))), new Classification<TestClassification>(id, TestClassification.FALSE));
	
		features.add(instance1);
		features.add(instance2);
		features.add(instance3);
		
		return features;
	}

	private static FeatureSetFactory createFeatureSetFactory() {
		return new FeatureSetFactory(Arrays.<Class<? extends Feature<?>>>asList(BinaryFeature1.class, BinaryFeature2.class));
	}
	
	private static enum BinaryFeature1 implements DiscreteFeature<BinaryFeature1>{
		TRUE,
		FALSE;

		@Override
		public BinaryFeature1 getValue() {
			return this;
		}

		@Override
		public boolean intersects(Feature<BinaryFeature1> otherFeature) {
			return this == otherFeature;
		}
		
	}
	
	private static enum BinaryFeature2 implements DiscreteFeature<BinaryFeature2>{
		TRUE,
		FALSE;

		@Override
		public BinaryFeature2 getValue() {
			return this;
		}
		
		@Override
		public boolean intersects(Feature<BinaryFeature2> otherFeature) {
			return this == otherFeature;
		}
	}
	
	private static enum TestClassification {
		TRUE,
		FALSE
	}
}
