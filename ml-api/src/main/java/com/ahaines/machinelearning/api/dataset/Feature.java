package com.ahaines.machinelearning.api.dataset;

/**
 * Defines a unique feature of an instance. Each feature contains a value defined by {@link #getValue()}.
 * The {@link #intersects(Feature)} determines if another feature's value intercepts this one.
 * @author andrewhaines
 *
 * @param <T>
 */
public interface Feature<T> {

	/**
	 * Returns the value of this feature
	 * @return
	 */
	T getValue();
	
	/**
	 * returns true if this feature intercepts the feature specified
	 * @param otherFeature
	 * @return
	 */
	boolean intersects(Feature<T> otherFeature);
	
	public static enum Features implements Feature<Void>{
		
		MISSING,
		ROOT;

		@Override
		public Void getValue() {
			return null; // there is not value for these types.
		}

		@Override
		public boolean intersects(Feature<Void> otherFeature) {
			throw new UnsupportedOperationException("interception should not be used for a structural element");
		}
	}
}
