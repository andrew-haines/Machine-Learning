package com.ahaines.machinelearning.api;

import com.ahaines.machinelearning.api.Model.Metrics;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedFeatureSet;
import com.ahaines.machinelearning.api.dataset.Dataset;
import com.ahaines.machinelearning.api.dataset.FeatureSet;

public interface ModelService<T extends Model> {
	
	public static Utils UTIL = new Utils();

	/**
	 * returns an arbitrary model trained on the supplied classified dataset
	 * @param trainingData
	 * @return
	 */
	T trainModel(ClassifiedDataset trainingData);
	
	/**
	 * returns a ClassifiedDataset with classifications for all instances in the supplied dataset against the supplied model.
	 * @param dataset
	 * @param model
	 * @return
	 */
	ClassifiedDataset classifyDataset(Dataset<? extends FeatureSet> dataset, T model);
	
	static class Utils extends com.ahaines.machinelearning.api.util.Utils{

		public <T extends Model> T getMetrics(ClassifiedDataset trainingSet, ClassifiedDataset expectedTestSet, ModelService<T> modelService, Enum<?> positiveClassification) {
			// calculate distribution
			long startTime = System.currentTimeMillis();
			T model = modelService.trainModel(trainingSet);
			Metrics metrics = model.getMetrics(); // call by reference update
			metrics.timeToBuildModel = System.currentTimeMillis() - startTime;
			
			// now classify the test set
			
			startTime = System.currentTimeMillis();
			ClassifiedDataset predictedClassifiedSet = modelService.classifyDataset(expectedTestSet, model);
			metrics.timeToClassify = System.currentTimeMillis() - startTime;
			
			populateMetrics(predictedClassifiedSet, expectedTestSet, positiveClassification, metrics);
			
			return model;
		}

		private void populateMetrics(ClassifiedDataset predictedClassifiedSet, ClassifiedDataset expectedTestSet, Enum<?> positiveClassifcation, Metrics metrics) {
			int numTruePositives = 0;
			int numTrueNegatives = 0;
			int positiveCount = 0;
			int negativeCount = 0;
			int totalNumInstances = 0;
			int totalCorrectlyClassified = 0;
			int numPredictedPositives = 0;
			int numPredictedNegatives = 0;
			
			for (ClassifiedFeatureSet instance: expectedTestSet.getInstances()){
				ClassifiedFeatureSet predictedInstance = predictedClassifiedSet.getInstance(instance.getId());
				if (instance.getClassification().getValue().equals(positiveClassifcation)){
					positiveCount++;
					if (predictedInstance.getClassification().getValue().equals(positiveClassifcation)){
						numTruePositives++;
					}
				} else {
					negativeCount++;
					if (!predictedInstance.getClassification().getValue().equals(positiveClassifcation)){
						numTrueNegatives++;
					}
				}
				totalNumInstances++;
				if (instance.getClassification().getValue().equals(predictedInstance.getClassification().getValue())){
					totalCorrectlyClassified++;
				}
				
				if (predictedInstance.getClassification().getValue().equals(positiveClassifcation)){
					numPredictedPositives++;
				} else{
					numPredictedNegatives++;
				}
			}
			metrics.numPositives = positiveCount;
			metrics.numNegatives = negativeCount;
			metrics.numPredictedPositives = numPredictedPositives;
			metrics.numPredictedNegatives = numPredictedNegatives;
			
			metrics.truePositiveRate = (double)numTruePositives / (double)positiveCount;
			metrics.trueNegativeRate = (double)numTrueNegatives / (double)negativeCount;
			
			metrics.accuracy = (double)totalCorrectlyClassified / (double)totalNumInstances;
			metrics.errorRate = 1 - metrics.accuracy;
			
			double positiveNegativeRatio = positiveCount / (positiveCount + negativeCount);
			
			metrics.weightedAccuracy = positiveNegativeRatio * metrics.truePositiveRate + (1- positiveNegativeRatio) * metrics.trueNegativeRate;
			metrics.weightedErrorRate = 1 - metrics.weightedAccuracy;
			
			int numFalsePositives = negativeCount - numTrueNegatives;
			double precision = (double)numTruePositives / ((double)numTruePositives + (double)numFalsePositives);
			
			metrics.fMeasure = (2 * precision * metrics.truePositiveRate) / (precision + metrics.truePositiveRate);
		}
	}
}
