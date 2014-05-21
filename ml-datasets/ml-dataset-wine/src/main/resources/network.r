
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
  
  # Cant find a nice 'R' way of doing the following that works with both matrixs and vectors.
  # This will do for now...
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
  if (nrow(instances) < k){
    stop(paste("You do not have enough instances ",nrow(instances)," to spread over k=",k," prototypes", sep=""));
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
    #print("INFO Not all prototypes have members. Removing empty prototypes");
    
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

getTrainedRBFNetwork <- function(trainingSet, k, lambda=0, distanceMeasure=l2Norm, emptyPrototypesHandler=truncatingEmptyPrototypeHandler, rbfFactory=gaussianRbfFactory){
  
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
  
  errorPenalty <- lambda * diag(nrow(sqDistanceMatrix));
  
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
  
  if (nCrossValidations == 1){ # hold out 1 cross validation. used for simple tests in debuging the procedure.
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
  
  averageErrorRate <- overalTotalError / nCrossValidations;
  
  print(paste("average error is:", (averageErrorRate)));
  print(paste("max error is:", maxError));
  print(paste("min error is:", minError));
  
  return (averageErrorRate);
}

rotateMask <- function(bucketMask, bucketSize){
  
  bucketMask <- c(bucketMask[bucketSize:length(bucketMask)], bucketMask[0:(bucketSize-1)]);
  
  return (bucketMask);
}