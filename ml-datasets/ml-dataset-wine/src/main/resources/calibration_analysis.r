require(ggplot2)
library(grid)
library(reshape2)  
library(lattice)

colNames <- c("time", "SY/LG","SY/G","SY/AA","SY/Gh","SY/gCTl","SY/gCT","T30/1","P10/1","P10/2","P40/1","T70/2","PA2");

# define out trainingSet class
trainingInstance <- setClass("trainingInstance", slots=c(features="numeric", expectedOutput="numeric"));

# 
averageDataSets <- function(dataset1, dataset2) {
  return ((dataset1 + dataset2) / 2);
}

describe_2 <- function(x) {
  c(mean=mean(x), median=median(x), IQR25=as.numeric(quantile(x, probs=c(0.25))), IQR75=as.numeric(quantile(x, probs=c(0.75))) )
}

#function to normalise an input vector between 0-1
norm <- function(colName, x, respectTo=x) {
  return ((x[[colName]] - min(x[[colName]], na.rm=TRUE)) / (max(x[[colName]], na.rm=TRUE) - min(x[[colName]], na.rm=TRUE)))
}

# takes a dataset and desamples it
deSampleDataset <- function(x, newFreq) {
  
  # determine current frequency in dataset
  currentFreq = x$time[2] - x$time[1];
  
  if (newFreq < currentFreq){
     stop(paste("the new frequency", newFreq, "cannot be less then the current frequency", currentFreq));
  }
  
  # work out how many samples we need to combine together
  newSampleWindow = newFreq / currentFreq;
  
  df <- x;
  
  #create an index property that groups every newSampleWindow samples together
  i <- seq(1,nrow(x), newSampleWindow)
  df$index <- rep(i, each=newSampleWindow)[1:nrow(df)]
  
  # now average each group and combine using aggregate function
  df <- aggregate(df, by=list(df$index), mean)
  
  # remove unwanted columns used to perform grouping. Only return the columns that were passed into function
  return (df[colnames(x)]);
}

normalise <- function(matrix, normalised=TRUE, withRespectTo=NULL){
  if (normalised == TRUE){
    
    timeRemoved = matrix[colnames(matrix)[2:ncol(matrix)]];
    
    if (is.null(withRespectTo)){
      withRespectTo <- timeRemoved;
    } else{ # remove time from withRespectTo
      withRespectTo <- withRespectTo[colnames(withRespectTo)[2:ncol(withRespectTo)]];
    }
    
    normalisedValues = as.data.frame(lapply(colnames(timeRemoved), norm, timeRemoved, withRespectTo));
    
    normalisedValues <- data.frame(matrix$"time", normalisedValues);
    
    colnames(normalisedValues) <- colnames(matrix);
    
    return (normalisedValues);
  }else { # otherwise disable normalisation
    return (matrix);
  }
}

plotSensors <- function(dataset, normalised=TRUE, withRespectTo=dataset) { 
  
  localEnv <- environment();
  yLabel = "Sensor Response";
  if (normalised == TRUE){
    yLabel = paste(yLabel, "Normalised");
  }
  
  df <- normalise(dataset, normalised, withRespectTo);
  df <- melt(df, id.vars="time");
  
  p <- ggplot(df, aes(x=time, y=value, color=variable, group=variable), environment = localEnv);
  p <- p + labs(title="Calibration Data Plots", dataset="Time", y=yLabel, x="Time");
  p <- p + theme(legend.position = "bottom");
  p <- p + guides(col = guide_legend(nrow = 2, byrow = TRUE, title="Sensor"));
  p <- p + geom_line();
  
  print(p);
}

truncateFeatures <- function(dataset, colsToKeep){
  return (dataset[colsToKeep])
}

na.rm<-function(x){  
  x[!is.na(x)]
}

l2Norm <- function(x1, x2){
  return (c(dist(rbind(x1, x2), method="euclidean")));
}

gaussianRbf <- function(distance, sigma){
  return (exp(- (distance^2 / (2 * sigma^2))));
}

gaussianRbfFactory <- function(sigma){
  return (function(distance){
    return (gaussianRbf(distance, sigma))
  });
}

getDistanceMatrix <- function(instances, prototypes, distanceMeasure=l2Norm){
  distances <- data.frame();
  
  for (i in seq(0, nrow(instances))){
    instance <- instances[i,];
    instanceDistances = vector();
    i <- 0;
    for (j in seq(0, nrow(prototypes))){
      prototype <- prototypes[j,];
      instanceDistances[i] <- distanceMeasure(instance, prototype);
      i <- i + 1;
    }
    distances <- rbind(distances, instanceDistances);
  }
  
  colnames(distances) <- rownames(prototypes);
  rownames(distances) <- rownames(instances);
  
  return (distances);
}

# simple empty prototype handler that just removes empty prototypes.
truncatingEmptyPrototypeHandler <- function(instances, k, distanceMeasure, prototypeHasMembershipMask, prototypes){
  return (prototypes[prototypeHasMembershipMask == TRUE]);
}

# this handler will recursively retry calculating the prototypes until every prototype has at least
# one instance member. Note that, for large k, this could be very computationally intensive especially
# if k ~ |instances|
recalculateEmptyPrototypeHandler <- function(instances, k, distanceMeasure, prototypeHasMembershipMask, prototypePositions){
  if (nrow(instances) > k){
    stop("You do not have enough instances to spread over k prototypes");
  }
  return (getKMeansPrototypes(instances, k, distanceMeasure, recalculateEmptyPrototypeHandler));
}

getKMeansPrototypes <- function(instances, k, distanceMeasure=l2Norm, emptyPrototypesHandler=truncatingEmptyPrototypeHandler){
  # First create k randomly assigned prototypes with dimensionality bounds 
  # defined by the instances.
  
  prototypePositions <- as.data.frame(apply(instances, 2, function(featureExamples){
    
    return (runif(k, min(featureExamples), max(featureExamples))); # for each feature, create a random value for each prototype between the min and max range of the provided feature examples for that feature.
  }));
  
  colnames(prototypePositions) <- colnames(instances); # name the dimensions to aid debugging
  
  lastPrototypeMembershipMask <- NULL;
  currentPrototypeMembershipMask <- NULL; # dummy values to ensure 1st iteration happens
  
  tryCatch({
    while((is.null(lastPrototypeMembershipMask) || is.null(currentPrototypeMembershipMask)) || !identical(lastPrototypeMembershipMask, currentPrototypeMembershipMask)){# only loop round if membership has changed
      lastPrototypeMembershipMask <- currentPrototypeMembershipMask;
      
      # compute the distance matrix between the instances and the prototypes
      distanceMatrix <- getDistanceMatrix(instances, prototypePositions, distanceMeasure);
      
      # now convert the distance matrix into an ownership binary mask by setting all
      # values for the instances row to FALSE if its not the minumum value and TRUE if it
      # is
      currentPrototypeMembershipMask <- as.data.frame(t(apply(distanceMatrix, 1, function(instanceDistances){
        return (t(c(instanceDistances == min(instanceDistances))));
      })));
      
      # now recompute the position of the prototypes based on the instances that are clustered
      # to it (ie using the instances that are set to TRUE under each prototype column).
      # The new positions of the prototypes will be the mean of all the feature values for all
      # the instances that belong to each prototype.
      
      newPrototypes <- as.data.frame(t(apply(currentPrototypeMembershipMask, 2, function(prototypeMemberShip){
        instancesInPrototype <- instances[prototypeMemberShip, ];
        if (nrow(instancesInPrototype) > 0){
          return (apply(instancesInPrototype, 2, function(featureOfInstances){
            return (mean(featureOfInstances));
          }));
        } else{ # if not we cannot work out mean.
          return (rep(NA, ncol(instances))); # we will replace these values with the old prototype values after we have calculated new values
        }
      })));
      
      colnames(newPrototypes) <- colnames(instances);
      
      row.names(newPrototypes) <- NULL;
      
      # any prototypes that were null (because they had no instances in them on this iteration),
      # assign the value from the previous prototype position (ie they dont move)
      newPrototypes[is.na(newPrototypes)] <- prototypePositions[is.na(newPrototypes)];
      
      prototypePositions <- newPrototypes;
    }
  }, error = function(err){
    print(err);
    
    print(lastPrototypeMembershipMask);
    print(currentPrototypeMembershipMask);
    
    return (err);
  });
  
  # create a prototype class.
  
  kprototype <- setClass("kprototype", slots=c(position="numeric", members="data.frame"));
  
  # now build more complex representation of prototypes containing their members
  prototypes <- apply(cbind(seq(1, nrow(prototypePositions)), prototypePositions), 1, function(prototypePosition){
    prototype <- kprototype();
    prototype@position <- prototypePosition[2:length(prototypePosition)];
    prototype@members <- instances[t(currentPrototypeMembershipMask[, prototypePosition[1]]),];
    return (prototype);
  });
  
  # TODO - might need to remove prototypes that havent got any instances attrbiuted to them
  # (ie they are still random points). We print a message here detailing if returned
  # clustered are still random.
  
  prototypeHasMembers <- apply(currentPrototypeMembershipMask, 2, function(prototype){
    return (any(prototype)); # for each of the prototypes, see if the ownership mask is set for any of the instances
  });
  
  if (!all(prototypeHasMembers)){ # if we have not got all prototypes containing members then display notification.
    print("INFO Not all prototypes have members. Removing empty prototypes");
    
    prototypes <- emptyPrototypesHandler(instances, k, distanceMeasure, prototypeHasMembers, prototypes);
  }
  
  return (prototypes);
}

computeRadialDistanceMatrix <- function(instances, networkModel, distanceMeasure=l2Norm){
  
  prototypePositions <- t(sapply(networkModel@prototypes, function(prototype){
    return (prototype@position);
  }));
  
  absoluteDistanceMatrix <- getDistanceMatrix(instances, prototypePositions, distanceMeasure);
  
  # now process the rbf for each distance
  
  radialDistanceMatrix <- apply(cbind(seq(1, ncol(absoluteDistanceMatrix)), t(absoluteDistanceMatrix)), 1, function(prototypeDistanceToInstances){
    prototypeNum <- prototypeDistanceToInstances[1];
    distances <- prototypeDistanceToInstances[2:length(prototypeDistanceToInstances)];
    
    return (networkModel@prototypes[[prototypeNum]]@rbf(distances));
  });
  
  return (radialDistanceMatrix);
}

getTrainedRBFNetwork <- function(trainingSet, k, lamba=0, distanceMeasure=l2Norm, emptyPrototypesHandler=truncatingEmptyPrototypeHandler, rbfFactory=gaussianRbfFactory){
  
  if (k >= length(trainingSet)){
    print("Warning. To avoid overfitting and increase generalisation. 
          The number of prototypes should be less then the number of 
          instances in the training set");
  }
  
  instances <- as.data.frame(t(sapply(trainingSet, function(trainingInstance){
    return (trainingInstance@features);
  })));
  
  expectedOutput <- t(sapply(trainingSet, function(trainingInstance){
    return (trainingInstance@expectedOutput);
  }));
  
  # generate k prototypes, clustered around our instances. 
  prototypes <- getKMeansPrototypes(instances, k, distanceMeasure, emptyPrototypesHandler);
  
  # now define the network state
  
  prototype <- setClass("prototype", slots=c(position="numeric", rbf="function"));
  
  network <- setClass("network", slots=c(prototypes="list", weights="matrix"));
  
  # calculate maxiumum distance between all prototypes and calculate sigma based
  # on sigma = d_max / sqrt(2 * k)
  
  prototypePositions <- t(sapply(prototypes, function(prototype){
    return (prototype@position);
  }));
  
  prototypeDistances <- getDistanceMatrix(prototypePositions, prototypePositions, distanceMeasure);
  
  maxProtoDistance <- max(prototypeDistances);
  
  maximumSigma <- maxProtoDistance / sqrt(2 * k);
  
  networkModel <- network();
  
  networkModel@prototypes <- lapply(prototypes, function(kprototype){ # for each prototype position, difine the rbf for it
    
    prototype <- prototype();
    
    prototype@position <- kprototype@position;
     
     # calculate the sigma of the guassian (radial shape) based on the data contained 
     # in this prototype cluster. Note that with few data points this might heavily skew the
     # RBF to outliers if the member size is low.
     
     if (nrow(kprototype@members) > 50){ # only use covariance matrix as the sigma if we have enough members
        sigma <- cov(kprototype@members); 
     } else{
       # use constant sigma
       
       sigma <- maximumSigma;
     }
     
     prototype@rbf <- rbfFactory(sigma);
    
    return (prototype);
   });
  
  
  # now we have our functions set up, lets learn the weights of the network based on the
  # input data...
  
  radialDistanceMatrix <- computeRadialDistanceMatrix(instances, networkModel, distanceMeasure);
  
  # using the poggio & girosi error function to inverse and apply a penalty term.
  # this could be simply replaced with the pseudo inverse * expectedOutput as
  # inv(t(M) * M)) * M is the pseudo inverse. As i want to use the penalty
  # term, i explicity define the pseudo inverse here.
  
  sqDistanceMatrix <- t(radialDistanceMatrix) %*% radialDistanceMatrix;
  
  errorPenalty <- lamba * diag(nrow(sqDistanceMatrix));
  
  networkModel@weights <- solve(sqDistanceMatrix + errorPenalty) %*% t(radialDistanceMatrix) %*% expectedOutput;
  
  return (networkModel);
}

# This function will take an instance and return a classification based on the provided
# network
classifyInstance <- function(instance, network, distanceMeasure=l2Norm){
  
  radialDistanceMatrix <- computeRadialDistanceMatrix(as.data.frame(instance), network, distanceMeasure);
  
  return (radialDistanceMatrix %*% network@weights);
}

trainAndgetCrossValidationErrorRate <- function(trainingSet, k=10, nCrossValidations=10, lamba=0, distanceMeasure=l2Norm, emptyPrototypesHandler=truncatingEmptyPrototypeHandler, rbfFactory=gaussianRbfFactory){
   
  if (length(trainingSet) < nCrossValidations){
    stop(paste("there are not enough instances in the training set to seperate the data into", nCrossValidations, " cross validations"));
  }
  # first shuffle the training set so buckets are randomly assigned
  
  trainingSet <- trainingSet[sample(length(trainingSet))];
  
  # now define buckets or bins for each of the n fold tests
  
  bucketSize <- floor(length(trainingSet) / nCrossValidations);
  
  trainingSetSize <- min(bucketSize * (nCrossValidations-1), length(trainingSet) - bucketSize);
  
  if (nCrossValidations == 1){ # just want a single test. Special case. Used for testing
    bucketSize <- 1;
    trainingSetSize <- length(trainingSet) - bucketSize;
  } 
  
  # create a mask where FALSE represents test set data, and TRUE represents training set data.
  # each time a test of training->test is performed, the mask is rotated so the test and training
  # set buckets are all considered
  
  bucketMask <- rep(FALSE, bucketSize);
  bucketMask <- c(bucketMask, rep(TRUE, trainingSetSize));
  
  validationNumber = 0;
                                      
  minError <- NULL;
  maxError <- NULL;
  overalTotalError <- 0;
  
  while(validationNumber != nCrossValidations){
    
    foldTrainingSet <- trainingSet[bucketMask];
    foldTestSet <- trainingSet[!bucketMask];
    
    network <- getTrainedRBFNetwork(foldTrainingSet, k, lamba, distanceMeasure, emptyPrototypesHandler, rbfFactory);
    
    testInstances <- as.data.frame(t(sapply(foldTestSet, function(trainingInst){
      return (trainingInst@features);
    })));
    predictions <- classifyInstance(testInstances, network);
    
    # work out the summed squared errors between the predictions and the expected results
    
    expectedOutputs <- t(sapply(foldTestSet, function(trainingInst){
      return (trainingInst@expectedOutput);
    }));
    
    errors <- (predictions - expectedOutputs)^2;
    
    totalError <- sum(errors);
    
    if (is.null(minError) || minError > totalError){
      minError <- totalError;
    }
    
    if (is.null(maxError) || maxError < totalError){
      maxError <- totalError;
    }
    
    overalTotalError <- overalTotalError + totalError;
    
    bucketMask <- rotateMask(bucketMask, bucketSize);
    
    validationNumber <- validationNumber + 1;
  }
  
  print(paste("average error is:", (overalTotalError / nCrossValidations)));
  print(paste("max error is:", maxError));
  print(paste("min error is:", minError));
}

rotateMask <- function(bucketMask, bucketSize){
  
  bucketMask <- c(bucketMask[bucketSize:length(bucketMask)], bucketMask[0:(bucketSize-1)]);
  
  return (bucketMask);
}

smoothDataset <- function(dataset) {
  size = dim(dataset)[2];
  smoothedDataset = dataset[1];
  colNames = c("time");
  
  colnames(smoothedDataset) <- colNames;
  for (col in 2:size){
    smoothedCol = ksmooth(t(dataset[1]), t(dataset[col]), "box", bandwidth=20);
    
    colNames = append(colNames, colnames(dataset)[col]);
                          
    smoothedDataset = merge.data.frame(smoothedDataset, smoothedCol, by.x="time", by.y="x", all=T);
    
    colnames(smoothedDataset) <- colNames;
  }
  
  return (smoothedDataset);
}

#This method takes 2 datsets, and subtracts a smoothed version of their results.
subtractSensorReadings <- function(rawDataset1, rawDataset2) {
  smoothedDatasetDifference = smoothDataset(rawDataset1)[2:dim(rawDataset1)[2]] - smoothDataset(rawDataset2)[2:dim(rawDataset2)[2]];
  
  # now add back in the time column
  
  return (cbind(rawDataset1[1], smoothedDatasetDifference));
}

getCummativeSquaresValues <- function(reading){
  timeRemoved <- reading[2:dim(reading)[2]];
  
  cumsums <- apply(timeRemoved, 2, function(x){
    
    return (cumsum(x^2));
  });
  
  return (cbind(reading[1], cumsums));
}

R_DATA = "/Users/haines/devsrc/Machine-Learning/ml-datasets/ml-dataset-wine/src/main/resources/";

SEP <- "\t";

# load all datasets
calibration_baseline_Data1 <- read.table(paste(R_DATA, "calibration/", "4EG_1_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
calibration_baseline_Data2 <- read.table(paste(R_DATA, "calibration/", "4EG_2_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
calibration_baseline_Data3 <- read.table(paste(R_DATA, "calibration/", "4EP_1_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
calibration_baseline_Data4 <- read.table(paste(R_DATA, "calibration/", "4EP_2_0ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EGcalibration_baseline_Data1 <- read.table(paste(R_DATA, "calibration/", "4EG_1_25ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EGcalibration_baseline_Data2 <- read.table(paste(R_DATA, "calibration/", "4EG_2_25ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EGcalibration_baseline_Data3 <- read.table(paste(R_DATA, "calibration/", "4EG_1_100ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EGcalibration_baseline_Data4 <- read.table(paste(R_DATA, "calibration/", "4EG_2_100ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EGcalibration_baseline_Data5 <- read.table(paste(R_DATA, "calibration/", "4EG_1_200ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EGcalibration_baseline_Data6 <- read.table(paste(R_DATA, "calibration/", "4EG_2_200ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)

EPcalibration_baseline_Data1 <- read.table(paste(R_DATA, "calibration/", "4EP_1_40ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EPcalibration_baseline_Data2 <- read.table(paste(R_DATA, "calibration/", "4EP_2_40ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EPcalibration_baseline_Data3 <- read.table(paste(R_DATA, "calibration/", "4EP_1_80ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EPcalibration_baseline_Data4 <- read.table(paste(R_DATA, "calibration/", "4EP_2_80ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EPcalibration_baseline_Data5 <- read.table(paste(R_DATA, "calibration/", "4EP_1_120ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)
EPcalibration_baseline_Data6 <- read.table(paste(R_DATA, "calibration/", "4EP_2_120ppb.txt", sep=""), quote="", header=F, comment.char="", sep=SEP, stringsAsFactors=F)

# add column names to aid in readability
colnames(calibration_baseline_Data1) <- colNames;
colnames(calibration_baseline_Data2) <- colNames;
colnames(calibration_baseline_Data3) <- colNames;
colnames(calibration_baseline_Data4) <- colNames;
colnames(EGcalibration_baseline_Data1) <- colNames;
colnames(EGcalibration_baseline_Data2) <- colNames;
colnames(EGcalibration_baseline_Data3) <- colNames;
colnames(EGcalibration_baseline_Data4) <- colNames;
colnames(EGcalibration_baseline_Data5) <- colNames;
colnames(EGcalibration_baseline_Data6) <- colNames;
colnames(EPcalibration_baseline_Data1) <- colNames;
colnames(EPcalibration_baseline_Data2) <- colNames;
colnames(EPcalibration_baseline_Data3) <- colNames;
colnames(EPcalibration_baseline_Data4) <- colNames;
colnames(EPcalibration_baseline_Data5) <- colNames;
colnames(EPcalibration_baseline_Data6) <- colNames;

# take the 2 trials of each dataset and average them.

avBaselineDatasetEG = averageDataSets(calibration_baseline_Data1, calibration_baseline_Data2);
av200EG = averageDataSets(EGcalibration_baseline_Data5, EGcalibration_baseline_Data6);
av100EG = averageDataSets(EGcalibration_baseline_Data3, EGcalibration_baseline_Data4);
av25EG = averageDataSets(EGcalibration_baseline_Data1, EGcalibration_baseline_Data2);

avBaselineDatasetEP = averageDataSets(calibration_baseline_Data3, calibration_baseline_Data4);
av120EP = averageDataSets(EPcalibration_baseline_Data5, EPcalibration_baseline_Data6);
av80EP = averageDataSets(EPcalibration_baseline_Data3, EPcalibration_baseline_Data4);
av40EP = averageDataSets(EPcalibration_baseline_Data1, EPcalibration_baseline_Data2);

# normalise all datasets to 0-1

# subtract the measured EG readings with that of the baselines to see which differs.
# Note that this function also smoothes the table using kernal smoothing 
egDifference200 = subtractSensorReadings(av200EG, avBaselineDatasetEG);
egDifference100 = subtractSensorReadings(av100EG, avBaselineDatasetEG);
egDifference25 = subtractSensorReadings(av25EG, avBaselineDatasetEG);
epDifference120 = subtractSensorReadings(av120EP, avBaselineDatasetEP);
epDifference80 = subtractSensorReadings(av80EP, avBaselineDatasetEP);
epDifference40 = subtractSensorReadings(av40EP, avBaselineDatasetEP);

# Plot sensors

# baseline
plotSensors(avBaselineDatasetEG, TRUE); # T70/2, T30/1, PA2
plotSensors(avBaselineDatasetEP, TRUE); # T70/2, T30/1, PA2

# EG difference
plotSensors(egDifference25, FALSE); # T70/2, T30/1, PA2
plotSensors(egDifference100, FALSE); # T70/2, T30/1, PA2
plotSensors(egDifference200, FALSE); # T70/2, T30/1, PA2

# EP difference
plotSensors(epDifference40, FALSE); # why is this the same as 120????
plotSensors(epDifference80, FALSE); # 
plotSensors(epDifference120, FALSE); # T70/2, T30/1, PA2

# these are not normalised... Lets try that next.

avBaselineDatasetEG = normalise(avBaselineDatasetEG);
av200EG = normalise(av200EG);
av100EG = normalise(av100EG);
av25EG = normalise(av25EG);

avBaselineDatasetEP = normalise(avBaselineDatasetEP);
av120EP = normalise(av120EP);
av80EP = normalise(av80EP);
av40EP = normalise(av40EP);

egDifference200 = subtractSensorReadings(av200EG, avBaselineDatasetEG);
egDifference100 = subtractSensorReadings(av100EG, avBaselineDatasetEG);
egDifference25 = subtractSensorReadings(av25EG, avBaselineDatasetEG);
epDifference120 = subtractSensorReadings(av120EP, avBaselineDatasetEP);
epDifference80 = subtractSensorReadings(av80EP, avBaselineDatasetEP);
epDifference40 = subtractSensorReadings(av40EP, avBaselineDatasetEP);

plotSensors(epDifference120, FALSE); # T70/2, T30/1, PA2
plotSensors(egDifference200, FALSE); # T70/2, T30/1, PA2

# look at cummative sum of squares to see which sensors have the largest overall reaction to
# the presence of analyts

plotSensors(getCummativeSquaresValues(egDifference200), FALSE);

plotSensors(getCummativeSquaresValues(epDifference120), FALSE);

# no obvious signal from sensors. Lets try PCA analysis

plotPrincipleComponents <- function(dataset){
  
  timeRemoved = dataset[2:dim(dataset)[2]];;
  
  wine.prcEG200 <- prcomp(timeRemoved, center=TRUE, scale=TRUE);

  # look at the eigen values (are the same as the varience of each PC so the square of the 
  # standard deviation). 
  mainPcs = colnames(wine.prcEG200$x)[wine.prcEG200$sdev ^ 2 > 1]; # kaiser criteria. Keep PC's for eigen values greater than 1
  print(paste('Main principle components', paste(mainPcs, collapse=',')));

  screeplot(wine.prcEG200, main="Scree Plot", type="lines", npcs=length(wine.prcEG200$sdev))
  
  # now lets look at what sensors contribute most to the located PC's

  
}




screeplot(wine.prcEG200, main="Scree Plot", xlab="Components")
screeplot(wine.prcEG200, main="Scree Plot", type="lines", npcs=length(wine.prcEG200$sdev))

load    <- wine.prcEG200$rotation
sorted.loadings <- load[order(load[, 1]), 1]
myTitle <- "Loadings Plot for PC1" 
myXlab  <- "Variable Loadings"
dotplot(sorted.loadings, main=myTitle, xlab=myXlab, cex=1.5, col="red")

biplot(wine.prcEG200, cex=c(1, 0.7))

my.var <- varimax(wine.prcEG200$rotation)

plotSensors(deSampleDataset(egDifference, 10), FALSE);
plotSensors(deSampleDataset(epDifference120, 10), FALSE);

nrow(egDifference);
nrow(deSampleDataset(egDifference, 10));




plotSensors(truncateFeatures(egDifference, c("time", "T70/2", "T30/1", "PA2")), FALSE)
plotSensors(truncateFeatures(epDifference, c("time", "T70/2", "T30/1", "PA2")), FALSE)

