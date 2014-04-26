package com.ahaines.machinelearning.api.dataset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.haines.ml.model.Feature;

/**
 * This represents a set of instances
 * @author andrewhaines
 *
 * @param <T> The type of feature set that defines the instances of the set.
 */
public interface Dataset<T extends FeatureSet> {
	
	/**
	 * Returns the instance uniquely defined by the supplied identifier
	 * @param instanceId
	 * @return
	 */
	public T getInstance(Identifier instanceId);
	
	/**
	 * Returns all instances in the dataset
	 * @return
	 */
	public Iterable<T> getInstances();
	
	/**
	 * Returns all feature types contained in the dataset. Each instance is enforced to have definitions for
	 * each of these types.
	 * @return
	 */
	public Iterable<Class<? extends Feature<?>>> getFeatureTypes();	
	
	static class DatasetImpl implements Dataset<FeatureSet>{

		private final Map<Identifier, FeatureSet> instances;
		private final Iterable<? extends Class<? extends Feature<?>>> featureTypes;
		
		private DatasetImpl(Map<Identifier, FeatureSet> instances, Iterable<? extends Class<? extends Feature<?>>> featureTypes){
			this.instances = Collections.unmodifiableMap(instances);
			this.featureTypes = featureTypes;
		}
		
		@Override
		public FeatureSet getInstance(Identifier instanceId){
			return instances.get(instanceId);
		}
	
		@Override
		public Iterable<FeatureSet> getInstances() {
			return instances.values();
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Iterable<Class<? extends Feature<?>>> getFeatureTypes() {
			return (Iterable<Class<? extends Feature<?>>>)(Iterable<?>)featureTypes;
		}
	}
	
	public static class DatasetBuilder {
		
		private final Map<Identifier, FeatureSet> dataset;
		private final Iterable<? extends Class<? extends Feature<?>>> featureTypes;
		
		public DatasetBuilder(Iterable<? extends Class<? extends Feature<?>>> featureTypes){
			this(new HashMap<Identifier, FeatureSet>(),featureTypes);
		}
		
		public DatasetBuilder(Iterable<FeatureSet> features, Iterable<? extends Class<? extends Feature<?>>> featureTypes){
			this(Identifiable.UTIL.index(features), featureTypes);
		}
		
		public DatasetBuilder(Map<Identifier, FeatureSet> dataset, Iterable<? extends Class<? extends Feature<?>>> featureTypes){
			this.dataset = dataset;
			this.featureTypes = featureTypes;
		}

		public void addInstance(FeatureSet instance){
			this.dataset.put(instance.getId(), instance);
		}
		
		public Dataset<FeatureSet> build(){
			return new DatasetImpl(dataset, featureTypes);
		}
	}
}
