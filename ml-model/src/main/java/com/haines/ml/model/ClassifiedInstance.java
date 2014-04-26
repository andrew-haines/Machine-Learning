package com.haines.ml.model;

public interface ClassifiedInstance<C> extends Instance{

	C getClassification();
}
