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

# 把資料讀進來
train <- read.csv(inputfile)
names(train) <- c("eruid","viewcount","uniq_viewcount","cat_0","cat_1","cat_A","cat_B","cat_C","cat_D","cat_E","cat_F","cat_G","cat_H","cat_I","cat_J","cat_K","cat_L","cat_O","cat_V","max_cat","buyCount","buy")

# 轉成 data frame 並指定 eruid 為 row id
train <- data.frame(train, row.names = "eruid")

# 指定 max_cat 為無序的類別變數
train$max_cat <- factor(train$max_cat, levels = c("0","1","A","B","C","D","E","F","G","H","I","J","K","L","O","V"))

# 指定 buy 為無序的類別變數
train$buy <- factor(train$buy, levels=c("1", "0"))

# 引用 randomForest library
if("randomForest" %in% rownames(installed.packages()) == FALSE) {install.packages("randomForest")}
library(randomForest)

model <- randomForest(train$buy ~ viewcount + uniq_viewcount + cat_0 + cat_1 + cat_A + cat_B + cat_C + cat_D + cat_E + cat_F + cat_G + cat_H + cat_I + cat_J + cat_K + cat_L + cat_O + cat_V + max_cat, data=train, importance=TRUE, ntree=30)

##############################################################################################################################

test_data <- read.csv(testfile)

# 轉成 data frame 並指定 eruid 為 row id
test_data <- data.frame(test_data, row.names = "eruid")

# 指定 max_cat 為無序的類別變數
test_data$max_cat <- factor(test_data$max_cat, levels = c("0","1","A","B","C","D","E","F","G","H","I","J","K","L","O","V"))

library(randomForest)
result <- predict(model, test_data)
write.csv(result, file=predict_output)
