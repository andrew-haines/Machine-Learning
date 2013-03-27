package com.ahaines.machinelearning.decisiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.DiscreteFeature;
import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet.FeatureSetFactory;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor.ImpurityProcessors;

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
		
		assertThat(impurity, is(equalTo(2.0)));
	}
	
	@Test
	public void givenImpureSet_whenCallingGiniGetImpurity_thenCorrectValueReturned(){
		double impurity = giniCandidate.getImpurity(getImpureFeatureSet());
		
		assertThat(impurity, is(closeTo(0.44, 0.01)));
	}
	
	@Test
	public void givenPureSet_whenCallingMinClassGetImpurity_thenCorrectValueReturned(){
		double impurity = minorityClassCandidate.getImpurity(getPureFeatureSet());
		
		assertThat(impurity, is(equalTo(1.0)));
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Iterable<ClassifiedFeatureSet> getPureFeatureSet() {
		List<ClassifiedFeatureSet> features = new ArrayList<ClassifiedFeatureSet>();
		
		FeatureSetFactory featureSetFactory = createFeatureSetFactory();
		
		Identifier id = Identifier.FACTORY.createIdentifier(1);
		
		ClassifiedFeatureSet instance1 = new ClassifiedFeatureSet(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.TRUE, BinaryFeature1.class),
				   new FeatureDefinition(BinaryFeature2.TRUE, BinaryFeature2.class))), new Classification(id, TestClassification.TRUE));
		
		id = Identifier.FACTORY.createIdentifier(2);
		
		ClassifiedFeatureSet instance2 = new ClassifiedFeatureSet(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.TRUE, BinaryFeature1.class),
				   																														   new FeatureDefinition(BinaryFeature2.FALSE, BinaryFeature2.class))), new Classification(id, TestClassification.TRUE));
		
		id = Identifier.FACTORY.createIdentifier(3);
		ClassifiedFeatureSet instance3 = new ClassifiedFeatureSet(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.FALSE, BinaryFeature1.class),
				   new FeatureDefinition(BinaryFeature2.FALSE, BinaryFeature2.class))), new Classification(id, TestClassification.TRUE));
	
		
		features.add(instance1);
		features.add(instance2);
		features.add(instance3);
		
		return features;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Iterable<ClassifiedFeatureSet> getImpureFeatureSet() {
		List<ClassifiedFeatureSet> features = new ArrayList<ClassifiedFeatureSet>();
		
		FeatureSetFactory featureSetFactory = createFeatureSetFactory();
		
		Identifier id = Identifier.FACTORY.createIdentifier(1);
		
		ClassifiedFeatureSet instance1 = new ClassifiedFeatureSet(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.TRUE, BinaryFeature1.class),
				   new FeatureDefinition(BinaryFeature2.TRUE, BinaryFeature2.class))), new Classification(id, TestClassification.TRUE));
		
		id = Identifier.FACTORY.createIdentifier(2);
		
		ClassifiedFeatureSet instance2 = new ClassifiedFeatureSet(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.TRUE, BinaryFeature1.class),
				   																														   new FeatureDefinition(BinaryFeature2.FALSE, BinaryFeature2.class))), 
				   																	    new Classification(id, TestClassification.FALSE));
		
		id = Identifier.FACTORY.createIdentifier(3);
		ClassifiedFeatureSet instance3 = new ClassifiedFeatureSet(featureSetFactory.createFeatureSet(id, Arrays.asList(new FeatureDefinition(BinaryFeature1.FALSE, BinaryFeature1.class),
				   new FeatureDefinition(BinaryFeature2.FALSE, BinaryFeature2.class))), new Classification(id, TestClassification.FALSE));
	
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
