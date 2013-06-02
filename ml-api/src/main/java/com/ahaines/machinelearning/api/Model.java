package com.ahaines.machinelearning.api;

public interface Model {

	Metrics getMetrics();
	
	public static class Metrics {
		long timeToBuildModel;
		long timeToClassify;
		double errorRate;
		double accuracy;
		double weightedAccuracy;
		double weightedErrorRate;
		double truePositiveRate;
		double trueNegativeRate;
		int numPositives;
		int numNegatives;
		int numPredictedPositives;
		int numPredictedNegatives;
		double fMeasure;
		
		public String toString(){
			return "\t\tTime to build model: "+timeToBuildModel+"\n" +
				   "\t\tTime to classify: "+timeToClassify+"\n" +
				   "\t\tTPR: "+truePositiveRate+"\n"+
				   "\t\tTNR: "+trueNegativeRate+"\n"+
				   "\t\tnumPositives: "+numPositives+"\n"+
				   "\t\tnumNegatives: "+numNegatives+"\n"+
				   "\t\tError Rate: "+errorRate+"\n" +
				   "\t\tAccuracy: "+accuracy+"\n" +
				   "\t\tfMeasure: "+fMeasure+"\n";
		}
		
		public StringBuilder toCsv(){
			StringBuilder builder = new StringBuilder();
			
			appendCsvValue(timeToBuildModel, builder);
			appendCsvValue(timeToClassify, builder);
			appendCsvValue(numPositives, builder);
			appendCsvValue(numNegatives, builder);
			appendCsvValue(truePositiveRate, builder);
			appendCsvValue(trueNegativeRate, builder);
			appendCsvValue(errorRate, builder);
			appendCsvValue(accuracy, builder);
			appendCsvValue(weightedAccuracy, builder);
			appendCsvValue(weightedErrorRate, builder);
			appendCsvValue(fMeasure, builder, true);
			
			return builder;
		}
		
		public static void appendCsvValue(Object value, StringBuilder builder) {
			appendCsvValue(value, builder, false);
		}

		private static void appendCsvValue(Object value, StringBuilder builder, boolean lastValue){
			builder.append("\"");
			builder.append(value);
			builder.append("\"");
			if (!lastValue){
				builder.append(",");
			}
		}
		
		public double getAccuracy(){
			return accuracy;
		}
	}
}
