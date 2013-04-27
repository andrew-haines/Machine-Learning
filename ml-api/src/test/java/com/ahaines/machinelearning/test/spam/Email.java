package com.ahaines.machinelearning.test.spam;

import java.util.Arrays;

import com.ahaines.machinelearning.api.dataset.DiscreteFeature;
import com.ahaines.machinelearning.api.dataset.Feature;

public class Email {

	public static enum EmailClassification{
		SPAM,
		HAM
	}
	
	public static class Features{
		
		public static Iterable<? extends Class<? extends Feature<?>>> ALL_FEATURE_TYPES = Arrays.asList(EnlargementFeature.class, ViagraFeature.class);
		
		public static enum Contains{
			PRESENT,
			ABSENT
		}
		
		private static abstract class ContainsFeature implements DiscreteFeature<Contains>{
			
			private final Contains value;
			
			protected ContainsFeature(Contains value){
				this.value = value;
			}
			@Override
			public Contains getValue() {
				return value;
			}

			@Override
			public boolean intersects(Feature<Contains> otherFeature) {
				return getValue() == otherFeature.getValue();
			}
		}
		
		public static class ViagraFeature extends ContainsFeature {
			
			public ViagraFeature(Contains value){
				super(value);
			}			
		}
		
		public static class EnlargementFeature extends ContainsFeature {
			
			public EnlargementFeature(Contains value){
				super(value);
			}			
		}
	}
}
