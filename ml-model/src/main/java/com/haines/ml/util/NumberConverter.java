package com.haines.ml.util;

public interface NumberConverter<T extends Number & Comparable<T>> {

	public static final NumberConverterFactory FACTORY = new NumberConverterFactory();
	
	T castToType(Number value);
	
	T getMaxPossibleValue();
	
	T getMinPossibleValue();
	
	public final class NumberConverterFactory{
		
		private NumberConverterFactory(){}
		
		public NumberConverter<Integer> getIntegerConverter(){
			return new NumberConverter<Integer>(){

				@Override
				public Integer castToType(Number value) {
					return value.intValue();
				}

				@Override
				public Integer getMaxPossibleValue() {
					return Integer.MAX_VALUE;
				}

				@Override
				public Integer getMinPossibleValue() {
					return Integer.MIN_VALUE;
				}
				
			};
		}

		public NumberConverter<Long> getLongConverter() {
			return new NumberConverter<Long>(){

				@Override
				public Long castToType(Number value) {
					return value.longValue();
				}

				@Override
				public Long getMaxPossibleValue() {
					return Long.MAX_VALUE;
				}

				@Override
				public Long getMinPossibleValue() {
					return Long.MIN_VALUE;
				}
				
			};
		}

		public NumberConverter<Double> getDoubleConverter() {
			return new NumberConverter<Double>(){

				@Override
				public Double castToType(Number value) {
					return value.doubleValue();
				}

				@Override
				public Double getMaxPossibleValue() {
					return Double.MAX_VALUE;
				}

				@Override
				public Double getMinPossibleValue() {
					return Double.MIN_VALUE;
				}
				
			};
		}
	}
}
