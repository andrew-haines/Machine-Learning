package com.ahaines.machinelearning.api.dataset;

import java.util.Iterator;
import java.util.Map;

import com.ahaines.machinelearning.api.dataset.Feature.Features;
import com.ahaines.machinelearning.api.util.CachedIterable;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * A type of {@link Dataset} that contains classified instances.
 * @author andrewhaines
 *
 */
public interface ClassifiedDataset extends Dataset<ClassifiedFeatureSet>{
	
	public static final ClassifiedDatasetFactory FACTORY = new ClassifiedDatasetFactory();
	
	public Map<Identifier, ? extends Classification<?>> getClassifications();
	
	public static class ClassifiedDatasetImpl implements ClassifiedDataset{
		
		protected final Dataset<? extends FeatureSet> dataset;
		protected final Map<Identifier, ? extends Classification<?>> classifications;
		private final Iterable<ClassifiedFeatureSet> instances;
		
		protected ClassifiedDatasetImpl(Dataset<? extends FeatureSet> dataset, final Map<Identifier, ? extends Classification<?>> classifications){
			this.dataset = dataset;
			this.classifications = classifications;
			
			final Iterable<? extends FeatureSet> dataInstances = dataset.getInstances();
			
			instances = new CachedIterable<ClassifiedFeatureSet>(new Iterable<ClassifiedFeatureSet>(){

				@Override
				public Iterator<ClassifiedFeatureSet> iterator() {
					final Iterator<? extends FeatureSet> it = dataInstances.iterator();
					
					return new Iterator<ClassifiedFeatureSet>(){

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public ClassifiedFeatureSet next() {
							FeatureSet featureSet = it.next();
							return new ClassifiedFeatureSet(featureSet, classifications.get(featureSet.getId()));
						}

						@Override
						public void remove() {
							it.remove();
						}
						
					};
				}
				
			});
		}
		
		public ClassifiedFeatureSet getInstance(Identifier instanceId){
			FeatureSet instance = dataset.getInstance(instanceId);
			
			Classification<?> classification = classifications.get(instanceId);
			
			return new ClassifiedFeatureSet(instance, classification);
		}
		
		public String toString(){
			
			StringBuilder builder = new StringBuilder();
			for (ClassifiedFeatureSet instance: getInstances()){
				builder.append(instance);
				builder.append("\n");
			}
			return builder.toString();
		}
		
		@Override
		public Iterable<Class<? extends Feature<?>>> getFeatureTypes(){
			return dataset.getFeatureTypes();
		}
		
		@Override
		public Iterable<ClassifiedFeatureSet> getInstances(){
			return instances;
		}

		@Override
		public Map<Identifier, ? extends Classification<?>> getClassifications() {
			return classifications;
		}
	}
	
	public static class ClassifiedDatasetFactory {
		
		public ClassifiedDataset create(Dataset<? extends FeatureSet> dataset, Iterable<? extends Classification<?>> classifications){
			
			return create(dataset, Identifiable.UTIL.index(classifications));
		}
		
		public ClassifiedDataset create(Dataset<? extends FeatureSet> dataset, Map<Identifier, ? extends Classification<?>> classifications){
			
			return new ClassifiedDatasetImpl(dataset, classifications);
		}
		
		public Iterable<ClassifiedFeatureSet> filterFeatureSet(Iterable<ClassifiedFeatureSet> instances, final FeatureDefinition feature){
			return Iterables.filter(instances, new Predicate<ClassifiedFeatureSet>(){
				
				@SuppressWarnings({ "rawtypes"})
				@Override
				public boolean apply(ClassifiedFeatureSet input){
					
					Feature<?> instanceFeatureValue = input.getFeature(feature.getFeatureType());
					
					if (instanceFeatureValue == Features.MISSING){
						 // if we are a missing feature then include in filter.
						
						return true;
					}
					return instanceFeatureValue.equals((Feature)feature.getFeature());
				}
			});
		}
	}
}