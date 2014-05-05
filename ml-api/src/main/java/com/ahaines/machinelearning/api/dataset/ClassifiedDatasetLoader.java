package com.ahaines.machinelearning.api.dataset;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ClassifiedDatasetLoader<C> {
	
	public static final Util UTIL = new Util();

	ClassifiedDataset<C> getClassifiedDataset();
	
	public static class Util{
		
		private static final Logger LOG = LoggerFactory.getLogger(Util.class);
		
		private Util(){}
		
		public Path getPath(URI uri) throws IOException{
			
			Path path;
			try{
				path = Paths.get(uri);
			} catch (FileSystemNotFoundException e){
				
				if (uri.getScheme().equalsIgnoreCase("jar")){
					String completeUri = uri.toString();
					
					int jarFileIdx = completeUri.indexOf(".jar!")+4;
					
					String jarFileLoc = completeUri.substring(0, jarFileIdx);
					String fileInJar = completeUri.substring(jarFileIdx+1);
					
					LOG.debug("Jar file location = "+jarFileLoc);
					LOG.debug("File in jar = "+fileInJar);
					
					FileSystem zipFs = FileSystems.newFileSystem(URI.create(jarFileLoc), Collections.<String, Object>emptyMap());
					path = zipFs.getPath(fileInJar);
				} else{
					throw e;
				}
			}
			
			return path;
		}
	}
}