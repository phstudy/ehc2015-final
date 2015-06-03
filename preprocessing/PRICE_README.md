## NetworkPriceFetcher

寫了新的價格資料工具 `org.qty.validate.NetworkPriceFetcher`，資料檔需放在 `$HOME/price.cache.data`。

當沒有此檔時，會使用 PriceUtils 裡的 prices 物件建立一份（檔案內容即該物件的序列化），當查詢價值沒有值時會上網抓：

```
org.qty.validate.NetworkPriceFetcher.lookPrice(String)
``` 

處理完畢後，開發者可以在程式結束前呼叫 savePriceState 將新抓的價格存入檔案

```
org.qty.validate.NetworkPriceFetcher.savePriceState()
```

### 資料下載

此 URL 為目前的狀態檔，可解壓放後放至 `$HOME/price.cache.data` 路徑

https://s3.amazonaws.com/qrtt1.articles/20150528/20150603_price.cache.data.tgz