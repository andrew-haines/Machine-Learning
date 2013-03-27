package com.ahaines.machinelearning.api.util;

import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.easymock.annotation.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(UnitilsJUnit4TestClassRunner.class)
public class CachedIterableUnitTest {
	
	private static final List<String> TEST_LIST = Arrays.asList("ITEM2", "ITEM5", "ITEM1", "ITEM2");
	private static final List<String> TEST_LIST_DIFFERENT = Arrays.asList("ITEM2", "ITEM4", "ITEM1", "ITEM2");
 	private static final List<String> TEST_LIST_DIFFERENT_LENGTH = Arrays.asList("ITEM2", "ITEM4", "ITEM1", "ITEM2", "ITEM8");
	
	private CachedIterable<String> candidate;
	
	@Mock
	private Iterable<String> iterationAwareIterable;
	
	@Before
	public void before(){
		candidate = new CachedIterable<String>(iterationAwareIterable);
		expect(iterationAwareIterable.iterator()).andReturn(TEST_LIST.iterator()).once();
		
		replay(iterationAwareIterable);
	}
	
	@Test
	public void givenCachedIterable_whenCallingIteratorTwice_thenOnlyOneIteratorIsCreatedOnDelegateInstance(){
		iterateAndConfirmValues(); 
		iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
		verify(iterationAwareIterable); // verify that it was only called once
	}
	
	@Test
	public void givenCachedIterable_whenCallingEquals_thenReturnsTrue(){
 	 	assertThat(candidate, is(equalTo(new CachedIterable<String>(TEST_LIST))));
 	 	verify(iterationAwareIterable); // verify that it was only called once
 	 	iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
 		verify(iterationAwareIterable); // verify that it was only called once
 	}

	@Test
	public void givenCachedIterableAndEmptyList_whenCallingEquals_thenReturnsFalse(){
		assertThat(candidate, is(not(equalTo(new CachedIterable<String>(Collections.<String>emptyList())))));
		verify(iterationAwareIterable); // verify that it was only called once
		iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
		verify(iterationAwareIterable); // verify that it was only called once
	}
	
	@Test
	public void givenCachedIterableAndDifferentIterable_whenCallingEquals_thenReturnsFalse(){
        assertThat(candidate, is(not(equalTo(new CachedIterable<String>(TEST_LIST_DIFFERENT)))));
        verify(iterationAwareIterable); // verify that it was only called once
        iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
        verify(iterationAwareIterable); // verify that it was only called once
 	}

	@Test
	public void givenCachedIterableAndDifferentLengthIterable_whenCallingEquals_thenReturnsFalse(){
 		assertThat(candidate, is(not(equalTo(new CachedIterable<String>(TEST_LIST_DIFFERENT_LENGTH)))));
 	 	verify(iterationAwareIterable); // verify that it was only called once
 	 	iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
 	 	verify(iterationAwareIterable); // verify that it was only called once
 	}

	@Test
	public void givenAlreadyCachedIterable_whenCallingEquals_thenReturnsTrue(){
 		iterateAndConfirmValues(); // fills cache
 	 	assertThat(candidate, is(not(equalTo(new CachedIterable<String>(TEST_LIST_DIFFERENT_LENGTH))))); // equals still holds
 	 	verify(iterationAwareIterable); // verify that it was only called once
 	 	iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
 	 	verify(iterationAwareIterable); // verify that it was only called once
 	}

	@Test
	public void givenNonCachedIterable_whenCallingToArray_thenReturnsCorrectArray(){
		assertThat(Arrays.equals(candidate.toArray(), TEST_LIST.toArray()), is(equalTo(true)));
	}

	@Test
        public void givenCachedIterable_whenCallingToArray_thenReturnsCorrectArray(){
                iterateAndConfirmValues();
		assertThat(Arrays.equals(candidate.toArray(), TEST_LIST.toArray()), is(equalTo(true)));
		verify(iterationAwareIterable); // verify that it was only called once
        }

	@Test
	public void givenNonCachedIterable_whenCallingToArrayWithParam_thenReturnsCorrectArray(){
		assertThat(Arrays.equals(candidate.toArray(new String[]{}), TEST_LIST.toArray(new String[]{})), is(equalTo(true)));
	}
	
	@Test
	public void givenCachedIterable_whenCallingToArrayWithParam_thenReturnsCorrectArray(){
		iterateAndConfirmValues();
		assertThat(Arrays.equals(candidate.toArray(new String[]{}), TEST_LIST.toArray(new String[]{})), is(equalTo(true)));
		verify(iterationAwareIterable); // verify that it was only called once
	}
	
	@Test
	public void givenCachedIterable_whenCallingIteratorAndThrowingErrorMidWay_thenIteratorIsNotCached(){
		reset(iterationAwareIterable);
		expect(iterationAwareIterable.iterator()).andAnswer(new IAnswer<Iterator<String>>(){

			@Override
			public Iterator<String> answer() throws Throwable {
				return TEST_LIST.iterator();
			}
			
		}).times(2); // we expect it to have been invoked twice as the first iteration does not complete
		replay(iterationAwareIterable);
		
		Iterator<String> it = candidate.iterator();
		for (int i = 0; i < TEST_LIST.size()/2; i++){
			if (it.hasNext()){
				it.next();
			} else{
				throw new AssertionError("Should never get here!");
			}
		}		
		iterateAndConfirmValues(); 
		
		verify(iterationAwareIterable); // verify that it was only called once
	}
	
	private void iterateAndConfirmValues(){
		int idx = 0;
		for (String item: candidate){
			assertThat(item, is(equalTo(TEST_LIST.get(idx++))));
		}
	}
}

