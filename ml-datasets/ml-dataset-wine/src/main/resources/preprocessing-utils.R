require(gridExtra)
require(ggplot2)
library(grid)
library(reshape2)  
library(lattice)

# define our trainingSet class
trainingInstance <- setClass("trainingInstance", slots=c(features="numeric", expectedOutput="numeric"));

truncateFeatures <- function(dataset, colsToKeep){
  return (dataset[colsToKeep])
}

multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  require(grid)
  
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  
  numPlots = length(plots)
  
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
                     ncol = cols, nrow = ceiling(numPlots/cols))
  }
  
  if (numPlots==1) {
    print(plots[[1]])
    
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    
    # Make each plot, in the correct location
    for (i in 1:numPlots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

#Extract Legend 
g_legend<-function(a.gplot){ 
  tmp <- ggplot_gtable(ggplot_build(a.gplot)) 
  leg <- which(sapply(tmp$grobs, function(x) x$name) == "guide-box") 
  legend <- tmp$grobs[[leg]] 
  return(legend)
} 

plotSensors <- function(dataset, normalised=TRUE, withRespectTo=dataset, title="Calibration Data Plots", showLegend=TRUE) { 
  
  localEnv <- environment();
  yLabel = "Sensor Response";
  if (normalised == TRUE){
    yLabel = paste(yLabel, "Normalised");
  }
  
  df <- normalise(dataset, normalised, withRespectTo);
  df <- melt(df, id.vars="time");
  
  p <- ggplot(df, aes(x=time, y=value, color=variable, group=variable), environment = localEnv);
  p <- p + labs(title=title, dataset="Time", y=yLabel, x="Time");
  if (showLegend == TRUE){
    p <- p + guides(col = guide_legend(nrow = 2, byrow = TRUE, title="Sensor"));
    p <- p + theme(legend.position = "bottom");
  } else{
    p <- p + theme(legend.position = "none");
  }
  p <- p + geom_line();
  
  return (p);
}

# function that takes 2 datasets and returns a single dataset that contains an average of
# the supplied datasets
averageDataSets <- function(dataset1, dataset2) {
  return ((dataset1 + dataset2) / 2);
}

#function to normalise an input vector between 0-1
norm <- function(colName, x, respectTo=x) {
  return ((x[[colName]] - min(x[[colName]], na.rm=TRUE)) / (max(x[[colName]], na.rm=TRUE) - min(x[[colName]], na.rm=TRUE)))
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

# take a dataset and returns a kernal smoothed representation of it.
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

# returns a cummative summed version of the provided dataset.
getCumulativeSquaresValues <- function(reading){
  timeRemoved <- reading[2:dim(reading)[2]];
  
  cumsums <- apply(timeRemoved, 2, function(x){
    
    return (cumsum(x^2));
  });
  
  return (cbind(reading[1], cumsums));
}

loadAllTrainingInstances <- function(){
  return ();
}