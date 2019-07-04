drop table if exists distil_consumers;

CREATE TYPE gender AS ENUM ('M', 'F');

CREATE TABLE distil_consumers
(
  id                               INT                         NOT NULL PRIMARY KEY,
  real_field                       REAL                        NOT NULL,
  double_precision_field           double precision            NOT NULL,
  smallserial_field                SMALLSERIAL                 NOT NULL,
  serial_field                     SERIAL                      NOT NULL,
  bigserial_field                  BIGSERIAL                   NOT NULL,

  integer_field                    INTEGER                     NOT NULL,
  int_field                        INT                         NOT NULL,
  smallint_field                   SMALLINT                    NOT NULL,
  numeric_field                    NUMERIC                     NOT NULL,
  bit_field                        BIT                         NOT NULL,
  bit_varying_field                bit varying                 NOT NULL,
  decimal_field                    DECIMAL                     NOT NULL,
  dec_field                        DEC                         NOT NULL,
  float_field                      FLOAT                       NOT NULL,
  money_field                      MONEY                       NOT NULL,

  character_varying_field          CHARACTER VARYING           NOT NULL,
  varchar_field                    VARCHAR                     NOT NULL,
  character_field                  CHARACTER                   NOT NULL,
  char_field                       CHAR                        NOT NULL,
  text_field                       TEXT                        NOT NULL,
  bytea_field                      BYTEA                       NOT NULL,

  timestamp_field                  TIMESTAMP                   NOT NULL,
  timestamp_with_timezone_field    TIMESTAMP WITH TIME ZONE    NOT NULL,
  timestamp_wihtout_timezone_field TIMESTAMP without time zone NOT NULL,
  date_field                       DATE                        NOT NULL,
  time_field                       TIME                        NOT NULL,
  time_with_timezone_field         TIME WITH TIME ZONE         NOT NULL,
  time_without_timezone_field      TIME without time zone      NOT NULL,
  interval_field                   interval                    NOT NULL,

  bool_field                       boolean                     NOT NULL,
  enum_field                       gender                      NOT NULL,
  cidr_field                       cidr                        NOT NULL,
  inet_field                       inet                        NOT NULL,
  macaddr_field                    macaddr                     NOT NULL,

  uuid_field                       UUID                        NOT NULL

);

insert into distil_consumers
values (0, 0, 0, 0, 0, 0, 0, 0, 0, 0, B'0', B'0', 0, 0, 0, 0, '0', '0', '0', '0', '0', E'\\000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000',
        '2019-01-01', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1 year', true, 'M', '192.168.100.128/25', '192.168.100.128', '08:00:2b:01:02:03',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
       ),
       (1, 1, 1, 1, 1, 1, 1, 1, 1, 1, B'1', B'1', 1, 1, 1, 1, '1', '1', '1', '1', '1', E'\\000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000',
        '2019-01-01', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1 year', true, 'M', '192.168.100.128/25', '192.168.100.128', '08:00:2b:01:02:03',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
       ),
       (2, 2, 2, 2, 2, 2, 2, 2, 2, 2, B'0', B'0', 2, 2, 2, 2, '2', '2', '2', '2', '2', E'\\000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000',
        '2019-01-01', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1970-01-01 00:00:01.000000', '1 year', true, 'M', '192.168.100.128/25', '192.168.100.128', '08:00:2b:01:02:03',
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
       )
