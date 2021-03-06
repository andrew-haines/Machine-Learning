package com.ahaines.machinelearning.decisiontree;

import com.ahaines.machinelearning.api.Model;
import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.FeatureSet;

public class Id3Model<C extends Enum<C>> implements Model{

	private final Id3Node<C> root;
	private final Metrics metrics;
	
	public Id3Model(Id3Node<C> root){
		this.root = root;
		this.metrics = new Id3Metrics(root.countNodes());
	}

	public Classification<C> getClassification(FeatureSet instance) {
		return new Classification<C>(instance.getId(), root.getClassification(instance));
	}
	
	public String toString(){
		return root.printTree("", false, new StringBuilder()).toString();
	}
	
	public int countNodes(){
		return root.countNodes();
	}

	@Override
	public Metrics getMetrics() {
		return metrics;
	}
	
	private static class Id3Metrics extends Metrics{

		private final int modelSize;
		public Id3Metrics(int numberNodes) {
			this.modelSize = numberNodes;
		}
		@Override
		public StringBuilder toCsv() {
			StringBuilder builder = new StringBuilder();
			
			appendCsvValue(modelSize, builder);
			builder.append(super.toCsv());
			return builder;
		}
		@Override
		public String toString() {
			String superString = super.toString();
			
			return "\t\tmodelSize: " +modelSize+"\n"+superString;
		}
		
		
	}
}
