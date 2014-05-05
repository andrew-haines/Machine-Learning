package com.ahaines.machinelearning.api.dataset.wine;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.ahaines.machinelearning.api.dataset.ClassifiedDataset;
import com.ahaines.machinelearning.api.dataset.ClassifiedDatasetLoader;

public class WineDatasetLoaders {
	
	private static final Pattern WINE_FILENAME_PATTERN = Pattern.compile("$(.*).txt^");
	
	private static ClassifiedDatasetLoader<WineClassification> getLoader(final URI uri){
		return new ClassifiedDatasetLoader<WineClassification>(){

			@Override
			public ClassifiedDataset<WineClassification> getClassifiedDataset() {
				try {
					return loadClassifiedData(uri);
				} catch (IOException e) {
					throw new RuntimeException("Unable to load classified dataset: "+uri.toString(), e);
				}
			}
			
		};
	}

	private static ClassifiedDataset<WineClassification> loadClassifiedData(URI uri) throws IOException{
	
		Path path = ClassifiedDatasetLoader.UTIL.getPath(uri);
		
		if (Files.isDirectory(path)){
			DirectoryStream<Path> directoryContents = Files.newDirectoryStream(path, new DirectoryStream.Filter<Path>(){

				@Override
				public boolean accept(Path entry) throws IOException {
					return WINE_FILENAME_PATTERN.matcher(entry.getFileName().toString()).matches();
				}
				
			});
			
			ClassifiedDataset<WineClassification> loader = null;
			
			for (Path file: directoryContents){
				
			}
			return loader;
		} else {
			throw new IllegalArgumentException(uri.toString()+" needs to be a directory.");
		}
	}
}
