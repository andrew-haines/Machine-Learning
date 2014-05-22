R_DATA = "/Users/haines/devsrc/Machine-Learning/ml-datasets/ml-dataset-wine/src/main/resources/";
colNames <- c("time", "SY/LG","SY/G","SY/AA","SY/Gh","SY/gCTl","SY/gCT","T30/1","P10/1","P10/2","P40/1","T70/2","PA2");

PREDICTION_DATA_LOCATION = paste(R_DATA, "prediction/", sep="");

predictionLoadedData <- setClass("predictionLoadedData", slots=c(features="data.frame"));

loadPredictionInstance <- function(wineId){
  file <- list.files(PREDICTION_DATA_LOCATION, paste("^",wineId,"_.._PRED.txt", sep=""), full.names=TRUE);
  
  instanceData <- read.table(file, quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F);
    
  trainingInst <- predictionLoadedData();
  
  trainingInst@features <- instanceData;
  
  return (trainingInst);
}

loadPredictionSet <- function(){
  wineIds <- c("731", "734", "738", "744", "746", "754", "759", "768");
  
  return (lapply(wineIds, function(wineId){
    return (loadPredictionInstance(wineId));
  }));
}

n <- 30;
k <- 50; # as the network fails to find more then 7 prototypes, we set this value here.
lambda <- 1;

# first train network with all training instances
preprocessedTrainingInstances <- sapply(loadTrainingSet(), function(trainingInst){
  
  processedInstace <- trainingInstance();
  processedInstace@features <- (preProcessDataset(trainingInst@features, n));
  processedInstace@expectedOutput <- trainingInst@expectedOutput;
  return (processedInstace);
});

network <- getTrainedRBFNetwork(preprocessedTrainingInstances, k, lambda, l2Norm, truncatingEmptyPrototypeHandler, gaussianRbfFactory);

preprocessedInstances <- sapply(loadPredictionSet(), function(trainingInst){
  return (preProcessDataset(trainingInst@features, n));
});

predictions <- classifyInstance(as.data.frame(t(preprocessedInstances)), network);

predictions;
