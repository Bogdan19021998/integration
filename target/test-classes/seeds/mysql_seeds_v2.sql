CREATE TABLE distil.distil_customers
(
  id                     TEXT             NOT NULL,
  integer_field          INTEGER          NOT NULL,
  int_field              INT              NOT NULL,
  smallint_field         SMALLINT         NOT NULL,
  tinyint_field          TINYINT          NOT NULL,
  mediumint_field        MEDIUMINT        NOT NULL,
  bit_field              BIT              NOT NULL,
  decimal_field          DECIMAL          NOT NULL,
  dec_field              DEC              NOT NULL,
  fixed_field            FIXED            NOT NULL,
  numeric_field          NUMERIC          NOT NULL,
  float_field            FLOAT            NOT NULL,
  real_field             REAL             NOT NULL,
  double_precision_field double precision NOT NULL,
  double_field           DOUBLE           NOT NULL,
  date_field             DATE             NOT NULL,
  time_field             TIME             NOT NULL,
  datetime_field         DATETIME         NOT NULL,
  timestamp_field        TIMESTAMP        NOT NULL,
  year_field             YEAR             NOT NULL,
  char_field             CHAR             NOT NULL,
  binary_field BINARY NOT NULL,
  blob_field             BLOB             NOT NULL,
  text_field             TEXT             NOT NULL,
  enum_field             ENUM('M', 'F')   NOT NULL,
  null_field             TEXT
);

insert into distil.distil_customers
values ('0', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        '2019-01-01', '08:00:00', '9999-12-31 23:59:59', '1970-01-01 00:00:03.000000', '1970', '0', '0', '0', '0', 'M',
        '0'),
       ('1', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        '2019-01-01', '08:00:00', '9999-12-31 23:59:59', '1970-01-01 00:00:03.000000', '1970', '1', '1', '1', '1', 'F',
        '1'),
       ('2', 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2,
        '2019-01-01', '08:00:00', '9999-12-31 23:59:59', '1970-01-01 00:00:03.000000', '1970', '2', '2', '2', '2', 'M',
        '2');

CREATE TABLE distil.distil_content
(
  id   text,
  name text,
  fake INT
);

INSERT INTO distil.distil_content
  VALUES
         ('0', 'name0', 0),
         ('1', 'name1', 1),
         ('2', 'name2', 2);