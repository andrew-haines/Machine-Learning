package com.ahaines.machinelearning.api.dataset;

public interface ClassifiedDatasetLoader<C> {

	ClassifiedDataset<C> getClassifiedDataset();
}