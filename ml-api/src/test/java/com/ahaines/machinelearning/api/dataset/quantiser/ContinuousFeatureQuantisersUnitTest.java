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
import com.ahaines.machinelearning.api.util.Utils;
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
	private ContinuousFeatureQuantiser constantBucketCandidate;
	
	@Before
	public void setUpCandidates(){
		averageQuantiserCandidate = ContinuousFeatureQuantisers.getAveragePivotQuantiser();
		clusterQuantiserCandidate = ContinuousFeatureQuantisers.getClusteredQuantiser();
		constantBucketCandidate = ContinuousFeatureQuantisers.getConstantBucketQuantiser(10);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenAverageFeatureQuantiser_whenCallingQuantise_then2CallbacksInstantiatedAroundAveragePoint(){
		
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet>> instances = new ArrayList<Iterable<ClassifiedFeatureSet>>();
		
		averageQuantiserCandidate.quantise(TEST_INSTANCES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<ClassifiedFeatureSet> instanceInSplit) {
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
		instances.add(createClassifiedInstance(4, Contains.ABSENT, Contains.ABSENT, 75, EmailClassification.SPAM));
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenClusterFeatureQuantiser_whenCallingQuantise_then2CallbacksInstantiatedAroundAveragePoint(){
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet>> instances = new ArrayList<Iterable<ClassifiedFeatureSet>>();
		
		clusterQuantiserCandidate.quantise(TEST_INSTANCES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<ClassifiedFeatureSet> instanceInSplit) {
				ranges.add(newRange);
				instances.add(instanceInSplit);
			}
		});
		
		assertThat(ranges.size(), is(equalTo(9)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(true)));
		assertThat(Utils.toCollection(instances.get(0)).size(), is(equalTo(1)));
		assertThat(Utils.toCollection(instances.get(0)).toArray(new ClassifiedFeatureSet[]{})[0].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(13))));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(23)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(25)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(26)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(28)), is(equalTo(false)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(26)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(25)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(29)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(30)), is(equalTo(false)));
		assertThat(Utils.toCollection(instances.get(1)).size(), is(equalTo(1)));
		assertThat(Utils.toCollection(instances.get(1)).toArray(new ClassifiedFeatureSet[]{})[0].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(3))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(29)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(30)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(31)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(43)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(44)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(45)), is(equalTo(false)));
		assertThat(Utils.toCollection(instances.get(2)).size(), is(equalTo(1)));
		assertThat(Utils.toCollection(instances.get(2)).toArray(new ClassifiedFeatureSet[]{})[0].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(2))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(43)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(44)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(45)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(46)), is(equalTo(false)));
		assertThat(Utils.toCollection(instances.get(3)).size(), is(equalTo(1)));
		assertThat(Utils.toCollection(instances.get(3)).toArray(new ClassifiedFeatureSet[]{})[0].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(6))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(4)).intersects(new Features.HoursIgnoredFeature(44)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(4)).intersects(new Features.HoursIgnoredFeature(45)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(4)).intersects(new Features.HoursIgnoredFeature(50)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(4)).intersects(new Features.HoursIgnoredFeature(55)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(4)).intersects(new Features.HoursIgnoredFeature(56)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(4)).intersects(new Features.HoursIgnoredFeature(60)), is(equalTo(false)));
		assertThat(Utils.toCollection(instances.get(4)).size(), is(equalTo(2)));
		assertThat(Utils.toCollection(instances.get(4)).toArray(new ClassifiedFeatureSet[]{})[0].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(7))));
		assertThat(Utils.toCollection(instances.get(4)).toArray(new ClassifiedFeatureSet[]{})[1].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(11))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(5)).intersects(new Features.HoursIgnoredFeature(55)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(5)).intersects(new Features.HoursIgnoredFeature(56)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(5)).intersects(new Features.HoursIgnoredFeature(60)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(5)).intersects(new Features.HoursIgnoredFeature(64)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(5)).intersects(new Features.HoursIgnoredFeature(65)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(5)).intersects(new Features.HoursIgnoredFeature(70)), is(equalTo(false)));
		assertThat(Utils.toCollection(instances.get(5)).size(), is(equalTo(1)));
		assertThat(Utils.toCollection(instances.get(5)).toArray(new ClassifiedFeatureSet[]{})[0].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(9))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(6)).intersects(new Features.HoursIgnoredFeature(62)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(6)).intersects(new Features.HoursIgnoredFeature(65)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(6)).intersects(new Features.HoursIgnoredFeature(75)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(6)).intersects(new Features.HoursIgnoredFeature(78)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(6)).intersects(new Features.HoursIgnoredFeature(90)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(6)).intersects(new Features.HoursIgnoredFeature(96)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(6)).intersects(new Features.HoursIgnoredFeature(97)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(6)).intersects(new Features.HoursIgnoredFeature(100)), is(equalTo(false)));
		assertThat(Utils.toCollection(instances.get(6)).size(), is(equalTo(2)));
		assertThat(Utils.toCollection(instances.get(6)).toArray(new ClassifiedFeatureSet[]{})[0].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(14))));
		assertThat(Utils.toCollection(instances.get(6)).toArray(new ClassifiedFeatureSet[]{})[1].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(4))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(7)).intersects(new Features.HoursIgnoredFeature(90)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(7)).intersects(new Features.HoursIgnoredFeature(96)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(7)).intersects(new Features.HoursIgnoredFeature(97)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(7)).intersects(new Features.HoursIgnoredFeature(98)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(7)).intersects(new Features.HoursIgnoredFeature(99)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(7)).intersects(new Features.HoursIgnoredFeature(100)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(7)).intersects(new Features.HoursIgnoredFeature(105)), is(equalTo(false)));
		assertThat(Utils.toCollection(instances.get(7)).size(), is(equalTo(3)));
		assertThat(Utils.toCollection(instances.get(7)).toArray(new ClassifiedFeatureSet[]{})[0].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(8))));
		assertThat(Utils.toCollection(instances.get(7)).toArray(new ClassifiedFeatureSet[]{})[1].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(12))));
		assertThat(Utils.toCollection(instances.get(7)).toArray(new ClassifiedFeatureSet[]{})[2].getId(), is(equalTo(Identifier.FACTORY.createIdentifier(1))));
	}
	
	@Test
	public void givenConstantBucketQuantiser_whenCallingQuantise_thenExpectedCallbacksInvokedSplitOccurs(){
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet>> instances = new ArrayList<Iterable<ClassifiedFeatureSet>>();
		
		constantBucketCandidate.quantise(TEST_INSTANCES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<ClassifiedFeatureSet> instanceInSplit) {
				ranges.add(newRange);
				System.out.println(newRange);
				instances.add(instanceInSplit);
			}
		});
		
		assertThat(ranges.size(), is(equalTo(10)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(Integer.MIN_VALUE)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(-45)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(21)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(22)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(25)), is(equalTo(false)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(-45)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(21)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(22)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(25)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(30)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(31)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(32)), is(equalTo(false)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(30)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(31)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(34)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(39)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(40)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(41)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(42)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(45)), is(equalTo(false)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(99)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(102)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(103)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(150)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(564)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(Integer.MAX_VALUE)), is(equalTo(true)));
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
