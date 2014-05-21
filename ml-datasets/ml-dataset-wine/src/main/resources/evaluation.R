# This file is responsible for evaluating the best possible set of free parameters for the model

trainingData <- loadTrainingSet()

evaluate1 <- function(trainingData){
  bestErrorRate <- NULL;
  bestK <- NULL;
  bestN <- NULL;
  bestLambda <- NULL;
  lapply(c(5, 10, 15, 20, 25, 30), function(k){
    lapply (c(10, 20, 30, 40, 50, 60), function(n){
      lapply (c(0, 0.5, 1, 2, 4, 8, 16, 32, 64, 128), function(lambda){
        
        print(paste("Considering k=",k,"desamplingsize=",n,"lambda",lambda));
        errorRate <- preProcessAndEvaluateNetwork(trainingData, k, n, lambda);
        print(paste("error rate calculated as",errorRate));
        if (is.null(bestErrorRate) || bestErrorRate > errorRate){
          bestErrorRate <- errorRate;
          bestK <- k;
          bestN <- n;
          bestLambda <- lambda;
        }
      });
    });
  });
  print(paste("bestK=",bestK," bestN=", bestN, " bestLambda=", bestLambda, sep=""));
}

evaluate2 <- function(trainingData){
  bestErrorRate <- NULL;
  bestK <- NULL;
  bestN <- NULL;
  bestLambda <- NULL;
  
#   lapply(c(5, 10, 15, 20, 25, 30, 35), function(k){
#     print(paste("Considering k=",k,"desamplingsize=",20,"lambda",0));
#     errorRate <- preProcessAndEvaluateNetwork(trainingData, k, 20, 0);
#     if (is.null(bestErrorRate) || bestErrorRate > errorRate){
#       bestErrorRate <- errorRate;
#       bestK <- k;
#     }
#   });
#   bestErrorRate <- NULL;
#   lapply (c(10, 20, 30, 40, 50, 60), function(n){
#     print(paste("Considering k=",bestK,"desamplingsize=",n,"lambda",0));
#     errorRate <- preProcessAndEvaluateNetwork(trainingData, 25, n, 0);
#     if (is.null(bestErrorRate) || bestErrorRate > errorRate){
#       bestErrorRate <- errorRate;
#       bestN <- n;
#     }
#   });
  bestErrorRate <- NULL;
  lapply (c(0, 0.5, 1, 2, 4, 8, 16, 32, 64, 128), function(lambda){
    print(paste("Considering k=",25,"desamplingsize=",20,"lambda",lambda));
    errorRate <- preProcessAndEvaluateNetwork(trainingData, 25, 20, lambda);
    if (is.null(bestErrorRate) || bestErrorRate > errorRate){
      bestErrorRate <- errorRate;
      bestLambda <- lambda;
    }
  });
  
  print(paste("bestK=",bestK," bestN=", bestN, " bestLambda=", bestLambda, sep=""));
}