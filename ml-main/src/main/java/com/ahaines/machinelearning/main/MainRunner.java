package com.ahaines.machinelearning.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;

import com.ahaines.machinelearning.api.Model.Metrics;
import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedDatasetLoader;
import com.ahaines.machinelearning.api.dataset.adultearnings.AdultEarningsClassification;
import com.ahaines.machinelearning.api.dataset.adultearnings.AdultEarningsDatasetLoaders;
import com.ahaines.machinelearning.decisiontree.ContinuousFeatureSplitter;
import com.ahaines.machinelearning.decisiontree.Id3Model;
import com.ahaines.machinelearning.decisiontree.DecisionTreeModelService;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor;
import com.ahaines.machinelearning.decisiontree.MissingFeatureClassifier;
import com.ahaines.machinelearning.decisiontree.ContinuousFeatureSplitter.ContinuousFeatureSplitters;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor.ImpurityProcessors;

/**
 * The main application for running the comparision of different configurations and their performance. 
 * 
 * For each parameter changed we observe the following performance metrics:
 * 
 * - time to train
 * - time to classify
 * - error rate against self
 * - error rate against test set
 * - size of model
 * 
 * For each parameter changed we run the test multiple times with different homogenious threshold values from (0.01->0.3)
 * 
 * The parameters changed are:
 * 
 * homogenious threshold - how much of a distribution of classification types we tolerate before we class an instance set as
 *  					   not being homogenious. The greater this value the less likely we are to branch on a given feature
 * 
 * ImpurityProcessor - The implementation of how we calculate how impure a set is based on splits around a feature. We trial
 * 					   4 different implementations in this test: minority class, gini index, √gini, and entropy
 * 
 * MissingFeatureClassifier - How we deal with missing features. The 2 implementations considered are average pivot split and
 * 							  incremental range clustering.
 * 
 * Continuous feature split - How we split on continuous features. 2 implementations are considered. One that splits around
 * 							  the average value and the other that splits based on incremental clustering of values.
 * 
 * @author andrewhaines
 *
 */
public class MainRunner {

	public static void main(String args[]) throws IOException, URISyntaxException{
		
		ClassifiedDatasetLoader trainingLoader = AdultEarningsDatasetLoaders.getTrainingDatasetLoader();
		ClassifiedDatasetLoader testLoader = AdultEarningsDatasetLoaders.getTestDatasetLoader();
		
		System.out.println("impurity calculations\n\n");
		System.out.println("Minority class impurity calculations");
		performRuns(trainingLoader, testLoader, ImpurityProcessors.getMinorityClassImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), ContinuousFeatureSplitters.getClusterSplitter());
		System.out.println("Gini index impurity calculations");
		performRuns(trainingLoader, testLoader, ImpurityProcessors.getGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), ContinuousFeatureSplitters.getClusterSplitter());
		System.out.println("Entropy impurity calculations");
		performRuns(trainingLoader, testLoader, ImpurityProcessors.getEntropyImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), ContinuousFeatureSplitters.getClusterSplitter());
		
		System.out.println("Square root Gini index calculations");
		performRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), ContinuousFeatureSplitters.getClusterSplitter());
		
		System.out.println("Missing Features\n\n");
		System.out.println("Most Homogenious");
		performRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getHomogeniousMissingFeatureClassifier(), ContinuousFeatureSplitters.getClusterSplitter());
		System.out.println("Most Rated");
		performRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), ContinuousFeatureSplitters.getClusterSplitter());
		

		System.out.println("Continuous Feature Splitters\n\n");
		System.out.println("Average feature splitter");
		performRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), ContinuousFeatureSplitters.getClusterSplitter());
		System.out.println("Cluster splitter");
		performRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), ContinuousFeatureSplitters.getClusterSplitter());
		
		
	}
	//0.3311
	private static void performRuns(ClassifiedDatasetLoader trainingLoader, ClassifiedDatasetLoader testLoader, ImpurityProcessor processor, MissingFeatureClassifier missingFeatureClassifier, ContinuousFeatureSplitter continuousFeatureSplitter) throws IOException{
		double homogeniousThreshold = 1;
		DecimalFormat df = new DecimalFormat("0.###");
		StringBuilder builder = new StringBuilder();
		while(homogeniousThreshold > 0.75){
			Metrics runMetrics = performRun(trainingLoader, testLoader, homogeniousThreshold, processor, missingFeatureClassifier, continuousFeatureSplitter);
			Metrics.appendCsvValue(df.format(homogeniousThreshold), builder);
			builder.append(runMetrics.toCsv());
			builder.append("\n");
		
			//System.out.println("\tthreshold = "+homogeniousThreshold);
			//System.out.println(runMetrics);
			
			homogeniousThreshold -= 0.005;
		}
		System.out.println(builder.toString());
	}
	
	private static Metrics performRun(ClassifiedDatasetLoader trainingLoader, ClassifiedDatasetLoader testLoader, double homogeniousThreshold, ImpurityProcessor processor, MissingFeatureClassifier missingFeatureClassifier, ContinuousFeatureSplitter continuousFeatureSplitter) throws IOException{
		ModelService<Id3Model> service = new DecisionTreeModelService(processor, continuousFeatureSplitter, homogeniousThreshold, missingFeatureClassifier);
		
		ClassifiedDataset trainingDataset = trainingLoader.getClassifiedDataset();

		ClassifiedDataset testDataset = testLoader.getClassifiedDataset();

		Id3Model model = ModelService.UTIL.getMetrics(trainingDataset, testDataset, service, AdultEarningsClassification.getLessThen50K());
		return model.getMetrics();
	}
}
