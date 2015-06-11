args <- commandArgs(TRUE)
workspace <- args[1]
inputfile <- args[2]
testfile <- args[3]
predict_output <- args[4]

#

local({
    r <- getOption("repos")
    r["CRAN"] <- "http://cran.r-project.org" 
    options(repos=r)
})

setwd(workspace)
print(getwd())



##
# 針對每 1 個 pid 的整合記錄，含以下欄位
# pid,view,viewBySession,price,cat,buyCount
#

# 把資料讀進來
train <- read.csv(inputfile, colClasses = c("character", "integer", "integer", "integer", "integer", "integer"))
names(train) <- c("pid","view","viewBySession","price","cat","buyCount")
train <- data.frame(train, row.names = "pid")

# buyCount 至少要是 1 才預測
train <- subset(train, buyCount>=1)

if("e1071" %in% rownames(installed.packages()) == FALSE) {install.packages("e1071")}
library(e1071)

# 25m
#model <- svm(buyCount~., data=train, kernel="polynomial")

# 1m6s
#model <- svm(buyCount~., data=train, kernel="linear")

model <- svm(buyCount~., data=train)

test <- read.csv(testfile, colClasses = c("character", "integer", "integer", "integer", "integer", "integer"))
names(test) <- c("pid","view","viewBySession","price","cat","buyCount")
test <- data.frame(test, row.names = "pid")
result <- predict(model, test)
write.csv(result, file=predict_output)


