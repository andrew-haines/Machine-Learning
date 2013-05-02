package com.ahaines.machinelearning.api.dataset.quantiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser.QuantiserEventProcessor;
import com.ahaines.machinelearning.test.spam.Email;
import com.ahaines.machinelearning.test.spam.Email.EmailClassification;
import com.ahaines.machinelearning.test.spam.Email.Features;
import com.ahaines.machinelearning.test.spam.Email.Features.Contains;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public class ContinuousFeatureQuantisersUnitTest {

	private static final FeatureSet.FeatureSetFactory FACTORY = new FeatureSet.FeatureSetFactory(Features.ALL_FEATURE_TYPES);
	private final static Iterable<ClassifiedFeatureSet> TEST_INSTANCES = getTestInstances();
	private ContinuousFeatureQuantiser averageQuantiserCandidate;
	private ContinuousFeatureQuantiser clusterQuantiserCandidate;
	
	@Before
	public void setUpCandidates(){
		averageQuantiserCandidate = ContinuousFeatureQuantisers.getAveragePivotQuantiser();
		clusterQuantiserCandidate = ContinuousFeatureQuantisers.getClusteredQuantiser();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenAverageFeatureQuantiser_whenCallingQuantise_then2CallbacksInstantiatedAroundAveragePoint(){
		
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet>> instances = new ArrayList<Iterable<ClassifiedFeatureSet>>();
		
		averageQuantiserCandidate.quantise(TEST_INSTANCES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public void newRangeDetermined(RangeFeature<? extends Number> newRange,	Iterable<ClassifiedFeatureSet> instanceInSplit) {
				ranges.add(newRange);
				instances.add(instanceInSplit);
			}
		});
		
		assertThat(ranges.size(), is(equalTo(2)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(60)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(63)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(12)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(Integer.MIN_VALUE)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(64)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(65)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(78)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(Integer.MAX_VALUE)), is(equalTo(false)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(60)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(63)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(12)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(Integer.MIN_VALUE)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(64)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(65)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(78)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(Integer.MAX_VALUE)), is(equalTo(true)));
	}

	private static Iterable<ClassifiedFeatureSet> getTestInstances() {
		List<ClassifiedFeatureSet> instances = new ArrayList<ClassifiedFeatureSet>();
		instances.add(createClassifiedInstance(1, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));
		instances.add(createClassifiedInstance(2, Contains.PRESENT, Contains.ABSENT, 30, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(3, Contains.PRESENT, Contains.ABSENT, 26, EmailClassification.HAM));
		instances.add(createClassifiedInstance(4, Contains.ABSENT, Contains.ABSENT, 75, EmailClassification.HAM));
		instances.add(createClassifiedInstance(5, Contains.PRESENT, Contains.PRESENT, 103, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(6, Contains.ABSENT, Contains.ABSENT, 44, EmailClassification.HAM));
		instances.add(createClassifiedInstance(7, Contains.PRESENT, Contains.ABSENT, 45, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(8, Contains.ABSENT, Contains.ABSENT, 97, EmailClassification.HAM));
		instances.add(createClassifiedInstance(9, Contains.PRESENT, Contains.ABSENT, 56, EmailClassification.HAM));
		instances.add(createClassifiedInstance(10, Contains.PRESENT, Contains.PRESENT, 100, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(11, Contains.ABSENT, Contains.ABSENT, 45, EmailClassification.HAM));
		instances.add(createClassifiedInstance(12, Contains.PRESENT, Contains.ABSENT, 98, EmailClassification.HAM));
		instances.add(createClassifiedInstance(13, Contains.PRESENT, Contains.PRESENT, 22, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(14, Contains.PRESENT, Contains.PRESENT, 65, EmailClassification.SPAM));
		
		//av = 64.714

		return instances;
	}
	
	private static ClassifiedFeatureSet createClassifiedInstance(int id, Contains viagra, Contains enlargment, int hoursIgnored, EmailClassification classification){
		return new ClassifiedFeatureSet(createInstance(id, viagra, enlargment, hoursIgnored), new Classification<EmailClassification>(Identifier.FACTORY.createIdentifier(id), classification));
	}
	
	private static FeatureSet createInstance(int id, Contains viagra, Contains enlargment, int hoursIgnored) {
		return FACTORY.createFeatureSet(Identifier.FACTORY.createIdentifier(id), Arrays.asList(new FeatureDefinition(new Features.ViagraFeature(viagra)), 
																							   new FeatureDefinition(new Features.EnlargementFeature(enlargment)), 
																							   new FeatureDefinition(new Features.HoursIgnoredFeature(hoursIgnored))));
	}
}
