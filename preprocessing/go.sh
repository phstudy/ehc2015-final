gradle installApp --offline
workspace=/Users/qrtt1/_ehc_final_data/
export JAVA_OPTS="-DEHC_FINAL_DATASET_DIR=$workspace"

# 前處理的 n 種方法

#preprocessing user EHC_2nd_round_train.log userdata_train.csv
#preprocessing user EHC_2nd_round_test.log userdata_test.csv

preprocessing product EHC_2nd_round_train.log product_train.csv
preprocessing product EHC_2nd_round_test.log product_test.csv
#Rscript training.r $workspace userdata_train.csv userdata_test.csv userdata_predict.csv
# => training product 的資料
# => 丟結果與分數
