# DATA-TRANSPORTER

```sh
AUTOMATION SERVICES LTD
VERSION : 1.0.0-SNAPSHOT
DEVELOPED BY : ZUBAYER AHAMED
```

## Build
```sh
mvn clean install
```

## Dcoumentation

Basic Properties
| Key | Des | Example |
| ------ | ------ | ------ |
| server.port | Server port to run application  | 8081 |
| scheduling.enabled | To enable/disable Schedular | true |
| data.transport.delay.initial | Schedular Initial delay time | PT10S |
| data.transport.delay | Schedular delay time | PT10S |

Primary Table (For tracking last transfered record)
| Key | Des | Example |
| ------ | ------ | ------ |
| spring.datasource.primary.url | Database URL  |  |
| spring.datasource.primary.username | Database Username |  |
| spring.datasource.primary.password | Database Password |  |
| spring.datasource.primary.driver-class-name | Driver class name |  |
| primary.table.name | Table name | recordstatus |
| primary.table.columns | Table columns | id,value,date |
| primary.table.read.orderby | Read order by column | value |
| primary.table.read.condition.column | Read condition column | value |
| primary.table.insert.columns | Data insert to columns | value |
| primary.table.values.columns | Data insert to columns values (Must match with source column) | {TRANSACTION_ID:"INTEGER"} |

Source Table
| Key | Des | Example |
| ------ | ------ | ------ |
| spring.datasource.from.url | Database URL |  |
| spring.datasource.from.username | Database Username |  |
| spring.datasource.from.password | Database Password |  |
| spring.datasource.from.driver-class-name | Driver class name |  |
| from.table.name | Table name | XXSSGIL_WB_INTEGRATION |
| from.table.columns | Table columns to read with data type | {TRANSACTION_ID:"INTEGER",TRIP_NUMBER:"INTEGER",LIGHT_WEIGHT_TIME_STAMP:"DATE",LOAD_WEIGHT_TIME_STAMP:"DATE",LIGHT_WEIGHT_BY:"STRING",LOAD_WEIGHT_BY:"STRING",LIGHT_WT_IN_KG:"INTEGER",LOAD_WT_IN_KG:"INTEGER"} |
| from.table.read.condition.column | Read conditional column with data type | {TRANSACTION_ID:"INTEGER"} |

Destination Table
| Key | Des | Example |
| ------ | ------ | ------ |
| spring.datasource.to.url | Database URL |  |
| spring.datasource.to.username | Database Username |  |
| spring.datasource.to.password | Database Password |  |
| spring.datasource.to.driver-class-name | Driver class name |  |
| to.table.name | Table name |  |
| to.table.insert.columns | Data insert to columns | TRIP_NO,LIGHT_WT_TIMESTAMP,LOAD_WT_TIMESTAMP,LIGHT_WT_BY,LOAD_WT_BY,LOAD_WT_IN_KG,LIGHT_WT_IN_KG |
| to.table.values.columns | Data insert values from source data columns with data type | {TRIP_NUMBER:"INTEGER",LIGHT_WEIGHT_TIME_STAMP:"DATE",LOAD_WEIGHT_TIME_STAMP:"DATE",LIGHT_WEIGHT_BY:"STRING",LOAD_WEIGHT_BY:"STRING",LIGHT_WT_IN_KG:"INTEGER",LOAD_WT_IN_KG:"INTEGER"} |