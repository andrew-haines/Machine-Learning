package com.ahaines.machinelearning.api.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ahaines.machinelearning.api.dataset.FeatureSet.FeatureSetFactory;
import com.haines.ml.model.DiscreteFeature;
import com.haines.ml.model.Feature;
import com.haines.ml.model.ContinuousFeature.IntegerFeature;
import com.haines.ml.model.ContinuousFeature.LongFeature;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class FeatureSetUnitTest {

	@SuppressWarnings("unchecked")
	private static final Iterable<Class<? extends Feature<?>>> TEST_FEATURE_TYPES = (Iterable<Class<? extends Feature<?>>>)(Iterable<?>)Arrays.asList(TestFeature1.class, TestFeature2.class, TestFeature3.class, TestFeature4.class, TestFeature5.class);
	
	public FeatureSetFactory candidate;
	
	@Before
	public void before(){
		this.candidate = new FeatureSetFactory(TEST_FEATURE_TYPES);
	}
	
	@Test
	public void givenCorrectFeatureValues_whenCallingCreate_thenFeatureSetCreatedCorrectly(){
		
		List<FeatureDefinition> features = new ArrayList<FeatureDefinition>();
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature2(23)));
		features.add(FeatureDefinition.get(new TestFeature3(Feature1Vals.TEST_VAL1)));
		features.add(FeatureDefinition.get(new TestFeature4(Feature2Vals.TEST_VAL3)));
		features.add(FeatureDefinition.get(new TestFeature5(3466767573245L)));
		
		FeatureSet instance = candidate.createFeatureSet(Identifier.FACTORY.createIdentifier(100), features);
		
		assertThat(instance.getFeature(TestFeature1.class).getValue(), is(equalTo(12L)));
		assertThat(instance.getFeature(TestFeature2.class).getValue(), is(equalTo(23)));
		assertThat(instance.getFeature(TestFeature3.class).getValue(), is(equalTo(Feature1Vals.TEST_VAL1)));
		assertThat(instance.getFeature(TestFeature4.class).getValue(), is(equalTo(Feature2Vals.TEST_VAL3)));
		assertThat(instance.getFeature(TestFeature5.class).getValue(), is(equalTo(3466767573245L)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void givenMissingFeatureValues_whenCallingCreate_thenExceptionThrown(){
		
		List<FeatureDefinition> features = new ArrayList<FeatureDefinition>();
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature2(23)));
		features.add(FeatureDefinition.get(new TestFeature3(Feature1Vals.TEST_VAL1)));
		features.add(FeatureDefinition.get(new TestFeature4(Feature2Vals.TEST_VAL3)));
		
		candidate.createFeatureSet(Identifier.FACTORY.createIdentifier(100), features);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void givenAdditionalFeatureValues_whenCallingCreate_thenExceptionThrown(){
		
		List<FeatureDefinition> features = new ArrayList<FeatureDefinition>();
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature2(23)));
		features.add(FeatureDefinition.get(new TestFeature3(Feature1Vals.TEST_VAL1)));
		features.add(FeatureDefinition.get(new TestFeature4(Feature2Vals.TEST_VAL3)));
		features.add(FeatureDefinition.get(new TestFeature5(3466767573245L)));
		features.add(FeatureDefinition.get(new TestFeature6()));
		
		candidate.createFeatureSet(Identifier.FACTORY.createIdentifier(100), features);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void givenDuplicateFeatureValues_whenCallingCreate_thenExceptionThrown(){
		
		List<FeatureDefinition> features = new ArrayList<FeatureDefinition>();
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature2(23)));
		features.add(FeatureDefinition.get(new TestFeature3(Feature1Vals.TEST_VAL1)));
		features.add(FeatureDefinition.get(new TestFeature4(Feature2Vals.TEST_VAL3)));
		features.add(FeatureDefinition.get(new TestFeature4(Feature2Vals.TEST_VAL2)));
		
		candidate.createFeatureSet(Identifier.FACTORY.createIdentifier(100), features);
	}
	
	/*
	 * Feature weights will be the same in this example but the number of features will be different.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void givenDuplicateFeatureValues2_whenCallingCreate_thenExceptionThrown(){
		
		List<FeatureDefinition> features = new ArrayList<FeatureDefinition>();
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		features.add(FeatureDefinition.get(new TestFeature1(12L)));
		
		candidate.createFeatureSet(Identifier.FACTORY.createIdentifier(100), features);
	}
}

class TestFeature1 extends LongFeature{

	public TestFeature1(long val){
		super(val);
	}
}

class TestFeature2 extends IntegerFeature{

	public TestFeature2(int val){
		super(val);
	}
}

enum Feature1Vals{
	TEST_VAL1,
	TEST_VAL2
}

class TestFeature3 implements DiscreteFeature<Feature1Vals>{

	private final Feature1Vals val;
	
	public TestFeature3(Feature1Vals val){
		this.val = val;
	}
	@Override
	public Feature1Vals getValue() {
		return val;
	}
	@Override
	public boolean intersects(Feature<Feature1Vals> otherFeature) {
		return val == otherFeature.getValue();
	}
}

enum Feature2Vals{
	TEST_VAL1,
	TEST_VAL2,
	TEST_VAL3
}

class TestFeature4 implements DiscreteFeature<Feature2Vals>{

	private final Feature2Vals val;
	
	public TestFeature4(Feature2Vals val){
		this.val = val;
	}
	@Override
	public Feature2Vals getValue() {
		return val;
	}
	
	@Override
	public boolean intersects(Feature<Feature2Vals> otherFeature) {
		return val == otherFeature.getValue();
	}
}

class TestFeature5 extends LongFeature {
	
	public TestFeature5(long val){
		super(val);
	}
}

class TestFeature6 implements Feature<Void>{

	@Override
	public Void getValue() {
		return null;
	}
	
	@Override
	public boolean intersects(Feature<Void> otherFeature) {
		return false;
	}
	
}