package com.ahaines.machinelearning.api.dataset;

public interface ContinuousFeature<T extends Number> extends Feature<T>{
	
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
	}
}
