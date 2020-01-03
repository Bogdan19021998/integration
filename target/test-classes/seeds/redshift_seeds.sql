drop table if exists distil_customers;

CREATE TABLE distil_customers
(
  id                               integer,
  smallint_field                   SMALLINT,
  int2_field                       INT2,
  integer_field                    INTEGER,
  int_field                        INT,
  bigint_field                     BIGINT,
  int8_field                       INT8,
  decimal_field                    DECIMAL,
  numeric_field                    NUMERIC,
  real_field                       REAL,
  float4_field                     FLOAT4,
  double_field                     DOUBLE PRECISION,
  float8_field                     FLOAT8,
  boolean_field                    BOOLEAN,
  bool_field                       BOOL,
  char_field                       CHAR,
  character_field                  CHARACTER,
  nchar_field                      NCHAR,
  bpchar_field                     BPCHAR,
  varchar_field                    VARCHAR,
  character_varying_field          CHARACTER VARYING,
  nvarchar_field                   NVARCHAR,
  text_field                       TEXT,
  date_field                       DATE,
  timestamp_field                  TIMESTAMP,
  timestamp_without_timezone_field TIMESTAMP WITHOUT TIME ZONE,
  timestamp_with_timezone_field    TIMESTAMP WITH TIME ZONE,
  timestamptz_field                TIMESTAMPTZ
);

insert into distil_customers values
(
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, '0', '0', '0', '0', '0',
 '0', '0', '0', '2019-01-01',
 '2019-01-01 00:00:00',
 '2019-01-01 00:00:00',
 '1970-01-01 00:00:01.000000',
 '1997-12-17 07:37:16-08'
),(
 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, true, '1', '1', '1', '1', '1',
 '0', '0', '0', '2019-01-01',
 '2019-01-01 00:00:00',
 '2019-01-01 00:00:00',
 '1970-01-01 00:00:01.000000',
 '1997-12-17 07:37:16-08'
),(
 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, true, '2', '2', '2', '2', '2',
 '0', '0', '0', '2019-01-01',
 '2019-01-01 00:00:00',
 '2019-01-01 00:00:00',
 '1970-01-01 00:00:01.000000',
 '1997-12-17 07:37:16-08'
);