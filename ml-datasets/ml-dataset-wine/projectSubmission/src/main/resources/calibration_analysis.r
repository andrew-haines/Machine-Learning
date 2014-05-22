require(ggplot2)
library(grid)
library(reshape2)  
library(lattice)

colNames <- c("time", "SY/LG","SY/G","SY/AA","SY/Gh","SY/gCTl","SY/gCT","T30/1","P10/1","P10/2","P40/1","T70/2","PA2");

describe_2 <- function(x) {
  c(mean=mean(x), median=median(x), IQR25=as.numeric(quantile(x, probs=c(0.25))), IQR75=as.numeric(quantile(x, probs=c(0.75))) )
}

na.rm<-function(x){  
  x[!is.na(x)]
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

# view absolute EG/EP data
pEG <- plotSensors(av200EG, FALSE, title="Absolute EG (200ppb)", showLegend=FALSE);
pEP <- plotSensors(av120EP, FALSE, title="Absolute EP (120ppb)", showLegend=FALSE);
legend <- g_legend(plotSensors(av120EP, FALSE, title="Absolute EP (120ppb)", showLegend=TRUE)
                   + guides(col = guide_legend(nrow = 12, byrow = TRUE, title="Sensor"))
                   + theme(legend.direction="vertical"));

grid.arrange(arrangeGrob(arrangeGrob(pEG, pEP, ncol=1), arrangeGrob(legend), ncol = 2, widths=c(10, 2)));

# these are not normalised... Lets try that next.

avBaselineDatasetEG = normalise(avBaselineDatasetEG);
av200EG = normalise(av200EG);
av100EG = normalise(av100EG);
av25EG = normalise(av25EG);

avBaselineDatasetEP = normalise(avBaselineDatasetEP);
av120EP = normalise(av120EP);
av80EP = normalise(av80EP);
av40EP = normalise(av40EP);

# view normalised EG/EP data
pEG <- plotSensors(av200EG, TRUE, title="Normalised EG (200ppb)", showLegend=FALSE);
pEP <- plotSensors(av120EP, TRUE, title="Normalised EP (120ppb)", showLegend=FALSE);
legend <- g_legend(plotSensors(av120EP, TRUE, title="Absolute EP (120ppb)", showLegend=TRUE)
                   + guides(col = guide_legend(nrow = 12, byrow = TRUE, title="Sensor"))
                   + theme(legend.direction="vertical"));

grid.arrange(arrangeGrob(arrangeGrob(pEG, pEP, ncol=1), arrangeGrob(legend), ncol = 2, widths=c(10, 2)));

# no subtract the readings from the baseline to try and isolte the analytes reactions outside of the wine

egDifference200 = subtractSensorReadings(av200EG, avBaselineDatasetEG);
egDifference100 = subtractSensorReadings(av100EG, avBaselineDatasetEG);
egDifference25 = subtractSensorReadings(av25EG, avBaselineDatasetEG);
epDifference120 = subtractSensorReadings(av120EP, avBaselineDatasetEP);
epDifference80 = subtractSensorReadings(av80EP, avBaselineDatasetEP);
epDifference40 = subtractSensorReadings(av40EP, avBaselineDatasetEP);

# EG difference
print(plotSensors(egDifference25, FALSE)); # T70/2, T30/1, PA2
print(plotSensors(egDifference100, FALSE)); # T70/2, T30/1, PA2
pEGDiff <- plotSensors(egDifference200, FALSE, title="Absolute EG diff (200ppb)", showLegend=FALSE); # T70/2, T30/1, PA2

# EP difference
print(plotSensors(epDifference40, FALSE, title="Absolute EG Diff (40ppb)")); # why is this the same as 120????
print(plotSensors(epDifference80, FALSE)); # 
pEPDiff <- plotSensors(epDifference120, FALSE, title="Absolute EP diff (120ppb)", showLegend=FALSE); # T70/2, T30/1, PA2

legend <- g_legend(plotSensors(epDifference120, TRUE, title="Absolute EP diff (120ppb)", showLegend=TRUE)
                   + guides(col = guide_legend(nrow = 12, byrow = TRUE, title="Sensor"))
                   + theme(legend.direction="vertical"));

grid.arrange(arrangeGrob(arrangeGrob(pEGDiff, pEPDiff, ncol=1), arrangeGrob(legend), ncol = 2, widths=c(10, 2)));



plotSensors(epDifference120, FALSE); # T70/2, T30/1, PA2
plotSensors(egDifference200, FALSE); # T70/2, T30/1, PA2

# look at cummative sum of squares to see which sensors have the largest overall reaction to
# the presence of analyts

plotSensors(getCummativeSquaresValues(egDifference200), FALSE);

plotSensors(getCummativeSquaresValues(epDifference120), FALSE);

p1 <- plotSensors(getCumulativeSquaresValues(normalise(egDifference200)), FALSE, title="Cumulative Squared Sum of EG Difference", showLegend=FALSE);
p2 <- plotSensors(getCumulativeSquaresValues(normalise(epDifference120)), FALSE, title="Cumulative Squared Sum of EP Difference", showLegend=FALSE);

legend <- g_legend(plotSensors(epDifference120, TRUE, title="Absolute EP diff (120ppb)", showLegend=TRUE)
                   + guides(col = guide_legend(nrow = 12, byrow = TRUE, title="Sensor"))
                   + theme(legend.direction="vertical"));

grid.arrange(arrangeGrob(arrangeGrob(p1, p2, ncol=1), arrangeGrob(legend), ncol = 2, widths=c(10, 2)));


# no obvious signal from sensors. Lets try PCA analysis

plotPrincipleComponents <- function(dataset, title){
  
  timeRemoved = dataset[2:dim(dataset)[2]];
  
  wine.prc <- prcomp(timeRemoved, center=TRUE, scale=TRUE);

  # look at the eigen values (are the same as the varience of each PC so the square of the 
  # standard deviation). 
  mainPcs = colnames(wine.prc$x)[wine.prc$sdev ^ 2 > 1]; # kaiser criteria. Keep PC's for eigen values greater than 1
  print(paste('Main principle components', paste(mainPcs, collapse=',')));

  print(summary(wine.prc))
  screeplot(wine.prc, main=title, type="lines", npcs=length(wine.prc$sdev))
  
  return (wine.prc);
  
  # now lets look at what sensors contribute most to the located PC's
}

wine.prcEG200 <- plotPrincipleComponents(egDifference200, "Scree Plot for EG difference");
wine.prcEP120 <- plotPrincipleComponents(epDifference120, "Scree Plot for EP difference");


screeplot(wine.prcEG200, main="Scree Plot", xlab="Components")
screeplot(wine.prcEG200, main="Scree Plot", type="lines", npcs=length(wine.prcEG200$sdev))

load    <- wine.prcEG200$rotation
sorted.loadings <- load[order(load[, 1]), 1]
myTitle <- "Loadings Plot for PC1" 
myXlab  <- "Variable Loadings"
dotplot(sorted.loadings, main=myTitle, xlab=myXlab, cex=1.5, col="red") #SY/G, SY/Gh, and SY/gCT

load    <- wine.prcEG200$rotation
sorted.loadings <- load[order(load[, 2]), 1]
myTitle <- "Loadings Plot for PC2" 
myXlab  <- "Variable Loadings"
dotplot(sorted.loadings, main=myTitle, xlab=myXlab, cex=1.5, col="red")

load    <- wine.prcEP120$rotation
sorted.loadings <- load[order(load[, 1]), 1]
myTitle <- "Loadings Plot for PC1" 
myXlab  <- "Variable Loadings"
dotplot(sorted.loadings, main=myTitle, xlab=myXlab, cex=1.5, col="red") #SY/G, SY/Gh, and SY/gCT

load    <- wine.prcEP120$rotation
sorted.loadings <- load[order(load[, 2]), 1]
myTitle <- "Loadings Plot for PC2" 
myXlab  <- "Variable Loadings"
dotplot(sorted.loadings, main=myTitle, xlab=myXlab, cex=1.5, col="red")

biplot(plotPrincipleComponents(deSampleDataset(egDifference200, 4), title="Desampled EG Diff"), cex=c(1, 0.7))

biplot(plotPrincipleComponents(deSampleDataset(epDifference120, 4), title="Desampled EP Diff"), cex=c(1, 0.7))

# result of desampling
plotSensors(deSampleDataset(egDifference200, 40), FALSE);
test <- plotPrincipleComponents(deSampleDataset(egDifference200, 40), title="Desamples EG Diff");

plotSensors(deSampleDataset(egDifference, 10), FALSE);
plotSensors(deSampleDataset(epDifference120, 10), FALSE);

nrow(egDifference);
nrow(deSampleDataset(egDifference, 10));



plotSensors(truncateFeatures(egDifference, c("time", "T70/2", "T30/1", "PA2")), FALSE)
plotSensors(truncateFeatures(epDifference, c("time", "T70/2", "T30/1", "PA2")), FALSE)

