package com.haines.ml.model;

import com.haines.ml.util.NumberConverter;

public interface ContinuousFeature<T extends Number & Comparable<T>> extends Feature<T>{
	
	public static class IntegerFeature implements ContinuousFeature<Integer>{

		private final Integer value;
		
		public IntegerFeature(Integer value){
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
	
	public static class DoubleFeature implements ContinuousFeature<Double>{

		private final Double value;
		
		public DoubleFeature(Double value){
			this.value = value;
		}
		
		@Override
		public Double getValue() {
			return value;
		}
		
		public String toString(){
			return String.valueOf(value);
		}

		@Override
		public boolean intersects(Feature<Double> otherFeature) {
			return value.equals(otherFeature.getValue());
		}

		@Override
		public NumberConverter<Double> getNumberConverter() {
			return NumberConverter.FACTORY.getDoubleConverter();
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
		
		public LongFeature(Long value){
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
