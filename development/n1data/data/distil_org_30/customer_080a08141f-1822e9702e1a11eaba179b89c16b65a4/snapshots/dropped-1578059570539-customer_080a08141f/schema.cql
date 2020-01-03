CREATE TABLE IF NOT EXISTS distil_org_30.customer_080a08141f (
	p bigint,
	cid_1321413847 text,
	c timestamp,
	caddress_addr1_1131273325 text,
	caddress_addr2_473978840 text,
	caddress_city_252586848 text,
	caddress_county_230642482 text,
	caddress_state_495593687 text,
	caddress_zip_629240728 text,
	cemail_address_710151187 text,
	cemail_client_1025132034 text,
	cemail_type_531940502 text,
	cip_opt_121136324 text,
	cip_signup_175447937 text,
	clanguage_302820787 text,
	clast_changed_569299292 timestamp,
	clast_note_created_at_1752086940 text,
	clast_note_created_by_1574850983 text,
	clast_note_note_1307570021 text,
	clast_note_note_id_1265791146 bigint,
	clist_id_172697496 text,
	clocation_country_code_1961767961 text,
	clocation_dstoff_1629070438 bigint,
	clocation_gmtoff_1916760559 bigint,
	clocation_latitude_1068746179 double,
	clocation_longitude_189142193 double,
	clocation_timezone_1168864996 text,
	cmember_rating_1466650703 bigint,
	cmerge_fields_d1000000_1448018717 text,
	cmerge_fields_d50_2044669708 text,
	cmerge_fields_d51_577784394 text,
	cmerge_fields_d55_496010148 text,
	cmerge_fields_fname_674939704 text,
	cmerge_fields_lname_408549417 text,
	cmerge_fields_phone_2064964548 text,
	csource_1513228796 text,
	cstats_avg_click_rate_756651628 double,
	cstats_avg_open_rate_789732619 double,
	cstats_ecommerce_data_currency_code_892304833 text,
	cstats_ecommerce_data_number_of_orders_1545763996 double,
	cstats_ecommerce_data_total_revenue_1844836265 double,
	cstatus_134890916 text,
	cstatus_if_new_634637784 text,
	ctags_count_1696670678 bigint,
	ctimestamp_opt_977250034 timestamp,
	ctimestamp_signup_362409926 timestamp,
	cunique_email_id_1597235092 text,
	cvip_1631940211 boolean,
	h text,
	u timestamp,
	PRIMARY KEY (p, cid_1321413847))
	WITH ID = 1822e970-2e1a-11ea-ba17-9b89c16b65a4
	AND CLUSTERING ORDER BY (cid_1321413847 ASC)
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
