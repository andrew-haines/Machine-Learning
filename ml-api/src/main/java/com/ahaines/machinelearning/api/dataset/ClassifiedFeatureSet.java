package com.ahaines.machinelearning.api.dataset;

/**
 * A type of {@link FeatureSet} instance that contains a {@link Classification}
 * @author andrewhaines
 *
 */
public class ClassifiedFeatureSet implements FeatureSet{
	private final FeatureSet instance;
	private final Classification<?> classification;
	
	public ClassifiedFeatureSet(FeatureSet instance, Classification<?> classification){
		if (instance == null || classification == null){
			throw new NullPointerException("instance: "+instance+" classification: "+classification);
		}
		this.instance = instance;
		this.classification = classification;
	}

	public Classification<?> getClassification() {
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
}
