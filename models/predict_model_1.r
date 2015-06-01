local({
    r <- getOption("repos")
    r["CRAN"] <- "http://cran.r-project.org" 
    options(repos=r)
})

##
# 針對每 1 個 eruid 的整合記錄，含以下欄位
#
# eruid,viewcount,uniq_viewcount,cat_0,cat_1,cat_A,cat_B,cat_C,cat_D,cat_E,cat_F,cat_G,cat_H,cat_I,cat_J,cat_K,cat_L,cat_O,cat_V,max_cat,buyCount,buy
#
# eruid 是 row id (對 R 來說，就是 data.frame 指定為 row.names 的欄位)
# 除了 max_cat 與 buy 是類別變數之外其餘皆為連續變數
#

# 把資料讀進來
test_data <- read.csv("eruid_test_qty.lab.csv")

# 轉成 data frame 並指定 eruid 為 row id
test_data <- data.frame(test_data, row.names = "eruid")

# 指定 max_cat 為無序的類別變數
test_data$max_cat <- factor(test_data$max_cat, levels = c("0","1","A","B","C","D","E","F","G","H","I","J","K","L","O","V"))

fit <- readRDS("model_qty_1.rda")
library(randomForest)
result <- predict(fit, test_data)
write.csv(result, file="model_1_result.csv")
