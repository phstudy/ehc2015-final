setwd("/Users/qrtt1/Desktop/EHC");
train = read.csv("train.csv");
library(rpart);
attach(train);
names(train) <- c("weekOfDay","hour","eruid", 
                  "cid1", "cid2", "cid3","cid4", "cid5", "eturec","pid","vienum","uid","price","buy","num")
# View(head(train, 100))

fit <- rpart(buy ~ weekOfDay + hour + pid + vienum + price + num, data=train, method="class")

install.packages('rattle')
install.packages('rpart.plot')
install.packages('RColorBrewer')

library(rattle)
library(rpart.plot)
library(RColorBrewer)

fancyRpartPlot(fit)
