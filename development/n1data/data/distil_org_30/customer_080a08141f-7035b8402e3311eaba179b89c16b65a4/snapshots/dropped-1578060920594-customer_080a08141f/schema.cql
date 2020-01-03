CREATE TABLE IF NOT EXISTS distil_org_30.customer_080a08141f (
	p bigint,
	cid_570831125 text,
	c timestamp,
	caddress_addr1_426055483 text,
	caddress_addr2_559302307 text,
	caddress_city_1285584563 text,
	caddress_county_1618694266 text,
	caddress_state_695768195 text,
	caddress_zip_412348133 text,
	cemail_address_1661711967 text,
	cemail_client_1893981313 text,
	cemail_type_1418584775 text,
	cip_opt_2000415873 text,
	cip_signup_391783994 text,
	clanguage_1745932482 text,
	clast_changed_1923988912 timestamp,
	clast_note_created_at_811871371 text,
	clast_note_created_by_1574743689 text,
	clast_note_note_745414716 text,
	clast_note_note_id_86822794 bigint,
	clist_id_633386480 text,
	clocation_country_code_1312082972 text,
	clocation_dstoff_1989279801 bigint,
	clocation_gmtoff_2015210946 bigint,
	clocation_latitude_1824972823 double,
	clocation_longitude_1385087880 double,
	clocation_timezone_1806510069 text,
	cmember_rating_734497903 bigint,
	cmerge_fields_d1000000_690475291 text,
	cmerge_fields_d50_1504198223 text,
	cmerge_fields_d51_646162879 text,
	cmerge_fields_d55_163378 text,
	cmerge_fields_fname_1547002344 text,
	cmerge_fields_lname_664518795 text,
	cmerge_fields_phone_651028566 text,
	csource_1269807879 text,
	cstats_avg_click_rate_349984787 double,
	cstats_avg_open_rate_1297963258 double,
	cstats_ecommerce_data_currency_code_528054427 text,
	cstats_ecommerce_data_number_of_orders_1297092286 double,
	cstats_ecommerce_data_total_revenue_678809981 double,
	cstatus_1407862899 text,
	cstatus_if_new_456005422 text,
	ctags_count_335736120 bigint,
	ctimestamp_opt_549898153 timestamp,
	ctimestamp_signup_408156525 timestamp,
	cunique_email_id_2063666978 text,
	cvip_1076177403 boolean,
	h text,
	u timestamp,
	PRIMARY KEY (p, cid_570831125))
	WITH ID = 7035b840-2e33-11ea-ba17-9b89c16b65a4
	AND CLUSTERING ORDER BY (cid_570831125 ASC)
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
