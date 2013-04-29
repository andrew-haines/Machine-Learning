package com.ahaines.machinelearning.api.util;

import java.util.Collection;

import com.google.common.collect.Lists;

public class Utils {

	public static <E> Collection<E> toCollection(Iterable<E> iterable) {
	    return (iterable instanceof Collection)
	        ? (Collection<E>) iterable
	        : Lists.newArrayList(iterable.iterator());
	  }
}
