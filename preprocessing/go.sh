#gradle installApp --offline
workspace=/Users/qrtt1/_ehc_final_data/
export JAVA_OPTS="-DEHC_FINAL_DATASET_DIR=$workspace"

# 前處理的 n 種方法

#preprocessing user EHC_2nd_round_train.log userdata_train.csv
#preprocessing user EHC_2nd_round_test.log userdata_test.csv
#preprocessing product EHC_2nd_round_train.log product_train.csv
#preprocessing product EHC_2nd_round_test.log product_test.csv

#Rscript generate_user_model.r $workspace userdata_train.csv userdata_test.csv userdata_predict.csv
Rscript generate_product_model.r $workspace product_train.csv product_test.csv product_predict.csv
#preprocessing result5 EHC_2nd_round_test.log userdata_predict.csv product_predict.csv 9 result.txt 
#preprocessing result6 EHC_2nd_round_test.log userdata_predict.csv product_predict.csv 9 result.txt 
#preprocessing result5 EHC_2nd_round_test.log userdata_predict.csv product_predict.csv 9 result.txt | tail
