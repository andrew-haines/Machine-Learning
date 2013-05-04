package com.ahaines.machinelearning.api.dataset;

import com.ahaines.machinelearning.api.dataset.quantiser.NumberConverter;

public interface ContinuousFeature<T extends Number & Comparable<T>> extends Feature<T>{
	
	public static class IntegerFeature implements ContinuousFeature<Integer>{

		private final Integer value;
		
		protected IntegerFeature(Integer value){
			this.value = value;
		}
		
		@Override
		public Integer getValue() {
			return value;
		}
		
		public String toString(){
			return String.valueOf(value);
		}

		@Override
		public boolean intersects(Feature<Integer> otherFeature) {
			return value.equals(otherFeature.getValue());
		}

		@Override
		public NumberConverter<Integer> getNumberConverter() {
			return NumberConverter.FACTORY.getIntegerConverter();
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj instanceof IntegerFeature){
				IntegerFeature other = (IntegerFeature)obj;
				
				return this.value.equals(other.value);
			}
			return false;
		}
		
		@Override
		public int hashCode(){
			return value.intValue();
		}
	}
	
	public static class LongFeature implements ContinuousFeature<Long>{

		private final Long value;
		
		protected LongFeature(Long value){
			this.value = value;
		}
		
		@Override
		public Long getValue() {
			return value;
		}
		
		public String toString(){
			return String.valueOf(value);
		}

		@Override
		public boolean intersects(Feature<Long> otherFeature) {
			return value.equals(otherFeature.getValue());
		}

		@Override
		public NumberConverter<Long> getNumberConverter() {
			return NumberConverter.FACTORY.getLongConverter();
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj instanceof IntegerFeature){
				IntegerFeature other = (IntegerFeature)obj;
				
				return this.value.equals(other.value);
			}
			return false;
		}
		
		@Override
		public int hashCode(){
			return value.intValue();
		}
	}
	
	NumberConverter<T> getNumberConverter();
}
