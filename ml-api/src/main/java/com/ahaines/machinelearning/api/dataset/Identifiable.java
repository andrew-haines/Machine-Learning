package com.ahaines.machinelearning.api.dataset;

import java.util.HashMap;
import java.util.Map;

/**
 * implementors of this interface defines that they can be uniquely identified by an {@link Identifier}.
 * @author andrewhaines
 *
 */
public interface Identifiable {
	
	public static final Utils UTIL = new Utils();

	Identifier getId();
	
	static class Utils{
		
		public <T extends Identifiable> Map<Identifier, T> index(Iterable<T> identifiables){
			return index(identifiables, new HashMap<Identifier, T>());
		}
		
		public <T extends Identifiable> Map<Identifier, T> index(Iterable<T> identifiables, Map<Identifier, T> mapToIndexTo){
			for (T identifiable: identifiables){
				mapToIndexTo.put(identifiable.getId(), identifiable);
			}
			
			return mapToIndexTo;
		}
		
		
	}
}
