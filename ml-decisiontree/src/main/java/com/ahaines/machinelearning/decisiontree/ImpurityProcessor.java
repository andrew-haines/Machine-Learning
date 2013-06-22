package com.ahaines.machinelearning.decisiontree;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;

public interface ImpurityProcessor {

	/**
	 * Returns the impurity of a collection of instances.
	 * @param instances
	 * @return
	 */
	<C extends Enum<C>> double getImpurity(Iterable<ClassifiedFeatureSet<C>> instances);
	
	public final static class ImpurityProcessors{
		
		private ImpurityProcessors(){}
		
		/**
		 * Returns a gini index processor that calculates impurity as: 2p(1-p)
		 * @return
		 */
		public static ImpurityProcessor getGiniIndexImpurityProcessor(){
			return new ImpurityProcessor(){

				@Override
				public <C extends Enum<C>> double getImpurity(Iterable<ClassifiedFeatureSet<C>> instances) {
					Map<C, Double> proportions = getProportions(instances);
					if (proportions.size() <= 1){ // if its got 0 instances then it is pure, and if it only has one enum classification for all the instances then it is also completely pure 
						return 0;
					}
					double impurity = 1;
					for (Entry<C,Double> proportion: proportions.entrySet()){
						impurity *= proportion.getValue();
					}
					
					return 2 * impurity;
				}
			};
		}
		
		/**
		 * Returns the square root of a gini processor calculated by √gini
		 * @return
		 */
		public static ImpurityProcessor getSquareRootGiniIndexImpurityProcessor(){
			final ImpurityProcessor giniProcessor = getGiniIndexImpurityProcessor();
			return new ImpurityProcessor(){

				@Override
				public <C extends Enum<C>> double getImpurity(Iterable<ClassifiedFeatureSet<C>> instances) {
					return Math.sqrt(giniProcessor.getImpurity(instances));
				}
			};
		}
		
		/**
		 * Returns the minority class impurity processor calculated by min(p, 1-p)
		 * @return
		 */
		public static ImpurityProcessor getMinorityClassImpurityProcessor(){
			return new ImpurityProcessor(){

				@Override
				public <C extends Enum<C>> double getImpurity(Iterable<ClassifiedFeatureSet<C>> instances) {
					Map<C, Double> proportions = getProportions(instances);
					if (proportions.size() <= 1){
						return 0;
					}
					double minPropertion = Double.MAX_VALUE;
					
					for (Double proportion: proportions.values()){
						minPropertion = Math.min(minPropertion, proportion);
					}
					
					return minPropertion;
				}
				
			};
		}
		
		/**
		 * Returns the entropy impurity processor calculated by -plog2(p)-(1-p)log2(1-p)
		 * @return
		 */
		public static ImpurityProcessor getEntropyImpurityProcessor(){
			return new ImpurityProcessor(){

				@Override
				public <C extends Enum<C>> double getImpurity(Iterable<ClassifiedFeatureSet<C>> instances) {
					Map<? extends Enum<?>, Double> proportions = getProportions(instances);
					
					double entropy = 0;
					
					for (Double proportion: proportions.values()){
						entropy -= proportion * log2(proportion);
					}
					return entropy;
				}
				
			};
		}
		
		private static double log2(double value){
			return Math.log(value) / Math.log(2);
		}
		
		static <C extends Enum<C>> Map<C, Double> getProportions(Iterable<ClassifiedFeatureSet<C>> instances){
			Map<C, Double> proportions = new HashMap<C, Double>();
			
			int totalInstances = 0;
			// calculates running proportion
			for (ClassifiedFeatureSet<C> instance: instances){
				
				totalInstances++;
				Double runningAverageForClass = getOrCreateAccumalatorFromMap(proportions, instance.getClassification());
				
				runningAverageForClass++;
				
				proportions.put(instance.getClassification().getValue(), runningAverageForClass);
			}
			
			for (Entry<C, Double> accumaltor: proportions.entrySet()){
				proportions.put(accumaltor.getKey(), accumaltor.getValue() / (double)totalInstances);
			}
			return proportions;
		}
		
		private static <C extends Enum<C>> Double getOrCreateAccumalatorFromMap(Map<C, Double> proportions, Classification<C> classification){
			Double proportion = proportions.get(classification.getValue());
			
			if (proportion == null){
				proportion = new Double(0);
			}
			
			
			return proportion;
		}
	}
}
