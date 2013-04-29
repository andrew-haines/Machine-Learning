package com.ahaines.machinelearning.api.dataset.quantiser;

import org.junit.Before;
import org.junit.Test;

import com.ahaines.machinelearning.api.dataset.ContinuousFeature.IntegerFeature;
import com.ahaines.machinelearning.api.dataset.quantiser.RangeFeature;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class RangeFeatureUnitTest {

	private RangeFeature<Integer> candidate;
	
	@Before
	public void before(){
		candidate = new RangeFeature<Integer>(5, 10);
	}
	
	@Test
	public void givenValuesInRange_whenCallingIntercepts_thenReturnsTrue(){
		assertThat(candidate.intersects(new TestFeature(6)), is(equalTo(true)));
	}
	
	@Test
	public void givenValuesOnLowerBoundary_whenCallingIntercepts_thenReturnsTrue(){
		assertThat(candidate.intersects(new TestFeature(5)), is(equalTo(true)));
	}
	
	@Test
	public void givenValuesOnUpperBoundary_whenCallingIntercepts_thenReturnsFalse(){
		assertThat(candidate.intersects(new TestFeature(10)), is(equalTo(false)));
	}
	
	@Test
	public void givenValuesOutsideRange1_whenCallingIntercepts_thenReturnsFalse(){
		assertThat(candidate.intersects(new TestFeature(11)), is(equalTo(false)));
	}
	
	@Test
	public void givenValuesOutsideRange2_whenCallingIntercepts_thenReturnsFalse(){
		assertThat(candidate.intersects(new TestFeature(4)), is(equalTo(false)));
	}
	
	private static class TestFeature extends IntegerFeature{

		protected TestFeature(Integer value) {
			super(value);
		}
		
	}
}
