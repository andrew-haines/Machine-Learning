package com.ahaines.machinelearning.api.dataset.adultearnings;

import com.ahaines.machinelearning.api.dataset.Classification;
import com.ahaines.machinelearning.api.dataset.Identifier;

public class AdultEarningsClassification extends Classification<AdultEarningsClassificationType>{

	public AdultEarningsClassification(Identifier id, AdultEarningsClassificationType value) {
		super(id, value);
	}
	
	public static AdultEarningsClassificationType getGreaterThen50KClassification(){
		return AdultEarningsClassificationType.GREATER_THEN_50K;
	}
	
	public static AdultEarningsClassificationType getLessThen50K(){
		return AdultEarningsClassificationType.LESS_THEN_50K;
	}
}

enum AdultEarningsClassificationType {

	GREATER_THEN_50K,
	LESS_THEN_50K
}