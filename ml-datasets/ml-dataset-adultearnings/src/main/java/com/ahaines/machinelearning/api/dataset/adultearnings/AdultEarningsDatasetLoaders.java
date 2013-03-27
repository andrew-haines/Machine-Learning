package com.ahaines.machinelearning.api.dataset.adultearnings;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;

import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedDatasetLoader;
import com.ahaines.machinelearning.api.dataset.ContinuousFeature.IntegerFeature;
import com.ahaines.machinelearning.api.dataset.Dataset.DatasetBuilder;
import com.ahaines.machinelearning.api.dataset.Feature.Features;
import com.ahaines.machinelearning.api.dataset.DiscreteFeature;
import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.FeatureDefinition;
import com.ahaines.machinelearning.api.dataset.Identifier;
import com.ahaines.machinelearning.api.dataset.adultearnings.AdultEarningsFeaures.EducationFeature;

/**
 * The dataloaders for loading and parsing the raw training and test files into the domain
 * representations
 * @author andrewhaines
 *
 */
public final class AdultEarningsDatasetLoaders{

	private static final String TRAINING_DATASET = "/adult.data.txt";
	private static final String TEST_DATASET = "/adult.test.txt";

	private AdultEarningsDatasetLoaders(){
	}

	public static ClassifiedDatasetLoader getTrainingDatasetLoader() throws IOException{
		return getTrainingDatasetLoader(TRAINING_DATASET);
	}
	
	public static ClassifiedDatasetLoader getTestDatasetLoader() throws IOException{
		return getTrainingDatasetLoader(TEST_DATASET);
	}
	
	public static ClassifiedDatasetLoader getTrainingDatasetLoader(String location) throws IOException {
		URL fileLocation = AdultEarningsDatasetLoaders.class.getResource(location);
		
		try{
			final ClassifiedDataset dataset = loadDataset(fileLocation.toURI(), true);
			return new ClassifiedDatasetLoader(){

				@Override
				public ClassifiedDataset getClassifiedDataset() {
					return dataset;
				}
				
			};
		} catch (URISyntaxException e){
			throw new RuntimeException("unable to load training set", e);
		}
		
	}

	private static ClassifiedDataset loadDataset(URI uri, boolean includeMissingFeatures) throws IOException {
		Path path = Paths.get(uri);
		
		List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
		// trim first 4 lines
		
		lines = lines.subList(4, lines.size());
		DatasetBuilder dataset = new DatasetBuilder(AdultEarningsFeaures.ALL_FEATURE_TYPES);
		Map<Identifier, AdultEarningsClassification> classifications = new HashMap<Identifier, AdultEarningsClassification>();
		int id = 0;
		int count = 0;
		for (String rawInstance: lines){
			Identifier instanceIdentifier = Identifier.FACTORY.createIdentifier(id++);
			List<FeatureDefinition> instanceFeatures = new ArrayList<FeatureDefinition>();
			String[] features = rawInstance.split(",");
			boolean containsMissingFeatures = false;
			
			for (int i = 0; i < features.length; i++){
				if (i < AdultEarningsFeaures.ALL_FEATURE_TYPES.size()){
					Class<? extends Feature<?>> featureType = AdultEarningsFeaures.ALL_FEATURE_TYPES.get(i);
					
					Feature<?> featureValue = getFeature(featureType, features[i]);
					if (featureValue == Features.MISSING){
						containsMissingFeatures = true;
					}
					if (featureValue == null){
						throw new NullPointerException("feature type: "+featureType+" value: "+features[i]);
					}
					instanceFeatures.add(new FeatureDefinition(featureValue, featureType));
					
				} else if (i == AdultEarningsFeaures.ALL_FEATURE_TYPES.size()){ // this is the classification entry
					AdultEarningsClassificationType classifier;
					
					switch (features[i].trim()){
					case ">50K": classifier = AdultEarningsClassificationType.GREATER_THEN_50K;break;
					case "<=50K":classifier = AdultEarningsClassificationType.LESS_THEN_50K;break;
					default: throw new IllegalArgumentException("unknown classification: "+features[i]);
					}
					
					classifications.put(instanceIdentifier, new AdultEarningsClassification(instanceIdentifier, classifier));
				}
				else{
					throw new RuntimeException("Unknown feature index: "+i+". Instance data is: "+rawInstance);
				}
			}
			if (!containsMissingFeatures || includeMissingFeatures){
				count++;
				dataset.addInstance(AdultEarningsFeaures.ADULT_FEATURE_SET.createFeatureSet(instanceIdentifier, instanceFeatures));
			}
		}
		
		System.out.println("loaded: "+count+" instances");
		
		return ClassifiedDataset.create(dataset.build(), classifications);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Feature<?> getFeature(Class<? extends Feature<?>> featureType, String featureString) {
		String processedFeatureString = featureString.trim()
													 .toUpperCase()
													 .replaceAll("-|&|\\(|\\)", "_");
		if (processedFeatureString.equals("?")){
			return Features.MISSING;
		} else if (IntegerFeature.class.isAssignableFrom(featureType)){ // i hate reflection but it does reduce alot of code in this instance
			try {
				return featureType.getConstructor(Integer.class).newInstance(Integer.parseInt(processedFeatureString));
				
			} catch (InstantiationException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				throw new RuntimeException("Unable to parse adult data file for feature: "+featureType+" and value: "+featureString, e);
			}
		} else if (Enum.class.isAssignableFrom(featureType)){
		
			if (featureType == EducationFeature.class){
				switch (processedFeatureString){
					case "11TH": return EducationFeature.ELEVETH;
					case "9TH": return EducationFeature.NINTH;
					case "10TH": return EducationFeature.TENTH;
					case "12TH": return EducationFeature.TWELFTH;
					case "7TH_8TH": return EducationFeature.SEVENTH_EIGHTH;
					case "5TH_6TH": return EducationFeature.FIFTH_SIXTH;
					case "1ST_4TH": return EducationFeature.FIRST_FOURTH;
				}
			}
			
			return (DiscreteFeature) EnumUtils.getEnum((Class<? extends Enum>)featureType, processedFeatureString);
		}
		else{
			throw new RuntimeException("Unknown feature type: "+featureType);
		}
	}
}