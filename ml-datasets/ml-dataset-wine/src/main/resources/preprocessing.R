
R_DATA = "/Users/haines/devsrc/Machine-Learning/ml-datasets/ml-dataset-wine/src/main/resources/";
TRAINING_DATA_LOCATION = paste(R_DATA, "training/", sep="");
CALIBRATION_DATA_LOCATION = paste(R_DATA, "calibration/", sep="");
EXPECTED_VALUES = read.table(paste(TRAINING_DATA_LOCATION, "true_values.dat", sep=""), quote="", header=F, comment.char="", sep=" ", stringsAsFactors=F);

calibration_baseline_Data1 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EG_1_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
calibration_baseline_Data2 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EG_2_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
calibration_baseline_Data3 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EP_1_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
calibration_baseline_Data4 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EP_2_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)

BASELINE <- smoothDataset(averageDataSets(averageDataSets(calibration_baseline_Data1, calibration_baseline_Data2),
                           averageDataSets(calibration_baseline_Data3, calibration_baseline_Data4)));

PCA_LOADINGS <- getPCALoadings();

getPCALoadings <- function(){
  
}


preProcessDataset <- function(dataset){
   
  
  # First smooth the dataset
  
  dataset <- smoothDataset(dataset);
  
  # Next subtract the dataset from the averaged baseline
  
  dataset <- subtractSensorReadings(BASELINE, dataset);
  
  
  
  return (dataset);
}

loadTrainingSet <- function(wineId){
  file <- list.files(TRAINING_DATA_LOCATION, paste("^",wineId,"_.._SAS.txt", sep=""), full.names=TRUE);
  
  instanceData <- read.table(file, quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F);
  
  expectedOutput <- EXPECTED_VALUES[EXPECTED_VALUES[1]==wineId];
  
  trainingInst <- trainingInstance();
  
  trainingInst@features <- instanceData;
  trainingInst@expectedOutput <- expectedOutput[2:3];
  
  return (trainingInst);
}