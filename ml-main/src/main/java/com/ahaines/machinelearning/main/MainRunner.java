package com.ahaines.machinelearning.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.Model.Metrics;
import com.ahaines.machinelearning.api.ModelService;
import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedDatasetLoader;
import com.ahaines.machinelearning.api.dataset.adultearnings.AdultEarningsClassification;
import com.ahaines.machinelearning.api.dataset.adultearnings.AdultEarningsDatasetLoaders;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantiser;
import com.ahaines.machinelearning.api.dataset.quantiser.ContinuousFeatureQuantisers;
import com.ahaines.machinelearning.decisiontree.ContinuousFeatureSplitter;
import com.ahaines.machinelearning.decisiontree.Id3Model;
import com.ahaines.machinelearning.decisiontree.DecisionTreeModelService;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor;
import com.ahaines.machinelearning.decisiontree.MissingFeatureClassifier;
import com.ahaines.machinelearning.decisiontree.ContinuousFeatureSplitter.ContinuousFeatureSplitters;
import com.ahaines.machinelearning.decisiontree.ImpurityProcessor.ImpurityProcessors;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModel;
import com.ahaines.machinelearning.naivebayes.NaiveBayesModelService;

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

	private static final int DEFAULT_BUCKET_SIZE = 10;
	private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("0.###");
	
	private static final Logger LOG = LoggerFactory.getLogger(MainRunner.class);

	public static void main(String args[]) throws IOException, URISyntaxException{
		
		setupLogger();
		
		ClassifiedDatasetLoader trainingLoader = AdultEarningsDatasetLoaders.getTrainingDatasetLoader();
		ClassifiedDatasetLoader testLoader = AdultEarningsDatasetLoaders.getTestDatasetLoader();
		
		ContinuousFeatureQuantiser cluster = ContinuousFeatureQuantisers.getClusteredQuantiser();
		ContinuousFeatureQuantiser average = ContinuousFeatureQuantisers.getAveragePivotQuantiser();
		ContinuousFeatureQuantiser constantBucket = ContinuousFeatureQuantisers.getConstantBucketQuantiser(DEFAULT_BUCKET_SIZE);
		
		LOG.info("############ ---- Decision Tree Tests ---- ############\n");
		LOG.info("\timpurity calculations\n");
		LOG.info("\t\tMinority class impurity calculations");
		performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getMinorityClassImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), cluster);
		LOG.info("\t\tGini index impurity calculations");
		performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), cluster);
		LOG.info("\t\tEntropy impurity calculations");
		performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getEntropyImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), cluster);
		
		LOG.info("\t\tSquare root Gini index calculations");
		performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), cluster);
		
		LOG.info("\tMissing Features\n");
		LOG.info("\t\tMost Homogenious");
		performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getHomogeniousMissingFeatureClassifier(), cluster);
		LOG.info("\t\tMost Rated");
		performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), cluster);
		

		LOG.info("\tContinuous Feature Splitters\n");
		LOG.info("\t\tAverage feature splitter");
		performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), average);
		LOG.info("\t\tCluster splitter");
		performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), cluster);
		//LOG.info("\t\tConstant Bucket splitter");
		//performDecisionTreeRuns(trainingLoader, testLoader, ImpurityProcessors.getSquareRootGiniIndexImpurityProcessor(), MissingFeatureClassifier.CLASSIFIERS.getMostRatedMissingFeatureClassifier(), constantBucket);
		
		LOG.info("############ ---- Naive Bayes Tests ---- ############\n");
		
		LOG.info("\t\tAverage feature splitter");
		performNaiveBayesRun(trainingLoader, testLoader, average);
		LOG.info("\t\tCluster splitter");
		performNaiveBayesRun(trainingLoader, testLoader, cluster);
		LOG.info("\t\tConstant Bucket splitter - 5 buckets");
		performNaiveBayesRun(trainingLoader, testLoader, ContinuousFeatureQuantisers.getConstantBucketQuantiser(5));
		LOG.info("\t\tConstant Bucket splitter - 10 buckets");
		performNaiveBayesRun(trainingLoader, testLoader, constantBucket);
		LOG.info("\t\tConstant Bucket splitter - 25 buckets");
		performNaiveBayesRun(trainingLoader, testLoader, ContinuousFeatureQuantisers.getConstantBucketQuantiser(25));
		LOG.info("\t\tConstant Bucket splitter - 50 buckets");
		performNaiveBayesRun(trainingLoader, testLoader, ContinuousFeatureQuantisers.getConstantBucketQuantiser(50));
	}
	
	private static void setupLogger() {
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.INFO);
		
		 //Logger logger = (Logger) LoggerFactory.getLogger("abc.xyz");

	     LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	     ConsoleAppender<ILoggingEvent> consoleAppender =
	                       (ConsoleAppender<ILoggingEvent>) rootLogger.getAppender("console");
	     if(consoleAppender != null) {
	       consoleAppender.stop();
	       PatternLayout pl = new PatternLayout();
	       pl.setPattern("%m%n)");
	       pl.setContext(lc);
	       pl.start();
	       consoleAppender.setLayout(pl);
	       consoleAppender.setContext(lc);
	       consoleAppender.start();
	     }
	}
	//0.3311
	private static void performDecisionTreeRuns(ClassifiedDatasetLoader trainingLoader, ClassifiedDatasetLoader testLoader, ImpurityProcessor processor, MissingFeatureClassifier missingFeatureClassifier, ContinuousFeatureQuantiser continuousFeatureSplitter) throws IOException{
		double homogeniousThreshold = 1;
		
		StringBuilder builder = new StringBuilder();
		while(homogeniousThreshold >= 0.75){
			Metrics runMetrics = performRun(trainingLoader, testLoader, homogeniousThreshold, processor, missingFeatureClassifier, ContinuousFeatureSplitters.getFeatureSplitter(continuousFeatureSplitter));
			//Metrics.appendCsvValue(DEFAULT_DECIMAL_FORMAT.format(homogeniousThreshold), builder);
			//builder.append(runMetrics.toCsv());
			//builder.append("\n");
		
			System.out.println("\tthreshold = "+homogeniousThreshold);
			System.out.println(runMetrics);
			
			homogeniousThreshold -= 0.05;
		}
		LOG.info(builder.toString());
	}
	
	private static Metrics performRun(ClassifiedDatasetLoader trainingLoader, ClassifiedDatasetLoader testLoader, double homogeniousThreshold, ImpurityProcessor processor, MissingFeatureClassifier missingFeatureClassifier, ContinuousFeatureSplitter continuousFeatureSplitter) throws IOException{
		ModelService<Id3Model> service = new DecisionTreeModelService(processor, continuousFeatureSplitter, homogeniousThreshold, missingFeatureClassifier);
		
		return performRun(trainingLoader, testLoader, service);
	}
	
	private static <T extends Model> Metrics performRun(ClassifiedDatasetLoader trainingLoader, ClassifiedDatasetLoader testLoader, ModelService<T> modelService) throws IOException{
		try{
			ClassifiedDataset trainingDataset = trainingLoader.getClassifiedDataset();
	
			ClassifiedDataset testDataset = testLoader.getClassifiedDataset();
	
			Model model = ModelService.UTIL.getMetrics(trainingDataset, testDataset, modelService, AdultEarningsClassification.getLessThen50K());
			return model.getMetrics();
		} catch (Error e){
			throw e;
		}
	}
	
	private static void performNaiveBayesRun(ClassifiedDatasetLoader trainingLoader, ClassifiedDatasetLoader testLoader, ContinuousFeatureQuantiser continuousFeatureQuantiser) throws IOException{
		NaiveBayesModelService service = new NaiveBayesModelService(continuousFeatureQuantiser);
		
		Metrics metrics = performRun(trainingLoader, testLoader, service);
		StringBuilder builder = new StringBuilder();
		
		builder.append(metrics.toCsv());
		builder.append("\n");
		
		LOG.info(builder.toString());
	}
}
