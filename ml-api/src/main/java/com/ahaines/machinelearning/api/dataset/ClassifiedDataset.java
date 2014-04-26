package com.ahaines.machinelearning.api.dataset;

import java.util.Iterator;
import java.util.Map;

import com.ahaines.machinelearning.api.util.CachedIterable;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.ahaines.machinelearning.api.dataset.Classification;
import com.haines.ml.model.Feature;
import com.haines.ml.model.Feature.Features;

/**
 * A type of {@link Dataset} that contains classified instances.
 * @author andrewhaines
 *
 */
public interface ClassifiedDataset<C> extends Dataset<ClassifiedFeatureSet<C>>{
	
	public static final ClassifiedDatasetFactory FACTORY = new ClassifiedDatasetFactory();
	
	public Map<Identifier, ? extends Classification<C>> getClassifications();
	
	public static class ClassifiedDatasetImpl<C> implements ClassifiedDataset<C>{
		
		protected final Dataset<? extends FeatureSet> dataset;
		protected final Map<Identifier, ? extends Classification<C>> classifications;
		private final Iterable<ClassifiedFeatureSet<C>> instances;
		
		protected ClassifiedDatasetImpl(Dataset<? extends FeatureSet> dataset, final Map<Identifier, ? extends Classification<C>> classifications){
			this.dataset = dataset;
			this.classifications = classifications;
			
			final Iterable<? extends FeatureSet> dataInstances = dataset.getInstances();
			
			instances = new CachedIterable<ClassifiedFeatureSet<C>>(new Iterable<ClassifiedFeatureSet<C>>(){

				@Override
				public Iterator<ClassifiedFeatureSet<C>> iterator() {
					final Iterator<? extends FeatureSet> it = dataInstances.iterator();
					
					return new Iterator<ClassifiedFeatureSet<C>>(){

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public ClassifiedFeatureSet<C> next() {
							FeatureSet featureSet = it.next();
							return new ClassifiedFeatureSet<C>(featureSet, classifications.get(featureSet.getId()));
						}

						@Override
						public void remove() {
							it.remove();
						}
						
					};
				}
				
			});
		}
		
		public ClassifiedFeatureSet<C> getInstance(Identifier instanceId){
			FeatureSet instance = dataset.getInstance(instanceId);
			
			Classification<C> classification = classifications.get(instanceId);
			
			return new ClassifiedFeatureSet<C>(instance, classification);
		}
		
		public String toString(){
			
			StringBuilder builder = new StringBuilder();
			for (ClassifiedFeatureSet<C> instance: getInstances()){
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
		public Iterable<ClassifiedFeatureSet<C>> getInstances(){
			return instances;
		}

		@Override
		public Map<Identifier, ? extends Classification<C>> getClassifications() {
			return classifications;
		}
	}
	
	public static class ClassifiedDatasetFactory {
		
		public <C> ClassifiedDataset<C> create(Dataset<? extends FeatureSet> dataset, Iterable<? extends Classification<C>> classifications){
			
			return create(dataset, Identifiable.UTIL.index(classifications));
		}
		
		public <C> ClassifiedDataset<C> create(Dataset<? extends FeatureSet> dataset, Map<Identifier, ? extends Classification<C>> classifications){
			
			return new ClassifiedDatasetImpl<C>(dataset, classifications);
		}
		
		public <C> Iterable<ClassifiedFeatureSet<C>> filterFeatureSet(Iterable<ClassifiedFeatureSet<C>> instances, final FeatureDefinition feature){
			return Iterables.filter(instances, new Predicate<ClassifiedFeatureSet<C>>(){
				
				@Override
				public boolean apply(ClassifiedFeatureSet<C> input){
					
					Feature<?> instanceFeatureValue = input.getFeature(feature.getFeatureType());
					
					if (instanceFeatureValue == Features.MISSING){
						 // if we are a missing feature then include in filter.
						
						return true;
					}
					return instanceFeatureValue.equals((Feature<?>)feature.getFeature());
				}
			});
		}
	}
}