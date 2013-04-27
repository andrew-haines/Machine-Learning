package com.ahaines.machinelearning.api.dataset;

/**
 * A feature definitions that defines the feature value and it's type. {@link Feature#getClass()} is not enough
 * to determine the type as default values defined in {@link Feature.Features} might be set.
 * @author andrewhaines
 *
 */
public class FeatureDefinition {

	private final Feature<?> feature;
	private final Class<? extends Feature<?>> featureType;
	
	public FeatureDefinition(Feature<?> feature, Class<? extends Feature<?>> featureType){
		this.feature = feature;
		this.featureType = featureType;
	}
	
	@SuppressWarnings("unchecked")
	public FeatureDefinition(Feature<?> feature){
		this(feature, (Class<? extends Feature<?>>)feature.getClass());
	}
	
	@SuppressWarnings("unchecked")
	public static FeatureDefinition get(Feature<?> feature){
		return new FeatureDefinition(feature, (Class<? extends Feature<?>>)feature.getClass());
	}

	public Feature<?> getFeature() {
		return feature;
	}

	public Class<? extends Feature<?>> getFeatureType() {
		return featureType;
	}
	
	public String toString(){
		return ""+featureType.getSimpleName()+": "+feature;
	}

	@Override
	public boolean equals(Object obj){
		if (obj instanceof FeatureDefinition){
			FeatureDefinition other = (FeatureDefinition)obj;
			
			return other.getFeatureType().equals(this.getFeatureType()) && other.getFeature().equals(this.getFeature());
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int val = 31;
		
		val *= featureType.hashCode();
		val *= feature.hashCode();
		
		return val;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean intersects(FeatureDefinition featureDefinition) {
		return feature.intersects((Feature)featureDefinition.getFeature());
	}
}
