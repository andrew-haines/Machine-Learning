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
import com.google.common.collect.Iterables;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public class ContinuousFeatureQuantisersUnitTest {

	private static final FeatureSet.FeatureSetFactory FACTORY = new FeatureSet.FeatureSetFactory(Features.ALL_FEATURE_TYPES);
	private final static Iterable<ClassifiedFeatureSet<EmailClassification>> TEST_INSTANCES = getTestInstances();
	private final static Iterable<ClassifiedFeatureSet<EmailClassification>> TEST_INSTANCES_DUPLICATES = getTestContinuousDuplicateInstances();
	private final static Iterable<ClassifiedFeatureSet<EmailClassification>> TEST_INSTANCES_ALL_SAME = getTestSameDuplicateInstances();
	
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
		final List<Iterable<ClassifiedFeatureSet<EmailClassification>>> instances = new ArrayList<Iterable<ClassifiedFeatureSet<EmailClassification>>>();
		
		averageQuantiserCandidate.quantise(TEST_INSTANCES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instanceInSplit) {
				ranges.add(newRange);
				instances.add((Iterable)instanceInSplit);
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

	private static Iterable<ClassifiedFeatureSet<EmailClassification>> getTestInstances() {
		List<ClassifiedFeatureSet<EmailClassification>> instances = new ArrayList<ClassifiedFeatureSet<EmailClassification>>();
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
	
	private static Iterable<ClassifiedFeatureSet<EmailClassification>> getTestContinuousDuplicateInstances() {
		List<ClassifiedFeatureSet<EmailClassification>> instances = new ArrayList<ClassifiedFeatureSet<EmailClassification>>();
		instances.add(createClassifiedInstance(1, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));
		instances.add(createClassifiedInstance(2, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(3, Contains.PRESENT, Contains.ABSENT, 50, EmailClassification.HAM));
		instances.add(createClassifiedInstance(4, Contains.ABSENT, Contains.ABSENT, 100, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(5, Contains.PRESENT, Contains.PRESENT, 100, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(6, Contains.ABSENT, Contains.ABSENT, 50, EmailClassification.HAM));
		instances.add(createClassifiedInstance(7, Contains.PRESENT, Contains.ABSENT, 50, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(16, Contains.PRESENT, Contains.PRESENT, 91, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(17, Contains.PRESENT, Contains.PRESENT, 91, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(18, Contains.PRESENT, Contains.PRESENT, 92, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(8, Contains.ABSENT, Contains.ABSENT, 50, EmailClassification.HAM));
		instances.add(createClassifiedInstance(9, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));
		instances.add(createClassifiedInstance(10, Contains.PRESENT, Contains.PRESENT, 100, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(11, Contains.ABSENT, Contains.ABSENT, 30, EmailClassification.HAM));
		instances.add(createClassifiedInstance(12, Contains.PRESENT, Contains.ABSENT, 30, EmailClassification.HAM));
		instances.add(createClassifiedInstance(13, Contains.PRESENT, Contains.PRESENT, 30, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(14, Contains.PRESENT, Contains.PRESENT, 20, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(15, Contains.PRESENT, Contains.PRESENT, 20, EmailClassification.SPAM));
		
		return instances;
	}
	
	private static Iterable<ClassifiedFeatureSet<EmailClassification>> getTestSameDuplicateInstances() {
		List<ClassifiedFeatureSet<EmailClassification>> instances = new ArrayList<ClassifiedFeatureSet<EmailClassification>>();
		instances.add(createClassifiedInstance(1, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));
		instances.add(createClassifiedInstance(2, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));
		instances.add(createClassifiedInstance(3, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));
		instances.add(createClassifiedInstance(4, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));
		instances.add(createClassifiedInstance(5, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.SPAM));
		instances.add(createClassifiedInstance(6, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));
		instances.add(createClassifiedInstance(7, Contains.PRESENT, Contains.ABSENT, 100, EmailClassification.HAM));

		return instances;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenClusterFeatureQuantiser_whenCallingQuantise_then2CallbacksInstantiatedAroundAveragePoint(){
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet<EmailClassification>>> instances = new ArrayList<Iterable<ClassifiedFeatureSet<EmailClassification>>>();
		
		clusterQuantiserCandidate.quantise(TEST_INSTANCES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instanceInSplit) {
				ranges.add(newRange);
				instances.add((Iterable)instanceInSplit);
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenConstantBucketQuantiser_whenCallingQuantise_thenExpectedCallbacksInvokedSplitOccurs(){
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet<EmailClassification>>> instances = new ArrayList<Iterable<ClassifiedFeatureSet<EmailClassification>>>();
		
		constantBucketCandidate.quantise(TEST_INSTANCES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@SuppressWarnings("rawtypes")
			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instancesInSplit) {
				ranges.add(newRange);
				System.out.println(newRange);
				instances.add((Iterable)instancesInSplit);
			}
		});
		
		assertThat(ranges.size(), is(equalTo(10)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(Integer.MIN_VALUE)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(-45)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(21)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(22)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(25)), is(equalTo(false)));
		assertThat(Iterables.size(instances.get(0)), is(equalTo(1)));
		assertThat(Iterables.get(instances.get(0), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(13))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(-45)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(21)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(22)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(23)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(25)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(30)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(32)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(33)), is(equalTo(false)));
		assertThat(Iterables.size(instances.get(1)), is(equalTo(2)));
		assertThat(Iterables.get(instances.get(1), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(3))));
		assertThat(Iterables.get(instances.get(1), 1).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(2))));
	
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(30)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(31)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(34)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(39)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(40)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(42)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(43)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(45)), is(equalTo(false)));
		
		assertThat(Iterables.size(instances.get(2)), is(equalTo(0)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(42)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(43)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(45)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(52)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(53)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(3)).intersects(new Features.HoursIgnoredFeature(56)), is(equalTo(false)));
		
		assertThat(Iterables.size(instances.get(3)), is(equalTo(3)));
		assertThat(Iterables.get(instances.get(3), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(6))));
		assertThat(Iterables.get(instances.get(3), 1).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(7))));
		assertThat(Iterables.get(instances.get(3), 2).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(11))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(99)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(102)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(103)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(150)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(564)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(Integer.MAX_VALUE)), is(equalTo(true)));
		assertThat(Iterables.size(instances.get(9)), is(equalTo(1)));
		assertThat(Iterables.get(instances.get(9), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(5))));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenConstantBucketQuantiser_whenCallingQuantiseWith2Buckets_thenExpectedCallbacksInvokedSplitOccurs(){
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet<EmailClassification>>> instances = new ArrayList<Iterable<ClassifiedFeatureSet<EmailClassification>>>();
		
		constantBucketCandidate = ContinuousFeatureQuantisers.getConstantBucketQuantiser(2);
		
		constantBucketCandidate.quantise(TEST_INSTANCES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instanceInSplit) {
				ranges.add(newRange);
				System.out.println(newRange);
				instances.add((Iterable)instanceInSplit);
			}
		});
		
		assertThat(ranges.size(), is(equalTo(3))); // minimum bucket size has to be 3
		
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(Integer.MIN_VALUE)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(-45)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(21)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(22)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(23)), is(equalTo(false)));
		assertThat(Iterables.size(instances.get(0)), is(equalTo(1)));
		assertThat(Iterables.get(instances.get(0), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(13))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(21)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(22)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(23)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(56)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(85)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(102)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(103)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(120)), is(equalTo(false)));
		assertThat(Iterables.size(instances.get(1)), is(equalTo(12)));
		assertThat(Iterables.get(instances.get(1), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(3))));
		assertThat(Iterables.get(instances.get(1), 1).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(2))));
		assertThat(Iterables.get(instances.get(1), 2).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(6))));
		assertThat(Iterables.get(instances.get(1), 3).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(7))));
		assertThat(Iterables.get(instances.get(1), 4).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(11))));
		assertThat(Iterables.get(instances.get(1), 5).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(9))));
		assertThat(Iterables.get(instances.get(1), 6).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(14))));
		assertThat(Iterables.get(instances.get(1), 7).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(4))));
		assertThat(Iterables.get(instances.get(1), 8).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(8))));
		assertThat(Iterables.get(instances.get(1), 9).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(12))));
		assertThat(Iterables.get(instances.get(1), 10).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(1))));
		assertThat(Iterables.get(instances.get(1), 11).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(10))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(99)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(102)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(103)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(150)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(564)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(2)).intersects(new Features.HoursIgnoredFeature(Integer.MAX_VALUE)), is(equalTo(true)));
		assertThat(Iterables.size(instances.get(2)), is(equalTo(1)));
		assertThat(Iterables.get(instances.get(2), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(5))));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenConstantBucketQuantiser_whenCallingQuantiseWith10BucketsAndDuplicateValuesInTestSet_thenExpectedCallbacksInvokedSplitOccurs(){
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet<EmailClassification>>> instances = new ArrayList<Iterable<ClassifiedFeatureSet<EmailClassification>>>();
		
		constantBucketCandidate.quantise(TEST_INSTANCES_DUPLICATES, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instanceInSplit) {
				ranges.add(newRange);
				System.out.println(newRange);
				instances.add((Iterable)instanceInSplit);
			}
		});
		
		assertThat(ranges.size(), is(equalTo(10)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(Integer.MIN_VALUE)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(-45)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(20)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(21)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(25)), is(equalTo(false)));
		assertThat(Iterables.size(instances.get(0)), is(equalTo(2)));
		assertThat(Iterables.get(instances.get(0), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(14))));
		assertThat(Iterables.get(instances.get(0), 1).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(15))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(20)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(21)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(26)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(29)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(30)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(1)).intersects(new Features.HoursIgnoredFeature(33)), is(equalTo(false)));
		assertThat(Iterables.size(instances.get(1)), is(equalTo(0)));
		
		assertThat(((RangeFeature<Integer>)ranges.get(8)).intersects(new Features.HoursIgnoredFeature(85)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(8)).intersects(new Features.HoursIgnoredFeature(89)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(8)).intersects(new Features.HoursIgnoredFeature(90)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(8)).intersects(new Features.HoursIgnoredFeature(95)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(8)).intersects(new Features.HoursIgnoredFeature(99)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(8)).intersects(new Features.HoursIgnoredFeature(100)), is(equalTo(false)));
		assertThat(Iterables.size(instances.get(8)), is(equalTo(3)));
		assertThat(Iterables.get(instances.get(8), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(16))));
		assertThat(Iterables.get(instances.get(8), 1).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(17))));
		assertThat(Iterables.get(instances.get(8), 2).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(18))));
		
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(98)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(99)), is(equalTo(false)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(100)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(101)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(254)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(9)).intersects(new Features.HoursIgnoredFeature(887)), is(equalTo(true)));
		assertThat(Iterables.size(instances.get(9)), is(equalTo(6)));
		assertThat(Iterables.get(instances.get(9), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(10))));
		assertThat(Iterables.get(instances.get(9), 1).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(9))));
		assertThat(Iterables.get(instances.get(9), 2).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(5))));
		assertThat(Iterables.get(instances.get(9), 3).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(4))));
		assertThat(Iterables.get(instances.get(9), 4).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(2))));
		assertThat(Iterables.get(instances.get(9), 5).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(1))));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void givenConstantBucketQuantiser_whenCallingQuantiseWith10BucketsAndAllSameValuesInTestSet_thenExpectedCallbacksInvokedSplitOccurs(){
		final List<RangeFeature<? extends Number>> ranges = new ArrayList<RangeFeature<? extends Number>>();
		final List<Iterable<ClassifiedFeatureSet<EmailClassification>>> instances = new ArrayList<Iterable<ClassifiedFeatureSet<EmailClassification>>>();
		
		constantBucketCandidate.quantise(TEST_INSTANCES_ALL_SAME, Email.Features.HoursIgnoredFeature.class, new QuantiserEventProcessor(){

			@Override
			public <T extends Number & Comparable<T>> void newRangeDetermined(RangeFeature<T> newRange,	Iterable<? extends ClassifiedFeatureSet<? extends Enum<?>>> instanceInSplit) {
				ranges.add(newRange);
				System.out.println(newRange);
				instances.add((Iterable)instanceInSplit);
			}
		});
		
		assertThat(ranges.size(), is(equalTo(1))); // as there is nothing to split, we can only return a complete range of all numbers
		
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(Integer.MIN_VALUE)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(-45)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(0)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(48)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(100)), is(equalTo(true)));
		assertThat(((RangeFeature<Integer>)ranges.get(0)).intersects(new Features.HoursIgnoredFeature(Integer.MAX_VALUE)), is(equalTo(true)));
		assertThat(Iterables.size(instances.get(0)), is(equalTo(7)));
		assertThat(Iterables.get(instances.get(0), 0).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(1))));
		assertThat(Iterables.get(instances.get(0), 1).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(2))));
		assertThat(Iterables.get(instances.get(0), 2).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(3))));
		assertThat(Iterables.get(instances.get(0), 3).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(4))));
		assertThat(Iterables.get(instances.get(0), 4).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(5))));
		assertThat(Iterables.get(instances.get(0), 5).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(6))));
		assertThat(Iterables.get(instances.get(0), 6).getId(), is(equalTo(Identifier.FACTORY.createIdentifier(7))));
	}
	
	private static ClassifiedFeatureSet<EmailClassification> createClassifiedInstance(int id, Contains viagra, Contains enlargment, int hoursIgnored, EmailClassification classification){
		return new ClassifiedFeatureSet<EmailClassification>(createInstance(id, viagra, enlargment, hoursIgnored), new Classification<EmailClassification>(Identifier.FACTORY.createIdentifier(id), classification));
	}
	
	private static FeatureSet createInstance(int id, Contains viagra, Contains enlargment, int hoursIgnored) {
		return FACTORY.createFeatureSet(Identifier.FACTORY.createIdentifier(id), Arrays.asList(new FeatureDefinition(new Features.ViagraFeature(viagra)), 
																							   new FeatureDefinition(new Features.EnlargementFeature(enlargment)), 
																							   new FeatureDefinition(new Features.HoursIgnoredFeature(hoursIgnored))));
	}
}
