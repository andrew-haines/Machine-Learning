package com.ahaines.machinelearning.api.dataset.adultearnings;

import java.util.Arrays;
import java.util.List;

import com.ahaines.machinelearning.api.dataset.ContinuousFeature.IntegerFeature;
import com.ahaines.machinelearning.api.dataset.DiscreteFeature;
import com.ahaines.machinelearning.api.dataset.Feature;
import com.ahaines.machinelearning.api.dataset.FeatureSet.FeatureSetFactory;

/**
 * This class defines all the feature types and validation rules for 1994 census dataset.
 * @author andrewhaines
 *
 */
public final class AdultEarningsFeaures {
	
	private AdultEarningsFeaures(){};

	public static final List<Class<? extends Feature<?>>> ALL_FEATURE_TYPES = createFeatureTypes(); 
	public static final FeatureSetFactory ADULT_FEATURE_SET = new FeatureSetFactory(ALL_FEATURE_TYPES);
	
	private static List<Class<? extends Feature<?>>> createFeatureTypes() {
		return Arrays.<Class<? extends Feature<?>>>asList(AgeFeature.class, 
															    WorkClassFeature.class, 
															    FnlWgtFeature.class, 
															    EducationFeature.class, 
															    EducationNumFeature.class,
															    MaritalStatusFeature.class,
															    OccupationFeature.class,
															    RelationshipFeature.class,
															    RaceFeature.class,
															    SexFeature.class,
															    CapitalGainFeature.class,
															    CapitalLossFeature.class,
															    HoursPerWeekFeature.class,
															    NativeCountryFeature.class);
	}
	
	public static class AgeFeature extends IntegerFeature{
		
		public AgeFeature(Integer age){
			super(age);
		}
	}
	
	public static enum WorkClassFeature implements DiscreteFeature<WorkClassFeature>{
		STATE_GOV,
		LOCAL_GOV,
		FEDERAL_GOV,
		SELF_EMP_NOT_INC,
		SELF_EMP_INC,
		PRIVATE,
		WITHOUT_PAY,
		NEVER_WORKED;

		@Override
		public WorkClassFeature getValue() {
			return this;
		}

		@Override
		public boolean intersects(Feature<WorkClassFeature> otherFeature) {
			return this == otherFeature;
		}
	}
	
	public static class FnlWgtFeature extends IntegerFeature{

		public FnlWgtFeature(Integer value) {
			super(value);
		}
	}
	
	public static enum EducationFeature implements DiscreteFeature<EducationFeature>{
		BACHELORS,
		SOME_COLLEGE,
		ELEVETH,
		HS_GRAD,
		PROF_SCHOOL,
		ASSOC_ACDM,
		ASSOC_VOC,
		NINTH,
		SEVENTH_EIGHTH,
		TWELFTH,
		MASTERS,
		FIRST_FOURTH,
		TENTH,
		DOCTORATE,
		FIFTH_SIXTH,
		PRESCHOOL;

		@Override
		public EducationFeature getValue() {
			return this;
		}
		
		@Override
		public boolean intersects(Feature<EducationFeature> otherFeature) {
			return this == otherFeature;
		}
	}
	
	public static class EducationNumFeature extends IntegerFeature{

		public EducationNumFeature(Integer value) {
			super(value);
		}
	}
	
	public static enum MaritalStatusFeature implements DiscreteFeature<MaritalStatusFeature>{
		MARRIED_CIV_SPOUSE, 
		DIVORCED, 
		NEVER_MARRIED, 
		SEPARATED, 
		WIDOWED, 
		MARRIED_SPOUSE_ABSENT, 
		MARRIED_AF_SPOUSE;

		@Override
		public MaritalStatusFeature getValue() {
			return this;
		}
		
		@Override
		public boolean intersects(Feature<MaritalStatusFeature> otherFeature) {
			return this == otherFeature;
		}
	}
	
	public static enum OccupationFeature implements DiscreteFeature<OccupationFeature>{
		TECH_SUPPORT, 
		CRAFT_REPAIR, 
		OTHER_SERVICE, 
		SALES, 
		EXEC_MANAGERIAL, 
		PROF_SPECIALTY, 
		HANDLERS_CLEANERS, 
		MACHINE_OP_INSPCT, 
		ADM_CLERICAL, 
		FARMING_FISHING, 
		TRANSPORT_MOVING, 
		PRIV_HOUSE_SERV, 
		PROTECTIVE_SERV, 
		ARMED_FORCES;

		@Override
		public OccupationFeature getValue() {
			return this;
		}
		
		@Override
		public boolean intersects(Feature<OccupationFeature> otherFeature) {
			return this == otherFeature;
		}
	}
	
	public static enum RelationshipFeature implements DiscreteFeature<RelationshipFeature>{
		WIFE, 
		OWN_CHILD,
		HUSBAND, 
		NOT_IN_FAMILY, 
		OTHER_RELATIVE, 
		UNMARRIED;

		@Override
		public RelationshipFeature getValue() {
			return this;
		}
		
		@Override
		public boolean intersects(Feature<RelationshipFeature> otherFeature) {
			return this == otherFeature;
		}
	}
	
	public static enum RaceFeature implements DiscreteFeature<RaceFeature>{
		WHITE, 
		ASIAN_PAC_ISLANDER, 
		AMER_INDIAN_ESKIMO, 
		OTHER, 
		BLACK;

		@Override
		public RaceFeature getValue() {
			return this;
		}
		
		@Override
		public boolean intersects(Feature<RaceFeature> otherFeature) {
			return this == otherFeature;
		}
	}
	
	public static enum SexFeature implements DiscreteFeature<SexFeature>{
		MALE,
		FEMALE;

		@Override
		public SexFeature getValue() {
			return this;
		}
		
		@Override
		public boolean intersects(Feature<SexFeature> otherFeature) {
			return this == otherFeature;
		}
	}
	
	public static class CapitalGainFeature extends IntegerFeature{

		public CapitalGainFeature(Integer gain) {
			super(gain);
		}
	}
	
	public static class CapitalLossFeature extends IntegerFeature{

		public CapitalLossFeature(Integer loss) {
			super(loss);
		}
	}
	
	public static class HoursPerWeekFeature extends IntegerFeature{

		public HoursPerWeekFeature(Integer hours) {
			super(hours);
		}
	}
	
	public static enum NativeCountryFeature implements DiscreteFeature<NativeCountryFeature>{
		UNITED_STATES,
		CAMBODIA,
		ENGLAND,
		PUERTO_RICO,
		CANADA,
		GERMANY,
		OUTLYING_US_GUAM_USVI_ETC_,
		INDIA,
		JAPAN,
		GREECE,
		SOUTH,
		CHINA,
		CUBA,
		IRAN,
		HONDURAS,
		PHILIPPINES,
		ITALY,
		POLAND,
		JAMAICA,
		VIETNAM,
		MEXICO,
		PORTUGAL,
		IRELAND,
		FRANCE,
		DOMINICAN_REPUBLIC,
		LAOS,
		ECUADOR,
		TAIWAN,
		HAITI,
		COLUMBIA,
		HUNGARY,
		GUATEMALA,
		NICARAGUA,
		SCOTLAND,
		THAILAND,
		YUGOSLAVIA,
		EL_SALVADOR,
		TRINADAD_TOBAGO,
		PERU,
		HONG,
		HOLAND_NETHERLANDS;

		@Override
		public NativeCountryFeature getValue() {
			return this;
		}
		
		@Override
		public boolean intersects(Feature<NativeCountryFeature> otherFeature) {
			return this == otherFeature;
		}
	}
}
