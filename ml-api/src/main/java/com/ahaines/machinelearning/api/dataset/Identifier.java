package com.ahaines.machinelearning.api.dataset;

import java.util.UUID;

/**
 * A placeholder interface for defining an identifier that uniquely defines a instance. A number
 * of static factory methods exist on the {@link #FACTORY} instance that simplify the creation
 * of identifiers.
 * 
 * @author andrewhaines
 *
 */
public interface Identifier {
	
	public static IdentifierFactory FACTORY = new IdentifierFactory();

	static class IdentifierFactory{
		
		/**
		 * creates an identifier based on the supplied integer
		 * @param id
		 * @return
		 */
		public Identifier createIdentifier(final int id){
			return new AbstractIdentifier(){

				@Override
				protected Object getPrimitiveValue() {
					return id;
				}
			};
		}
		
		/**
		 * Creates an identifier based on the supplied long
		 * @param id
		 * @return
		 */
		public Identifier createIdentifier(final long id){
			return new AbstractIdentifier(){

				@Override
				protected Object getPrimitiveValue() {
					return id;
				}
			};
		}
		
		/**
		 * Creates a random identifier backed by a {@link UUID}
		 * @return
		 */
		public Identifier createRandomIdentifier(){
			final UUID guid = UUID.randomUUID();
			return new AbstractIdentifier() {
				
				@Override
				protected Object getPrimitiveValue() {
					return guid;
				}
			};
		}
	}
	
	static abstract class AbstractIdentifier implements Identifier{

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AbstractIdentifier){
				AbstractIdentifier other = (AbstractIdentifier)obj;
				
				return this.getPrimitiveValue().equals(other.getPrimitiveValue());
			}
			
			return false;
		}

		@Override
		public int hashCode() {
			return getPrimitiveValue().hashCode();
		}
		
		protected abstract Object getPrimitiveValue();
		
		public String toString(){
			return getPrimitiveValue().toString();
		}
	}
}
