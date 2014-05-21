R_DATA = "/Users/haines/devsrc/Machine-Learning/ml-datasets/ml-dataset-wine/src/main/resources/";
colNames <- c("time", "SY/LG","SY/G","SY/AA","SY/Gh","SY/gCTl","SY/gCT","T30/1","P10/1","P10/2","P40/1","T70/2","PA2");

PREDICTION_DATA_LOCATION = paste(R_DATA, "prediction/", sep="");

loadPredictionInstance <- function(wineId){
  file <- list.files(TRAINING_DATA_LOCATION, paste("^",wineId,"_.._SAS.txt", sep=""), full.names=TRUE);
  
  instanceData <- read.table(file, quote="", header=F, comment.char="", sep="\t", stringsAsFactors=F);
    
  trainingInst <- trainingInstance();
  
  trainingInst@features <- instanceData;
  trainingInst@expectedOutput <- c();
  
  return (trainingInst);
}

loadPredictionSet <- function(){
  wineIds <- c("731", "734", "738", "744", "746", "754", "759", "768");
  
  return (lapply(wineIds, function(wineId){
    return (loadPredictionInstance(wineId));
  }));
}

n <- 30;
k <- 25;
lambda <- 3;

# first train network with all training instances
preprocessedTrainingInstances <- sapply(loadTrainingSet(), function(trainingInst){
  trainingInst@features <- (preProcessDataset(trainingInst@features, n));
  
  return (trainingInst);
});

network <- getTrainedRBFNetwork(preprocessedTrainingInstances, k, lambda, distanceMeasure, emptyPrototypesHandler, rbfFactory);

preprocessedInstances <- sapply(loadPredictionSet(), function(trainingInst){
  trainingInst@features <- (preProcessDataset(trainingInst@features, n));
  
  return (trainingInst);
});

predictions <- classifyInstance(preprocessedInstances, network);

predictions;