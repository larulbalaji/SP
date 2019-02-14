BEGIN TRY
	BEGIN TRANSACTION

	DECLARE @RuleId bigint;
	DECLARE @POId1 bigint;
	DECLARE @PolicyId bigint;
	DECLARE @CategoryID bigint;

	INSERT INTO [whiteops].[data_classification_category]
			   ([Name]
			   ,[Description]
			   ,[IsSystem])
	VALUES
		 ('GDPR'
		 ,'GDPR'
		 ,1)
	
	set @CategoryID = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy]
           ([name]
           ,[description]
           ,[owner_ra_user_id]
           ,[is_active]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('General Data Protection Regulation (GDPR)'
           ,'Detect personal information related to residents of the European Union (EU) , as required by GDPR.'
           ,NULL
           ,0
           ,1
           ,0)

	set @PolicyId = SCOPE_IDENTITY();



	----

	---phone
	---

	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU Phone Number'
           ,'EU Phone Number'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (1
           ,'EU Phone Number'
           ,'EU Phone Number'
           ,0
           ,'[0]([4]?\d{2}[ ,-]?(\d{2}[ ,-]?){2}\d{2})|(\d[ ,-]\d{3}[ ,-]?\d{2}[ ,-]?\d{2})?&?[0][9,8]?\d[ ,-]?(\d{3})[ ,-]?\d{4}?&?[0][0-9]{1,2}[ ,-]?\d{3}[ ,-]?\d{4}?&?([2]|[9])\d[ ]?\d{6}?&?[1-9]\d{2}[ ,-]?\d{3}[ ,-]?\d{3}?&?(\d{2}[ ,-]?){3}\d{2}?&?[3-9]\d{2,3}[ ]?\d{4}?&?((90[0-9]{3})?0|\+358\s?)(?!(100|20(0|2(0|[2-3])|9[8-9])|300|600|700|708|75(00[0-3]|(1|2)\d{2}|30[0-2]|32[0-2]|75[0-2]|98[0-2])))(4|50|10[1-9]|20(1|2(1|[4-9])|[3-9])|29|30[1-9]|71|73|75(00[3-9]|30[3-9]|32[3-9]|53[3-9]|83[3-9])|2|3|5|6|8|9|1[3-9])\s?(\d\s?){4,19}\d?&?[0][1-9][ ,-]?(\d{2}[ ,-]?){3}\d{2}?&?[2,6]\d{2}[ ,-]?\d{3}[ ,-]?\d{4}?&?(06)?[ ,-]?(([1][ ,-]?\d{3}[ ,-]?\d{4})|(\d{2}[ ,-]?\d{3}[ ,-]?\d{3}))?&?(0)\d[ ,-]?[ ,-]\d{3}[ ,-]\d{4,5}?&?((0)\d{1,2}[ ,-]?\d{7,8})|((3)\d{2}[ ,-]?\d{7})?&?[5,6,2]\d[ ,-]?\d{3}[ ,-]?\d{3}?&?[8]((\d[ ,-]?\d{3}[ ,-]?\d{4})|(\d{2}[ ,-]?\d{2}[ ,-]\d{4})|(\d{3}[ ,-]?\d{5}))?&?([6]\d[1][ ,-]?\d{3}[ ,-]?\d{3})?&?\d{4}[ ,-]?\d{4}?&?[0]((\d{2}[ ,-]?\d{3}[ ,-]?\d{4})|(\d{3}[ ,-]?\d{3}[ ,-]?\d{3}))?&?\d{2}[ ,-]?\d{3}[ ,-]?\d{2}[ ,-]?\d{2}?&?[1-9]\d[ ,-]?\d{3}[ ,-]?\d{4}?&?(0)[7,2,3]\d{2}[ ,-]\d{3}[ ,-]\d{3}?&?(0)((\d[ ,-,\/]\d{8})|(\d{2}[ ,-,\/]\d{7}))?&?(0)((\d[ ,-]\d{3}[ ,-]\d{2}[ ,-]\d{2})|(\d{2}[ ,-]\d{3}[ ,-]\d{3})|\d{3}[ ,-]\d{2}[ ,-]\d{2})?&?[1-9]\d{2}[ ,-](\d{2}[ ,-]){2}\d{2}?&?(0)[1-9]\d{0,3}[ ,-]\d{6,8}?&?(0)[1,2,3,7]((\d[ ,-]\d{4}[ ,-]\d{4})|(\d{2}[ ,-]\d{3}[ ,-]\d{4})|(\d{3}[ ,-]\d{6}))'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,5
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)




-----
--EU Phone Terms
----


	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU Phone Terms'
           ,'EU Phone Terms'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (7 -- 7 keyword
           ,'EU Phone Terms'
           ,'EU Phone Terms'
           ,0
           ,N'Telefonnummer?&?телефонен номер?&?broj telefona?&?τηλέφωνο?&?telefon numarası?&?telefonní číslo?&?telefonnummer?&?telefoninumber?&?puhelinnumero?&?numéro de téléphone?&?Telefonnummer?&?τηλέφωνο?&?telefonszám?&?uimhir ghutháin?&?numero di telefono?&?tālruņa numurs?&?telefono numeris?&?Telefonsnummer?&?numru tat-telefon?&?telefoonnummer?&?numer telefonu?&?número de telefone?&?număr de telefon?&?telefónne číslo?&?telefonska številka?&?número telefónico?&?telefonnummer?&?telephone number?&?Handynummer?&?mobilnummer?&?мобилен номер?&?broj mobitela?&?αριθμός κινητού?&?číslo mobilního telefonu?&?mobilnummer?&?mobiili number?&?matkapuhelinnumero?&?numéro de portable?&?Handynummer?&?mobilnummer?&?αριθμός κινητού?&?mobil szám?&?uimhir Fón póca?&?numero di cellulare?&?Mobilā telefona numurs?&?Mobilus Numeris?&?Handysnummer?&?Handy Zuel?&?numru mobbli?&?mobiel nummer?&?numer komórkowy?&?número de celular?&?número de telemóvel?&?numar de mobil?&?mobilné číslo?&?Mobilna številka?&?Número de móvil?&?mobilnummer?&?Cellphone?&?Mobile number'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,4 -- 4 keyword
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)



 ------------------
 --bank code
 -----

 	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU IBAN Bank Code'
           ,'EU IBAN Bank Code'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (1
           ,'EU IBAN Bank Code'
           ,'EU IBAN Bank Code'
           ,0
           ,'(AT)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(BE)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(BG)\d{2}[ ][A-Z]{4}[ ]\d{4}[ ]\d{2}[A-Z,a-z,0-9]{2}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{2}?&?(HR)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d?&?(CY)\d{2}[ ]\d{4}[ ]\d{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}?&?(CZ)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(DK)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{2}?&?(EE)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(FI)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{2}?&?(FR)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{2}[A-Z,a-z,0-9]{2}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]\d{2}?&?(DE)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{2}?&?(GR)\d{2}[ ]\d{4}[ ]\d{3}[A-Z,a-z,0-9][ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{3}?&?(HU)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(IE)\d{2}[ ][A-Z,a-z,0-9]{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{2}?&?(IT)\d{2}[ ][A-Z]\d{3}[ ]\d{4}[ ]\d{3}[A-Z,a-z,0-9][ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{3}?&?(LV)\d{2}[ ][A-Z]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]?&?(LT)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(LU)\d{2}[ ]\d{3}[A-Z,a-z,0-9][ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}?&?(MT)\d{2}[ ][A-Z]{4}[ ]\d{4}[ ]\d[A-Z,a-z,0-9]{3}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{3}?&?(NL)\d{2}[ ][A-Z]{4}[ ]\d{4}[ ]\d{4}[ ]\d{2}?&?(PL)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(PT)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d?&?(RO)\d{2}[ ][A-Z]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}[ ][A-Z,a-z,0-9]{4}?&?(SK)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(SI)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{3}?&?(ES)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(SE)\d{2}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}?&?(GB)\d{2}[ ][A-Z]{4}[ ]\d{4}[ ]\d{4}[ ]\d{4}[ ]\d{2}'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,5
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)


	-----

	--eu id  number
	---


	
 	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU ID Number'
           ,'EU ID Number'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (1
           ,'EU ID Number'
           ,'EU ID Number'
           ,0
           ,'[0-9]{2}([ ]|[.]|[-])?(([0][0-9])|([1][0-2]))([ ]|[.]|[-])?(([0-2][0-9])|([3][0-1]))([ ]|[.]|[-])?[0-9]{3}([ ]|[.]|[-])?[0-9]{2}?&?[0-9]{2}([0,2,4][1-9]|[1,3,5][0-2])([0-2][0-9]|[3][0-1])[0-9]{4}?&?(HR)?[ ]?[[0-9]{11}?&?[0-9]{2}([0,5][1-9]|[1,6][0-2])([0-2][0-9]|[3][0-1])\/?[0-9]{4}?&?([0-2][0-9]|[3][0-1])([0][1-9]|[1][0-9]{2}][0-2])[0-9]{2}\-?[0-9]{4}?&?[1-6][0-9]{2}([0][1-9]|[1][0-2])([0-2][0-9]|[3][0-1])[0-9]{4}?&?([0-2][0-9]|[3][0-1])([0][0-9]|[1][0-2])[0-9]{2}([+]|[-]|[A])[0-9]{3}([0-9]|[A-Y])?&?[1-2][ ]?[0-9]{2}[ ]?([0][1-9]|[1][0-2])[ ]?[0-9]{5}[ ]?[0-9]{3}[ ]?([0-8][0-9]|[9][0-7])?&?[0-9]{2}(([0-2][0-9])|([3][0-1]))(([0][0-9])|([1][0-2]))[0-9]{2}[A-Z,ß,Ä,Ö,Ü][0-9]{3}?&?[ABEZHIKMNOPTYX]{2}\-?[0-9]{6}?&?(([0-2][0-9])|([3][0-1]))(([0][1-9])|([1][0-2]))[0-9]{7}?&?[0-8][ ]?[0-9]{2}(([0][0-9])|([1][0-2]))(([0-2][0-9])|([3][0-1]))[ ]?[0-9]{4}?&?[1-8][ ]?[0-9]{2}[0,1][0-9][0-9]{2}[ ]?[0-9]{4}?&?[0-9]{7}[A-Z]{2}?&?[A-Z]{6}[0-9]{2}[A-EHLMPR-T]((([0-2]|[4-6])[0-9])|([3,7][0-2]))[A-Z][0-9]{3}[A-Z]?&?([0-2][0-9]|[3][0-1])([0][1-9]|[1][0-2])[0-9]{2}\-?[0-2][0-9]{4}?&?[3-6][ ]?[0-9]{2}(([0][0-9])|([1][0-2]))(([0-2][0-9])|([3][0-1]))[ ]?[0-9]{3}[ ]?[0-9]?&?[1-2][8-9,0][0-9]{2}[ ]?(([0][1-9])|([1][0-2]))[ ]?(([0-2][0-9])|([3][0-1]))[ ]?[0-9]{3}[ ]?[0-9]{2}?&?[0-9]{7}[A,B,G,H,L,M,P,Z]?&?[0-9]{9}?&?[0-9]{2}([0,8,2,4,6][1-9]|[1,9,3,5,7][0-2])([0-2][0-9]|[3][0-1])[0-9]{5}?&?[1-9][0-9]{2}([0][1-9]|[1][0-2])([0-2][0-9]|[3][0-1])([0-4][0-9]|[5][0-2]|[9]{2})[0-9]{4}?&?[0-9]{2}([0,5][1-9]|[1,6][0-2])([0-2][0-9]|[3][0-1])\/?[0-9]{4}?&?[A-Z]{2}[ ]?[0-9]{6}?&?([0-2][0-9]|[3][0-1])([0][1-9]|[1][0-2])[0-9]{3}50[0-9]{4}?&?([0-9]|([,X,M,L,K,Y]\-?))[0-9]{7}\-?[A-H,J-N,P-T,V-Z]?&?([1][9]|[2][0])?[0-9]{2}([0][1-9]|[1][0-2])([0-2][0-9]|[3][0-1])\-?[0-9]{4}?&?[A-Z]{2}[ ]?[0-9]{2}[ ]?[0-9]{2}[ ]?[0-9]{2}[ ]?[ABCD]?&?(([46][0-9]{2})|([7][0][0-8]))([ ]|[-])?[0-9]{3}([ ]|[-])?[0-9]{4}?&?(([1-2][0-9])|([0][1-9])|([3][0-1]))(([0][ ]?[0-9])|([1][ ]?[0-2]))[0-9]{2}[ ]?[0-9]{4}?&?[3][2-9][0-9]([ ]|[-])?[0-9]{3}([ ]|[-])?[0-9]{4}'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,5
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)



-----
--EU ID Terms
----


	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU ID Terms'
           ,'EU ID Terms'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (7 -- 7 keyword
           ,'EU ID Terms'
           ,'EU ID Terms'
           ,0
           ,N'CCR-ID?&?Numéro de registre national?&?Rijksregisternummer?&?единен граждански номер?&?ЕГН?&?EGN?&?Osobna iskaznica?&?Δελτίο Ταυτότητας?&?Kimlik Kartı?&?Občanský Průkaz?&?Personal Identification Number?&?Da. CPR?&?Det Centrale Personregister?&?Personal Identification Code?&?isikukood?&?Personal Identity Code?&?Henkilötunnus?&?HETU?&?Personbeteckning?&?numéro d''inscription au répertoire?&?NIR?&?National Repertory registration?&?numéro de sécurité sociale?&?INSEE?&?Versicherungsnummer?&?Rentenversicherungsnummer?&?VSNR?&?RVNR?&?AMKA?&?Αριθμός Μητρώου Κοινωνικής Ασφάλισης?&?Személyi szám?&?társadalombiztosítási azonosító jel?&?TAJ vagy?&?TAJ-szám?&?Personal Public Service Number?&?PPS NoUimhir Phearsanta Seirbhíse Poiblí?&?Uimh. PSP?&?Codice fiscal?&?Personas kods?&?Asmens kodas?&?Burgerservicenummer?&?BSN?&?Citizen''s Service Number?&?Public Electronic Census System?&?Polish Powszechny Elektroniczny System Ewidencji Ludności?&?PESEL?&?Personal Numerical Code?&?Cod Numeric Personal?&?CNP?&?Birth number?&?Rodné číslo?&?RČ?&?Citizen''s Identification Card Number?&?Číslo občianskeho preukazu?&?ČOP?&?Enotna matična številka občana?&?EMŠO?&?Documento Nacional de Identidad?&?DNI?&?personnummer?&?NI Number?&?NINO?&?NHS Number?&?CHI number?&?Community Health Index?&?NHS Number'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,4 -- 4 keyword
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)


	-----
	-- EU Zip Codes
	--


	 	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU Zip Codes'
           ,'EU Zip Codes'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (1
           ,'EU Zip Codes'
           ,'EU Zip Codes'
           ,0
           ,'(AU[ ,-])?[1-9]\d{3}?&?(BE[ ,-])?[1-9]\d{3}?&?(BG[ ,-])?[1-9]\d{3}?&?(HR[ ,-])?[1-5]\d{4}?&?(CY[ ,-])?[1-9]\d{3}?&?(CZ[ ,-])?[1-7]\d{2}[ ]?\d{2}?&?(DK[ ,-])?\d{4}?&?(EE[ ,-])?\d{5}?&?(((FI)|(AX))[ ,-])?\d{4}[0,1,5]?&?(FR[ ,-])?\d{5}?&?(DE[ ,-])?\d{5}?&?(GR[ ,-])?\d{3}[ ]?\d{2}?&?(HU[ ,-])?[1-9]\d{3}?&?(IE[ ,-])[A,C-F,H,K,N,P,R,T,V-Y]\d{2}[ ,-][A,C-F,H,K,N,P,R,T,V-Y,0-9]{4}?&?(IT[ ,-])?\d{5}?&?(LV)[ ,-]\d{4}?&?(LT)[ ,-]\d{5}?&?(LU[ ,-])?[1-9]\d{3}?&?(MT[-, ])?(ATD|BBG|BKR|BML|BRG|BZN|DGL|FGR|FNT|FRN|GDJ|GHR|GRB|GSM|GSR|GXQ|GZR|HMR|IKL|ISL|KCM|KKP|KKR|KMN|LJA|LQA|MDN|MFN|MGR:MLH|MQB|MRS|MSD|MSK|MST|MTF|MTP|MXK|MXR|NDR|NXR|PBK|PLA|PTA|QLA|QRD|QRM|RBT|SCM|SFI|SGN|SGW|SLC|SLM|TP|SLZ|SNT|SPB|STJ|SVR|SWQ|TXN|VCT|VLT|XBX|XJR|XLN|XRA|XWK|ZBB|ZBG|ZBR|ZRQ|ZTN|)[ ,-]\d{4}?&?(NL[-, ])?\d{4}[ ,-][A-Z]{2}?&?(PL[-, ])?\d{2}[ ,-]\d{3}?&?(PT[-, ])?\d{4}[ ,-]\d{3}?&?(RO[-, ])?\d{6}?&?(SK[ ,-])?[0,8-9]\d{2}[ ]?\d{2}?&?(SI[ ,-])?[1-9]\d{3}?&?(ES[ ,-])?((0[1-9])|([1-4][0-9])|(5[0-2]))\d{3}?&?(SE[ ,-])?(([1-8]\d{2})|([9][0-7]\d)|([9][8][0-4]))\s\d{2}?&?(UK[ ,-])?(AB|AL|B|BA|BB|BD|BH|BL|BN|BR|BS|BT|CA|CB|CF|CH|CM|CO|CR|CT|CV|CW|DA|DD|DE|DG|DH|DL|DN|DT|DY|E|EC|EH|EN|EX|FK|FY|G|GL|GY|GU|HA|HD|HG|HP|HR|HS|HU|HX|IG|IM|IP|IV|JE|KA|KT|KW|KY|L|LA|LD|LE|LL|LN|LS|LU|M|ME|MK|ML|N|NE|NG|NN|NP|NR|NW|OL|OX|PA|PE|PH|PL|PO|PR|RG|RH|RM|S|SA|SE|SG|SK|SL|SM|SN|SO|SP|SR|SS|ST|SW|SY|TA|TD|TF|TN|TQ|TR|TS|TW|UB|W|WA|WC|WD|WF|WN|WR|WS|WV|YO|ZE)\d(\d|[A-Z])?\s\d[AB,D-H,J,L-N,P-U,V-Z]{2}'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,5
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)

-----
--EU Zip Terms
----


	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU Zip Terms'
           ,'EU Zip Terms'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (7 -- 7 keyword
           ,'EU Zip Terms'
           ,'EU Zip Terms'
           ,0
           ,N'Postleitzahl?&?Пощенски код?&?Poštanski broj?&?Poštovní směrovací číslo?&?poštovní kód?&?PSČ?&?postnummer?&?postiindeks?&?sihtnumber?&?Postinumero?&?code postal?&?Postleitzahl?&?ταχυδρομικός τομέας?&?irányítószám?&?Eircode?&?Codice postale?&?codice di avviamento postale?&?codice postale interno?&?CAP?&?CPI?&?Pasta indekss?&?Pašto indeksas?&?Postleitzuel?&?Kodiċi postali?&?postcode?&?Kod pocztowy?&?Código postal?&?codul poștal?&?poštové smerovacie číslo?&?Poštna številka?&?Código posta?&?postnummer?&?postcode?&?postal code?&?post code'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,4 -- 4 keyword
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)


	---license plate
	---

		 	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU License Plate'
           ,'EU License Plate'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (1
           ,'EU License Plate'
           ,'EU License Plate'
           ,0
           ,'(A|AM|B|BA|BB|BD|BG|BH|BL|BM|BN|BP|BR|BZ|DL|DO|E|EF|EU|FB|FE|FF|FK|FR|FV|G|GB|GD|GF|GK|GM|GR|GS|GU|HA|HB|HF|HL|HO|I|IL|IM|JE|JO|JU|JW|K|KB|KI|KF|KK|KL|KO|KR|KS|KU|L|LA|LB|LE|LF|LI|LL|LN|LZ|MA|MD|ME|MI|MT|MU|MZ|N|ND|NK|O|OP|OW|P|PE|PL|PT|RA|RE|RI|RO|S|SB|SD|SE|SK|SL|SP|SO|SR|ST|SV|SW|SZ|T|TA|TD|TK|TU|UU|V|VB|VD|VI|VK|VL|VO|W|WB|WD|WK|WE|WL|WN|WO|WT|WU|WY|WZ|ZE|ZT|ZW)[ ,∇,-][0-9][A-P,R-Z,0-9]{1,4}[A-P,R-Z]?&?([0-9][ ,-])?((([0-9]{3})[ ,-]([A-Z]{3}))|(([A-Z]{3})[ ,-]([0-9]{3})))?&?[A-C,E,H,K,M,O-P,T,X-Y]{1,2}[ ,-]\d{4}[ ,-][A-C,E,H,K,M,O-P,T,X-Y]{1,2}?&?(NG|ŽU|NA|ZG|MA|ZD|KŽ|VŽ|KT|VU|KR|VT|KC|VK|KA|ŠI|IM|ST|GS|SL|DU|SK|DJ|SB|DE|RI|DA|PŽ|ČK|PU|BJ|OG|BM|OS|)[ ,-][0-9]{3,4}[ ,-][A-Z]{1,2}?&?[A-Z]{3}[ ,-][0-9]{3}?&?[1-9][A-C,E,H,J-M,P,S-V,Z][A,S,0-9][ ,-][0-9]{4}?&?[A-Z]{2}[ ,-][0-9]{2}[ ,-]?[0-9]{3}?&?[0-9]{3}[ ,-][A-Z]{3}?&?[A-Z]{3}[ ,-][0-9]{3}?&?[A-H,J-N,P-T,V-Z]{2}[ ,-][0-9]{3}[ ,-][A-H,J-N,P-T,V-Z]{2}?&?(((B|C|D|E|F|G|H|J|K|L|M|N|O|P|R|S|V|W|X|Y|Z)[ ,-][0-9,A-Z]{7})|((AB|AC|AH|AE|AK|AL|AM|AN|AÖ|AP|AR|AS|AT|AU|AW|AZ|BA|BB|BC|BD|BE|BF|BG|BH|BI|BK|BL|BM|BN|BO|BÖ|BP|BR|BS|BT|BU|BW|BZ|CA|CB|CE|CO|CR|CW|DA|DB|DD|DE|DH|DI|DL|DM|DN|DO|DS|DT|DU|DW|DZ|EA|EB|ED|EE|EF|EG|EH|EI|EL|EM|EN|ER|ES|EU|EW|FB|FD|FF|FG|FH|FI|FL|FN|FO|FR|FS|FT|FÜ|FW|FZ|GA|GC|GD|GE|GF|GG|GI|GK|GL|GM|GN|GÖ|GP|GR|GS|GT|GÜ|GV|GW|GZ|HA|HB|HC|HD|HE|HF|HG|HH|HI|HL|HM|HN|HO|HP|HR|HS|HU|HV|HW|HX|HY|HZ|IK|IL|IN|IS|IZ|JB|JE|JL|KA|KB|KC|KE|KF|KG|KH|KI|KK|KL|KM|KN|KO|KR|KS|KT|KU|KW|KY|LA|LB|LC|LD|LE|LF|LG|LH|LI|LK|LL|LM|LN|LÖ|LP|LR|LS|LU|MA|MB|MC|MD|ME|MG|MH|MI|MK|ML|MM|MN|MO|MQ|MR|MS|MT|MÜ|MW|MY|MZ|NB|ND|NE|NF|NH|NI|NK|NL|NM|NÖ|NP|NR|NU|NT|NW|NY|NZ|OA|OB|OC|OD|OE|OF|OG|OH|OK|OL|OP|OR|OS|OZ|PA|PB|PE|PF|PI|PK|PL|PM|PN|PR|PS|PW|PZ|RA|RC|RD|RE|RG|RH|RI|RL|RM|RN|RO|RS|RT|RV|RW|RY|RZ|SB|SC|SE|SF|SG|SH|SI|SK|SL|SM|SN|SO|SP|SR|ST|SU|SW|SY|SZ|TE|TF|TG|TO|TP|TR|TS|TT|TÜ|ÜB|UE|UH|UL|UM|UN|VB|VK|VL|VS|WA|WB|WD|WE|WF|WG|WI|WK|WL|WM|WN|WO|WR|WS|WT|WÜ|WW|WZ|ZE|ZI|ZP|ZR|ZS|ZW|ZZ)[ ,-][A-Z,0-9]{6})|((ABG|AIB|AIC|ALF|ALS|ALZ|ANA|ANG|ANK|APD|ARN|ART|ASD|ASL|ASZ|AUR|AZE|BAD|BAR|BBG|BBL|BCH|BED|BEI|BEL|BER|BGD|BGL|BID|BIN|BIR|BIT|BIW|BKS|BLB|BLK|BNA|BOG|BOH|BOR|BOT|BRA|BRB|BRG|BRI|BRK|BRL|BRV|BSB|BSK|BTF|BÜD|BUL|BUR|BÜS|BÜZ|BWL|BYL|BZA|CAS|CHA|CLP|CLZ|COC|COE|CUX|DAH|DAN|DAU|DBR|DEG|DEL|DGF|DIL|DIN|DIZ|DKB|DLG|DON|DUD|DÜW|EBE|EBN|EBS|ECK|EHI|EIC|EIH|EIL|EIN|EMD|EMS|ERB|ERH|ERK|ESA|ESB|ESW|EUT|FAL|FDB|FDS|FEU|FFB|FKB|FLÖ|FOR|FRG|FRI|FRW|FTL|FÜS|GAN|GAP|GDB|GEL|GEM|GEO|GER|GHA|GHC|GLA|GMN|GNT|GOA|GOH|GRA|GRI|GRM|GRS|GRZ|GTH|GUB|GUN|GVM|HAB|HAL|HAM|HAS|HBN|HBS|HCH|HDH|HDL|HEB|HEF|HEI|HEL|HER|HET|HGN|HGW|HHM|HIG|HIP|HMU|HOG|HOH|HOL|HOM|HOR|HÖS|HOT|HRO|HSK|HST|HÜN|HUS|HVL|HWI|IGB|ILL|JEV|JÜL|KAR|KEH|KEL|KEM|KIB|KLE|KLT|KLZ|KÖN|KÖT|KRU|KÜN|KUS|KYF|LAN|LAT|LAU|LBS|LBZ|LDK|LDS|LEO|LER|LEV|LIB|LIF|LIN|LIP|LÖB|LOH|LOS|LSA|LSN|LSZ|LÜD|LUK|LÜN|LWL|MAB|MAI|MAK|MAL|MAR|MED|MEG|MEI|MEK|MEL|MEP|MER|MES|MET|MGH|MGN|MHL|MIL|MOD|MOL|MON|MOS|MSP|MST|MTK|MTL|MÜB|MÜL|MÜN|MÜR|MVL|MYK|MZG|NAB|NAI|NAU|NDH|NEA|NEB|NEC|NEN|NES|NEU|NEW|NIB|NMB|NMS|NOH|NOL|NOM|NOR|NRÜ|NRW|NVP|NWM|OAL|OBB|OBG|OCH|OHA|ÖHR|OHV|OHZ|OLD|OPR|OSL|OTT|OTW|OVI|OVL|OVP|PAF|PAN|PAR|PCH|PEG|PER|PIR|PLÖ|PRÜ|QFT|QLB|RDG|REG|REH|REI|RID|RIE|ROD|ROF|ROH|ROK|ROL|ROS|ROT|ROW|RPL|RSL|RÜD|RÜG|RWL|SAB|SAD|SÄK|SAL|SAN|SAW|SBG|SBK|SCZ|SDH|SDL|SDT|SEB|SEE|SEF|SEL|SFA|SFB|SFT|SGH|SHA|SHG|SHK|SHL|SIG|SIM|SLE|SLF|SLG|SLN|SLS|SLÜ|SLZ|SMÜ|SNH|SOB|SOG|SOK|SOL|SÖM|SON|SPB|SPN|SPR|SRB|SRO|STA|STB|STD|STE|STH|STL|STO|SUL|SÜW|SWA|SZB|TBB|THL|THW|TIR|TÖL|TÖN|TUT|UEM|UER|UFF|USI|VAI|VEC|VER|VIB|VIE|VIT|VOF|VOH|WAF|WAK|WAN|WAR|WAT|WBS|WDA|WEB|WEG|WEL|WEM|WEN|WER|WES|WHV|WIL|WIS|WIT|WIZ|WLG|WMS|WND|WOB|WOH|WOL|WOR|WOS|WRN|WSF|WST|WSW|WTL|WTM|WUG|WUM|WUN|WUR|WZL|ZEL|ZIG)[ ,-][A-Z,0-9]{5}))?&?[A-B,E,H-I,K,M-P,T,X-Z]{3}[ ,-]\d{4}?&?[A-N,P,R-Z][A-P,R-Z][A-H,J-N,P,R-Z][ ,-][0-9]{3}?&?\d{2}[1,2][ ,-](WD|TS|TN|LK|WW|WX|WH|W|T|SO|RN|OY|MO|MN|MH|LS|LM|LH|LD|L|KY|KK|KE|G|DL|D|CW|CN|CE|C)[ ,-]\d{4,6}?&?[A-Z]{2}[ ,-]\d{3}[A-Z]{2}?&?[A-Z]{2}[ ,-]\d{3,4}?&?[A-Z]{3}[ ,-]\d{3}?&?[A-H,J-N,P-Z]{2}[ ,-]\d{4}?&?[A-Z]{3}[ ,-]\d{3}?&?([A-Z]{2}|\d{2})[ ,-]([A-Z]{2}|\d{2})[ ,-]([A-Z]{2}|\d{2})|(\d[ ,-](([A-B,D,F-H,J-N,P,R-T,V-Z]{3}[ ,-]\d{2})|([A-B,D,F-H,J-N,P,R-T,V-Z]{2}[ ,-]\d{3})))|(\d{2}[ ,-][A-B,D,F-H,J-N,P,R-T,V-Z]{3}[ ,-]\d)|(\d{3}[ ,-][A-B,D,F-H,J-N,P,R-T,V-Z]{2}[ ,-]\d)|[A-B,D,F-H,J-N,P,R-T,V-Z][ ,-](\d{3}[ ,-]([A-B,D,F-H,J-N,P,R-T,V-Z]{2})|\d{2}[ ,-][A-B,D,F-H,J-N,P,R-T,V-Z]{3})|([A-B,D,F-H,J-N,P,R-T,V-Z]{2}[ ,-]\d{3}[ ,-][A-B,D,F-H,J-N,P,R-T,V-Z])|([A-B,D,F-H,J-N,P,R-T,V-Z]{3}[ ,-]\d{2}[ ,-][A-B,D,F-H,J-N,P,R-T,V-Z])?&?((ZS|ZK|PZ|PP|PN|PL|PK|NO|NE|TK|SZ|SY|SW|ST|SL|SR|SO|SM|SL|SK|SJ|SI|SH|SG|SD|SC|SB|GS|GD|GA|BS|BL|BI|RZ|RT|RP|RK|OP|OK|OB|WZ|WY|WX|WX|WW|WW|WW|WV|WU|WT|WS|WR|WP|WO|WN|WM|WL|WK|WJ|WI|WH|WG|WF|WE|WD|WB|WA|KT|KN|FZ|FG|LZ|LU|LC|LB|ES|EP|EL|CW|CT|CG|CB|DW|DL|DJ|ZS|ZK|PZ|PP|PN|PL|PK|NO|NE|TK|SZ|SY|SW|ST|SL|SR|SO|SM|SL|SK|SJ|SI|SH|SG|SD|SC|SB|GS|GD|GA|BS|BL|BI|RZ|RT|RP|RK|OP|OK|OB|WZ|WY|WX|WX|WW|WW|WW|WV|WU|WT|WS|WR|WP|WO|WN|WM|WL|WK|WJ|WI|WH|WG|WF|WE|WD|WB|WA|KT|KN|FZ|FG|LZ|LU|LC|LB|ES|EP|EL|CW|CT|CG|CB|DW|DL|DJ|DB)[ ,-][0-9,A-Z]{5})|((ZWA|ZSZ|ZSW|ZST|ZSL|ZSD|ZPY|ZPL|ZMY|ZLO|ZKO|ZKL|ZKA|ZGY|ZGR|ZGL|ZDR|ZCH|ZBI|PZL|PWR|PWL|PWA|PTU|PSZ|PSR|PSL|PSE|PRA|PPL|POZ|POT|POS|POB|PO,|PNT|PMI|PLE|PKS|PKR|PKO|PKN|PKL|PKE|PKA|PJA|PGS|PGO|PGN|PCT|PCH|NWE|NSZ|NPI|NOS|NOL|NOG|NOE|NNM|NNI|NMR|NLI|NKE|NIL|NGO|NGI|NEL|NEB|NDZ|NBR|NBA|TSZ|TST|TSK|TSA|TPI|TOS|TOP|TLW|TKN|TKI|TKA|TJE|TBU|SZY|SZO|SZA|SWD|STY|STA|SRC|SRB|SPS|SPI|SMY|SMI|SLU|SKL|SJZ|SGL|SCZ|SCI|SBL|SBI|SBE|GWE|GTC|GSZ|GST|GSP|GSL|GPU|GND|GMB|GLE|GKW|GKS|GKA|GDA|GCZ|GCH|GBY|BZA|BWM|BSU|BSK|BSI|BSE|BMN|BLM|BKL|BIA|BHA|BGR|BBI|BAU|RZE|RTA|RST|RSR|RSA|RRS|RPZ|RPR|RNI|RMI|RLU|RLS|RLE|RLA|RKR|RKL|RJS|RJA|RDE|RBR|RBI|OST|OPR|OPO|OOL|ONY|ONA|OKR|OKL|OGL|WZY|WZW|WZU|WWY|WWL|WWE|WSZ|WSK|WSI|WSE|WSC|WRA|WPZ|WPY|WPU|WPR|WPN|WPL|WPI|WOT|WOS|WOR|WND|WML|WMA|WLS|WLI|WKZ|WGS|WGR|WGM|WCI|WBR|KWI|KWA|KTT|KTA|KSU|KRA|KR,|KPR|KOS|KOL|KNT|KNS|KMY|KMI|KLI|KGR|KDA|KCH|KBR|KBC|FZI|FZG|FZA|FWS|FSW|FSU|FSL|FSD|FNW|FMI|FKR|FGW|LZA|LWL|LUB|LTM|LSW|LRY|LRA|LPU|LPA|LOP|LLU|LLE|LLB|LKS|LKR|LJA|LHR|LCH|LBL|LBI|EZG|EZD|EWI|EWE|ETM|ESK|ESI|ERW|ERA|EPJ|EPI|EPD|EPA|EOP|ELW|ELE|ELC|ELA|EKU|EBR|EBE|CZN|CWL|CWA|CTU|CTR|CSW|CSE|CRY|CRA|CNA|CMG|CLI|CIN|CGR|CGD|CCH|CBY|CBR|CAL|DZL|DZG|DZA|DWR|DWL|DTR|DSW|DST|DSR|DPL|DOL|DOA|DMI|DLW|DLU|DLE|DLB|DKL|DKA|DJE|DJA|DGR|DGL|DDZ|DBL|DBA)[ ,-][0-9,A-Z]{4})?&?([A-Z]{2}[ ,-]\d{2}[ ,-]\d{2})|(\d{2}[ ,-]\d{2}[ ,-][A-Z]{2})|(\d{2}[ ,-][A-Z]{2}[ ,-]\d{2})?&?(VS|VN|VL|TR|TM|TL|SV|SM|SJ|SB|PH|OT|NT|MS|MM|MH|IS|IL|IF|HR|HD|GR|GL|GJ|DJ|DB|CV|CT|CS|CL|CJ|BZ|BV|BT|BR|BN|BH|BC|B|AR|AG|AB)[ ,-][0-9]{2,3}[ ,-][A-P,R-Z]{3}?&?(BA|BD|BE|BI|BL|BT|BB|BC|BK|BJ|BN|BR|BS|BY|CA|DK|DS|DT|GA|GL|HC|HE|IL|KA|KE|KC|KI|KK|KM|KN|KS|LC|LE|LM|LV|MA|MI|ML|MT|MY|NR|NI|NT|NM|NO|NZ|PB|PD|PE|PK|PN|PO|PV|PS|PP|PT|PU|RA|RK|RS|RV|SA|SB|SC|SE|SI|SK|SL|SN|SO|SP|SV|TT|TA|TB|TN|TC|TE|TO|TR|TS|TV|VK|VT|ZA|ZI|ZL|ZC|ZH|ZM|ZV)[ ,-]\d{3}[ ,-]?[A-Z]{2}?&?(CE|GO|KK|KP|KR|LJ|MB|MS|NM|PO|SG)[ ,-](([0-9,A-Z]{2}[ ,-][0-9,A-Z]{3})|([0-9,A-Z]{3}[ ,-][0-9,A-Z]{2}))?&?[0-9]{4}[ ,-][B-D,F-H,J-N,P,R-T,V-Z]{3}?&?[A-P,R-U,W-Z]{3}[ ,-][0-9]{3}?&?[A-H,J-P,R-Y]{2}[ ,-]?\d{2}[ ,-][A-H,J-P,R-Z]{3}'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,5
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)


-----
--EU License Plate Terms
----


	INSERT INTO [whiteops].[dc_policy_rule]
           ([name]
           ,[description]
           ,[type_id]
           ,[is_active]
           ,[last_modified]
           ,[last_events_cache]
           ,[smtp_responses_to_resource_owners]
           ,[threshold]
           ,[timeframe]
           ,[is_system]
           ,[is_internal])
     VALUES
           ('EU License Plate Terms'
           ,'EU License Plate Terms'
           ,1
           ,1
           ,NULL
           ,NULL
           ,0
           ,15
           ,30
           ,1
           ,0)
	
	set @RuleId = SCOPE_IDENTITY();

	 INSERT INTO [whiteops].[dc_policy_rule_category]
           ([dc_policy_rule_id]
           ,[category_id])
     VALUES
           (@RuleId
           ,@CategoryID)


	INSERT INTO [whiteops].[dc_policy_object]
           ([dc_field_sub_type_id]
           ,[name]
           ,[description]
           ,[user_defined]
           ,[value]
           ,[is_displayed]
           ,[mask_values_start]
           ,[mask_values_end]
           ,[verifier_id])
     VALUES
           (7 -- 7 keyword
           ,'EU License Plate Terms'
           ,'EU License Plate Terms'
           ,0
           ,N'Nummernschild?&?Behördliches Kennzeichen?&?Plaque d''immatriculation?&?Регистрационен номер?&?Licencna ploča?&?Πινακίδα κυκλοφορίας?&?plaka?&?Poznávací značka?&?Nummerplade?&?Numbrimärk?&?Rekisterikilpi?&?Plaque d''immatriculation?&?Nummernschild?&?Kraftfahrzeugkennzeich?&?Πινακίδα κυκλοφορίας?&?Rendszámtábla?&?Pláta ceadúnais?&?targa?&?Numurzīme?&?Valstybinis numeris?&?Lizenz Risotto?&?pjanċa liċenzja?&?kentekenplaat?&?tablica rejestracyjna?&?placa?&?număr de înmatriculare?&?poznávacia značka?&?evidenčné číslo vozidla?&?registrske tablice?&?placa?&?registreringsskylt?&?license plate?&?number plate'
           ,1
           ,0
           ,4
           ,NULL)

	set @POId1 = SCOPE_IDENTITY();

	INSERT INTO [whiteops].[dc_policy_atomic_rule]
			   ([dc_policy_rule_id]
			   ,[wpc_field_id]
			   ,[operator_type_id]
			   ,[right_values]
			   ,[dc_field_id]
			   ,[dc_operator_type_id]
			   ,[dc_policy_object_id])
		 VALUES
			   (@RuleId
			   ,NULL
			   ,NULL
			   ,NULL
			   ,4 -- 4 keyword
			   ,38
			   ,@POId1)


	INSERT INTO [whiteops].[dc_policy_rule_dc_policy]
           ([dc_policy_id]
           ,[dc_policy_rule_id]
           ,[is_rule_active])
     VALUES
           (@PolicyId
           ,@RuleId
           ,1)


	COMMIT

END TRY
BEGIN CATCH

	WHILE (@@TRANCOUNT > 0)
	BEGIN 
		ROLLBACK;
	END

	DECLARE @ErrorMessage NVARCHAR(MAX);
	DECLARE @ErrorSeverity int;
	DECLARE @ErrorState  int; 

	SELECT
		@ErrorMessage = ERROR_MESSAGE(),
		@ErrorSeverity = ERROR_SEVERITY(),
		@ErrorState = ERROR_STATE();

	RAISERROR (
		@ErrorMessage,
		@ErrorSeverity,
		@ErrorState
	);
END CATCH

