delete from data_history;

alter table data_history
    alter column data type varchar(4000) using data::varchar(4000);
