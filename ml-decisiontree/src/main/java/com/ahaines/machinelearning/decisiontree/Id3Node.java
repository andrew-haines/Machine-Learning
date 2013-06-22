package com.ahaines.machinelearning.decisiontree;

import java.util.ArrayList;
import java.util.Collection;

import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.Feature.Features;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.FeatureSet;

/**
 * An internal node of the decision tree data structure. This class is either a leaf node or the parent of a non terminal
 * decision node {@link DecisionId3Node}. Please note that these classes are implementation details of the Id3 algorithm and
 * as such are scoped to this package only.
 * 
 * @author andrewhaines
 *
 */
class Id3Node<C extends Enum<C>> {

	protected final C mostCommonClassification;
	private final FeatureDefinition feature;
	
	public Id3Node(C mostCommonClassification, FeatureDefinition feature){
		this.mostCommonClassification = mostCommonClassification;
		if (feature == null){
			throw new NullPointerException("feature == null");
		}
		this.feature = feature;
	}
	
	public FeatureDefinition getFeature() {
		return feature;
	}
	
	C getClassification(FeatureSet instance){
		return mostCommonClassification;
	}
	
	public String toString(){
		return "Node: "+feature;
	}
	
	public int countNodes() {
		return 1; // itself
	}
	
	public StringBuilder printTree(String prefix, boolean isEnd, StringBuilder builder){
		
		builder.append(prefix);
		if (isEnd){
			builder.append(" --");
		} else{
			builder.append("|--");
		}
		
		builder.append(toString());
		builder.append("\n");
		
		return builder;
    }
	
	/**
	 * A decision branch node that contains a n > 1 decision on further classification
	 * @author andrewhaines
	 *
	 */
	static class DecisionId3Node<C extends Enum<C>> extends Id3Node<C>{
		
		private final Collection<Id3Node<C>> children;
		private final MissingFeatureClassifier missingFeatureProcessor;
		
		DecisionId3Node(C mostCommonClassification, FeatureDefinition feature, MissingFeatureClassifier missingFeatureProcessor) {
			super(mostCommonClassification, feature);
			this.children = new ArrayList<Id3Node<C>>();
			this.missingFeatureProcessor = missingFeatureProcessor;
		}
		
		void addDecisionNode(Id3Node<C> node){
			this.children.add(node);
		}

		@Override
		public C getClassification(FeatureSet instance) {
			try{
				Feature<?> featureValue = instance.getFeature(super.getFeature().getFeatureType());
				if (featureValue == Features.MISSING){
					missingFeatureProcessor.getClassificationForMissingFeature(instance, this);
					return super.getClassification(instance); // in the event that we do not have this feature, look at the most common classification at this current node
				}
				for (Id3Node<C> child: children){
					if (child.getFeature().intersects(new FeatureDefinition(featureValue, super.getFeature().getFeatureType()))){
						return child.getClassification(instance);
					}
				}
				throw new IllegalStateException("there is no child for feature: "+featureValue+":"+super.getFeature().getFeatureType().getSimpleName()+" at this decision branch: "+children);
			}catch (NullPointerException e){
				throw new NullPointerException("instance:"+instance+" type: "+super.getFeature());
			}
		}
		
		public String toString(){
			return "DecisionNode: "+this.getFeature();
		}
		
		public StringBuilder printTree(String prefix, boolean isEnd, StringBuilder builder){
			
			builder = super.printTree(prefix, isEnd, builder);
			int i = 0;
			for(Id3Node<C> child: children){
				i++;
				
				if (children.size() >= 1 && i == children.size()) {
	                child.printTree(prefix + (isEnd ?"    " : "|   "), true, builder);
	            } else{ 
	            	child.printTree(prefix + (isEnd ? "    " : "|   "), false, builder);
	            }
			}
		
	        return builder;
	    }
		
		@Override
		public int countNodes() {
			int childrenNodeCount = 0;
			
			for (Id3Node<C> child: children){
				childrenNodeCount += child.countNodes();
			}
			
			return super.countNodes() + childrenNodeCount;
		}
		
		Iterable<Id3Node<C>> getChildren(){
			return children;
		}
	}
}
