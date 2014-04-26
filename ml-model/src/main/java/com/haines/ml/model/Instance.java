package com.haines.ml.model;

public interface Instance {

	Iterable<Feature<?>> getFeatures();
}
