
R_DATA = "/Users/haines/devsrc/Machine-Learning/ml-datasets/ml-dataset-wine/src/main/resources/";
colNames <- c("time", "SY/LG","SY/G","SY/AA","SY/Gh","SY/gCTl","SY/gCT","T30/1","P10/1","P10/2","P40/1","T70/2","PA2");
TRAINING_DATA_LOCATION = paste(R_DATA, "training/", sep="");
CALIBRATION_DATA_LOCATION = paste(R_DATA, "calibration/", sep="");
EXPECTED_VALUES = read.table(paste(TRAINING_DATA_LOCATION, "true_values.dat", sep=""), quote="", header=F, comment.char="", sep=" ", stringsAsFactors=F);

loadedData <- setClass("loadedData", slots=c(features="data.frame", expectedOutput="numeric"));


calibration_baseline_Data1 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EG_1_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
calibration_baseline_Data2 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EG_2_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
calibration_baseline_Data3 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EP_1_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
calibration_baseline_Data4 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EP_2_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
EGcalibration_baseline_Data5 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EG_1_200ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
EGcalibration_baseline_Data6 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EG_2_200ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
EPcalibration_baseline_Data5 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EP_1_120ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)
EPcalibration_baseline_Data6 <- read.table(paste(CALIBRATION_DATA_LOCATION, "4EP_2_120ppb.txt", sep=""), quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F)

colnames(calibration_baseline_Data1) <- colNames;
colnames(calibration_baseline_Data2) <- colNames;
colnames(calibration_baseline_Data3) <- colNames;
colnames(calibration_baseline_Data4) <- colNames;

colnames(EGcalibration_baseline_Data5) <- colNames;
colnames(EGcalibration_baseline_Data6) <- colNames;
colnames(EPcalibration_baseline_Data5) <- colNames;
colnames(EPcalibration_baseline_Data6) <- colNames;

BASELINE_EG <- averageDataSets(calibration_baseline_Data1, calibration_baseline_Data2);
BASELINE_EP <-averageDataSets(calibration_baseline_Data3, calibration_baseline_Data4);

BASELINE <- averageDataSets(BASELINE_EG, BASELINE_EP);

EG_CALIBRATIONS <- subtractSensorReadings(averageDataSets(EGcalibration_baseline_Data5, EGcalibration_baseline_Data6), BASELINE_EG);
EP_CALIBRATIONS <- subtractSensorReadings(averageDataSets(EPcalibration_baseline_Data5, EPcalibration_baseline_Data6), BASELINE_EP);

getPCALoadings <- function(calibrationDataset, desampleFreq){
  
  calibrationDataset <- deSampleDataset(calibrationDataset, desampleFreq);
  
  timeRemoved = calibrationDataset[2:dim(calibrationDataset)[2]];
  
  pca <- prcomp(timeRemoved, center=TRUE, scale=TRUE);
  
  # print(paste("primary PCs", paste(c(colnames(pca$x)[pca$sdev ^ 2 > 1]), collapse=', '))); # kaiser criteria. Keep PC's for eigen values greater than 1
  
  return (pca$rotation);
}


preProcessDataset <- function(dataset, desampleFreq){
   
  originalDimensionality <- prod(dim(dataset));
  
  # First smooth the dataset
  
  dataset <- smoothDataset(dataset);
  
  # Next subtract the dataset from the averaged baseline
  
  dataset <- subtractSensorReadings(dataset, BASELINE);
  
  # desample dataset
  
  dataset <- deSampleDataset(dataset, desampleFreq);
  
  # calculate the total loadings from both 4EG and 4EP calibration sets
  
  Loadings4EG <- getPCALoadings(EG_CALIBRATIONS, desampleFreq);
  Loadings4EP <- getPCALoadings(EP_CALIBRATIONS, desampleFreq);
  
  # now project our dataset onto these new loadings (rotate data onto new feature space)
  
  timeRemoved <- as.matrix(dataset[2:dim(dataset)[2]]);
  
  datasetEG <- as.data.frame(timeRemoved %*% Loadings4EG);
  datasetEP <- as.data.frame(timeRemoved %*% Loadings4EP);
  
  colnames(datasetEG) <- colnames(Loadings4EG);
  colnames(datasetEP) <- colnames(Loadings4EP);
  
  # now strip out all components except for PC1 and PC2 for EG
  
  processedDataset <- truncateFeatures(datasetEG, c("PC1", "PC2"));
  processedDataset <- cbind(processedDataset, truncateFeatures(datasetEP, c("PC1")));
  
  # now convert dataframe into a single vector of the data.

  processedDataset <- c(processedDataset, recursive=TRUE);
  #print(paste("preprocessed dataset from", originalDimensionality, "dimensions into ", length(processedDataset), "new dimensions"));
  
  return (processedDataset);
}

trainingInstance <- setClass("trainingInstance", slots=c(features="numeric", expectedOutput="numeric"));

loadTrainingInstance <- function(wineId){
  file <- list.files(TRAINING_DATA_LOCATION, paste("^",wineId,"_.._SAS.txt", sep=""), full.names=TRUE);
  
  instanceData <- read.table(file, quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F);
  
  expectedOutput <- EXPECTED_VALUES[EXPECTED_VALUES[1]==wineId];
  
  trainingInst <- loadedData();
  
  trainingInst@features <- instanceData;
  trainingInst@expectedOutput <- expectedOutput[2:3];
  
  return (trainingInst);
}

loadTrainingSet <- function(){
  wineIds <- c("725","726",
               "727","728",
               "729","730",
               "732","733",
               "735","736",
               "737","739",
               "740","741",            
               "742","743",              
               "745","747",             
               "748","749",
               "750","751",
               "752","753",
               "755","756", 
               "757","758",
               "760","761",
               "762","763",
               "764","765",
               "766","767",
               "769","770");
  
  return (lapply(wineIds, function(wineId){
    return (loadTrainingInstance(wineId));
  }));
}

preProcessAndEvaluateNetwork <- function(trainingInstances, k, n, lambda){
  preprocessedInstances <- sapply(trainingInstances, function(trainingInst){
    processedInstace <- trainingInstance();
    processedInstace@features <- (preProcessDataset(trainingInst@features, n));
    processedInstace@expectedOutput <- trainingInst@expectedOutput;
    return (processedInstace);
  });
  
  print(paste("finished preprocessing", length(preprocessedInstances), "instances. Training network"));
  # use 15 cross fold
  return (trainAndgetCrossValidationErrorRate(preprocessedInstances, k, 18, lambda, l2Norm, truncatingEmptyPrototypeHandler, gaussianRbfFactory));
}