#gradle installApp --offline
workspace=/Users/qrtt1/_ehc_final_data/
export JAVA_OPTS="-DEHC_FINAL_DATASET_DIR=$workspace"

#preprocessing user EHC_2nd_round_train.log userdata_train.csv
#preprocessing user EHC_2nd_round_test.log userdata_test.csv
Rscript training.r $workspace userdata_train.csv userdata_test.csv userdata_predict.csv
