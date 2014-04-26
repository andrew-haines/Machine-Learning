package com.ahaines.machinelearning.api.dataset;

import com.ahaines.machinelearning.api.dataset.Classification;
import com.haines.ml.model.ClassifiedInstance;
import com.haines.ml.model.Feature;

/**
 * A type of {@link FeatureSet} instance that contains a {@link Classification}
 * @author andrewhaines
 *
 */
public class ClassifiedFeatureSet<C> implements FeatureSet, ClassifiedInstance<Classification<C>>{
	private final FeatureSet instance;
	private final Classification<C> classification;
	
	public ClassifiedFeatureSet(FeatureSet instance, Classification<C> classification){
		if (instance == null || classification == null){
			throw new NullPointerException("instance: "+instance+" classification: "+classification);
		}
		this.instance = instance;
		this.classification = classification;
	}

	public Classification<C> getClassification() {
		return classification;
	}
	
	public String toString(){
		return instance.toString()+" -> "+classification;
	}

	@Override
	public Identifier getId() {
		return instance.getId();
	}

	@Override
	public <T extends Feature<?>> T getFeature(Class<T> featureType) {
		return instance.getFeature(featureType);
	}

	@Override
	public Iterable<Class<? extends Feature<?>>> getFeatureTypes() {
		return instance.getFeatureTypes();
	}

	@Override
	public Iterable<Feature<?>> getFeatures() {
		return instance.getFeatures();
	}
}
