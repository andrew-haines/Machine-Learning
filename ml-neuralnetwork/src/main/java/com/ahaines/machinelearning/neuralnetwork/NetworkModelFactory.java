package com.ahaines.machinelearning.neuralnetwork;

public interface NetworkModelFactory<C, T extends NetworkModel<C, T>> {

	public T createModel();
}
