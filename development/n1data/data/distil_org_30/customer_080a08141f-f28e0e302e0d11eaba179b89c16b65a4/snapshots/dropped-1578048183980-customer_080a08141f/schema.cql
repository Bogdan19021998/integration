CREATE TABLE IF NOT EXISTS distil_org_30.customer_080a08141f (
	p bigint,
	cid_308587321 text,
	c timestamp,
	caddress_addr1_817645167 text,
	caddress_addr2_1080952654 text,
	caddress_city_1705746523 text,
	caddress_county_844407145 text,
	caddress_state_492975685 text,
	caddress_zip_971581095 text,
	cemail_address_406575556 text,
	cemail_client_519854475 text,
	cemail_type_390075164 text,
	cip_opt_1476266755 text,
	cip_signup_1802935114 text,
	clanguage_80051488 text,
	clast_changed_1772966048 timestamp,
	clast_note_created_at_1215623712 text,
	clast_note_created_by_803827312 text,
	clast_note_note_130901119 text,
	clast_note_note_id_1178254833 bigint,
	clist_id_2018954374 text,
	clocation_country_code_895220811 text,
	clocation_dstoff_172392888 bigint,
	clocation_gmtoff_1619686775 bigint,
	clocation_latitude_951660749 double,
	clocation_longitude_1428124209 double,
	clocation_timezone_1870948881 text,
	cmember_rating_801550245 bigint,
	cmerge_fields_d1000000_640868653 text,
	cmerge_fields_d50_1027947766 text,
	cmerge_fields_d51_1122044798 text,
	cmerge_fields_d55_1792136538 text,
	cmerge_fields_fname_845353406 text,
	cmerge_fields_lname_1513486578 text,
	cmerge_fields_phone_588789099 text,
	csource_541885605 text,
	cstats_avg_click_rate_58852807 double,
	cstats_avg_open_rate_1782190605 double,
	cstats_ecommerce_data_currency_code_1163194750 text,
	cstats_ecommerce_data_number_of_orders_1979863981 double,
	cstats_ecommerce_data_total_revenue_1594734598 double,
	cstatus_578194536 text,
	cstatus_if_new_1028372804 text,
	ctags_count_828215856 bigint,
	ctimestamp_opt_561889058 timestamp,
	ctimestamp_signup_17172363 timestamp,
	cunique_email_id_1647558774 text,
	cvip_955714878 boolean,
	h text,
	u timestamp,
	PRIMARY KEY (p, cid_308587321))
	WITH ID = f28e0e30-2e0d-11ea-ba17-9b89c16b65a4
	AND CLUSTERING ORDER BY (cid_308587321 ASC)
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
