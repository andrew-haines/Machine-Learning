require(ggplot2)
library(grid)
library(reshape2)  
library(lattice)

colNames <- c("time", "SY/LG","SY/G","SY/AA","SY/Gh","SY/gCTl","SY/gCT","T30/1","P10/1","P10/2","P40/1","T70/2","PA2");

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
    return (guassianRbf(distance, sigma))
  });
}

getDistanceMatrix <- function(instances, prototypes, distanceMeasure=l2Norm){
  return (as.data.frame(apply(prototypes, 1, function(prototype){
      return (apply(instances, 1, function(instance){
        return (distanceMeasure(prototype, instance));
      }));
  })));
}

getKMeansPrototypes <- function(instances, k, distanceMeasure=l2Norm){
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
  
  # TODO - might need to remove prototypes that havent got any instances attrbiuted to them
  # (ie they are still random points). We print a message here detailing if returned
  # clustered are still random.
  
  prototypeHasMembers <- apply(currentPrototypeMembershipMask, 2, function(prototype){
    return (any(prototype)); # for each of the prototypes, see if the ownership mask is set for any of the instances
  });
  
  if (!all(prototypeHasMembers)){ # if we have not got all prototypes containing members then display notification.
    print("Warning! Not all prototypes have members");
  }
  
  # create a prototype class.
  
  kprototype <- setClass("kprototype", slots=c(position="numeric", members="data.frame"));
  
  # now build more complex representation of prototypes containing their members
  prototypes <- apply(cbind(seq(1, nrow(prototypePositions)), prototypePositions), 1, function(prototypePosition){
    prototype <- kprototype();
    prototype@position <- prototypePosition[2:length(prototypePosition)];
    prototype@members <- instances[t(currentPrototypeMembershipMask[, prototypePosition[1]]),];
    return (prototype);
  });
  return (prototypes);
}

getTrainedRBFNetwork <- function(instances, classes, k, lamba, distanceMeasure=l2Norm, rbfFactory=gaussianRbfFactory){
  
  if (k >= nrows(instances)){
    print("Warning. To avoid overfitting and increase generalisation. 
          The number of prototypes should be less then the number of 
          instances in the training set");
  }
  
  # generate k prototypes, clustered around our instances. 
  prototypes <- getKMeansPrototypes(instances, k, distanceMeasure);
  
  # now define the network state
  
  prototype <- setClass("prototype", slots=c(position="numeric", rbf="closure"));
  
  network <- setClass("network", slots=c(prototypes="prototype", weights="numeric", usingBias="boolean"));
  
  # calculate maxiumum distance between all prototypes and calculate sigma based
  # on sigma = d_max / sqrt(2 * k)
  
  prototypePositions <- apply(prototypes, 1, function(prototype){
    return (prototype@position);
  });
  
  prototypeDistances <- getDistanceMatrix(prototypePositions, prototypePositions, distanceMeasure);
  
  maxProtoDistance <- max(prototypeDistances[1,]); # as the matrix is diaganol (prototype->prototype comparison), just looking at the largest value of one row will do.
  
  maximumSigma <- maxProtoDistance / sqrt(2 * k);
  
  networkModel@prototypes <- apply(prototypes, 1, function(prototype){ # for each prototype position, difine the rbf for it
     prototype@position <- prototypePosition;
     
     # calculate the sigma of the guassian (radial shape) based on the data contained 
     # in this prototype cluster. Note that with few data points this might heavily skew the
     # RBF to outliers if the member size is low.
     
     if (nrow(prototype@members) > 50){ # only use covariance matrix as the sigma if we have enough members
        sigma <- cov(prototype@members); 
     } else{
       # use constant sigma
       
       sigma <- maximumSigma;
     }
     
     prototype@rbf <- rbfFactory(sigma);
   });
  
  
  # now we have our functions set up, lets learn the weights of the network based on the
  # input data...
  
  distanceMatrix <- getDistanceMatrix(prototypePositions, instances, distanceMeasure);
  
  # now process the rbf for each distance
}
# 
# trainAndgetCrossValidationErrorRate <- function(network, trainingSet, k=10){
#   
#   
#   
# }



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

