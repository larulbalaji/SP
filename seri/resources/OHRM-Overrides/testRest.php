<?php

		$url="http://192.168.20.1:8080/identityiq/rest/workflows/Trigger%20HR%20Processing/launch";
		$username="spadmin";
		$password="admin";
		
		$subdata = array("foo1" => "bar");
		$data = array("workflowArgs" => $subdata);                                                                    
		$data_string = json_encode($data); 
		
		$curl=curl_init($url);
		curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($curl,CURLOPT_POST, 1);
		curl_setopt($curl, CURLOPT_USERPWD, $username . ":" . $password);
		
		curl_setopt($curl, CURLOPT_POSTFIELDS, $data_string);
		curl_setopt($curl, CURLOPT_HTTPHEADER, array(                                                                          
			'Content-Type: application/json',                                                                                
			'Content-Length: ' . strlen($data_string))                                                                       
		);  
		
		$result = curl_exec($curl);
		error_log("from " . $url);
		if ( curl_errno($curl) ) {
		  error_log("errno=" . curl_errno($curl) );
		} else {
		  error_log("No Errno");
		}
		error_log("length=".strlen($result) );
		error_log("Sample: " . substr($result, 0, 20) ); 
		error_log("Curl Returns " . $result);

		curl_close($curl);
?>