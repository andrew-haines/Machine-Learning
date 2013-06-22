package com.ahaines.machinelearning.decisiontree;

import java.util.HashMap;
import java.util.Map;

import com.ahaines.machinelearning.api.dataset.FeatureSet;
import com.ahaines.machinelearning.decisiontree.Id3Node.DecisionId3Node;

/**
 * A classifier for classifying missing features.
 * @author andrewhaines
 *
 */
public interface MissingFeatureClassifier{
	
	public static MissingFeatureClassifiers CLASSIFIERS = new MissingFeatureClassifiers();
	
	<C extends Enum<C>> C getClassificationForMissingFeature(FeatureSet instance, DecisionId3Node<C> decisionId3Node);
	
	public static class MissingFeatureClassifiers{
		
		/**
		 * Returns a classifier that classifies based on the most homogenious classification value at the 
		 * current node in the tree
		 * @return
		 */
		public MissingFeatureClassifier getHomogeniousMissingFeatureClassifier(){
			return new MissingFeatureClassifier(){

				@Override
				public <C extends Enum<C>> C getClassificationForMissingFeature(FeatureSet instance, DecisionId3Node<C> decisionId3Node) {
					return decisionId3Node.mostCommonClassification;
				}
				
			};
		}
		
		/**
		 * classifies based on the most probable classification from all current nodes children
		 * @return
		 */
		public MissingFeatureClassifier getMostRatedMissingFeatureClassifier(){
			return new MissingFeatureClassifier(){

				@Override
				public <C extends Enum<C>>C getClassificationForMissingFeature(FeatureSet instance, DecisionId3Node<C> decisionId3Node) {
					Map<C, Integer> classificationCounts = new HashMap<C, Integer>();
					C maximiumVotedClassification = decisionId3Node.mostCommonClassification;
					int highestVote = 0;
					for (Id3Node<C> child: decisionId3Node.getChildren()){
						C childClassification = child.getClassification(instance);
						Integer previousRating = classificationCounts.get(childClassification);
						
						if (previousRating == null){
							previousRating = new Integer(0);
						}
						previousRating++;
						
						if (highestVote < previousRating){
							highestVote = previousRating;
							maximiumVotedClassification = childClassification;
						}
						
						classificationCounts.put(childClassification, previousRating);
					}
					return maximiumVotedClassification;
				}
				
			};
		}
	}
}