CREATE TABLE IF NOT EXISTS distil_org_30.customer_080a08141f (
	p bigint,
	cid_803708197 text,
	c timestamp,
	caddress_addr1_2030219587 text,
	caddress_addr2_1114100546 text,
	caddress_city_1024213833 text,
	caddress_county_1681343534 text,
	caddress_state_77860366 text,
	caddress_zip_196502674 text,
	cemail_address_1984942012 text,
	cemail_client_1239025392 text,
	cemail_type_169962707 text,
	cip_opt_1520287001 text,
	cip_signup_1058630021 text,
	clanguage_747547686 text,
	clast_changed_471120641 timestamp,
	clast_note_created_at_470302926 text,
	clast_note_created_by_1559413987 text,
	clast_note_note_49485790 text,
	clast_note_note_id_1699180995 bigint,
	clist_id_1594979186 text,
	clocation_country_code_177082647 text,
	clocation_dstoff_390922112 bigint,
	clocation_gmtoff_1626547973 bigint,
	clocation_latitude_257811627 double,
	clocation_longitude_854267168 double,
	clocation_timezone_717848771 text,
	cmember_rating_2060479940 bigint,
	cmerge_fields_d1000000_15186597 text,
	cmerge_fields_d50_1677773698 text,
	cmerge_fields_d51_831318007 text,
	cmerge_fields_d55_1182265370 text,
	cmerge_fields_fname_668125708 text,
	cmerge_fields_lname_1897013450 text,
	cmerge_fields_phone_1154986090 text,
	csource_1006227532 text,
	cstats_avg_click_rate_2062696931 double,
	cstats_avg_open_rate_872985698 double,
	cstats_ecommerce_data_currency_code_263606217 text,
	cstats_ecommerce_data_number_of_orders_425949715 double,
	cstats_ecommerce_data_total_revenue_1018544345 double,
	cstatus_243414572 text,
	cstatus_if_new_714611223 text,
	ctags_count_785910773 bigint,
	ctimestamp_opt_1775701927 timestamp,
	ctimestamp_signup_197064840 timestamp,
	cunique_email_id_1148478049 text,
	cvip_771947384 boolean,
	h text,
	u timestamp,
	PRIMARY KEY (p, cid_803708197))
	WITH ID = e2e59250-2e0d-11ea-ba17-9b89c16b65a4
	AND CLUSTERING ORDER BY (cid_803708197 ASC)
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
