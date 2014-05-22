# This file is responsible for evaluating the best possible set of free parameters for the model

trainingData <- loadTrainingSet()

evaluate1 <- function(trainingData){
  bestErrorRate <- NULL;
  bestK <- NULL;
  bestN <- NULL;
  bestLambda <- NULL;
  
  for (k in c(5, 10, 15, 20, 25, 30)){
    for (n in c(10, 20, 30, 40, 50, 60)){
      for (lambda in c(0, 0.5, 1, 2, 4, 8, 16, 32, 64, 128)){
        print(paste("Considering k=",k,"desamplingsize=",n,"lambda",lambda));
        errorRate <- preProcessAndEvaluateNetwork(trainingData, k, n, lambda);
        print(paste("error rate calculated as",errorRate));
        if (is.null(bestErrorRate) || bestErrorRate > errorRate){
          bestErrorRate <- errorRate;
          bestK <- k;
          bestN <- n;
          bestLambda <- lambda;
        }
      }
    }
  }
  print(paste("bestK=",bestK," bestN=", bestN, " bestLambda=", bestLambda, sep=""));
}

evaluate2 <- function(trainingData){
  bestErrorRate <- NULL;
  bestK <- NULL;
  bestN <- NULL;
  bestLambda <- NULL;
  
  for (k in c(5, 10, 15, 20, 25, 30)){
    print(paste("Considering k=",k,"desamplingsize=",20,"lambda",0));
    errorRate <- preProcessAndEvaluateNetwork(trainingData, k, 20, 0);
    if (is.null(bestErrorRate) || bestErrorRate > errorRate){
      bestErrorRate <- errorRate;
      bestK <- k;
    }
  };
  bestErrorRate <- NULL;
  for (n in c(10, 20, 30, 40, 50, 60)){
    print(paste("Considering k=",bestK,"desamplingsize=",n,"lambda",0));
    errorRate <- preProcessAndEvaluateNetwork(trainingData, 25, n, 0);
    if (is.null(bestErrorRate) || bestErrorRate > errorRate){
      bestErrorRate <- errorRate;
      bestN <- n;
    }
  }
  bestErrorRate <- NULL;
  for (lambda in c(0, 0.5, 1, 2, 4, 8, 16, 32, 64, 128)) {
    print(paste("Considering k=",25,"desamplingsize=",20,"lambda",lambda));
    errorRate <- preProcessAndEvaluateNetwork(trainingData, 25, 20, lambda);
    if (is.null(bestErrorRate) || bestErrorRate > errorRate){
      bestErrorRate <- errorRate;
      bestLambda <- lambda;
    }
  };
  
  print(paste("bestK=",bestK," bestN=", bestN, " bestLambda=", bestLambda, sep=""));
}