package com.ahaines.machinelearning.api.dataset.wine;

import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.Identifier;

public class WineClassification extends Classification<WineAnalytes>{

	public WineClassification(Identifier id, WineAnalytes value) {
		super(id, value);
	}
}
