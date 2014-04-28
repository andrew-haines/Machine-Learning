package com.ahaines.machinelearning.api.dataset.wine;

import java.util.Arrays;

import com.ahaines.machinelearning.api.dataset.FeatureSet.FeatureSetFactory;
import com.haines.ml.model.ContinuousFeature.DoubleFeature;
import com.haines.ml.model.Feature;

public class WineFeatures {

	private WineFeatures(){};
	
	public static final Iterable<Class<? extends Feature<?>>> ALL_FEATURE_TYPES = createFeatureTypes(); 
	public static final FeatureSetFactory ADULT_FEATURE_SET = new FeatureSetFactory(ALL_FEATURE_TYPES);
	
	@SuppressWarnings("unchecked")
	private static Iterable<Class<? extends Feature<?>>> createFeatureTypes() {
		return Arrays.<Class<? extends Feature<?>>>asList(TimeStamp.class,
														  SensorSYLG.class,
														  SensorSYG.class,
														  SensorSYAA.class,
														  SensorSYGh.class,
														  SensorSYgCTl.class,
														  SensorSYgCT.class,
														  SensorT301.class,
														  SensorP101.class,
														  SensorP102.class,
														  SensorP401.class,
														  SensorT702.class,
														  SensorPA2.class);
	}

	
	/*
	 * The following defines all the features of this dataset. Each instance contains all these
	 * features. If a feature is missing (which there isnt for this particular dataset), then an
	 * instance can declare the value of the feature as {@link Feature.Features#MISSING)} and 
	 * the framework will handle this accordingly.
	 */
	
	/**
	 * This is not actually a feature but more of a dataset identification. No information can be gained
	 * from this quantity as the prediction set is some completely different varieties
	 * 
	 * TODO although having said this, the wines are potentially blends of similar grapes. 
	 * Varieties that are blends could possibly contain information if a particular grape variety 
	 * might yeild more 4ep/4eg* components. To determine this, I'd need to look at the different
	 * wine training set and see if there is a correlation between certain varieties and predict
	 * the possible blends they might contain. Unfortunately this still wont help in classifing the
	 * prediction set as we have no idea what the blends are in the specified varity. So we cant use
	 * this as a possible feature and this exercise would be purely academic.
	 * 
	 * @author haines
	 *
	 */
	public static enum WineVariety{
		_725,
		_726,
		_727,
		_728,
		_729,
		_730,
		_731,
		_732,
		_733,
		_734,
		_735,
		_736,
		_737,
		_738,
		_739,
		_740,
		_741,
		_742,
		_743,
		_744,
		_745,
		_746,
		_747,
		_748,
		_749,
		_750,
		_751,
		_752,
		_753,
		_754,
		_755,
		_756,
		_757,
		_758,
		_759,
		_760,
		_761,
		_762,
		_763,
		_764,
		_765,
		_766,
		_767,
		_769,
		_770;
	}
	
	
	public static class TimeStamp extends DoubleFeature{

		public TimeStamp(Double value) {
			super(value);
		}
		
	}
	
	public static class SensorSYLG extends DoubleFeature{

		public SensorSYLG(Double value) {
			super(value);
		}
	}
	
	public static class SensorSYG extends DoubleFeature{

		public SensorSYG(Double value) {
			super(value);
		}
	}
	
	public static class SensorSYAA extends DoubleFeature{

		public SensorSYAA(Double value) {
			super(value);
		}
	}
	
	public static class SensorSYGh extends DoubleFeature{

		public SensorSYGh(Double value) {
			super(value);
		}
	}
	
	public static class SensorSYgCTl extends DoubleFeature{

		public SensorSYgCTl(Double value) {
			super(value);
		}
	}
	
	public static class SensorSYgCT extends DoubleFeature{

		public SensorSYgCT(Double value) {
			super(value);
		}
	}
	
	public static class SensorT301 extends DoubleFeature{
		
		public SensorT301(Double value){
			super(value);
		}
	}
	
	public static class SensorP101 extends DoubleFeature{

		public SensorP101(Double value) {
			super(value);
		}
	}
	
	public static class SensorP102 extends DoubleFeature{

		public SensorP102(Double value) {
			super(value);
		}
	}
	
	public static class SensorP401 extends DoubleFeature{

		public SensorP401(Double value) {
			super(value);
		}
	}
	
	public static class SensorT702 extends DoubleFeature{

		public SensorT702(Double value) {
			super(value);
		}
	}
	public static class SensorPA2 extends DoubleFeature{

		public SensorPA2(Double value) {
			super(value);
		}
	}
}
