require(ggplot2)
library(grid)
library(reshape2)  

colNames <- c("time", "SY/LG","SY/G","SY/AA","SY/Gh","SY/gCTl","SY/gCT","T30/1","P10/1","P10/2","P40/1","T70/2","PA2");

averageDataSets <- function(dataset1, dataset2) {
  return ((dataset1 + dataset2) / 2);
}

describe_2 <- function(x) {
  c(mean=mean(x), median=median(x), IQR25=as.numeric(quantile(x, probs=c(0.25))), IQR75=as.numeric(quantile(x, probs=c(0.75))) )
}

norm <- function(x) {
  return ((x - min(x, na.rm=TRUE)) / (max(x, na.rm=TRUE) - min(x, na.rm=TRUE)))
}

normalise <- function(vector, normalised=TRUE){
  if (normalised == TRUE){
    #result <- (vector - min(vector)) / (max(vector) - min(vector));
    
    return (as.data.frame(lapply(vector, norm)) );
  }else { # otherwise disable normalisation
    return (vector);
  }
}

plotSensors <- function(dataset, normalised=TRUE) { 
  
  localEnv <- environment();
  yLabel = "Sensor Response";
  if (normalised == TRUE){
    yLabel = paste(yLabel, "Normalised");
  }
  
  df <- normalise(dataset, normalised);
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

na.rm<-function(x)
{  x[!is.na(x)]
}

crossval<- function(x,y, nstep=20,bmax=0,  L1=TRUE){
  # finds the best bandwidth for ksmooth by cross-validation
  # searches for bandwidts from zero to bmax 
  # uses criterion L1 or L2 (default)
  # nstep is number of grid points for bandwidth search
  # stud=TRUE means studentize by kernel smoothed estimate of variance 
  # (with same bandwidth)
  
  if (bmax==0){bmax<- 1.5*sqrt(var(x))}
  
  bstep<-  bmax/nstep
  n<- length(x)
  SSE<- c(1:nstep)*0 
  
  if(L1==FALSE){
    for (i in 1:nstep){
      for (k in 2:(n-1)){
        xx<- c(x[1:(k-1)], x[(k+1):n])
        yy<- c(y[1:(k-1)], y[(k+1):n])
        kss<- ksmooth(xx,yy,"normal",ban=(i*bstep),x.points=x[k]) 
        # rr<-  kresid(xx,yy, (i*bstep))
        SSE[i]<- SSE[i]+(y[k]-kss$y )^2
      }}}
  
  if(L1==TRUE){
    for (i in 1:nstep){
      for (k in 2:(n-1)){
        xx<- c(x[1:(k-1)], x[(k+1):n])
        yy<- c(y[1:(k-1)], y[(k+1):n])
        kss<- ksmooth(xx,yy,"normal",ban=(i*bstep),x.points=x[k]) 
        #  rr<-  kresid(xx,yy, (i*bstep))
        SSE[i]<- SSE[i]+ abs(y[k]-kss$y ) 
      }}}
  
  k<- c(1:nstep)*bstep
  k<- k[SSE==min(SSE,na.rm = TRUE)]
  na.rm(k)
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

R_DATA = "/Users/haines/devsrc/Machine-Learning/ml-datasets/ml-dataset-wine/src/main/resources/";

SEP <- "\t";

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

avBaselineDatasetEG = averageDataSets(calibration_baseline_Data1, calibration_baseline_Data2);
av200EG = averageDataSets(EGcalibration_baseline_Data5, EGcalibration_baseline_Data6);

avBaselineDatasetEP = averageDataSets(calibration_baseline_Data3, calibration_baseline_Data4);
av120EP = averageDataSets(EPcalibration_baseline_Data5, EPcalibration_baseline_Data6);
av80EP = averageDataSets(EPcalibration_baseline_Data3, EPcalibration_baseline_Data4);

egDifference = subtractSensorReadings(av200EG, avBaselineDatasetEG);
epDifference = subtractSensorReadings(av120EP, avBaselineDatasetEP);
epDifference2 = subtractSensorReadings(av80EP, avBaselineDatasetEP);

plotSensors(avBaselineDatasetEG, TRUE); # T70/2, T30/1, PA2
plotSensors(egDifference, FALSE); # T70/2, T30/1, PA2
plotSensors(epDifference, FALSE); # T70/2, T30/1, PA2
plotSensors(epDifference2, FALSE); 

plotSensors(truncateFeatures(egDifference, c("time", "T70/2", "T30/1", "PA2")), FALSE)
plotSensors(truncateFeatures(epDifference, c("time", "T70/2", "T30/1", "PA2")), FALSE)
