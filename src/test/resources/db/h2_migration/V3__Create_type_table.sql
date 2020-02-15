create table type(
    int_c INT not null,
    boolean_c BOOLEAN not null,
    tinyint_c TINYINT not null,
    smallint_c SMALLINT not null,
    bigint_c BIGINT not null,
    decimal_c DECIMAL not null,
    double_c DOUBLE not null,
    float_c FLOAT not null,
    real_c REAL not null,
    time_c TIME not null,
    date_c DATE not null,
    timestamp_c TIMESTAMP not null,
    timestamp_with_time_zone_c TIMESTAMP WITH TIME ZONE not null,
    binary_c BINARY null,
    varbinary_c VARBINARY null,
    longvarbinary_c LONGVARBINARY null,
    varchar_c VARCHAR not null,
    longvarchar_c LONGVARCHAR not null,
    char_c CHAR not null,
    blob_c BLOB null,
    clob_c CLOB not null,
    PRIMARY KEY(int_c)
);