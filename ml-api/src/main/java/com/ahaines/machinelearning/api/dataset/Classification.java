package com.ahaines.machinelearning.api.dataset;

/**
 * Defines a classification value to an instance denoted by {@link #getId()}.
 * @author andrewhaines
 *
 * @param <T> The enum that represents the classifications
 */
public class Classification<T extends Enum<T>> implements Identifiable{

	private final Identifier id;
	private final T value;
	
	public Classification(Identifier id, T value){
		this.id = id;
		this.value = value;
	}
	
	@Override
	public Identifier getId() {
		return id;
	}
	
	public T getValue(){
		return value;
	}
	
	public String toString(){
		return value.toString();
	}
}
