CREATE TABLE IF NOT EXISTS distil_org_30.customer_080a08141f (
	p bigint,
	cid_1438534721 text,
	c timestamp,
	caddress_addr1_888874903 text,
	caddress_addr2_164699740 text,
	caddress_city_1542102731 text,
	caddress_county_81885534 text,
	caddress_state_672825264 text,
	caddress_zip_1326917627 text,
	cemail_address_1663378109 text,
	cemail_client_1538088282 text,
	cemail_type_1865011537 text,
	cip_opt_218554974 text,
	cip_signup_1710078380 text,
	clanguage_1322256059 text,
	clast_changed_1829813730 timestamp,
	clast_note_created_at_1576842661 text,
	clast_note_created_by_1621338380 text,
	clast_note_note_403261103 text,
	clast_note_note_id_1784086910 bigint,
	clist_id_326108326 text,
	clocation_country_code_713687469 text,
	clocation_dstoff_936977262 bigint,
	clocation_gmtoff_594070269 bigint,
	clocation_latitude_1965941096 double,
	clocation_longitude_1655166536 double,
	clocation_timezone_118174486 text,
	cmember_rating_1584733970 bigint,
	cmerge_fields_d1000000_238602261 text,
	cmerge_fields_d50_509832925 text,
	cmerge_fields_d51_481994451 text,
	cmerge_fields_d55_1846705155 text,
	cmerge_fields_fname_1036573023 text,
	cmerge_fields_lname_1404757637 text,
	cmerge_fields_phone_348138032 text,
	csource_324481848 text,
	cstats_avg_click_rate_1166806425 double,
	cstats_avg_open_rate_2035097270 double,
	cstats_ecommerce_data_currency_code_407039890 text,
	cstats_ecommerce_data_number_of_orders_1474998462 double,
	cstats_ecommerce_data_total_revenue_426102940 double,
	cstatus_802936353 text,
	cstatus_if_new_1923640973 text,
	ctags_count_2128327351 bigint,
	ctimestamp_opt_620469194 timestamp,
	ctimestamp_signup_581295458 timestamp,
	cunique_email_id_622045433 text,
	cvip_367030089 boolean,
	h text,
	u timestamp,
	PRIMARY KEY (p, cid_1438534721))
	WITH ID = f9196ce0-2e17-11ea-ba17-9b89c16b65a4
	AND CLUSTERING ORDER BY (cid_1438534721 ASC)
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
