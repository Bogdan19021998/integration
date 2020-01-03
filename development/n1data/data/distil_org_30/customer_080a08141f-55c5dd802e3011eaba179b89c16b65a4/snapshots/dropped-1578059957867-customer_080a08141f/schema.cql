CREATE TABLE IF NOT EXISTS distil_org_30.customer_080a08141f (
	p bigint,
	cid_129331098 text,
	c timestamp,
	caddress_addr1_901313176 text,
	caddress_addr2_826384790 text,
	caddress_city_1559095873 text,
	caddress_county_1150388382 text,
	caddress_state_1244688460 text,
	caddress_zip_43768638 text,
	cemail_address_483814315 text,
	cemail_client_1763162280 text,
	cemail_type_1051051577 text,
	cip_opt_1476914017 text,
	cip_signup_1961647143 text,
	clanguage_2011175035 text,
	clast_changed_1676733611 timestamp,
	clast_note_created_at_345973362 text,
	clast_note_created_by_1857069027 text,
	clast_note_note_824336608 text,
	clast_note_note_id_682558789 bigint,
	clist_id_1713462523 text,
	clocation_country_code_1569744032 text,
	clocation_dstoff_1709996666 bigint,
	clocation_gmtoff_13767775 bigint,
	clocation_latitude_1508965091 double,
	clocation_longitude_1989890976 double,
	clocation_timezone_1262134376 text,
	cmember_rating_1185336860 bigint,
	cmerge_fields_d1000000_515444204 text,
	cmerge_fields_d50_183003780 text,
	cmerge_fields_d51_928904543 text,
	cmerge_fields_d55_1342371484 text,
	cmerge_fields_fname_611789742 text,
	cmerge_fields_lname_173671212 text,
	cmerge_fields_phone_2023858782 text,
	csource_2062945428 text,
	cstats_avg_click_rate_435932720 double,
	cstats_avg_open_rate_755100551 double,
	cstats_ecommerce_data_currency_code_642714688 text,
	cstats_ecommerce_data_number_of_orders_870850374 double,
	cstats_ecommerce_data_total_revenue_721006094 double,
	cstatus_2124943554 text,
	cstatus_if_new_1102758839 text,
	ctags_count_1800644727 bigint,
	ctimestamp_opt_416470721 timestamp,
	ctimestamp_signup_1098860246 timestamp,
	cunique_email_id_874507845 text,
	cvip_1406187502 boolean,
	h text,
	u timestamp,
	PRIMARY KEY (p, cid_129331098))
	WITH ID = 55c5dd80-2e30-11ea-ba17-9b89c16b65a4
	AND CLUSTERING ORDER BY (cid_129331098 ASC)
	AND bloom_filter_fp_chance = 0.01
	AND dclocal_read_repair_chance = 0.1
	AND crc_check_chance = 1.0
	AND default_time_to_live = 0
	AND gc_grace_seconds = 864000
	AND min_index_interval = 128
	AND max_index_interval = 2048
	AND memtable_flush_period_in_ms = 0
	AND read_repair_chance = 0.0
	AND speculative_retry = '99PERCENTILE'
	AND comment = ''
	AND caching = { 'keys': 'ALL', 'rows_per_partition': 'NONE' }
	AND compaction = { 'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy' }
	AND compression = { 'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor' }
	AND cdc = false
	AND extensions = {  };
